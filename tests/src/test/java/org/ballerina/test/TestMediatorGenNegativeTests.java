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
import io.ballerina.projects.Project;
import io.ballerina.projects.Package;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LinePosition;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TestMediatorGenNegativeTests {
    public static final Path RES_DIR = Paths.get("src/test/resources/ballerina").toAbsolutePath();

    @Test
    public void test() {
        Path path = RES_DIR.resolve("project4");
        Project project = ProjectLoader.loadProject(path);
        Package pkg = project.currentPackage();
        PackageCompilation packageCompilation = pkg.getCompilation();
        DiagnosticResult diagnosticResult = packageCompilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 2);
        Diagnostic[] diagnostics = diagnosticResult.diagnostics().toArray(new Diagnostic[0]);
        validateError(diagnostics, 0, "unsupported return type found", 5, 17);
        validateError(diagnostics, 1, "unsupported parameter type found", 12, 35);
    }

    private void validateError(Diagnostic[] diagnostics, int errorIndex, String expectedErrMsg, int expectedErrLine,
                                     int expectedErrCol) {
        Diagnostic diagnostic = diagnostics[errorIndex];
        Assert.assertEquals(diagnostic.message(), expectedErrMsg);
        LinePosition linePosition = diagnostic.location().lineRange().startLine();
        Assert.assertEquals(linePosition.line() + 1, expectedErrLine);
        Assert.assertEquals(linePosition.offset() + 1, expectedErrCol);
    }
}
