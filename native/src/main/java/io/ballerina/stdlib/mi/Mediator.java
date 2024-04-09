package io.ballerina.stdlib.mi;

import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.BalRuntime;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.module.core.SimpleMediator;
import org.wso2.carbon.module.core.SimpleMessageContext;

import java.util.HashMap;

public class Mediator extends SimpleMediator {
    static Scheduler scheduler = new Scheduler(false);

    public void mediate(SimpleMessageContext context) {
        HashMap<String, Object> properties = new HashMap<String, Object>() {
            {
                this.put("firstArgument", getBXmlParameter(context, "param0"));
                this.put("secondArgument", getBXmlParameter(context, "param1"));
                this.put("functionName", context.getProperty("paramFunctionName"));
            }
        };

        BalRuntime.balStart(scheduler, properties, Constants.ORG_NAME, Constants.MODULE_NAME, Constants.VERSION);
        context.setProperty("result", BXmlConverter.toOMElement((BXml) properties.get("result")) );

    }

    public BXml getBXmlParameter(SimpleMessageContext context, String parameterName) {
        return OMElementConverter.toBXml((OMElement) context.getProperty((String) context.getProperty(parameterName)));
    }
}
