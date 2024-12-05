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
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.template.TemplateContext;

import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;

import static io.ballerina.stdlib.mi.Constants.BOOLEAN;
import static io.ballerina.stdlib.mi.Constants.DECIMAL;
import static io.ballerina.stdlib.mi.Constants.FLOAT;
import static io.ballerina.stdlib.mi.Constants.INT;
import static io.ballerina.stdlib.mi.Constants.JSON;
import static io.ballerina.stdlib.mi.Constants.STRING;
import static io.ballerina.stdlib.mi.Constants.XML;

public class Mediator extends AbstractMediator {
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

    private static String getResultProperty(MessageContext context) {
        return lookupTemplateParameter(context, Constants.RESULT).toString();
    }

    public boolean mediate(MessageContext context) {
        String balFunctionReturnType = context.getProperty(Constants.RETURN_TYPE).toString();
        final CountDownLatch latch = new CountDownLatch(1);
        Callback returnCallback = new Callback() {
            public void notifySuccess(Object result) {
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
                latch.countDown();
            }

            public void notifyFailure(BError result) {
                handleException(result.getMessage(), context);
                latch.countDown();
            }
        };

        Object[] args = new Object[Integer.parseInt(context.getProperty(Constants.SIZE).toString())];
        if (!setParameters(args, context)) {
            return false;
        }
        rt.invokeMethodAsync(context.getProperty(Constants.FUNCTION_NAME).toString(), returnCallback, args);
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        return true;
    }

    private boolean setParameters(Object[] args, MessageContext context) {
        for (int i = 0; i < args.length; i++) {
            Object param = getParameter(context, "param" + i, "paramType" + i);
            if (param == null) {
                return false;
            }
            args[i] = param;
        }
        return true;
    }

    private Object getParameter(MessageContext context, String value, String type) {
        String paramName = context.getProperty(value).toString();
        Object param = lookupTemplateParameter(context, paramName);
        if (param == null) {
            log.error("Error in getting the ballerina function parameter: " + paramName);
            return null;
        }
        String paramType = context.getProperty(type).toString();
        return switch (paramType) {
            case BOOLEAN -> Boolean.parseBoolean(param.toString());
            case INT -> Long.parseLong(param.toString());
            case STRING -> StringUtils.fromString(param.toString());
            case FLOAT -> Double.parseDouble(param.toString());
            case DECIMAL -> ValueCreator.createDecimalValue(param.toString());
            case JSON -> getBMapParameter(param);
            case XML -> getBXmlParameter(context, value);
            default -> null;
        };
    }

    private BXml getBXmlParameter(MessageContext context, String parameterName) {
        OMElement omElement = getOMElement(context, parameterName);
        if (omElement == null) {
            return null;
        }
        return OMElementConverter.toBXml(omElement);
    }

    private OMElement getOMElement(MessageContext ctx, String value) {
        String param = ctx.getProperty(value).toString();
        if (lookupTemplateParameter(ctx, param) != null) {
            return (OMElement) lookupTemplateParameter(ctx, param);
        }
        log.error("Error in getting the OMElement");
        return null;
    }

    public static Object lookupTemplateParameter(MessageContext ctx, String paramName) {
        Stack funcStack = (Stack) ctx.getProperty(Constants.SYNAPSE_FUNCTION_STACK);
        TemplateContext currentFuncHolder = (TemplateContext) funcStack.peek();
        return currentFuncHolder.getParameterValue(paramName);
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
