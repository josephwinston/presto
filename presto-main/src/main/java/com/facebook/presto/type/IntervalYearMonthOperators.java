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
import com.facebook.presto.spi.type.SqlIntervalYearMonth;
import com.facebook.presto.spi.type.IntervalYearMonthType;
import com.facebook.presto.spi.type.VarcharType;
import io.airlift.slice.Slice;

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
import static io.airlift.slice.Slices.utf8Slice;

public final class IntervalYearMonthOperators
{
    private IntervalYearMonthOperators()
    {
    }

    @ScalarOperator(ADD)
    @SqlType(IntervalYearMonthType.class)
    public static long add(@SqlType(IntervalYearMonthType.class) long left, @SqlType(IntervalYearMonthType.class) long right)
    {
        return left + right;
    }

    @ScalarOperator(SUBTRACT)
    @SqlType(IntervalYearMonthType.class)
    public static long subtract(@SqlType(IntervalYearMonthType.class) long left, @SqlType(IntervalYearMonthType.class) long right)
    {
        return left - right;
    }

    @ScalarOperator(MULTIPLY)
    @SqlType(IntervalYearMonthType.class)
    public static long multiplyByBigint(@SqlType(IntervalYearMonthType.class) long left, @SqlType(BigintType.class) long right)
    {
        return left * right;
    }

    @ScalarOperator(MULTIPLY)
    @SqlType(IntervalYearMonthType.class)
    public static long multiplyByDouble(@SqlType(IntervalYearMonthType.class) long left, @SqlType(DoubleType.class) double right)
    {
        return (long) (left * right);
    }

    @ScalarOperator(MULTIPLY)
    @SqlType(IntervalYearMonthType.class)
    public static long bigintMultiply(@SqlType(BigintType.class) long left, @SqlType(IntervalYearMonthType.class) long right)
    {
        return left * right;
    }

    @ScalarOperator(MULTIPLY)
    @SqlType(IntervalYearMonthType.class)
    public static long doubleMultiply(@SqlType(DoubleType.class) double left, @SqlType(IntervalYearMonthType.class) long right)
    {
        return (long) (left * right);
    }

    @ScalarOperator(DIVIDE)
    @SqlType(IntervalYearMonthType.class)
    public static long divideByDouble(@SqlType(IntervalYearMonthType.class) long left, @SqlType(DoubleType.class) double right)
    {
        return (long) (left / right);
    }

    @ScalarOperator(NEGATION)
    @SqlType(IntervalYearMonthType.class)
    public static long negate(@SqlType(IntervalYearMonthType.class) long value)
    {
        return -value;
    }

    @ScalarOperator(EQUAL)
    @SqlType(BooleanType.class)
    public static boolean equal(@SqlType(IntervalYearMonthType.class) long left, @SqlType(IntervalYearMonthType.class) long right)
    {
        return left == right;
    }

    @ScalarOperator(NOT_EQUAL)
    @SqlType(BooleanType.class)
    public static boolean notEqual(@SqlType(IntervalYearMonthType.class) long left, @SqlType(IntervalYearMonthType.class) long right)
    {
        return left != right;
    }

    @ScalarOperator(LESS_THAN)
    @SqlType(BooleanType.class)
    public static boolean lessThan(@SqlType(IntervalYearMonthType.class) long left, @SqlType(IntervalYearMonthType.class) long right)
    {
        return left < right;
    }

    @ScalarOperator(LESS_THAN_OR_EQUAL)
    @SqlType(BooleanType.class)
    public static boolean lessThanOrEqual(@SqlType(IntervalYearMonthType.class) long left, @SqlType(IntervalYearMonthType.class) long right)
    {
        return left <= right;
    }

    @ScalarOperator(GREATER_THAN)
    @SqlType(BooleanType.class)
    public static boolean greaterThan(@SqlType(IntervalYearMonthType.class) long left, @SqlType(IntervalYearMonthType.class) long right)
    {
        return left > right;
    }

    @ScalarOperator(GREATER_THAN_OR_EQUAL)
    @SqlType(BooleanType.class)
    public static boolean greaterThanOrEqual(@SqlType(IntervalYearMonthType.class) long left, @SqlType(IntervalYearMonthType.class) long right)
    {
        return left >= right;
    }

    @ScalarOperator(BETWEEN)
    @SqlType(BooleanType.class)
    public static boolean between(
            @SqlType(IntervalYearMonthType.class) long value,
            @SqlType(IntervalYearMonthType.class) long min,
            @SqlType(IntervalYearMonthType.class) long max)
    {
        return min <= value && value <= max;
    }

    @ScalarOperator(CAST)
    @SqlType(VarcharType.class)
    public static Slice castToSlice(@SqlType(IntervalYearMonthType.class) long value)
    {
        return utf8Slice(SqlIntervalYearMonth.formatMonths(value));
    }

    @ScalarOperator(HASH_CODE)
    public static int hashCode(@SqlType(IntervalYearMonthType.class) long value)
    {
        return (int) (value ^ (value >>> 32));
    }
}
