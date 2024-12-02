/*
 *
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *  *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.ballerina.stdlib.mi.plugin.codeaction;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.plugins.codeaction.CodeActionArgument;
import io.ballerina.projects.plugins.codeaction.CodeActionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionExecutionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionInfo;
import io.ballerina.projects.plugins.codeaction.DocumentEdit;
import io.ballerina.stdlib.mi.plugin.DiagnosticCode;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocumentChange;
import io.ballerina.tools.text.TextEdit;
import io.ballerina.tools.text.TextRange;
import io.ballerina.projects.plugins.codeaction.CodeAction;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Code action that adds the MI annotation to a ballerina function.
 *
 * @since 0.1.4
 */
public class MIAnnotationCodeAction implements CodeAction {

    private final String NAME = "Add MI annotation";
    private final String NODE_LOCATION_KEY = "node.location";
    private final String ANNOTATION_STRING = "@mi:ConnectorInfo\n";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public List<String> supportedDiagnosticCodes() {
        return List.of(DiagnosticCode.MI_ANNOTATION_ADD.diagnosticId());
    }

    @Override
    public Optional<CodeActionInfo> codeActionInfo(CodeActionContext context) {
        NonTerminalNode node =
                findNode(context.currentDocument().syntaxTree(), context.diagnostic().location().lineRange());
        if (node == null || node.kind() != SyntaxKind.FUNCTION_DEFINITION) {
            return Optional.empty();
        }

        CodeActionArgument locationArg = CodeActionArgument.from(NODE_LOCATION_KEY, node.location().lineRange());
        return Optional.of(CodeActionInfo.from(NAME, List.of(locationArg)));

    }

    @Override
    public List<DocumentEdit> execute(CodeActionExecutionContext context) {
        Optional<LineRange> lineRange = getLineRangeFromLocationKey(context);

        if (lineRange.isEmpty()) {
            return Collections.emptyList();
        }

        SyntaxTree syntaxTree = context.currentDocument().syntaxTree();
        NonTerminalNode node = findNode(syntaxTree, lineRange.get());

        int start = getFunctionStartOffset((FunctionDefinitionNode) node);
        TextRange textRange = TextRange.from(start, 0);
        List<TextEdit> textEdits = List.of(TextEdit.from(textRange, ANNOTATION_STRING));
        TextDocumentChange change = TextDocumentChange.from(textEdits.toArray(new TextEdit[0]));
        TextDocument modifiedTextDocument = syntaxTree.textDocument().apply(change);
        return Collections.singletonList(new DocumentEdit(context.fileUri(), SyntaxTree.from(modifiedTextDocument)));
    }

    private NonTerminalNode findNode(SyntaxTree syntaxTree, LineRange lineRange) {
        if (lineRange == null) {
            return null;
        }

        TextDocument textDocument = syntaxTree.textDocument();
        int start = textDocument.textPositionFrom(lineRange.startLine());
        int end = textDocument.textPositionFrom(lineRange.endLine());
        return ((ModulePartNode) syntaxTree.rootNode()).findNode(TextRange.from(start, end - start), true);
    }

    private Optional<LineRange> getLineRangeFromLocationKey(CodeActionExecutionContext context) {
        for (CodeActionArgument argument : context.arguments()) {
            if (NODE_LOCATION_KEY.equals(argument.key())) {
                return Optional.of(argument.valueAs(LineRange.class));
            }
        }
        return Optional.empty();
    }

    private int getFunctionStartOffset(FunctionDefinitionNode node) {
        if (node.qualifierList().size() > 0) {
            return node.qualifierList().get(0).textRange().startOffset();
        }
        return node.functionKeyword().textRange().startOffset();
    }

}
