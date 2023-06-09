package eu.domibus.core.ebms3.receiver.token;

import com.sun.xml.messaging.saaj.soap.impl.TextImpl;
import eu.domibus.core.ebms3.receiver.interceptor.TrustSenderInterceptor;
import eu.domibus.test.common.MessageTestUtility;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.util.WSSecurityUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class TokenReferenceExtractorTest {


    @Tested
    private TokenReferenceExtractor tokenReferenceExtractor;

    @Test
    public void testX509PKIPathv1TokenReference(@Mocked final Element securityHeader,
                                                @Mocked final Element signature,
                                                @Mocked final Element keyInfo,
                                                @Mocked final Element securityTokenrRefecence,
                                                @Mocked final Node uriNode,
                                                @Mocked final Node valueTypeNode) throws WSSecurityException {

        final String uri = "#X509-5f905b9f-f1e2-4f05-8369-123f330455d1";
        final String valueType = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509PKIPathv1";

        new Expectations() {{
            securityHeader.getFirstChild();
            result = signature;
            signature.getLocalName();
            result = WSConstants.SIGNATURE.getLocalPart();
            signature.getNamespaceURI();
            result = WSConstants.SIGNATURE.getNamespaceURI();
            signature.getFirstChild();
            result = keyInfo;
            keyInfo.getLocalName();
            result = TrustSenderInterceptor.KEYINFO.getLocalPart();
            keyInfo.getNamespaceURI();
            result = TrustSenderInterceptor.KEYINFO.getNamespaceURI();
            keyInfo.getFirstChild();
            result = securityTokenrRefecence;
            securityTokenrRefecence.getNodeType();
            result = Node.ELEMENT_NODE;
            securityTokenrRefecence.getChildNodes().getLength();
            result = 1;
            securityTokenrRefecence.getChildNodes().item(0).getLocalName();
            result = TokenReferenceExtractor.REFERENCE;
            securityTokenrRefecence.getChildNodes().item(0).getAttributes().getNamedItem(TokenReferenceExtractor.URI);
            result = uriNode;
            uriNode.getNodeValue();
            result = uri;
            securityTokenrRefecence.getChildNodes().item(0).getAttributes().getNamedItem(TokenReferenceExtractor.VALUE_TYPE);
            result = valueTypeNode;
            valueTypeNode.getNodeValue();
            result = valueType;

        }};
        final BinarySecurityTokenReference tokenReference = (BinarySecurityTokenReference) tokenReferenceExtractor.extractTokenReference(securityHeader);
        Assert.assertEquals(uri, tokenReference.getUri());
        Assert.assertEquals(valueType, tokenReference.getValueType());

    }

    @Test
    public void testKeySubjectIdendtifier(@Mocked final Element securityHeader,
                                          @Mocked final Element signature,
                                          @Mocked final Element keyInfo,
                                          @Mocked final Element securityTokenrRefecence) throws WSSecurityException {

        final String uri = "#X509-5f905b9f-f1e2-4f05-8369-123f330455d1";
        final String valueType = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509PKIPathv1";

        new Expectations() {{
            securityHeader.getFirstChild();
            result = signature;
            signature.getLocalName();
            result = WSConstants.SIGNATURE.getLocalPart();
            signature.getNamespaceURI();
            result = WSConstants.SIGNATURE.getNamespaceURI();
            signature.getFirstChild();
            result = keyInfo;
            keyInfo.getLocalName();
            result = TrustSenderInterceptor.KEYINFO.getLocalPart();
            keyInfo.getNamespaceURI();
            result = TrustSenderInterceptor.KEYINFO.getNamespaceURI();
            keyInfo.getFirstChild();
            result = securityTokenrRefecence;
            securityTokenrRefecence.getNodeType();
            result = Node.ELEMENT_NODE;
            securityTokenrRefecence.getChildNodes().getLength();
            result = 1;
            securityTokenrRefecence.getChildNodes().item(0).getLocalName();
            result = "KeyIdentifier";

        }};
        final BinarySecurityTokenReference tokenReference = (BinarySecurityTokenReference) tokenReferenceExtractor.extractTokenReference(securityHeader);
        Assert.assertNull(tokenReference);

    }

    @Test
    public void testGetSecTokenRefWithSpacesWithMock(@Mocked final Element securityHeader,
                                                     @Mocked final Element signature,
                                                     @Mocked final Element keyInfo,
                                                     @Mocked final Node textNode,
                                                     @Mocked final Element securityTokenrRefecence) throws WSSecurityException {

        new Expectations() {{
            securityHeader.getFirstChild();
            result = signature;
            signature.getLocalName();
            result = WSConstants.SIGNATURE.getLocalPart();
            signature.getNamespaceURI();
            result = WSConstants.SIGNATURE.getNamespaceURI();
            signature.getFirstChild();
            result = keyInfo;
            keyInfo.getLocalName();
            result = TrustSenderInterceptor.KEYINFO.getLocalPart();
            keyInfo.getNamespaceURI();
            result = TrustSenderInterceptor.KEYINFO.getNamespaceURI();
            keyInfo.getFirstChild();
            result = textNode;
            textNode.getNodeType();
            result = Node.TEXT_NODE;
            textNode.getNextSibling();
            result = securityTokenrRefecence;
            securityTokenrRefecence.getNodeType();
            result = Node.ELEMENT_NODE;
        }};
        final Element resultReferenceElement = tokenReferenceExtractor.getSecTokenRef(securityHeader);
        Assert.assertEquals(securityTokenrRefecence, resultReferenceElement);

    }

    @Test
    public void testGetSecTokenFromXMLWithSpaces() throws WSSecurityException, XMLStreamException, ParserConfigurationException {
        Document doc = MessageTestUtility.readDocument("/dataset/as4/RawXMLMessageWithSpaces.xml");
        final Element soapHeader = WSSecurityUtil.getSOAPHeader(doc);

        final Element securityHeader = WSSecurityUtil.getSecurityHeader(soapHeader, null, true);

        Element getToken = tokenReferenceExtractor.getSecTokenRef(securityHeader);
        Assert.assertNotNull(getToken);
    }

    @Test
    public void testGetFirstChildElement(@Mocked final Node parentNode,
                                         @Mocked final Node someNode,
                                         @Mocked final TextImpl textNode,
                                         @Mocked final Element resultElement) {

        new Expectations() {{
            parentNode.getFirstChild();
            result = someNode;
            someNode.getNodeType();
            result = Node.TEXT_NODE;
            someNode.getNextSibling();
            result = textNode;
            textNode.getNodeType();
            result = Node.TEXT_NODE;
            textNode.getNextSibling();
            result = resultElement;
            resultElement.getNodeType();
            result = Node.ELEMENT_NODE;
        }};

        Element targetElement = tokenReferenceExtractor.getFirstChildElement(parentNode);
        Assert.assertEquals(resultElement, targetElement);
    }
}