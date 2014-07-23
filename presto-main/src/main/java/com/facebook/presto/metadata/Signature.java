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
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Signature
{
    private final String name;
    private final Type returnType;
    private final List<Type> argumentTypes;
    private final boolean approximate;
    private final boolean internal;

    @JsonCreator
    public Signature(
            @JsonProperty("name") String name,
            @JsonProperty("returnType") Type returnType,
            @JsonProperty("argumentTypes") List<? extends Type> argumentTypes,
            @JsonProperty("approximate") boolean approximate,
            @JsonProperty("internal") boolean internal)
    {
        checkNotNull(name, "name is null");
        checkNotNull(returnType, "returnType is null");
        checkNotNull(argumentTypes, "argumentTypes is null");

        this.name = name;
        this.returnType = returnType;
        this.argumentTypes = ImmutableList.copyOf(argumentTypes);
        this.approximate = approximate;
        this.internal = internal;
    }

    public Signature(String name, Type returnType, Type... argumentTypes)
    {
        this(name, returnType, ImmutableList.copyOf(argumentTypes), false);
    }

    public Signature(String name, Type returnType, List<? extends Type> argumentTypes, boolean approximate)
    {
        this(name, returnType, argumentTypes, approximate, false);
    }

    public static Signature internalFunction(String name, Type returnType, Type... argumentTypes)
    {
        return new Signature(name, returnType, ImmutableList.copyOf(argumentTypes), false, true);
    }

    public static Signature internalFunction(String name, Type returnType, List<Type> argumentTypes)
    {
        return new Signature(name, returnType, argumentTypes, false, true);
    }

    @JsonProperty
    public boolean isApproximate()
    {
        return approximate;
    }

    @JsonProperty
    public String getName()
    {
        return name;
    }

    @JsonProperty
    public Type getReturnType()
    {
        return returnType;
    }

    @JsonProperty
    public List<Type> getArgumentTypes()
    {
        return argumentTypes;
    }

    @JsonProperty
    public boolean isInternal()
    {
        return internal;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, returnType, argumentTypes, approximate, internal);
    }

    Signature withAlias(String name)
    {
        return new Signature(name, returnType, argumentTypes, approximate, internal);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Signature other = (Signature) obj;
        return Objects.equals(this.name, other.name) &&
                Objects.equals(this.returnType, other.returnType) &&
                Objects.equals(this.argumentTypes, other.argumentTypes) &&
                Objects.equals(this.approximate, other.approximate) &&
                Objects.equals(this.internal, other.internal);
    }

    public String toString()
    {
        return (internal ? "%" : "") + name + (approximate ? "[approximate]" : "") + "(" + Joiner.on(",").join(argumentTypes) + "):" + returnType;
    }
}
