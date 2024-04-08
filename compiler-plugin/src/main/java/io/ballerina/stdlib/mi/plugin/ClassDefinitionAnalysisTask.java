package io.ballerina.stdlib.mi.plugin;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import io.ballerina.stdlib.mi.plugin.model.Component;
import io.ballerina.stdlib.mi.plugin.model.Connector;
import io.ballerina.stdlib.mi.plugin.model.Param;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Map;


public class ClassDefinitionAnalysisTask implements AnalysisTask<SyntaxNodeAnalysisContext> {
    @Override
    public void perform(SyntaxNodeAnalysisContext context) {
        PackageDescriptor descriptor = context.currentPackage().manifest().descriptor();
        String orgName = descriptor.org().value();
        String moduleName = descriptor.name().value();
        String version = String.valueOf(descriptor.version().value());

        ClassDefinitionNode node = (ClassDefinitionNode) context.node();
        Connector connector = new Connector();

        SemanticModel semanticModel = context.compilation().getSemanticModel(context.moduleId());
        if (semanticModel.symbol(node).isEmpty()) return;
        Map<String, MethodSymbol> methods = ((ClassSymbol) semanticModel.symbol(node).get()).methods();


        for (Map.Entry<String, MethodSymbol> entry : methods.entrySet()) {
            MethodSymbol value = entry.getValue();
            if (value.getName().isEmpty()) continue;

            Component component = new Component(value.getName().get());

            int noOfParams = 0;
            if (value.typeDescriptor().params().isPresent()) {
                noOfParams = value.typeDescriptor().params().get().size();
            }

            for (int i = 0; i < noOfParams; i++) {
                if (value.typeDescriptor().params().get().get(i).getName().isEmpty()) continue;
                Param param = new Param(Integer.toString(i),value.typeDescriptor().params().get().get(i).getName().get());
                component.setParam(param);
            }

            Param functionName = new Param("FunctionName",value.getName().get());
            Param sizeParam = new Param("Size",Integer.toString(noOfParams));
            Param orgParam = new Param("OrgName",orgName);
            Param moduleParam = new Param("ModuleName",moduleName);
            Param versionParam = new Param("Version",version);

            component.setParam(functionName);
            component.setParam(sizeParam);
            component.setParam(orgParam);
            component.setParam(moduleParam);
            component.setParam(versionParam);

            connector.setComponent(component);
            component.generateInstanceXml();
            component.generateTemplateXml();
        }
        connector.generateInstanceXml();
        try {
            Utils.zipF(getClass().getClassLoader(), "connector", "connector");
            Utils.zipFolder("connector", "Z-connector-1.0-SNAPSHOT.zip");
            Utils.deleteDirectory("connector");
        } catch (IOException | URISyntaxException e ) {
            throw new RuntimeException(e);
        }
    }
}
