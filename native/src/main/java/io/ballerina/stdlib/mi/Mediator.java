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
    private String firstArgument = "";
    private String secondArgument = "";
    private String thirdArgument = "";
    private String fourthArgument = "";
    private String fifthArgument = "";
    private String sixthArgument = "";
    private String functionName = "transform_1";

    public void mediate(SimpleMessageContext context) {
        HashMap<String, Object> properties = new HashMap<String, Object>() {
            {
                this.put("payload", Z.this.getPayload(context));
                this.put("firstArgument", Z.this.getBXmlParameter(context, "param0"));
                this.put("secondArgument", Z.this.getBXmlParameter(context, "param1"));
                this.put("thirdArgument", Z.this.getThirdArgument());
                this.put("fourthArgument", Z.this.getFourthArgument());
                this.put("fifthArgument", Z.this.getFifthArgument());
                this.put("sixthArgument", Z.this.getSixthArgument());
                this.put("functionName", context.getProperty("paramFunctionName"));
                this.put("xmlProperty", context.getProperty("xmlProperty"));
            }
        };

        BalRuntime.balStart(scheduler, properties, Constants.ORG_NAME, Constants.MODULE_NAME, Constants.VERSION);
        context.setProperty("result", BXmlConverter.toOMElement((BXml) properties.get("result")) );

    }

    public BXml getBXmlParameter(SimpleMessageContext context, String parameterName) {
        return OMElementConverter.toBXml((OMElement) context.getProperty((String) context.getProperty(parameterName)));
    }

    public void setFirstArgument(String value) {
        this.firstArgument = value;
    }

    public String getFirstArgument() {
        return this.firstArgument;
    }

    public void setSecondArgument(String value) {
        this.secondArgument = value;
    }

    public String getSecondArgument() {
        return this.secondArgument;
    }

    public void setThirdArgument(String value) {
        this.thirdArgument = value;
    }

    public String getThirdArgument() {
        return this.thirdArgument;
    }

    public String getFunctionName() {
        return this.functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public BXml getPayload(SimpleMessageContext context) {
        return OMElementConverter.toBXml(context.getRootXmlElement());
    }

    public String getFourthArgument() {
        return this.fourthArgument;
    }

    public void setFourthArgument(String fourthArgument) {
        this.fourthArgument = fourthArgument;
    }

    public String getFifthArgument() {
        return this.fifthArgument;
    }

    public void setFifthArgument(String fifthArgument) {
        this.fifthArgument = fifthArgument;
    }

    public String getSixthArgument() {
        return this.sixthArgument;
    }

    public void setSixthArgument(String sixthArgument) {
        this.sixthArgument = sixthArgument;
    }

}
