-- *****************************************************************************************************
-- Domibus 4.2 to 5.0 data time migration to UTC procedures
--
-- Note: please follow the steps below to ensure the procedure run successfully
--  1. Identify your current named time zone such as 'Europe/Brussels', 'US/Eastern', 'MET' or 'UTC' (e.g. issue SELECT @@GLOBAL.time_zone, @@SESSION.time_zone;)
--  2. Populate the MySQL time zone tables if not already done: https://dev.mysql.com/doc/refman/8.0/en/time-zone-support.html#time-zone-installation
--  3. Invoke the UTC conversion procedure below passing the named time zone identified at step 1 above
--
-- Parameters to be provided:
--  TIMEZONE: the timezone ID in which the date time values have been previously saved (e.g. 'Europe/Brussels')
-- *****************************************************************************************************
DELIMITER //

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_utc_conversion
//

CREATE PROCEDURE MIGRATE_42_TO_50_utc_conversion(IN TIMEZONE VARCHAR(255))
BEGIN
    UPDATE TB_ACTION_AUDIT
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_ACTION_AUDIT
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_ACTION_AUDIT
    SET REVISION_DATE = CONVERT_TZ(REVISION_DATE, TIMEZONE, 'UTC')
    WHERE REVISION_DATE IS NOT NULL;

    UPDATE TB_ALERT
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_ALERT
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_ALERT
    SET NEXT_ATTEMPT = CONVERT_TZ(NEXT_ATTEMPT, TIMEZONE, 'UTC')
    WHERE NEXT_ATTEMPT IS NOT NULL;

    UPDATE TB_ALERT
    SET PROCESSED_TIME = CONVERT_TZ(PROCESSED_TIME, TIMEZONE, 'UTC')
    WHERE PROCESSED_TIME IS NOT NULL;

    UPDATE TB_ALERT
    SET REPORTING_TIME = CONVERT_TZ(REPORTING_TIME, TIMEZONE, 'UTC')
    WHERE REPORTING_TIME IS NOT NULL;

    UPDATE TB_ALERT
    SET REPORTING_TIME_FAILURE = CONVERT_TZ(REPORTING_TIME_FAILURE, TIMEZONE, 'UTC')
    WHERE REPORTING_TIME_FAILURE IS NOT NULL;

    UPDATE TB_AUTHENTICATION_ENTRY
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_AUTHENTICATION_ENTRY
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_AUTHENTICATION_ENTRY
    SET PASSWORD_CHANGE_DATE = CONVERT_TZ(PASSWORD_CHANGE_DATE, TIMEZONE, 'UTC')
    WHERE PASSWORD_CHANGE_DATE IS NOT NULL;

    UPDATE TB_AUTHENTICATION_ENTRY
    SET SUSPENSION_DATE = CONVERT_TZ(SUSPENSION_DATE, TIMEZONE, 'UTC')
    WHERE SUSPENSION_DATE IS NOT NULL;

    UPDATE TB_AUTHENTICATION_ENTRY_AUD
    SET PASSWORD_CHANGE_DATE = CONVERT_TZ(PASSWORD_CHANGE_DATE, TIMEZONE, 'UTC')
    WHERE PASSWORD_CHANGE_DATE IS NOT NULL;

    UPDATE TB_BACKEND_FILTER
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_BACKEND_FILTER
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_CERTIFICATE
    SET ALERT_EXP_NOTIFICATION_DATE = CONVERT_TZ(ALERT_EXP_NOTIFICATION_DATE, TIMEZONE, 'UTC')
    WHERE ALERT_EXP_NOTIFICATION_DATE IS NOT NULL;

    UPDATE TB_CERTIFICATE
    SET ALERT_IMM_NOTIFICATION_DATE = CONVERT_TZ(ALERT_IMM_NOTIFICATION_DATE, TIMEZONE, 'UTC')
    WHERE ALERT_IMM_NOTIFICATION_DATE IS NOT NULL;

    UPDATE TB_CERTIFICATE
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_CERTIFICATE
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_CERTIFICATE
    SET REVOKE_NOTIFICATION_DATE = CONVERT_TZ(REVOKE_NOTIFICATION_DATE, TIMEZONE, 'UTC')
    WHERE REVOKE_NOTIFICATION_DATE IS NOT NULL;

    UPDATE TB_CERTIFICATE
    SET NOT_VALID_BEFORE_DATE = CONVERT_TZ(NOT_VALID_BEFORE_DATE, TIMEZONE, 'UTC')
    WHERE NOT_VALID_BEFORE_DATE IS NOT NULL;

    UPDATE TB_CERTIFICATE
    SET NOT_VALID_AFTER_DATE = CONVERT_TZ(NOT_VALID_AFTER_DATE, TIMEZONE, 'UTC')
    WHERE NOT_VALID_AFTER_DATE IS NOT NULL;

    UPDATE TB_CERTIFICATE_AUD
    SET ALERT_EXP_NOTIFICATION_DATE = CONVERT_TZ(ALERT_EXP_NOTIFICATION_DATE, TIMEZONE, 'UTC')
    WHERE ALERT_EXP_NOTIFICATION_DATE IS NOT NULL;

    UPDATE TB_CERTIFICATE_AUD
    SET ALERT_IMM_NOTIFICATION_DATE = CONVERT_TZ(ALERT_IMM_NOTIFICATION_DATE, TIMEZONE, 'UTC')
    WHERE ALERT_IMM_NOTIFICATION_DATE IS NOT NULL;

    UPDATE TB_CERTIFICATE_AUD
    SET REVOKE_NOTIFICATION_DATE = CONVERT_TZ(REVOKE_NOTIFICATION_DATE, TIMEZONE, 'UTC')
    WHERE REVOKE_NOTIFICATION_DATE IS NOT NULL;

    UPDATE TB_CERTIFICATE_AUD
    SET NOT_VALID_BEFORE_DATE = CONVERT_TZ(NOT_VALID_BEFORE_DATE, TIMEZONE, 'UTC')
    WHERE NOT_VALID_BEFORE_DATE IS NOT NULL;

    UPDATE TB_CERTIFICATE_AUD
    SET NOT_VALID_AFTER_DATE = CONVERT_TZ(NOT_VALID_AFTER_DATE, TIMEZONE, 'UTC')
    WHERE NOT_VALID_AFTER_DATE IS NOT NULL;

    UPDATE TB_COMMAND
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_COMMAND
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_COMMAND_PROPERTY
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_COMMAND_PROPERTY
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_ENCRYPTION_KEY
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_ENCRYPTION_KEY
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_ERROR
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_ERROR
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_ERROR_LOG
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_ERROR_LOG
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_ERROR_LOG
    SET NOTIFIED = CONVERT_TZ(NOTIFIED, TIMEZONE, 'UTC')
    WHERE NOTIFIED IS NOT NULL;

    UPDATE TB_ERROR_LOG
    SET TIME_STAMP = CONVERT_TZ(TIME_STAMP, TIMEZONE, 'UTC')
    WHERE TIME_STAMP IS NOT NULL;

    UPDATE TB_EVENT
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_EVENT
    SET LAST_ALERT_DATE = CONVERT_TZ(LAST_ALERT_DATE, TIMEZONE, 'UTC')
    WHERE LAST_ALERT_DATE IS NOT NULL;

    UPDATE TB_EVENT
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_EVENT
    SET REPORTING_TIME = CONVERT_TZ(REPORTING_TIME, TIMEZONE, 'UTC')
    WHERE REPORTING_TIME IS NOT NULL;

    UPDATE TB_EVENT_ALERT
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_EVENT_ALERT
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_EVENT_PROPERTY
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_EVENT_PROPERTY
    SET DATE_VALUE = CONVERT_TZ(DATE_VALUE, TIMEZONE, 'UTC')
    WHERE DATE_VALUE IS NOT NULL;

    UPDATE TB_EVENT_PROPERTY
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_MESSAGE_ACKNW
    SET ACKNOWLEDGE_DATE = CONVERT_TZ(ACKNOWLEDGE_DATE, TIMEZONE, 'UTC')
    WHERE ACKNOWLEDGE_DATE IS NOT NULL;

    UPDATE TB_MESSAGE_ACKNW
    SET CREATE_DATE = CONVERT_TZ(CREATE_DATE, TIMEZONE, 'UTC')
    WHERE CREATE_DATE IS NOT NULL;

    UPDATE TB_MESSAGE_ACKNW
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_MESSAGE_ACKNW
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_MESSAGE_ACKNW_PROP
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_MESSAGE_ACKNW_PROP
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PROPERTY
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PROPERTY
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_MESSAGING
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_MESSAGING
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_MESSAGING_LOCK
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_MESSAGING_LOCK
    SET MESSAGE_RECEIVED = CONVERT_TZ(MESSAGE_RECEIVED, TIMEZONE, 'UTC')
    WHERE MESSAGE_RECEIVED IS NOT NULL;

    UPDATE TB_MESSAGING_LOCK
    SET MESSAGE_STALED = CONVERT_TZ(MESSAGE_STALED, TIMEZONE, 'UTC')
    WHERE MESSAGE_STALED IS NOT NULL;

    UPDATE TB_MESSAGING_LOCK
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_MESSAGING_LOCK
    SET NEXT_ATTEMPT = CONVERT_TZ(NEXT_ATTEMPT, TIMEZONE, 'UTC')
    WHERE NEXT_ATTEMPT IS NOT NULL;

    UPDATE TB_PART_INFO
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PART_INFO
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PARTY_ID
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PARTY_ID
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PLUGIN_USER_PASSWD_HISTORY
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PLUGIN_USER_PASSWD_HISTORY
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PLUGIN_USER_PASSWD_HISTORY
    SET PASSWORD_CHANGE_DATE = CONVERT_TZ(PASSWORD_CHANGE_DATE, TIMEZONE, 'UTC')
    WHERE PASSWORD_CHANGE_DATE IS NOT NULL;

    UPDATE TB_PM_ACTION
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_ACTION
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_AGREEMENT
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_AGREEMENT
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_BUSINESS_PROCESS
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_BUSINESS_PROCESS
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_CONFIGURATION
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_CONFIGURATION
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_CONFIGURATION_RAW
    SET CONFIGURATION_DATE = CONVERT_TZ(CONFIGURATION_DATE, TIMEZONE, 'UTC')
    WHERE CONFIGURATION_DATE IS NOT NULL;

    UPDATE TB_PM_CONFIGURATION_RAW
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_CONFIGURATION_RAW
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_CONFIGURATION_RAW_AUD
    SET CONFIGURATION_DATE = CONVERT_TZ(CONFIGURATION_DATE, TIMEZONE, 'UTC')
    WHERE CONFIGURATION_DATE IS NOT NULL;

    UPDATE TB_PM_ERROR_HANDLING
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_ERROR_HANDLING
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_JOIN_PAYLOAD_PROFILE
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_JOIN_PAYLOAD_PROFILE
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_JOIN_PROCESS_INIT_PARTY
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_JOIN_PROCESS_INIT_PARTY
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_JOIN_PROCESS_LEG
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_JOIN_PROCESS_LEG
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_JOIN_PROCESS_RESP_PARTY
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_JOIN_PROCESS_RESP_PARTY
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_JOIN_PROPERTY_SET
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_JOIN_PROPERTY_SET
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_LEG
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_LEG
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_LEG_MPC
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_LEG_MPC
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_MEP
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_MEP
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_MEP_BINDING
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_MEP_BINDING
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_MESSAGE_PROPERTY
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_MESSAGE_PROPERTY
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_MESSAGE_PROPERTY_SET
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_MESSAGE_PROPERTY_SET
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_MPC
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_MPC
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_PARTY
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_PARTY
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_PARTY_ID_TYPE
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_PARTY_ID_TYPE
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_PARTY_IDENTIFIER
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_PARTY_IDENTIFIER
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_PAYLOAD
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_PAYLOAD
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_PAYLOAD_PROFILE
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_PAYLOAD_PROFILE
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_PROCESS
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_PROCESS
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_RECEPTION_AWARENESS
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_RECEPTION_AWARENESS
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_RELIABILITY
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_RELIABILITY
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_ROLE
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_ROLE
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_SECURITY
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_SECURITY
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_SERVICE
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_SERVICE
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_PM_SPLITTING
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_PM_SPLITTING
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_RECEIPT
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_RECEIPT
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_RECEIPT_DATA
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_RECEIPT_DATA
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_REV_CHANGES
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_REV_CHANGES
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_REV_INFO
    SET REVISION_DATE = CONVERT_TZ(REVISION_DATE, TIMEZONE, 'UTC')
    WHERE REVISION_DATE IS NOT NULL;

    UPDATE TB_ROUTING_CRITERIA
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_ROUTING_CRITERIA
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_SEND_ATTEMPT
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_SEND_ATTEMPT
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_SEND_ATTEMPT
    SET START_DATE = CONVERT_TZ(START_DATE, TIMEZONE, 'UTC')
    WHERE START_DATE IS NOT NULL;

    UPDATE TB_SEND_ATTEMPT
    SET END_DATE = CONVERT_TZ(END_DATE, TIMEZONE, 'UTC')
    WHERE END_DATE IS NOT NULL;

    UPDATE TB_MESSAGE_FRAGMENT
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_MESSAGE_FRAGMENT
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_MESSAGE_GROUP
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_MESSAGE_GROUP
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_MESSAGE_HEADER
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_MESSAGE_HEADER
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_USER
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_USER
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_USER
    SET PASSWORD_CHANGE_DATE = CONVERT_TZ(PASSWORD_CHANGE_DATE, TIMEZONE, 'UTC')
    WHERE PASSWORD_CHANGE_DATE IS NOT NULL;

    UPDATE TB_USER
    SET SUSPENSION_DATE = CONVERT_TZ(SUSPENSION_DATE, TIMEZONE, 'UTC')
    WHERE SUSPENSION_DATE IS NOT NULL;

    UPDATE TB_USER_AUD
    SET PASSWORD_CHANGE_DATE = CONVERT_TZ(PASSWORD_CHANGE_DATE, TIMEZONE, 'UTC')
    WHERE PASSWORD_CHANGE_DATE IS NOT NULL;

    UPDATE TB_USER_MESSAGE
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_USER_MESSAGE
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_SIGNAL_MESSAGE
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_SIGNAL_MESSAGE
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_MESSAGE_LOG
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_MESSAGE_LOG
    SET DELETED = CONVERT_TZ(DELETED, TIMEZONE, 'UTC')
    WHERE DELETED IS NOT NULL;

    UPDATE TB_MESSAGE_LOG
    SET DOWNLOADED = CONVERT_TZ(DOWNLOADED, TIMEZONE, 'UTC')
    WHERE DOWNLOADED IS NOT NULL;

    UPDATE TB_MESSAGE_LOG
    SET FAILED = CONVERT_TZ(FAILED, TIMEZONE, 'UTC')
    WHERE FAILED IS NOT NULL;

    UPDATE TB_MESSAGE_LOG
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_MESSAGE_LOG
    SET NEXT_ATTEMPT = CONVERT_TZ(NEXT_ATTEMPT, TIMEZONE, 'UTC')
    WHERE NEXT_ATTEMPT IS NOT NULL;

    UPDATE TB_MESSAGE_LOG
    SET RECEIVED = CONVERT_TZ(RECEIVED, TIMEZONE, 'UTC')
    WHERE RECEIVED IS NOT NULL;

    UPDATE TB_MESSAGE_LOG
    SET RESTORED = CONVERT_TZ(RESTORED, TIMEZONE, 'UTC')
    WHERE RESTORED IS NOT NULL;

    UPDATE TB_MESSAGE_INFO
    SET TIME_STAMP = CONVERT_TZ(TIME_STAMP, TIMEZONE, 'UTC')
    WHERE TIME_STAMP IS NOT NULL;

    UPDATE TB_MESSAGE_INFO
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_MESSAGE_INFO
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_RAWENVELOPE_LOG
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_RAWENVELOPE_LOG
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_USER_PASSWORD_HISTORY
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_USER_PASSWORD_HISTORY
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_USER_PASSWORD_HISTORY
    SET PASSWORD_CHANGE_DATE = CONVERT_TZ(PASSWORD_CHANGE_DATE, TIMEZONE, 'UTC')
    WHERE PASSWORD_CHANGE_DATE IS NOT NULL;

    UPDATE TB_USER_ROLE
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_USER_ROLE
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_USER_ROLES
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_USER_ROLES
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE WS_PLUGIN_TB_MESSAGE_LOG
    SET RECEIVED = CONVERT_TZ(RECEIVED, TIMEZONE, 'UTC')
    WHERE RECEIVED IS NOT NULL;

    COMMIT;
