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
package com.facebook.presto.util;

import com.facebook.presto.spi.ColumnMetadata;
import com.facebook.presto.spi.RecordCursor;
import com.google.common.base.Splitter;
import io.airlift.slice.Slices;
import org.testng.annotations.Test;

import static com.facebook.presto.spi.type.VarcharType.VARCHAR;
import static com.google.common.io.CharStreams.newReaderSupplier;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class TestDelimitedRecordIterable
{
    @Test
    public void testExtraction()
            throws Exception
    {
        DelimitedRecordSet recordIterable = new DelimitedRecordSet(
                newReaderSupplier("apple,fuu,123\nbanana,bar,456"),
                Splitter.on(','),
                new ColumnMetadata("fruit", VARCHAR, 0, false),
                new ColumnMetadata("foo", VARCHAR, 1, false),
                new ColumnMetadata("value", VARCHAR, 2, false));

        RecordCursor cursor = recordIterable.cursor();
        assertTrue(cursor.advanceNextPosition());
        assertEquals(cursor.getSlice(0), Slices.utf8Slice("apple"));
        assertEquals(cursor.getSlice(1), Slices.utf8Slice("fuu"));
        assertEquals(cursor.getSlice(2), Slices.utf8Slice("123"));
        assertEquals(cursor.getLong(2), 123L);
        assertEquals(cursor.getDouble(2), 123.0);

        assertTrue(cursor.advanceNextPosition());
        assertEquals(cursor.getSlice(0), Slices.utf8Slice("banana"));
        assertEquals(cursor.getSlice(1), Slices.utf8Slice("bar"));
        assertEquals(cursor.getSlice(2), Slices.utf8Slice("456"));
        assertEquals(cursor.getLong(2), 456L);
        assertEquals(cursor.getDouble(2), 456.0);

        assertFalse(cursor.advanceNextPosition());
    }
}
