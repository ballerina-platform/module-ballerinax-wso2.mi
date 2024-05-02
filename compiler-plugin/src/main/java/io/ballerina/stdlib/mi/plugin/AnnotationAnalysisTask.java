package io.ballerina.stdlib.mi.plugin;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.syntax.tree.*;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.stdlib.mi.plugin.model.Component;
import io.ballerina.stdlib.mi.plugin.model.Connector;
import io.ballerina.stdlib.mi.plugin.model.Param;

public class AnnotationAnalysisTask implements AnalysisTask<SyntaxNodeAnalysisContext> {

    @Override
    public void perform(SyntaxNodeAnalysisContext context) {

        Connector connector = Connector.getConnector();
        SemanticModel semanticModel = context.compilation().getSemanticModel(context.moduleId());

        AnnotationNode node = (AnnotationNode) context.node();
        AnnotationSymbol annotationSymbol = (AnnotationSymbol) semanticModel.symbol(node).get();
        if (annotationSymbol.getName().isEmpty()) return;

        if (!annotationSymbol.getName().get().equals(io.ballerina.stdlib.mi.plugin.Annotations.ANNOTATION_QUALIFIER))
            return;


        if (node.annotValue().isEmpty()) return;
        SeparatedNodeList<MappingFieldNode> fields = node.annotValue().get().fields();

        for (MappingFieldNode field : fields) {
            if (field instanceof SpecificFieldNode) {
                String fieldName = ((SpecificFieldNode) field).fieldName().toString().trim();
                if (((SpecificFieldNode) field).valueExpr().isEmpty()) {
                    continue;
                }
                String fieldValue = ((SpecificFieldNode) field).valueExpr().get().toString().trim();
                fieldValue = fieldValue.substring(1, fieldValue.length() - 1);
                if (fieldName.equals(io.ballerina.stdlib.mi.plugin.Annotations.ICON_PATH)) {
                    connector.setIconPath(fieldValue);
                }
            }
        }

        FunctionDefinitionNode functionNode = (FunctionDefinitionNode) node.parent().parent();

        if (semanticModel.symbol(node).isEmpty()) return;

        PackageDescriptor descriptor = context.currentPackage().manifest().descriptor();
        String orgName = descriptor.org().value();
        String moduleName = descriptor.name().value();
        String version = String.valueOf(descriptor.version().value().major());

        if (semanticModel.symbol(functionNode).isEmpty()) return;

        FunctionSymbol functionSymbol = (FunctionSymbol) semanticModel.symbol(functionNode).get();
        if (functionSymbol.getName().isEmpty()) return;
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
        Param orgParam = new Param("OrgName", orgName);
        Param moduleParam = new Param("ModuleName", moduleName);
        Param versionParam = new Param("Version", version);

        component.setParam(functionName);
        component.setParam(sizeParam);
        component.setParam(orgParam);
        component.setParam(moduleParam);
        component.setParam(versionParam);

        connector.setComponent(component);
    }
}
