package io.ballerina.stdlib.mi.plugin;

import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.plugins.CompilerLifecycleEventContext;
import io.ballerina.projects.plugins.CompilerLifecycleTask;
import io.ballerina.stdlib.mi.plugin.model.Component;
import io.ballerina.stdlib.mi.plugin.model.Connector;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BalCompilerLifeCycleTask implements CompilerLifecycleTask<CompilerLifecycleEventContext> {

    private static void generateXmlFiles(Path connectorFolderPath, Connector connector) {
        File connectorFolder = new File(connectorFolderPath.toUri());
        if (!connectorFolder.exists()) {
            connectorFolder.mkdir();
        }

        connector.generateInstanceXml(connectorFolder);

        for (Component component : connector.getComponents()) {
            component.generateInstanceXml(connectorFolder);
            component.generateTemplateXml(connectorFolder);
        }
    }

    @Override
    public void perform(CompilerLifecycleEventContext context) {


        String sourcePathStr = context.getGeneratedArtifactPath().get().toString();
        Path sourcePath = Paths.get(sourcePathStr);

        PackageDescriptor descriptor = context.currentPackage().manifest().descriptor();
        Path destinationPath = context.currentPackage().project().sourceRoot().resolve(Connector.TYPE_NAME);

        Connector connector = Connector.getConnector();
        connector.setName(descriptor.name().value());
        connector.setVersion(descriptor.version().value().toString());

        generateXmlFiles(destinationPath, connector);

        try {
            URI jarPath = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            Utils.copyResources(getClass().getClassLoader(), destinationPath, jarPath);
            Files.copy(sourcePath, destinationPath.resolve("lib").resolve(sourcePath.getFileName()));
            Utils.zipFolder(destinationPath, connector.getZipFileName());
            Utils.deleteDirectory(destinationPath);

        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
