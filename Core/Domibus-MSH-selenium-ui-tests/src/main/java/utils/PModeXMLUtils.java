package utils;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PModeXMLUtils {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc;


    public PModeXMLUtils(String xmlStr) throws ParserConfigurationException, IOException, SAXException {
        try {
            doc = dBuilder.parse(new InputSource(new StringReader(xmlStr)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PModeXMLUtils(File file) throws ParserConfigurationException, IOException, SAXException {
        doc = dBuilder.parse(file);
    }

    public String getCurrentPartyName() {
        String currentParty = doc.getChildNodes().item(0).getAttributes().getNamedItem("party").getTextContent();
        log.debug("identified party name: " + currentParty);
        return currentParty;
    }

    public List<String> getAllPartyNames() {
        List<String> partyNames = new ArrayList<>();
        NodeList nList = doc.getElementsByTagName("party");
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            partyNames.add(nNode.getAttributes().getNamedItem("name").getTextContent());
        }
        log.debug("found part names: " + partyNames);
        return partyNames;
    }

    public List<String> getPartIds(String partyName) {

        List<String> partyIds = new ArrayList<>();
        NodeList nList = doc.getElementsByTagName("identifier");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            Node parent = nNode.getParentNode();
            String parentPartyName = parent.getAttributes().getNamedItem("name").getTextContent();
            if (StringUtils.equalsIgnoreCase(parentPartyName, partyName)) {
                partyIds.add(nNode.getAttributes().getNamedItem("name").getTextContent());
            }
        }
        log.debug("found: " + partyIds);
        return partyIds;
    }

    public HashMap<String, List<String>> getAllPartIds() {
        HashMap<String, List<String>> partyIds = new HashMap<>();

        NodeList nList = doc.getElementsByTagName("identifier");
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            log.debug("found ID = " + nNode.getAttributes().getNamedItem("partyId").getTextContent());

            String id = nNode.getAttributes().getNamedItem("partyId").getTextContent();

            Node parent = nNode.getParentNode();
            String partyName = parent.getAttributes().getNamedItem("name").getTextContent();

            if (!partyIds.containsKey(partyName)) {
                partyIds.put(partyName, new ArrayList<>());
            }
            partyIds.get(partyName).add(id);

        }
        log.debug("found: " + partyIds);
        return partyIds;
    }

    public String addPartyToPmode() throws IOException {
        String name = Gen.randomAlphaNumeric(5);
        String url = "http://" + name + ".com";
        String partyId = name + "_gw";
        String partyIdType =
                doc.getElementsByTagName("partyIdType").item(0).getAttributes().getNamedItem("name").getTextContent();

        addPartyToPmode(name, url, partyId, partyIdType);

        return name;
    }

    public void addPartyToPmode(String name, String url, String partyId, String partyIdType) throws IOException {
        Element newParty = doc.createElement("party");
        newParty.setAttribute("name", name);
        newParty.setAttribute("endpoint", url);

        Element identifier = doc.createElement("identifier");
        identifier.setAttribute("partyId", partyId);
        identifier.setAttribute("partyIdType", partyIdType);
        newParty.appendChild(identifier);

        doc.getElementsByTagName("parties")
                .item(0).appendChild(newParty);
    }

    public void addPartyToProcessInitiators(String partyName, String processName) throws IOException {
        Element initiatorParty = doc.createElement("initiatorParty");
        initiatorParty.setAttribute("name", partyName);

        NodeList initiatorParties = doc.getElementsByTagName("initiatorParties");
        for (int i = 0; i < initiatorParties.getLength(); i++) {
            Node iniParties = initiatorParties.item(i);
            String curProcName = iniParties.getParentNode().getAttributes().getNamedItem("name").getTextContent();
            if (StringUtils.isEmpty(processName)
                    || StringUtils.equalsIgnoreCase(processName, curProcName)) {
                iniParties.appendChild(initiatorParty);
            }
        }
    }

    public void addPartyToProcessResponders(String partyName, String processName) throws IOException {
        Element responderParty = doc.createElement("responderParty");
        responderParty.setAttribute("name", partyName);

        NodeList responderParties = doc.getElementsByTagName("responderParties");
        for (int i = 0; i < responderParties.getLength(); i++) {
            Node respParties = responderParties.item(i);
            String curProcName = respParties.getParentNode().getAttributes().getNamedItem("name").getTextContent();
            if (StringUtils.isEmpty(processName)
                    || StringUtils.equalsIgnoreCase(processName, curProcName)) {
                respParties.appendChild(responderParty);
            }
        }
    }

    public String printDoc() throws IOException {
        OutputFormat format = new OutputFormat(doc);
        format.setIndenting(false);
        Writer out = new StringWriter();
        XMLSerializer serializer = new XMLSerializer(out, format);
        serializer.serialize(doc);
        return out.toString();
    }

    public void removeParty(String partyName) {
        NodeList parties = doc.getElementsByTagName("party");
        for (int i = 0; i < parties.getLength(); i++) {
            Node partyNode = parties.item(i);
            if (partyNode.getAttributes().getNamedItem("name").getTextContent().equalsIgnoreCase(partyName)) {
                partyNode.getParentNode().removeChild(partyNode);
            }
        }

        removePartyFromAllProcesses(partyName);
    }

    public void removePartyFromAllProcesses(String partyName) {
        removePartyFromProcessInitiators(null, partyName);
        removePartyFromProcessResponders(null, partyName);
    }

    public void removePartyFromProcessInitiators(String process, String partyName) {
        NodeList initiators = doc.getElementsByTagName("initiatorParty");
        for (int i = 0; i < initiators.getLength(); i++) {
            Node curnode = initiators.item(i);
            if (curnode.getAttributes().getNamedItem("name").getTextContent().equalsIgnoreCase(partyName)) {
                Node parentProcessNode = curnode.getParentNode();
                String curProcess = parentProcessNode.getParentNode().getAttributes().getNamedItem("name").getTextContent();
                if (StringUtils.isEmpty(process)
                        || StringUtils.equalsIgnoreCase(curProcess, process)) {
                    parentProcessNode.removeChild(curnode);
                }
            }
        }
    }

    public void removePartyFromProcessResponders(String process, String partyName) {
        NodeList responders = doc.getElementsByTagName("responderParty");
        for (int i = 0; i < responders.getLength(); i++) {
            Node curnode = responders.item(i);
            if (curnode.getAttributes().getNamedItem("name").getTextContent().equalsIgnoreCase(partyName)) {
                Node parentProcessNode = curnode.getParentNode();
                String curProcess = parentProcessNode.getParentNode().getAttributes().getNamedItem("name").getTextContent();
                if (StringUtils.isEmpty(process)
                        || StringUtils.equalsIgnoreCase(curProcess, process)) {
                    parentProcessNode.removeChild(curnode);
                }
            }
        }
    }

    public Boolean isDuplicateDetectionEnabled() {
        return Boolean.parseBoolean(doc.getElementsByTagName("receptionAwareness")
                .item(0).getAttributes().getNamedItem("duplicateDetection").getTextContent());
    }

    public void setDuplicateDetection(Boolean enabled) {
        doc.getElementsByTagName("receptionAwareness")
                .item(0).getAttributes().getNamedItem("duplicateDetection").setTextContent(enabled.toString());
    }

    public void duplicateNode(String nodeName, boolean uppercase) {
        log.debug("duplicating node: " + nodeName);
        NodeList nList = doc.getElementsByTagName(nodeName);
        Node nNode = nList.item(0);
        Node nNodeCopy = nNode.cloneNode(true);
        if (uppercase) {
            uppercaseAllAttributes(nNodeCopy);
        }
        nNode.getParentNode().appendChild(nNodeCopy);
    }

    public void duplicatePartyIdNewName() {
        NodeList nList = doc.getElementsByTagName("party");
        Node nNode = nList.item(0);
        Node nNodeCopy = nNode.cloneNode(true);

        log.info("duplicating party with name: " + nNode.getAttributes().getNamedItem("name").getTextContent());
        log.info("setting new name for party");
        nNodeCopy.getAttributes().getNamedItem("name").setNodeValue("newPartyName");

        nNode.getParentNode().appendChild(nNodeCopy);

    }

    public void removePartyIdType() {
        NodeList nList = doc.getElementsByTagName("identifier");
        Node nNode = nList.item(0);
        nNode.getAttributes().getNamedItem("partyIdType").setTextContent("");

    }

    public void setAttributeValueForNode(String nodeName, String attributeName, String attributeValue) {
        NodeList nList = doc.getElementsByTagName(nodeName);
        Node nNode = nList.item(0);
        nNode.getAttributes().getNamedItem(attributeName).setTextContent(attributeValue);

    }

    public void uppercaseAllAttributes(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            NamedNodeMap attributes = node.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                attribute.setNodeValue(attribute.getNodeValue().toUpperCase());
            }
        }
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            uppercaseAllAttributes(children.item(i));
        }
    }


    public void makeEmptyNode(String nodeName) {
        log.debug("cloning empty node: " + nodeName);
        NodeList nList = doc.getElementsByTagName(nodeName);
        Node node = nList.item(0);
        Node newNode = doc.createElement(nodeName);
        node.getParentNode().appendChild(newNode);
    }

    public void uppercaseAllAttributes(String nodeName) {
        uppercaseAllAttributes(doc.getElementsByTagName(nodeName).item(0));
    }
}
