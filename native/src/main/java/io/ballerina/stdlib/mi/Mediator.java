/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.stdlib.mi;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.JsonUtils;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.module.core.SimpleMediator;
import org.wso2.carbon.module.core.SimpleMessageContext;

import java.util.Objects;

import static io.ballerina.stdlib.mi.Constants.BOOLEAN;
import static io.ballerina.stdlib.mi.Constants.DECIMAL;
import static io.ballerina.stdlib.mi.Constants.FLOAT;
import static io.ballerina.stdlib.mi.Constants.INT;
import static io.ballerina.stdlib.mi.Constants.JSON;
import static io.ballerina.stdlib.mi.Constants.NIL;
import static io.ballerina.stdlib.mi.Constants.STRING;
import static io.ballerina.stdlib.mi.Constants.XML;

public class Mediator extends SimpleMediator {
    private static volatile Runtime rt = null;

    public Mediator() {
        if (rt == null) {
            synchronized (Mediator.class) {
                if (rt == null) {
                    ModuleInfo moduleInfo = new ModuleInfo();
                    init(moduleInfo);
                }
            }
        }
    }

    // This constructor is added to test the mediator
    public Mediator(ModuleInfo moduleInfo) {
        init(moduleInfo);
    }

    private static String getResultProperty(SimpleMessageContext context) {
        return context.lookupTemplateParameter(Constants.RESULT).toString();
    }

    public void mediate(SimpleMessageContext context) {
        String balFunctionReturnType = context.getProperty(Constants.RETURN_TYPE).toString();
        Callback returnCallback = new Callback() {
            public void notifySuccess(Object result) {
                log.info("Notify Success");
                Object res = result;
                if (Objects.equals(balFunctionReturnType, XML)) {
                    res = BXmlConverter.toOMElement((BXml) result);
                } else if (Objects.equals(balFunctionReturnType, DECIMAL)) {
                    res = ((BDecimal) result).value().toString();
                } else if (Objects.equals(balFunctionReturnType, STRING)) {
                    res = ((BString) res).getValue();
                } else if (result instanceof BMap) {
                    res = result.toString();
                }
                context.setProperty(getResultProperty(context), res);
            }

            public void notifyFailure(BError result) {
                log.info("Notify Failure");
                context.setProperty(Constants.RESULT, result.toString());
            }
        };

        rt.invokeMethodAsync(context.getProperty(Constants.FUNCTION_NAME).toString(), returnCallback,
                getParameters(context));
    }

    private Object[] getParameters(SimpleMessageContext context) {
        Object[] args = new Object[Integer.parseInt(context.getProperty(Constants.SIZE).toString())];
        for (int i = 0; i < args.length; i++) {
            args[i] = getParameter(context, "param" + i, "paramType" + i);
        }
        return args;
    }

    private Object getParameter(SimpleMessageContext context, String value, String type) {
        String paramName = context.getProperty(value).toString();
        Object param = context.lookupTemplateParameter(paramName);
        if (param == null) {
            log.error("Error in getting the ballerina function parameter");
            return null;
        }
        String paramType = context.getProperty(type).toString();
        return switch (paramType) {
            case NIL -> null;
            case BOOLEAN -> Boolean.parseBoolean((String) param);
            case INT -> Long.parseLong((String) param);
            case STRING -> StringUtils.fromString((String) param);
            case FLOAT -> Double.parseDouble((String) param);
            case DECIMAL -> ValueCreator.createDecimalValue((String) param);
            case JSON -> getBMapParameter(param);
            default -> getBXmlParameter(context, value);
        };
    }

    private BXml getBXmlParameter(SimpleMessageContext context, String parameterName) {
        OMElement omElement = getOMElement(context, parameterName);
        if (omElement == null) {
            return null;
        }
        return OMElementConverter.toBXml(omElement);
    }

    private OMElement getOMElement(SimpleMessageContext ctx, String value) {
        String param = ctx.getProperty(value).toString();
        if (ctx.lookupTemplateParameter(param) != null) {
            return (OMElement) ctx.lookupTemplateParameter(param);
        }
        log.error("Error in getting the OMElement");
        return null;
    }

    private BMap getBMapParameter(Object param) {
        if (param instanceof String) {
            return (BMap) JsonUtils.parse((String) param);
        } else {
            return (BMap) JsonUtils.parse(param.toString());
        }
    }

    private void init(ModuleInfo moduleInfo) {
        Module module = new Module(moduleInfo.getOrgName(), moduleInfo.getModuleName(), moduleInfo.getModuleVersion());
        rt = Runtime.from(module);
        rt.init();
        rt.start();
    }
}
