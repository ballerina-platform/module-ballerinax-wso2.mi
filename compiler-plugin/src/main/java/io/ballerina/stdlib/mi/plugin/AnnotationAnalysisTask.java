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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.List;
import java.util.Optional;

import static io.ballerina.stdlib.mi.plugin.DiagnosticErrorCode.UNSUPPORTED_PARAM_TYPE;
import static io.ballerina.stdlib.mi.plugin.DiagnosticErrorCode.UNSUPPORTED_RETURN_TYPE;

public class AnnotationAnalysisTask implements AnalysisTask<SyntaxNodeAnalysisContext> {

    private static final String ANNOTATION_QUALIFIER = "ConnectorInfo";

    private static void checkParametersAndReturnType(SyntaxNodeAnalysisContext context, FunctionSymbol functionSymbol) {
        Optional<List<ParameterSymbol>> params = functionSymbol.typeDescriptor().params();
        if (params.isPresent()) {
            for (ParameterSymbol parameterSymbol : params.get()) {
                String paramType = getParamTypeName(parameterSymbol.typeDescriptor().typeKind());
                if (paramType == null) {
                    DiagnosticInfo diagnosticInfo = new DiagnosticInfo(UNSUPPORTED_PARAM_TYPE.diagnosticId(),
                            UNSUPPORTED_PARAM_TYPE.message(), DiagnosticSeverity.ERROR);
                    context.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                            parameterSymbol.getLocation().get()));
                }
            }
        }

        Optional<TypeSymbol> optReturnTypeSymbol = functionSymbol.typeDescriptor().returnTypeDescriptor();
        if (optReturnTypeSymbol.isEmpty()) {
            return;
        }
        String returnType = getReturnTypeName(optReturnTypeSymbol.get().typeKind());
        if (returnType == null) {
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(UNSUPPORTED_RETURN_TYPE.diagnosticId(),
                    UNSUPPORTED_RETURN_TYPE.message(), DiagnosticSeverity.ERROR);
            context.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                    functionSymbol.getLocation().get()));
        }
    }

    private static String getParamTypeName(TypeDescKind typeKind) {
        return switch (typeKind) {
            case BOOLEAN, INT, STRING, FLOAT, DECIMAL, XML, JSON -> typeKind.getName();
            default -> null;
        };
    }

    private static String getReturnTypeName(TypeDescKind typeKind) {
        return switch (typeKind) {
            case NIL, BOOLEAN, INT, STRING, FLOAT, DECIMAL, XML, JSON, ANY -> typeKind.getName();
            default -> null;
        };
    }

    private static FunctionSymbol getFunctionSymbol(SyntaxNodeAnalysisContext context, SemanticModel semanticModel) {
        if (!(context.node() instanceof AnnotationNode annotationNode)) return null;
        Optional<Symbol> symbol = semanticModel.symbol(annotationNode);
        if (symbol.isEmpty()) return null;
        if (!(symbol.get() instanceof AnnotationSymbol annotationSymbol)) return null;
        Optional<String> annotationName = annotationSymbol.getName();
        if (annotationName.isEmpty()) return null;
        if (!annotationName.get().equals(ANNOTATION_QUALIFIER)) return null;

        if (!(annotationNode.parent().parent() instanceof FunctionDefinitionNode functionNode)) return null;
        Optional<Symbol> funcSymbol = semanticModel.symbol(functionNode);
        if (funcSymbol.isEmpty()) return null;
        if (!(funcSymbol.get() instanceof FunctionSymbol functionSymbol)) return null;
        return functionSymbol;
    }

    @Override
    public void perform(SyntaxNodeAnalysisContext context) {
        SemanticModel semanticModel = context.compilation().getSemanticModel(context.moduleId());
        FunctionSymbol functionSymbol = getFunctionSymbol(context, semanticModel);
        if (functionSymbol == null) return;

        Optional<String> functionName = functionSymbol.getName();
        if (functionName.isEmpty()) return;
        checkParametersAndReturnType(context, functionSymbol);
    }
}
