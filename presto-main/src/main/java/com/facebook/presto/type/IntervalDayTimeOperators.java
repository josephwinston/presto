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
package com.facebook.presto.type;

import com.facebook.presto.operator.scalar.ScalarOperator;
import com.facebook.presto.spi.type.BigintType;
import com.facebook.presto.spi.type.BooleanType;
import com.facebook.presto.spi.type.DoubleType;
import com.facebook.presto.spi.type.SqlIntervalDayTime;
import com.facebook.presto.spi.type.IntervalDayTimeType;
import com.facebook.presto.spi.type.VarcharType;
import io.airlift.slice.Slice;
import io.airlift.slice.Slices;

import static com.facebook.presto.metadata.OperatorType.ADD;
import static com.facebook.presto.metadata.OperatorType.BETWEEN;
import static com.facebook.presto.metadata.OperatorType.CAST;
import static com.facebook.presto.metadata.OperatorType.DIVIDE;
import static com.facebook.presto.metadata.OperatorType.EQUAL;
import static com.facebook.presto.metadata.OperatorType.GREATER_THAN;
import static com.facebook.presto.metadata.OperatorType.GREATER_THAN_OR_EQUAL;
import static com.facebook.presto.metadata.OperatorType.HASH_CODE;
import static com.facebook.presto.metadata.OperatorType.LESS_THAN;
import static com.facebook.presto.metadata.OperatorType.LESS_THAN_OR_EQUAL;
import static com.facebook.presto.metadata.OperatorType.MULTIPLY;
import static com.facebook.presto.metadata.OperatorType.NEGATION;
import static com.facebook.presto.metadata.OperatorType.NOT_EQUAL;
import static com.facebook.presto.metadata.OperatorType.SUBTRACT;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class IntervalDayTimeOperators
{
    private IntervalDayTimeOperators()
    {
    }

    @ScalarOperator(ADD)
    @SqlType(IntervalDayTimeType.class)
    public static long add(@SqlType(IntervalDayTimeType.class) long left, @SqlType(IntervalDayTimeType.class) long right)
    {
        return left + right;
    }

    @ScalarOperator(SUBTRACT)
    @SqlType(IntervalDayTimeType.class)
    public static long subtract(@SqlType(IntervalDayTimeType.class) long left, @SqlType(IntervalDayTimeType.class) long right)
    {
        return left - right;
    }

    @ScalarOperator(MULTIPLY)
    @SqlType(IntervalDayTimeType.class)
    public static long multiplyByBigint(@SqlType(IntervalDayTimeType.class) long left, @SqlType(BigintType.class) long right)
    {
        return left * right;
    }

    @ScalarOperator(MULTIPLY)
    @SqlType(IntervalDayTimeType.class)
    public static long multiplyByDouble(@SqlType(IntervalDayTimeType.class) long left, @SqlType(DoubleType.class) double right)
    {
        return (long) (left * right);
    }

    @ScalarOperator(MULTIPLY)
    @SqlType(IntervalDayTimeType.class)
    public static long bigintMultiply(@SqlType(BigintType.class) long left, @SqlType(IntervalDayTimeType.class) long right)
    {
        return left * right;
    }

    @ScalarOperator(MULTIPLY)
    @SqlType(IntervalDayTimeType.class)
    public static long doubleMultiply(@SqlType(DoubleType.class) double left, @SqlType(IntervalDayTimeType.class) long right)
    {
        return (long) (left * right);
    }

    @ScalarOperator(DIVIDE)
    @SqlType(IntervalDayTimeType.class)
    public static long divideByDouble(@SqlType(IntervalDayTimeType.class) long left, @SqlType(DoubleType.class) double right)
    {
        return (long) (left / right);
    }

    @ScalarOperator(NEGATION)
    @SqlType(IntervalDayTimeType.class)
    public static long negate(@SqlType(IntervalDayTimeType.class) long value)
    {
        return -value;
    }

    @ScalarOperator(EQUAL)
    @SqlType(BooleanType.class)
    public static boolean equal(@SqlType(IntervalDayTimeType.class) long left, @SqlType(IntervalDayTimeType.class) long right)
    {
        return left == right;
    }

    @ScalarOperator(NOT_EQUAL)
    @SqlType(BooleanType.class)
    public static boolean notEqual(@SqlType(IntervalDayTimeType.class) long left, @SqlType(IntervalDayTimeType.class) long right)
    {
        return left != right;
    }

    @ScalarOperator(LESS_THAN)
    @SqlType(BooleanType.class)
    public static boolean lessThan(@SqlType(IntervalDayTimeType.class) long left, @SqlType(IntervalDayTimeType.class) long right)
    {
        return left < right;
    }

    @ScalarOperator(LESS_THAN_OR_EQUAL)
    @SqlType(BooleanType.class)
    public static boolean lessThanOrEqual(@SqlType(IntervalDayTimeType.class) long left, @SqlType(IntervalDayTimeType.class) long right)
    {
        return left <= right;
    }

    @ScalarOperator(GREATER_THAN)
    @SqlType(BooleanType.class)
    public static boolean greaterThan(@SqlType(IntervalDayTimeType.class) long left, @SqlType(IntervalDayTimeType.class) long right)
    {
        return left > right;
    }

    @ScalarOperator(GREATER_THAN_OR_EQUAL)
    @SqlType(BooleanType.class)
    public static boolean greaterThanOrEqual(@SqlType(IntervalDayTimeType.class) long left, @SqlType(IntervalDayTimeType.class) long right)
    {
        return left >= right;
    }

    @ScalarOperator(BETWEEN)
    @SqlType(BooleanType.class)
    public static boolean between(
            @SqlType(IntervalDayTimeType.class) long value,
            @SqlType(IntervalDayTimeType.class) long min,
            @SqlType(IntervalDayTimeType.class) long max)
    {
        return min <= value && value <= max;
    }

    @ScalarOperator(CAST)
    @SqlType(VarcharType.class)
    public static Slice castToSlice(@SqlType(IntervalDayTimeType.class) long value)
    {
        return Slices.copiedBuffer(SqlIntervalDayTime.formatMillis(value), UTF_8);
    }

    @ScalarOperator(HASH_CODE)
    public static int hashCode(@SqlType(IntervalDayTimeType.class) long value)
    {
        return (int) (value ^ (value >>> 32));
    }
}
