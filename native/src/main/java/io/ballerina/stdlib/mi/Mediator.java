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
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BXml;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.module.core.SimpleMediator;
import org.wso2.carbon.module.core.SimpleMessageContext;

public class Mediator extends SimpleMediator {
    private static Runtime rt = null;

    public Mediator() {
        ModuleInfo moduleInfo = new ModuleInfo();
        Module module = new Module(moduleInfo.getOrgName(), moduleInfo.getModuleName(), moduleInfo.getModuleVersion());
        rt = Runtime.from(module);

        rt.init();
        rt.start();
    }

    private static String getResultProperty(SimpleMessageContext context) {
        String result = context.lookupTemplateParameter(Constants.RESULT).toString();
        if (result != null) {
            return result;
        }
        return Constants.RESULT;
    }

    public void mediate(SimpleMessageContext context) {
        Callback returnCallback = new Callback() {
            public void notifySuccess(Object result) {
                log.info("Notify Success");
                context.setProperty(getResultProperty(context), BXmlConverter.toOMElement((BXml) result));
            }

            public void notifyFailure(BError result) {
                log.info("Notify Failure");
                context.setProperty(Constants.RESULT, result.toString());
            }
        };

        rt.invokeMethodAsync(context.getProperty(Constants.FUNCTION_NAME).toString(), returnCallback,
                getParameters(context));
    }

    private BXml getBXmlParameter(SimpleMessageContext context, String parameterName) {
        OMElement omElement = getOMElement(context, parameterName);
        if (omElement == null) {
            return null;
        }
        return OMElementConverter.toBXml(omElement);
    }

    private Object[] getParameters(SimpleMessageContext context) {
        Object[] args = new Object[Integer.parseInt(context.getProperty(Constants.SIZE).toString())];
        for (int i = 0; i < args.length; i++) {
            args[i] = getBXmlParameter(context, "param" + i);
        }
        return args;
    }

    private OMElement getOMElement(SimpleMessageContext ctx, String value) {
        String param = ctx.getProperty(value).toString();
        if (ctx.lookupTemplateParameter(param) != null) {
            return (OMElement) ctx.lookupTemplateParameter(param);
        }
        log.error("Error in getting the OMElement");
        return null;
    }


}
