-- ********************************************************************************************************
-- Domibus 4.2.3 to 5.0 data migration subprograms
--
-- Main entry point is the procedure 'MIGRATE_42_TO_50_migrate'.
--
-- Parameters to be adjusted:
-- BATCH_SIZE - size of the batch for data migration on each migrated table after which there is a commit;
--              default value is 10000
-- VERBOSE_LOGS - more information into the logs; default to false
--
-- Tables which are migrated: TB_USER_MESSAGE, TB_MESSAGE_FRAGMENT, TB_MESSAGE_GROUP, TB_MESSAGE_HEADER,
-- TB_MESSAGE_LOG, TB_RECEIPT, TB_RECEIPT_DATA, TB_RAWENVELOPE_LOG, TB_PROPERTY, TB_PART_INFO,
-- TB_ERROR_LOG, TB_MESSAGE_ACKNW, TB_SEND_ATTEMPT
-- ********************************************************************************************************
DELIMITER //

-- batch size for commit of the migrated records
SET @BATCH_SIZE := 10000
//

-- enable more verbose logs
SET @VERBOSE_LOGS := FALSE
//

DROP FUNCTION IF EXISTS MIGRATE_42_TO_50_check_table_exists
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_trace
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_log_verbose
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_drop_table_if_exists
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_truncate_or_create_table
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_check_counts
//

DROP FUNCTION IF EXISTS MIGRATE_42_TO_50_generate_scalable_seq
//

DROP FUNCTION IF EXISTS MIGRATE_42_TO_50_generate_id
//

DROP FUNCTION IF EXISTS MIGRATE_42_TO_50_generate_new_id
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_update_migration_pks
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_lookup_migration_pk
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_lookup_audit_migration_pk
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_d_mpc_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_d_role_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_d_msh_role_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_d_service_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_d_msg_status_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_d_agreement_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_d_action_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_d_party_rec
//

DROP FUNCTION IF EXISTS MIGRATE_42_TO_50_get_msg_subtype
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_d_notif_status_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_user_message_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_signal_message_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_d_msg_property_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_d_part_property_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_prepare_timezone_offset
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_user_message
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_message_fragment
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_message_group
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_message_header
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_signal_receipt
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_raw_envelope_log
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_message_log
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_property
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_part_info_user
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_part_info_property
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_error_log
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_message_acknw
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_send_attempt
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_action_audit
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_alert
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_event
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_event_alert
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_event_property
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_authentication_entry
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_plugin_user_passwd_history
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_backend_filter
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_routing_criteria
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_certificate
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_command
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_command_property
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_encryption_key
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_message_acknw_prop
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_messaging_lock
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_business_process
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_action
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_agreement
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_error_handling
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_mep
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_mep_binding
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_message_property
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_message_property_set
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_join_property_set
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_party
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_configuration
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_mpc
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_party_id_type
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_party_identifier
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_payload
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_payload_profile
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_join_payload_profile
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_reception_awareness
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_reliability
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_role
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_security
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_service
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_splitting
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_leg
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_leg_mpc
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_process
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_join_process_init_party
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_join_process_leg
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_join_process_resp_party
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_configuration_raw
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_user_domain
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_user
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_user_password_history
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_user_role
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_user_roles
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_ws_plugin_tb_message_log
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_rev_info
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_rev_changes
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_authentication_entry_aud
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_back_rcriteria_aud
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_backend_filter_aud
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_certificate_aud
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_configuration_aud
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_configuration_raw_aud
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_party_aud
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_party_id_type_aud
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_pm_party_identifier_aud
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_routing_criteria_aud
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_user_aud
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_user_role_aud
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_user_roles_aud
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_multitenancy
//

/** -- Helper procedures and functions start -*/
CREATE FUNCTION MIGRATE_42_TO_50_check_table_exists(in_tab_name VARCHAR(64))
RETURNS BOOLEAN
READS SQL DATA
    BEGIN
        DECLARE v_table_exists INT;
        SELECT COUNT(*) INTO v_table_exists FROM information_schema.TABLES WHERE UPPER(table_name) = UPPER(in_tab_name);
        RETURN v_table_exists > 0;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_trace(in_message MEDIUMTEXT)
    BEGIN
        SELECT in_message AS trace;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_log_verbose(in_message MEDIUMTEXT)
    BEGIN
        IF @VERBOSE_LOGS THEN
            CALL MIGRATE_42_TO_50_trace(in_message);
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_drop_table_if_exists(in_tab_name VARCHAR(64))
    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION
            BEGIN
                 GET DIAGNOSTICS CONDITION 1
                     @p2 = MESSAGE_TEXT;
                CALL MIGRATE_42_TO_50_trace(CONCAT('drop_table_if_exists for ', in_tab_name, ' -> execute immediate error: ', @p2));
            END;

        IF MIGRATE_42_TO_50_check_table_exists(in_tab_name) THEN
            BEGIN
                SET @q := CONCAT('DROP TABLE ', in_tab_name);
                PREPARE stmt FROM @q;
                EXECUTE stmt;
                DEALLOCATE PREPARE stmt;

                CALL MIGRATE_42_TO_50_trace(CONCAT('Table ', in_tab_name, ' dropped'));
            END;
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_truncate_or_create_table(in_tab_name VARCHAR(64), in_create_sql MEDIUMTEXT)
    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION
            BEGIN
                GET DIAGNOSTICS CONDITION 1
                    @p2 = MESSAGE_TEXT;
                CALL MIGRATE_42_TO_50_trace(CONCAT('Execute immediate error: ', @p2));
            END;

        IF MIGRATE_42_TO_50_check_table_exists(in_tab_name) THEN
            SET @q := CONCAT('TRUNCATE TABLE ', in_tab_name);
            PREPARE stmt FROM @q;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;

            CALL MIGRATE_42_TO_50_trace(CONCAT('Table ', in_tab_name, ' truncated'));
        ELSE
            SET @q := in_create_sql;
            PREPARE stmt FROM @q;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;

            CALL MIGRATE_42_TO_50_trace(CONCAT('Table ', in_tab_name, ' created'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_check_counts(in_tab_name1 VARCHAR(64), in_tab_name2 VARCHAR(64), OUT out_count_match BOOLEAN)
    BEGIN
        SET @v_count_tab1 := 0;
        SET @v_count_tab2 := 0;
        BEGIN
            DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                        @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('check_counts -> execute immediate error: ', @p2));
                END;

            SET @q := CONCAT('SELECT COUNT(*) INTO @v_count_tab1 FROM ', in_tab_name1);
            PREPARE stmt FROM @q;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;

            SET @q := CONCAT('SELECT COUNT(*) INTO @v_count_tab2 FROM ', in_tab_name2);
            PREPARE stmt FROM @q;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
        END;

        SELECT @v_count_tab1 = @v_count_tab2 INTO out_count_match;
        IF out_count_match THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT('Table ', in_tab_name1, ' has same number of records as table ',
                                in_tab_name2, ' records=', @v_count_tab1));
        ELSE
            CALL MIGRATE_42_TO_50_trace(CONCAT('Table ', in_tab_name1,
                ' has different number of records as table ', in_tab_name2));
        END IF;
    END
//

CREATE FUNCTION MIGRATE_42_TO_50_generate_scalable_seq(incr BIGINT, creation_time DATE)
RETURNS BIGINT
READS SQL DATA
    BEGIN
        DECLARE seq_id BIGINT;
        SELECT CAST(CONCAT(DATE_FORMAT(creation_time, '%y%m%d%H'), LPAD(incr, 10, '0')) AS UNSIGNED)
        INTO seq_id;
        RETURN seq_id;
    END
//

-- This function generates a sequence id based on DOMIBUS_SCALABLE_SEQUENCE for a new entry
CREATE FUNCTION MIGRATE_42_TO_50_generate_id(sequence_name VARCHAR(255))
RETURNS BIGINT
READS SQL DATA
    BEGIN
        DECLARE next_val BIGINT;
        DECLARE seq_id BIGINT;

        SELECT NEXT_VAL
        INTO next_val
        FROM DOMIBUS_SCALABLE_SEQUENCE
        WHERE UPPER(SEQUENCE_NAME) = UPPER(sequence_name);

        SELECT MIGRATE_42_TO_50_generate_scalable_seq(next_val, SYSDATE())
        INTO seq_id;
        RETURN seq_id;
    END
//

-- This function generates a new sequence id based on DOMIBUS_SCALABLE_SEQUENCE for an old entry based on old id_pk and old creation_time
CREATE FUNCTION MIGRATE_42_TO_50_generate_new_id(old_id BIGINT, creation_time DATE)
RETURNS BIGINT
READS SQL DATA
    BEGIN
        DECLARE seq_id BIGINT;
        SELECT MIGRATE_42_TO_50_generate_scalable_seq(old_id, creation_time)
        INTO seq_id;
        RETURN seq_id;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_update_migration_pks(migration_key VARCHAR(255), old_id BIGINT, new_id BIGINT, INOUT migration_pks JSON)
    BEGIN
        CALL MIGRATE_42_TO_50_log_verbose(CONCAT('Update migration primary keys mappings for ', migration_key,
                ' having old_id=', old_id, ', new_id=', new_id));

        IF NOT JSON_CONTAINS_PATH(migration_pks, 'one', CONCAT('$.', migration_key)) THEN
            CALL MIGRATE_42_TO_50_log_verbose('Create missing key mapping');
            SET migration_pks := JSON_SET(migration_pks, CONCAT('$.', migration_key), CAST('{}' AS JSON));
        END IF;

        SET migration_pks := JSON_SET(migration_pks, CONCAT('$.', migration_key, '."', old_id, '"'), new_id);
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_lookup_migration_pk(migration_key VARCHAR(255), migration_pks JSON, old_id BIGINT, OUT new_id BIGINT)
    BEGIN
        CALL MIGRATE_42_TO_50_log_verbose(CONCAT('Lookup migration primary key mapping for ', migration_key,
                 ' having old_id=', old_id, ', new_id=', new_id));

        SET new_id := CAST(JSON_EXTRACT(migration_pks, CONCAT('$.', migration_key, '."', old_id, '"')) AS UNSIGNED);
    END
//


CREATE PROCEDURE MIGRATE_42_TO_50_lookup_audit_migration_pk(migration_key VARCHAR(255), migration_pks JSON, missing_entity_date_prefix DATE, old_id BIGINT, OUT new_id BIGINT)
BEGIN
    CALL MIGRATE_42_TO_50_log_verbose(CONCAT('Lookup audit migration primary key mapping for ', migration_key,
                 ' having old_id=', old_id, ', new_id=', new_id));

    CALL MIGRATE_42_TO_50_lookup_migration_pk(migration_key, migration_pks, old_id, new_id);

    IF new_id IS NULL THEN
        CALL MIGRATE_42_TO_50_log_verbose('Audit primary key not found (deleted entity?): prefixing with current date');
        SET new_id := MIGRATE_42_TO_50_generate_scalable_seq(old_id, missing_entity_date_prefix);
    END IF;
