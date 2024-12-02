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
import io.ballerina.projects.BallerinaToml;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.stdlib.mi.plugin.model.Component;
import io.ballerina.stdlib.mi.plugin.model.Connector;
import io.ballerina.stdlib.mi.plugin.model.FunctionParam;
import io.ballerina.stdlib.mi.plugin.model.Param;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlStringValueNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.List;
import java.util.Optional;

import static io.ballerina.stdlib.mi.plugin.DiagnosticCode.UNSUPPORTED_PARAM_TYPE;
import static io.ballerina.stdlib.mi.plugin.DiagnosticCode.UNSUPPORTED_RETURN_TYPE;

public class AnnotationAnalysisTask implements AnalysisTask<SyntaxNodeAnalysisContext> {

    private static final String FUNCTION_NAME = "FunctionName";
    private static final String SIZE = "Size";

    private static void setParameters(SyntaxNodeAnalysisContext context, FunctionSymbol functionSymbol,
                                      Component component) {
        int noOfParams = 0;
        Optional<List<ParameterSymbol>> params = functionSymbol.typeDescriptor().params();
        if (params.isPresent()) {
            List<ParameterSymbol> parameterSymbols = params.get();
            noOfParams = parameterSymbols.size();

            for (int i = 0; i < noOfParams; i++) {
                ParameterSymbol parameterSymbol = parameterSymbols.get(i);
                String paramType = getParamTypeName(parameterSymbol.typeDescriptor().typeKind());
                if (paramType == null) {
                    DiagnosticInfo diagnosticInfo = new DiagnosticInfo(UNSUPPORTED_PARAM_TYPE.diagnosticId(),
                            UNSUPPORTED_PARAM_TYPE.message(), DiagnosticSeverity.ERROR);
                    context.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                            parameterSymbol.getLocation().get()));
                } else {
                    Optional<String> optParamName = parameterSymbol.getName();
                    if (optParamName.isPresent()) {
                        FunctionParam param = new FunctionParam(Integer.toString(i), optParamName.get(), paramType);
                        component.addBalFuncParams(param);
                    }
                }
            }
        }
        Param sizeParam = new Param(SIZE, Integer.toString(noOfParams));
        Param functionNameParam = new Param(FUNCTION_NAME, component.getName());
        component.setParam(sizeParam);
        component.setParam(functionNameParam);
        Optional<TypeSymbol> optReturnTypeSymbol = functionSymbol.typeDescriptor().returnTypeDescriptor();
        if (optReturnTypeSymbol.isEmpty()) {
            component.setBalFuncReturnType(TypeDescKind.NIL.getName());
        } else {
            String returnType = getReturnTypeName(optReturnTypeSymbol.get().typeKind());
            if (returnType == null) {
                DiagnosticInfo diagnosticInfo = new DiagnosticInfo(UNSUPPORTED_RETURN_TYPE.diagnosticId(),
                        UNSUPPORTED_RETURN_TYPE.message(), DiagnosticSeverity.ERROR);
                context.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                        functionSymbol.getLocation().get()));
            } else {
                component.setBalFuncReturnType(returnType);
            }
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

    private static void setIcon(BallerinaToml ballerinaToml, Connector connector) {
        if (connector.getIconPath() != null) return;
        TomlDocument tomlDocument = ballerinaToml.tomlDocument();
        TomlTableNode path = (TomlTableNode) tomlDocument.toml().rootNode().entries().get(Connector.TOML_ICON_NODE);
        if (path == null) return;

        TomlKeyValueNode iconPath = (TomlKeyValueNode) path.entries().get(Connector.TOML_ICON_KEY);
        if (iconPath == null) return;

        TomlStringValueNode valuePath = (TomlStringValueNode) iconPath.value();
        connector.setIconPath(valuePath.getValue());
    }

    private static FunctionSymbol getFunctionSymbol(SyntaxNodeAnalysisContext context, SemanticModel semanticModel) {
        if (!(context.node() instanceof AnnotationNode annotationNode)) return null;
        Optional<Symbol> symbol = semanticModel.symbol(annotationNode);
        if (symbol.isEmpty()) return null;
        if (!(symbol.get() instanceof AnnotationSymbol annotationSymbol)) return null;
        Optional<String> annotationName = annotationSymbol.getName();
        if (annotationName.isEmpty()) return null;
        if (!annotationName.get().equals(Component.ANNOTATION_QUALIFIER)) return null;

        if (!(annotationNode.parent().parent() instanceof FunctionDefinitionNode functionNode)) return null;
        Optional<Symbol> funcSymbol = semanticModel.symbol(functionNode);
        if (funcSymbol.isEmpty()) return null;
        if (!(funcSymbol.get() instanceof FunctionSymbol functionSymbol)) return null;
        return functionSymbol;
    }

    private static void setModuleInfo(PackageDescriptor descriptor, Connector connector) {
        String orgName = descriptor.org().value();
        String moduleName = descriptor.name().value();
        String version = String.valueOf(descriptor.version().value().major());

        if (connector.getOrgName() == null) {
            connector.setOrgName(orgName);
        }
        if (connector.getModuleName() == null) {
            connector.setModuleName(moduleName);
        }
        if (connector.getModuleVersion() == null) {
            connector.setModuleVersion(version);
        }
    }

    @Override
    public void perform(SyntaxNodeAnalysisContext context) {

        SemanticModel semanticModel = context.compilation().getSemanticModel(context.moduleId());

        FunctionSymbol functionSymbol = getFunctionSymbol(context, semanticModel);
        if (functionSymbol == null) return;

        Optional<BallerinaToml> ballerinaToml = context.currentPackage().ballerinaToml();
        if (ballerinaToml.isEmpty()) return;

        Connector connector = Connector.getConnector();
        PackageDescriptor descriptor = context.currentPackage().manifest().descriptor();
        setIcon(ballerinaToml.get(), connector);
        setModuleInfo(descriptor, connector);

        Optional<String> functionName = functionSymbol.getName();
        if (functionName.isEmpty()) return;
        Component component = new Component(functionName.get());
        setParameters(context, functionSymbol, component);

        connector.setComponent(component);
    }
}