END
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_utc_conversion_multitenancy
//

CREATE PROCEDURE MIGRATE_42_TO_50_utc_conversion_multitenancy(IN TIMEZONE VARCHAR(255))
BEGIN
    UPDATE TB_ALERT
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_ALERT
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_ALERT
    SET NEXT_ATTEMPT = CONVERT_TZ(NEXT_ATTEMPT, TIMEZONE, 'UTC')
    WHERE NEXT_ATTEMPT IS NOT NULL;

    UPDATE TB_ALERT
    SET PROCESSED_TIME = CONVERT_TZ(PROCESSED_TIME, TIMEZONE, 'UTC')
    WHERE PROCESSED_TIME IS NOT NULL;

    UPDATE TB_ALERT
    SET REPORTING_TIME = CONVERT_TZ(REPORTING_TIME, TIMEZONE, 'UTC')
    WHERE REPORTING_TIME IS NOT NULL;

    UPDATE TB_ALERT
    SET REPORTING_TIME_FAILURE = CONVERT_TZ(REPORTING_TIME_FAILURE, TIMEZONE, 'UTC')
    WHERE REPORTING_TIME_FAILURE IS NOT NULL;

    UPDATE TB_COMMAND
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_COMMAND
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_COMMAND_PROPERTY
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_COMMAND_PROPERTY
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_EVENT
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_EVENT
    SET LAST_ALERT_DATE = CONVERT_TZ(LAST_ALERT_DATE, TIMEZONE, 'UTC')
    WHERE LAST_ALERT_DATE IS NOT NULL;

    UPDATE TB_EVENT
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_EVENT
    SET REPORTING_TIME = CONVERT_TZ(REPORTING_TIME, TIMEZONE, 'UTC')
    WHERE REPORTING_TIME IS NOT NULL;

    UPDATE TB_EVENT_ALERT
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_EVENT_ALERT
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_EVENT_PROPERTY
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_EVENT_PROPERTY
    SET DATE_VALUE = CONVERT_TZ(DATE_VALUE, TIMEZONE, 'UTC')
    WHERE DATE_VALUE IS NOT NULL;

    UPDATE TB_EVENT_PROPERTY
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_REV_CHANGES
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_REV_CHANGES
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_REV_INFO
    SET REVISION_DATE = CONVERT_TZ(REVISION_DATE, TIMEZONE, 'UTC')
    WHERE REVISION_DATE IS NOT NULL;

    UPDATE TB_USER
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_USER
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_USER
    SET PASSWORD_CHANGE_DATE = CONVERT_TZ(PASSWORD_CHANGE_DATE, TIMEZONE, 'UTC')
    WHERE PASSWORD_CHANGE_DATE IS NOT NULL;

    UPDATE TB_USER
    SET SUSPENSION_DATE = CONVERT_TZ(SUSPENSION_DATE, TIMEZONE, 'UTC')
    WHERE SUSPENSION_DATE IS NOT NULL;

    UPDATE TB_USER_AUD
    SET PASSWORD_CHANGE_DATE = CONVERT_TZ(PASSWORD_CHANGE_DATE, TIMEZONE, 'UTC')
    WHERE PASSWORD_CHANGE_DATE IS NOT NULL;

    UPDATE TB_USER_DOMAIN
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_USER_DOMAIN
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_USER_PASSWORD_HISTORY
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_USER_PASSWORD_HISTORY
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_USER_PASSWORD_HISTORY
    SET PASSWORD_CHANGE_DATE = CONVERT_TZ(PASSWORD_CHANGE_DATE, TIMEZONE, 'UTC')
    WHERE PASSWORD_CHANGE_DATE IS NOT NULL;

    UPDATE TB_USER_ROLE
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_USER_ROLE
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    UPDATE TB_USER_ROLES
    SET CREATION_TIME = CONVERT_TZ(CREATION_TIME, TIMEZONE, 'UTC')
    WHERE CREATION_TIME IS NOT NULL;

    UPDATE TB_USER_ROLES
    SET MODIFICATION_TIME = CONVERT_TZ(MODIFICATION_TIME, TIMEZONE, 'UTC')
    WHERE MODIFICATION_TIME IS NOT NULL;

    COMMIT;
END
//