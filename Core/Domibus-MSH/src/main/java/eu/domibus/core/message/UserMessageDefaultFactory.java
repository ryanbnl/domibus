package eu.domibus.core.message;

import eu.domibus.api.datasource.AutoCloseFileDataSource;
import eu.domibus.api.model.*;
import eu.domibus.api.model.splitandjoin.MessageFragmentEntity;
import eu.domibus.api.model.splitandjoin.MessageGroupEntity;
import eu.domibus.core.message.dictionary.*;
import eu.domibus.core.message.splitandjoin.SplitAndJoinDefaultService;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import java.io.File;
import java.util.*;

/**
 * Factory for creating UserMessage instances
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class UserMessageDefaultFactory implements UserMessageFactory {

    private static final List<String> ALLOWED_PROPERTIES = Arrays.asList(
            "originalSender", "finalRecipient", "trackingIdentifier");
    public static final String CID_FRAGMENT = "cid:fragment";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String TEXT_XML = "text/xml";

    protected PartPropertyDictionaryService partPropertyDictionaryService;
    protected MessagePropertyDictionaryService messagePropertyDictionaryService;
    protected PartyIdDictionaryService partyIdDictionaryService;
    protected PartyRoleDictionaryService partyRoleDictionaryService;
    protected AgreementDictionaryService agreementDictionaryService;
    protected ServiceDictionaryService serviceDictionaryService;
    protected ActionDictionaryService actionDictionaryService;
    protected MpcDictionaryService mpcDictionaryService;

    public UserMessageDefaultFactory(PartPropertyDictionaryService partPropertyDictionaryService, MessagePropertyDictionaryService messagePropertyDictionaryService, PartyIdDictionaryService partyIdDictionaryService, PartyRoleDictionaryService partyRoleDictionaryService, AgreementDictionaryService agreementDictionaryService, ServiceDictionaryService serviceDictionaryService, ActionDictionaryService actionDictionaryService, MpcDictionaryService mpcDictionaryService) {
        this.partPropertyDictionaryService = partPropertyDictionaryService;
        this.messagePropertyDictionaryService = messagePropertyDictionaryService;
        this.partyIdDictionaryService = partyIdDictionaryService;
        this.partyRoleDictionaryService = partyRoleDictionaryService;
        this.agreementDictionaryService = agreementDictionaryService;
        this.serviceDictionaryService = serviceDictionaryService;
        this.actionDictionaryService = actionDictionaryService;
        this.mpcDictionaryService = mpcDictionaryService;
    }

    @Override
    public UserMessage createUserMessageFragment(UserMessage sourceMessage, MessageGroupEntity messageGroupEntity, Long fragmentNumber, String fragmentFile) {
        UserMessage result = new UserMessage();
        result.setMessageFragment(true);
        String messageId = sourceMessage.getMessageId() + SplitAndJoinDefaultService.FRAGMENT_FILENAME_SEPARATOR + fragmentNumber;
        result.setMessageId(messageId);
        result.setRefToMessageId(sourceMessage.getRefToMessageId());
        result.setTimestamp(sourceMessage.getTimestamp());
        result.setConversationId(sourceMessage.getConversationId());
        result.setAgreementRef(getAgreementRef(sourceMessage));
        result.setMpc(mpcDictionaryService.findOrCreateMpc(sourceMessage.getMpcValue()));
        result.setAction(actionDictionaryService.findOrCreateAction(sourceMessage.getActionValue()));
        result.setService(serviceDictionaryService.findOrCreateService(sourceMessage.getService().getValue(), sourceMessage.getService().getType()));
        result.setPartyInfo(createPartyInfo(sourceMessage.getPartyInfo()));
        result.setMessageProperties(createMessageProperties(sourceMessage.getMessageProperties()));

        return result;
    }

    @Override
    public UserMessage cloneUserMessageFragment(UserMessage userMessageFragment) {
        UserMessage result = new UserMessage();
        result.setMessageId(userMessageFragment.getMessageId());
        result.setRefToMessageId(userMessageFragment.getRefToMessageId());
        result.setTimestamp(userMessageFragment.getTimestamp());

        result.setConversationId(userMessageFragment.getConversationId());
        result.setAgreementRef(getAgreementRef(userMessageFragment));
        result.setAction(actionDictionaryService.findOrCreateAction(userMessageFragment.getActionValue()));
        result.setService(serviceDictionaryService.findOrCreateService(userMessageFragment.getService().getValue(), userMessageFragment.getService().getType()));

        result.setPartyInfo(createPartyInfo(userMessageFragment.getPartyInfo()));
        result.setMessageProperties(createMessageProperties(userMessageFragment.getMessageProperties()));
        return result;
    }

    @Override
    public MessageFragmentEntity createMessageFragmentEntity(MessageGroupEntity messageGroupEntity, Long fragmentNumber) {
        MessageFragmentEntity result = new MessageFragmentEntity();
        result.setFragmentNumber(fragmentNumber);
        result.setGroup(messageGroupEntity);
        return result;
    }

    @Override
    public PartInfo createMessageFragmentPartInfo(String fragmentFile, Long fragmentNumber) {
        final PartInfo partInfo = new PartInfo();
        partInfo.setInBody(false);
        partInfo.setPayloadDatahandler(new DataHandler(new AutoCloseFileDataSource(fragmentFile)));
        partInfo.setHref(CID_FRAGMENT + fragmentNumber);
        partInfo.setFileName(fragmentFile);
        partInfo.setLength(new File(fragmentFile).length());
        partInfo.setMime(APPLICATION_OCTET_STREAM);

        PartProperty partProperty = partPropertyDictionaryService.findOrCreatePartProperty(Property.MIME_TYPE, APPLICATION_OCTET_STREAM, null);
        Set<PartProperty> partProperties = new HashSet<>();
        partProperties.add(partProperty);
        partInfo.setPartProperties(partProperties);
        return partInfo;
    }

    protected AgreementRefEntity getAgreementRef(UserMessage userMessage) {
        AgreementRefEntity agreementRef = userMessage.getAgreementRef();
        if (agreementRef == null) {
            return null;
        }
        return agreementDictionaryService.findOrCreateAgreement(agreementRef.getValue(), agreementRef.getType());
    }

    protected ServiceEntity createService(UserMessage userMessage) {
        ServiceEntity result = new ServiceEntity();
        ServiceEntity userMessageService = userMessage.getService();
        result.setType(userMessageService.getType());
        result.setValue(userMessageService.getValue());
        return result;
    }

    protected PartyInfo createPartyInfo(final PartyInfo source) {
        final PartyInfo partyInfo = new PartyInfo();

        if (source.getFrom() != null) {
            final From from = new From();
            PartyRole fromPartyRole = partyRoleDictionaryService.findOrCreateRole(source.getFrom().getRoleValue());
            from.setFromRole(fromPartyRole);

            PartyId fromPartyId = source.getFrom().getFromPartyId();
            PartyId fromParty = partyIdDictionaryService.findOrCreateParty(fromPartyId.getValue(), fromPartyId.getType());
            from.setFromPartyId(fromParty);

            partyInfo.setFrom(from);
        }

        if (source.getTo() != null) {
            final To to = new To();
            PartyRole toPartyRole = partyRoleDictionaryService.findOrCreateRole(source.getTo().getRoleValue());
            to.setToRole(toPartyRole);

            PartyId toPartyId = source.getTo().getToPartyId();
            PartyId toParty = partyIdDictionaryService.findOrCreateParty(toPartyId.getValue(), toPartyId.getType());
            to.setToPartyId(toParty);

            partyInfo.setTo(to);
        }

        return partyInfo;
    }

    protected Set<MessageProperty> createMessageProperties(final Collection<MessageProperty> userMessageProperties) {
        final Set<MessageProperty> messageProperties = new HashSet<>();

        for (MessageProperty sourceProperty : userMessageProperties) {
            if (ALLOWED_PROPERTIES.contains(sourceProperty.getName())) {
                MessageProperty messageProperty = messagePropertyDictionaryService.findOrCreateMessageProperty(sourceProperty.getName(), sourceProperty.getValue(), sourceProperty.getType());
                messageProperties.add(messageProperty);
            }
        }

        return messageProperties;
    }
}
