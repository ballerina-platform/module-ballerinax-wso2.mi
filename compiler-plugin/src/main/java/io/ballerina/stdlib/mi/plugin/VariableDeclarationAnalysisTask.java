/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.stdlib.mi.plugin;

import io.ballerina.compiler.api.impl.symbols.BallerinaUnionTypeSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Analysis task that emits error diagnostics for local and module-level variable declarations with a type that match
 * the shape of the `Listener` type found in the code.
 *
 * @since 0.1.3
 */
public class VariableDeclarationAnalysisTask implements AnalysisTask<SyntaxNodeAnalysisContext> {

    private final List<String> methods = List.of("start", "gracefulStop", "immediateStop", "attach", "detach");

    @Override
    public void perform(SyntaxNodeAnalysisContext context) {
        Optional<Symbol> symbol = context.semanticModel().symbol(context.node());
        if (symbol.isEmpty() || !(symbol.get() instanceof VariableSymbol)) {
            return;
        }

        TypeSymbol typeSymbol = getRawType(((VariableSymbol) symbol.get()).typeDescriptor());
        List<ObjectTypeSymbol> objectTypeSymbols = new ArrayList<>();
        if (typeSymbol.typeKind() == TypeDescKind.UNION) {
            objectTypeSymbols = getObjectTypeMembers((BallerinaUnionTypeSymbol) typeSymbol);
        } else if (typeSymbol.typeKind() == TypeDescKind.OBJECT) {
            objectTypeSymbols.add((ObjectTypeSymbol) typeSymbol);
        }

        for (ObjectTypeSymbol objectTypeSymbol : objectTypeSymbols) {
            List<String> classMethods = objectTypeSymbol.methods().keySet().stream().toList();
            if (!classMethods.containsAll(methods)) {
                continue;
            }
            DiagnosticErrorCode diagnosticCode = DiagnosticErrorCode.LISTENER_SHAPE_VAR_NOT_ALLOWED;
            DiagnosticInfo diagnosticInfo =
                    new DiagnosticInfo(diagnosticCode.diagnosticId(), diagnosticCode.message(),
                            DiagnosticSeverity.ERROR);
            context.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo, context.node().location()));
            return;
        }
    }

    private TypeSymbol getRawType(TypeSymbol typeDescriptor) {
        if (typeDescriptor.typeKind() == TypeDescKind.INTERSECTION) {
            return getRawType(((IntersectionTypeSymbol) typeDescriptor).effectiveTypeDescriptor());
        }
        if (typeDescriptor.typeKind() == TypeDescKind.TYPE_REFERENCE) {
            TypeReferenceTypeSymbol typeRef = (TypeReferenceTypeSymbol) typeDescriptor;
            if (typeRef.typeDescriptor().typeKind() == TypeDescKind.INTERSECTION) {
                return getRawType(((IntersectionTypeSymbol) typeRef.typeDescriptor()).effectiveTypeDescriptor());
            }
            TypeSymbol rawType = typeRef.typeDescriptor();
            if (rawType.typeKind() == TypeDescKind.TYPE_REFERENCE) {
                return getRawType(rawType);
            }
            return rawType;
        }
        return typeDescriptor;
    }

    private List<ObjectTypeSymbol> getObjectTypeMembers(BallerinaUnionTypeSymbol unionTypeSymbol) {
        ArrayList<ObjectTypeSymbol> objectTypes = new ArrayList<>();
        for (TypeSymbol member : unionTypeSymbol.memberTypeDescriptors()) {
            member = getRawType(member);
            TypeDescKind typeDescKind = member.typeKind();
            if (typeDescKind == TypeDescKind.OBJECT) {
                objectTypes.add((ObjectTypeSymbol) member);
            } else if (typeDescKind == TypeDescKind.UNION) {
                objectTypes.addAll(getObjectTypeMembers((BallerinaUnionTypeSymbol) member));
            }
        }
        return objectTypes;
    }
}
