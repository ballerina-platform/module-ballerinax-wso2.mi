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

import io.ballerina.compiler.api.impl.symbols.BallerinaFunctionSymbol;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;

import java.util.List;
import java.util.Optional;

import static io.ballerina.stdlib.mi.plugin.MICompilerPluginUtils.getParamTypeName;
import static io.ballerina.stdlib.mi.plugin.MICompilerPluginUtils.getReturnTypeName;

public class FunctionAnalysisTask implements AnalysisTask<SyntaxNodeAnalysisContext> {

    private final String ANNOTATION_NAME = "ConnectorInfo";

    @Override
    public void perform(SyntaxNodeAnalysisContext context) {
        Node node = context.node();
        Optional<Symbol> symbol = context.semanticModel().symbol(node);

        if (symbol.isEmpty() || !(symbol.get() instanceof BallerinaFunctionSymbol functionSymbol) ||
                !canAddMIAnnotation(functionSymbol)) {
            return;
        }
        DiagnosticCode diagnosticCode = DiagnosticCode.MI_ANNOTATION_ADD;
        context.reportDiagnostic(DiagnosticFactory.createDiagnostic(
                new DiagnosticInfo(diagnosticCode.diagnosticId(), diagnosticCode.message(), diagnosticCode.severity()),
                node.location()));
    }

    private boolean canAddMIAnnotation(BallerinaFunctionSymbol functionSymbol) {
        return !hasMIAnnotation(functionSymbol) && checkFunctionParametersAndQualifiers(functionSymbol);
    }

    private boolean hasMIAnnotation(BallerinaFunctionSymbol functionSymbol) {
        for (AnnotationSymbol annotationSymbol : functionSymbol.annotations()) {
            Optional<String> name = annotationSymbol.getName();
            if (name.isEmpty() || !name.get().equals(ANNOTATION_NAME)) {
                continue;
            }
            Optional<ModuleSymbol> module = annotationSymbol.getModule();
            if (module.isPresent() && module.get().nameEquals("mi")) {
                return true;
            }
        }
        return false;
    }

    private boolean checkFunctionParametersAndQualifiers(FunctionSymbol functionSymbol) {
        if (functionSymbol.qualifiers().stream().noneMatch(q -> q == Qualifier.PUBLIC)) {
            return false;
        }
        Optional<List<ParameterSymbol>> params = functionSymbol.typeDescriptor().params();
        if (params.isPresent()) {
            for (ParameterSymbol parameterSymbol : params.get()) {
                if (getParamTypeName(parameterSymbol.typeDescriptor().typeKind()) == null) {
                    return false;
                }
            }
        }
        Optional<TypeSymbol> optReturnTypeSymbol = functionSymbol.typeDescriptor().returnTypeDescriptor();
        return optReturnTypeSymbol.isEmpty() || getReturnTypeName(optReturnTypeSymbol.get().typeKind()) != null;
    }
}
