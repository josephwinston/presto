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
package com.facebook.presto.serde;

import com.facebook.presto.block.BlockAssertions;
import com.facebook.presto.spi.block.Block;
import com.facebook.presto.spi.block.BlockBuilderStatus;
import com.facebook.presto.spi.block.BlockEncoding;
import com.facebook.presto.spi.block.VariableWidthBlockEncoding;
import io.airlift.slice.DynamicSliceOutput;
import io.airlift.slice.Slices;
import org.testng.annotations.Test;

import static com.facebook.presto.spi.type.VarcharType.VARCHAR;

public class TestUncompressedBlockSerde
{
    @Test
    public void testRoundTrip()
    {
        Block expectedBlock = VARCHAR.createBlockBuilder(new BlockBuilderStatus())
                .appendSlice(Slices.utf8Slice("alice"))
                .appendSlice(Slices.utf8Slice("bob"))
                .appendSlice(Slices.utf8Slice("charlie"))
                .appendSlice(Slices.utf8Slice("dave"))
                .build();

        DynamicSliceOutput sliceOutput = new DynamicSliceOutput(1024);
        BlockEncoding blockEncoding = new VariableWidthBlockEncoding(VARCHAR);
        blockEncoding.writeBlock(sliceOutput, expectedBlock);
        Block actualBlock = blockEncoding.readBlock(sliceOutput.slice().getInput());
        BlockAssertions.assertBlockEquals(actualBlock, expectedBlock);
    }

    @Test
    public void testCreateBlockWriter()
    {
        Block block = VARCHAR.createBlockBuilder(new BlockBuilderStatus())
                .appendSlice(Slices.utf8Slice("alice"))
                .appendSlice(Slices.utf8Slice("bob"))
                .appendSlice(Slices.utf8Slice("charlie"))
                .appendSlice(Slices.utf8Slice("dave"))
                .build();

        DynamicSliceOutput sliceOutput = new DynamicSliceOutput(1024);
        BlockEncoding blockEncoding = new UncompressedEncoder(sliceOutput).append(block).append(block).finish();
        Block actualBlock = blockEncoding.readBlock(sliceOutput.slice().getInput());
        BlockAssertions.assertBlockEquals(actualBlock, VARCHAR.createBlockBuilder(new BlockBuilderStatus())
                .appendSlice(Slices.utf8Slice("alice"))
                .appendSlice(Slices.utf8Slice("bob"))
                .appendSlice(Slices.utf8Slice("charlie"))
                .appendSlice(Slices.utf8Slice("dave"))
                .appendSlice(Slices.utf8Slice("alice"))
                .appendSlice(Slices.utf8Slice("bob"))
                .appendSlice(Slices.utf8Slice("charlie"))
                .appendSlice(Slices.utf8Slice("dave"))
                .build());
    }
}
