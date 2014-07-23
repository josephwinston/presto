/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.hive.util;

import com.fasterxml.jackson.core.Base64Variants;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.UnionObject;
import org.apache.hadoop.hive.serde2.objectinspector.UnionObjectInspector;
import org.apache.hadoop.io.BytesWritable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.testng.annotations.Test;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.facebook.presto.hive.util.SerDeUtils.getJsonBytes;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory.ObjectInspectorOptions;
import static org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory.getReflectionObjectInspector;
import static org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory.getStandardUnionObjectInspector;
import static org.apache.hadoop.hive.serde2.objectinspector.StandardUnionObjectInspector.StandardUnion;
import static org.testng.Assert.assertEquals;

@SuppressWarnings("PackageVisibleField")
public class TestSerDeUtils
{
    private static final DateTimeZone SESSION_TIME_ZONE = DateTimeZone.forID("Europe/Berlin");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(SESSION_TIME_ZONE);

    private static class ListHolder
    {
        List<InnerStruct> array;
    }

    private static class InnerStruct
    {
        public InnerStruct(Integer intVal, Long longVal)
        {
            this.intVal = intVal;
            this.longVal = longVal;
        }

        Integer intVal;
        Long longVal;
    }

    private static class OuterStruct
    {
        Byte byteVal;
        Short shortVal;
        Integer intVal;
        Long longVal;
        Float floatVal;
        Double doubleVal;
        String stringVal;
        byte[] byteArray;
        List<InnerStruct> structArray;
        Map<String, InnerStruct> map;
        InnerStruct innerStruct;
    }

    private static ObjectInspector getInspector(Type type)
    {
        return getReflectionObjectInspector(type, ObjectInspectorOptions.JAVA);
    }

    @Test
    public void testPrimitiveJsonString()
    {
        // boolean
        String expectedBoolean = "true";
        String actualBoolean = toJsonString(true, getInspector(Boolean.class));
        assertEquals(actualBoolean, expectedBoolean);

        // byte
        String expectedByte = "5";
        String actualByte = toJsonString((byte) 5, getInspector(Byte.class));
        assertEquals(actualByte, expectedByte);
        // short
        String expectedShort = "2";
        String actualShort = toJsonString((short) 2, getInspector(Short.class));
        assertEquals(actualShort, expectedShort);

        // int
        String expectedInt = "1";
        String actualInt = toJsonString(1, getInspector(Integer.class));
        assertEquals(actualInt, expectedInt);

        // long
        String expectedLong = "10";
        String actualLong = toJsonString(10L, getInspector(Long.class));
        assertEquals(actualLong, expectedLong);

        // float
        String expectedFloat = "20.0";
        String actualFloat = toJsonString(20.0f, getInspector(Float.class));
        assertEquals(actualFloat, expectedFloat);

        // double
        String expectedDouble = "30.12";
        String actualDouble = toJsonString(30.12d, getInspector(Double.class));
        assertEquals(actualDouble, expectedDouble);

        // string
        String expectedString = "\"abdd\"";
        String actualString = toJsonString("abdd", getInspector(String.class));
        assertEquals(actualString, expectedString);

        // timestamp
        DateTime dateTime = new DateTime(2008, 10, 28, 16, 7, 15, 0);
        String expectedTimestamp = "\"" + TIMESTAMP_FORMAT.print(dateTime) + "\"";
        String actualTimestamp = toJsonString(new Timestamp(dateTime.getMillis()), getInspector(Timestamp.class));
        assertEquals(actualTimestamp, expectedTimestamp);

        // binary
        byte[] byteArray = {81, 82, 84, 85};
        String expectedBinary = "\"UVJUVQ==\"";
        String actualBinary = toJsonString(byteArray, getInspector(byte[].class));
        assertEquals(actualBinary, expectedBinary);
    }

    @Test
    public void testListJsonString()
    {
        List<InnerStruct> mArray = new ArrayList<>(2);
        InnerStruct is1 = new InnerStruct(8, 9L);
        InnerStruct is2 = new InnerStruct(10, 11L);
        mArray.add(is1);
        mArray.add(is2);
        ListHolder listHolder = new ListHolder();
        listHolder.array = mArray;

        String actual = toJsonString(listHolder, getInspector(ListHolder.class));
        String expected = "{\"array\":[" +
                "{\"intval\":8,\"longval\":9}," +
                "{\"intval\":10,\"longval\":11}]}";
        assertEquals(actual, expected);
    }

