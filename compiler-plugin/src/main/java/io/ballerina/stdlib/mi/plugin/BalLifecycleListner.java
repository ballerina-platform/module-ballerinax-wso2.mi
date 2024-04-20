package io.ballerina.stdlib.mi.plugin;

import io.ballerina.projects.plugins.CompilerLifecycleContext;
import io.ballerina.projects.plugins.CompilerLifecycleListener;
import io.ballerina.stdlib.mi.plugin.model.Connector;

public class BalLifecycleListner extends CompilerLifecycleListener {

    @Override
    public void init(CompilerLifecycleContext compilerLifecycleContext) {
        compilerLifecycleContext.addCodeGenerationCompletedTask(new BalCompilerLifeCycleTask());
    }
}
