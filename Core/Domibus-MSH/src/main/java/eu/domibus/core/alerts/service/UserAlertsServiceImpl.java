package eu.domibus.core.alerts.service;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.user.UserBase;
import eu.domibus.api.user.UserEntityBase;
import eu.domibus.core.alerts.configuration.account.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.configuration.common.AlertConfigurationService;
import eu.domibus.core.alerts.configuration.generic.RepetitiveAlertConfiguration;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.EventProperties;
import eu.domibus.core.user.UserDaoBase;
import eu.domibus.core.user.UserLoginErrorReason;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@Service
public abstract class UserAlertsServiceImpl implements UserAlertsService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserAlertsServiceImpl.class);
    private static final String DEFAULT = "default ";

    protected AlertConfigurationService alertConfigurationService;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private EventService eventService;

    protected abstract String getMaximumDefaultPasswordAgeProperty();

    protected abstract String getMaximumPasswordAgeProperty();

    protected abstract EventType getEventTypeForPasswordImminentExpiration();

    protected abstract EventType getEventTypeForPasswordExpired();

    protected abstract UserDaoBase<UserEntityBase> getUserDao();

    protected abstract UserEntityBase.Type getUserType();

    protected abstract AccountDisabledModuleConfiguration getAccountDisabledConfiguration();

    @Override
    public void triggerLoginEvents(String userName, UserLoginErrorReason userLoginErrorReason) {
        EventType loginFailEventType = getUserType() == UserEntityBase.Type.CONSOLE ? EventType.USER_LOGIN_FAILURE : EventType.PLUGIN_USER_LOGIN_FAILURE;
        switch (userLoginErrorReason) {
            case BAD_CREDENTIALS:
                eventService.enqueueEvent(loginFailEventType, getEventId(userName), new EventProperties(userName, getUserType().getName(), new Date(), false));
                break;
            case INACTIVE:
            case SUSPENDED:
                final AccountDisabledModuleConfiguration accountDisabledConfiguration = getAccountDisabledConfiguration();
                if (BooleanUtils.isTrue(accountDisabledConfiguration.shouldTriggerAccountDisabledAtEachLogin())) {
                    EventType eventType = getUserType() == UserEntityBase.Type.CONSOLE ? EventType.USER_ACCOUNT_DISABLED : EventType.PLUGIN_USER_ACCOUNT_DISABLED;
                    eventService.enqueueEvent(eventType, getEventId(userName), new EventProperties(getUserType().getName(), userName, new Date(), true));
                } else {
                    eventService.enqueueEvent(loginFailEventType, getEventId(userName), new EventProperties(userName, getUserType().getName(), new Date(), true));
                }
                break;
            case UNKNOWN:
                break;
        }
    }

    protected String getEventId(String userName) {
        return getUserType().getCode() + "/" + userName;
    }

    @Override
    public void triggerDisabledEvent(UserBase user) {
        LOG.debug("Sending account disabled event for user:[{}]", user.getUserName());
        EventType eventType = getUserType() == UserEntityBase.Type.CONSOLE ? EventType.USER_ACCOUNT_DISABLED : EventType.PLUGIN_USER_ACCOUNT_DISABLED;
        eventService.enqueueEvent(eventType, getEventId(user.getUserName()), new EventProperties(getUserType().getName(), user.getUserName(), new Date(), true));
    }

    @Override
    public void triggerEnabledEvent(UserBase user) {
        LOG.debug("Sending account enabled event for user:[{}]", user.getUserName());
        EventType eventType = getUserType() == UserEntityBase.Type.CONSOLE ? EventType.USER_ACCOUNT_ENABLED : EventType.PLUGIN_USER_ACCOUNT_ENABLED;
        eventService.enqueueEvent(eventType, getEventId(user.getUserName()), new EventProperties(getUserType().getName(), user.getUserName(), new Date(), true));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void triggerPasswordExpirationEvents() {
        try {
            triggerExpiredEvents(true);
            triggerExpiredEvents(false);
        } catch (Exception ex) {
            LOG.error("Send password expired alerts failed ", ex);
        }
        try {
            triggerImminentExpirationEvents(true);
            triggerImminentExpirationEvents(false);
        } catch (Exception ex) {
            LOG.error("Send imminent expiration alerts failed ", ex);
        }
    }

    protected void triggerImminentExpirationEvents(boolean usersWithDefaultPassword) {
        triggerExpirationEvents(usersWithDefaultPassword, true, getEventTypeForPasswordImminentExpiration());
    }

    protected void triggerExpiredEvents(boolean usersWithDefaultPassword) {
        triggerExpirationEvents(usersWithDefaultPassword, false, getEventTypeForPasswordExpired());
    }

    private void triggerExpirationEvents(boolean usersWithDefaultPassword, boolean imminent, EventType eventType) {
        RepetitiveAlertConfiguration alertConfiguration = (RepetitiveAlertConfiguration) alertConfigurationService.getConfiguration(eventType.geDefaultAlertType());
        if (!alertConfiguration.isActive()) {
            LOG.debug("[{}] alert module is not ebabled.", eventType);
            return;
        }

        String expirationProperty = usersWithDefaultPassword ? getMaximumDefaultPasswordAgeProperty() : getMaximumPasswordAgeProperty();
        int maxPasswordAgeInDays = domibusPropertyProvider.getIntegerProperty(expirationProperty);
        if (maxPasswordAgeInDays == 0) {
            // if password expiration is disabled, do not trigger the corresponding alerts, regardless of alert enabled/disabled status
            LOG.debug("Property maximum password in days is 0 so password expiration is disabled.");
            return;
        }

        final Integer duration = alertConfiguration.getDelay();
        LocalDate from = imminent ? LocalDate.now().minusDays(maxPasswordAgeInDays)
                : LocalDate.now().minusDays(maxPasswordAgeInDays).minusDays(duration);

        LocalDate to = from.plusDays(duration);
        LOG.debug("[{}]: Searching for {} users with password change date between [{}]->[{}]", eventType, (usersWithDefaultPassword ? DEFAULT : StringUtils.EMPTY), from, to);

        List<UserEntityBase> eligibleUsers = getUserDao().findWithPasswordChangedBetween(from, to, usersWithDefaultPassword);
        LOG.debug("[{}]: Found [{}] eligible {} users", eventType, eligibleUsers.size(), (usersWithDefaultPassword ? DEFAULT : StringUtils.EMPTY));

        eligibleUsers.forEach(user -> eventService.enqueuePasswordExpirationEvent(eventType, user, maxPasswordAgeInDays));
    }

}
