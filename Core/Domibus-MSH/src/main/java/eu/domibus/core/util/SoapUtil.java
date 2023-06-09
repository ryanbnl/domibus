package eu.domibus.core.util;

import com.google.common.io.CharStreams;
import eu.domibus.api.ebms3.model.ObjectFactory;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.MessageImpl;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_LOGGING_EBMS3_ERROR_PRINT;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_LOGGING_PAYLOAD_PRINT;

/**
 * @author idragusa
 * @author Cosmin Baciu
 * @since 3.2.5
 */
@Service
public class SoapUtil {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SoapUtil.class);

    protected DomibusPropertyProvider domibusPropertyProvider;
    protected XMLUtil xmlUtil;

    public SoapUtil(DomibusPropertyProvider domibusPropertyProvider, XMLUtil xmlUtil) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.xmlUtil = xmlUtil;
    }

    @Timer(clazz = SoapUtil.class, value = "logMessage")
    @Counter(clazz = SoapUtil.class, value = "logMessage")
    public void logMessage(SOAPMessage request) throws IOException, TransformerException {
        if (LOG.isDebugEnabled() && domibusPropertyProvider.getBooleanProperty(DOMIBUS_LOGGING_PAYLOAD_PRINT)) {
            try (StringWriter sw = new StringWriter()) {
                TransformerFactory transformerFactory = xmlUtil.getTransformerFactory();
                transformerFactory.newTransformer().transform(new DOMSource(request.getSOAPPart()), new StreamResult(sw));

                LOG.debug(sw.toString());
                LOG.debug("received attachments:");
                final Iterator i = request.getAttachments();
                while (i.hasNext()) {
                    LOG.debug("attachment: {}", i.next());
                }
            }
        }
    }

    /**
     * Creates a SOAPMessage based on a CXF MessageImpl instance
     *
     * @param messageImpl
     * @return
     * @throws SOAPException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws TransformerException
     */
    public SOAPMessage createUserMessage(MessageImpl messageImpl) throws SOAPException, IOException, ParserConfigurationException, SAXException, TransformerException {
        LOG.debug("Creating SOAPMessage");
        SOAPMessage message = xmlUtil.getMessageFactorySoap12().createMessage();

        final Collection<Attachment> attachments = messageImpl.getAttachments();
        for (Attachment attachment : attachments) {
            LOG.debug("Creating attachmentPart [{}]", attachment.getId());
            final AttachmentPart attachmentPart = message.createAttachmentPart(attachment.getDataHandler());

            attachmentPart.setContentId(attachment.getId());
            attachmentPart.setContentType(attachment.getDataHandler().getContentType());
            message.addAttachmentPart(attachmentPart);
            LOG.debug("Finished creating attachmentPart [{}]", attachment.getId());
        }

        LOG.debug("Getting soapEnvelopeString");
        final String soapEnvelopeString = IOUtils.toString(messageImpl.getContent(InputStream.class), StandardCharsets.UTF_8);
        LOG.debug("Finished getting soapEnvelopeString");

        final SOAPMessage soapMessage = createSOAPMessage(soapEnvelopeString);
        final SOAPElement next = (SOAPElement) soapMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next();
        message.getSOAPHeader().addChildElement(next);

        message.saveChanges();

        if (LOG.isDebugEnabled()) {
            final String rawXMLMessage = getRawXMLMessage(message);
            LOG.debug("Created SOAPMessage [{}]", rawXMLMessage);
        }

        return message;
    }

    public String getRawXMLMessage(SOAPMessage soapMessage) throws TransformerException {
        return getRawXmlFromNode(soapMessage.getSOAPPart());
    }

    public String getRawXMLMessage(Node node) throws TransformerException {
        return getRawXmlFromNode(node);
    }

    protected String getRawXmlFromNode(Node node) throws TransformerException {
        final StringWriter rawXmlMessageWriter = new StringWriter();

        TransformerFactory transformerFactory = xmlUtil.getTransformerFactory();

        transformerFactory.newTransformer().transform(
                new DOMSource(node),
                new StreamResult(rawXmlMessageWriter));

        return rawXmlMessageWriter.toString();
    }

    public SOAPMessage createSOAPMessage(final String rawXml) throws SOAPException, IOException, ParserConfigurationException, SAXException {
        LOG.debug("Creating SOAPMessage from rawXML [{}]", rawXml);

        MessageFactory factory = xmlUtil.getMessageFactorySoap12();
        SOAPMessage message = factory.createMessage();

        DocumentBuilderFactory dbFactory = xmlUtil.getDocumentBuilderFactoryNamespaceAware();
        DocumentBuilder builder = dbFactory.newDocumentBuilder();

        try (StringReader stringReader = new StringReader(rawXml);
             InputStream targetStream = new ByteArrayInputStream(CharStreams.toString(stringReader).getBytes(StandardCharsets.UTF_8.name()))
        ) {
            LOG.debug("Parsing SOAPMessage document");
            Document document = builder.parse(targetStream);
            LOG.debug("Finished parsing SOAPMessage document");

            DOMSource domSource = new DOMSource(document);
            SOAPPart soapPart = message.getSOAPPart();
            soapPart.setContent(domSource);
            return message;
        }
    }


    public void logRawXmlMessageWhenEbMS3Error(final SOAPMessage soapMessage) {
        final boolean printError = domibusPropertyProvider.getBooleanProperty(DOMIBUS_LOGGING_EBMS3_ERROR_PRINT);
        if (!printError) {
            LOG.debug("Printing EbMS3 error is disabled, exiting");
            return;
        }
        String xmlMessage;
        try {
            xmlMessage = getRawXMLMessage(soapMessage);
        } catch (TransformerException e) {
            LOG.warn("Unable to extract the raw message XML due to: ", e);
            return;
        }

        LOG.error("An EbMS3 error was received check the raw xml message: {}", xmlMessage);
    }

    public Node getChildElement(SOAPMessage request) throws SOAPException {
        if (request.getSOAPBody().hasChildNodes()) {
            return ((Node) request.getSOAPBody().getChildElements().next());
        }
        return null;
    }

}
