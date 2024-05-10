package io.ballerina.stdlib.mi;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BXml;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.module.core.SimpleMediator;
import org.wso2.carbon.module.core.SimpleMessageContext;

public class Mediator extends SimpleMediator {

    private static final Log log = LogFactory.getLog(Mediator.class);
    private static Runtime rt = null;

    public Mediator() {
        ModuleInfo moduleInfo = new ModuleInfo();
        Module module = new Module(moduleInfo.getOrgName(), moduleInfo.getModuleName(), moduleInfo.getModuleVersion());
        rt = Runtime.from(module);

        rt.init();
        rt.start();
    }

    public void mediate(SimpleMessageContext context) {
        Callback returnCallback = new Callback() {
            public void notifySuccess(Object result) {
                log.info("notifySuccess");
                context.setProperty(Constants.RESULT, BXmlConverter.toOMElement((BXml) result));
            }

            public void notifyFailure(BError result) {
                log.error("notifyFailure");
                context.setProperty(Constants.RESULT, result.toString());
            }
        };

        rt.invokeMethodAsync(context.getProperty(Constants.FUNCTION_NAME).toString(), returnCallback,
                getParameters(context));
    }

    private BXml getBXmlParameter(SimpleMessageContext context, String parameterName) {
        return OMElementConverter.toBXml((OMElement) context.getProperty((String) context.getProperty(parameterName)));
    }

    private Object[] getParameters(SimpleMessageContext context) {

        Object[] args = new Object[Integer.parseInt(context.getProperty(Constants.SIZE).toString())];
        for (int i = 0; i < args.length; i++) {
            args[i] = getBXmlParameter(context, "param" + i);
        }
        return args;
    }
}
