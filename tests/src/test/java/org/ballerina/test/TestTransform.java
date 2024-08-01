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
import java.util.List;
import java.util.Stack;

public class TestTransform {

    @Test
    public void testXmlTransform1() throws XMLStreamException {
        String project = "project2";
        ModuleInfo moduleInfo = new ModuleInfo("testOrg", project, "1");
        Mediator mediator = new Mediator(moduleInfo);

        List<Property> properties = new ArrayList<>() {{
            add(new Property("paramFunctionName", "test"));
            add(new Property("paramSize", 3));
            add(new Property("param0", "xmlA"));
            add(new Property("paramType0", "xml"));
            add(new Property("param1", "xmlB"));
            add(new Property("paramType1", "xml"));
            add(new Property("param2", "xmlC"));
            add(new Property("paramType2", "xml"));
            add(new Property("returnType", "xml"));
        }};
        TestMessageContext context = createMessageContext(properties);

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

        List<Property> properties = new ArrayList<>() {{
            add(new Property("paramFunctionName", "test"));
            add(new Property("paramSize", 4));
            add(new Property("param0", "xmlA"));
            add(new Property("paramType0", "xml"));
            add(new Property("param1", "xmlB"));
            add(new Property("paramType1", "xml"));
            add(new Property("param2", "xmlC"));
            add(new Property("paramType2", "xml"));
            add(new Property("param3", "xmlD"));
            add(new Property("paramType3", "xml"));
            add(new Property("returnType", "xml"));
        }};
        TestMessageContext context = createMessageContext(properties);

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

    @Test
    public void testIntTransform1() {
        String project = "project2";
        ModuleInfo moduleInfo = new ModuleInfo("testOrg", project, "1");
        Mediator mediator = new Mediator(moduleInfo);

        List<Property> properties = new ArrayList<>() {{
            add(new Property("paramFunctionName", "testInt"));
            add(new Property("paramSize", 2));
            add(new Property("param0", "i1"));
            add(new Property("paramType0", "int"));
            add(new Property("param1", "i2"));
            add(new Property("paramType1", "int"));
            add(new Property("returnType", "int"));
        }};
        TestMessageContext context = createMessageContext(properties);

        Stack<TemplateContext> stack = new Stack<>();
        TemplateContext templateContext = new TemplateContext("testTemplateFunc", new ArrayList<>());
        stack.push(templateContext);

        HashMap<Object, Object> map = new HashMap<>();
        map.put("i1", "23");
        map.put("i2", "17");
        map.put("Result", "r");
        templateContext.setMappedValues(map);

        context.setProperty("_SYNAPSE_FUNCTION_STACK", stack);
        mediator.mediate(context);
        Assert.assertEquals(context.getProperty("r").toString(), "40");
    }

    @Test
    public void testStringTransform1() {
        String project = "project2";
        ModuleInfo moduleInfo = new ModuleInfo("testOrg", project, "1");
        Mediator mediator = new Mediator(moduleInfo);

        List<Property> properties = new ArrayList<>() {{
            add(new Property("paramFunctionName", "testString"));
            add(new Property("paramSize", 2));
            add(new Property("param0", "s1"));
            add(new Property("paramType0", "string"));
            add(new Property("param1", "s2"));
            add(new Property("paramType1", "string"));
            add(new Property("returnType", "string"));
        }};
        TestMessageContext context = createMessageContext(properties);

        Stack<TemplateContext> stack = new Stack<>();
        TemplateContext templateContext = new TemplateContext("testTemplateFunc", new ArrayList<>());
        stack.push(templateContext);

        HashMap<Object, Object> map = new HashMap<>();
        map.put("s1", "Hello ");
        map.put("s2", "World");
        map.put("Result", "r");
        templateContext.setMappedValues(map);

        context.setProperty("_SYNAPSE_FUNCTION_STACK", stack);
        mediator.mediate(context);
        Assert.assertEquals(context.getProperty("r").toString(), "Hello World");
    }

    @Test
    public void testBooleanTransform1() {
        String project = "project2";
        ModuleInfo moduleInfo = new ModuleInfo("testOrg", project, "1");
        Mediator mediator = new Mediator(moduleInfo);

        List<Property> properties = new ArrayList<>() {{
            add(new Property("paramFunctionName", "testBoolean"));
            add(new Property("paramSize", 2));
            add(new Property("param0", "b1"));
            add(new Property("paramType0", "boolean"));
            add(new Property("param1", "b2"));
            add(new Property("paramType1", "boolean"));
            add(new Property("returnType", "boolean"));
        }};
        TestMessageContext context = createMessageContext(properties);

        Stack<TemplateContext> stack = new Stack<>();
        TemplateContext templateContext = new TemplateContext("testTemplateFunc", new ArrayList<>());
        stack.push(templateContext);

        HashMap<Object, Object> map = new HashMap<>();
        map.put("b1", "true");
        map.put("b2", "false");
        map.put("Result", "r");
        templateContext.setMappedValues(map);

        context.setProperty("_SYNAPSE_FUNCTION_STACK", stack);
        mediator.mediate(context);
        Assert.assertEquals(context.getProperty("r").toString(), "false");
    }

    @Test
    public void testEmptyFunc1() {
        String project = "project2";
        ModuleInfo moduleInfo = new ModuleInfo("testOrg", project, "1");
        Mediator mediator = new Mediator(moduleInfo);

        List<Property> properties = new ArrayList<>() {{
            add(new Property("paramFunctionName", "testEmpty"));
            add(new Property("paramSize", 0));
            add(new Property("returnType", "nil"));
        }};
        TestMessageContext context = createMessageContext(properties);

        Stack<TemplateContext> stack = new Stack<>();
        TemplateContext templateContext = new TemplateContext("testTemplateFunc", new ArrayList<>());
        stack.push(templateContext);

        HashMap<Object, Object> map = new HashMap<>();
        map.put("Result", "r");
        templateContext.setMappedValues(map);

        context.setProperty("_SYNAPSE_FUNCTION_STACK", stack);
        mediator.mediate(context);
        Assert.assertNull(context.getProperty("r"));
    }

    @Test
    public void testFloatTransform1() {
        String project = "project2";
        ModuleInfo moduleInfo = new ModuleInfo("testOrg", project, "1");
        Mediator mediator = new Mediator(moduleInfo);

        List<Property> properties = new ArrayList<>() {{
            add(new Property("paramFunctionName", "testFloat"));
            add(new Property("paramSize", 2));
            add(new Property("param0", "f1"));
            add(new Property("paramType0", "float"));
            add(new Property("param1", "f2"));
            add(new Property("paramType1", "float"));
            add(new Property("returnType", "float"));
        }};
        TestMessageContext context = createMessageContext(properties);

        Stack<TemplateContext> stack = new Stack<>();
        TemplateContext templateContext = new TemplateContext("testTemplateFunc", new ArrayList<>());
        stack.push(templateContext);

        HashMap<Object, Object> map = new HashMap<>();
        map.put("f1", "12.3");
        map.put("f2", "3.21");
        map.put("Result", "r");
        templateContext.setMappedValues(map);

        context.setProperty("_SYNAPSE_FUNCTION_STACK", stack);
        mediator.mediate(context);
        Assert.assertEquals(context.getProperty("r").toString(), "15.510000000000002");
    }

    @Test
    public void testJsonTransform1() {
        String project = "project2";
        ModuleInfo moduleInfo = new ModuleInfo("testOrg", project, "1");
        Mediator mediator = new Mediator(moduleInfo);

        List<Property> properties = new ArrayList<>() {{
            add(new Property("paramFunctionName", "testJson"));
            add(new Property("paramSize", 2));
            add(new Property("param0", "j1"));
            add(new Property("paramType0", "json"));
            add(new Property("param1", "j2"));
            add(new Property("paramType1", "json"));
            add(new Property("returnType", "json"));
        }};
        TestMessageContext context = createMessageContext(properties);

        Stack<TemplateContext> stack = new Stack<>();
        TemplateContext templateContext = new TemplateContext("testTemplateFunc", new ArrayList<>());
        stack.push(templateContext);

        HashMap<Object, Object> map = new HashMap<>();
        map.put("j1", "{\"name\": \"John\", \"age\": 23}");
        map.put("j2", "{\"city\": \"Colombo\", \"country\": \"Sri Lanka\"}");
        map.put("Result", "r");
        templateContext.setMappedValues(map);

        context.setProperty("_SYNAPSE_FUNCTION_STACK", stack);
        mediator.mediate(context);
        Assert.assertEquals(context.getProperty("r").toString(),
                "{\"name\":\"John\",\"age\":23,\"city\":\"Colombo\",\"country\":\"Sri Lanka\"}");
    }

    @Test
    public void testXmlReturn1() throws XMLStreamException {
        String project = "project3";
        ModuleInfo moduleInfo = new ModuleInfo("testOrg", project, "1");
        Mediator mediator = new Mediator(moduleInfo);

        List<Property> properties = new ArrayList<>() {{
            add(new Property("paramFunctionName", "testXmlReturn"));
            add(new Property("paramSize", 1));
            add(new Property("param0", "x"));
            add(new Property("paramType0", "xml"));
            add(new Property("returnType", "xml"));
        }};
        TestMessageContext context = createMessageContext(properties);

        Stack<TemplateContext> stack = new Stack<>();
        TemplateContext templateContext = new TemplateContext("testTemplateFunc", new ArrayList<>());
        stack.push(templateContext);

        HashMap<Object, Object> map = new HashMap<>();
        map.put("x", AXIOMUtil.stringToOM("<Document><FIToFICstmrCdtTrf><GrpHdr><MsgId>123489</MsgId><CreDtTm" +
                ">0111522180</CreDtTm><NbOfTxs>1</NbOfTxs><SttlmInf><SttlmMtd>INDA</SttlmMtd></SttlmInf></GrpHdr" +
                "><CdtTrfTxInf><PmtId><EndToEndId>12348912a456789123</EndToEndId></PmtId><IntrBkSttlmAmt " +
                "Ccy=\"USD\">500000</IntrBkSttlmAmt><ChrgBr>DEBT</ChrgBr><Dbtr><number>1</number></Dbtr><CdtrAgt" +
                "><FinInstnId>100009</FinInstnId></CdtrAgt></CdtTrfTxInf></FIToFICstmrCdtTrf></Document>"));
        map.put("Result", "r");
        templateContext.setMappedValues(map);

        context.setProperty("_SYNAPSE_FUNCTION_STACK", stack);
        mediator.mediate(context);
        Assert.assertEquals(context.getProperty("r").toString(),
                "<Document><FIToFICstmrCdtTrf><GrpHdr><MsgId>123489</MsgId><CreDtTm>0111522180</CreDtTm><NbOfTxs>1" +
                        "</NbOfTxs><SttlmInf><SttlmMtd>INDA</SttlmMtd></SttlmInf></GrpHdr><CdtTrfTxInf><PmtId" +
                        "><EndToEndId>12348912a456789123</EndToEndId></PmtId><IntrBkSttlmAmt " +
                        "Ccy=\"USD\">500000</IntrBkSttlmAmt><ChrgBr>DEBT</ChrgBr><Dbtr><number>1</number></Dbtr" +
                        "><CdtrAgt><FinInstnId>100009</FinInstnId></CdtrAgt></CdtTrfTxInf></FIToFICstmrCdtTrf" +
                        "></Document>");
    }

    @Test
    public void testXmlReturn2() throws XMLStreamException {
        String project = "project3";
        ModuleInfo moduleInfo = new ModuleInfo("testOrg", project, "1");
        Mediator mediator = new Mediator(moduleInfo);

        List<Property> properties = new ArrayList<>() {{
            add(new Property("paramFunctionName", "testXmlReturn"));
            add(new Property("paramSize", 1));
            add(new Property("param0", "x"));
            add(new Property("paramType0", "xml"));
            add(new Property("returnType", "xml"));
        }};
        TestMessageContext context = createMessageContext(properties);

        Stack<TemplateContext> stack = new Stack<>();
        TemplateContext templateContext = new TemplateContext("testTemplateFunc", new ArrayList<>());
        stack.push(templateContext);

        HashMap<Object, Object> map = new HashMap<>();
        map.put("x", AXIOMUtil.stringToOM("<library xmlns:bk=\"http://example.com/book\" xmlns:auth=\"http://example" +
                ".com/author\"><bk:Book bk:id=\"001\"><bk:Title>1984</bk:Title><bk:Author " +
                "auth:country=\"UK\"><auth:Name>George Orwell</auth:Name></bk:Author></bk:Book></library>"));
        map.put("Result", "r");
        templateContext.setMappedValues(map);

        context.setProperty("_SYNAPSE_FUNCTION_STACK", stack);
        mediator.mediate(context);
        Assert.assertEquals(context.getProperty("r").toString(),
                "<library><bk:Book xmlns:bk=\"http://example.com/book\" " +
                        "bk:id=\"001\"><bk:Title>1984</bk:Title><bk:Author xmlns:auth=\"http://example.com/author\" " +
                        "auth:country=\"UK\"><auth:Name>George Orwell</auth:Name></bk:Author></bk:Book></library>");
    }

    private TestMessageContext createMessageContext(List<Property> properties) {
        TestMessageContext context = new TestMessageContext();
        for (Property property : properties) {
            context.setProperty(property.name, property.value);
        }
        return context;
    }

    public static class Property {
        private final String name;
        private final Object value;

        public Property(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }

}
