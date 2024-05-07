package io.ballerina.stdlib.mi.plugin;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.projects.BallerinaToml;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.stdlib.mi.plugin.model.Component;
import io.ballerina.stdlib.mi.plugin.model.Connector;
import io.ballerina.stdlib.mi.plugin.model.Param;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlStringValueNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;

import java.util.List;
import java.util.Optional;

public class AnnotationAnalysisTask implements AnalysisTask<SyntaxNodeAnalysisContext> {

    private static final String FUNCTION_NAME = "FunctionName";
    private static final String SIZE = "Size";
    private static final String ORG_NAME = "OrgName";
    private static final String MODULE_NAME = "ModuleName";
    private static final String VERSION = "Version";

    private static void setFunctionDescriptions(PackageDescriptor descriptor, String functionName, Component component) {
        String orgName = descriptor.org().value();
        String moduleName = descriptor.name().value();
        String version = String.valueOf(descriptor.version().value().major());

        Param functionNameParam = new Param(FUNCTION_NAME, functionName);
        Param orgParam = new Param(ORG_NAME, orgName);
        Param moduleParam = new Param(MODULE_NAME, moduleName);
        Param versionParam = new Param(VERSION, version);

        component.setParam(functionNameParam);
        component.setParam(orgParam);
        component.setParam(moduleParam);
        component.setParam(versionParam);
    }

    private static void setArguments(FunctionSymbol functionSymbol, Component component) {
        int noOfParams = 0;
        Optional<List<ParameterSymbol>> params = functionSymbol.typeDescriptor().params();
        if (params.isPresent()) {
            noOfParams = params.get().size();
        }

        for (int i = 0; i < noOfParams; i++) {
            ParameterSymbol parameterSymbol = params.get().get(i);
            if (parameterSymbol.getName().isEmpty()) continue;
            Param param = new Param(Integer.toString(i), parameterSymbol.getName().get());
            component.setParam(param);
        }
        Param sizeParam = new Param(SIZE, Integer.toString(noOfParams));
        component.setParam(sizeParam);
    }

    private static void setIcon(BallerinaToml ballerinaToml, Connector connector) {
        if (connector.getIconPath() != null) return;
        TomlDocument tomlDocument = ballerinaToml.tomlDocument();
        TomlTableNode path = (TomlTableNode) tomlDocument.toml().rootNode().entries().get(Connector.TOML_ICON_NODE);
        if (path == null) return;

        TomlKeyValueNode iconPath = (TomlKeyValueNode) path.entries().get(Connector.TOML_ICON_KEY);
        if (iconPath == null) return;

        TomlStringValueNode valuePath = (TomlStringValueNode) iconPath.value();
        connector.setIconPath(valuePath.getValue());
    }

    private static FunctionSymbol getFunctionSymbol(SyntaxNodeAnalysisContext context, SemanticModel semanticModel) {
        if (!(context.node() instanceof AnnotationNode annotationNode)) return null;
        Optional<Symbol> symbol = semanticModel.symbol(annotationNode);
        if (symbol.isEmpty()) return null;
        if (!(symbol.get() instanceof AnnotationSymbol annotationSymbol)) return null;
        Optional<String> annotationName = annotationSymbol.getName();
        if (annotationName.isEmpty()) return null;
        if (!annotationName.get().equals(Component.ANNOTATION_QUALIFIER)) return null;


        if (!(annotationNode.parent().parent() instanceof FunctionDefinitionNode functionNode)) return null;
        Optional<Symbol> funcSymbol = semanticModel.symbol(functionNode);
        if (funcSymbol.isEmpty()) return null;
        if (!(funcSymbol.get() instanceof FunctionSymbol functionSymbol)) return null;
        return functionSymbol;
    }

    @Override
    public void perform(SyntaxNodeAnalysisContext context) {

        SemanticModel semanticModel = context.compilation().getSemanticModel(context.moduleId());

        FunctionSymbol functionSymbol = getFunctionSymbol(context, semanticModel);
        if (functionSymbol == null) return;

        Optional<BallerinaToml> ballerinaToml = context.currentPackage().ballerinaToml();
        if (ballerinaToml.isEmpty()) return;

        Connector connector = Connector.getConnector();
        setIcon(ballerinaToml.get(), connector);

        Optional<String> functionName = functionSymbol.getName();
        if (functionName.isEmpty()) return;
        Component component = new Component(functionName.get());
        setArguments(functionSymbol, component);

        PackageDescriptor descriptor = context.currentPackage().manifest().descriptor();
        setFunctionDescriptions(descriptor, functionName.get(), component);

        connector.setComponent(component);
    }
}
