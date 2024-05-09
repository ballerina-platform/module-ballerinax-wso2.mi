package io.ballerina.stdlib.mi.plugin;

import io.ballerina.projects.plugins.CompilerLifecycleContext;
import io.ballerina.projects.plugins.CompilerLifecycleListener;

public class BalLifecycleListener extends CompilerLifecycleListener {

    @Override
    public void init(CompilerLifecycleContext compilerLifecycleContext) {
        compilerLifecycleContext.addCodeGenerationCompletedTask(new BalCompilerLifeCycleTask());
    }
}
