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

package org.ballerina.test;

import com.google.gson.Gson;
import io.ballerina.projects.CodeActionManager;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.plugins.codeaction.CodeActionArgument;
import io.ballerina.projects.plugins.codeaction.CodeActionContextImpl;
import io.ballerina.projects.plugins.codeaction.CodeActionExecutionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionExecutionContextImpl;
import io.ballerina.projects.plugins.codeaction.CodeActionInfo;
import io.ballerina.projects.plugins.codeaction.DocumentEdit;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AnnotationTest {

    private final Gson GSON = new Gson();
    private final Path RESOURCE_PATH = Paths.get("src", "test", "resources", "ballerina", "annotation_code_action");
    private final String PROVIDER_NAME = "MI_HINT_001/ballerinax/mi/Add MI annotation";
    private Project project;
    private Path filePath;
    private Project projectNeg;
    private Path filePathNeg;

    @BeforeClass
    public void setup() {
        filePath = RESOURCE_PATH.resolve("add_mi.bal");
        filePathNeg = RESOURCE_PATH.resolve("add_mi_neg.bal");
        project = ProjectLoader.loadProject(filePath,
                ProjectEnvironmentBuilder.getBuilder(EnvironmentBuilder.getBuilder().build()));
        projectNeg = ProjectLoader.loadProject(filePathNeg,
                ProjectEnvironmentBuilder.getBuilder(EnvironmentBuilder.getBuilder().build()));
    }

    @Test(dataProvider = "testMIAnnotationDataProvider")
    public void testMIAnnotationAddCodeAction(LinePosition linePosition) {
        Package currentPackage = project.currentPackage();
        PackageCompilation compilation = currentPackage.getCompilation();
        CodeActionManager codeActionManager = compilation.getCodeActionManager();

        List<CodeActionInfo> codeActions =
                getCodeActions(filePath, linePosition, project, currentPackage, compilation, codeActionManager);
        Optional<CodeActionInfo> found = getMIAnnotationCodeActionInfo(codeActions);
        Assert.assertTrue(found.isPresent(), "Codeaction not found for line position: " + linePosition);
        Assert.assertEquals(executeCodeAction(project, filePath, found.get(), currentPackage, compilation).size(), 1,
                "Expected a change to the file");
    }

    @DataProvider(name = "testMIAnnotationDataProvider")
    public Object[] testMIAnnotationDataProvider() {
        return new Object[]{
                LinePosition.from(18, 0), LinePosition.from(22, 0), LinePosition.from(26, 0)
        };
    }

    @Test(dataProvider = "testMIAnnotationNegativeDataProvider")
    public void testMIAnnotationAddCodeActionNegative(LinePosition linePosition) {
        Package currentPackage = projectNeg.currentPackage();
        PackageCompilation compilation = currentPackage.getCompilation();
        CodeActionManager codeActionManager = compilation.getCodeActionManager();

        List<CodeActionInfo> codeActions =
                getCodeActions(filePathNeg, linePosition, projectNeg, currentPackage, compilation, codeActionManager);
        Assert.assertEquals(codeActions.size(), 0, "Did not expect MI Annotation code action at: " + linePosition);
    }

    @DataProvider(name = "testMIAnnotationNegativeDataProvider")
    public Object[] testMIAnnotationNegativeDataProvider() {
        return new Object[]{
                LinePosition.from(24, 0), LinePosition.from(28, 0), LinePosition.from(16, 0),
                LinePosition.from(32, 0), LinePosition.from(36, 0), LinePosition.from(40, 0),
                LinePosition.from(44, 0), LinePosition.from(48, 0)
        };
    }

    private Optional<CodeActionInfo> getMIAnnotationCodeActionInfo(List<CodeActionInfo> codeActions) {
        return codeActions.stream()
                .filter(codeActionInfo -> codeActionInfo.getProviderName().equals(PROVIDER_NAME))
                .findFirst();
    }

    private List<CodeActionInfo> getCodeActions(Path filePath, LinePosition cursorPos, Project project,
                                                Package currentPackage, PackageCompilation compilation,
                                                CodeActionManager codeActionManager) {
        DocumentId documentId = project.documentId(filePath);
        Document document = currentPackage.getDefaultModule().document(documentId);

        return compilation.diagnosticResult().diagnostics().stream()
                .filter(diagnostic -> isWithinRange(diagnostic.location().lineRange(), cursorPos) &&
                        filePath.endsWith(diagnostic.location().lineRange().filePath()))
                .flatMap(diagnostic -> {
                    CodeActionContextImpl context = CodeActionContextImpl.from(
                            filePath.toUri().toString(), filePath, cursorPos, document,
                            compilation.getSemanticModel(documentId.moduleId()), diagnostic);
                    return codeActionManager.codeActions(context).getCodeActions().stream();
                })
                .collect(Collectors.toList());
    }

    private List<DocumentEdit> executeCodeAction(Project project, Path filePath, CodeActionInfo codeAction,
                                                 Package currentPackage, PackageCompilation compilation) {
        DocumentId documentId = project.documentId(filePath);
        Document document = currentPackage.getDefaultModule().document(documentId);

        List<CodeActionArgument> codeActionArguments = codeAction.getArguments().stream()
                .map(arg -> CodeActionArgument.from(GSON.toJsonTree(arg)))
                .collect(Collectors.toList());

        CodeActionExecutionContext executionContext = CodeActionExecutionContextImpl.from(
                filePath.toUri().toString(),
                filePath,
                null,
                document,
                compilation.getSemanticModel(document.documentId().moduleId()),
                codeActionArguments);

        return compilation.getCodeActionManager()
                .executeCodeAction(codeAction.getProviderName(), executionContext);
    }

    private static boolean isWithinRange(LineRange lineRange, LinePosition pos) {
        int sLine = lineRange.startLine().line();
        int sCol = lineRange.startLine().offset();
        int eLine = lineRange.endLine().line();
        int eCol = lineRange.endLine().offset();

        return ((sLine == eLine && pos.line() == sLine) &&
                (pos.offset() >= sCol && pos.offset() <= eCol)
        ) || ((sLine != eLine) && (pos.line() > sLine && pos.line() < eLine ||
                pos.line() == eLine && pos.offset() <= eCol ||
                pos.line() == sLine && pos.offset() >= sCol
        ));
    }
}
