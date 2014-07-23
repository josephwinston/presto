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

import com.facebook.presto.spi.type.Type;
import com.google.common.base.Joiner;

import java.util.List;

import static com.facebook.presto.spi.type.BigintType.BIGINT;
import static com.facebook.presto.spi.type.BooleanType.BOOLEAN;
import static com.facebook.presto.type.UnknownType.UNKNOWN;
import static com.google.common.base.Preconditions.checkArgument;

public enum OperatorType
{
    ADD("+")
            {
                @Override
                void validateSignature(Type returnType, List<Type> argumentTypes)
                {
                    validateOperatorSignature(this, returnType, argumentTypes, 2);
                }
            },

    SUBTRACT("-")
            {
                @Override
                void validateSignature(Type returnType, List<Type> argumentTypes)
                {
                    validateOperatorSignature(this, returnType, argumentTypes, 2);
                }
            },

    MULTIPLY("*")
            {
                @Override
                void validateSignature(Type returnType, List<Type> argumentTypes)
                {
                    validateOperatorSignature(this, returnType, argumentTypes, 2);
                }
            },

    DIVIDE("/")
            {
                @Override
                void validateSignature(Type returnType, List<Type> argumentTypes)
                {
                    validateOperatorSignature(this, returnType, argumentTypes, 2);
                }
            },

    MODULUS("%")
            {
                @Override
                void validateSignature(Type returnType, List<Type> argumentTypes)
                {
                    validateOperatorSignature(this, returnType, argumentTypes, 2);
                }
            },

    NEGATION("-")
            {
                @Override
                void validateSignature(Type returnType, List<Type> argumentTypes)
                {
                    validateOperatorSignature(this, returnType, argumentTypes, 1);
                }
            },

    EQUAL("=")
            {
                @Override
                void validateSignature(Type returnType, List<Type> argumentTypes)
                {
                    validateComparisonOperatorSignature(this, returnType, argumentTypes, 2);
                }
            },

    NOT_EQUAL("<>")
            {
                @Override
                void validateSignature(Type returnType, List<Type> argumentTypes)
                {
                    validateComparisonOperatorSignature(this, returnType, argumentTypes, 2);
                }
            },

    LESS_THAN("<")
            {
                @Override
                void validateSignature(Type returnType, List<Type> argumentTypes)
                {
                    validateComparisonOperatorSignature(this, returnType, argumentTypes, 2);
                }
            },
    LESS_THAN_OR_EQUAL("<=")
            {
                @Override
                void validateSignature(Type returnType, List<Type> argumentTypes)
                {
                    validateComparisonOperatorSignature(this, returnType, argumentTypes, 2);
                }
            },

    GREATER_THAN(">")
            {
                @Override
                void validateSignature(Type returnType, List<Type> argumentTypes)
                {
                    validateComparisonOperatorSignature(this, returnType, argumentTypes, 2);
                }
            },

    GREATER_THAN_OR_EQUAL(">=")
            {
                @Override
                void validateSignature(Type returnType, List<Type> argumentTypes)
                {
                    validateComparisonOperatorSignature(this, returnType, argumentTypes, 2);
                }
            },

    BETWEEN("BETWEEN")
            {
                @Override
                void validateSignature(Type returnType, List<Type> argumentTypes)
                {
                    validateComparisonOperatorSignature(this, returnType, argumentTypes, 3);
                }
            },

    CAST("CAST")
            {
                @Override
                void validateSignature(Type returnType, List<Type> argumentTypes)
                {
                    validateOperatorSignature(this, returnType, argumentTypes, 1);
                }
            },

    HASH_CODE("HASH CODE")
            {
                @Override
                void validateSignature(Type returnType, List<Type> argumentTypes)
                {
                    validateOperatorSignature(this, returnType, argumentTypes, 1);
                    checkArgument(returnType.equals(BIGINT), "%s operator must return a BIGINT: %s", this, formatSignature(this, returnType, argumentTypes));
                }
            };

    private final String operator;

    OperatorType(String operator)
    {
        this.operator = operator;
    }

    public String getOperator()
    {
        return operator;
    }

    abstract void validateSignature(Type returnType, List<Type> argumentTypes);

    private static void validateOperatorSignature(OperatorType operatorType, Type returnType, List<Type> argumentTypes, int expectedArgumentCount)
    {
        String signature = formatSignature(operatorType, returnType, argumentTypes);
        checkArgument(!returnType.equals(UNKNOWN), "%s operator return type can not be NULL: %s", operatorType, signature);
        checkArgument(argumentTypes.size() == expectedArgumentCount, "%s operator must have exactly %s argument: %s", operatorType, expectedArgumentCount, signature);
    }

    private static void validateComparisonOperatorSignature(OperatorType operatorType, Type returnType, List<Type> argumentTypes, int expectedArgumentCount)
    {
        validateOperatorSignature(operatorType, returnType, argumentTypes, expectedArgumentCount);
        checkArgument(returnType.equals(BOOLEAN), "%s operator must return a BOOLEAN: %s", operatorType, formatSignature(operatorType, returnType, argumentTypes));
    }

    private static String formatSignature(OperatorType operatorType, Type returnType, List<Type> argumentTypes)
    {
        return operatorType + "(" + Joiner.on(", ").join(argumentTypes) + ")::" + returnType;
    }
}