END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_d_mpc_rec(in_mpc VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_mpc IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record added into TB_D_MPC');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('Add new record into TB_D_MPC: ', in_mpc));

                    -- create new record
                    INSERT INTO TB_D_MPC (VALUE) VALUES (in_mpc);
                    COMMIT;

                    SET out_id_pk := LAST_INSERT_ID();
                END;

            SELECT ID_PK INTO out_id_pk FROM TB_D_MPC WHERE VALUE = in_mpc;
        END;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_d_role_rec(in_role VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_role IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record added into TB_D_ROLE');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('Add new record into TB_D_ROLE: ', in_role));

                    -- create new record
                    INSERT INTO TB_D_ROLE(ROLE) VALUES (in_role);
                    COMMIT;

                    SET out_id_pk := LAST_INSERT_ID();
                END;

            SELECT ID_PK INTO out_id_pk FROM TB_D_ROLE WHERE ROLE = in_role;
        END;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_d_msh_role_rec(in_role VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_role IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record added into TB_D_MSH_ROLE');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('Add new record into TB_D_MSH_ROLE: ', in_role));

                    -- create new record
                    INSERT INTO TB_D_MSH_ROLE(ROLE) VALUES (in_role);
                    COMMIT;

                    SET out_id_pk := LAST_INSERT_ID();
                END;

            SELECT ID_PK INTO out_id_pk FROM TB_D_MSH_ROLE WHERE ROLE = in_role;
        END;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_d_service_rec(in_service_type VARCHAR(255), in_service_value VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_service_type IS NULL AND in_service_value IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record added into TB_D_SERVICE');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('Add new record into TB_D_SERVICE: ',
                            COALESCE(in_service_type, ''), ' , ', COALESCE(in_service_value, '')));

                    -- create new record
                    INSERT INTO TB_D_SERVICE(TYPE, VALUE) VALUES (in_service_type, in_service_value);
                    COMMIT;

                    SET out_id_pk := LAST_INSERT_ID();
                END;

            IF in_service_type IS NULL THEN
                SELECT ID_PK INTO out_id_pk FROM TB_D_SERVICE WHERE TYPE IS NULL AND VALUE = in_service_value;
            ELSEIF in_service_value IS NULL THEN
                SELECT ID_PK INTO out_id_pk FROM TB_D_SERVICE WHERE TYPE = in_service_type AND VALUE IS NULL;
            ELSE
                SELECT ID_PK INTO out_id_pk FROM TB_D_SERVICE WHERE TYPE = in_service_type AND VALUE = in_service_value;
            END IF;
        END;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_d_msg_status_rec(in_message_status VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_message_status IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record added into TB_D_MESSAGE_STATUS');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('Add new record into TB_D_MESSAGE_STATUS: ', in_message_status));

                    -- create new record
                    INSERT INTO TB_D_MESSAGE_STATUS(STATUS) VALUES (in_message_status);
                    COMMIT;

                    SET out_id_pk := LAST_INSERT_ID();
                END;

            SELECT ID_PK INTO out_id_pk FROM TB_D_MESSAGE_STATUS WHERE STATUS = in_message_status;
        END;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_d_agreement_rec(in_agreement_type VARCHAR(255), in_agreement_value VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_agreement_type IS NULL AND in_agreement_value IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record added into TB_D_AGREEMENT');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('Add new record into TB_D_AGREEMENT: ',
                            COALESCE(in_agreement_type, ''), ' , ', COALESCE(in_agreement_value, '')));

                    -- create new record
                    INSERT INTO TB_D_AGREEMENT(TYPE, VALUE) VALUES (in_agreement_type, in_agreement_value);
                    COMMIT;

                    SET out_id_pk := LAST_INSERT_ID();
                END;

            SELECT ID_PK INTO out_id_pk FROM TB_D_AGREEMENT WHERE TYPE = in_agreement_type AND VALUE = in_agreement_value;
        END;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_d_action_rec(in_action VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_action IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record added into TB_D_ACTION');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('Add new record into TB_D_ACTION: ', in_action));

                    -- create new record
                    INSERT INTO TB_D_ACTION(ACTION) VALUES (in_action);
                    COMMIT;

                    SET out_id_pk := LAST_INSERT_ID();
                END;

            SELECT ID_PK INTO out_id_pk FROM TB_D_ACTION WHERE ACTION = in_action;
        END;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_d_party_rec(in_party_type VARCHAR(255), in_party_value VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_party_type IS NULL AND in_party_value IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record added into TB_D_PARTY');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('Add new record into TB_D_PARTY: ',
                            COALESCE(in_party_type, ''), ' , ', COALESCE(in_party_value, '')));

                    -- create new record
                    INSERT INTO TB_D_PARTY(TYPE, VALUE) VALUES (in_party_type, in_party_value);
                    COMMIT;

                    SET out_id_pk := LAST_INSERT_ID();
                END;

            SELECT ID_PK INTO out_id_pk FROM TB_D_PARTY WHERE TYPE = in_party_type AND VALUE = in_party_value;
        END;
    END
//

CREATE FUNCTION MIGRATE_42_TO_50_get_msg_subtype(in_msg_subtype VARCHAR(255))
RETURNS BOOLEAN
DETERMINISTIC
    BEGIN
        DECLARE test_message BOOLEAN DEFAULT FALSE;
        IF in_msg_subtype = 'TEST' THEN
            SET test_message := TRUE;
        END IF;
        RETURN test_message;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_d_notif_status_rec(in_status VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_status IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record added into TB_D_NOTIFICATION_STATUS');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('Add new record into TB_D_NOTIFICATION_STATUS: ', in_status));

                    -- create new record
                    INSERT INTO TB_D_NOTIFICATION_STATUS(STATUS) VALUES (in_status);
                    COMMIT;

                    SET out_id_pk := LAST_INSERT_ID();
                END;

            SELECT ID_PK INTO out_id_pk FROM TB_D_NOTIFICATION_STATUS WHERE STATUS = in_status;
        END;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_user_message_rec(in_message_id VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_message_id IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record to look into MIGR_TB_USER_MESSAGE');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('No record found into MIGR_TB_USER_MESSAGE for MESSAGE_ID = ', in_message_id));
                END;

            -- TODO check index on message_id column?
            SELECT ID_PK INTO out_id_pk FROM MIGR_TB_USER_MESSAGE WHERE MESSAGE_ID = in_message_id;
        END;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_signal_message_rec(in_message_id VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_message_id IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record to look into MIGR_TB_SIGNAL_MESSAGE');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('No record found into MIGR_TB_SIGNAL_MESSAGE for MESSAGE_ID = ', in_message_id));
                END;

            -- TODO check index on signal_message_id column?
            SELECT ID_PK INTO out_id_pk FROM MIGR_TB_SIGNAL_MESSAGE WHERE SIGNAL_MESSAGE_ID = in_message_id;
        END;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_d_msg_property_rec(in_prop_name VARCHAR(255), in_prop_value VARCHAR(1024),
        in_prop_type VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_prop_name IS NULL AND in_prop_value IS NULL AND in_prop_type IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record added into TB_D_MESSAGE_PROPERTY');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('Add new record into TB_D_MESSAGE_PROPERTY: ',
                            COALESCE(in_prop_name, ''), ' , ', COALESCE(in_prop_value, ''), ' , ',
                            COALESCE(in_prop_type, '')));

                    -- create new record
                    INSERT INTO TB_D_MESSAGE_PROPERTY(NAME, VALUE, TYPE) VALUES (in_prop_name, in_prop_value, in_prop_type);
                    COMMIT;

                    SET out_id_pk := LAST_INSERT_ID();
                END;

            SELECT ID_PK INTO out_id_pk FROM TB_D_MESSAGE_PROPERTY WHERE (NAME = in_prop_name AND TYPE = in_prop_type
                    AND VALUE = in_prop_value) OR (NAME = in_prop_name AND TYPE IS NULL AND VALUE = in_prop_value);
        END;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_d_part_property_rec(in_prop_name VARCHAR(255), in_prop_value VARCHAR(1024),
        in_prop_type VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_prop_name IS NULL AND in_prop_value IS NULL AND in_prop_type IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record added into TB_D_PART_PROPERTY');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('Add new record into TB_D_PART_PROPERTY: ',
                            COALESCE(in_prop_name, ''), ' , ', COALESCE(in_prop_value, ''), ' , ',
                            COALESCE(in_prop_type, '')));

                    -- create new record
                    INSERT INTO TB_D_PART_PROPERTY(NAME, VALUE, TYPE) VALUES (in_prop_name, in_prop_value, in_prop_type);
                    COMMIT;

                    SET out_id_pk := LAST_INSERT_ID();
                END;

            SELECT ID_PK INTO out_id_pk FROM TB_D_PART_PROPERTY WHERE (NAME = in_prop_name AND VALUE = in_prop_value
                    AND TYPE = in_prop_type) OR (NAME = in_prop_name AND VALUE = in_prop_value AND TYPE IS NULL);
        END;
    END
//

/**-- TB_D_TIMEZONE_OFFSET migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_prepare_timezone_offset(INOUT migration_pks JSON)
    BEGIN
        DECLARE calculated_id_pk BIGINT;

        SELECT ID_PK INTO calculated_id_pk FROM TB_D_TIMEZONE_OFFSET WHERE NEXT_ATTEMPT_TIMEZONE_ID = 'UTC';

        IF calculated_id_pk IS NULL THEN
            SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(1, SYSDATE());
            CALL MIGRATE_42_TO_50_update_migration_pks('timezone_offset', 1, calculated_id_pk, migration_pks);

            INSERT INTO TB_D_TIMEZONE_OFFSET (ID_PK, NEXT_ATTEMPT_TIMEZONE_ID, NEXT_ATTEMPT_OFFSET_SECONDS, CREATION_TIME, CREATED_BY)
            VALUES (calculated_id_pk,
                    'UTC',
                    0,
                    SYSDATE(),
                    'migration');
        END IF;
    END
//

/**-- TB_USER_MESSAGE migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_user_message(INOUT migration_pks JSON)
    BEGIN
        DECLARE action VARCHAR(255);
        DECLARE agreement_ref_type VARCHAR(255);
        DECLARE agreement_ref_value VARCHAR(255);
        DECLARE conversation_id VARCHAR(255);
        DECLARE ebms3_timestamp TIMESTAMP;
        DECLARE from_party_type VARCHAR(255);
        DECLARE from_party_value VARCHAR(255);
        DECLARE from_role VARCHAR(255);
        DECLARE id_pk BIGINT;
        DECLARE message_fragment BIT(1);
        DECLARE message_id VARCHAR(255);
        DECLARE message_subtype VARCHAR(255);
        DECLARE mpc VARCHAR(255);
        DECLARE ref_to_message_id VARCHAR(255);
        DECLARE service_type VARCHAR(255);
        DECLARE service_value VARCHAR(255);
        DECLARE source_message BIT(1);
        DECLARE to_party_type VARCHAR(255);
        DECLARE to_party_value VARCHAR(255);
        DECLARE to_role VARCHAR(255);
        DECLARE creation_time TIMESTAMP;

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_mpc_id_fk BIGINT;
        DECLARE calculated_from_role_id_fk BIGINT;
        DECLARE calculated_to_role_id_fk BIGINT;
        DECLARE calculated_service_id_fk BIGINT;
        DECLARE calculated_agreement_id_fk BIGINT;
        DECLARE calculated_action_id_fk BIGINT;
        DECLARE calculated_from_party_id_fk BIGINT;
        DECLARE calculated_to_party_id_fk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_user_message CURSOR FOR
            SELECT UM.ID_PK,
                   MI.MESSAGE_ID,
                   MI.REF_TO_MESSAGE_ID,
                   UM.COLL_INFO_CONVERS_ID      CONVERSATION_ID,
                   ML.SOURCE_MESSAGE,
                   ML.MESSAGE_FRAGMENT,
                   MI.TIME_STAMP                EBMS3_TIMESTAMP,
                   UM.MPC,
                   UM.FROM_ROLE,
                   UM.TO_ROLE,
                   UM.SERVICE_TYPE,
                   UM.SERVICE_VALUE,
                   UM.AGREEMENT_REF_TYPE,
                   UM.AGREEMENT_REF_VALUE,
                   UM.COLLABORATION_INFO_ACTION ACTION,
                   PA1.TYPE                     FROM_PARTY_TYPE,
                   PA1.VALUE                    FROM_PARTY_VALUE,
                   PA2.TYPE                     TO_PARTY_TYPE,
                   PA2.VALUE                    TO_PARTY_VALUE,
                   ML.MESSAGE_SUBTYPE,
                   UM.CREATION_TIME
            FROM TB_MESSAGE_LOG ML
                     LEFT OUTER JOIN TB_MESSAGE_INFO MI ON ML.MESSAGE_ID = MI.MESSAGE_ID,
                 TB_USER_MESSAGE UM
                     LEFT OUTER JOIN TB_PARTY_ID PA1 ON UM.ID_PK = PA1.FROM_ID
                     LEFT OUTER JOIN TB_PARTY_ID PA2 ON UM.ID_PK = PA2.TO_ID
            WHERE UM.MESSAGEINFO_ID_PK = MI.ID_PK;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_USER_MESSAGE';
        SET @v_tab_new := 'MIGR_TB_USER_MESSAGE';

        /** migrate old columns and add data into dictionary tables */
        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_user_message;
        read_loop: LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                    BEGIN
                        GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                        CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_user_message -> execute immediate error: ', @p2));
                    END;

                FETCH c_user_message INTO id_pk, message_id, ref_to_message_id, conversation_id, source_message,
                        message_fragment, ebms3_timestamp, mpc, from_role, to_role, service_type, service_value,
                        agreement_ref_type, agreement_ref_value, action, from_party_type, from_party_value,
                        to_party_type, to_party_value, message_subtype, creation_time;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('user_message', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_update_migration_pks('message_info', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_get_tb_d_mpc_rec(mpc, calculated_mpc_id_fk);
                CALL MIGRATE_42_TO_50_get_tb_d_role_rec(from_role, calculated_from_role_id_fk);
                CALL MIGRATE_42_TO_50_get_tb_d_role_rec(to_role, calculated_to_role_id_fk);
                CALL MIGRATE_42_TO_50_get_tb_d_service_rec(service_type, service_value, calculated_service_id_fk);
                CALL MIGRATE_42_TO_50_get_tb_d_agreement_rec(agreement_ref_type, agreement_ref_value, calculated_agreement_id_fk);
                CALL MIGRATE_42_TO_50_get_tb_d_action_rec(action, calculated_action_id_fk);
                CALL MIGRATE_42_TO_50_get_tb_d_party_rec(from_party_type, from_party_value, calculated_from_party_id_fk);
                CALL MIGRATE_42_TO_50_get_tb_d_party_rec(to_party_type, to_party_value, calculated_to_party_id_fk);

                INSERT INTO MIGR_TB_USER_MESSAGE (ID_PK, MESSAGE_ID, REF_TO_MESSAGE_ID, CONVERSATION_ID, SOURCE_MESSAGE,
                         MESSAGE_FRAGMENT, EBMS3_TIMESTAMP, MPC_ID_FK , FROM_ROLE_ID_FK, TO_ROLE_ID_FK, SERVICE_ID_FK,
                         AGREEMENT_ID_FK, ACTION_ID_FK, FROM_PARTY_ID_FK, TO_PARTY_ID_FK, TEST_MESSAGE)
                 VALUES (calculated_id_pk,
                         message_id,
                         ref_to_message_id,
                         conversation_id,
                         source_message,
                         message_fragment,
                         ebms3_timestamp,
                         calculated_mpc_id_fk,
                         calculated_from_role_id_fk,
                         calculated_to_role_id_fk,
                         calculated_service_id_fk,
                         calculated_agreement_id_fk,
                         calculated_action_id_fk,
                         calculated_from_party_id_fk,
                         calculated_to_party_id_fk,
                         MIGRATE_42_TO_50_get_msg_subtype(message_subtype));

                 SET @i = @i + 1;
                 IF @i MOD @BATCH_SIZE = 0 THEN
                     COMMIT;
                     CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                     SET @v_batch_no := @v_batch_no + 1;
                 END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_user_message;

        -- check counts
        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

/**-- TB_MESSAGE_FRAGMENT migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_message_fragment(INOUT migration_pks JSON)
    BEGIN
        DECLARE created_by VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE fragment_number INT;
        DECLARE group_id VARCHAR(255);
        DECLARE group_id_fk BIGINT;
        DECLARE id_pk BIGINT;
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_message_fragment CURSOR FOR
            SELECT UM.ID_PK, -- 1:1 ID_PK implementation
                   MF.GROUP_ID,
                   MF.FRAGMENT_NUMBER,
                   MF.CREATION_TIME,
                   MF.CREATED_BY,
                   MF.MODIFICATION_TIME,
                   MF.MODIFIED_BY,
                   MG.ID_PK GROUP_ID_FK
            FROM TB_MESSAGE_FRAGMENT MF,
                 TB_MESSAGE_GROUP MG,
                 TB_USER_MESSAGE UM
            WHERE UM.FK_MESSAGE_FRAGMENT_ID = MF.ID_PK
              AND MF.GROUP_ID = MG.GROUP_ID;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_MESSAGE_FRAGMENT';
        SET @v_tab_new := 'MIGR_TB_SJ_MESSAGE_FRAGMENT';
        SET @v_tab_user_message := 'TB_USER_MESSAGE';

        IF NOT MIGRATE_42_TO_50_check_table_exists(@v_tab_user_message) THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_user_message, ' should exists before starting ', @v_tab, ' migration'));
        END IF;

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_message_fragment;
        read_loop: LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                    BEGIN
                        GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                        CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_message_fragment -> execute immediate error: ', @p2));
                    END;

                FETCH c_message_fragment INTO id_pk, group_id, fragment_number, creation_time, created_by,
                        modification_time, modified_by, group_id_fk;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('user_message', migration_pks, id_pk, calculated_id_pk);

                INSERT INTO MIGR_TB_SJ_MESSAGE_FRAGMENT (ID_PK, GROUP_ID_FK, FRAGMENT_NUMBER, CREATION_TIME,
                        CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        group_id_fk,
                        fragment_number,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Migrated ', @i, ' records in total'));
        CLOSE c_message_fragment;

        -- check counts
        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

/**-- TB_MESSAGE_GROUP migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_message_group(INOUT migration_pks JSON)
    BEGIN
        DECLARE compressed_message_size BIGINT;
        DECLARE compression_algorithm VARCHAR(255);
        DECLARE created_by VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE expired BIT(1);
        DECLARE fragment_count INT;
        DECLARE group_id VARCHAR(255);
        DECLARE id_pk BIGINT;
        DECLARE message_size BIGINT;
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);
        DECLARE msh_role VARCHAR(255);
        DECLARE received_fragments INT;
        DECLARE rejected BIT(1);
        DECLARE sent_fragments INT;
        DECLARE soap_action VARCHAR(255);
        DECLARE source_message_id VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_msh_role_id_fk BIGINT;
        DECLARE calculated_source_message_id_fk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_message_group CURSOR FOR
            SELECT MG.ID_PK,
                   MG.GROUP_ID,
                   MG.MESSAGE_SIZE,
                   MG.FRAGMENT_COUNT,
                   MG.SENT_FRAGMENTS,
                   MG.RECEIVED_FRAGMENTS,
                   MG.COMPRESSION_ALGORITHM,
                   MG.COMPRESSED_MESSAGE_SIZE,
                   MG.SOAP_ACTION,
                   MG.REJECTED,
                   MG.EXPIRED,
                   MG.MSH_ROLE,
                   MG.SOURCE_MESSAGE_ID,
                   MG.CREATION_TIME,
                   MG.CREATED_BY,
                   MG.MODIFICATION_TIME,
                   MG.MODIFIED_BY
            FROM TB_MESSAGE_GROUP MG;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_MESSAGE_GROUP';
        SET @v_tab_new := 'MIGR_TB_SJ_MESSAGE_GROUP';
        SET @v_tab_user_message_new  := 'MIGR_TB_USER_MESSAGE';

        IF NOT MIGRATE_42_TO_50_check_table_exists(@v_tab_user_message_new) THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_user_message_new, ' should exists before starting ', @v_tab, ' migration'));
        END IF;

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_message_group;
        read_loop: LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                    BEGIN
                        GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                        CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_message_group -> execute immediate error: ', @p2));
                    END;

                FETCH c_message_group INTO id_pk, group_id, message_size, fragment_count, sent_fragments,
                        received_fragments, compression_algorithm, compressed_message_size, soap_action, rejected,
                        expired, msh_role, source_message_id, creation_time, created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('message_group', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_get_tb_d_msh_role_rec(msh_role, calculated_msh_role_id_fk);
                CALL MIGRATE_42_TO_50_get_tb_user_message_rec(source_message_id, calculated_source_message_id_fk);

                INSERT INTO MIGR_TB_SJ_MESSAGE_GROUP (ID_PK, GROUP_ID, MESSAGE_SIZE, FRAGMENT_COUNT, SENT_FRAGMENTS,
                        RECEIVED_FRAGMENTS, COMPRESSION_ALGORITHM, COMPRESSED_MESSAGE_SIZE, SOAP_ACTION, REJECTED,
                        EXPIRED, MSH_ROLE_ID_FK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY,
                        SOURCE_MESSAGE_ID_FK)
                VALUES (calculated_id_pk,
                        group_id,
                        message_size,
                        fragment_count,
                        sent_fragments,
                        received_fragments,
                        compression_algorithm,
                        compressed_message_size,
                        soap_action,
                        rejected,
                        expired,
                        calculated_msh_role_id_fk,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by,
                        calculated_source_message_id_fk);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                                @v_tab_new,  ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Migrated ', @i, ' records in total'));
        CLOSE c_message_group;

        -- check counts
        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;

    END
//

/**-- TB_MESSAGE_GROUP migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_message_header(INOUT migration_pks JSON)
    BEGIN
        DECLARE boundary VARCHAR(255);
        DECLARE created_by VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE id_pk BIGINT;
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);
        DECLARE start VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_message_header CURSOR FOR
            SELECT MG.ID_PK, -- 1:1 ID_PK implementation
                   MH.BOUNDARY,
                   MH.START,
                   MH.CREATION_TIME,
                   MH.CREATED_BY,
                   MH.MODIFICATION_TIME,
                   MH.MODIFIED_BY
            FROM TB_MESSAGE_HEADER MH,
                 TB_MESSAGE_GROUP MG
            WHERE MG.FK_MESSAGE_HEADER_ID = MH.ID_PK;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab  := 'TB_MESSAGE_HEADER';
        SET @v_tab_new := 'MIGR_TB_SJ_MESSAGE_HEADER';
        SET @v_tab_message_group := 'TB_MESSAGE_GROUP';

        IF NOT MIGRATE_42_TO_50_check_table_exists(@v_tab_message_group) THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_message_group, ' should exists before starting ', @v_tab, ' migration'));
        END IF;

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_message_header;
        read_loop: LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                    BEGIN
                        GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                        CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_message_header -> execute immediate error: ', @p2));
                    END;

                FETCH c_message_header INTO id_pk, boundary, start, creation_time, created_by, modification_time,
                        modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('message_group', migration_pks, id_pk, calculated_id_pk);

                INSERT INTO MIGR_TB_SJ_MESSAGE_HEADER (ID_PK, BOUNDARY, START_MULTIPART, CREATION_TIME, CREATED_BY,
                        MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        boundary,
                        start,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Migrated ', @i, ' records in total'));
        CLOSE c_message_header;

        -- check counts
        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;

    END
//

/**-- TB_SIGNAL_MESSAGE, TB_RECEIPT and TB_RECEIPT_DATA migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_signal_receipt(INOUT migration_pks JSON)
    BEGIN
        DECLARE created_by VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE ebms3_timestamp TIMESTAMP;
        DECLARE id_pk BIGINT;
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);
        DECLARE r_created_by VARCHAR(255);
        DECLARE r_creation_time TIMESTAMP;
        DECLARE r_modification_time TIMESTAMP;
        DECLARE r_modified_by VARCHAR(255);
        DECLARE raw_xml LONGTEXT;
        DECLARE ref_to_message_id VARCHAR(255);
        DECLARE signal_message_id VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_signal_message_receipt CURSOR FOR
            SELECT UM.ID_PK, --  1:1 here
                   MI.MESSAGE_ID        SIGNAL_MESSAGE_ID,
                   MI.REF_TO_MESSAGE_ID REF_TO_MESSAGE_ID,
                   MI.TIME_STAMP        EBMS3_TIMESTAMP,
                   SM.CREATION_TIME,
                   SM.CREATED_BY,
                   SM.MODIFICATION_TIME,
                   SM.MODIFIED_BY,
                   RD.RAW_XML,
                   RD.CREATION_TIME     R_CREATION_TIME,
                   RD.CREATED_BY        R_CREATED_BY,
                   RD.MODIFICATION_TIME R_MODIFICATION_TIME,
                   RD.MODIFIED_BY       R_MODIFIED_BY
            FROM TB_MESSAGE_INFO MI,
                 TB_SIGNAL_MESSAGE SM,
                 TB_MESSAGING ME,
                 TB_USER_MESSAGE UM,
                 TB_RECEIPT RE,
                 TB_RECEIPT_DATA RD
            WHERE SM.MESSAGEINFO_ID_PK = MI.ID_PK
              AND ME.SIGNAL_MESSAGE_ID = SM.ID_PK
              AND ME.USER_MESSAGE_ID = UM.ID_PK
              AND RE.ID_PK = SM.RECEIPT_ID_PK
              AND RD.RECEIPT_ID = RE.ID_PK;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab_signal := 'TB_SIGNAL_MESSAGE';
        SET @v_tab_signal_new := 'MIGR_TB_SIGNAL_MESSAGE';
        SET @v_tab_messaging := 'TB_MESSAGING';
        SET @v_tab_user_message := 'TB_USER_MESSAGE';
        SET @v_tab_receipt := 'TB_RECEIPT';
        SET @v_tab_receipt_data := 'TB_RECEIPT_DATA';
        SET @v_tab_receipt_new := 'MIGR_TB_RECEIPT';

        IF NOT MIGRATE_42_TO_50_check_table_exists(@v_tab_messaging) THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_messaging, ' should exists before starting ', @v_tab_signal, ' migration'));
        END IF;
        IF NOT MIGRATE_42_TO_50_check_table_exists(@v_tab_user_message) THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_user_message, ' should exists before starting ', @v_tab_signal, ' migration'));
        END IF;

        /** migrate old columns and add data into dictionary tables */
        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_signal, ' ,', @v_tab_receipt, ' and', @v_tab_receipt_data, ' migration started...'));

        OPEN c_signal_message_receipt;
        read_loop: LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                    BEGIN
                        GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                        CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_signal_receipt -> execute immediate error: ', @p2));
                    END;

                FETCH c_signal_message_receipt INTO id_pk, signal_message_id, ref_to_message_id, ebms3_timestamp,
                        creation_time, created_by, modification_time, modified_by, raw_xml, r_creation_time,
                        r_created_by, r_modification_time, r_modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('user_message', migration_pks, id_pk, calculated_id_pk);

                -- new tb_signal_message table
                INSERT INTO MIGR_TB_SIGNAL_MESSAGE (ID_PK, SIGNAL_MESSAGE_ID, REF_TO_MESSAGE_ID, EBMS3_TIMESTAMP,
                        CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        signal_message_id,
                        ref_to_message_id,
                        ebms3_timestamp,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                -- new tb_receipt table
                INSERT INTO MIGR_TB_RECEIPT (ID_PK, RAW_XML, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        CAST(raw_xml AS BINARY),
                        r_creation_time,
                        r_created_by,
                        r_modification_time,
                        r_modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                                @v_tab_signal_new, ' and ', @v_tab_receipt_new, ': Commit after ',
                                @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_signal_new, ' and ', @v_tab_receipt_new));
        CLOSE c_signal_message_receipt;

        -- check counts
        CALL MIGRATE_42_TO_50_check_counts(@v_tab_signal, @v_tab_signal_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_signal, ' migration is done'));
        END IF;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab_receipt, @v_tab_receipt_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_receipt, ' and ', @v_tab_receipt_data, ' migration is done'));
        END IF;

    END
//

