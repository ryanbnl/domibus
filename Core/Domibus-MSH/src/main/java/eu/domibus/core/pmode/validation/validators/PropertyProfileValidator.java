package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.model.MessageProperty;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Property;
import eu.domibus.common.model.configuration.PropertySet;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.message.UserMessageServiceHelper;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Koch, Stefan Mueller
 * @version 3.0
 * @since 3.0
 */

@Service
public class PropertyProfileValidator {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PropertyProfileValidator.class);

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    UserMessageServiceHelper userMessageDefaultServiceHelper;

    @Autowired
    DomibusConfigurationService domibusConfigurationService;

    public void validate(final UserMessage userMessage, final String pmodeKey) throws EbMS3Exception {
        final LegConfiguration legConfiguration = this.pModeProvider.getLegConfiguration(pmodeKey);
        final PropertySet propSet = legConfiguration.getPropertySet();
        if (propSet == null || CollectionUtils.isEmpty(propSet.getProperties())) {
            LOG.businessInfo(DomibusMessageCode.BUS_PROPERTY_PROFILE_VALIDATION_SKIP, legConfiguration.getName());
            // no profile means everything is valid
            return;
        }

        Set<MessageProperty> messageProperties = userMessage.getMessageProperties();
        final List<Property> modifiablePropertyList = new ArrayList<>(propSet.getProperties());
        if (messageProperties != null) {
            checkDuplicateMessageProperties(modifiablePropertyList, messageProperties);

            for (final eu.domibus.api.model.Property property : messageProperties) {
                Property profiled = null;
                for (final Property profiledProperty : modifiablePropertyList) {
                    if (profiledProperty.getKey().equalsIgnoreCase(property.getName())) {
                        profiled = profiledProperty;
                        break;
                    }
                }
                modifiablePropertyList.remove(profiled);
                if (profiled == null) {
                    LOG.businessError(DomibusMessageCode.BUS_PROPERTY_MISSING, property.getName());
                    throw EbMS3ExceptionBuilder.getInstance()
                            .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                            .message("Property profiling for this exchange does not include a property named [" + property.getName() + "]")
                            .refToMessageId(userMessage.getMessageId())
                            .build();
                }

                switch (profiled.getDatatype().toLowerCase()) {
                    case "string":
                        break;
                    case "int":
                        try {
                            Integer.parseInt(property.getValue()); //NOSONAR: Validation is done via exception
                            break;
                        } catch (final NumberFormatException e) {
                            throw EbMS3ExceptionBuilder.getInstance()
                                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                                    .message("Property profiling for this exchange requires a INTEGER datatype for property named: " + property.getName() + ", but got " + property.getValue())
                                    .refToMessageId(userMessage.getMessageId())
                                    .build();
                        }
                    case "boolean":
                        if (property.getValue().equalsIgnoreCase("false") || property.getValue().equalsIgnoreCase("true")) {
                            break;
                        }
                        throw EbMS3ExceptionBuilder.getInstance()
                                .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                                .message("Property profiling for this exchange requires a BOOLEAN datatype for property named: " + property.getName() + ", but got " + property.getValue())
                                .refToMessageId(userMessage.getMessageId())
                                .build();
                    default:
                        PropertyProfileValidator.LOG.warn("Validation for Datatype " + profiled.getDatatype() + " not possible. This type is not known by the validator. The value will be accepted unchecked");
                }


            }
        }

        for (final Property property : modifiablePropertyList) {
            if (property.isRequired()) {
                LOG.businessError(DomibusMessageCode.BUS_PROPERTY_MISSING, property.getName());
                throw EbMS3ExceptionBuilder.getInstance()
                        .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                        .message("Required property missing [" + property.getName() + "]")
                        .refToMessageId(userMessage.getMessageId())
                        .build();
            }
        }

        LOG.businessInfo(DomibusMessageCode.BUS_PROPERTY_PROFILE_VALIDATION, propSet.getName());
    }

    protected void checkDuplicateMessageProperties(List<Property> modifiablePropertyList, Set<MessageProperty> messageProperties) throws EbMS3Exception {
        for (final Property profiledProperty : modifiablePropertyList) {
            int duplicateMessagePropertiesCount = (int) messageProperties.stream().filter(string -> string.getName().equalsIgnoreCase(profiledProperty.getKey())).count();
            if (duplicateMessagePropertiesCount > 1) {
                LOG.businessError(DomibusMessageCode.BUS_PROPERTY_DUPLICATE, profiledProperty.getKey());
                throw EbMS3ExceptionBuilder.getInstance()
                        .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0052)
                        .message("Duplicate Message property found for property name [" + profiledProperty.getKey() + "]")
                        .build();
            }
        }
    }
}
