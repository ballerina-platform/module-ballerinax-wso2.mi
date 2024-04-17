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

package io.ballerina.mi.cmd;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.ProjectLoader;
import picocli.CommandLine;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@CommandLine.Command(name = "mi", description = "Generate MI connector")
public class MiCmd implements BLauncherCmd {
    private final PrintStream printStream;
    private static final String CMD_NAME = "mi";

    public MiCmd() {
        this.printStream = System.out;
    }

    @CommandLine.Option(names = {"--help", "-h"}, usageHelp = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--input", "-i"}, description = "Ballerina project path")
    private String sourcePath;

    @Override
    public void execute() {
        if (sourcePath == null) {
            StringBuilder stringBuilder = new StringBuilder();
            printUsage(stringBuilder);
            printStream.println(stringBuilder);
            return;
        }

        Path path = Path.of(sourcePath);
        Project project = ProjectLoader.loadProject(path);
        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_17);
        Path bin = path.resolve("target").resolve("bin");
        try {
            if (!Files.exists(bin)) {
                Files.createDirectory(bin);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Path executablePath = bin.resolve(project.currentPackage().descriptor().name().value() + ".jar");
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.EXEC, executablePath);
        if (packageCompilation.diagnosticResult().diagnosticCount() > 0) {
            printStream.println("Errors in compiling Ballerina project");
        }
    }

    @Override
    public String getName() {
        return CMD_NAME;
    }

    @Override
    public void printLongDesc(StringBuilder stringBuilder) {
        Class<?> clazz = MiCmd.class;
        ClassLoader classLoader = clazz.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("cli-docs/mi.help");
        if (inputStream != null) {
            try (InputStreamReader inputStreamREader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(inputStreamREader)) {
                String content = br.readLine();
                printStream.append(content);
                while ((content = br.readLine()) != null) {
                    printStream.append('\n').append(content);
                }
            } catch (IOException e) {
                printStream.println("Helper text is not available.");
            }
        }
    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {

    }

    @Override
    public void setParentCmdParser(picocli.CommandLine commandLine) {

    }
}
