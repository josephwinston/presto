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

import com.facebook.presto.spi.block.Block;
import com.facebook.presto.spi.block.BlockBuilder;
import com.facebook.presto.spi.type.Type;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import io.airlift.slice.Slice;
import io.airlift.slice.Slices;
import io.airlift.units.DataSize;
import io.airlift.units.DataSize.Unit;

import java.util.Iterator;

import static com.facebook.presto.spi.type.BigintType.BIGINT;

public final class BlockUtils
{
    private BlockUtils() {}

    public static void appendObject(BlockBuilder blockBuilder, Object value)
    {
        if (value == null) {
            blockBuilder.appendNull();
        }
        else if (value instanceof Boolean) {
            blockBuilder.appendBoolean((Boolean) value);
        }
        else if (value instanceof Double || value instanceof Float) {
            blockBuilder.appendDouble(((Number) value).doubleValue());
        }
        else if (value instanceof Number) {
            blockBuilder.appendLong(((Number) value).longValue());
        }
        else if (value instanceof byte[]) {
            blockBuilder.appendSlice(Slices.wrappedBuffer((byte[]) value));
        }
        else if (value instanceof String) {
            blockBuilder.appendSlice(Slices.utf8Slice((String) value));
        }
        else if (value instanceof Slice) {
            blockBuilder.appendSlice((Slice) value);
        }
        else {
            throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
        }
    }

    // TODO: remove this hack after empty blocks are supported
    public static BlockIterable emptyBlockIterable()
    {
        return new BlockIterable()
        {
            @Override
            public Type getType()
            {
                return BIGINT;
            }

            @Override
            public Optional<DataSize> getDataSize()
            {
                return Optional.of(new DataSize(0, Unit.BYTE));
            }

            @Override
            public Optional<Integer> getPositionCount()
            {
                return Optional.of(0);
            }

            @Override
            public Iterator<Block> iterator()
            {
                return Iterators.emptyIterator();
            }
        };
    }

    public static BlockIterable toBlocks(Iterable<Block> blocks)
    {
        return new BlocksIterableAdapter(Iterables.get(blocks, 0).getType(),
                Optional.<DataSize>absent(),
                Optional.<Integer>absent(),
                blocks);
    }

    private static class BlocksIterableAdapter
            implements BlockIterable
    {
        private final Type type;
        private final Iterable<Block> blocks;
        private Optional<DataSize> dataSize;
        private final Optional<Integer> positionCount;

        public BlocksIterableAdapter(Type type, Optional<DataSize> dataSize, Optional<Integer> positionCount, Iterable<Block> blocks)
        {
            this.type = type;
            this.blocks = blocks;
            this.dataSize = dataSize;
            this.positionCount = positionCount;
        }

        @Override
        public Type getType()
        {
            return type;
        }

        @Override
        public Optional<DataSize> getDataSize()
        {
            return dataSize;
        }

        @Override
        public Optional<Integer> getPositionCount()
        {
            return positionCount;
        }

        @Override
        public Iterator<Block> iterator()
        {
            return blocks.iterator();
        }
    }
}
