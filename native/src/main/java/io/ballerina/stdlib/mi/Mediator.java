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

public class Mediator extends SimpleMediator {
    private String firstArgument = "arg5";
    private String secondArgument = "arg6";
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

        Module module = new Module(Constants.ORG_NAME, Constants.MODULE_NAME, "1");
        Runtime rt = Runtime.from(module);
        Object[] args = new Object[2];

        args[0] = StringUtils.fromString(firstArgument);
        args[1] = StringUtils.fromString(secondArgument);
        rt.init();

        rt.start();
        rt.invokeMethodAsync(functionName, returnCallback, args);
    }

    public BXml getBXmlParameter(SimpleMessageContext context, String parameterName) {
        return OMElementConverter.toBXml((OMElement) context.getProperty((String) context.getProperty(parameterName)));
    }
}
