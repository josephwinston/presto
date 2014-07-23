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
package com.facebook.presto.spi.type;

import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.spi.block.BlockBuilder;
import com.facebook.presto.spi.block.BlockBuilderStatus;
import com.facebook.presto.spi.block.BlockEncodingFactory;
import com.facebook.presto.spi.block.FixedWidthBlockUtil.FixedWidthBlockBuilderFactory;
import io.airlift.slice.Slice;
import io.airlift.slice.SliceOutput;

import static com.facebook.presto.spi.block.FixedWidthBlockUtil.createIsolatedFixedWidthBlockBuilderFactory;
import static io.airlift.slice.SizeOf.SIZE_OF_LONG;

public final class IntervalYearMonthType
        implements FixedWidthType
{
    public static final IntervalYearMonthType INTERVAL_YEAR_MONTH = new IntervalYearMonthType();

    public static IntervalYearMonthType getInstance()
    {
        return INTERVAL_YEAR_MONTH;
    }

    private static final FixedWidthBlockBuilderFactory BLOCK_BUILDER_FACTORY = createIsolatedFixedWidthBlockBuilderFactory(INTERVAL_YEAR_MONTH);
    public static final BlockEncodingFactory<?> BLOCK_ENCODING_FACTORY = BLOCK_BUILDER_FACTORY.getBlockEncodingFactory();

    private IntervalYearMonthType()
    {
    }

    @Override
    public String getName()
    {
        return "interval year to month";
    }

    @Override
    public Class<?> getJavaType()
    {
        return long.class;
    }

    @Override
    public int getFixedSize()
    {
        return (int) SIZE_OF_LONG;
    }

    @Override
    public Object getObjectValue(ConnectorSession session, Slice slice, int offset)
    {
        return new SqlIntervalYearMonth((int) slice.getLong(offset));
    }

    @Override
    public BlockBuilder createBlockBuilder(BlockBuilderStatus blockBuilderStatus)
    {
        return BLOCK_BUILDER_FACTORY.createFixedWidthBlockBuilder(blockBuilderStatus);
    }

    @Override
    public BlockBuilder createFixedSizeBlockBuilder(int positionCount)
    {
        return BLOCK_BUILDER_FACTORY.createFixedWidthBlockBuilder(positionCount);
    }

    @Override
    public boolean getBoolean(Slice slice, int offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeBoolean(SliceOutput sliceOutput, boolean value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLong(Slice slice, int offset)
    {
        return slice.getLong(offset);
    }

    @Override
    public void writeLong(SliceOutput sliceOutput, long value)
    {
        sliceOutput.writeLong(value);
    }

    @Override
    public double getDouble(Slice slice, int offset)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeDouble(SliceOutput sliceOutput, double value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Slice getSlice(Slice slice, int offset)
    {
        return slice.slice(offset, getFixedSize());
    }

    @Override
    public void writeSlice(SliceOutput sliceOutput, Slice value, int offset)
    {
        sliceOutput.writeBytes(value, offset, SIZE_OF_LONG);
    }

    @Override
    public boolean equalTo(Slice leftSlice, int leftOffset, Slice rightSlice, int rightOffset)
    {
        long leftValue = leftSlice.getLong(leftOffset);
        long rightValue = rightSlice.getLong(rightOffset);
        return leftValue == rightValue;
    }

    @Override
    public int hash(Slice slice, int offset)
    {
        long value = slice.getLong(offset);
        return (int) (value ^ (value >>> 32));
    }

    @Override
    public int compareTo(Slice leftSlice, int leftOffset, Slice rightSlice, int rightOffset)
    {
        long leftValue = leftSlice.getLong(leftOffset);
        long rightValue = rightSlice.getLong(rightOffset);
        return Long.compare(leftValue, rightValue);
    }

    @Override
    public void appendTo(Slice slice, int offset, BlockBuilder blockBuilder)
    {
        long value = slice.getLong(offset);
        blockBuilder.appendLong(value);
    }

    @Override
    public void appendTo(Slice slice, int offset, SliceOutput sliceOutput)
    {
        sliceOutput.writeBytes(slice, offset, (int) SIZE_OF_LONG);
    }

    @Override
    public String toString()
    {
        return getName();
    }
}
