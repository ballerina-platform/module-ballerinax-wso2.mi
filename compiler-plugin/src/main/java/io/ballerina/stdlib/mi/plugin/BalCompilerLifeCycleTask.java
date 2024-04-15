package io.ballerina.stdlib.mi.plugin;

import io.ballerina.projects.plugins.CompilerLifecycleEventContext;
import io.ballerina.projects.plugins.CompilerLifecycleTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BalCompilerLifeCycleTask implements CompilerLifecycleTask<CompilerLifecycleEventContext> {
    @Override
    public void perform(CompilerLifecycleEventContext compilerLifecycleEventContext) {
        String sourcePathStr = compilerLifecycleEventContext.getGeneratedArtifactPath().get().toString();
        String destinationPathStr = "connector" + File.separator + "lib" + File.separator;

        Path sourcePath = Paths.get(sourcePathStr);
        Path destinationPath = Paths.get(destinationPathStr);

        try {
            // Copy the JAR file from source to destination
            Files.copy(sourcePath, destinationPath.resolve(sourcePath.getFileName()));
            System.out.println("JAR file copied successfully.");
        } catch (IOException e) {
            System.err.println("Error occurred while copying the JAR file: " + e.getMessage());
        }

        try {
            Utils.zipFolder("connector", "BallerinaTransformer-connector-1.0-SNAPSHOT.zip");
//            Utils.deleteDirectory("connector");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
