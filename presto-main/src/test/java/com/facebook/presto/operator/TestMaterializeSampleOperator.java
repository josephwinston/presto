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

import com.facebook.presto.execution.TaskId;
import com.facebook.presto.operator.MaterializeSampleOperator.MaterializeSampleOperatorFactory;
import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.testing.MaterializedResult;
import com.google.common.collect.ImmutableList;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

import static com.facebook.presto.operator.OperatorAssertion.appendSampleWeight;
import static com.facebook.presto.operator.RowPagesBuilder.rowPagesBuilder;
import static com.facebook.presto.spi.type.BigintType.BIGINT;
import static com.facebook.presto.spi.type.TimeZoneKey.UTC_KEY;
import static com.facebook.presto.testing.MaterializedResult.resultBuilder;
import static io.airlift.concurrent.Threads.daemonThreadsNamed;
import static java.util.concurrent.Executors.newCachedThreadPool;

@Test(singleThreaded = true)
public class TestMaterializeSampleOperator
{
    private ExecutorService executor;
    private DriverContext driverContext;

    @BeforeMethod
    public void setUp()
    {
        executor = newCachedThreadPool(daemonThreadsNamed("test"));
        ConnectorSession session = new ConnectorSession("user", "source", "catalog", "schema", UTC_KEY, Locale.ENGLISH, "address", "agent");
        driverContext = new TaskContext(new TaskId("query", "stage", "task"), executor, session)
                .addPipelineContext(true, true)
                .addDriverContext();
    }

    @AfterMethod
    public void tearDown()
    {
        executor.shutdownNow();
    }

    @Test
    public void testZeroSampleWeight()
            throws Exception
    {
        List<Page> input = rowPagesBuilder(BIGINT)
                .addSequencePage(100, 1)
                .build();
        input = appendSampleWeight(input, 0);

        OperatorFactory operatorFactory = new MaterializeSampleOperatorFactory(0, ImmutableList.of(BIGINT), 1);
        Operator operator = operatorFactory.createOperator(driverContext);

        MaterializedResult expected = resultBuilder(driverContext.getSession(), BIGINT)
                .build();

        OperatorAssertion.assertOperatorEqualsIgnoreOrder(operator, input, expected);
    }

    @Test
    public void testMaterialization()
            throws Exception
    {
        List<Page> input = rowPagesBuilder(BIGINT)
                .addSequencePage(100, 1)
                .build();
        input = appendSampleWeight(input, 2);

        OperatorFactory operatorFactory = new MaterializeSampleOperatorFactory(0, ImmutableList.of(BIGINT), 1);
        Operator operator = operatorFactory.createOperator(driverContext);

        List<Page> expectedPages = rowPagesBuilder(BIGINT)
                .addSequencePage(100, 1)
                .addSequencePage(100, 1)
                .build();

        MaterializedResult expected = resultBuilder(driverContext.getSession(), BIGINT)
                .pages(expectedPages)
                .build();

        OperatorAssertion.assertOperatorEqualsIgnoreOrder(operator, input, expected);
    }
}
