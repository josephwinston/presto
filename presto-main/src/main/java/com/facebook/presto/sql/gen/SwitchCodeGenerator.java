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
package com.facebook.presto.sql.gen;

import com.facebook.presto.byteCode.Block;
import com.facebook.presto.byteCode.ByteCodeNode;
import com.facebook.presto.byteCode.CompilerContext;
import com.facebook.presto.byteCode.Variable;
import com.facebook.presto.byteCode.control.IfStatement;
import com.facebook.presto.byteCode.instruction.LabelNode;
import com.facebook.presto.byteCode.instruction.VariableInstruction;
import com.facebook.presto.metadata.OperatorType;
import com.facebook.presto.metadata.Signature;
import com.facebook.presto.spi.type.Type;
import com.facebook.presto.sql.relational.CallExpression;
import com.facebook.presto.sql.relational.RowExpression;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Primitives;

import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

public class SwitchCodeGenerator
        implements ByteCodeGenerator
{
    @Override
    public ByteCodeNode generateExpression(Signature signature, ByteCodeGeneratorContext generatorContext, Type returnType, List<RowExpression> arguments)
    {
        // TODO: compile as
        /*
            hashCode = hashCode(<value>)

            // all constant expressions before a non-constant
            switch (hashCode) {
                case ...:
                    if (<value> == <constant1>) {
                       ...
                    }
                    else if (<value> == <constant2>) {
                       ...
                    }
                    else if (...) {
                    }
                case ...:
                    ...
            }

            if (<value> == <non-constant1>) {
                ...
            }
            else if (<value> == <non-constant2>) {
                ...
            }
            ...

            // repeat with next sequence of constant expressions
         */

        CompilerContext context = generatorContext.getContext();

        // process value, else, and all when clauses
        RowExpression value = arguments.get(0);
        ByteCodeNode valueBytecode = generatorContext.generate(value);
        ByteCodeNode elseValue;

        List<RowExpression> whenClauses;
        RowExpression last = arguments.get(arguments.size() - 1);
        if (last instanceof CallExpression && ((CallExpression) last).getSignature().getName().equals("WHEN")) {
            whenClauses = arguments.subList(1, arguments.size());
            elseValue = new Block(context)
                    .putVariable("wasNull", true)
                    .pushJavaDefault(returnType.getJavaType());
        }
        else {
            whenClauses = arguments.subList(1, arguments.size() - 1);
            elseValue = generatorContext.generate(last);
        }

        // determine the type of the value and result
        Class<?> valueType = value.getType().getJavaType();

        // evaluate the value and store it in a variable
        LabelNode nullValue = new LabelNode("nullCondition");
        Variable tempVariable = context.createTempVariable(valueType);
        Block block = new Block(context)
                .append(valueBytecode)
                .append(ByteCodeUtils.ifWasNullClearPopAndGoto(context, nullValue, void.class, valueType))
                .putVariable(tempVariable.getLocalVariableDefinition());

        ByteCodeNode getTempVariableNode = VariableInstruction.loadVariable(tempVariable.getLocalVariableDefinition());

        // build the statements
        elseValue = new Block(context).visitLabel(nullValue).append(elseValue);
        // reverse list because current if statement builder doesn't support if/else so we need to build the if statements bottom up
        for (RowExpression clause : Lists.reverse(whenClauses)) {
            Preconditions.checkArgument(clause instanceof CallExpression && ((CallExpression) clause).getSignature().getName().equals("WHEN"));

            RowExpression operand = ((CallExpression) clause).getArguments().get(0);
            RowExpression result = ((CallExpression) clause).getArguments().get(1);

            // call equals(value, operand)
            FunctionBinding functionBinding = generatorContext.getBootstrapBinder().bindOperator(
                    OperatorType.EQUAL,
                    generatorContext.generateGetSession(),
                    ImmutableList.of(generatorContext.generate(operand), getTempVariableNode),
                    ImmutableList.of(value.getType(), operand.getType()));

            MethodType methodType = functionBinding.getCallSite().type();
            Class<?> unboxedReturnType = Primitives.unwrap(methodType.returnType());

            LabelNode end = new LabelNode("end");
            Block equalsCall = new Block(context)
                    .setDescription("invoke")
                    .comment(operand.toString());
            ArrayList<Class<?>> stackTypes = new ArrayList<>();
            for (int i = 0; i < functionBinding.getArguments().size(); i++) {
                equalsCall.append(functionBinding.getArguments().get(i));
                stackTypes.add(methodType.parameterType(i));
                equalsCall.append(ByteCodeUtils.ifWasNullPopAndGoto(context, end, unboxedReturnType, Lists.reverse(stackTypes)));
            }
            equalsCall.invokeDynamic(functionBinding.getName(), methodType, functionBinding.getBindingId());
            equalsCall.visitLabel(end);

            Block condition = new Block(context)
                    .append(equalsCall)
                    .putVariable("wasNull", false);

            elseValue = new IfStatement(context,
                    "when",
                    condition,
                    generatorContext.generate(result),
                    elseValue);
        }

        return block.append(elseValue);
    }
}
