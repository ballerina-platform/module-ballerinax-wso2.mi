package io.ballerina.stdlib.mi.plugin;

import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.plugins.CompilerLifecycleEventContext;
import io.ballerina.projects.plugins.CompilerLifecycleTask;
import io.ballerina.stdlib.mi.plugin.model.Component;
import io.ballerina.stdlib.mi.plugin.model.Connector;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BalCompilerLifeCycleTask implements CompilerLifecycleTask<CompilerLifecycleEventContext> {

    @Override
    public void perform(CompilerLifecycleEventContext context) {


        String sourcePathStr = context.getGeneratedArtifactPath().get().toString();
        Path sourcePath = Paths.get(sourcePathStr);

        PackageDescriptor descriptor = context.currentPackage().manifest().descriptor();
        String moduleName = descriptor.name().value();

        Path destinationPath = context.currentPackage().project().sourceRoot().resolve(Connector.TYPE_NAME);

        Connector connector = Connector.getConnector();
        connector.setName(moduleName);
        connector.setVersion(descriptor.version().value().toString());

        // Generate all the xml files
        createXmlFiles(destinationPath, connector);

        // Copy the resources from the resources folder to the connector folder
        try {
            Utils.moveResources(getClass().getClassLoader(), destinationPath);
        } catch (IOException | URISyntaxException e ) {
            throw new RuntimeException(e);
        }

        // Copy the Ballerina JAR file to the connector folder
        try {
            // Copy the JAR file from source to destination
            Files.copy(sourcePath, destinationPath.resolve("lib").resolve(sourcePath.getFileName()));
            System.out.println("JAR file copied successfully.");
        } catch (IOException e) {
            System.err.println("Error occurred while copying the JAR file: " + e.getMessage());
        }

        // Zip the connector folder and delete the folder
        try {
            Utils.zipFolder(destinationPath, connector.getZipFileName());
            Utils.deleteDirectory(destinationPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createXmlFiles(Path connectorFolderPath, Connector connector) {
        File connectorFolder = new File(connectorFolderPath.toUri());
        if (!connectorFolder.exists()) {
            connectorFolder.mkdir();
        }

        connector.generateInstanceXml(connectorFolder); //This need to be done after all the function definitions are analyzed

        for(Component component : connector.getComponents()){
            component.generateInstanceXml(connectorFolder);
            component.generateTemplateXml(connectorFolder);
        }
    }
}
