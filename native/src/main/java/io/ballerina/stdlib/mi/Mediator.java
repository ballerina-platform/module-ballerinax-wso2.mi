package io.ballerina.stdlib.mi;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BXml;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.module.core.SimpleMediator;
import org.wso2.carbon.module.core.SimpleMessageContext;

import java.util.HashMap;

public class Mediator extends SimpleMediator {
    private String firstArgument = "arg1";
    private String secondArgument = "arg2";
    private String functionName = "foo";

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

        Module module = new Module(Constants.ORG_NAME, Constants.MODULE_NAME, "0");
        Runtime rt = Runtime.from(module);
        Object[] args = new Object[2];

        args[0] = StringUtils.fromString(firstArgument);
        args[1] = StringUtils.fromString(secondArgument);
        rt.init();

        rt.start();
        rt.invokeMethodAsync(functionName, returnCallback, args);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public BXml getBXmlParameter(SimpleMessageContext context, String parameterName) {
        return OMElementConverter.toBXml((OMElement) context.getProperty((String) context.getProperty(parameterName)));
    }
}
