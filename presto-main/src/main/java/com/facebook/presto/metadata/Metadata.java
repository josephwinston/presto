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
package com.facebook.presto.metadata;

import com.facebook.presto.spi.ColumnMetadata;
import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.spi.type.Type;
import com.facebook.presto.sql.tree.QualifiedName;
import com.google.common.base.Optional;
import com.google.common.collect.Multimap;

import javax.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Metadata
{
    Type getType(String typeName);

    FunctionInfo resolveFunction(QualifiedName name, List<? extends Type> parameterTypes, boolean approximate);

    @NotNull
    FunctionInfo getExactFunction(Signature handle);

    boolean isAggregationFunction(QualifiedName name);

    @NotNull
    List<FunctionInfo> listFunctions();

    void addFunctions(List<FunctionInfo> functions);

    void addOperators(Multimap<OperatorType, FunctionInfo> operators);

    FunctionInfo resolveOperator(OperatorType operatorType, List<? extends Type> argumentTypes)
            throws OperatorNotFoundException;

    FunctionInfo getExactOperator(OperatorType operatorType, Type returnType, List<? extends Type> argumentTypes)
            throws OperatorNotFoundException;

    @NotNull
    List<String> listSchemaNames(ConnectorSession session, String catalogName);

    /**
     * Returns a table handle for the specified table name.
     */
    @NotNull
    Optional<TableHandle> getTableHandle(ConnectorSession session, QualifiedTableName tableName);

    /**
     * Return the metadata for the specified table handle.
     *
     * @throws RuntimeException if table handle is no longer valid
     */
    @NotNull
    TableMetadata getTableMetadata(TableHandle tableHandle);

    /**
     * Get the names that match the specified table prefix (never null).
     */
    @NotNull
    List<QualifiedTableName> listTables(ConnectorSession session, QualifiedTablePrefix prefix);

    /**
     * Returns a handle for the specified table column.
     *
     * @throws RuntimeException if table handle is no longer valid
     */
    @NotNull
    Optional<ColumnHandle> getColumnHandle(TableHandle tableHandle, String columnName);

    /**
     * Returns the handle for the sample weight column.
     *
     * @throws RuntimeException if the table handle is no longer valid
     */
    @NotNull
    Optional<ColumnHandle> getSampleWeightColumnHandle(TableHandle tableHandle);

    /**
     * Returns true iff this catalog supports creation of sampled tables
     *
     */
    boolean canCreateSampledTables(ConnectorSession session, String catalogName);

    /**
     * Gets all of the columns on the specified table, or an empty map if the columns can not be enumerated.
     *
     * @throws RuntimeException if table handle is no longer valid
     */
    @NotNull
    Map<String, ColumnHandle> getColumnHandles(TableHandle tableHandle);

    /**
     * Gets the metadata for the specified table column.
     *
     * @throws RuntimeException if table or column handles are no longer valid
     */
    @NotNull
    ColumnMetadata getColumnMetadata(TableHandle tableHandle, ColumnHandle columnHandle);

    /**
     * Gets the metadata for all columns that match the specified table prefix.
     */
    @NotNull
    Map<QualifiedTableName, List<ColumnMetadata>> listTableColumns(ConnectorSession session, QualifiedTablePrefix prefix);

    /**
     * Creates a table using the specified table metadata.
     */
    @NotNull
    TableHandle createTable(ConnectorSession session, String catalogName, TableMetadata tableMetadata);

    /**
     * Drops the specified table
     *
     * @throws RuntimeException if the table can not be dropped or table handle is no longer valid
     */
    void dropTable(TableHandle tableHandle);

    /**
     * Begin the atomic creation of a table with data.
     */
    OutputTableHandle beginCreateTable(ConnectorSession session, String catalogName, TableMetadata tableMetadata);

    /**
     * Commit a table creation with data after the data is written.
     */
    void commitCreateTable(OutputTableHandle tableHandle, Collection<String> fragments);

    /**
     * Gets all the loaded catalogs
     *
     * @return Map of catalog name to connector id
     */
    @NotNull
    Map<String, String> getCatalogNames();

    /**
     * Get the names that match the specified table prefix (never null).
     */
    @NotNull
    List<QualifiedTableName> listViews(ConnectorSession session, QualifiedTablePrefix prefix);

    /**
     * Get the view definitions that match the specified table prefix (never null).
     */
    @NotNull
    Map<QualifiedTableName, ViewDefinition> getViews(ConnectorSession session, QualifiedTablePrefix prefix);

    /**
     * Returns the view definition for the specified view name.
     */
    @NotNull
    Optional<ViewDefinition> getView(ConnectorSession session, QualifiedTableName viewName);

    /**
     * Creates the specified view with the specified view definition.
     */
    void createView(ConnectorSession session, QualifiedTableName viewName, String viewData, boolean replace);

    /**
     * Drops the specified view.
     */
    void dropView(ConnectorSession session, QualifiedTableName viewName);
}
