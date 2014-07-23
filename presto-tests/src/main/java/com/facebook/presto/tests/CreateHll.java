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
package com.facebook.presto.tests;

import com.facebook.presto.operator.scalar.ScalarFunction;
import com.facebook.presto.spi.type.BigintType;
import com.facebook.presto.spi.type.HyperLogLogType;
import com.facebook.presto.type.SqlType;
import io.airlift.slice.Slice;
import io.airlift.stats.cardinality.HyperLogLog;

public final class CreateHll
{
    private CreateHll() {}

    @ScalarFunction
    @SqlType(HyperLogLogType.class)
    public static Slice createHll(@SqlType(BigintType.class) long value)
    {
        HyperLogLog hll = HyperLogLog.newInstance(4096);
        hll.add(value);
        return hll.serialize();
    }
}
