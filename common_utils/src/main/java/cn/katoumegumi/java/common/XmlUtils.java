package cn.katoumegumi.java.common;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * xml转换
 *
 * @author 星梦苍天
 */
public class XmlUtils {

    /**
     * map转xml字符串
     *
     * @param map
     * @return
     */
    public static String mapToXml(Map<Object, Object> map) {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            Element element = document.createElement("xml");
            document.appendChild(element);
            mapToXml(document, element, map);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            try {
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(document);
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                StringWriter writer = new StringWriter();
                StreamResult result = new StreamResult(writer);
                try {
                    transformer.transform(source, result);
                } catch (TransformerException e) {
                    e.printStackTrace();
                    return null;
                }
                return writer.getBuffer().toString();
            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
                return null;
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void mapToXml(Document document, Element root, Map<Object, Object> map) {
        Set<Map.Entry<Object, Object>> set = map.entrySet();
        for (Map.Entry<Object, Object> entry : set) {
            String key = WsBeanUtils.objectToT(entry.getKey(), String.class);
            Object value = entry.getValue();
            Element field = document.createElement(key);

            if (WsBeanUtils.isBaseType(value.getClass())) {
                field.appendChild(document.createTextNode(WsBeanUtils.objectToT(value, String.class)));
            } else if (WsBeanUtils.isArray(value.getClass())) {
                if (value instanceof Collection) {
                    Collection<Object> collection = (Collection<Object>) value;
                    for (Object o : collection) {
                        field.appendChild(document.createTextNode(WsBeanUtils.objectToT(o, String.class)));
                    }
                } else {
                    Object[] objects = (Object[]) value;
                    for (Object o : objects) {
                        field.appendChild(document.createTextNode(WsBeanUtils.objectToT(o, String.class)));
                    }
                }
            } else if (value instanceof Map) {
                mapToXml(document, field, (Map<Object, Object>) value);
            } else {
                Map<Object, Object> objectMap = WsBeanUtils.convertToMap(value);
                mapToXml(document, field, objectMap);
            }
            root.appendChild(field);
        }
    }


    public static Map<Object, Object> xmlToMap(String xml) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            byteArrayInputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            try {
                Document document = documentBuilder.parse(byteArrayInputStream);
                document.getDocumentElement().normalize();
                Element element = document.getDocumentElement();
                return xmlToMap(element);
            } catch (SAXException | IOException e) {
                e.printStackTrace();
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } finally {
            if (byteArrayInputStream != null) {
                try {
                    byteArrayInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    private static Map<Object, Object> xmlToMap(Element element) {
        Map<Object, Object> map = new HashMap<>();
        NodeList nodeList = element.getChildNodes();
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) node;
                NodeList nl = e.getChildNodes();
                int nlLength = nl.getLength();
                if (nlLength == 0) {
                    continue;
                } else if (nlLength == 1) {
                    Node childNode = nl.item(0);
                    if (childNode.getNodeType() == Node.TEXT_NODE) {
                        map.put(e.getNodeName(), childNode.getNodeValue());
                    } else if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element ce = (Element) childNode;
                        map.put(e.getNodeName(), xmlToMap(ce));
                    }
                } else {
                    List<Object> list = new ArrayList<>(nlLength);
                    for (int k = 0; k < nlLength; k++) {
                        Node childNode = nl.item(k);
                        if (childNode.getNodeType() == Node.TEXT_NODE) {
                            list.add(childNode.getNodeValue());
                        } else if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element ce = (Element) childNode;
                            list.add(xmlToMap(ce));
                        }
                    }
                    map.put(e.getNodeName(), list);
                }


            }
        }
        if (map.size() > 0) {
            return map;
        } else {
            return null;
        }

    }

}
