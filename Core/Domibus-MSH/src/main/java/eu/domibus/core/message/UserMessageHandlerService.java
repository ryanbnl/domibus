package eu.domibus.core.message;

import eu.domibus.api.ebms3.model.mf.Ebms3MessageFragmentType;
import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.UserMessage;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface UserMessageHandlerService {
    /**
     * Handles incoming UserMessages
     *
     * @param legConfiguration
     * @param pmodeKey
     * @param request
     * @param userMessage
     * @param testMessage
     * @return
     * @throws EbMS3Exception
     * @throws TransformerException
     * @throws IOException
     * @throws JAXBException
     * @throws SOAPException
     */
    SOAPMessage handleNewUserMessage(LegConfiguration legConfiguration, String pmodeKey, SOAPMessage request, UserMessage userMessage, Ebms3MessageFragmentType ebms3MessageFragmentType, List<PartInfo> partInfoList, boolean testMessage) throws EbMS3Exception, TransformerException, IOException, JAXBException, SOAPException;

    /**
     * Handles incoming source messages for SplitAndJoin
     *
     * @param legConfiguration
     * @param pmodeKey
     * @param request
     * @param testMessage
     * @return
     * @throws EbMS3Exception
     * @throws TransformerException
     * @throws IOException
     * @throws JAXBException
     * @throws SOAPException
     */
    SOAPMessage handleNewSourceUserMessage(LegConfiguration legConfiguration, String pmodeKey, SOAPMessage request, UserMessage userMessage, List<PartInfo> partInfoList, boolean testMessage) throws EbMS3Exception, TransformerException, IOException, JAXBException, SOAPException;

}
