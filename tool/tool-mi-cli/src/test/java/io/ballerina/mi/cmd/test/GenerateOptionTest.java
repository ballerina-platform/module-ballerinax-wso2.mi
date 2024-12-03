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

package io.ballerina.mi.cmd.test;

import io.ballerina.mi.cmd.MiCmd;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.projects.util.ProjectConstants.USER_DIR;

import org.testng.Assert;
import org.testng.annotations.Test;

public class GenerateOptionTest {

    private final Path BASE_PATH = Paths.get("src", "test", "resources", "ballerina");
    private final Path PACKAGE_PATH = BASE_PATH.resolve("gen");
    private final ByteArrayOutputStream outputErrStream = new ByteArrayOutputStream();

    @BeforeTest
    public void setup() {
        System.setErr(new PrintStream(outputErrStream));
    }

    @Test
    public void testGenerateCommand() {
        System.setProperty(USER_DIR, PACKAGE_PATH.toAbsolutePath().toString());
        String[] args = {"-g", "gen.bal"};
        MiCmd miCmd = new MiCmd();
        new CommandLine(miCmd).parseArgs(args);
        miCmd.execute();
        Assert.assertTrue(Files.exists(PACKAGE_PATH.resolve("gen.bal")));
    }

    @Test
    public void testGenerateCommandWithExistingFileName() {
        System.setProperty(USER_DIR, PACKAGE_PATH.toAbsolutePath().toString());
        String[] args = {"-g", "main.bal"};
        MiCmd miCmd = new MiCmd();
        new CommandLine(miCmd).parseArgs(args);
        miCmd.execute();
        Assert.assertTrue(outputErrStream.toString()
                .contains("A file with the specified name already exists in the Ballerina Project"));
    }

    @Test
    public void testGenerateCommandWithNonBallerinaProject() {
        System.setProperty(USER_DIR, BASE_PATH.toAbsolutePath().toString());
        String[] args = {"-g", "main.bal"};
        MiCmd miCmd = new MiCmd();
        new CommandLine(miCmd).parseArgs(args);
        miCmd.execute();
        Assert.assertTrue(outputErrStream.toString().contains("Current directory is not a Ballerina Project"));
    }

    @AfterTest
    public void clean() throws IOException {
        Files.deleteIfExists(PACKAGE_PATH.resolve("gen.bal"));
    }

}
