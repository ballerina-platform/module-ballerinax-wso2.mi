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

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.plugins.CodeAnalysisContext;
import io.ballerina.projects.plugins.CodeAnalyzer;

import java.util.List;

/**
 * Compiler plugin for Ballerina MI Mediator
 *
 * @since 0.1.0
 */
public class BalMediatorCodeAnalyzer extends CodeAnalyzer {

    @Override
    public void init(CodeAnalysisContext codeAnalysisContext) {
        codeAnalysisContext.addSyntaxNodeAnalysisTask(
                new AnnotationAnalysisTask(), SyntaxKind.ANNOTATION
        );
        codeAnalysisContext.addSyntaxNodeAnalysisTask(new ListenerAndServiceDefAnalysisTask(),
                List.of(SyntaxKind.SERVICE_DECLARATION, SyntaxKind.LISTENER_DECLARATION));
        codeAnalysisContext.addSyntaxNodeAnalysisTask(new VariableDeclarationAnalysisTask(),
                List.of(SyntaxKind.MODULE_VAR_DECL, SyntaxKind.LOCAL_VAR_DECL));
        codeAnalysisContext.addSyntaxNodeAnalysisTask(new FunctionAnalysisTask(), SyntaxKind.FUNCTION_DEFINITION);
    }
}
