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

import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.directory.ProjectLoader;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestMediatorContent {
    public static final Path RES_DIR = Paths.get("src/test/resources/ballerina").toAbsolutePath();

    @Test(dataProvider = "data-provider")
    public void test(String projectPath) throws IOException {
        Path path = getProjectPath(projectPath);
        Project project = ProjectLoader.loadProject(path);
        Package pkg = project.currentPackage();
        PackageCompilation packageCompilation = pkg.getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_17);
        Path jarPath = path.resolve("test.jar");
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.EXEC, jarPath);
        Assert.assertEquals(packageCompilation.diagnosticResult().diagnosticCount(), 0);
        // TODO: Extract the zip file and verify the content
        Files.delete(jarPath);
        Files.delete(Paths.get(projectPath + "-connector-0.1.0.zip").toAbsolutePath());
    }

    @DataProvider(name = "data-provider")
    public Object[][] dataProvider() {
        return new Object[][]{
                {"project1"}
        };
    }

    private Path getProjectPath(String path) {
        return RES_DIR.resolve(path);
    }
}
