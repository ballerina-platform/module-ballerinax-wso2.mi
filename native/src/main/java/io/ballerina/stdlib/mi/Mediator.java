package io.ballerina.stdlib.mi;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BXml;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.module.core.SimpleMediator;
import org.wso2.carbon.module.core.SimpleMessageContext;

public class Mediator extends SimpleMediator {

    //    private static final Module module = new Module(Constants.ORG_NAME, Constants.MODULE_NAME, "1");
    private static Runtime rt = null;
    private boolean isInitialized = false;

//    public Mediator() {
//        System.out.println("Initializing Ballerina Mediator ....................");
//        rt.init();
//        rt.start();
//    }c

    public void mediate(SimpleMessageContext context) {
        Callback returnCallback = new Callback() {
            public void notifySuccess(Object result) {
                System.out.println("notifySuccess");
                System.out.println(result);
                context.setProperty(Constants.RESULT, result);
            }

            public void notifyFailure(BError result) {
                System.out.println("notifyFailure");
                System.out.println(result);
                context.setProperty(Constants.RESULT, result);
            }
        };

        if (!isInitialized) {

            Module module = new Module(context.getProperty(Constants.ORG_NAME).toString(), context.getProperty(Constants.MODULE_NAME).toString(), context.getProperty(Constants.MAJOR_VERSION).toString());
            rt = Runtime.from(module);

            System.out.println("Initializing Ballerina Mediator ....................");
            rt.init();
            rt.start();

            isInitialized = true;
        }

        rt.invokeMethodAsync(context.getProperty(Constants.FUNCTION_NAME).toString(), returnCallback, getParameters(context));
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
