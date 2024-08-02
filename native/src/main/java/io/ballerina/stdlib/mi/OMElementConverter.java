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

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlItem;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMComment;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMProcessingInstruction;
import org.apache.axiom.om.OMText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;

public class OMElementConverter {

    public static BXml toBXml(OMElement omElement) {
        return getXmlItem(omElement);
    }

    private static BXml toBXml(OMNode omNode) {
        switch (omNode.getType()) {
            case OMNode.ELEMENT_NODE:
                return getXmlItem((OMElement) omNode);
            case OMNode.TEXT_NODE:
            case OMNode.SPACE_NODE:
                return getXmlText((OMText) omNode);
            case OMNode.PI_NODE:
                return getXmlPI((OMProcessingInstruction) omNode);
            case OMNode.COMMENT_NODE:
                return getXmlComment((OMComment) omNode);
            case OMNode.CDATA_SECTION_NODE:
                return getXmlCData((OMText) omNode);
            case OMNode.DTD_NODE:
                // NOTE: cannot occur
                return null;
            case OMNode.ENTITY_REFERENCE_NODE:
                // NOTE: cannot occur
                return null;
            default:
                return null;
        }
    }


    private static QName getQNameOMElement(OMElement omElement) {
        return new QName(omElement.getQName().getNamespaceURI(), omElement.getQName().getLocalPart(),
                omElement.getQName().getPrefix());
    }

    private static QName getQNameOMAttribute(OMAttribute omAttribute) {
        return omAttribute.getQName();
    }

    private static void addAttributes(OMElement omElement, BXmlItem xmlItem) {
        // NOTE: Extracted the idea from
        // bvm/ballerina-runtime/src/main/java/io/ballerina/runtime/internal/XmlTreeBuilder.java
        Iterator attributes = omElement.getAllAttributes();
        BMap<BString, BString> attributesMap = xmlItem.getAttributesMap();
        Set<QName> usedNS = new HashSet<>();

        while (attributes.hasNext()) {

            OMAttribute attribute = (OMAttribute) attributes.next();
            QName qName = getQNameOMAttribute(attribute);
            //CHECK: Good to put break point here and check the values
            attributesMap.put(StringUtils.fromString(qName.toString()),
                    StringUtils.fromString(attribute.getAttributeValue()));
            if (!qName.getPrefix().isEmpty()) {
                usedNS.add(qName);
            }
        }
        if (omElement.getQName().getPrefix() != null) {
            if (!omElement.getQName().getPrefix().isEmpty()) {
                usedNS.add(getQNameOMElement(omElement));
            }
        }

        for (QName qName : usedNS) {
            String prefix = qName.getPrefix();
            String namespaceURI = qName.getNamespaceURI();
            if (namespaceURI.isEmpty()) {
                namespaceURI = "";
            }

            BString xmlnsPrefix = StringUtils.fromString(BXmlItem.XMLNS_NS_URI_PREFIX + prefix);
            attributesMap.put(xmlnsPrefix, StringUtils.fromString(namespaceURI));
        }

        //TODO: There is still another part in the code that referred
        //NOTE: This is for the namespaces that are declared but not used in its attributes
        for (Iterator it = omElement.getAllDeclaredNamespaces(); it.hasNext(); ) {

            OMNamespace omNamespace = (OMNamespace) it.next();
            String uri = omNamespace.getNamespaceURI();
            String prefix = omNamespace.getPrefix();
            if (prefix == null || prefix.isEmpty()) {
                attributesMap.put(BXmlItem.XMLNS_PREFIX, StringUtils.fromString(uri));
            } else {
                attributesMap.put(StringUtils.fromString(BXmlItem.XMLNS_NS_URI_PREFIX + prefix),
                        StringUtils.fromString(uri));
            }
        }
    }

    private static BXml getXmlItem(OMElement omElement) {
        // TODO: find the issue that fail and put it here
        QName qName = getQNameOMElement(omElement);
        BXmlItem xmlItem = ValueCreator.createXmlItem(qName, false);

        addAttributes(omElement, xmlItem);

        ArrayList<BXml> xmlList = new ArrayList<>();

        // CHECK: why we did like this way (getDescendants rather than getChildren)
        Iterator descendants = omElement.getChildren();

        while (descendants.hasNext()) {
            OMNode childNode = (OMNode) descendants.next();
            if (childNode.getParent() == omElement) {
                BXml childXml = toBXml(childNode);

                xmlList.add(childXml);
            }
        }
        xmlItem.setChildren(ValueCreator.createXmlSequence(xmlList));
        return xmlItem;
    }

    private static BXml getXmlText(OMText omText) {
        return ValueCreator.createXmlText(StringUtils.fromString(omText.getText()));
    }

    private static BXml getXmlPI(OMProcessingInstruction omProcessingInstruction) {
        return ValueCreator.createXmlProcessingInstruction(StringUtils.fromString(omProcessingInstruction.getTarget()),
                StringUtils.fromString(omProcessingInstruction.getValue()));
    }

    private static BXml getXmlComment(OMComment omComment) {
        return ValueCreator.createXmlComment(StringUtils.fromString(omComment.getValue()));
    }

    private static BXml getXmlCData(OMText omText) {
        String text = omText.getText();
        return ValueCreator.createXmlText(StringUtils.fromString(text));
    }
}
