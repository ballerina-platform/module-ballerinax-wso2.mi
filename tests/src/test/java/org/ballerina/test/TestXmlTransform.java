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
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.stream.XMLStreamException;

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
        context.setProperty("xmlA", AXIOMUtil.stringToOM("<name>John</name>"));
        context.setProperty("param1", "xmlB");
        context.setProperty("xmlB", AXIOMUtil.stringToOM("<age>30</age>"));
        context.setProperty("param2", "xmlC");
        context.setProperty("xmlC", AXIOMUtil.stringToOM("<city>Colombo</city>"));
        mediator.mediate(context);
        Assert.assertEquals(context.getProperty("result"), "<name>John</name><apr30>8:99999</apr30>");
    }

    @Test
    public void testXmlTransform2() throws XMLStreamException {
        String project = "project3";
        ModuleInfo moduleInfo = new ModuleInfo("testOrg", project, "1");
        Mediator mediator = new Mediator(moduleInfo);

        TestMessageContext context = new TestMessageContext();
        context.setProperty("paramFunctionName", "test");
        context.setProperty("paramSize", 3);
        context.setProperty("param0", "xmlA");
        context.setProperty("xmlA", AXIOMUtil.stringToOM("<name>John</name>"));
        context.setProperty("param1", "xmlB");
        context.setProperty("xmlB", AXIOMUtil.stringToOM("<age>31</age>"));
        context.setProperty("param2", "xmlC");
        context.setProperty("xmlC", AXIOMUtil.stringToOM("<city>Colombo</city>"));
        mediator.mediate(context);
        Assert.assertEquals(context.getProperty("result"), "<name>John</name><age>31</age><city>Colombo</city>");
    }
}
