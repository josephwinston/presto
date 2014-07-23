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
package com.facebook.presto.block;

import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.spi.block.Block;
import com.facebook.presto.spi.block.BlockBuilder;
import com.facebook.presto.spi.block.BlockBuilderStatus;
import com.facebook.presto.spi.type.Type;
import io.airlift.slice.Slice;
import io.airlift.slice.Slices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.facebook.presto.block.BlockIterables.createBlockIterable;
import static com.facebook.presto.spi.type.BigintType.BIGINT;
import static com.facebook.presto.spi.type.BooleanType.BOOLEAN;
import static com.facebook.presto.spi.type.DoubleType.DOUBLE;
import static com.facebook.presto.spi.type.TimeZoneKey.UTC_KEY;
import static com.facebook.presto.spi.type.VarcharType.VARCHAR;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.testng.Assert.assertEquals;

public final class BlockAssertions
{
    public static final ConnectorSession SESSION = new ConnectorSession("user", "source", "catalog", "schema", UTC_KEY, Locale.ENGLISH, "address", "agent");

    private BlockAssertions()
    {
    }

    public static Object getOnlyValue(Block block)
    {
        assertEquals(block.getPositionCount(), 1, "Block positions");
        return block.getObjectValue(SESSION, 0);
    }

    public static List<Object> toValues(BlockIterable blocks)
    {
        List<Object> values = new ArrayList<>();
        for (Block block : blocks) {
            for (int position = 0; position < block.getPositionCount(); position++) {
                values.add(block.getObjectValue(SESSION, position));
            }
        }
        return Collections.unmodifiableList(values);
    }

    public static List<Object> toValues(Block block)
    {
        List<Object> values = new ArrayList<>();
        for (int position = 0; position < block.getPositionCount(); position++) {
            values.add(block.getObjectValue(SESSION, position));
        }
        return Collections.unmodifiableList(values);
    }

    public static void assertBlockEquals(Block actual, Block expected)
    {
        assertEquals(actual.getType(), expected.getType());

        for (int position = 0; position < actual.getPositionCount(); position++) {
            assertEquals(actual.getObjectValue(SESSION, position), expected.getObjectValue(SESSION, position));
        }
    }

    public static Block createStringsBlock(String... values)
    {
        checkNotNull(values, "varargs 'values' is null");

        return createStringsBlock(Arrays.asList(values));
    }

    public static Block createStringsBlock(Iterable<String> values)
    {
        BlockBuilder builder = VARCHAR.createBlockBuilder(new BlockBuilderStatus());

        for (String value : values) {
            if (value == null) {
                builder.appendNull();
            }
            else {
                builder.appendSlice(Slices.utf8Slice(value));
            }
        }

        return builder.build();
    }

    public static Block createStringSequenceBlock(int start, int end)
    {
        BlockBuilder builder = VARCHAR.createBlockBuilder(new BlockBuilderStatus());

        for (int i = start; i < end; i++) {
            builder.appendSlice(Slices.utf8Slice(String.valueOf(i)));
        }

        return builder.build();
    }

    public static Block createBooleansBlock(Boolean... values)
    {
        checkNotNull(values, "varargs 'values' is null");

        return createBooleansBlock(Arrays.asList(values));
    }

    public static Block createBooleansBlock(Boolean value, int count)
    {
        return createBooleansBlock(Collections.nCopies(count, value));
    }

    public static Block createBooleansBlock(Iterable<Boolean> values)
    {
        BlockBuilder builder = BOOLEAN.createBlockBuilder(new BlockBuilderStatus());

        for (Boolean value : values) {
            if (value == null) {
                builder.appendNull();
            }
            else {
                builder.appendBoolean(value);
            }
        }

        return builder.build();
    }

    // This method makes it easy to create blocks without having to add an L to every value
    public static Block createLongsBlock(int... values)
    {
        BlockBuilder builder = BIGINT.createBlockBuilder(new BlockBuilderStatus());

        for (int value : values) {
            builder.appendLong((long) value);
        }

        return builder.build();
    }

    public static Block createLongsBlock(Long... values)
    {
        checkNotNull(values, "varargs 'values' is null");

        return createLongsBlock(Arrays.asList(values));
    }

    public static Block createLongsBlock(Iterable<Long> values)
    {
        BlockBuilder builder = BIGINT.createBlockBuilder(new BlockBuilderStatus());

        for (Long value : values) {
            if (value == null) {
                builder.appendNull();
            }
            else {
                builder.appendLong(value);
            }
        }

        return builder.build();
    }

    public static Block createLongSequenceBlock(int start, int end)
    {
        BlockBuilder builder = BIGINT.createBlockBuilder(new BlockBuilderStatus());

        for (int i = start; i < end; i++) {
            builder.appendLong(i);
        }

        return builder.build();
    }

    public static Block createBooleanSequenceBlock(int start, int end)
    {
        BlockBuilder builder = BOOLEAN.createBlockBuilder(new BlockBuilderStatus());

        for (int i = start; i < end; i++) {
            builder.appendBoolean(i % 2 == 0);
        }

        return builder.build();
    }

    public static Block createDoublesBlock(Double... values)
    {
        checkNotNull(values, "varargs 'values' is null");

        return createDoublesBlock(Arrays.asList(values));
    }

    public static Block createDoublesBlock(Iterable<Double> values)
    {
        BlockBuilder builder = DOUBLE.createBlockBuilder(new BlockBuilderStatus());

        for (Double value : values) {
            if (value == null) {
                builder.appendNull();
            }
            else {
                builder.appendDouble(value);
            }
        }

        return builder.build();
    }

    public static Block createDoubleSequenceBlock(int start, int end)
    {
        BlockBuilder builder = DOUBLE.createBlockBuilder(new BlockBuilderStatus());

        for (int i = start; i < end; i++) {
            builder.appendDouble((double) i);
        }

        return builder.build();
    }

    public static BlockIterableBuilder blockIterableBuilder(Type type)
    {
        return new BlockIterableBuilder(type);
    }

    public static class BlockIterableBuilder
    {
        private final List<Block> blocks = new ArrayList<>();
        private BlockBuilder blockBuilder;

        private BlockIterableBuilder(Type type)
        {
            blockBuilder = type.createBlockBuilder(new BlockBuilderStatus());
        }

        public BlockIterableBuilder append(Slice value)
        {
            blockBuilder.appendSlice(value);
            return this;
        }

        public BlockIterableBuilder append(double value)
        {
            blockBuilder.appendDouble(value);
            return this;
        }

        public BlockIterableBuilder append(long value)
        {
            blockBuilder.appendLong(value);
            return this;
        }

        public BlockIterableBuilder append(String value)
        {
            blockBuilder.appendSlice(Slices.utf8Slice(value));
            return this;
        }

        public BlockIterableBuilder append(byte[] value)
        {
            blockBuilder.appendSlice(Slices.wrappedBuffer(value));
            return this;
        }

        public BlockIterableBuilder appendNull()
        {
            blockBuilder.appendNull();
            return this;
        }

        public BlockIterableBuilder newBlock()
        {
            if (!blockBuilder.isEmpty()) {
                Block block = blockBuilder.build();
                blocks.add(block);
                blockBuilder = block.getType().createBlockBuilder(new BlockBuilderStatus());
            }
            return this;
        }

        public BlockIterable build()
        {
            newBlock();
            return createBlockIterable(blocks);
        }
    }
}
