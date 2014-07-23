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
package com.facebook.presto.raptor.metadata;

import com.facebook.presto.metadata.InMemoryNodeManager;
import com.facebook.presto.metadata.MetadataUtil.TableMetadataBuilder;
import com.facebook.presto.metadata.NodeVersion;
import com.facebook.presto.metadata.PrestoNode;
import com.facebook.presto.raptor.RaptorConnectorId;
import com.facebook.presto.raptor.RaptorMetadata;
import com.facebook.presto.raptor.RaptorPartitionKey;
import com.facebook.presto.raptor.RaptorSplitManager;
import com.facebook.presto.spi.ConnectorColumnHandle;
import com.facebook.presto.spi.ConnectorPartition;
import com.facebook.presto.spi.ConnectorPartitionResult;
import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.spi.ConnectorSplitSource;
import com.facebook.presto.spi.ConnectorTableHandle;
import com.facebook.presto.spi.ConnectorTableMetadata;
import com.facebook.presto.spi.Domain;
import com.facebook.presto.spi.PartitionKey;
import com.facebook.presto.spi.Range;
import com.facebook.presto.spi.SortedRangeSet;
import com.facebook.presto.spi.TupleDomain;
import com.facebook.presto.spi.type.BigintType;
import com.facebook.presto.type.TypeRegistry;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import io.airlift.testing.FileUtils;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.facebook.presto.spi.type.TimeZoneKey.UTC_KEY;
import static com.facebook.presto.spi.type.VarcharType.VARCHAR;
import static io.airlift.slice.Slices.utf8Slice;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(singleThreaded = true)
public class TestRaptorSplitManager
{
    private static final ConnectorSession SESSION = new ConnectorSession("user", "test", "default", "default", UTC_KEY, Locale.ENGLISH, null, null);
    private static final ConnectorTableMetadata TEST_TABLE = TableMetadataBuilder.tableMetadataBuilder("demo", "test_table")
            .partitionKeyColumn("ds", VARCHAR)
            .column("foo", VARCHAR)
            .column("bar", BigintType.BIGINT)
            .build();

    private Handle dummyHandle;
    private File dataDir;
    private RaptorSplitManager raptorSplitManager;
    private ConnectorTableHandle tableHandle;
    private ConnectorColumnHandle dsColumnHandle;

    @BeforeMethod
    public void setup()
            throws Exception
    {
        TypeRegistry typeRegistry = new TypeRegistry();
        DBI dbi = new DBI("jdbc:h2:mem:test" + System.nanoTime());
        dbi.registerMapper(new TableColumn.Mapper(typeRegistry));
        dbi.registerMapper(new RaptorPartitionKey.Mapper(typeRegistry));
        dummyHandle = dbi.open();
        dataDir = Files.createTempDir();
        ShardManager shardManager = new DatabaseShardManager(dbi);
        InMemoryNodeManager nodeManager = new InMemoryNodeManager();

        String nodeName = UUID.randomUUID().toString();
        nodeManager.addNode("raptor", new PrestoNode(nodeName, new URI("http://127.0.0.1/"), NodeVersion.UNKNOWN));

        RaptorConnectorId connectorId = new RaptorConnectorId("raptor");
        RaptorMetadata metadata = new RaptorMetadata(connectorId, dbi, shardManager);

        tableHandle = metadata.createTable(SESSION, TEST_TABLE);
        dsColumnHandle = metadata.getColumnHandle(tableHandle, "ds");

        UUID shardUuid1 = UUID.randomUUID();
        UUID shardUuid2 = UUID.randomUUID();
        UUID shardUuid3 = UUID.randomUUID();
        UUID shardUuid4 = UUID.randomUUID();

        shardManager.commitPartition(
                tableHandle,
                "ds=1",
                ImmutableList.<PartitionKey>of(new RaptorPartitionKey("ds=1", "ds", VARCHAR, "1")),
                ImmutableMap.<UUID, String>builder()
                        .put(shardUuid1, nodeName)
                        .put(shardUuid2, nodeName)
                        .put(shardUuid3, nodeName)
                        .build());

        shardManager.commitPartition(
                tableHandle,
                "ds=2",
                ImmutableList.<PartitionKey>of(new RaptorPartitionKey("ds=2", "ds", VARCHAR, "2")),
                ImmutableMap.<UUID, String>builder()
                        .put(shardUuid4, nodeName)
                        .build());

        raptorSplitManager = new RaptorSplitManager(connectorId, nodeManager, shardManager, metadata);
    }

    @AfterMethod
    public void teardown()
    {
        dummyHandle.close();
        FileUtils.deleteRecursively(dataDir);
    }

    @Test
    public void testSanity()
            throws InterruptedException
    {
        ConnectorPartitionResult partitionResult = raptorSplitManager.getPartitions(tableHandle, TupleDomain.<ConnectorColumnHandle>all());
        assertEquals(partitionResult.getPartitions().size(), 2);
        assertTrue(partitionResult.getUndeterminedTupleDomain().isAll());

        List<ConnectorPartition> partitions = partitionResult.getPartitions();
        TupleDomain<ConnectorColumnHandle> columnUnionedTupleDomain = TupleDomain.columnWiseUnion(partitions.get(0).getTupleDomain(), partitions.get(1).getTupleDomain());
        assertEquals(columnUnionedTupleDomain, TupleDomain.withColumnDomains(ImmutableMap.of(dsColumnHandle, Domain.create(SortedRangeSet.of(Range.equal(utf8Slice("1")), Range.equal(utf8Slice("2"))), false))));

        ConnectorSplitSource splitSource = raptorSplitManager.getPartitionSplits(tableHandle, partitions);
        int splitCount = 0;
        while (!splitSource.isFinished()) {
            splitCount += splitSource.getNextBatch(1000).size();
        }
        assertEquals(splitCount, 4);
    }
}
