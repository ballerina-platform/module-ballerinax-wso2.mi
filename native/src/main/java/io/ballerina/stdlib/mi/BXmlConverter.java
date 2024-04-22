package io.ballerina.stdlib.mi;

import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlItem;
import io.ballerina.runtime.internal.values.XmlPi;
import org.apache.axiom.om.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public class BXmlConverter {
    private static final OMFactory factory = OMAbstractFactory.getOMFactory();

    static Pair<String, String> extractNamespace(String value) {

        if (value.startsWith("{")) {
            int index = value.indexOf("}");
            String ns = value.substring(1, index);
            String localName = value.substring(index + 1);
            return Pair.of(ns, localName);
        }

        return Pair.of("", value);
    }

    public static OMElement toOMElement(BXml bXml) {
        BXmlItem xmlItem = (BXmlItem) bXml;

        OMNamespace namespace = factory.createOMNamespace(xmlItem.getQName().getNamespaceURI(), xmlItem.getQName().getPrefix());
        BMap<BString, BString> bMap = xmlItem.getAttributesMap();

        OMElement rootElement = factory.createOMElement(xmlItem.getQName().getLocalPart(), namespace);
        // create a map of namespaces with key:"" and value:null
        Map<String, OMNamespace> namespaceMap = new HashMap<>();
        namespaceMap.put("", null);

        for (Map.Entry<BString, BString> entry : bMap.entrySet()) {
            //TODO: handle namespace
            if (entry.getKey().getValue().startsWith(BXmlItem.XMLNS_NS_URI_PREFIX)) {
                //if this is a namespace
                Pair<String, String> pair = extractNamespace(entry.getKey().getValue());
                OMNamespace omNamespace = factory.createOMNamespace(entry.getValue().getValue(), pair.getRight());
                namespaceMap.put(entry.getValue().getValue(), omNamespace);
            }
        }
        for (Map.Entry<BString, BString> attribute : bMap.entrySet()) {
            if (!attribute.getKey().getValue().startsWith(BXmlItem.XMLNS_NS_URI_PREFIX)) {
                //if this is a namespace
                Pair<String, String> pair = extractNamespace(attribute.getKey().getValue());
                OMNamespace ns = namespaceMap.get(pair.getLeft());
                OMAttribute omattribute = factory.createOMAttribute(pair.getRight(), namespaceMap.get(pair.getLeft()), attribute.getValue().getValue());
                rootElement.addAttribute(omattribute);
                //TODO: previously used OMAttribute creation method research why it was changed to attribute.
            }

        }
        addChildrenElements(rootElement, bXml);
        return rootElement;
    }

    static void addChildrenElements(OMElement rootElement, BXml bXml) {
        BXmlItem xmlItem = (BXmlItem) bXml;
        for (int i = 0; i < xmlItem.children().size(); i++) {
            BXml child = xmlItem.children().getItem(i);
            switch (child.getNodeType()) {
                case ELEMENT:
                    OMElement childElement = toOMElement(child);
                    rootElement.addChild(childElement);
                    break;
                case TEXT:
                    OMText omText = factory.createOMText(rootElement, child.getTextValue());
                    rootElement.addChild(omText);
                    break;
                case COMMENT:
                    OMComment omComment = factory.createOMComment(rootElement, child.getTextValue());
                    rootElement.addChild(omComment);
                    break;
                case PI:
                    XmlPi xmlPi = (XmlPi) child;
                    OMProcessingInstruction omProcessingInstruction = factory.createOMProcessingInstruction(null, xmlPi.getTarget(), xmlPi.getData());
                    rootElement.addChild(omProcessingInstruction);
                    break;
                default:
                    break;
            }
        }
    }

    private boolean isNamespace(String value) {
        // starts with XmlItem.XMLNS_NS_URI_PREFIX true else false
        return value.startsWith(BXmlItem.XMLNS_NS_URI_PREFIX);
    }
}