    private static class MapHolder
    {
        Map<String, InnerStruct> map;
    }

    @Test
    public void testMapJsonString()
    {
        MapHolder holder = new MapHolder();
        holder.map = new TreeMap<>();
        holder.map.put("twelve", new InnerStruct(13, 14L));
        holder.map.put("fifteen", new InnerStruct(16, 17L));
        String actual = toJsonString(holder, getInspector(MapHolder.class));
        String expected = "{\"map\":{" +
                "\"fifteen\":{\"intval\":16,\"longval\":17}," +
                "\"twelve\":{\"intval\":13,\"longval\":14}}}";
        assertEquals(actual, expected);
    }

    @Test
    public void testStructJsonString()
    {
        // test simple structs
        InnerStruct innerStruct = new InnerStruct(13, 14L);
        String actual = toJsonString(innerStruct, getInspector(InnerStruct.class));
        String expected = "{\"intval\":13,\"longval\":14}";
        assertEquals(actual, expected);

        // test complex structs
        OuterStruct outerStruct = new OuterStruct();
        outerStruct.byteVal = 1;
        outerStruct.shortVal = 2;
        outerStruct.intVal = 3;
        outerStruct.longVal = 4L;
        outerStruct.floatVal = 5.01f;
        outerStruct.doubleVal = 6.001d;
        outerStruct.stringVal = "seven";
        outerStruct.byteArray = new byte[] {'2'};
        InnerStruct is1 = new InnerStruct(2, -5L);
        InnerStruct is2 = new InnerStruct(-10, 0L);
        outerStruct.structArray = new ArrayList<>(2);
        outerStruct.structArray.add(is1);
        outerStruct.structArray.add(is2);
        outerStruct.map = new TreeMap<>();
        outerStruct.map.put("twelve", new InnerStruct(0, 5L));
        outerStruct.map.put("fifteen", new InnerStruct(-5, -10L));
        outerStruct.innerStruct = new InnerStruct(18, 19L);

        actual = toJsonString(outerStruct, getInspector(OuterStruct.class));
        expected = "{" +
                "\"byteval\":1," +
                "\"shortval\":2," +
                "\"intval\":3," +
                "\"longval\":4," +
                "\"floatval\":5.01," +
                "\"doubleval\":6.001," +
                "\"stringval\":\"seven\"," +
                "\"bytearray\":\"Mg==\"," +
                "\"structarray\":[" +
                "{\"intval\":2,\"longval\":-5}," +
                "{\"intval\":-10,\"longval\":0}]," +
                "\"map\":{" +
                "\"fifteen\":{\"intval\":-5,\"longval\":-10}," +
                "\"twelve\":{\"intval\":0,\"longval\":5}}," +
                "\"innerstruct\":{\"intval\":18,\"longval\":19}}";

        assertEquals(actual, expected);
    }

    @Test
    public void testUnionJsonString()
    {
        UnionObjectInspector unionInspector = getStandardUnionObjectInspector(ImmutableList.of(getInspector(InnerStruct.class)));

        UnionObject union = new StandardUnion((byte) 0, new InnerStruct(1, 2L));
        String actual = toJsonString(union, unionInspector);
        String expected = "{\"0\":{\"intval\":1,\"longval\":2}}";
        assertEquals(actual, expected);
    }

    @Test
    public void testReuse()
            throws Exception
    {
        BytesWritable value = new BytesWritable();

        byte[] first = "hello world".getBytes(UTF_8);
        value.set(first, 0, first.length);

        byte[] second = "bye".getBytes(UTF_8);
        value.set(second, 0, second.length);

        Type type = new TypeToken<Map<BytesWritable, Integer>>() {}.getType();
        ObjectInspector inspector = getReflectionObjectInspector(type, ObjectInspectorOptions.JAVA);

        byte[] bytes = getJsonBytes(SESSION_TIME_ZONE, ImmutableMap.of(value, 0), inspector);

        String encoded = Base64Variants.getDefaultVariant().encode(second);
        assertEquals(new String(bytes, UTF_8), "{\"" + encoded + "\":0}");
    }

    private static String toJsonString(Object object, ObjectInspector inspector)
    {
        return new String(getJsonBytes(SESSION_TIME_ZONE, object, inspector), UTF_8);
    }
}
