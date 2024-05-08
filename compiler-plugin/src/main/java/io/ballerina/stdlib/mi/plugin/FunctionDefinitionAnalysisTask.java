package io.ballerina.stdlib.mi.plugin;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.stdlib.mi.plugin.model.Component;
import io.ballerina.stdlib.mi.plugin.model.Connector;
import io.ballerina.stdlib.mi.plugin.model.Param;

public class FunctionDefinitionAnalysisTask implements AnalysisTask<SyntaxNodeAnalysisContext> {

    @Override
    public void perform(SyntaxNodeAnalysisContext context) {
        PackageDescriptor descriptor = context.currentPackage().manifest().descriptor();
        String orgName = descriptor.org().value();
        String moduleName = descriptor.name().value();
        String version = String.valueOf(descriptor.version().value().major());

        FunctionDefinitionNode node = (FunctionDefinitionNode) context.node();

        SemanticModel semanticModel = context.compilation().getSemanticModel(context.moduleId());
        if (semanticModel.symbol(node).isEmpty()) return;

        FunctionSymbol functionSymbol = (FunctionSymbol) semanticModel.symbol(node).get();
        Component component = new Component(functionSymbol.getName().get());
        int noOfParams = 0;
        if (functionSymbol.typeDescriptor().params().isPresent()) {
            noOfParams = functionSymbol.typeDescriptor().params().get().size();
        }

        for (int i = 0; i < noOfParams; i++) {
            if (functionSymbol.typeDescriptor().params().get().get(i).getName().isEmpty()) continue;
            Param param = new Param(Integer.toString(i), functionSymbol.typeDescriptor().params().get().get(i).getName().get());
            component.setParam(param);
        }

        Param functionName = new Param("FunctionName", functionSymbol.getName().get());
        Param sizeParam = new Param("Size", Integer.toString(noOfParams));
        component.setParam(functionName);
        component.setParam(sizeParam);

        Connector connector = Connector.getConnector();
        connector.setOrgName(orgName);
        connector.setModuleName(moduleName);
        connector.setModuleVersion(version);
        connector.setComponent(component);
    }
}
