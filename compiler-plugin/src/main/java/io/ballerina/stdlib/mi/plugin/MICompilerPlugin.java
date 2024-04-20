package io.ballerina.stdlib.mi.plugin;

import io.ballerina.projects.plugins.CompilerPlugin;
import io.ballerina.projects.plugins.CompilerPluginContext;
import io.ballerina.stdlib.mi.plugin.model.Connector;

public class MICompilerPlugin extends CompilerPlugin {

    @Override
    public void init(CompilerPluginContext compilerPluginContext) {
        compilerPluginContext.addCodeAnalyzer(new BalMediatorCodeAnalyzer());
        compilerPluginContext.addCompilerLifecycleListener(new BalLifecycleListner());
    }
}

