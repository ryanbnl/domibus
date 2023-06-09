package eu.domibus.plugin.fs;

import eu.domibus.ext.services.FileUtilExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.ProcessingType;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.fs.ebms3.*;
import eu.domibus.plugin.fs.exception.FSPayloadException;
import eu.domibus.plugin.fs.exception.FSPluginException;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.activation.DataHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.domibus.logging.DomibusMessageCode.MANDATORY_MESSAGE_HEADER_METADATA_MISSING;

/**
 * This class is responsible for transformations from {@link FSMessage} to
 * {@link eu.domibus.plugin.Submission} and vice versa
 *
 * @author @author FERNANDES Henrique, GONCALVES Bruno
 * @author Cosmin Baciu
 */
@Component
public class FSMessageTransformer implements MessageRetrievalTransformer<FSMessage>, MessageSubmissionTransformer<FSMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSMessageTransformer.class);

    private static final String PAYLOAD_PROPERTY_MIME_TYPE = "MimeType";

    protected final ObjectFactory objectFactory = new ObjectFactory();

    protected final FSMimeTypeHelper fsMimeTypeHelper;

    protected final FileUtilExtService fileUtilExtService;

    public FSMessageTransformer(FSMimeTypeHelper fsMimeTypeHelper,
                                FileUtilExtService fileUtilExtService) {
        this.fsMimeTypeHelper = fsMimeTypeHelper;
        this.fileUtilExtService = fileUtilExtService;
    }

    /**
     * Transforms {@link eu.domibus.plugin.Submission} to {@link FSMessage}
     *
     * @param submission the message to be transformed
     * @param messageOut output target
     * @return result of the transformation as {@link FSMessage}
     */
    @Override
    public FSMessage transformFromSubmission(final Submission submission, final FSMessage messageOut) {
        UserMessage metadata = objectFactory.createUserMessage();
        metadata.setMessageInfo(getMessageInfo(submission));
        metadata.setMpc(submission.getMpc());
        ProcessingType processingType = submission.getProcessingType();
        if (processingType == null) {
            processingType = ProcessingType.PUSH;
            LOG.debug("Submission processing type is null, setting default processing type:[{}]", processingType);
        }
        metadata.setProcessingType(eu.domibus.plugin.fs.ebms3.ProcessingType.valueOf(processingType.name()));
        metadata.setPartyInfo(getPartyInfoFromSubmission(submission));
        metadata.setCollaborationInfo(getCollaborationInfoFromSubmission(submission));
        metadata.setMessageProperties(getMessagePropertiesFromSubmission(submission));
        metadata.setPayloadInfo(getPayloadInfoFromSubmission(submission));
        Map<String, FSPayload> dataHandlers = getPayloadsFromSubmission(submission);
        return new FSMessage(dataHandlers, metadata);
    }

    protected MessageInfo getMessageInfo(Submission submission) {
        MessageInfo result = new MessageInfo();
        result.setMessageId(submission.getMessageId());
        result.setRefToMessageId(submission.getRefToMessageId());
        return result;
    }

    /**
     * Transforms {@link FSMessage} to {@link eu.domibus.plugin.Submission}
     *
     * @param messageIn the message ({@link FSMessage}) to be tranformed
     * @return the result of the transformation as
     * {@link eu.domibus.plugin.Submission}
     */
    @Override
    public Submission transformToSubmission(final FSMessage messageIn) {
        UserMessage metadata = messageIn.getMetadata();
        Submission submission = new Submission();
        submission.setMpc(metadata.getMpc());
        submission.setProcessingType(ProcessingType.valueOf(metadata.getProcessingType().value()));
        setPartyInfoToSubmission(submission, metadata.getPartyInfo());
        setCollaborationInfoToSubmission(submission, metadata.getCollaborationInfo());
        setMessagePropertiesToSubmission(submission, metadata.getMessageProperties());
        try {
            setPayloadToSubmission(submission, messageIn.getPayloads(), metadata);
        } catch (FSPayloadException ex) {
            throw new FSPluginException("Could not set payload to Submission", ex);
        }
        return submission;
    }

    protected void setPayloadToSubmission(Submission submission, final Map<String, FSPayload> dataHandlers, UserMessage metadata) {
        for (Map.Entry<String, FSPayload> entry : dataHandlers.entrySet()) {
            String contentId = entry.getKey();
            final FSPayload fsPayload = entry.getValue();
            DataHandler dataHandler = fsPayload.getDataHandler();
            final String fileName = fsPayload.getFileName();
            String mimeType = fsMimeTypeHelper.getMimeType(dataHandler.getName());

            /* PartInfo defined in the metadata file take precedence to the plugin properties */
            String metadataContentId = extractContentIdFromMetadata(metadata);
            if (StringUtils.isNotBlank(metadataContentId)) {
                LOG.debug("Setting contentId from metadata file: [{}]", metadataContentId);
                contentId = metadataContentId;
            }
            String metadataMimeType = extractMimeTypeFromMetadata(metadata);
            if (StringUtils.isNotBlank(metadataMimeType)) {
                LOG.debug("Setting mimeType from metadata file: [{}]", metadataMimeType);
                mimeType = metadataMimeType;
            }
            if (StringUtils.isEmpty(mimeType)) {
                throw new FSPayloadException("Could not detect mime type for " + dataHandler.getName());
            }
            ArrayList<Submission.TypedProperty> payloadProperties = new ArrayList<>();
            payloadProperties.add(new Submission.TypedProperty(PAYLOAD_PROPERTY_MIME_TYPE, mimeType));
            payloadProperties.add(new Submission.TypedProperty(MessageConstants.PAYLOAD_PROPERTY_FILE_NAME, fileName));
            List<Property> additionalPropertyList = extractAdditionalPropertyListFromMetadata(metadata);
            if (additionalPropertyList != null) {
                for (Property property : additionalPropertyList) {
                    LOG.debug("Adding property name:[{}] type:[{}] value:[{}] to payload [{}]", property.getName(),
                            property.getType(), property.getValue(), contentId);
                    payloadProperties.add(new Submission.TypedProperty(property.getName(), property.getValue(), property.getType()));
                }
            }
            final Submission.Payload payload = new Submission.Payload(contentId, dataHandler, payloadProperties, false, null, null);
            payload.setPayloadSize(fsPayload.getFileSize());
            payload.setFilepath(fsPayload.getFilePath());
            submission.addPayload(payload);
        }
    }


    protected PartInfo extractPartInfoFromMetadata(UserMessage metadata) {
        if (metadata.getPayloadInfo() == null) {
            return null;
        }
        if (CollectionUtils.isEmpty(metadata.getPayloadInfo().getPartInfo())) {
            return null;
        }

        if (metadata.getPayloadInfo().getPartInfo().size() > 1) {
            throw new FSPluginException("FS plugin can only handle one payload per message. Multiple PartInfo found.");
        }

        /* FS plugin sends one payload at a time */
        return metadata.getPayloadInfo().getPartInfo().get(0);
    }

    protected String extractContentIdFromMetadata(UserMessage metadata) {
        PartInfo partInfo = extractPartInfoFromMetadata(metadata);
        if (partInfo != null) {
            return partInfo.getHref();
        }

        return null;
    }

    protected PartProperties extractPartPropertiesFromMetadata(UserMessage metadata) {
        PartInfo partInfo = extractPartInfoFromMetadata(metadata);
        if (partInfo != null) {
            return partInfo.getPartProperties();
        }

        return null;
    }

    protected String extractMimeTypeFromMetadata(UserMessage metadata) {
        PartProperties partProperties = extractPartPropertiesFromMetadata(metadata);
        if (partProperties == null) {
            return null;
        }

        for (Property property : partProperties.getProperty()) {
            if (PAYLOAD_PROPERTY_MIME_TYPE.equals(property.getName())) {
                return property.getValue();
            }
        }
        return null;
    }

    /*
     * Additional PartProperties are accepted for each payload.
     * This method extracts all properties that are not MimeType and FileName.
     */
    protected List<Property> extractAdditionalPropertyListFromMetadata(UserMessage metadata) {
        PartProperties partProperties = extractPartPropertiesFromMetadata(metadata);
        if (partProperties == null) {
            return null;
        }
        List<Property> propertyList = new ArrayList<>();

        for (Property property : partProperties.getProperty()) {
            if (!PAYLOAD_PROPERTY_MIME_TYPE.equals(property.getName()) &&
                    !MessageConstants.PAYLOAD_PROPERTY_FILE_NAME.equals(property.getName())) {
                propertyList.add(property);
            }
        }
        return propertyList;
    }

    protected Map<String, FSPayload> getPayloadsFromSubmission(Submission submission) {
        Map<String, FSPayload> result = new HashMap<>(submission.getPayloads().size());
        for (final Submission.Payload payload : submission.getPayloads()) {

            //mime type
            String mimeType = extractPayloadProperty(payload, PAYLOAD_PROPERTY_MIME_TYPE);
            if (mimeType == null) {
                mimeType = payload.getPayloadDatahandler().getContentType();
            }

            //file name
            String fileName = fileUtilExtService.sanitizeFileName(extractPayloadProperty(payload, MessageConstants.PAYLOAD_PROPERTY_FILE_NAME));
            LOG.debug("{} property found=[{}]", MessageConstants.PAYLOAD_PROPERTY_FILE_NAME, fileName);

            FSPayload fsPayload = new FSPayload(mimeType, fileName, payload.getPayloadDatahandler());
            if (StringUtils.isNotEmpty(payload.getFilepath())) {
                fsPayload.setFilePath(payload.getFilepath());
                try (FileObject fileObject = VFS.getManager().resolveFile(payload.getFilepath())){
                    fsPayload.setFileSize(fileObject.getContent().getSize());
                } catch (FileSystemException e) {
                    LOG.warn("File [{}] not found to get the size", payload.getFilepath());
                }
            }
            result.put(payload.getContentId(), fsPayload);
        }
        return result;
    }

    protected String extractPayloadProperty(Submission.Payload payload, String propertyName) {
        String propertyValue = null;
        for (Submission.TypedProperty payloadProperty : payload.getPayloadProperties()) {
            if (payloadProperty.getKey().equals(propertyName)) {
                propertyValue = payloadProperty.getValue();
                break;
            }
        }
        return propertyValue;
    }

    private void setMessagePropertiesToSubmission(Submission submission, MessageProperties messageProperties) {
        if (messageProperties == null) {
            return;
        }

        for (Property messageProperty : messageProperties.getProperty()) {
            String name = messageProperty.getName();
            String value = messageProperty.getValue();
            String type = messageProperty.getType();

            if (type != null) {
                submission.addMessageProperty(name, value, type);
            } else {
                submission.addMessageProperty(name, value);
            }
        }
    }

    protected PayloadInfo getPayloadInfoFromSubmission(Submission submission) {
        final PayloadInfo payloadInfo = new PayloadInfo();

        for (Submission.Payload submissionPayload : submission.getPayloads()) {
            final PartInfo partInfo = new PartInfo();
            partInfo.setHref(submissionPayload.getContentId());
            final PartProperties partProperties = new PartProperties();
            for (final Submission.TypedProperty payloadProperty : submissionPayload.getPayloadProperties()) {
                final Property property = new Property();
                property.setName(payloadProperty.getKey());
                property.setValue(payloadProperty.getValue());
                property.setType(payloadProperty.getType());
                LOG.debug("Adding property name:[{}] type:[{}] value:[{}] to payload [{}]", property.getName(),
                        property.getType(), property.getValue(), submissionPayload.getContentId());
                partProperties.getProperty().add(property);
            }

            partInfo.setPartProperties(partProperties);
            payloadInfo.getPartInfo().add(partInfo);
        }
        return payloadInfo;
    }

    protected MessageProperties getMessagePropertiesFromSubmission(Submission submission) {
        MessageProperties messageProperties = objectFactory.createMessageProperties();

        for (Submission.TypedProperty typedProperty : submission.getMessageProperties()) {
            Property messageProperty = objectFactory.createProperty();
            messageProperty.setType(typedProperty.getType());
            messageProperty.setName(typedProperty.getKey());
            messageProperty.setValue(typedProperty.getValue());
            messageProperties.getProperty().add(messageProperty);
        }
        return messageProperties;
    }

    protected void setCollaborationInfoToSubmission(Submission submission, CollaborationInfo collaborationInfo) {
        AgreementRef agreementRef = collaborationInfo.getAgreementRef();
        Service service = collaborationInfo.getService();

        if (agreementRef != null) {
            submission.setAgreementRef(agreementRef.getValue());
            submission.setAgreementRefType(agreementRef.getType());
        }
        if (service != null) {
            submission.setService(service.getValue());
            submission.setServiceType(service.getType());
        }
        submission.setAction(collaborationInfo.getAction());
        if (collaborationInfo.getConversationId() != null) {
            submission.setConversationId(collaborationInfo.getConversationId());
        }
    }

    protected CollaborationInfo getCollaborationInfoFromSubmission(Submission submission) {
        AgreementRef agreementRef = objectFactory.createAgreementRef();
        agreementRef.setType(submission.getAgreementRefType());
        agreementRef.setValue(submission.getAgreementRef());

        Service service = objectFactory.createService();
        service.setType(submission.getServiceType());
        service.setValue(submission.getService());

        CollaborationInfo collaborationInfo = objectFactory.createCollaborationInfo();
        collaborationInfo.setAgreementRef(agreementRef);
        collaborationInfo.setService(service);
        collaborationInfo.setAction(submission.getAction());
        collaborationInfo.setConversationId(submission.getConversationId());

        return collaborationInfo;
    }

    protected void setPartyInfoToSubmission(Submission submission, PartyInfo partyInfo) {
        From from = partyInfo.getFrom();
        To to = partyInfo.getTo();

        submission.addFromParty(from.getPartyId().getValue(), from.getPartyId().getType());
        submission.setFromRole(from.getRole());
        if (to != null) {
            submission.addToParty(to.getPartyId().getValue(), to.getPartyId().getType());
            submission.setToRole(to.getRole());
        }
    }

    protected PartyInfo getPartyInfoFromSubmission(Submission submission) {
        // From
        Submission.Party fromParty = submission.getFromParties().iterator().next();
        String fromRole = submission.getFromRole();

        PartyId fromPartyId = objectFactory.createPartyId();
        validateFromParty(fromParty, fromRole);

        fromPartyId.setType(fromParty.getPartyIdType());
        fromPartyId.setValue(fromParty.getPartyId());

        From from = objectFactory.createFrom();
        from.setPartyId(fromPartyId);
        from.setRole(fromRole);

        // To
        Submission.Party toParty = submission.getToParties().iterator().next();
        String toRole = submission.getToRole();

        PartyId toPartyId = objectFactory.createPartyId();
        toPartyId.setType(toParty.getPartyIdType());
        toPartyId.setValue(toParty.getPartyId());

        To to = objectFactory.createTo();
        to.setPartyId(toPartyId);
        to.setRole(toRole);

        // PartyInfo
        PartyInfo partyInfo = objectFactory.createPartyInfo();
        partyInfo.setFrom(from);
        partyInfo.setTo(to);

        return partyInfo;
    }

    protected void validateFromParty(Submission.Party fromParty, String fromRole) throws FSPluginException {
        if (fromParty == null) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "PartyInfo/From");
            throw new FSPluginException("Mandatory field PartyInfo/From is not provided.");
        }

        if (StringUtils.isBlank(fromParty.getPartyId())) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "PartyInfo/From/PartyId");
            throw new FSPluginException("Mandatory field From PartyId is not provided.");
        }
        validateFromRole(fromRole);
    }


    protected void validateFromRole(String fromRole) throws FSPluginException {
        if (StringUtils.isBlank(fromRole)) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "PartyInfo/From/Role");
            throw new FSPluginException("Mandatory field From Role is not provided.");
        }
    }
}
