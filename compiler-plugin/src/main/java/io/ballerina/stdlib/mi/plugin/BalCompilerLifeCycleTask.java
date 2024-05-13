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
import java.util.Optional;

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

    private static void generateJsonFiles(Path connectorFolderPath, Connector connector) {
        File connectorFolder = new File(connectorFolderPath.toUri());
        if (!connectorFolder.exists()) {
            connectorFolder.mkdir();
        }

        for (Component component : connector.getComponents()) {
            component.generateUIJson(connectorFolder);
        }
    }

    @Override
    public void perform(CompilerLifecycleEventContext context) {
        Optional<Path> generatedArtifactPath = context.getGeneratedArtifactPath();
        if (generatedArtifactPath.isEmpty()) {
            return;
        }

        Path sourcePath = generatedArtifactPath.get();
        PackageDescriptor descriptor = context.currentPackage().manifest().descriptor();

        Connector connector = Connector.getConnector();
        connector.setName(descriptor.name().value());
        connector.setVersion(descriptor.version().value().toString());


        try {
            Path destinationPath = Files.createTempDirectory(Connector.TEMP_PATH);
            generateXmlFiles(destinationPath, connector);
            generateJsonFiles(destinationPath, connector);
//            Utils.generateJson("template","func7", connector);
            URI jarPath = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            Utils.copyResources(getClass().getClassLoader(), destinationPath, jarPath, connector.getOrgName(),
                    connector.getModuleName(), connector.getModuleVersion());
            Files.copy(sourcePath, destinationPath.resolve(Connector.LIB_PATH).resolve(sourcePath.getFileName()));
            Utils.zipFolder(destinationPath, connector.getZipFileName());
            Utils.deleteDirectory(destinationPath);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