/**-- TB_RAWENVELOPE_LOG migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_raw_envelope_log(INOUT migration_pks JSON)
    BEGIN
        DECLARE created_by VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE id_pk BIGINT;
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);
        DECLARE raw_xml LONGTEXT;
        DECLARE type VARCHAR(6);

        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_raw_envelope CURSOR FOR
            SELECT UM.ID_PK, --  1:1 here
                   'USER' AS TYPE,
                   RA.RAW_XML,
                   RA.CREATION_TIME,
                   RA.CREATED_BY,
                   RA.MODIFICATION_TIME,
                   RA.MODIFIED_BY
            FROM TB_USER_MESSAGE UM,
                 TB_RAWENVELOPE_LOG RA
            WHERE UM.ID_PK = RA.USERMESSAGE_ID_FK
            UNION ALL
            SELECT UM.ID_PK, --  1:1 here
                   'SIGNAL' AS TYPE,
                   RA.RAW_XML,
                   RA.CREATION_TIME,
                   RA.CREATED_BY,
                   RA.MODIFICATION_TIME,
                   RA.MODIFIED_BY
            FROM TB_SIGNAL_MESSAGE SM,
                 TB_MESSAGING ME,
                 TB_USER_MESSAGE UM,
                 TB_RAWENVELOPE_LOG RA
            WHERE ME.SIGNAL_MESSAGE_ID = SM.ID_PK
              AND ME.USER_MESSAGE_ID = UM.ID_PK
              AND SM.ID_PK = RA.SIGNALMESSAGE_ID_FK;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_count_user := 0;
        SET @v_count_signal := 0;
        SET @v_tab_migrated := @v_tab_signal_new;
        SET @v_tab := 'TB_RAWENVELOPE_LOG';
        SET @v_tab_user_new := 'MIGR_TB_USER_MESSAGE_RAW';
        SET @v_tab_signal_new := 'MIGR_TB_SIGNAL_MESSAGE_RAW';
        SET @v_tab_user_message := 'TB_USER_MESSAGE';
        SET @v_tab_messaging := 'TB_MESSAGING';

        IF NOT MIGRATE_42_TO_50_check_table_exists(@v_tab_messaging) THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_messaging, ' should exists before starting ', @v_tab, ' migration'));
        END IF;
        IF NOT MIGRATE_42_TO_50_check_table_exists(@v_tab_user_message) THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_user_message, ' should exists before starting ', @v_tab, ' migration'));
        END IF;

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_raw_envelope;
        read_loop: LOOP
            FETCH c_raw_envelope INTO id_pk, type, raw_xml, creation_time, created_by, modification_time, modified_by;

            IF done THEN
                LEAVE read_loop;
            END IF;

            CALL MIGRATE_42_TO_50_lookup_migration_pk('user_message', migration_pks, id_pk, calculated_id_pk);

            IF @v_type = 'USER' THEN
                BEGIN
                    DECLARE EXIT HANDLER FOR SQLEXCEPTION
                        BEGIN
                            GET DIAGNOSTICS CONDITION 1
                                @p2 = MESSAGE_TEXT;
                            CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_raw_envelope_log for ', @v_tab_user_new, '-> execute immediate error: ', @p2));
                        END;

                    SET @v_count_user := @v_count_user + 1;

                    INSERT INTO MIGR_TB_USER_MESSAGE_RAW (ID_PK, RAW_XML, CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                            MODIFIED_BY)
                    VALUES (calculated_id_pk,
                        CAST(raw_xml AS BINARY),
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);
                END;
            ELSE
                BEGIN
                    DECLARE EXIT HANDLER FOR SQLEXCEPTION
                        BEGIN
                            GET DIAGNOSTICS CONDITION 1
                                @p2 = MESSAGE_TEXT;
                            CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_raw_envelope_log for ', @v_tab_signal_new, '-> execute immediate error: ', @p2));
                        END;

                    SET @v_count_signal := @v_count_signal + 1;

                    INSERT INTO MIGR_TB_SIGNAL_MESSAGE_RAW (ID_PK, RAW_XML, CREATION_TIME, CREATED_BY,
                            MODIFICATION_TIME, MODIFIED_BY)
                    VALUES (calculated_id_pk,
                            CAST(raw_xml AS BINARY),
                            creation_time,
                            created_by,
                            modification_time,
                            modified_by);
                END;
            END IF;

            SET @i = @i + 1;
            IF @i MOD @BATCH_SIZE = 0 THEN
                COMMIT;
                CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_migrated, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                SET @v_batch_no := @v_batch_no + 1;
            END IF;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT(
            'Migrated ', @i, ' records in total: ', @v_count_user, ' into ', @v_tab_user_new, ' and ', @v_count_signal,
            ' into ', @v_tab_signal_new));
        CLOSE c_raw_envelope;

        -- check counts
        IF @v_count_user + @v_count_signal = @i THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

/**-- TB_MESSAGE_LOG migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_message_log()
    BEGIN
        DECLARE backend VARCHAR(255);
        DECLARE created_by VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE deleted TIMESTAMP;
        DECLARE downloaded TIMESTAMP;
        DECLARE failed TIMESTAMP;
        DECLARE id_pk BIGINT;
        DECLARE message_id VARCHAR(255);
        DECLARE message_status VARCHAR(255);
        DECLARE message_type VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);
        DECLARE msh_role VARCHAR(255);
        DECLARE next_attempt TIMESTAMP;
        DECLARE notification_status VARCHAR(255);
        DECLARE received TIMESTAMP;
        DECLARE restored TIMESTAMP;
        DECLARE scheduled BIT(1);
        DECLARE send_attempts INT;
        DECLARE send_attempts_max INT;
        DECLARE version INT;

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_message_status_id_fk BIGINT;
        DECLARE calculated_msh_role_id_fk BIGINT;
        DECLARE calculated_notification_status_id_fk BIGINT;

        DECLARE done INT DEFAULT FALSE;

        DECLARE c_message_log CURSOR FOR
            SELECT ML.ID_PK,
                   ML.MESSAGE_ID,
                   ML.MESSAGE_TYPE,
                   ML.BACKEND,
                   ML.RECEIVED,
                   ML.DOWNLOADED,
                   ML.FAILED,
                   ML.RESTORED,
                   ML.DELETED,
                   ML.NEXT_ATTEMPT,
                   ML.SEND_ATTEMPTS,
                   ML.SEND_ATTEMPTS_MAX,
                   ML.SCHEDULED,
                   ML.VERSION,
                   ML.MESSAGE_STATUS,
                   ML.MSH_ROLE,
                   ML.NOTIFICATION_STATUS,
                   ML.CREATION_TIME,
                   ML.CREATED_BY,
                   ML.MODIFICATION_TIME,
                   ML.MODIFIED_BY
            FROM TB_MESSAGE_LOG ML;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_count_user := 0;
        SET @v_count_signal := 0;
        SET @v_tab := 'TB_MESSAGE_LOG';
        SET @v_tab_user_new := 'MIGR_TB_USER_MESSAGE_LOG';
        SET @v_tab_signal_new := 'MIGR_TB_SIGNAL_MESSAGE_LOG';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_message_log;
        read_loop: LOOP
            FETCH c_message_log INTO id_pk, message_id, message_type, backend, received, downloaded, failed, restored,
                    deleted, next_attempt, send_attempts, send_attempts_max, scheduled, version, message_status,
                    msh_role, notification_status, creation_time, created_by, modification_time, modified_by;

            IF done THEN
                LEAVE read_loop;
            END IF;

            IF message_type = 'USER_MESSAGE' THEN
                BEGIN
                    DECLARE EXIT HANDLER FOR SQLEXCEPTION
                        BEGIN
                            GET DIAGNOSTICS CONDITION 1
                                @p2 = MESSAGE_TEXT;
                            CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_message_log for ', @v_tab_user_new, '-> execute immediate error: ', @p2));
                        END;

                    SET @v_count_user := @v_count_user + 1;

                    CALL MIGRATE_42_TO_50_get_tb_user_message_rec(message_id, calculated_id_pk);
                    CALL MIGRATE_42_TO_50_get_tb_d_msg_status_rec(message_status, calculated_message_status_id_fk);
                    CALL MIGRATE_42_TO_50_get_tb_d_msh_role_rec(msh_role, calculated_msh_role_id_fk);
                    CALL MIGRATE_42_TO_50_get_tb_d_notif_status_rec(notification_status, calculated_notification_status_id_fk);

                    INSERT INTO MIGR_TB_USER_MESSAGE_LOG (ID_PK, BACKEND, RECEIVED, DOWNLOADED, FAILED, RESTORED,
                            DELETED, NEXT_ATTEMPT, SEND_ATTEMPTS, SEND_ATTEMPTS_MAX, SCHEDULED, VERSION,
                            MESSAGE_STATUS_ID_FK, MSH_ROLE_ID_FK, NOTIFICATION_STATUS_ID_FK, CREATION_TIME, CREATED_BY,
                            MODIFICATION_TIME, MODIFIED_BY)
                    VALUES (calculated_id_pk,
                            backend,
                            received,
                            downloaded,
                            failed,
                            restored,
                            deleted,
                            next_attempt,
                            send_attempts,
                            send_attempts_max,
                            scheduled,
                            version,
                            calculated_message_status_id_fk,
                            calculated_msh_role_id_fk,
                            calculated_notification_status_id_fk,
                            creation_time,
                            created_by,
                            modification_time,
                            modified_by);
                END;
            ELSE
                BEGIN
                    DECLARE EXIT HANDLER FOR SQLEXCEPTION
                        BEGIN
                            GET DIAGNOSTICS CONDITION 1
                                @p2 = MESSAGE_TEXT;
                            CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_message_log for ', @v_tab_signal_new, '-> execute immediate error: ', @p2));
                        END;

                    SET @v_count_signal := @v_count_signal + 1;

                    CALL MIGRATE_42_TO_50_get_tb_signal_message_rec(message_id, calculated_id_pk);
                    CALL MIGRATE_42_TO_50_get_tb_d_msg_status_rec(message_status, calculated_message_status_id_fk);
                    CALL MIGRATE_42_TO_50_get_tb_d_msh_role_rec(msh_role, calculated_msh_role_id_fk);

                    INSERT INTO MIGR_TB_SIGNAL_MESSAGE_LOG (ID_PK, RECEIVED, DELETED, MESSAGE_STATUS_ID_FK,
                            MSH_ROLE_ID_FK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                    VALUES (calculated_id_pk,
                            received,
                            downloaded,
                            calculated_message_status_id_fk,
                            calculated_msh_role_id_fk,
                            creation_time,
                            created_by,
                            modification_time,
                            modified_by);
                END;
            END IF;

            SET @i = @i + 1;
            IF @i MOD @BATCH_SIZE = 0 THEN
                COMMIT;
                CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                SET @v_batch_no := @v_batch_no + 1;
            END IF;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT(
            'Migrated ', @i, ' records in total: ', @v_count_user, ' into ', @v_tab_user_new, ' and ', @v_count_signal,
            ' into ', @v_tab_signal_new));
        CLOSE c_message_log;

        -- check counts
        IF @v_count_user + @v_count_signal = @i THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;

    END
//

/**- TB_PROPERTY, TB_USER_MESSAGE data migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_property(INOUT migration_pks JSON)
    BEGIN
        DECLARE created_by VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);
        DECLARE name VARCHAR(255);
        DECLARE type VARCHAR(255);
        DECLARE user_message_id_fk BIGINT;
        DECLARE value VARCHAR(1024);

        DECLARE calculated_user_message_id_fk BIGINT;
        DECLARE calculated_message_property_fk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_property CURSOR FOR
            SELECT UM.ID_PK USER_MESSAGE_ID_FK,
                   TP.NAME,
                   TP.VALUE,
                   TP.TYPE,
                   TP.CREATION_TIME,
                   TP.CREATED_BY,
                   TP.MODIFICATION_TIME,
                   TP.MODIFIED_BY
            FROM TB_PROPERTY TP,
                 TB_USER_MESSAGE UM
            WHERE TP.MESSAGEPROPERTIES_ID = UM.ID_PK
              AND TP.PARTPROPERTIES_ID IS NULL;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PROPERTY';
        SET @v_tab_user_message := 'TB_USER_MESSAGE';
        SET @v_tab_properties_new := 'MIGR_TB_MESSAGE_PROPERTIES';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_property;
        read_loop: LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                    BEGIN
                        GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                        CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_property -> execute immediate error: ', @p2));
                    END;

                FETCH c_property INTO user_message_id_fk, name, value, type, creation_time, created_by,
                        modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('user_message', migration_pks, user_message_id_fk, calculated_user_message_id_fk);
                CALL MIGRATE_42_TO_50_get_tb_d_msg_property_rec(name, value, type, calculated_message_property_fk);

                INSERT INTO MIGR_TB_MESSAGE_PROPERTIES (USER_MESSAGE_ID_FK, MESSAGE_PROPERTY_FK, CREATION_TIME,
                        CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_user_message_id_fk,
                        calculated_message_property_fk,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_properties_new));
        CLOSE c_property;

        -- check counts
        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_properties_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

/**- TB_PART_INFO, TB_USER_MESSAGE data migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_part_info_user(INOUT migration_pks JSON)
    BEGIN
        DECLARE binary_data LONGBLOB;
        DECLARE created_by VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE description_lang VARCHAR(255);
        DECLARE description_value VARCHAR(255);
        DECLARE encrypted BIT(1);
        DECLARE filename VARCHAR(255);
        DECLARE href VARCHAR(255);
        DECLARE id_pk BIGINT;
        DECLARE in_body BIT(1);
        DECLARE mime VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);
        DECLARE part_order INT;
        DECLARE user_message_id_fk BIGINT;

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_user_message_id_fk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_part_info CURSOR FOR
            SELECT PI.ID_PK,
                   PI.BINARY_DATA,
                   PI.DESCRIPTION_LANG,
                   PI.DESCRIPTION_VALUE,
                   PI.HREF,
                   PI.IN_BODY,
                   PI.FILENAME,
                   PI.MIME,
                   PI.PART_ORDER,
                   PI.ENCRYPTED,
                   PI.CREATED_BY,
                   PI.CREATION_TIME,
                   PI.MODIFIED_BY,
                   PI.MODIFICATION_TIME,
                   UM.ID_PK USER_MESSAGE_ID_FK
            FROM TB_USER_MESSAGE UM,
                 TB_PART_INFO PI
            WHERE PI.PAYLOADINFO_ID = UM.ID_PK;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PART_INFO';
        SET @v_tab_user_message := 'TB_USER_MESSAGE';
        SET @v_tab_new := 'MIGR_TB_PART_INFO';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_part_info;
        read_loop: LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                    BEGIN
                        GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                        CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_part_info_user -> execute immediate error: ', @p2));
                    END;

                FETCH c_part_info INTO id_pk, binary_data, description_lang, description_value, href, in_body, filename,
                        mime, part_order, encrypted, created_by, creation_time, modified_by, modification_time,
                        user_message_id_fk;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('part_info', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('user_message', migration_pks, user_message_id_fk, calculated_user_message_id_fk);

                INSERT INTO MIGR_TB_PART_INFO (ID_PK, BINARY_DATA, DESCRIPTION_LANG, DESCRIPTION_VALUE, HREF, IN_BODY,
                        FILENAME, MIME, PART_ORDER, ENCRYPTED, USER_MESSAGE_ID_FK, CREATION_TIME, CREATED_BY,
                        MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        binary_data,
                        description_lang,
                        description_value,
                        href,
                        in_body,
                        filename,
                        mime,
                        part_order,
                        encrypted,
                        calculated_user_message_id_fk,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_part_info;

        -- check counts
        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' and ', @v_tab_user_message, ' migration is done'));
        END IF;
    END
//

/**- TB_PART_INFO, TB_PROPERTY data migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_part_info_property(INOUT migration_pks JSON)
    BEGIN
        DECLARE created_by VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);
        DECLARE name VARCHAR(255);
        DECLARE part_info_id_fk BIGINT;
        DECLARE type VARCHAR(255);
        DECLARE value VARCHAR(1024);

        DECLARE calculated_part_info_id_fk BIGINT;
        DECLARE calculated_part_info_property_fk BIGINT;

        DECLARE done INT DEFAULT FALSE;

        DECLARE c_part_prop CURSOR FOR
            SELECT PR.NAME,
                   PR.VALUE,
                   PR.TYPE,
                   PI.ID_PK PART_INFO_ID_FK,
                   PR.CREATION_TIME,
                   PR.CREATED_BY,
                   PR.MODIFICATION_TIME,
                   PR.MODIFIED_BY
            FROM TB_PART_INFO PI,
                 TB_PROPERTY PR
            WHERE PR.PARTPROPERTIES_ID = PI.ID_PK;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab_info := 'TB_PART_INFO';
        SET @v_tab_property := 'TB_PROPERTY';
        SET @v_tab_new := 'MIGR_TB_PART_PROPERTIES';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_info, ' and ', @v_tab_property, ' migration started...'));

        OPEN c_part_prop;
        read_loop: LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                    BEGIN
                        GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                        CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_part_info_property for ', @v_tab_new, ' -> execute immediate error: ', @p2));
                    END;

                FETCH c_part_prop INTO name, value, type, part_info_id_fk, creation_time, created_by, modification_time,
                        modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('part_info', migration_pks, part_info_id_fk, calculated_part_info_id_fk);
                CALL MIGRATE_42_TO_50_get_tb_d_part_property_rec(name, value, type, calculated_part_info_property_fk);

                -- TODO check if AUTOINCREMENT is fine
                INSERT INTO MIGR_TB_PART_PROPERTIES (PART_INFO_ID_FK, PART_INFO_PROPERTY_FK, CREATION_TIME, CREATED_BY,
                        MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_part_info_id_fk,
                    calculated_part_info_property_fk,
                    creation_time,
                    created_by,
                    modification_time,
                    modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_part_prop;
    END
//

/**- TB_ERROR_LOG data migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_error_log(INOUT migration_pks JSON)
    BEGIN
        DECLARE created_by VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE error_code VARCHAR(255);
        DECLARE error_detail VARCHAR(255);
        DECLARE error_signal_message_id VARCHAR(255);
        DECLARE id_pk BIGINT;
        DECLARE message_in_error_id VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);
        DECLARE msh_role VARCHAR(255);
        DECLARE notified TIMESTAMP;
        DECLARE time_stamp TIMESTAMP;
        DECLARE user_message_id_fk BIGINT;

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_user_message_id_fk BIGINT;
        DECLARE calculated_msh_role_id_fk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_error_log CURSOR FOR
            SELECT EL.ID_PK,
                   EL.ERROR_CODE,
                   EL.ERROR_DETAIL,
                   EL.ERROR_SIGNAL_MESSAGE_ID,
                   EL.MESSAGE_IN_ERROR_ID,
                   EL.MSH_ROLE,
                   EL.NOTIFIED,
                   EL.TIME_STAMP,
                   EL.CREATION_TIME,
                   EL.CREATED_BY,
                   EL.MODIFICATION_TIME,
                   EL.MODIFIED_BY,
                   UMMI.ID_PK USER_MESSAGE_ID_FK
            FROM TB_ERROR_LOG EL
                     LEFT JOIN
                 (SELECT MI.MESSAGE_ID, UM.ID_PK
                  FROM TB_MESSAGE_INFO MI,
                       TB_USER_MESSAGE UM
                  WHERE UM.MESSAGEINFO_ID_PK = MI.ID_PK) UMMI
                 ON EL.MESSAGE_IN_ERROR_ID = UMMI.MESSAGE_ID;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_ERROR_LOG';
        SET @v_tab_new := 'MIGR_TB_ERROR_LOG';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_error_log;
        read_loop: LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                    BEGIN
                        GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                        CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_error_log -> execute immediate error: ', @p2));
                    END;

                FETCH c_error_log INTO id_pk, error_code, error_detail, error_signal_message_id, message_in_error_id,
                        msh_role, notified, time_stamp, creation_time, created_by, modification_time, modified_by,
                        user_message_id_fk;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('error_log', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('message_info', migration_pks, user_message_id_fk, calculated_user_message_id_fk);
                CALL MIGRATE_42_TO_50_get_tb_d_msh_role_rec(msh_role, calculated_msh_role_id_fk);

                INSERT INTO MIGR_TB_ERROR_LOG (ID_PK, ERROR_CODE, ERROR_DETAIL, ERROR_SIGNAL_MESSAGE_ID,
                        MESSAGE_IN_ERROR_ID, MSH_ROLE_ID_FK, NOTIFIED, TIME_STAMP, USER_MESSAGE_ID_FK, CREATION_TIME,
                        CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        error_code,
                        error_detail,
                        error_signal_message_id,
                        message_in_error_id,
                        calculated_msh_role_id_fk,
                        notified,
                        time_stamp,
                        calculated_user_message_id_fk,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_error_log;

        -- check counts
        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

/**- TB_MESSAGE_ACKNW data migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_message_acknw(INOUT migration_pks JSON)
    BEGIN
        DECLARE acknowledge_date TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE from_value VARCHAR(255);
        DECLARE id_pk BIGINT;
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);
        DECLARE to_value VARCHAR(255);
        DECLARE user_message_id_fk BIGINT;

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_user_message_id_fk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_message_acknw CURSOR FOR
            SELECT MA.ID_PK,
                   MA.FROM_VALUE,
                   MA.TO_VALUE,
                   MA.ACKNOWLEDGE_DATE,
                   MA.CREATION_TIME,
                   MA.CREATED_BY,
                   MA.MODIFICATION_TIME,
                   MA.MODIFIED_BY,
                   UM.ID_PK USER_MESSAGE_ID_FK
            FROM
                TB_MESSAGE_ACKNW MA,
                TB_MESSAGE_INFO MI,
                TB_USER_MESSAGE UM
            WHERE UM.MESSAGEINFO_ID_PK = MI.ID_PK
              AND MI.MESSAGE_ID = MA.MESSAGE_ID;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_MESSAGE_ACKNW';
        SET @v_tab_new := 'MIGR_TB_MESSAGE_ACKNW';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_message_acknw;
        read_loop: LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                    BEGIN
                        GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                        CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_message_acknw -> execute immediate error: ', @p2));
                    END;

                FETCH c_message_acknw INTO id_pk, from_value, to_value, acknowledge_date, creation_time, created_by,
                        modification_time, modified_by, user_message_id_fk;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('message_acknw', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('user_message', migration_pks, user_message_id_fk, calculated_user_message_id_fk);

                INSERT INTO MIGR_TB_MESSAGE_ACKNW (ID_PK, FROM_VALUE, TO_VALUE, ACKNOWLEDGE_DATE, USER_MESSAGE_ID_FK,
                        CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        from_value,
                        to_value,
                        acknowledge_date,
                        calculated_user_message_id_fk,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_message_acknw;

        -- check counts
        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

/**- TB_SEND_ATTEMPT data migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_send_attempt(INOUT migration_pks JSON)

BEGIN
    DECLARE status VARCHAR(255);
    DECLARE error VARCHAR(255);
    DECLARE created_by VARCHAR(255);
    DECLARE creation_time TIMESTAMP;
    DECLARE start_date TIMESTAMP;
    DECLARE id_pk BIGINT;
    DECLARE modification_time TIMESTAMP;
    DECLARE modified_by VARCHAR(255);
    DECLARE end_date TIMESTAMP;
    DECLARE user_message_id_fk BIGINT;

    DECLARE calculated_id_pk BIGINT;
    DECLARE calculated_user_message_id_fk BIGINT;

    DECLARE done INT DEFAULT FALSE;
    DECLARE migration_status BOOLEAN;

    DECLARE c_send_attempt CURSOR FOR
        SELECT SA.ID_PK,
               SA.START_DATE,
               SA.END_DATE,
               SA.STATUS,
               SA.ERROR,
               SA.CREATION_TIME,
               SA.CREATED_BY,
               SA.MODIFICATION_TIME,
               SA.MODIFIED_BY,
               UM.ID_PK USER_MESSAGE_ID_FK
        FROM TB_SEND_ATTEMPT SA,
             TB_MESSAGE_INFO MI,
             TB_USER_MESSAGE UM
        WHERE UM.MESSAGEINFO_ID_PK = MI.ID_PK
          AND MI.MESSAGE_ID = SA.MESSAGE_ID;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

    SET @i := 0;
    SET @v_batch_no := 1;
    SET @v_tab := 'TB_SEND_ATTEMPT';
    SET @v_tab_new := 'MIGR_TB_SEND_ATTEMPT';

    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

    OPEN c_send_attempt;
    read_loop:
    LOOP
        BEGIN
            DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                        @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_send_attempt -> execute immediate error: ', @p2));
                END;

            FETCH c_send_attempt INTO id_pk, start_date, end_date, status, error, creation_time, created_by,
                    modification_time, modified_by, user_message_id_fk;

            IF done THEN
                LEAVE read_loop;
            END IF;

            SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
            CALL MIGRATE_42_TO_50_update_migration_pks('send_attempt', id_pk, calculated_id_pk, migration_pks);
            CALL MIGRATE_42_TO_50_lookup_migration_pk('user_message', migration_pks, user_message_id_fk, calculated_user_message_id_fk);

            INSERT INTO MIGR_TB_SEND_ATTEMPT (ID_PK, START_DATE, END_DATE, STATUS, ERROR, USER_MESSAGE_ID_FK,
                                              CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
            VALUES (calculated_id_pk,
                    start_date,
                    end_date,
                    status,
                    error,
                    calculated_user_message_id_fk,
                    creation_time,
                    created_by,
                    modification_time,
                    modified_by);

            SET @i = @i + 1;
            IF @i MOD @BATCH_SIZE = 0 THEN
                COMMIT;
                CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                SET @v_batch_no := @v_batch_no + 1;
            END IF;
        END;
    END LOOP read_loop;
    COMMIT;

    CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
    CLOSE c_send_attempt;

    CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
    IF migration_status THEN
        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
    END IF;
END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_action_audit(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE audit_type VARCHAR(31);
        DECLARE entity_id VARCHAR(255);
        DECLARE modification_type VARCHAR(255);
        DECLARE revision_date TIMESTAMP;
        DECLARE user_name VARCHAR(255);
        DECLARE from_queue VARCHAR(255);
        DECLARE to_queue VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_action_audit CURSOR FOR
            SELECT AA.ID_PK,
                    AA.AUDIT_TYPE,
                    AA.ENTITY_ID,
                    AA.MODIFICATION_TYPE,
                    AA.REVISION_DATE,
                    AA.USER_NAME,
                    AA.FROM_QUEUE,
                    AA.TO_QUEUE,
                    AA.CREATION_TIME,
                    AA.CREATED_BY,
                    AA.MODIFICATION_TIME,
                    AA.MODIFIED_BY
            FROM TB_ACTION_AUDIT AA;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;
        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_ACTION_AUDIT';
        SET @v_tab_new := 'MIGR_TB_ACTION_AUDIT';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_action_audit;
        read_loop:
        LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_action_audit -> execute immediate error: ', @p2));
                END;

                FETCH c_action_audit INTO id_pk, audit_type, entity_id, modification_type, revision_date, user_name,
                        from_queue, to_queue, creation_time, created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('action_audit', id_pk, calculated_id_pk, migration_pks);

                INSERT INTO MIGR_TB_ACTION_AUDIT (ID_PK, AUDIT_TYPE, ENTITY_ID, MODIFICATION_TYPE, REVISION_DATE,
                                                  USER_NAME, FROM_QUEUE, TO_QUEUE, CREATION_TIME, CREATED_BY,
                                                  MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        audit_type,
                        entity_id,
                        modification_type,
                        revision_date,
                        user_name,
                        from_queue,
                        to_queue,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_action_audit;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_alert(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE alert_type VARCHAR(50);
        DECLARE attempts_number INT;
        DECLARE max_attempts_number INT;
        DECLARE processed BIT(1);
        DECLARE processed_time TIMESTAMP;
        DECLARE reporting_time TIMESTAMP;
        DECLARE reporting_time_failure TIMESTAMP;
        DECLARE next_attempt TIMESTAMP;
        DECLARE alert_status VARCHAR(50);
        DECLARE alert_level VARCHAR(20);
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE fk_timezone_offset BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_alert CURSOR FOR
            SELECT A.ID_PK,
                    A.ALERT_TYPE,
                    A.ATTEMPTS_NUMBER,
                    A.MAX_ATTEMPTS_NUMBER,
                    A.PROCESSED,
                    A.PROCESSED_TIME,
                    A.REPORTING_TIME,
                    A.REPORTING_TIME_FAILURE,
                    A.NEXT_ATTEMPT,
                    A.ALERT_STATUS,
                    A.ALERT_LEVEL,
                    A.CREATION_TIME,
                    A.CREATED_BY,
                    A.MODIFICATION_TIME,
                    A.MODIFIED_BY
            FROM TB_ALERT A;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_ALERT';
        SET @v_tab_new := 'MIGR_TB_ALERT';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_alert;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_alert -> execute immediate error: ', @p2));
                END;

                FETCH c_alert INTO id_pk, alert_type, attempts_number, max_attempts_number, processed, processed_time,
                        reporting_time, reporting_time_failure, next_attempt, alert_status,
                        alert_level, creation_time, created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('alert', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('timezone_offset', migration_pks, 1, fk_timezone_offset);

                INSERT INTO MIGR_TB_ALERT (ID_PK, ALERT_TYPE, ATTEMPTS_NUMBER, MAX_ATTEMPTS_NUMBER, PROCESSED,
                                           PROCESSED_TIME, REPORTING_TIME, REPORTING_TIME_FAILURE, NEXT_ATTEMPT,
                                           FK_TIMEZONE_OFFSET, ALERT_STATUS, ALERT_LEVEL, CREATION_TIME, CREATED_BY,
                                           MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        alert_type,
                        attempts_number,
                        max_attempts_number,
                        processed,
                        processed_time,
                        reporting_time,
                        reporting_time_failure,
                        next_attempt,
                        fk_timezone_offset,
                        alert_status,
                        alert_level,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_alert;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_event(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE event_type VARCHAR(50);
        DECLARE reporting_time TIMESTAMP;
        DECLARE last_alert_date TIMESTAMP;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_event CURSOR FOR
        SELECT E.ID_PK,
                E.EVENT_TYPE,
                E.REPORTING_TIME,
                E.LAST_ALERT_DATE,
                E.CREATION_TIME,
                E.CREATED_BY,
                E.MODIFICATION_TIME,
                E.MODIFIED_BY
        FROM TB_EVENT E;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_EVENT';
        SET @v_tab_new := 'MIGR_TB_EVENT';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_event;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_event -> execute immediate error: ', @p2));
                END;

                FETCH c_event INTO id_pk, event_type, reporting_time, last_alert_date, creation_time, created_by,
                    modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('event', id_pk, calculated_id_pk, migration_pks);

                INSERT INTO MIGR_TB_EVENT (ID_PK, EVENT_TYPE, REPORTING_TIME, LAST_ALERT_DATE, CREATION_TIME,
                                           CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        event_type,
                        reporting_time,
                        last_alert_date,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_event;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_event_alert(INOUT migration_pks JSON)
    BEGIN
        DECLARE fk_event BIGINT;
        DECLARE fk_alert BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_fk_event BIGINT;
        DECLARE calculated_fk_alert BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_event_alert CURSOR FOR
        SELECT EA.FK_EVENT,
                EA.FK_ALERT,
                EA.CREATION_TIME,
                EA.CREATED_BY,
                EA.MODIFICATION_TIME,
                EA.MODIFIED_BY
        FROM TB_EVENT_ALERT EA;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_EVENT_ALERT';
        SET @v_tab_new := 'MIGR_TB_EVENT_ALERT';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_event_alert;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_event_alert -> execute immediate error: ', @p2));
                END;

                FETCH c_event_alert INTO fk_event, fk_alert, creation_time, created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('alert', migration_pks, fk_alert, calculated_fk_alert);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('event', migration_pks, fk_event, calculated_fk_event);

                INSERT INTO MIGR_TB_EVENT_ALERT (FK_EVENT, FK_ALERT, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_fk_event,
                        calculated_fk_alert,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_event_alert;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_event_property(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE property_type VARCHAR(50);
        DECLARE fk_event BIGINT;
        DECLARE dtype VARCHAR(31);
        DECLARE string_value VARCHAR(255);
        DECLARE date_value TIMESTAMP;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_event BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_event_property CURSOR FOR
        SELECT EA.ID_PK,
                EA.PROPERTY_TYPE,
                EA.FK_EVENT,
                EA.DTYPE,
                EA.STRING_VALUE,
                EA.DATE_VALUE,
                EA.CREATION_TIME,
                EA.CREATED_BY,
                EA.MODIFICATION_TIME,
                EA.MODIFIED_BY
        FROM TB_EVENT_PROPERTY EA;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_EVENT_PROPERTY';
        SET @v_tab_new := 'MIGR_TB_EVENT_PROPERTY';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_event_property;
        read_loop:
        LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_event_property -> execute immediate error: ', @p2));
                END;

                FETCH c_event_property INTO id_pk, property_type, fk_event, dtype, string_value, date_value,
                        creation_time, created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('event', migration_pks, fk_event, calculated_fk_event);

                INSERT INTO MIGR_TB_EVENT_PROPERTY (ID_PK, PROPERTY_TYPE, FK_EVENT, DTYPE, STRING_VALUE, DATE_VALUE,
                                                    CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        property_type,
                        calculated_fk_event,
                        dtype,
                        string_value,
                        date_value,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_event_property;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_authentication_entry(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE certificate_id VARCHAR(255);
        DECLARE username VARCHAR(255);
        DECLARE passwd VARCHAR(255);
        DECLARE auth_roles VARCHAR(255);
        DECLARE original_user VARCHAR(255);
        DECLARE backend VARCHAR(255);
        DECLARE password_change_date TIMESTAMP;
        DECLARE default_password BIT(1);
        DECLARE attempt_count INT;
        DECLARE suspension_date TIMESTAMP;
        DECLARE user_enabled BIT(1);
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_authentication_entry CURSOR FOR
        SELECT AE.ID_PK,
                AE.CERTIFICATE_ID,
                AE.USERNAME,
                AE.PASSWD,
                AE.AUTH_ROLES,
                AE.ORIGINAL_USER,
                AE.BACKEND,
                AE.PASSWORD_CHANGE_DATE,
                AE.DEFAULT_PASSWORD,
                AE.ATTEMPT_COUNT,
                AE.SUSPENSION_DATE,
                AE.USER_ENABLED,
                AE.CREATION_TIME,
                AE.CREATED_BY,
                AE.MODIFICATION_TIME,
                AE.MODIFIED_BY
        FROM TB_AUTHENTICATION_ENTRY AE;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_AUTHENTICATION_ENTRY';
        SET @v_tab_new := 'MIGR_TB_AUTHENTICATION_ENTRY';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_authentication_entry;
        read_loop:
        LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_authentication_entry -> execute immediate error: ', @p2));
                END;

                FETCH c_authentication_entry INTO id_pk, certificate_id, username, passwd, auth_roles, original_user,
                        backend, password_change_date, default_password, attempt_count, suspension_date, user_enabled,
                        creation_time, created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('authentication_entry', id_pk, calculated_id_pk, migration_pks);

                INSERT INTO MIGR_TB_AUTHENTICATION_ENTRY (ID_PK, CERTIFICATE_ID, USERNAME, PASSWD, AUTH_ROLES,
                                                          ORIGINAL_USER, BACKEND, PASSWORD_CHANGE_DATE,
                                                          DEFAULT_PASSWORD, ATTEMPT_COUNT, SUSPENSION_DATE,
                                                          USER_ENABLED, CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                          MODIFIED_BY)
                VALUES (calculated_id_pk,
                        certificate_id,
                        username,
                        passwd,
                        auth_roles,
                        original_user,
                        backend,
                        password_change_date,
                        default_password,
                        attempt_count,
                        suspension_date,
                        user_enabled,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_authentication_entry;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_plugin_user_passwd_history(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE user_id BIGINT;
        DECLARE user_password VARCHAR(255);
        DECLARE password_change_date TIMESTAMP;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_user_id BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_plugin_user_passwd_history CURSOR FOR
            SELECT PUPH.ID_PK,
                    PUPH.USER_ID,
                    PUPH.USER_PASSWORD,
                    PUPH.PASSWORD_CHANGE_DATE,
                    PUPH.CREATION_TIME,
                    PUPH.CREATED_BY,
                    PUPH.MODIFICATION_TIME,
                    PUPH.MODIFIED_BY
            FROM TB_PLUGIN_USER_PASSWD_HISTORY PUPH;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PLUGIN_USER_PASSWD_HISTORY';
        SET @v_tab_new := 'MIGR_TB_PLUGIN_USR_PASSWD_HIST';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_plugin_user_passwd_history;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_plugin_user_passwd_history -> execute immediate error: ', @p2));
                END;

                FETCH c_plugin_user_passwd_history INTO id_pk, user_id, user_password, password_change_date,
                        creation_time, created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('authentication_entry', migration_pks, user_id, calculated_user_id);

                INSERT INTO MIGR_TB_PLUGIN_USR_PASSWD_HIST (ID_PK, USER_ID, USER_PASSWORD, PASSWORD_CHANGE_DATE,
                                                                CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                                MODIFIED_BY)
                VALUES (calculated_id_pk,
                        calculated_user_id,
                        user_password,
                        password_change_date,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_plugin_user_passwd_history;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_backend_filter(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE backend_name VARCHAR(255);
        DECLARE priority INT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_backend_filter CURSOR FOR
            SELECT BF.ID_PK,
                    BF.BACKEND_NAME,
                    BF.PRIORITY,
                    BF.CREATION_TIME,
                    BF.CREATED_BY,
                    BF.MODIFICATION_TIME,
                    BF.MODIFIED_BY
            FROM TB_BACKEND_FILTER BF;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_BACKEND_FILTER';
        SET @v_tab_new := 'MIGR_TB_BACKEND_FILTER';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_backend_filter;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_backend_filter -> execute immediate error: ', @p2));
                END;

                FETCH c_backend_filter INTO id_pk, backend_name, priority, creation_time, created_by,
                        modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('backend_filter', id_pk, calculated_id_pk, migration_pks);

                INSERT INTO MIGR_TB_BACKEND_FILTER (ID_PK, BACKEND_NAME, PRIORITY, CREATION_TIME, CREATED_BY,
                                                    MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        backend_name,
                        priority,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_backend_filter;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_routing_criteria(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE expression VARCHAR(255);
        DECLARE name VARCHAR(255);
        DECLARE fk_backend_filter BIGINT;
        DECLARE priority INT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_backend_filter BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_routing_criteria CURSOR FOR
            SELECT RC.ID_PK,
                    RC.EXPRESSION,
                    RC.NAME,
                    RC.FK_BACKEND_FILTER,
                    RC.PRIORITY,
                    RC.CREATION_TIME,
                    RC.CREATED_BY,
                    RC.MODIFICATION_TIME,
                    RC.MODIFIED_BY
            FROM TB_ROUTING_CRITERIA RC;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_ROUTING_CRITERIA';
        SET @v_tab_new := 'MIGR_TB_ROUTING_CRITERIA';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_routing_criteria;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_routing_criteria -> execute immediate error: ', @p2));
                END;

                FETCH c_routing_criteria INTO id_pk, expression, name, fk_backend_filter, priority, creation_time,
                        created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('routing_criteria', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('backend_filter', migration_pks, fk_backend_filter, calculated_fk_backend_filter);

                INSERT INTO MIGR_TB_ROUTING_CRITERIA (ID_PK, EXPRESSION, NAME, FK_BACKEND_FILTER, PRIORITY,
                                                      CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        expression,
                        name,
                        calculated_fk_backend_filter,
                        priority,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_routing_criteria;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_certificate(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE certificate_alias VARCHAR(255);
        DECLARE not_valid_before_date DATETIME;
        DECLARE not_valid_after_date DATETIME;
        DECLARE revoke_notification_date TIMESTAMP;
        DECLARE alert_imm_notification_date TIMESTAMP;
        DECLARE alert_exp_notification_date TIMESTAMP;
        DECLARE certificate_status VARCHAR(255);
        DECLARE certificate_type VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_certificate CURSOR FOR
            SELECT C.ID_PK,
                    C.CERTIFICATE_ALIAS,
                    C.NOT_VALID_BEFORE_DATE,
                    C.NOT_VALID_AFTER_DATE,
                    C.REVOKE_NOTIFICATION_DATE,
                    C.ALERT_IMM_NOTIFICATION_DATE,
                    C.ALERT_EXP_NOTIFICATION_DATE,
                    C.CERTIFICATE_STATUS,
                    C.CERTIFICATE_TYPE,
                    C.CREATION_TIME,
                    C.CREATED_BY,
                    C.MODIFICATION_TIME,
                    C.MODIFIED_BY
            FROM TB_CERTIFICATE C;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_CERTIFICATE';
        SET @v_tab_new := 'MIGR_TB_CERTIFICATE';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_certificate;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_certificate -> execute immediate error: ', @p2));
                END;

                FETCH c_certificate INTO id_pk, certificate_alias, not_valid_before_date, not_valid_after_date,
                        revoke_notification_date, alert_imm_notification_date, alert_exp_notification_date,
                        certificate_status, certificate_type, creation_time, created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('certificate', id_pk, calculated_id_pk, migration_pks);

                INSERT INTO MIGR_TB_CERTIFICATE (ID_PK, CERTIFICATE_ALIAS, NOT_VALID_BEFORE_DATE, NOT_VALID_AFTER_DATE,
                                                 REVOKE_NOTIFICATION_DATE, ALERT_IMM_NOTIFICATION_DATE,
                                                 ALERT_EXP_NOTIFICATION_DATE, CERTIFICATE_STATUS, CERTIFICATE_TYPE,
                                                 CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        certificate_alias,
                        not_valid_before_date,
                        not_valid_after_date,
                        revoke_notification_date,
                        alert_imm_notification_date,
                        alert_exp_notification_date,
                        certificate_status,
                        certificate_type,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_certificate;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_command(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE server_name VARCHAR(255);
        DECLARE command_name VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_command CURSOR FOR
            SELECT C.ID_PK,
                    C.SERVER_NAME,
                    C.COMMAND_NAME,
                    C.CREATION_TIME,
                    C.CREATED_BY,
                    C.MODIFICATION_TIME,
                    C.MODIFIED_BY
            FROM TB_COMMAND C;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_COMMAND';
        SET @v_tab_new := 'MIGR_TB_COMMAND';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_command;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_command -> execute immediate error: ', @p2));
                END;

                FETCH c_command INTO id_pk, server_name, command_name, creation_time, created_by, modification_time,
                        modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('command', id_pk, calculated_id_pk, migration_pks);

                INSERT INTO MIGR_TB_COMMAND (ID_PK, SERVER_NAME, COMMAND_NAME, CREATION_TIME, CREATED_BY,
                                             MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        server_name,
                        command_name,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_command;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_command_property(INOUT migration_pks JSON)
    BEGIN
        DECLARE property_name VARCHAR(50);
        DECLARE property_value VARCHAR(255);
        DECLARE fk_command BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_fk_command BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_command_property CURSOR FOR
            SELECT CP.PROPERTY_NAME,
                    CP.PROPERTY_VALUE,
                    CP.FK_COMMAND,
                    CP.CREATION_TIME,
                    CP.CREATED_BY,
                    CP.MODIFICATION_TIME,
                    CP.MODIFIED_BY
            FROM TB_COMMAND_PROPERTY CP;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_COMMAND_PROPERTY';
        SET @v_tab_new := 'MIGR_TB_COMMAND_PROPERTY';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_command_property;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_command_property -> execute immediate error: ', @p2));
                END;

                FETCH c_command_property INTO property_name, property_value, fk_command, creation_time, created_by,
                        modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('command', migration_pks, fk_command, calculated_fk_command);

                INSERT INTO MIGR_TB_COMMAND_PROPERTY (PROPERTY_NAME, PROPERTY_VALUE, FK_COMMAND, CREATION_TIME,
                                                      CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (property_name,
                        property_value,
                        calculated_fk_command,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_command_property;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_encryption_key()
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE key_usage VARCHAR(255);
        DECLARE secret_key LONGBLOB;
        DECLARE init_vector LONGBLOB;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_encryption_key CURSOR FOR
            SELECT EK.ID_PK,
                    EK.KEY_USAGE,
                    EK.SECRET_KEY,
                    EK.INIT_VECTOR,
                    EK.CREATION_TIME,
                    EK.CREATED_BY,
                    EK.MODIFICATION_TIME,
                    EK.MODIFIED_BY
            FROM TB_ENCRYPTION_KEY EK;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_ENCRYPTION_KEY';
        SET @v_tab_new := 'MIGR_TB_ENCRYPTION_KEY';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_encryption_key;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_encryption_key -> execute immediate error: ', @p2));
                END;

                FETCH c_encryption_key INTO id_pk, key_usage, secret_key, init_vector, creation_time, created_by,
                        modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);

                INSERT INTO MIGR_TB_ENCRYPTION_KEY (ID_PK, KEY_USAGE, SECRET_KEY, INIT_VECTOR, CREATION_TIME,
                                                    CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        key_usage,
                        CAST(secret_key AS BINARY),
                        CAST(init_vector AS BINARY),
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_encryption_key;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_message_acknw_prop(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE property_name VARCHAR(255);
        DECLARE property_value VARCHAR(255);
        DECLARE fk_msg_acknowledge BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_msg_acknowledge BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_message_acknw_prop CURSOR FOR
            SELECT MAP.ID_PK,
                    MAP.PROPERTY_NAME,
                    MAP.PROPERTY_VALUE,
                    MAP.FK_MSG_ACKNOWLEDGE,
                    MAP.CREATION_TIME,
                    MAP.CREATED_BY,
                    MAP.MODIFICATION_TIME,
                    MAP.MODIFIED_BY
            FROM TB_MESSAGE_ACKNW_PROP MAP;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_MESSAGE_ACKNW_PROP';
        SET @v_tab_new := 'MIGR_TB_MESSAGE_ACKNW_PROP';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_message_acknw_prop;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_message_acknw_prop -> execute immediate error: ', @p2));
                END;

                FETCH c_message_acknw_prop INTO id_pk, property_name, property_value, fk_msg_acknowledge,
                            creation_time, created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('message_acknw', migration_pks, fk_msg_acknowledge, calculated_fk_msg_acknowledge);

                INSERT INTO MIGR_TB_MESSAGE_ACKNW_PROP (ID_PK, PROPERTY_NAME, PROPERTY_VALUE, FK_MSG_ACKNOWLEDGE,
                                                        CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        property_name,
                        property_value,
                        calculated_fk_msg_acknowledge,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_message_acknw_prop;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_messaging_lock(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE message_type VARCHAR(10);
        DECLARE message_received TIMESTAMP;
        DECLARE message_state VARCHAR(10);
        DECLARE message_id VARCHAR(255);
        DECLARE initiator VARCHAR(255);
        DECLARE mpc VARCHAR(255);
        DECLARE send_attempts INT;
        DECLARE send_attempts_max INT;
        DECLARE next_attempt TIMESTAMP;
        DECLARE message_staled TIMESTAMP;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE fk_timezone_offset BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_messaging_lock CURSOR FOR
            SELECT ML.ID_PK,
                    ML.MESSAGE_TYPE,
                    ML.MESSAGE_RECEIVED,
                    ML.MESSAGE_STATE,
                    ML.MESSAGE_ID,
                    ML.INITIATOR,
                    ML.MPC,
                    ML.SEND_ATTEMPTS,
                    ML.SEND_ATTEMPTS_MAX,
                    ML.NEXT_ATTEMPT,
                    ML.MESSAGE_STALED,
                    ML.CREATION_TIME,
                    ML.CREATED_BY,
                    ML.MODIFICATION_TIME,
                    ML.MODIFIED_BY
            FROM TB_MESSAGING_LOCK ML;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_MESSAGING_LOCK';
        SET @v_tab_new := 'MIGR_TB_MESSAGING_LOCK';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_messaging_lock;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_messaging_lock -> execute immediate error: ', @p2));
                END;

                FETCH c_messaging_lock INTO id_pk, message_type, message_received, message_state, message_id,
                        initiator, mpc, send_attempts, send_attempts_max, next_attempt, message_staled,
                        creation_time, created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('timezone_offset', migration_pks, 1, fk_timezone_offset);

                INSERT INTO MIGR_TB_MESSAGING_LOCK (ID_PK, MESSAGE_TYPE, MESSAGE_RECEIVED, MESSAGE_STATE, MESSAGE_ID,
                                                    INITIATOR, MPC, SEND_ATTEMPTS, SEND_ATTEMPTS_MAX, NEXT_ATTEMPT,
                                                    FK_TIMEZONE_OFFSET, MESSAGE_STALED, CREATION_TIME, CREATED_BY,
                                                    MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        message_type,
                        message_received,
                        message_state,
                        message_id,
                        initiator,
                        mpc,
                        send_attempts,
                        send_attempts_max,
                        next_attempt,
                        fk_timezone_offset,
                        message_staled,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_messaging_lock;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_business_process(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_business_process CURSOR FOR
            SELECT PBP.ID_PK,
                    PBP.CREATION_TIME,
                    PBP.CREATED_BY,
                    PBP.MODIFICATION_TIME,
                    PBP.MODIFIED_BY
            FROM TB_PM_BUSINESS_PROCESS PBP;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_BUSINESS_PROCESS';
        SET @v_tab_new := 'MIGR_TB_PM_BUSINESS_PROCESS';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_business_process;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_business_process -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_business_process INTO id_pk, creation_time, created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_business_process', id_pk, calculated_id_pk, migration_pks);

                INSERT INTO MIGR_TB_PM_BUSINESS_PROCESS (ID_PK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                         MODIFIED_BY)
                VALUES (calculated_id_pk,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_business_process;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_action(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE name VARCHAR(255);
        DECLARE value VARCHAR(255);
        DECLARE fk_businessprocess BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_businessprocess BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_action CURSOR FOR
            SELECT PA.ID_PK,
                    PA.NAME,
                    PA.VALUE,
                    PA.FK_BUSINESSPROCESS,
                    PA.CREATION_TIME,
                    PA.CREATED_BY,
                    PA.MODIFICATION_TIME,
                    PA.MODIFIED_BY
            FROM TB_PM_ACTION PA;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_ACTION';
        SET @v_tab_new := 'MIGR_TB_PM_ACTION';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_action;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_action -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_action INTO id_pk, name, value, fk_businessprocess, creation_time, created_by,
                        modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_action', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_business_process', migration_pks, fk_businessprocess, calculated_fk_businessprocess);

                INSERT INTO MIGR_TB_PM_ACTION (ID_PK, NAME, VALUE, FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY,
                                               MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        name,
                        value,
                        calculated_fk_businessprocess,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_action;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_agreement(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE name VARCHAR(255);
        DECLARE type VARCHAR(255);
        DECLARE value VARCHAR(255);
        DECLARE fk_businessprocess BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_businessprocess BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_agreement CURSOR FOR
            SELECT PA.ID_PK,
                    PA.NAME,
                    PA.TYPE,
                    PA.VALUE,
                    PA.FK_BUSINESSPROCESS,
                    PA.CREATION_TIME,
                    PA.CREATED_BY,
                    PA.MODIFICATION_TIME,
                    PA.MODIFIED_BY
            FROM TB_PM_AGREEMENT PA;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_AGREEMENT';
        SET @v_tab_new := 'MIGR_TB_PM_AGREEMENT';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_agreement;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_agreement -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_agreement INTO id_pk, name, type, value, fk_businessprocess, creation_time, created_by,
                        modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_agreement', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_business_process', migration_pks, fk_businessprocess, calculated_fk_businessprocess);

                INSERT INTO MIGR_TB_PM_AGREEMENT (ID_PK, NAME, TYPE, VALUE, FK_BUSINESSPROCESS, CREATION_TIME,
                                                  CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        name,
                        type,
                        value,
                        calculated_fk_businessprocess,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_agreement;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_error_handling(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE business_error_notify_consumer BIT(1);
        DECLARE business_error_notify_producer BIT(1);
        DECLARE delivery_fail_notify_producer BIT(1);
        DECLARE error_as_response BIT(1);
        DECLARE name VARCHAR(255);
        DECLARE fk_businessprocess BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_businessprocess BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_error_handling CURSOR FOR
            SELECT PEH.ID_PK,
                    PEH.BUSINESS_ERROR_NOTIFY_CONSUMER,
                    PEH.BUSINESS_ERROR_NOTIFY_PRODUCER,
                    PEH.DELIVERY_FAIL_NOTIFY_PRODUCER,
                    PEH.ERROR_AS_RESPONSE,
                    PEH.NAME,
                    PEH.FK_BUSINESSPROCESS,
                    PEH.CREATION_TIME,
                    PEH.CREATED_BY,
                    PEH.MODIFICATION_TIME,
                    PEH.MODIFIED_BY
            FROM TB_PM_ERROR_HANDLING PEH;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_ERROR_HANDLING';
        SET @v_tab_new := 'MIGR_TB_PM_ERROR_HANDLING';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_error_handling;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_error_handling -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_error_handling INTO id_pk, business_error_notify_consumer, business_error_notify_producer,
                        delivery_fail_notify_producer, error_as_response, name, fk_businessprocess, creation_time,
                        created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_error_handling', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_business_process', migration_pks, fk_businessprocess, calculated_fk_businessprocess);

                INSERT INTO MIGR_TB_PM_ERROR_HANDLING (ID_PK, BUSINESS_ERROR_NOTIFY_CONSUMER,
                                                       BUSINESS_ERROR_NOTIFY_PRODUCER, DELIVERY_FAIL_NOTIFY_PRODUCER,
                                                       ERROR_AS_RESPONSE, NAME, FK_BUSINESSPROCESS, CREATION_TIME,
                                                       CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        business_error_notify_consumer,
                        business_error_notify_producer,
                        delivery_fail_notify_producer,
                        error_as_response,
                        name,
                        calculated_fk_businessprocess,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_error_handling;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_mep(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE leg_count INT;
        DECLARE name VARCHAR(255);
        DECLARE value VARCHAR(1024);
        DECLARE fk_businessprocess BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_businessprocess BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_mep CURSOR FOR
            SELECT PM.ID_PK,
                    PM.LEG_COUNT,
                    PM.NAME,
                    PM.VALUE,
                    PM.FK_BUSINESSPROCESS,
                    PM.CREATION_TIME,
                    PM.CREATED_BY,
                    PM.MODIFICATION_TIME,
                    PM.MODIFIED_BY
            FROM TB_PM_MEP PM;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_MEP';
        SET @v_tab_new := 'MIGR_TB_PM_MEP';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_mep;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_mep -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_mep INTO id_pk, leg_count, name, value, fk_businessprocess, creation_time, created_by,
                        modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_mep', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_business_process', migration_pks, fk_businessprocess, calculated_fk_businessprocess);

                INSERT INTO MIGR_TB_PM_MEP (ID_PK, LEG_COUNT, NAME, VALUE, FK_BUSINESSPROCESS, CREATION_TIME,
                                            CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        leg_count,
                        name,
                        value,
                        calculated_fk_businessprocess,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_mep;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_mep_binding(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE name VARCHAR(255);
        DECLARE value VARCHAR(1024);
        DECLARE fk_businessprocess BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_businessprocess BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_mep_binding CURSOR FOR
            SELECT PMB.ID_PK,
                    PMB.NAME,
                    PMB.VALUE,
                    PMB.FK_BUSINESSPROCESS,
                    PMB.CREATION_TIME,
                    PMB.CREATED_BY,
                    PMB.MODIFICATION_TIME,
                    PMB.MODIFIED_BY
            FROM TB_PM_MEP_BINDING PMB;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_MEP_BINDING';
        SET @v_tab_new := 'MIGR_TB_PM_MEP_BINDING';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_mep_binding;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_mep_binding -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_mep_binding INTO id_pk, name, value, fk_businessprocess, creation_time, created_by,
                        modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_mep_binding', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_business_process', migration_pks, fk_businessprocess, calculated_fk_businessprocess);

                INSERT INTO MIGR_TB_PM_MEP_BINDING (ID_PK, NAME, VALUE, FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY,
                                                    MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        name,
                        value,
                        calculated_fk_businessprocess,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_mep_binding;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_message_property(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE datatype VARCHAR(255);
        DECLARE key_ VARCHAR(255);
        DECLARE name VARCHAR(255);
        DECLARE required_ BIT(1);
        DECLARE fk_businessprocess BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_businessprocess BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_message_property CURSOR FOR
            SELECT PMP.ID_PK,
                    PMP.DATATYPE,
                    PMP.KEY_,
                    PMP.NAME,
                    PMP.REQUIRED_,
                    PMP.FK_BUSINESSPROCESS,
                    PMP.CREATION_TIME,
                    PMP.CREATED_BY,
                    PMP.MODIFICATION_TIME,
                    PMP.MODIFIED_BY
            FROM TB_PM_MESSAGE_PROPERTY PMP;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_MESSAGE_PROPERTY';
        SET @v_tab_new := 'MIGR_TB_PM_MESSAGE_PROPERTY';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_message_property;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_message_property -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_message_property INTO id_pk, datatype, key_, name, required_, fk_businessprocess,
                        creation_time, created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_message_property', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_business_process', migration_pks, fk_businessprocess, calculated_fk_businessprocess);

                INSERT INTO MIGR_TB_PM_MESSAGE_PROPERTY (ID_PK, DATATYPE, KEY_, NAME, REQUIRED_, FK_BUSINESSPROCESS,
                                                         CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        datatype,
                        key_,
                        name,
                        required_,
                        calculated_fk_businessprocess,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_message_property;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_message_property_set(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE name VARCHAR(255);
        DECLARE fk_businessprocess BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_businessprocess BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_message_property_set CURSOR FOR
            SELECT PMPS.ID_PK,
                    PMPS.NAME,
                    PMPS.FK_BUSINESSPROCESS,
                    PMPS.CREATION_TIME,
                    PMPS.CREATED_BY,
                    PMPS.MODIFICATION_TIME,
                    PMPS.MODIFIED_BY
            FROM TB_PM_MESSAGE_PROPERTY_SET PMPS;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_MESSAGE_PROPERTY_SET';
        SET @v_tab_new := 'MIGR_TB_PM_MSG_PROPERTY_SET';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_message_property_set;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_message_property_set -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_message_property_set INTO id_pk, name, fk_businessprocess, creation_time, created_by,
                        modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_message_property_set', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_business_process', migration_pks, fk_businessprocess, calculated_fk_businessprocess);

                INSERT INTO MIGR_TB_PM_MSG_PROPERTY_SET (ID_PK, NAME, FK_BUSINESSPROCESS, CREATION_TIME,
                                                             CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        name,
                        calculated_fk_businessprocess,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_message_property_set;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_join_property_set(INOUT migration_pks JSON)
    BEGIN
        DECLARE property_fk BIGINT;
        DECLARE set_fk BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_property_fk BIGINT;
        DECLARE calculated_set_fk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_join_property_set CURSOR FOR
            SELECT PJPS.PROPERTY_FK,
                    PJPS.SET_FK,
                    PJPS.CREATION_TIME,
                    PJPS.CREATED_BY,
                    PJPS.MODIFICATION_TIME,
                    PJPS.MODIFIED_BY
            FROM TB_PM_JOIN_PROPERTY_SET PJPS;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_JOIN_PROPERTY_SET';
        SET @v_tab_new := 'MIGR_TB_PM_JOIN_PROPERTY_SET';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_join_property_set;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_join_property_set -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_join_property_set INTO property_fk, set_fk, creation_time, created_by, modification_time,
                        modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                -- FKs are inverse here so we need to pass the other FK value when doing the lookups
                -- (i.e PROPERTY_FK for pm_message_property_set and SET_FK for pm_message_property)
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_message_property_set', migration_pks, property_fk, calculated_property_fk);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_message_property', migration_pks, set_fk, calculated_set_fk);

                INSERT INTO MIGR_TB_PM_JOIN_PROPERTY_SET (PROPERTY_FK, SET_FK, CREATION_TIME, CREATED_BY,
                                                          MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_property_fk,
                        calculated_set_fk,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_join_property_set;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_party(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE endpoint VARCHAR(1024);
        DECLARE name VARCHAR(255);
        DECLARE password VARCHAR(255);
        DECLARE username VARCHAR(255);
        DECLARE fk_businessprocess BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_businessprocess BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_party CURSOR FOR
            SELECT PP.ID_PK,
                    PP.ENDPOINT,
                    PP.NAME,
                    PP.PASSWORD,
                    PP.USERNAME,
                    PP.FK_BUSINESSPROCESS,
                    PP.CREATION_TIME,
                    PP.CREATED_BY,
                    PP.MODIFICATION_TIME,
                    PP.MODIFIED_BY
            FROM TB_PM_PARTY PP;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_PARTY';
        SET @v_tab_new := 'MIGR_TB_PM_PARTY';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_party;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_party -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_party INTO id_pk, endpoint, name, password, username, fk_businessprocess, creation_time,
                        created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_party', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_business_process', migration_pks, fk_businessprocess, calculated_fk_businessprocess);

                INSERT INTO MIGR_TB_PM_PARTY (ID_PK, ENDPOINT, NAME, PASSWORD, USERNAME, FK_BUSINESSPROCESS,
                                              CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        endpoint,
                        name,
                        password,
                        username,
                        calculated_fk_businessprocess,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_party;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_configuration(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE fk_businessprocesses BIGINT;
        DECLARE fk_party BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_businessprocess BIGINT;
        DECLARE calculated_fk_party BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_configuration CURSOR FOR
            SELECT PC.ID_PK,
                    PC.FK_BUSINESSPROCESSES,
                    PC.FK_PARTY,
                    PC.CREATION_TIME,
                    PC.CREATED_BY,
                    PC.MODIFICATION_TIME,
                    PC.MODIFIED_BY
            FROM TB_PM_CONFIGURATION PC;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_CONFIGURATION';
        SET @v_tab_new := 'MIGR_TB_PM_CONFIGURATION';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_configuration;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_configuration -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_configuration INTO id_pk, fk_businessprocesses, fk_party, creation_time, created_by,
                        modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_configuration', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_business_process', migration_pks, fk_businessprocesses, calculated_fk_businessprocess);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_party', migration_pks, fk_party, calculated_fk_party);

                INSERT INTO MIGR_TB_PM_CONFIGURATION (ID_PK, FK_BUSINESSPROCESSES, FK_PARTY, CREATION_TIME, CREATED_BY,
                                                      MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        calculated_fk_businessprocess,
                        calculated_fk_party,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_configuration;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_mpc(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE default_mpc BIT(1);
        DECLARE is_enabled BIT(1);
        DECLARE name VARCHAR(255);
        DECLARE qualified_name VARCHAR(255);
        DECLARE retention_downloaded INT;
        DECLARE retention_undownloaded INT;
        DECLARE retention_sent INT;
        DECLARE delete_message_metadata BIT(1);
        DECLARE max_batch_delete INT;
        DECLARE fk_configuration BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_configuration BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_mpc CURSOR FOR
            SELECT PM.ID_PK,
                    PM.DEFAULT_MPC,
                    PM.IS_ENABLED,
                    PM.NAME,
                    PM.QUALIFIED_NAME,
                    PM.RETENTION_DOWNLOADED,
                    PM.RETENTION_UNDOWNLOADED,
                    PM.RETENTION_SENT,
                    PM.DELETE_MESSAGE_METADATA,
                    PM.MAX_BATCH_DELETE,
                    PM.FK_CONFIGURATION,
                    PM.CREATION_TIME,
                    PM.CREATED_BY,
                    PM.MODIFICATION_TIME,
                    PM.MODIFIED_BY
            FROM TB_PM_MPC PM;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_MPC';
        SET @v_tab_new := 'MIGR_TB_PM_MPC';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_mpc;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_mpc -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_mpc INTO id_pk, default_mpc, is_enabled, name, qualified_name, retention_downloaded,
                        retention_undownloaded, retention_sent, delete_message_metadata, max_batch_delete,
                        fk_configuration, creation_time, created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_mpc', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_configuration', migration_pks, fk_configuration, calculated_fk_configuration);

                INSERT INTO MIGR_TB_PM_MPC (ID_PK, DEFAULT_MPC, IS_ENABLED, NAME, QUALIFIED_NAME, RETENTION_DOWNLOADED,
                                            RETENTION_UNDOWNLOADED, RETENTION_SENT, DELETE_MESSAGE_METADATA,
                                            MAX_BATCH_DELETE, FK_CONFIGURATION, CREATION_TIME, CREATED_BY,
                                            MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        default_mpc,
                        is_enabled,
                        name,
                        qualified_name,
                        retention_downloaded,
                        retention_undownloaded,
                        retention_sent,
                        delete_message_metadata,
                        max_batch_delete,
                        calculated_fk_configuration,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_mpc;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_party_id_type(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE name VARCHAR(255);
        DECLARE value VARCHAR(1024);
        DECLARE fk_businessprocess BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_businessprocess BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_party_id_type CURSOR FOR
            SELECT PPIT.ID_PK,
                    PPIT.NAME,
                    PPIT.VALUE,
                    PPIT.FK_BUSINESSPROCESS,
                    PPIT.CREATION_TIME,
                    PPIT.CREATED_BY,
                    PPIT.MODIFICATION_TIME,
                    PPIT.MODIFIED_BY
            FROM TB_PM_PARTY_ID_TYPE PPIT;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_PARTY_ID_TYPE';
        SET @v_tab_new := 'MIGR_TB_PM_PARTY_ID_TYPE';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_party_id_type;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_party_id_type -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_party_id_type INTO id_pk, name, value, fk_businessprocess, creation_time, created_by,
                        modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_party_id_type', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_business_process', migration_pks, fk_businessprocess, calculated_fk_businessprocess);

                INSERT INTO MIGR_TB_PM_PARTY_ID_TYPE (ID_PK, NAME, VALUE, FK_BUSINESSPROCESS, CREATION_TIME,
                                                      CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        name,
                        value,
                        calculated_fk_businessprocess,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_party_id_type;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_party_identifier(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE party_id VARCHAR(255);
        DECLARE fk_party_id_type BIGINT;
        DECLARE fk_party BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_party_id_type BIGINT;
        DECLARE calculated_fk_party BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_party_identifier CURSOR FOR
            SELECT PPI.ID_PK,
                    PPI.PARTY_ID,
                    PPI.FK_PARTY_ID_TYPE,
                    PPI.FK_PARTY,
                    PPI.CREATION_TIME,
                    PPI.CREATED_BY,
                    PPI.MODIFICATION_TIME,
                    PPI.MODIFIED_BY
            FROM TB_PM_PARTY_IDENTIFIER PPI;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_PARTY_IDENTIFIER';
        SET @v_tab_new := 'MIGR_TB_PM_PARTY_IDENTIFIER';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_party_identifier;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_party_identifier -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_party_identifier INTO id_pk, party_id, fk_party_id_type, fk_party, creation_time,
                        created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_party_identifier', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_party_id_type', migration_pks, fk_party_id_type, calculated_fk_party_id_type);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_party', migration_pks, fk_party, calculated_fk_party);

                INSERT INTO MIGR_TB_PM_PARTY_IDENTIFIER (ID_PK, PARTY_ID, FK_PARTY_ID_TYPE, FK_PARTY, CREATION_TIME,
                                                         CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        party_id,
                        calculated_fk_party_id_type,
                        calculated_fk_party,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_party_identifier;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_payload(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE cid VARCHAR(255);
        DECLARE in_body BIT(1);
        DECLARE max_size INT;
        DECLARE mime_type VARCHAR(255);
        DECLARE name VARCHAR(255);
        DECLARE required_ BIT(1);
        DECLARE schema_file VARCHAR(1024);
        DECLARE fk_businessprocess BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_businessprocess BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_payload CURSOR FOR
            SELECT PP.ID_PK,
                    PP.CID,
                    PP.IN_BODY,
                    PP.MAX_SIZE,
                    PP.MIME_TYPE,
                    PP.NAME,
                    PP.REQUIRED_,
                    PP.SCHEMA_FILE,
                    PP.FK_BUSINESSPROCESS,
                    PP.CREATION_TIME,
                    PP.CREATED_BY,
                    PP.MODIFICATION_TIME,
                    PP.MODIFIED_BY
            FROM TB_PM_PAYLOAD PP;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_PAYLOAD';
        SET @v_tab_new := 'MIGR_TB_PM_PAYLOAD';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_payload;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_payload -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_payload INTO id_pk, cid, in_body, max_size, mime_type, name, required_, schema_file,
                        fk_businessprocess, creation_time, created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_payload', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_business_process', migration_pks, fk_businessprocess, calculated_fk_businessprocess);

                INSERT INTO MIGR_TB_PM_PAYLOAD (ID_PK, CID, IN_BODY, MAX_SIZE, MIME_TYPE, NAME, REQUIRED_, SCHEMA_FILE,
                                                FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                MODIFIED_BY)
                VALUES (calculated_id_pk,
                        cid,
                        in_body,
                        max_size,
                        mime_type,
                        name,
                        required_,
                        schema_file,
                        calculated_fk_businessprocess,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_payload;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_payload_profile(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE max_size BIGINT;
        DECLARE name VARCHAR(255);
        DECLARE fk_businessprocess BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_businessprocess BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_payload_profile CURSOR FOR
            SELECT PPP.ID_PK,
                    PPP.MAX_SIZE,
                    PPP.NAME,
                    PPP.FK_BUSINESSPROCESS,
                    PPP.CREATION_TIME,
                    PPP.CREATED_BY,
                    PPP.MODIFICATION_TIME,
                    PPP.MODIFIED_BY
            FROM TB_PM_PAYLOAD_PROFILE PPP;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_PAYLOAD_PROFILE';
        SET @v_tab_new := 'MIGR_TB_PM_PAYLOAD_PROFILE';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_payload_profile;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_payload_profile -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_payload_profile INTO id_pk, max_size, name, fk_businessprocess, creation_time, created_by,
                        modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_payload_profile', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_business_process', migration_pks, fk_businessprocess, calculated_fk_businessprocess);

                INSERT INTO MIGR_TB_PM_PAYLOAD_PROFILE (ID_PK, MAX_SIZE, NAME, FK_BUSINESSPROCESS, CREATION_TIME,
                                                        CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        max_size,
                        name,
                        calculated_fk_businessprocess,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_payload_profile;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_join_payload_profile(INOUT migration_pks JSON)
    BEGIN
        DECLARE fk_payload BIGINT;
        DECLARE fk_profile BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_fk_payload BIGINT;
        DECLARE calculated_fk_profile BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_join_payload_profile CURSOR FOR
            SELECT PJPP.FK_PAYLOAD,
                    PJPP.FK_PROFILE,
                    PJPP.CREATION_TIME,
                    PJPP.CREATED_BY,
                    PJPP.MODIFICATION_TIME,
                    PJPP.MODIFIED_BY
            FROM TB_PM_JOIN_PAYLOAD_PROFILE PJPP;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_JOIN_PAYLOAD_PROFILE';
        SET @v_tab_new := 'MIGR_TB_PM_JOIN_PAYLD_PROFILE';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_join_payload_profile;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_join_payload_profile -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_join_payload_profile INTO fk_payload, fk_profile, creation_time, created_by,
                        modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                -- FKs are inverse here so we need to pass the other FK value when doing the lookups
                -- (i.e FK_PAYLOAD for pm_payload_profile and FK_PROFILE for pm_payload)
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_payload_profile', migration_pks, fk_payload, calculated_fk_payload);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_payload', migration_pks, fk_profile, calculated_fk_profile);

                INSERT INTO MIGR_TB_PM_JOIN_PAYLD_PROFILE (FK_PAYLOAD, FK_PROFILE, CREATION_TIME, CREATED_BY,
                                                             MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_fk_payload,
                        calculated_fk_profile,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_join_payload_profile;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_reception_awareness(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE duplicate_detection BIT(1);
        DECLARE name VARCHAR(255);
        DECLARE retry_count INT;
        DECLARE retry_timeout INT;
        DECLARE strategy VARCHAR(255);
        DECLARE fk_businessprocess BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_businessprocess BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_reception_awareness CURSOR FOR
            SELECT PRA.ID_PK,
                    PRA.DUPLICATE_DETECTION,
                    PRA.NAME,
                    PRA.RETRY_COUNT,
                    PRA.RETRY_TIMEOUT,
                    PRA.STRATEGY,
                    PRA.FK_BUSINESSPROCESS,
                    PRA.CREATION_TIME,
                    PRA.CREATED_BY,
                    PRA.MODIFICATION_TIME,
                    PRA.MODIFIED_BY
            FROM TB_PM_RECEPTION_AWARENESS PRA;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_RECEPTION_AWARENESS';
        SET @v_tab_new := 'MIGR_TB_PM_RECEPTION_AWARENESS';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_reception_awareness;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_reception_awareness -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_reception_awareness INTO id_pk, duplicate_detection, name, retry_count, retry_timeout,
                        strategy, fk_businessprocess, creation_time, created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_reception_awareness', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_business_process', migration_pks, fk_businessprocess, calculated_fk_businessprocess);

                INSERT INTO MIGR_TB_PM_RECEPTION_AWARENESS (ID_PK, DUPLICATE_DETECTION, NAME, RETRY_COUNT,
                                                            RETRY_TIMEOUT, STRATEGY, FK_BUSINESSPROCESS, CREATION_TIME,
                                                            CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        duplicate_detection,
                        name,
                        retry_count,
                        retry_timeout,
                        strategy,
                        calculated_fk_businessprocess,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_reception_awareness;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_reliability(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE name VARCHAR(255);
        DECLARE non_repudiation BIT(1);
        DECLARE reply_pattern VARCHAR(255);
        DECLARE fk_businessprocess BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_businessprocess BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_reliability CURSOR FOR
            SELECT PR.ID_PK,
                    PR.NAME,
                    PR.NON_REPUDIATION,
                    PR.REPLY_PATTERN,
                    PR.FK_BUSINESSPROCESS,
                    PR.CREATION_TIME,
                    PR.CREATED_BY,
                    PR.MODIFICATION_TIME,
                    PR.MODIFIED_BY
            FROM TB_PM_RELIABILITY PR;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_RELIABILITY';
        SET @v_tab_new := 'MIGR_TB_PM_RELIABILITY';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_reliability;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_reliability -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_reliability INTO id_pk, name, non_repudiation, reply_pattern, fk_businessprocess,
                        creation_time, created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_reliability', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_business_process', migration_pks, fk_businessprocess, calculated_fk_businessprocess);

                INSERT INTO MIGR_TB_PM_RELIABILITY (ID_PK, NAME, NON_REPUDIATION, REPLY_PATTERN, FK_BUSINESSPROCESS,
                                                    CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        name,
                        non_repudiation,
                        reply_pattern,
                        calculated_fk_businessprocess,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_reliability;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_role(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE name VARCHAR(255);
        DECLARE value VARCHAR(255);
        DECLARE fk_businessprocess BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_businessprocess BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_role CURSOR FOR
            SELECT PR.ID_PK,
                    PR.NAME,
                    PR.VALUE,
                    PR.FK_BUSINESSPROCESS,
                    PR.CREATION_TIME,
                    PR.CREATED_BY,
                    PR.MODIFICATION_TIME,
                    PR.MODIFIED_BY
            FROM TB_PM_ROLE PR;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_ROLE';
        SET @v_tab_new := 'MIGR_TB_PM_ROLE';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_role;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_role -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_role INTO id_pk, name, value, fk_businessprocess, creation_time, created_by, modification_time,
                        modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_role', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_business_process', migration_pks, fk_businessprocess, calculated_fk_businessprocess);

                INSERT INTO MIGR_TB_PM_ROLE (ID_PK, NAME, VALUE, FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY,
                                             MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        name,
                        value,
                        calculated_fk_businessprocess,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_role;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_security(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE name VARCHAR(255);
        DECLARE policy VARCHAR(255);
        DECLARE signature_method VARCHAR(255);
        DECLARE fk_businessprocess BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_businessprocess BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_security CURSOR FOR
            SELECT PS.ID_PK,
                    PS.NAME,
                    PS.POLICY,
                    PS.SIGNATURE_METHOD,
                    PS.FK_BUSINESSPROCESS,
                    PS.CREATION_TIME,
                    PS.CREATED_BY,
                    PS.MODIFICATION_TIME,
                    PS.MODIFIED_BY
            FROM TB_PM_SECURITY PS;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_SECURITY';
        SET @v_tab_new := 'MIGR_TB_PM_SECURITY';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_security;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_security -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_security INTO id_pk, name, policy, signature_method, fk_businessprocess, creation_time,
                        created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_security', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_business_process', migration_pks, fk_businessprocess, calculated_fk_businessprocess);

                INSERT INTO MIGR_TB_PM_SECURITY (ID_PK, NAME, POLICY, SIGNATURE_METHOD, FK_BUSINESSPROCESS,
                                                 CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        name,
                        policy,
                        signature_method,
                        calculated_fk_businessprocess,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_security;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_service(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE name VARCHAR(255);
        DECLARE service_type VARCHAR(255);
        DECLARE value VARCHAR(255);
        DECLARE fk_businessprocess BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_businessprocess BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_service CURSOR FOR
            SELECT PS.ID_PK,
                    PS.NAME,
                    PS.SERVICE_TYPE,
                    PS.VALUE,
                    PS.FK_BUSINESSPROCESS,
                    PS.CREATION_TIME,
                    PS.CREATED_BY,
                    PS.MODIFICATION_TIME,
                    PS.MODIFIED_BY
            FROM TB_PM_SERVICE PS;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_SERVICE';
        SET @v_tab_new := 'MIGR_TB_PM_SERVICE';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_service;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_service -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_service INTO id_pk, name, service_type, value, fk_businessprocess, creation_time,
                        created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_service', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_business_process', migration_pks, fk_businessprocess, calculated_fk_businessprocess);

                INSERT INTO MIGR_TB_PM_SERVICE (ID_PK, NAME, SERVICE_TYPE, VALUE, FK_BUSINESSPROCESS, CREATION_TIME,
                                                CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        name,
                        service_type,
                        value,
                        calculated_fk_businessprocess,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_service;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_splitting(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE name VARCHAR(255);
        DECLARE fragment_size INT;
        DECLARE compression BIT(1);
        DECLARE compression_algorithm VARCHAR(255);
        DECLARE join_interval INT;
        DECLARE fk_businessprocess BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_businessprocess BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_splitting CURSOR FOR
            SELECT PS.ID_PK,
                    PS.NAME,
                    PS.FRAGMENT_SIZE,
                    PS.COMPRESSION,
                    PS.COMPRESSION_ALGORITHM,
                    PS.JOIN_INTERVAL,
                    PS.FK_BUSINESSPROCESS,
                    PS.CREATION_TIME,
                    PS.CREATED_BY,
                    PS.MODIFICATION_TIME,
                    PS.MODIFIED_BY
            FROM TB_PM_SPLITTING PS;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_SPLITTING';
        SET @v_tab_new := 'MIGR_TB_PM_SPLITTING';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_splitting;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_splitting -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_splitting INTO id_pk, name, fragment_size, compression, compression_algorithm, join_interval,
                        fk_businessprocess, creation_time, created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_splitting', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_business_process', migration_pks, fk_businessprocess, calculated_fk_businessprocess);

                INSERT INTO MIGR_TB_PM_SPLITTING (ID_PK, NAME, FRAGMENT_SIZE, COMPRESSION, COMPRESSION_ALGORITHM,
                                                  JOIN_INTERVAL, FK_BUSINESSPROCESS, CREATION_TIME, CREATED_BY,
                                                  MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        name,
                        fragment_size,
                        compression,
                        compression_algorithm,
                        join_interval,
                        calculated_fk_businessprocess,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_splitting;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_leg(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE compress_payloads BIT(1);
        DECLARE name VARCHAR(255);
        DECLARE fk_action BIGINT;
        DECLARE fk_mpc BIGINT;
        DECLARE fk_error_handling BIGINT;
        DECLARE fk_payload_profile BIGINT;
        DECLARE fk_property_set BIGINT;
        DECLARE fk_reception_awareness BIGINT;
        DECLARE fk_reliability BIGINT;
        DECLARE fk_security BIGINT;
        DECLARE fk_service BIGINT;
        DECLARE fk_businessprocess BIGINT;
        DECLARE fk_splitting BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_action BIGINT;
        DECLARE calculated_fk_mpc BIGINT;
        DECLARE calculated_fk_error_handling BIGINT;
        DECLARE calculated_fk_payload_profile BIGINT;
        DECLARE calculated_fk_property_set BIGINT;
        DECLARE calculated_fk_reception_awareness BIGINT;
        DECLARE calculated_fk_reliability BIGINT;
        DECLARE calculated_fk_security BIGINT;
        DECLARE calculated_fk_service BIGINT;
        DECLARE calculated_fk_businessprocess BIGINT;
        DECLARE calculated_fk_splitting BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_leg CURSOR FOR
            SELECT PL.ID_PK,
                    PL.COMPRESS_PAYLOADS,
                    PL.NAME,
                    PL.FK_ACTION,
                    PL.FK_MPC,
                    PL.FK_ERROR_HANDLING,
                    PL.FK_PAYLOAD_PROFILE,
                    PL.FK_PROPERTY_SET,
                    PL.FK_RECEPTION_AWARENESS,
                    PL.FK_RELIABILITY,
                    PL.FK_SECURITY,
                    PL.FK_SERVICE,
                    PL.FK_BUSINESSPROCESS,
                    PL.FK_SPLITTING,
                    PL.CREATION_TIME,
                    PL.CREATED_BY,
                    PL.MODIFICATION_TIME,
                    PL.MODIFIED_BY
            FROM TB_PM_LEG PL;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_LEG';
        SET @v_tab_new := 'MIGR_TB_PM_LEG';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_leg;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_leg -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_leg INTO id_pk, compress_payloads, name, fk_action, fk_mpc, fk_error_handling,
                    fk_payload_profile, fk_property_set, fk_reception_awareness, fk_reliability, fk_security,
                    fk_service, fk_businessprocess, fk_splitting, creation_time, created_by, modification_time,
                    modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_leg', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_action', migration_pks, fk_action, calculated_fk_action);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_mpc', migration_pks, fk_mpc, calculated_fk_mpc);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_error_handling', migration_pks, fk_error_handling, calculated_fk_error_handling);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_payload_profile', migration_pks, fk_payload_profile, calculated_fk_payload_profile);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_message_property_set', migration_pks, fk_property_set, calculated_fk_property_set);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_reception_awareness', migration_pks, fk_reception_awareness, calculated_fk_reception_awareness);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_reliability', migration_pks, fk_reliability, calculated_fk_reliability);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_security', migration_pks, fk_security, calculated_fk_security);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_service', migration_pks, fk_service, calculated_fk_service);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_business_process', migration_pks, fk_businessprocess, calculated_fk_businessprocess);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_splitting', migration_pks, fk_splitting, calculated_fk_splitting);

                INSERT INTO MIGR_TB_PM_LEG (ID_PK, COMPRESS_PAYLOADS, NAME, FK_ACTION, FK_MPC, FK_ERROR_HANDLING,
                                            FK_PAYLOAD_PROFILE, FK_PROPERTY_SET, FK_RECEPTION_AWARENESS,
                                            FK_RELIABILITY, FK_SECURITY, FK_SERVICE, FK_BUSINESSPROCESS, FK_SPLITTING,
                                            CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        compress_payloads,
                        name,
                        calculated_fk_action,
                        calculated_fk_mpc,
                        calculated_fk_error_handling,
                        calculated_fk_payload_profile,
                        calculated_fk_property_set,
                        calculated_fk_reception_awareness,
                        calculated_fk_reliability,
                        calculated_fk_security,
                        calculated_fk_service,
                        calculated_fk_businessprocess,
                        calculated_fk_splitting,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_leg;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_leg_mpc(INOUT migration_pks JSON)
    BEGIN
        DECLARE legconfiguration_id_pk BIGINT;
        DECLARE partympcmap_id_pk BIGINT;
        DECLARE partympcmap_key BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_legconfiguration_id_pk BIGINT;
        DECLARE calculated_partympcmap_id_pk BIGINT;
        DECLARE calculated_partympcmap_key BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_leg_mpc CURSOR FOR
            SELECT PLM.LEGCONFIGURATION_ID_PK,
                    PLM.PARTYMPCMAP_ID_PK,
                    PLM.PARTYMPCMAP_KEY,
                    PLM.CREATION_TIME,
                    PLM.CREATED_BY,
                    PLM.MODIFICATION_TIME,
                    PLM.MODIFIED_BY
            FROM TB_PM_LEG_MPC PLM;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_LEG_MPC';
        SET @v_tab_new := 'MIGR_TB_PM_LEG_MPC';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_leg_mpc;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_leg_mpc -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_leg_mpc INTO legconfiguration_id_pk, partympcmap_id_pk, partympcmap_key, creation_time,
                        created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_leg', migration_pks, legconfiguration_id_pk, calculated_legconfiguration_id_pk);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_mpc', migration_pks, partympcmap_id_pk, calculated_partympcmap_id_pk);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_party', migration_pks, partympcmap_key, calculated_partympcmap_key);

                INSERT INTO MIGR_TB_PM_LEG_MPC (LEGCONFIGURATION_ID_PK, PARTYMPCMAP_ID_PK, PARTYMPCMAP_KEY,
                                                CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_legconfiguration_id_pk,
                        calculated_partympcmap_id_pk,
                        calculated_partympcmap_key,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                       ' records '));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_leg_mpc;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_process(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE name VARCHAR(255);
        DECLARE fk_agreement BIGINT;
        DECLARE fk_initiator_role BIGINT;
        DECLARE fk_mep BIGINT;
        DECLARE fk_mep_binding BIGINT;
        DECLARE fk_responder_role BIGINT;
        DECLARE fk_businessprocess BIGINT;
        DECLARE use_dynamic_initiator BIT(1);
        DECLARE use_dynamic_responder BIT(1);
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_agreement BIGINT;
        DECLARE calculated_fk_initiator_role BIGINT;
        DECLARE calculated_fk_mep BIGINT;
        DECLARE calculated_fk_mep_binding BIGINT;
        DECLARE calculated_fk_responder_role BIGINT;
        DECLARE calculated_fk_businessprocess BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_process CURSOR FOR
            SELECT PP.ID_PK,
                    PP.NAME,
                    PP.FK_AGREEMENT,
                    PP.FK_INITIATOR_ROLE,
                    PP.FK_MEP,
                    PP.FK_MEP_BINDING,
                    PP.FK_RESPONDER_ROLE,
                    PP.FK_BUSINESSPROCESS,
                    PP.USE_DYNAMIC_INITIATOR,
                    PP.USE_DYNAMIC_RESPONDER,
                    PP.CREATION_TIME,
                    PP.CREATED_BY,
                    PP.MODIFICATION_TIME,
                    PP.MODIFIED_BY
            FROM TB_PM_PROCESS PP;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_PROCESS';
        SET @v_tab_new := 'MIGR_TB_PM_PROCESS';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_process;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_process -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_process INTO id_pk, name, fk_agreement, fk_initiator_role, fk_mep, fk_mep_binding,
                        fk_responder_role, fk_businessprocess, use_dynamic_initiator, use_dynamic_responder,
                        creation_time, created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_process', id_pk, calculated_id_pk, migration_pks);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_agreement', migration_pks, fk_agreement, calculated_fk_agreement);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_role', migration_pks, fk_initiator_role, calculated_fk_initiator_role);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_mep', migration_pks, fk_mep, calculated_fk_mep);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_mep_binding', migration_pks, fk_mep_binding, calculated_fk_mep_binding);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_role', migration_pks, fk_responder_role, calculated_fk_responder_role);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_business_process', migration_pks, fk_businessprocess, calculated_fk_businessprocess);

                INSERT INTO MIGR_TB_PM_PROCESS (ID_PK, NAME, FK_AGREEMENT, FK_INITIATOR_ROLE, FK_MEP, FK_MEP_BINDING,
                                                FK_RESPONDER_ROLE, FK_BUSINESSPROCESS, USE_DYNAMIC_INITIATOR,
                                                USE_DYNAMIC_RESPONDER, CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                MODIFIED_BY)
                VALUES (calculated_id_pk,
                        name,
                        calculated_fk_agreement,
                        calculated_fk_initiator_role,
                        calculated_fk_mep,
                        calculated_fk_mep_binding,
                        calculated_fk_responder_role,
                        calculated_fk_businessprocess,
                        use_dynamic_initiator,
                        use_dynamic_responder,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_process;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_join_process_init_party(INOUT migration_pks JSON)
    BEGIN
        DECLARE process_fk BIGINT;
        DECLARE party_fk BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_process_fk BIGINT;
        DECLARE calculated_party_fk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_join_process_init_party CURSOR FOR
            SELECT PJPIP.PROCESS_FK,
                    PJPIP.PARTY_FK,
                    PJPIP.CREATION_TIME,
                    PJPIP.CREATED_BY,
                    PJPIP.MODIFICATION_TIME,
                    PJPIP.MODIFIED_BY
            FROM TB_PM_JOIN_PROCESS_INIT_PARTY PJPIP;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_JOIN_PROCESS_INIT_PARTY';
        SET @v_tab_new := 'MIGR_TB_PM_JOIN_PROC_INI_PARTY';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_join_process_init_party;
        read_loop:
        LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_join_process_init_party -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_join_process_init_party INTO process_fk, party_fk, creation_time, created_by,
                        modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_process', migration_pks, process_fk, calculated_process_fk);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_party', migration_pks, party_fk, calculated_party_fk);

                INSERT INTO MIGR_TB_PM_JOIN_PROC_INI_PARTY (PROCESS_FK, PARTY_FK, CREATION_TIME, CREATED_BY,
                                                                MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_process_fk,
                        calculated_party_fk,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_join_process_init_party;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_join_process_leg(INOUT migration_pks JSON)
    BEGIN
        DECLARE process_fk BIGINT;
        DECLARE leg_fk BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_process_fk BIGINT;
        DECLARE calculated_leg_fk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_join_process_leg CURSOR FOR
            SELECT PJPL.PROCESS_FK,
                    PJPL.LEG_FK,
                    PJPL.CREATION_TIME,
                    PJPL.CREATED_BY,
                    PJPL.MODIFICATION_TIME,
                    PJPL.MODIFIED_BY
            FROM TB_PM_JOIN_PROCESS_LEG PJPL;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_JOIN_PROCESS_LEG';
        SET @v_tab_new := 'MIGR_TB_PM_JOIN_PROCESS_LEG';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_join_process_leg;
        read_loop:
        LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_join_process_leg -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_join_process_leg INTO process_fk, leg_fk, creation_time, created_by, modification_time,
                        modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_process', migration_pks, process_fk, calculated_process_fk);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_leg', migration_pks, leg_fk, calculated_leg_fk);

                INSERT INTO MIGR_TB_PM_JOIN_PROCESS_LEG (PROCESS_FK, LEG_FK, CREATION_TIME, CREATED_BY,
                                                         MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_process_fk,
                        calculated_leg_fk,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_join_process_leg;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_join_process_resp_party(INOUT migration_pks JSON)
    BEGIN
        DECLARE process_fk BIGINT;
        DECLARE party_fk BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_process_fk BIGINT;
        DECLARE calculated_party_fk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_join_process_resp_party CURSOR FOR
            SELECT PJPRP.PROCESS_FK,
                    PJPRP.PARTY_FK,
                    PJPRP.CREATION_TIME,
                    PJPRP.CREATED_BY,
                    PJPRP.MODIFICATION_TIME,
                    PJPRP.MODIFIED_BY
            FROM TB_PM_JOIN_PROCESS_RESP_PARTY PJPRP;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_JOIN_PROCESS_RESP_PARTY';
        SET @v_tab_new := 'MIGR_TB_PM_JOIN_PROC_RSP_PARTY';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_join_process_resp_party;
        read_loop:
        LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_join_process_resp_party -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_join_process_resp_party INTO process_fk, party_fk, creation_time, created_by,
                        modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_process', migration_pks, process_fk, calculated_process_fk);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('pm_party', migration_pks, party_fk, calculated_party_fk);

                INSERT INTO MIGR_TB_PM_JOIN_PROC_RSP_PARTY (PROCESS_FK, PARTY_FK, CREATION_TIME, CREATED_BY,
                                                                MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_process_fk,
                        calculated_party_fk,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_join_process_resp_party;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_configuration_raw(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE configuration_date TIMESTAMP;
        DECLARE xml LONGBLOB;
        DECLARE description VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_configuration_raw CURSOR FOR
            SELECT PCR.ID_PK,
                    PCR.CONFIGURATION_DATE,
                    PCR.XML,
                    PCR.DESCRIPTION,
                    PCR.CREATION_TIME,
                    PCR.CREATED_BY,
                    PCR.MODIFICATION_TIME,
                    PCR.MODIFIED_BY
            FROM TB_PM_CONFIGURATION_RAW PCR;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_CONFIGURATION_RAW';
        SET @v_tab_new := 'MIGR_TB_PM_CONFIGURATION_RAW';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_configuration_raw;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_configuration_raw -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_configuration_raw INTO id_pk, configuration_date, xml, description, creation_time,
                        created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('pm_configuration_raw', id_pk, calculated_id_pk, migration_pks);

                INSERT INTO MIGR_TB_PM_CONFIGURATION_RAW (ID_PK, CONFIGURATION_DATE, XML, DESCRIPTION, CREATION_TIME,
                                                          CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        configuration_date,
                        CAST(xml AS BINARY),
                        description,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_configuration_raw;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_user_domain()
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE user_name VARCHAR(255);
        DECLARE domain VARCHAR(255);
        DECLARE preferred_domain VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_user_domain CURSOR FOR
        SELECT UD.ID_PK,
                UD.USER_NAME,
                UD.DOMAIN,
                UD.PREFERRED_DOMAIN,
                UD.CREATION_TIME,
                UD.CREATED_BY,
                UD.MODIFICATION_TIME,
                UD.MODIFIED_BY
        FROM TB_USER_DOMAIN UD;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_USER_DOMAIN';
        SET @v_tab_new := 'MIGR_TB_USER_DOMAIN';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_user_domain;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                                    @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_user_domain -> execute immediate error: ', @p2));
                END;

                FETCH c_user_domain INTO id_pk, user_name, domain, preferred_domain, creation_time, created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);

                INSERT INTO MIGR_TB_USER_DOMAIN (ID_PK, USER_NAME, DOMAIN, PREFERRED_DOMAIN, CREATION_TIME, CREATED_BY,
                                                 MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        user_name,
                        domain,
                        preferred_domain,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_user_domain;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_user(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE user_email VARCHAR(255);
        DECLARE user_enabled BIT(1);
        DECLARE user_password VARCHAR(255);
        DECLARE user_name VARCHAR(255);
        DECLARE optlock INT;
        DECLARE attempt_count INT;
        DECLARE suspension_date TIMESTAMP;
        DECLARE user_deleted BIT(1);
        DECLARE password_change_date TIMESTAMP;
        DECLARE default_password BIT(1);
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_user CURSOR FOR
            SELECT U.ID_PK,
                    U.USER_EMAIL,
                    U.USER_ENABLED,
                    U.USER_PASSWORD,
                    U.USER_NAME,
                    U.OPTLOCK,
                    U.ATTEMPT_COUNT,
                    U.SUSPENSION_DATE,
                    U.USER_DELETED,
                    U.PASSWORD_CHANGE_DATE,
                    U.DEFAULT_PASSWORD,
                    U.CREATION_TIME,
                    U.CREATED_BY,
                    U.MODIFICATION_TIME,
                    U.MODIFIED_BY
            FROM TB_USER U;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_USER';
        SET @v_tab_new := 'MIGR_TB_USER';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_user;
        read_loop:
        LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_user -> execute immediate error: ', @p2));
                END;

                FETCH c_user INTO id_pk, user_email, user_enabled, user_password, user_name, optlock, attempt_count,
                        suspension_date, user_deleted, password_change_date, default_password, creation_time,
                        created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('user', id_pk, calculated_id_pk, migration_pks);

                INSERT INTO MIGR_TB_USER (ID_PK, USER_EMAIL, USER_ENABLED, USER_PASSWORD, USER_NAME, OPTLOCK,
                                          ATTEMPT_COUNT, SUSPENSION_DATE, USER_DELETED, PASSWORD_CHANGE_DATE,
                                          DEFAULT_PASSWORD, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        user_email,
                        user_enabled,
                        user_password,
                        user_name,
                        optlock,
                        attempt_count,
                        suspension_date,
                        user_deleted,
                        password_change_date,
                        default_password,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_user;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_user_password_history(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE user_id BIGINT;
        DECLARE user_password VARCHAR(255);
        DECLARE password_change_date TIMESTAMP;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_user_id BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_user_password_history CURSOR FOR
            SELECT UPH.ID_PK,
                    UPH.USER_ID,
                    UPH.USER_PASSWORD,
                    UPH.PASSWORD_CHANGE_DATE,
                    UPH.CREATION_TIME,
                    UPH.CREATED_BY,
                    UPH.MODIFICATION_TIME,
                    UPH.MODIFIED_BY
            FROM TB_USER_PASSWORD_HISTORY UPH;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_USER_PASSWORD_HISTORY';
        SET @v_tab_new := 'MIGR_TB_USER_PASSWORD_HISTORY';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_user_password_history;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_user_password_history -> execute immediate error: ', @p2));
                END;

                FETCH c_user_password_history INTO id_pk, user_id, user_password, password_change_date, creation_time,
                        created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('user', migration_pks, user_id, calculated_user_id);

                INSERT INTO MIGR_TB_USER_PASSWORD_HISTORY (ID_PK, USER_ID, USER_PASSWORD, PASSWORD_CHANGE_DATE,
                                                           CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (calculated_id_pk,
                        calculated_user_id,
                        user_password,
                        password_change_date,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_user_password_history;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_user_role(INOUT migration_pks JSON)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE role_name VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_user_role CURSOR FOR
            SELECT UR.ID_PK,
                    UR.ROLE_NAME,
                    UR.CREATION_TIME,
                    UR.CREATED_BY,
                    UR.MODIFICATION_TIME,
                    UR.MODIFIED_BY
            FROM TB_USER_ROLE UR;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_USER_ROLE';
        SET @v_tab_new := 'MIGR_TB_USER_ROLE';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_user_role;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_user_role -> execute immediate error: ', @p2));
                END;

                FETCH c_user_role INTO id_pk, role_name, creation_time, created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);
                CALL MIGRATE_42_TO_50_update_migration_pks('user_role', id_pk, calculated_id_pk, migration_pks);

                INSERT INTO MIGR_TB_USER_ROLE (ID_PK, ROLE_NAME, CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                               MODIFIED_BY)
                VALUES (calculated_id_pk,
                        role_name,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_user_role;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_user_roles(INOUT migration_pks JSON)
    BEGIN
        DECLARE user_id BIGINT;
        DECLARE role_id BIGINT;
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_user_id BIGINT;
        DECLARE calculated_role_id BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_user_roles CURSOR FOR
            SELECT UR.USER_ID,
                    UR.ROLE_ID,
                    UR.CREATION_TIME,
                    UR.CREATED_BY,
                    UR.MODIFICATION_TIME,
                    UR.MODIFIED_BY
            FROM TB_USER_ROLES UR;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_USER_ROLES';
        SET @v_tab_new := 'MIGR_TB_USER_ROLES';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_user_roles;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_user_roles -> execute immediate error: ', @p2));
                END;

                FETCH c_user_roles INTO user_id, role_id, creation_time, created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('user', migration_pks, user_id, calculated_user_id);
                CALL MIGRATE_42_TO_50_lookup_migration_pk('user_role', migration_pks, role_id, calculated_role_id);

                INSERT INTO MIGR_TB_USER_ROLES (USER_ID, ROLE_ID, CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                MODIFIED_BY)
                VALUES (calculated_user_id,
                        calculated_role_id,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_user_roles;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_ws_plugin_tb_message_log()
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE message_id VARCHAR(255);
        DECLARE conversation_id VARCHAR(255);
        DECLARE ref_to_message_id VARCHAR(255);
        DECLARE from_party_id VARCHAR(255);
        DECLARE final_recipient VARCHAR(255);
        DECLARE original_sender VARCHAR(255);
        DECLARE received TIMESTAMP;

        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_ws_plugin_message_log CURSOR FOR
            SELECT WPML.ID_PK,
                    WPML.MESSAGE_ID,
                    WPML.CONVERSATION_ID,
                    WPML.REF_TO_MESSAGE_ID,
                    WPML.FROM_PARTY_ID,
                    WPML.FINAL_RECIPIENT,
                    WPML.ORIGINAL_SENDER,
                    WPML.RECEIVED
            FROM WS_PLUGIN_TB_MESSAGE_LOG WPML;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'WS_PLUGIN_TB_MESSAGE_LOG';
        SET @v_tab_new := 'MIGR_WS_PLUGIN_TB_MESSAGE_LOG';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_ws_plugin_message_log;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_ws_plugin_message_log -> execute immediate error: ', @p2));
                END;

                FETCH c_ws_plugin_message_log INTO id_pk, message_id, conversation_id, ref_to_message_id,
                        from_party_id, final_recipient, original_sender, received;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, SYSDATE());

                INSERT INTO MIGR_WS_PLUGIN_TB_MESSAGE_LOG (ID_PK, MESSAGE_ID, CONVERSATION_ID, REF_TO_MESSAGE_ID,
                                                           FROM_PARTY_ID, FINAL_RECIPIENT, ORIGINAL_SENDER, RECEIVED)
                VALUES (calculated_id_pk,
                        message_id,
                        conversation_id,
                        ref_to_message_id,
                        from_party_id,
                        final_recipient,
                        original_sender,
                        received);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_ws_plugin_message_log;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_rev_info(INOUT migration_pks JSON)
    BEGIN
        DECLARE id BIGINT;
        DECLARE timestamp BIGINT;
        DECLARE revision_date TIMESTAMP;
        DECLARE user_name VARCHAR(255);

        DECLARE calculated_id BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_rev_info CURSOR FOR
            SELECT RI.ID,
                    RI.TIMESTAMP,
                    RI.REVISION_DATE,
                    RI.USER_NAME
            FROM TB_REV_INFO RI;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_REV_INFO';
        SET @v_tab_new := 'MIGR_TB_REV_INFO';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_rev_info;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_rev_info -> execute immediate error: ', @p2));
                END;

                FETCH c_rev_info INTO id, timestamp, revision_date, user_name;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                SET calculated_id := MIGRATE_42_TO_50_generate_scalable_seq(id, revision_date);
                CALL MIGRATE_42_TO_50_update_migration_pks('rev_info', id, calculated_id, migration_pks);

                INSERT INTO MIGR_TB_REV_INFO (ID, TIMESTAMP, REVISION_DATE, USER_NAME)
                VALUES (calculated_id,
                        timestamp,
                        revision_date,
                        user_name);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_rev_info;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_rev_changes(INOUT migration_pks JSON, missing_entity_date_prefix DATE)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE rev BIGINT;
        DECLARE audit_order INT;
        DECLARE entity_name VARCHAR(255);
        DECLARE group_name VARCHAR(255);
        DECLARE entity_id VARCHAR(255);
        DECLARE modification_type VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE calculated_rev BIGINT;
        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_entity_id BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_rev_changes CURSOR FOR
            SELECT RC.ID_PK,
                    RC.REV,
                    RC.AUDIT_ORDER,
                    RC.ENTITY_NAME,
                    RC.GROUP_NAME,
                    RC.ENTITY_ID,
                    RC.MODIFICATION_TYPE,
                    RC.CREATION_TIME,
                    RC.CREATED_BY,
                    RC.MODIFICATION_TIME,
                    RC.MODIFIED_BY
            FROM TB_REV_CHANGES RC;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_REV_CHANGES';
        SET @v_tab_new := 'MIGR_TB_REV_CHANGES';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_rev_changes;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_rev_changes -> execute immediate error: ', @p2));
                END;

                FETCH c_rev_changes INTO id_pk, rev, audit_order, entity_name, group_name, entity_id, modification_type,
                        creation_time, created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('rev_info', migration_pks, rev, calculated_rev);
                SET calculated_id_pk := MIGRATE_42_TO_50_generate_scalable_seq(id_pk, creation_time);

                CASE entity_name
                    WHEN 'eu.domibus.core.user.plugin.AuthenticationEntity' THEN CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('authentication_entry', migration_pks, missing_entity_date_prefix, CAST(entity_id AS UNSIGNED), calculated_entity_id);
                    WHEN 'eu.domibus.core.plugin.routing.BackendFilterEntity' THEN CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('backend_filter', migration_pks, missing_entity_date_prefix, CAST(entity_id AS UNSIGNED), calculated_entity_id);
                    WHEN 'eu.domibus.core.plugin.routing.RoutingCriteriaEntity' THEN CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('routing_criteria', migration_pks, missing_entity_date_prefix, CAST(entity_id AS UNSIGNED), calculated_entity_id);
                    WHEN 'eu.domibus.core.user.ui.User' THEN CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('user', migration_pks, missing_entity_date_prefix, CAST(entity_id AS UNSIGNED), calculated_entity_id);
                    WHEN 'eu.domibus.common.model.configuration.Configuration' THEN CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('pm_configuration', migration_pks, missing_entity_date_prefix, CAST(entity_id AS UNSIGNED), calculated_entity_id);
                    WHEN 'eu.domibus.common.model.configuration.ConfigurationRaw' THEN CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('pm_configuration_raw', migration_pks, missing_entity_date_prefix, CAST(entity_id AS UNSIGNED), calculated_entity_id);
                    WHEN 'eu.domibus.common.model.configuration.Party' THEN CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('pm_party', migration_pks, missing_entity_date_prefix, CAST(entity_id AS UNSIGNED), calculated_entity_id);
                    WHEN 'eu.domibus.common.model.configuration.PartyIdType' THEN CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('pm_party_id_type', migration_pks, missing_entity_date_prefix, CAST(entity_id AS UNSIGNED), calculated_entity_id);
                    WHEN 'eu.domibus.core.certificate.Certificate' THEN CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('certificate', migration_pks, missing_entity_date_prefix, CAST(entity_id AS UNSIGNED), calculated_entity_id);
                    WHEN 'eu.domibus.core.user.ui.UserRole' THEN CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('user_role', migration_pks, missing_entity_date_prefix, CAST(entity_id AS UNSIGNED), calculated_entity_id);
                    WHEN 'eu.domibus.core.audit.model.TruststoreAudit' THEN CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('action_audit', migration_pks, missing_entity_date_prefix, CAST(entity_id AS UNSIGNED), calculated_entity_id);
                    WHEN 'eu.domibus.core.audit.model.PModeAudit' THEN CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('action_audit', migration_pks, missing_entity_date_prefix, CAST(entity_id AS UNSIGNED), calculated_entity_id);
                    WHEN 'eu.domibus.core.audit.model.PModeArchiveAudit' THEN CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('action_audit', migration_pks, missing_entity_date_prefix, CAST(entity_id AS UNSIGNED), calculated_entity_id);
                    WHEN 'eu.domibus.core.audit.model.MessageAudit' THEN CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('action_audit', migration_pks, missing_entity_date_prefix, CAST(entity_id AS UNSIGNED), calculated_entity_id);
                    WHEN 'eu.domibus.core.audit.model.JmsMessageAudit' THEN CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('action_audit', migration_pks, missing_entity_date_prefix, CAST(entity_id AS UNSIGNED), calculated_entity_id);
                    ELSE
                        BEGIN
                            -- use the previous entity ID value for unknown entities
                            CALL MIGRATE_42_TO_50_trace(CONCAT('Unknown entity name ', entity_name));
                            SET calculated_entity_id := MIGRATE_42_TO_50_generate_scalable_seq(entity_id, missing_entity_date_prefix);
                        END;
                END CASE;

                INSERT INTO MIGR_TB_REV_CHANGES (ID_PK, REV, AUDIT_ORDER, ENTITY_NAME, GROUP_NAME, ENTITY_ID,
                                                 MODIFICATION_TYPE, CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                                                 MODIFIED_BY)
                VALUES (calculated_id_pk,
                        calculated_rev,
                        audit_order,
                        entity_name,
                        group_name,
                        CAST(calculated_entity_id AS CHAR(255)),
                        modification_type,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_rev_changes;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_authentication_entry_aud(INOUT migration_pks JSON, missing_entity_date_prefix DATE)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE rev BIGINT;
        DECLARE revtype TINYINT;
        DECLARE certificate_id VARCHAR(255);
        DECLARE certificateid_mod BIT(1);
        DECLARE username VARCHAR(255);
        DECLARE username_mod BIT(1);
        DECLARE passwd VARCHAR(255);
        DECLARE password_mod BIT(1);
        DECLARE auth_roles VARCHAR(255);
        DECLARE authroles_mod BIT(1);
        DECLARE original_user VARCHAR(255);
        DECLARE originaluser_mod BIT(1);
        DECLARE backend VARCHAR(255);
        DECLARE backend_mod BIT(1);
        DECLARE user_enabled BIT(1);
        DECLARE active_mod BIT(1);
        DECLARE password_change_date TIMESTAMP;
        DECLARE passwordchangedate_mod BIT(1);
        DECLARE default_password BIT(1);
        DECLARE defaultpassword_mod BIT(1);

        DECLARE calculated_rev BIGINT;
        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_authentication_entry_aud CURSOR FOR
            SELECT AEA.ID_PK,
                    AEA.REV,
                    AEA.REVTYPE,
                    AEA.CERTIFICATE_ID,
                    AEA.CERTIFICATEID_MOD,
                    AEA.USERNAME,
                    AEA.USERNAME_MOD,
                    AEA.PASSWD,
                    AEA.PASSWORD_MOD,
                    AEA.AUTH_ROLES,
                    AEA.AUTHROLES_MOD,
                    AEA.ORIGINAL_USER,
                    AEA.ORIGINALUSER_MOD,
                    AEA.BACKEND,
                    AEA.BACKEND_MOD,
                    AEA.USER_ENABLED,
                    AEA.ACTIVE_MOD,
                    AEA.PASSWORD_CHANGE_DATE,
                    AEA.PASSWORDCHANGEDATE_MOD,
                    AEA.DEFAULT_PASSWORD,
                    AEA.DEFAULTPASSWORD_MOD
            FROM TB_AUTHENTICATION_ENTRY_AUD AEA;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_AUTHENTICATION_ENTRY_AUD';
        SET @v_tab_new := 'MIGR_TB_AUTH_ENTRY_AUD';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_authentication_entry_aud;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_authentication_entry_aud -> execute immediate error: ', @p2));
                END;

                FETCH c_authentication_entry_aud INTO id_pk, rev, revtype, certificate_id, certificateid_mod, username,
                        username_mod, passwd, password_mod, auth_roles, authroles_mod, original_user, originaluser_mod,
                        backend, backend_mod, user_enabled, active_mod, password_change_date, passwordchangedate_mod,
                        default_password, defaultpassword_mod;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('rev_info', migration_pks, rev, calculated_rev);
                CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('authentication_entry', migration_pks, missing_entity_date_prefix, id_pk, calculated_id_pk);

                INSERT INTO MIGR_TB_AUTH_ENTRY_AUD (ID_PK, REV, REVTYPE, CERTIFICATE_ID, CERTIFICATEID_MOD,
                                                              USERNAME, USERNAME_MOD, PASSWD, PASSWORD_MOD, AUTH_ROLES,
                                                              AUTHROLES_MOD, ORIGINAL_USER, ORIGINALUSER_MOD, BACKEND,
                                                              BACKEND_MOD, USER_ENABLED, ACTIVE_MOD,
                                                              PASSWORD_CHANGE_DATE, PASSWORDCHANGEDATE_MOD,
                                                              DEFAULT_PASSWORD, DEFAULTPASSWORD_MOD)
                VALUES (calculated_id_pk,
                        calculated_rev,
                        revtype,
                        certificate_id,
                        certificateid_mod,
                        username,
                        username_mod,
                        passwd,
                        password_mod,
                        auth_roles,
                        authroles_mod,
                        original_user,
                        originaluser_mod,
                        backend,
                        backend_mod,
                        user_enabled,
                        active_mod,
                        password_change_date,
                        passwordchangedate_mod,
                        default_password,
                        defaultpassword_mod);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_authentication_entry_aud;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_back_rcriteria_aud(INOUT migration_pks JSON, missing_entity_date_prefix DATE)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE rev BIGINT;
        DECLARE revtype TINYINT;
        DECLARE fk_backend_filter BIGINT;
        DECLARE priority INT;

        DECLARE calculated_rev BIGINT;
        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_backend_filter BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_back_rcriteria_aud CURSOR FOR
            SELECT BRA.ID_PK,
                    BRA.REV,
                    BRA.REVTYPE,
                    BRA.FK_BACKEND_FILTER,
                    BRA.PRIORITY
            FROM TB_BACK_RCRITERIA_AUD BRA;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_BACK_RCRITERIA_AUD';
        SET @v_tab_new := 'MIGR_TB_BACK_RCRITERIA_AUD';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_back_rcriteria_aud;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_back_rcriteria_aud -> execute immediate error: ', @p2));
                END;

                FETCH c_back_rcriteria_aud INTO id_pk, rev, revtype, fk_backend_filter, priority;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('rev_info', migration_pks, rev, calculated_rev);
                CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('routing_criteria', migration_pks, missing_entity_date_prefix, id_pk, calculated_id_pk);
                CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('backend_filter', migration_pks, missing_entity_date_prefix, fk_backend_filter, calculated_fk_backend_filter);

                INSERT INTO MIGR_TB_BACK_RCRITERIA_AUD (ID_PK, REV, REVTYPE, FK_BACKEND_FILTER, PRIORITY)
                VALUES (calculated_id_pk,
                        calculated_rev,
                        revtype,
                        calculated_fk_backend_filter,
                        priority);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_back_rcriteria_aud;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_backend_filter_aud(INOUT migration_pks JSON, missing_entity_date_prefix DATE)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE rev BIGINT;
        DECLARE revtype TINYINT;
        DECLARE backend_name VARCHAR(255);
        DECLARE backendname_mod BIT(1);
        DECLARE priority INT;
        DECLARE index_mod BIT(1);
        DECLARE routingcriterias_mod BIT(1);

        DECLARE calculated_rev BIGINT;
        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_backend_filter_aud CURSOR FOR
            SELECT BFA.ID_PK,
                    BFA.REV,
                    BFA.REVTYPE,
                    BFA.BACKEND_NAME,
                    BFA.BACKENDNAME_MOD,
                    BFA.PRIORITY,
                    BFA.INDEX_MOD,
                    BFA.ROUTINGCRITERIAS_MOD
            FROM TB_BACKEND_FILTER_AUD BFA;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_BACKEND_FILTER_AUD';
        SET @v_tab_new := 'MIGR_TB_BACKEND_FILTER_AUD';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_backend_filter_aud;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_backend_filter_aud -> execute immediate error: ', @p2));
                END;

                FETCH c_backend_filter_aud INTO id_pk, rev, revtype, backend_name, backendname_mod, priority,
                        index_mod, routingcriterias_mod;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('rev_info', migration_pks, rev, calculated_rev);
                CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('backend_filter', migration_pks, missing_entity_date_prefix, id_pk, calculated_id_pk);

                INSERT INTO MIGR_TB_BACKEND_FILTER_AUD (ID_PK, REV, REVTYPE, BACKEND_NAME, BACKENDNAME_MOD, PRIORITY,
                                                        INDEX_MOD, ROUTINGCRITERIAS_MOD)
                VALUES (calculated_id_pk,
                        calculated_rev,
                        revtype,
                        backend_name,
                        backendname_mod,
                        priority,
                        index_mod,
                        routingcriterias_mod);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_backend_filter_aud;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_certificate_aud(INOUT migration_pks JSON, missing_entity_date_prefix DATE)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE rev BIGINT;
        DECLARE revtype TINYINT;
        DECLARE certificate_alias VARCHAR(255);
        DECLARE not_valid_before_date DATETIME;
        DECLARE not_valid_after_date DATETIME;
        DECLARE revoke_notification_date TIMESTAMP;
        DECLARE alert_imm_notification_date TIMESTAMP;
        DECLARE alert_exp_notification_date TIMESTAMP;
        DECLARE certificate_status VARCHAR(255);
        DECLARE certificate_type VARCHAR(255);

        DECLARE calculated_rev BIGINT;
        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_certificate_aud CURSOR FOR
            SELECT CA.ID_PK,
                    CA.REV,
                    CA.REVTYPE,
                    CA.CERTIFICATE_ALIAS,
                    CA.NOT_VALID_BEFORE_DATE,
                    CA.NOT_VALID_AFTER_DATE,
                    CA.REVOKE_NOTIFICATION_DATE,
                    CA.ALERT_IMM_NOTIFICATION_DATE,
                    CA.ALERT_EXP_NOTIFICATION_DATE,
                    CA.CERTIFICATE_STATUS,
                    CA.CERTIFICATE_TYPE
            FROM TB_CERTIFICATE_AUD CA;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_CERTIFICATE_AUD';
        SET @v_tab_new := 'MIGR_TB_CERTIFICATE_AUD';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_certificate_aud;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_certificate_aud -> execute immediate error: ', @p2));
                END;

                FETCH c_certificate_aud INTO id_pk, rev, revtype, certificate_alias, not_valid_before_date,
                        not_valid_after_date, revoke_notification_date, alert_imm_notification_date,
                        alert_exp_notification_date, certificate_status, certificate_type;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('rev_info', migration_pks, rev, calculated_rev);
                CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('certificate', migration_pks, missing_entity_date_prefix, id_pk, calculated_id_pk);

                INSERT INTO MIGR_TB_CERTIFICATE_AUD (ID_PK, REV, REVTYPE, CERTIFICATE_ALIAS, NOT_VALID_BEFORE_DATE,
                                                     NOT_VALID_AFTER_DATE, REVOKE_NOTIFICATION_DATE,
                                                     ALERT_IMM_NOTIFICATION_DATE, ALERT_EXP_NOTIFICATION_DATE,
                                                     CERTIFICATE_STATUS, CERTIFICATE_TYPE)
                VALUES (calculated_id_pk,
                        calculated_rev,
                        revtype,
                        certificate_alias,
                        not_valid_before_date,
                        not_valid_after_date,
                        revoke_notification_date,
                        alert_imm_notification_date,
                        alert_exp_notification_date,
                        certificate_status,
                        certificate_type);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_certificate_aud;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_configuration_aud(INOUT migration_pks JSON, missing_entity_date_prefix DATE)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE rev BIGINT;
        DECLARE revtype TINYINT;
        DECLARE expression VARCHAR(255);
        DECLARE expression_mod BIT(1);
        DECLARE name VARCHAR(255);
        DECLARE name_mod BIT(1);

        DECLARE calculated_rev BIGINT;
        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_configuration_aud CURSOR FOR
            SELECT PCA.ID_PK,
                    PCA.REV,
                    PCA.REVTYPE,
                    PCA.EXPRESSION,
                    PCA.EXPRESSION_MOD,
                    PCA.NAME,
                    PCA.NAME_MOD
            FROM TB_PM_CONFIGURATION_AUD PCA;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_CONFIGURATION_AUD';
        SET @v_tab_new := 'MIGR_TB_PM_CONFIGURATION_AUD';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_configuration_aud;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_configuration_aud -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_configuration_aud INTO id_pk, rev, revtype, expression, expression_mod, name, name_mod;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('rev_info', migration_pks, rev, calculated_rev);
                CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('pm_configuration', migration_pks, missing_entity_date_prefix, id_pk, calculated_id_pk);

                INSERT INTO MIGR_TB_PM_CONFIGURATION_AUD (ID_PK, REV, REVTYPE, EXPRESSION, EXPRESSION_MOD, NAME,
                                                          NAME_MOD)
                VALUES (calculated_id_pk,
                        calculated_rev,
                        revtype,
                        expression,
                        expression_mod,
                        name,
                        name_mod);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_configuration_aud;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_configuration_raw_aud(INOUT migration_pks JSON, missing_entity_date_prefix DATE)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE rev BIGINT;
        DECLARE revtype TINYINT;
        DECLARE configuration_date TIMESTAMP;
        DECLARE configurationdate_mod BIT(1);
        DECLARE description VARCHAR(255);
        DECLARE description_mod VARCHAR(255);
        DECLARE xml LONGBLOB;
        DECLARE xml_mod BIT(1);

        DECLARE calculated_rev BIGINT;
        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_configuration_raw_aud CURSOR FOR
            SELECT PCRA.ID_PK,
                    PCRA.REV,
                    PCRA.REVTYPE,
                    PCRA.CONFIGURATION_DATE,
                    PCRA.CONFIGURATIONDATE_MOD,
                    PCRA.DESCRIPTION,
                    PCRA.DESCRIPTION_MOD,
                    PCRA.XML,
                    PCRA.XML_MOD
            FROM TB_PM_CONFIGURATION_RAW_AUD PCRA;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_CONFIGURATION_RAW_AUD';
        SET @v_tab_new := 'MIGR_TB_PM_CONF_RAW_AUD';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_configuration_raw_aud;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_configuration_raw_aud -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_configuration_raw_aud INTO id_pk, rev, revtype, configuration_date, configurationdate_mod,
                        description, description_mod, xml, xml_mod;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('rev_info', migration_pks, rev, calculated_rev);
                CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('pm_configuration_raw', migration_pks, missing_entity_date_prefix, id_pk, calculated_id_pk);

                -- XML (BLOB > LONGBLOB)
                INSERT INTO MIGR_TB_PM_CONF_RAW_AUD (ID_PK, REV, REVTYPE, CONFIGURATION_DATE,
                                                              CONFIGURATIONDATE_MOD, DESCRIPTION, DESCRIPTION_MOD,
                                                              XML, XML_MOD)
                VALUES (calculated_id_pk,
                        calculated_rev,
                        revtype,
                        configuration_date,
                        configurationdate_mod,
                        description,
                        description_mod,
                        xml,
                        xml_mod);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_configuration_raw_aud;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_party_aud(INOUT migration_pks JSON, missing_entity_date_prefix DATE)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE rev BIGINT;
        DECLARE revtype TINYINT;
        DECLARE endpoint VARCHAR(1024);
        DECLARE endpoint_mod BIT(1);
        DECLARE name VARCHAR(255);
        DECLARE name_mod BIT(1);
        DECLARE password VARCHAR(255);
        DECLARE password_mod BIT(1);
        DECLARE username VARCHAR(255);
        DECLARE username_mod BIT(1);

        DECLARE calculated_rev BIGINT;
        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_party_aud CURSOR FOR
            SELECT PPA.ID_PK,
                    PPA.REV,
                    PPA.REVTYPE,
                    PPA.ENDPOINT,
                    PPA.ENDPOINT_MOD,
                    PPA.NAME,
                    PPA.NAME_MOD,
                    PPA.PASSWORD,
                    PPA.PASSWORD_MOD,
                    PPA.USERNAME,
                    PPA.USERNAME_MOD
            FROM TB_PM_PARTY_AUD PPA;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_PARTY_AUD';
        SET @v_tab_new := 'MIGR_TB_PM_PARTY_AUD';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_party_aud;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_party_aud -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_party_aud INTO id_pk, rev, revtype, endpoint, endpoint_mod, name, name_mod, password,
                        password_mod, username, username_mod;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('rev_info', migration_pks, rev, calculated_rev);
                CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('pm_party', migration_pks, missing_entity_date_prefix, id_pk, calculated_id_pk);

                INSERT INTO MIGR_TB_PM_PARTY_AUD (ID_PK, REV, REVTYPE, ENDPOINT, ENDPOINT_MOD, NAME, NAME_MOD,
                                                  PASSWORD, PASSWORD_MOD, USERNAME, USERNAME_MOD)
                VALUES (calculated_id_pk,
                        calculated_rev,
                        revtype,
                        endpoint,
                        endpoint_mod,
                        name,
                        name_mod,
                        password,
                        password_mod,
                        username,
                        username_mod);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_party_aud;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_party_id_type_aud(INOUT migration_pks JSON, missing_entity_date_prefix DATE)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE rev BIGINT;
        DECLARE revtype TINYINT;
        DECLARE name VARCHAR(255);
        DECLARE name_mod BIT(1);
        DECLARE value VARCHAR(1024);
        DECLARE value_mod BIT(1);

        DECLARE calculated_rev BIGINT;
        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_party_id_type_aud CURSOR FOR
            SELECT PPITA.ID_PK,
                    PPITA.REV,
                    PPITA.REVTYPE,
                    PPITA.NAME,
                    PPITA.NAME_MOD,
                    PPITA.VALUE,
                    PPITA.VALUE_MOD
            FROM TB_PM_PARTY_ID_TYPE_AUD PPITA;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_PARTY_ID_TYPE_AUD';
        SET @v_tab_new := 'MIGR_TB_PM_PARTY_ID_TYPE_AUD';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_party_id_type_aud;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_party_id_type_aud -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_party_id_type_aud INTO id_pk, rev, revtype, name, name_mod, value, value_mod;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('rev_info', migration_pks, rev, calculated_rev);
                CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('pm_party_id_type', migration_pks, missing_entity_date_prefix, id_pk, calculated_id_pk);

                INSERT INTO MIGR_TB_PM_PARTY_ID_TYPE_AUD (ID_PK, REV, REVTYPE, NAME, NAME_MOD, VALUE, VALUE_MOD)
                VALUES (calculated_id_pk,
                        calculated_rev,
                        revtype,
                        name,
                        name_mod,
                        value,
                        value_mod);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_party_id_type_aud;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_pm_party_identifier_aud(INOUT migration_pks JSON, missing_entity_date_prefix DATE)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE rev BIGINT;
        DECLARE revtype TINYINT;
        DECLARE fk_party BIGINT;

        DECLARE calculated_rev BIGINT;
        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_fk_party BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_pm_party_identifier_aud CURSOR FOR
            SELECT PPIA.ID_PK,
                    PPIA.REV,
                    PPIA.REVTYPE,
                    PPIA.FK_PARTY
            FROM TB_PM_PARTY_IDENTIFIER_AUD PPIA;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PM_PARTY_IDENTIFIER_AUD';
        SET @v_tab_new := 'MIGR_TB_PM_PARTY_IDENTIF_AUD';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_pm_party_identifier_aud;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_pm_party_identifier_aud -> execute immediate error: ', @p2));
                END;

                FETCH c_pm_party_identifier_aud INTO id_pk, rev, revtype, fk_party;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('rev_info', migration_pks, rev, calculated_rev);
                CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('pm_party_identifier', migration_pks, missing_entity_date_prefix, id_pk, calculated_id_pk);
                CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('pm_party', migration_pks, missing_entity_date_prefix, fk_party, calculated_fk_party);

                INSERT INTO MIGR_TB_PM_PARTY_IDENTIF_AUD (ID_PK, REV, REVTYPE, FK_PARTY)
                VALUES (calculated_id_pk,
                        calculated_rev,
                        revtype,
                        calculated_fk_party);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_pm_party_identifier_aud;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_routing_criteria_aud(INOUT migration_pks JSON, missing_entity_date_prefix DATE)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE rev BIGINT;
        DECLARE revtype TINYINT;
        DECLARE expression VARCHAR(255);
        DECLARE expression_mod BIT(1);
        DECLARE name VARCHAR(255);
        DECLARE name_mod BIT(1);

        DECLARE calculated_rev BIGINT;
        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_routing_criteria_aud CURSOR FOR
            SELECT RCA.ID_PK,
                    RCA.REV,
                    RCA.REVTYPE,
                    RCA.EXPRESSION,
                    RCA.EXPRESSION_MOD,
                    RCA.NAME,
                    RCA.NAME_MOD
            FROM TB_ROUTING_CRITERIA_AUD RCA;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_ROUTING_CRITERIA_AUD';
        SET @v_tab_new := 'MIGR_TB_ROUTING_CRITERIA_AUD';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_routing_criteria_aud;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_routing_criteria_aud -> execute immediate error: ', @p2));
                END;

                FETCH c_routing_criteria_aud INTO id_pk, rev, revtype, expression, expression_mod, name, name_mod;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('rev_info', migration_pks, rev, calculated_rev);
                CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('routing_criteria', migration_pks, missing_entity_date_prefix, id_pk, calculated_id_pk);

                INSERT INTO MIGR_TB_ROUTING_CRITERIA_AUD (ID_PK, REV, REVTYPE, EXPRESSION, EXPRESSION_MOD, NAME,
                                                          NAME_MOD)
                VALUES (calculated_id_pk,
                        calculated_rev,
                        revtype,
                        expression,
                        expression_mod,
                        name,
                        name_mod);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_routing_criteria_aud;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_user_aud(INOUT migration_pks JSON, missing_entity_date_prefix DATE)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE rev BIGINT;
        DECLARE revtype TINYINT;
        DECLARE user_enabled BIT(1);
        DECLARE active_mod BIT(1);
        DECLARE user_deleted BIT(1);
        DECLARE deleted_mod BIT(1);
        DECLARE user_email VARCHAR(255);
        DECLARE email_mod BIT(1);
        DECLARE user_password VARCHAR(255);
        DECLARE password_mod BIT(1);
        DECLARE user_name VARCHAR(255);
        DECLARE username_mod BIT(1);
        DECLARE optlock INT;
        DECLARE version_mod BIT(1);
        DECLARE roles_mod BIT(1);
        DECLARE password_change_date TIMESTAMP;
        DECLARE passwordchangedate_mod BIT(1);
        DECLARE default_password BIT(1);
        DECLARE defaultpassword_mod BIT(1);

        DECLARE calculated_rev BIGINT;
        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_user_aud CURSOR FOR
            SELECT UA.ID_PK,
                    UA.REV,
                    UA.REVTYPE,
                    UA.USER_ENABLED,
                    UA.ACTIVE_MOD,
                    UA.USER_DELETED,
                    UA.DELETED_MOD,
                    UA.USER_EMAIL,
                    UA.EMAIL_MOD,
                    UA.USER_PASSWORD,
                    UA.PASSWORD_MOD,
                    UA.USER_NAME,
                    UA.USERNAME_MOD,
                    UA.OPTLOCK,
                    UA.VERSION_MOD,
                    UA.ROLES_MOD,
                    UA.PASSWORD_CHANGE_DATE,
                    UA.PASSWORDCHANGEDATE_MOD,
                    UA.DEFAULT_PASSWORD,
                    UA.DEFAULTPASSWORD_MOD
            FROM TB_USER_AUD UA;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_USER_AUD';
        SET @v_tab_new := 'MIGR_TB_USER_AUD';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_user_aud;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_user_aud -> execute immediate error: ', @p2));
                END;

                FETCH c_user_aud INTO id_pk, rev, revtype, user_enabled, active_mod, user_deleted, deleted_mod,
                        user_email, email_mod, user_password, password_mod, user_name, username_mod, optlock,
                        version_mod, roles_mod, password_change_date, passwordchangedate_mod, default_password,
                        defaultpassword_mod;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('rev_info', migration_pks, rev, calculated_rev);
                CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('user', migration_pks, missing_entity_date_prefix, id_pk, calculated_id_pk);

                INSERT INTO MIGR_TB_USER_AUD (ID_PK, REV, REVTYPE, USER_ENABLED, ACTIVE_MOD, USER_DELETED, DELETED_MOD,
                                              USER_EMAIL, EMAIL_MOD, USER_PASSWORD, PASSWORD_MOD, USER_NAME,
                                              USERNAME_MOD, OPTLOCK, VERSION_MOD, ROLES_MOD, PASSWORD_CHANGE_DATE,
                                              PASSWORDCHANGEDATE_MOD, DEFAULT_PASSWORD, DEFAULTPASSWORD_MOD)
                VALUES (calculated_id_pk,
                        calculated_rev,
                        revtype,
                        user_enabled,
                        active_mod,
                        user_deleted,
                        deleted_mod,
                        user_email,
                        email_mod,
                        user_password,
                        password_mod,
                        user_name,
                        username_mod,
                        optlock,
                        version_mod,
                        roles_mod,
                        password_change_date,
                        passwordchangedate_mod,
                        default_password,
                        defaultpassword_mod);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_user_aud;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_user_role_aud(INOUT migration_pks JSON, missing_entity_date_prefix DATE)
    BEGIN
        DECLARE id_pk BIGINT;
        DECLARE rev BIGINT;
        DECLARE revtype TINYINT;
        DECLARE role_name VARCHAR(255);
        DECLARE name_mod BIT(1);
        DECLARE users_mod BIT(1);

        DECLARE calculated_rev BIGINT;
        DECLARE calculated_id_pk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_user_role_aud CURSOR FOR
            SELECT URA.ID_PK,
                    URA.REV,
                    URA.REVTYPE,
                    URA.ROLE_NAME,
                    URA.NAME_MOD,
                    URA.USERS_MOD
            FROM TB_USER_ROLE_AUD URA;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_USER_ROLE_AUD';
        SET @v_tab_new := 'MIGR_TB_USER_ROLE_AUD';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_user_role_aud;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_user_role_aud -> execute immediate error: ', @p2));
                END;

                FETCH c_user_role_aud INTO id_pk, rev, revtype, role_name, name_mod, users_mod;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('rev_info', migration_pks, rev, calculated_rev);
                CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('user_role', migration_pks, missing_entity_date_prefix, id_pk, calculated_id_pk);

                INSERT INTO MIGR_TB_USER_ROLE_AUD (ID_PK, REV, REVTYPE, ROLE_NAME, NAME_MOD, USERS_MOD)
                VALUES (calculated_id_pk,
                        calculated_rev,
                        revtype,
                        role_name,
                        name_mod,
                        users_mod);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_user_role_aud;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_migrate_user_roles_aud(INOUT migration_pks JSON, missing_entity_date_prefix DATE)
    BEGIN
        DECLARE rev BIGINT;
        DECLARE revtype TINYINT;
        DECLARE user_id BIGINT;
        DECLARE role_id BIGINT;

        DECLARE calculated_rev BIGINT;
        DECLARE calculated_user_id BIGINT;
        DECLARE calculated_role_id BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_user_roles_aud CURSOR FOR
            SELECT URA.REV,
                    URA.REVTYPE,
                    URA.USER_ID,
                    URA.ROLE_ID
            FROM TB_USER_ROLES_AUD URA;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_USER_ROLES_AUD';
        SET @v_tab_new := 'MIGR_TB_USER_ROLES_AUD';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_user_roles_aud;
        read_loop:
		LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_user_roles_aud -> execute immediate error: ', @p2));
                END;

                FETCH c_user_roles_aud INTO rev, revtype, user_id, role_id;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_lookup_migration_pk('rev_info', migration_pks, rev, calculated_rev);
                CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('user', migration_pks, missing_entity_date_prefix, user_id, calculated_user_id);
                CALL MIGRATE_42_TO_50_lookup_audit_migration_pk('user_role', migration_pks, missing_entity_date_prefix, role_id, calculated_role_id);

                INSERT INTO MIGR_TB_USER_ROLES_AUD (REV, REVTYPE, USER_ID, ROLE_ID)
                VALUES (calculated_rev,
                        revtype,
                        calculated_user_id,
                        calculated_role_id);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_user_roles_aud;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

/**-- main entry point for running the single tenancy or multitenancy non-general schema migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate()
    BEGIN
        SET @migration_pks := '{}';

        CALL MIGRATE_42_TO_50_prepare_timezone_offset(@migration_pks);

        -- keep it in this order
        -- START migrate to the new schema (including primary keys to the new format)
        CALL MIGRATE_42_TO_50_migrate_user_message(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_message_fragment(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_message_group(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_message_header(@migration_pks);

        CALL MIGRATE_42_TO_50_migrate_signal_receipt(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_message_log;

        CALL MIGRATE_42_TO_50_migrate_raw_envelope_log(@migration_pks);

        CALL MIGRATE_42_TO_50_migrate_property(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_part_info_user(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_part_info_property(@migration_pks);

        CALL MIGRATE_42_TO_50_migrate_error_log(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_message_acknw(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_send_attempt(@migration_pks);
        -- END migrate to the new schema (including primary keys to the new format)

        -- START migrate primary keys to new format
        CALL MIGRATE_42_TO_50_migrate_action_audit(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_alert(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_event(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_event_alert(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_event_property(@migration_pks);
        --
        CALL MIGRATE_42_TO_50_migrate_authentication_entry(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_plugin_user_passwd_history(@migration_pks);
        --
        CALL MIGRATE_42_TO_50_migrate_backend_filter(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_routing_criteria(@migration_pks);
        --
        CALL MIGRATE_42_TO_50_migrate_certificate(@migration_pks);
        --
        CALL MIGRATE_42_TO_50_migrate_command(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_command_property(@migration_pks);
        --
        CALL MIGRATE_42_TO_50_migrate_encryption_key; -- SECRET_KEY & INIT_VECTOR (BLOB > LONGBLOB)
        --
        CALL MIGRATE_42_TO_50_migrate_message_acknw_prop(@migration_pks);
        --
        -- CALL MIGRATE_42_TO_50_migrate_message_ui; -- not part of this task (UI replication to be fixed later)
        --
        CALL MIGRATE_42_TO_50_migrate_messaging_lock(@migration_pks);
        --
        CALL MIGRATE_42_TO_50_migrate_pm_business_process(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_action(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_agreement(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_error_handling(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_mep(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_mep_binding(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_message_property(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_message_property_set(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_join_property_set(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_party(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_configuration(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_mpc(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_party_id_type(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_party_identifier(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_payload(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_payload_profile(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_join_payload_profile(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_reception_awareness(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_reliability(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_role(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_security(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_service(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_splitting(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_leg(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_leg_mpc(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_process(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_join_process_init_party(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_join_process_leg(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_pm_join_process_resp_party(@migration_pks);
        --
        CALL MIGRATE_42_TO_50_migrate_pm_configuration_raw(@migration_pks); -- XML (BLOB > LONGBLOB)
        --
        CALL MIGRATE_42_TO_50_migrate_user(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_user_password_history(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_user_role(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_user_roles(@migration_pks);
        --
        CALL MIGRATE_42_TO_50_migrate_ws_plugin_tb_message_log;
        --
        SET @missing_entity_date_prefix := SYSDATE();
        CALL MIGRATE_42_TO_50_migrate_rev_info(@migration_pks);
        CALL MIGRATE_42_TO_50_migrate_rev_changes(@migration_pks, @missing_entity_date_prefix);
        CALL MIGRATE_42_TO_50_migrate_authentication_entry_aud(@migration_pks, @missing_entity_date_prefix);
        CALL MIGRATE_42_TO_50_migrate_back_rcriteria_aud(@migration_pks, @missing_entity_date_prefix);
        CALL MIGRATE_42_TO_50_migrate_backend_filter_aud(@migration_pks, @missing_entity_date_prefix);
        CALL MIGRATE_42_TO_50_migrate_certificate_aud(@migration_pks, @missing_entity_date_prefix);
        CALL MIGRATE_42_TO_50_migrate_pm_configuration_aud(@migration_pks, @missing_entity_date_prefix);
        CALL MIGRATE_42_TO_50_migrate_pm_configuration_raw_aud(@migration_pks, @missing_entity_date_prefix); -- XML (BLOB > LONGBLOB)
        CALL MIGRATE_42_TO_50_migrate_pm_party_aud(@migration_pks, @missing_entity_date_prefix);
        CALL MIGRATE_42_TO_50_migrate_pm_party_id_type_aud(@migration_pks, @missing_entity_date_prefix);
        CALL MIGRATE_42_TO_50_migrate_pm_party_identifier_aud(@migration_pks, @missing_entity_date_prefix);
        CALL MIGRATE_42_TO_50_migrate_routing_criteria_aud(@migration_pks, @missing_entity_date_prefix);
        CALL MIGRATE_42_TO_50_migrate_user_aud(@migration_pks, @missing_entity_date_prefix);
        CALL MIGRATE_42_TO_50_migrate_user_role_aud(@migration_pks, @missing_entity_date_prefix);
        CALL MIGRATE_42_TO_50_migrate_user_roles_aud(@migration_pks, @missing_entity_date_prefix);
        -- END migrate primary keys to new format
    END
//

/**-- main entry point for running the multitenancy general schema migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_multitenancy()
BEGIN
    SET @migration_pks := '{}';

    -- keep it in this order
    CALL MIGRATE_42_TO_50_prepare_timezone_offset(@migration_pks);

    -- START migrate primary keys to new format
    CALL MIGRATE_42_TO_50_migrate_alert(@migration_pks);
    CALL MIGRATE_42_TO_50_migrate_event(@migration_pks);
    CALL MIGRATE_42_TO_50_migrate_event_alert(@migration_pks);
    CALL MIGRATE_42_TO_50_migrate_event_property(@migration_pks);
    --
    CALL MIGRATE_42_TO_50_migrate_command(@migration_pks);
    CALL MIGRATE_42_TO_50_migrate_command_property(@migration_pks);
    --
    CALL MIGRATE_42_TO_50_migrate_user_domain;
    --
    CALL MIGRATE_42_TO_50_migrate_user(@migration_pks);
    CALL MIGRATE_42_TO_50_migrate_user_password_history(@migration_pks);
    CALL MIGRATE_42_TO_50_migrate_user_role(@migration_pks);
    CALL MIGRATE_42_TO_50_migrate_user_roles(@migration_pks);
    --
    SET @missing_entity_date_prefix := SYSDATE();
    CALL MIGRATE_42_TO_50_migrate_rev_info(@migration_pks);
    CALL MIGRATE_42_TO_50_migrate_rev_changes(@migration_pks, @missing_entity_date_prefix);
    CALL MIGRATE_42_TO_50_migrate_user_role_aud(@migration_pks, @missing_entity_date_prefix);
    CALL MIGRATE_42_TO_50_migrate_user_roles_aud(@migration_pks, @missing_entity_date_prefix);
    -- END migrate primary keys to new format
END
//