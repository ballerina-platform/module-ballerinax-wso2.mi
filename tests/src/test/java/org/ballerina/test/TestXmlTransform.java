/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerina.test;

import io.ballerina.stdlib.mi.Mediator;
import io.ballerina.stdlib.mi.ModuleInfo;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.synapse.mediators.template.TemplateContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class TestXmlTransform {

    @Test
    public void testXmlTransform1() throws XMLStreamException {
        String project = "project2";
        ModuleInfo moduleInfo = new ModuleInfo("testOrg", project, "1");
        Mediator mediator = new Mediator(moduleInfo);

        TestMessageContext context = new TestMessageContext();
        context.setProperty("paramFunctionName", "test");
        context.setProperty("paramSize", 3);
        context.setProperty("param0", "xmlA");
        context.setProperty("param1", "xmlB");
        context.setProperty("param2", "xmlC");

        Stack<TemplateContext> stack = new Stack<>();
        TemplateContext templateContext = new TemplateContext("testTemplateFunc", new ArrayList<>());
        stack.push(templateContext);

        HashMap<Object, Object> map = new HashMap<>();
        map.put("xmlA", AXIOMUtil.stringToOM("<name>John</name>"));
        map.put("xmlB", AXIOMUtil.stringToOM("<apr30>8:99999</apr30>"));
        map.put("xmlC", AXIOMUtil.stringToOM("<city>Colombo</city>"));
        map.put("Result", "res");
        templateContext.setMappedValues(map);

        context.setProperty("_SYNAPSE_FUNCTION_STACK", stack);
        mediator.mediate(context);
        Assert.assertEquals(context.getProperty("res").toString(),
                "<may21><time>9:31</time><name>John</name><apr30>8:99999</apr30><city>Colombo</city></may21>");
    }

    @Test
    public void testXmlTransform2() throws XMLStreamException {
        String project = "project3";
        ModuleInfo moduleInfo = new ModuleInfo("testOrg", project, "1");
        Mediator mediator = new Mediator(moduleInfo);

        TestMessageContext context = new TestMessageContext();
        context.setProperty("paramFunctionName", "test");
        context.setProperty("paramSize", 4);
        context.setProperty("param0", "xmlA");
        context.setProperty("param1", "xmlB");
        context.setProperty("param2", "xmlC");
        context.setProperty("param3", "xmlD");

        Stack<TemplateContext> stack = new Stack<>();
        TemplateContext templateContext = new TemplateContext("testTemplateFunc", new ArrayList<>());
        stack.push(templateContext);

        HashMap<Object, Object> map = new HashMap<>();
        map.put("xmlA", AXIOMUtil.stringToOM("<name>John</name>"));
        map.put("xmlB", AXIOMUtil.stringToOM("<apr30>8:99999</apr30>"));
        map.put("xmlC", AXIOMUtil.stringToOM("<city>Colombo</city>"));
        map.put("xmlD", AXIOMUtil.stringToOM("<country>SriLanka</country>"));
        map.put("Result", "r");
        templateContext.setMappedValues(map);

        context.setProperty("_SYNAPSE_FUNCTION_STACK", stack);
        mediator.mediate(context);
        Assert.assertEquals(context.getProperty("r").toString(),
                "<may22><name>John</name><apr30>8:99999</apr30><city>Colombo</city>" +
                        "<country>SriLanka</country></may22>");
    }
}
