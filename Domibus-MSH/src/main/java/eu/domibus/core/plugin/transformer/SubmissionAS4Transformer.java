package eu.domibus.core.plugin.transformer;

import eu.domibus.api.model.*;
import eu.domibus.core.generator.id.MessageIdGenerator;
import eu.domibus.core.message.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.Submission;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * @author Christian Koch, Stefan Mueller
 */
@org.springframework.stereotype.Service
public class SubmissionAS4Transformer {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SubmissionAS4Transformer.class);

    @Autowired
    private MessageIdGenerator messageIdGenerator;

    @Autowired
    protected MpcDao mpcDao;

    @Autowired
    protected MessagePropertyDao messagePropertyDao;

    @Autowired
    protected ServiceDao serviceDao;

    @Autowired
    protected ActionDao actionDao;

    @Autowired
    protected AgreementDao agreementDao;

    @Autowired
    protected PartyIdDao partyIdDao;

    @Autowired
    protected PartyRoleDao partyRoleDao;

    @Autowired
    protected PartPropertyDao partPropertyDao;

    @Autowired
    protected MessageDictionaryService messageDictionaryService;

    public UserMessage transformFromSubmission(final Submission submission) {
        final UserMessage result = new UserMessage();
        final MpcEntity mpc = messageDictionaryService.findOrCreateMpc(submission.getMpc());
        result.setMpc(mpc);
        this.generateMessageInfo(submission, result);
        this.generatePartyInfo(submission, result);
        this.generateCollaborationInfo(submission, result);
        this.generateMessageProperties(submission, result);

        return result;
    }

    private void generateMessageProperties(final Submission submission, final UserMessage result) {
        Set<MessageProperty> messageProperties = new HashSet<>();

        for (Submission.TypedProperty propertyEntry : submission.getMessageProperties()) {
            final Property prop = new Property();
            prop.setName(propertyEntry.getKey());
            prop.setValue(propertyEntry.getValue());
            prop.setType(propertyEntry.getType());

            final MessageProperty propertyByName = messagePropertyDao.findOrCreateProperty(prop.getName(), prop.getValue(), prop.getType());
            messageProperties.add(propertyByName);
        }

        result.setMessageProperties(messageProperties);
    }

    private void generateCollaborationInfo(final Submission submission, final UserMessage result) {
        // if the conversation id is null, we generate one; otherwise we pass it forward
        String conversationId = submission.getConversationId();
        result.setConversationId(conversationId == null ? this.generateConversationId() : conversationId.trim());

        final ActionEntity action = messageDictionaryService.findOrCreateAction(submission.getAction());
        result.setAction(action);

        final AgreementRefEntity agreementRef = messageDictionaryService.findOrCreateAgreement(submission.getAgreementRef(), submission.getAgreementRefType());
        result.setAgreementRef(agreementRef);

        final ServiceEntity service = messageDictionaryService.findOrCreateService(submission.getService(), submission.getServiceType());
        result.setService(service);
    }

    private void generateMessageInfo(final Submission submission, final UserMessage result) {
        result.setMessageId((submission.getMessageId() != null && submission.getMessageId().trim().length() > 0) ? submission.getMessageId() : this.messageIdGenerator.generateMessageId());
        result.setTimestamp(new Date());
        result.setRefToMessageId(submission.getRefToMessageId());
    }

    private void generatePartyInfo(final Submission submission, final UserMessage result) {
        final PartyInfo partyInfo = new PartyInfo();
        result.setPartyInfo(partyInfo);

        final From partyFrom = getPartyFrom(submission, partyInfo);
        partyInfo.setFrom(partyFrom);

        final To partyTo = getPartyTo(submission, partyInfo);
        partyInfo.setTo(partyTo);
    }

    private From getPartyFrom(Submission submission, PartyInfo partyInfo) {
        final From from = new From();

        final PartyRole fromRole = messageDictionaryService.findOrCreateRole(submission.getFromRole());
        from.setRole(fromRole);

        final Set<Submission.Party> fromParties = submission.getFromParties();
        if (CollectionUtils.isNotEmpty(fromParties)) {
            if(fromParties.size() > 1) {
                LOG.warn("Cannot have multiple from parties, using the first party");
            }

            final Submission.Party party = fromParties.iterator().next();
            final PartyId fromParty = messageDictionaryService.findOrCreateParty(party.getPartyId(), party.getPartyIdType());
            from.setPartyId(fromParty);

            return from;
        }
        return from;
    }

    private To getPartyTo(Submission submission, PartyInfo partyInfo) {
        final To to = new To();

        final PartyRole toRole = messageDictionaryService.findOrCreateRole(submission.getToRole());
        to.setRole(toRole);

        final Set<Submission.Party> toParties = submission.getToParties();
        if (CollectionUtils.isNotEmpty(toParties)) {
            if(toParties.size() > 1) {
                LOG.warn("Cannot have multiple to parties, using the first party");
            }
            final Submission.Party party = toParties.iterator().next();
            final PartyId toParty = messageDictionaryService.findOrCreateParty(party.getPartyId(), party.getPartyIdType());
            to.setPartyId(toParty);
            return to;
        }
        return to;
    }


    public List<PartInfo> generatePartInfoList(final Submission submission) {
        List<PartInfo> result = new ArrayList<>();

        for (final Submission.Payload payload : submission.getPayloads()) {
            final PartInfo partInfo = new PartInfo();
            partInfo.setInBody(payload.isInBody());
            partInfo.setPayloadDatahandler(payload.getPayloadDatahandler());
            partInfo.setHref(payload.getContentId());
            partInfo.setLength(payload.getPayloadSize());
            partInfo.setFileName(payload.getFilepath());

            Set<PartProperty> properties = new HashSet<>();
            partInfo.setPartProperties(properties);
            for (final Submission.TypedProperty entry : payload.getPayloadProperties()) {
                final PartProperty property = messageDictionaryService.findOrCreatePartProperty(entry.getKey(), entry.getValue(), entry.getType());
                properties.add(property);
            }
            result.add(partInfo);
        }

        return result;

    }

    public Submission transformFromMessaging(final UserMessage userMessage, List<PartInfo> partInfoList) {
        final Submission result = new Submission();

        if (userMessage == null) {
            return result;
        }

        result.setMpc(userMessage.getMpcValue());
        result.setAction(userMessage.getActionValue());
        result.setService(userMessage.getService().getValue());
        result.setServiceType(userMessage.getService().getType());
        if (userMessage.getAgreementRef() != null) {
            result.setAgreementRef(userMessage.getAgreementRef().getValue());
            result.setAgreementRefType(userMessage.getAgreementRef().getType());
        }
        result.setConversationId(userMessage.getConversationId());

        result.setMessageId(userMessage.getMessageId());
        result.setRefToMessageId(userMessage.getRefToMessageId());

        if (CollectionUtils.isNotEmpty(partInfoList)) {
            for (final PartInfo partInfo : partInfoList) {
                addPayload(result, partInfo);
            }
        }
        result.setFromRole(userMessage.getPartyInfo().getFrom().getRoleValue());
        result.setToRole(userMessage.getPartyInfo().getTo().getRoleValue());

        final PartyId partyFromId = userMessage.getPartyInfo().getFrom().getPartyId();
        result.addFromParty(partyFromId.getValue(), partyFromId.getType());


        final PartyId partyTo = userMessage.getPartyInfo().getTo().getPartyId();
        result.addToParty(partyTo.getValue(), partyTo.getType());


        if (userMessage.getMessageProperties() != null) {
            for (final MessageProperty property : userMessage.getMessageProperties()) {
                result.addMessageProperty(property.getName(), property.getValue(), property.getType());
            }
        }
        return result;
    }

    protected void addPayload(Submission result, PartInfo partInfo) {
        final Collection<Submission.TypedProperty> properties = new ArrayList<>();
        if (partInfo.getPartProperties() != null) {
            for (final PartProperty property : partInfo.getPartProperties()) {
                properties.add(new Submission.TypedProperty(property.getName(), property.getValue(), property.getType()));
            }
        }
        final Submission.Payload payload = new Submission.Payload(partInfo.getHref(), partInfo.getPayloadDatahandler(), properties, partInfo.isInBody(), null, null);
        if (partInfo.getFileName() != null) {
            final String fileNameWithoutPath = FilenameUtils.getName(partInfo.getFileName());
            properties.add(new Submission.TypedProperty("FileName", fileNameWithoutPath, null));

            payload.setFilepath(partInfo.getFileName());
        }
        result.addPayload(payload);
    }

    private String generateConversationId() {
        return this.messageIdGenerator.generateMessageId();
    }
}
