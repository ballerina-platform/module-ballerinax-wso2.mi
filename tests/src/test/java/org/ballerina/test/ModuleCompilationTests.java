/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerina.test;

import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Package;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.text.LinePosition;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Perform module compilation related tests.
 *
 * @since 0.1.3
 */
public class ModuleCompilationTests {

    public static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/ballerina").toAbsolutePath();
    private static final Path DISTRIBUTION_PATH = Paths.get("../", "target", "ballerina-runtime")
            .toAbsolutePath();

    @Test
    public void testProjectCompilation() {
        Package pkg = loadPackage("project1");
        PackageCompilation packageCompilation = pkg.getCompilation();
        DiagnosticResult diagnosticResult = packageCompilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0);
    }

    @Test
    public void testUnsupportedParamAndReturnType() {
        Package pkg = loadPackage("project2");
        PackageCompilation packageCompilation = pkg.getCompilation();
        DiagnosticResult diagnosticResult = packageCompilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 2);
        Diagnostic[] diagnostics = diagnosticResult.diagnostics().toArray(new Diagnostic[0]);
        validateError(diagnostics, 0, "unsupported return type found", 5, 17);
        validateError(diagnostics, 1, "unsupported parameter type found", 12, 35);
    }

    @Test
    public void testErrorsOnServiceDefinition() {
        Package pkg = loadPackage("project3");
        PackageCompilation packageCompilation = pkg.getCompilation();
        DiagnosticResult diagnosticResult = packageCompilation.diagnosticResult();
        Diagnostic[] errorDiagnosticsList = diagnosticResult.diagnostics().stream()
                .filter(r -> r.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR))
                .toArray(Diagnostic[]::new);
        Assert.assertEquals(errorDiagnosticsList.length, 8);
        validateError(errorDiagnosticsList, 0,
                "service definition is not allowed when `ballerinax/mi` connector is in use",
                22, 1);
        validateError(errorDiagnosticsList, 1,
                "service definition is not allowed when `ballerinax/mi` connector is in use",
                28, 1);
        validateError(errorDiagnosticsList, 2,
                "listener declaration is not allowed when `ballerinax/mi` connector is in use", 40, 1);
        validateError(errorDiagnosticsList, 3,
                "defining variables with a type that has the shape of `Listener` is not allowed when the " +
                        "`ballerinax/mi` connector is in use.", 41, 1);
        validateError(errorDiagnosticsList, 4,
                "defining variables with a type that has the shape of `Listener` is not allowed when the " +
                        "`ballerinax/mi` connector is in use.", 49, 1);
        validateError(errorDiagnosticsList, 5,
                "defining variables with a type that has the shape of `Listener` is not allowed when the " +
                        "`ballerinax/mi` connector is in use.", 76, 1);
        validateError(errorDiagnosticsList, 6,
                "defining variables with a type that has the shape of `Listener` is not allowed when the " +
                        "`ballerinax/mi` connector is in use.", 117, 5);
        validateError(errorDiagnosticsList, 7,
                "defining variables with a type that has the shape of `Listener` is not allowed when the " +
                        "`ballerinax/mi` connector is in use.", 121, 5);
    }

    private void validateError(Diagnostic[] diagnostics, int errorIndex, String expectedErrMsg, int expectedErrLine,
                               int expectedErrCol) {
        Diagnostic diagnostic = diagnostics[errorIndex];
        Assert.assertEquals(diagnostic.message(), expectedErrMsg);
        LinePosition linePosition = diagnostic.location().lineRange().startLine();
        Assert.assertEquals(linePosition.line() + 1, expectedErrLine);
        Assert.assertEquals(linePosition.offset() + 1, expectedErrCol);
    }

    static Package loadPackage(String path) {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve(path);
        Environment environment = EnvironmentBuilder.getBuilder().setBallerinaHome(DISTRIBUTION_PATH).build();
        ProjectEnvironmentBuilder projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);
        BuildProject project = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        return project.currentPackage();
    }
}
