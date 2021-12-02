package eu.domibus.core.alerts.model.common;

import eu.domibus.logging.DomibusMessageCode;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * @author Thomas Dussart, Ion Perpegel
 * @since 4.0
 */
public enum EventType {

    MSG_STATUS_CHANGED(AlertType.MSG_STATUS_CHANGED, "message", MessageEvent.class),

    CERT_IMMINENT_EXPIRATION(AlertType.CERT_IMMINENT_EXPIRATION, "certificateImminentExpiration", CertificateEvent.class),
    CERT_EXPIRED(AlertType.CERT_EXPIRED, "certificateExpired", CertificateEvent.class),

    USER_LOGIN_FAILURE(AlertType.USER_LOGIN_FAILURE, "loginFailure", UserLoginFailedEventProperties.class, true),
    USER_ACCOUNT_DISABLED(AlertType.USER_ACCOUNT_DISABLED, "accountDisabled", UserAccountDisabledEventProperties.class, true),
    USER_ACCOUNT_ENABLED(AlertType.USER_ACCOUNT_ENABLED, "accountEnabled", UserAccountEnabledEventProperties.class, true),
    PLUGIN_USER_LOGIN_FAILURE(AlertType.PLUGIN_USER_LOGIN_FAILURE, "loginFailure", UserLoginFailedEventProperties.class),
    PLUGIN_USER_ACCOUNT_DISABLED(AlertType.PLUGIN_USER_ACCOUNT_DISABLED, "accountDisabled", UserAccountDisabledEventProperties.class),
    PLUGIN_USER_ACCOUNT_ENABLED(AlertType.PLUGIN_USER_ACCOUNT_ENABLED, "accountEnabled", UserAccountEnabledEventProperties.class),

    PASSWORD_EXPIRED(AlertType.PASSWORD_EXPIRED, "PASSWORD_EXPIRATION", PasswordExpirationEventProperties.class, true,
            DomibusMessageCode.SEC_PASSWORD_EXPIRED),
    PASSWORD_IMMINENT_EXPIRATION(AlertType.PASSWORD_IMMINENT_EXPIRATION, "PASSWORD_EXPIRATION", PasswordExpirationEventProperties.class, true,
            DomibusMessageCode.SEC_PASSWORD_IMMINENT_EXPIRATION),
    PLUGIN_PASSWORD_EXPIRED(AlertType.PLUGIN_PASSWORD_EXPIRED, "PASSWORD_EXPIRATION", PasswordExpirationEventProperties.class,
            DomibusMessageCode.SEC_PASSWORD_EXPIRED),
    PLUGIN_PASSWORD_IMMINENT_EXPIRATION(AlertType.PLUGIN_PASSWORD_IMMINENT_EXPIRATION, "PASSWORD_EXPIRATION", PasswordExpirationEventProperties.class,
            DomibusMessageCode.SEC_PASSWORD_IMMINENT_EXPIRATION),
    PLUGIN(AlertType.PLUGIN, "PLUGIN_EVENT", null,
            DomibusMessageCode.PLUGIN_DEFAULT),
    ARCHIVING_NOTIFICATION_FAILED(AlertType.ARCHIVING_NOTIFICATION_FAILED, "ARCHIVING_NOTIFICATION_FAILED", ArchivingEventProperties.class),
    ARCHIVING_MESSAGES_NON_FINAL(AlertType.ARCHIVING_MESSAGES_NON_FINAL, "ARCHIVING_MESSAGES_NON_FINAL", MessageEvent.class),
    ARCHIVING_START_DATE_STOPPED(AlertType.ARCHIVING_START_DATE_STOPPED, "ARCHIVING_START_DATE_STOPPED", null),
    PARTITION_EXPIRATION(AlertType.PARTITION_EXPIRATION, "PARTITION_EXPIRATION",  PartitionExpirationEvent.class);



    private AlertType defaultAlertType;
    private final String queueSelector;
    private Class<? extends Enum> propertiesEnumClass;
    private boolean userRelated;
    private final DomibusMessageCode securityMessageCode;

    EventType(AlertType defaultAlertType, String queueSelector, Class<? extends Enum> propertiesEnumClass,
              boolean isUserRelated, DomibusMessageCode securityMessageCode) {
        this.defaultAlertType = defaultAlertType;
        this.queueSelector = queueSelector;
        this.securityMessageCode = securityMessageCode;
        this.propertiesEnumClass = propertiesEnumClass;
        this.userRelated = isUserRelated;
    }

    EventType(AlertType defaultAlertType, String queueSelector, Class<? extends Enum> propertiesEnumClass,
              boolean isUserRelated) {
        this(defaultAlertType, queueSelector, propertiesEnumClass, isUserRelated, null);
    }

    EventType(AlertType defaultAlertType, String queueSelector, Class<? extends Enum> propertiesEnumClass,
              DomibusMessageCode securityMessageCode) {
        this(defaultAlertType, queueSelector, propertiesEnumClass, false, securityMessageCode);
    }

    EventType(AlertType defaultAlertType, String queueSelector, Class<? extends Enum> propertiesEnumClass) {
        this(defaultAlertType, queueSelector, propertiesEnumClass, false, null);
    }


    public AlertType geDefaultAlertType() {
        return this.defaultAlertType;
    }

    public List<String> getProperties() {
        ArrayList<String> list = new ArrayList<>();
        if (this.propertiesEnumClass != null) {
            EnumSet.allOf(this.propertiesEnumClass).forEach(x -> list.add(((Enum) x).name()));
        }
        return list;
    }

    public String getQueueSelector() {
        return this.queueSelector;
    }

    public DomibusMessageCode getSecurityMessageCode() {
        return this.securityMessageCode;
    }

    public boolean isUserRelated() {
        return this.userRelated;
    }
}
