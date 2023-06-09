package eu.domibus.core.alerts.configuration.global;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.validators.DomibusPropertyValidator;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.core.alerts.configuration.common.AlertConfigurationService;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * Manages the reading of common alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class CommonConfigurationManager {
    private static final Logger LOG = DomibusLoggerFactory.getLogger(CommonConfigurationManager.class);

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private ConfigurationLoader<CommonConfiguration> loader;

    public CommonConfiguration getConfiguration() {
        return loader.getConfiguration(this::readConfiguration);
    }

    public void reset() {
        loader.resetConfiguration();
    }

    protected CommonConfiguration readConfiguration() {
        final boolean emailActive = isSendEmailActive();
        final Integer alertLifeTimeInDays = domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_CLEANER_ALERT_LIFETIME);

        if (!emailActive) {
            return new CommonConfiguration(alertLifeTimeInDays);
        }

        return readDomainEmailConfiguration(alertLifeTimeInDays);
    }

    private Boolean isSendEmailActive() {
        return domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_MAIL_SENDING_ACTIVE);
    }

    protected CommonConfiguration readDomainEmailConfiguration(Integer alertLifeTimeInDays) {
        final String alertEmailSender = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_EMAIL);
        final String alertEmailReceiver = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_RECEIVER_EMAIL);

        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        if (StringUtils.isEmpty(alertEmailReceiver) || StringUtils.isEmpty(alertEmailSender)) {
            LOG.error("Alert module can not send email, mail sender property name:[{}]/value[{}] and receiver property name:[{}]/value[{}] are mandatory in the domain [{}].",
                    DOMIBUS_ALERT_SENDER_EMAIL, alertEmailSender, DOMIBUS_ALERT_RECEIVER_EMAIL, alertEmailReceiver, currentDomain);
            throw new IllegalArgumentException("Empty sender/receiver email address configured for the alert module.");
        }

        List<String> emailsToValidate = new ArrayList<>(Arrays.asList(alertEmailSender));
        emailsToValidate.addAll(Arrays.asList(alertEmailReceiver.split(";")));
        DomibusPropertyValidator validator = DomibusPropertyMetadata.Type.EMAIL.getValidator();
        for (String email : emailsToValidate) {
            if (!validator.isValid(email)) {
                LOG.error("Alert module can not send email, Invalid sender/receiver email address: [{}] configured in the domain: [{}].", email, currentDomain);
                throw new IllegalArgumentException("Invalid sender/receiver email address configured for the alert module: " + email);
            }
        }

        return new CommonConfiguration(alertLifeTimeInDays, alertEmailSender, alertEmailReceiver);
    }
}
