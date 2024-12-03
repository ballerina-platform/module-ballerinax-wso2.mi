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
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.tools.diagnostics.Diagnostic;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@CommandLine.Command(name = "mi", description = "Generate MI connector")
public class MiCmd implements BLauncherCmd {

    private static final String CMD_NAME = "mi";
    private final String HELP_FILE = "cli-docs/mi.help";
    private final PrintStream printStream;
    private final PrintStream printErrStream;
    @CommandLine.Option(names = {"--help", "-h"}, usageHelp = true)
    private boolean helpFlag;
    @CommandLine.Option(names = {"--input", "-i"}, description = "Ballerina project path")
    private String sourcePath;
    @CommandLine.Option(names = {"--generate-template", "-g"}, description = "Ballerina file name")
    private String fileName;

    public MiCmd() {
        this.printStream = System.out;
        this.printErrStream = System.err;
    }

    @Override
    public void execute() {
        if (sourcePath == null && fileName == null && helpFlag) {
            StringBuilder stringBuilder = new StringBuilder();
            readResourceFile(HELP_FILE, stringBuilder);
            printStream.println(stringBuilder);
            return;
        }

        if (fileName != null) {
            Path currentDir = Path.of(System.getProperty(ProjectConstants.USER_DIR));
            if (!ProjectUtils.isBallerinaProject(currentDir)) {
                printErrStream.println("Current directory is not a Ballerina Project");
                return;
            }

            Path targetFile = currentDir.resolve(fileName);
            if (Files.exists(targetFile)) {
                printErrStream.println("A file with the specified name already exists in the Ballerina Project");
                return;
            }

            StringBuilder stringBuilder = new StringBuilder();
            readResourceFile("generate_cmd_templates/generate.bal", stringBuilder);
            try {
                Files.createFile(currentDir.resolve(fileName));
                Files.writeString(targetFile, stringBuilder);
            } catch (IOException e) {
                printErrStream.println("Could not create the file due to: " + e.getMessage());
            }
            return;
        }

        Path path = Path.of(sourcePath);
        Project project = ProjectLoader.loadProject(path);
        Package pkg = project.currentPackage();
        PackageCompilation packageCompilation = pkg.getCompilation();
        for (Diagnostic diagnostic : packageCompilation.diagnosticResult().diagnostics()) {
            printStream.println(diagnostic.toString());
            return;
        }
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_17);
        Path bin = path.resolve("target").resolve("bin");
        try {
            createBinFolder(bin);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Path executablePath = bin.resolve(pkg.descriptor().name().value() + ".jar");
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.EXEC, executablePath);
        if (packageCompilation.diagnosticResult().diagnosticCount() > 0) {
            printStream.println("Errors in compiling Ballerina project");
        }
    }

    private void createBinFolder(Path bin) throws IOException {
        File[] files = bin.toFile().listFiles();
        if (files != null) {
            for (File file : Objects.requireNonNull(files)) {
                file.delete();
            }
        }
        Files.deleteIfExists(bin);
        Files.createDirectory(bin);
    }

    @Override
    public String getName() {
        return CMD_NAME;
    }

    @Override
    public void printLongDesc(StringBuilder stringBuilder) {
        readResourceFile(HELP_FILE, stringBuilder);
    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {

    }

    @Override
    public void setParentCmdParser(picocli.CommandLine commandLine) {

    }

    private void readResourceFile(String resource, StringBuilder stringBuilder) {
        Class<?> clazz = MiCmd.class;
        ClassLoader classLoader = clazz.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(resource);
        if (inputStream != null) {
            try (InputStreamReader inputStreamREader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(inputStreamREader)) {
                String content = br.readLine();
                stringBuilder.append(content);
                while ((content = br.readLine()) != null) {
                    stringBuilder.append('\n').append(content);
                }
            } catch (IOException e) {
                stringBuilder.append(e.getMessage());
            }
        }
    }
}
