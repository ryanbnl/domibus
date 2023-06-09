package eu.domibus.core.alerts.service;

import eu.domibus.api.user.UserEntityBase;
import eu.domibus.core.alerts.configuration.common.AlertConfigurationService;
import eu.domibus.core.alerts.configuration.common.AlertModuleConfiguration;
import eu.domibus.core.alerts.configuration.account.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.configuration.generic.RepetitiveAlertConfiguration;
import eu.domibus.core.user.UserDaoBase;
import eu.domibus.core.user.ui.UserDao;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PASSWORD_POLICY_DEFAULT_PASSWORD_EXPIRATION;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PASSWORD_POLICY_EXPIRATION;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@Service
public class ConsoleUserAlertsServiceImpl extends UserAlertsServiceImpl {

    public static final String MAXIMUM_PASSWORD_AGE = DOMIBUS_PASSWORD_POLICY_EXPIRATION; //NOSONAR
    public static final String MAXIMUM_DEFAULT_PASSWORD_AGE = DOMIBUS_PASSWORD_POLICY_DEFAULT_PASSWORD_EXPIRATION; //NOSONAR

    protected final UserDao userDao;

    public ConsoleUserAlertsServiceImpl(UserDao userDao, AlertConfigurationService alertConfigurationService) {
        this.userDao = userDao;
        this.alertConfigurationService = alertConfigurationService;
    }

    @Override
    protected String getMaximumDefaultPasswordAgeProperty() {
        return MAXIMUM_DEFAULT_PASSWORD_AGE;
    }

    @Override
    protected String getMaximumPasswordAgeProperty() {
        return MAXIMUM_PASSWORD_AGE;
    }

    @Override
    protected EventType getEventTypeForPasswordImminentExpiration() {
        return EventType.PASSWORD_IMMINENT_EXPIRATION;
    }

    @Override
    protected EventType getEventTypeForPasswordExpired() {
        return EventType.PASSWORD_EXPIRED;
    }

    @Override
    protected UserDaoBase getUserDao() {
        return userDao;
    }

    @Override
    protected UserEntityBase.Type getUserType() {
        return UserEntityBase.Type.CONSOLE;
    }

    @Override
    protected AccountDisabledModuleConfiguration getAccountDisabledConfiguration() {
        return (AccountDisabledModuleConfiguration) alertConfigurationService.getConfiguration(AlertType.USER_ACCOUNT_DISABLED);
    }

}
