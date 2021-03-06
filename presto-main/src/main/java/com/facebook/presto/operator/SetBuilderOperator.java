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
package com.facebook.presto.operator;

import com.facebook.presto.operator.ChannelSet.ChannelSetBuilder;
import com.facebook.presto.spi.block.Block;
import com.facebook.presto.spi.type.Type;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import javax.annotation.concurrent.ThreadSafe;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@ThreadSafe
public class SetBuilderOperator
        implements Operator
{
    public static class SetSupplier
    {
        private final Type type;
        private final SettableFuture<ChannelSet> channelSetFuture = SettableFuture.create();

        public SetSupplier(Type type)
        {
            this.type = checkNotNull(type, "type is null");
        }

        public Type getType()
        {
            return type;
        }

        public ListenableFuture<ChannelSet> getChannelSet()
        {
            return channelSetFuture;
        }

        void setChannelSet(ChannelSet channelSet)
        {
            boolean wasSet = channelSetFuture.set(checkNotNull(channelSet, "channelSet is null"));
            checkState(wasSet, "ChannelSet already set");
        }
    }

    public static class SetBuilderOperatorFactory
            implements OperatorFactory
    {
        private final int operatorId;
        private final SetSupplier setProvider;
        private final int setChannel;
        private final int expectedPositions;
        private boolean closed;

        public SetBuilderOperatorFactory(
                int operatorId,
                List<Type> types,
                int setChannel,
                int expectedPositions)
        {
            this.operatorId = operatorId;
            Preconditions.checkArgument(setChannel >= 0, "setChannel is negative");
            this.setProvider = new SetSupplier(checkNotNull(types, "types is null").get(setChannel));
            this.setChannel = setChannel;
            this.expectedPositions = checkNotNull(expectedPositions, "expectedPositions is null");
        }

        public SetSupplier getSetProvider()
        {
            return setProvider;
        }

        @Override
        public List<Type> getTypes()
        {
            return ImmutableList.of();
        }

        @Override
        public Operator createOperator(DriverContext driverContext)
        {
            checkState(!closed, "Factory is already closed");
            OperatorContext operatorContext = driverContext.addOperatorContext(operatorId, SetBuilderOperator.class.getSimpleName());
            return new SetBuilderOperator(operatorContext, setProvider, setChannel, expectedPositions);
        }

        @Override
        public void close()
        {
            closed = true;
        }
    }

    private final OperatorContext operatorContext;
    private final SetSupplier setSupplier;
    private final int setChannel;

    private final ChannelSetBuilder channelSetBuilder;

    private boolean finished;

    public SetBuilderOperator(
            OperatorContext operatorContext,
            SetSupplier setSupplier,
            int setChannel,
            int expectedPositions)
    {
        this.operatorContext = checkNotNull(operatorContext, "operatorContext is null");
        this.setSupplier = checkNotNull(setSupplier, "setProvider is null");
        this.setChannel = setChannel;
        this.channelSetBuilder = new ChannelSetBuilder(
                setSupplier.getType(),
                expectedPositions,
                checkNotNull(operatorContext, "operatorContext is null"));
    }

    @Override
    public OperatorContext getOperatorContext()
    {
        return operatorContext;
    }

    @Override
    public List<Type> getTypes()
    {
        return ImmutableList.of();
    }

    @Override
    public void finish()
    {
        if (finished) {
            return;
        }

        ChannelSet channelSet = channelSetBuilder.build();
        setSupplier.setChannelSet(channelSet);
        operatorContext.recordGeneratedOutput(channelSet.getEstimatedSize(), channelSet.size());
        finished = true;
    }

    @Override
    public boolean isFinished()
    {
        return finished;
    }

    @Override
    public ListenableFuture<?> isBlocked()
    {
        return NOT_BLOCKED;
    }

    @Override
    public boolean needsInput()
    {
        return !finished;
    }

    @Override
    public void addInput(Page page)
    {
        checkNotNull(page, "page is null");
        checkState(!isFinished(), "Operator is already finished");

        Block sourceBlock = page.getBlock(setChannel);
        channelSetBuilder.addBlock(sourceBlock);
    }

    @Override
    public Page getOutput()
    {
        return null;
    }
}
