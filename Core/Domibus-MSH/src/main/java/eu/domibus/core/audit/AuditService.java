package eu.domibus.core.audit;

import eu.domibus.api.audit.AuditLog;
import eu.domibus.api.audit.envers.RevisionLogicalName;
import eu.domibus.api.model.MSHRole;
import eu.domibus.core.audit.envers.ModificationType;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Service in charge of retrieving the audit logs.
 * The audit component will track historical changes on a set of tables.
 */
public interface AuditService {
    /**
     * Retrieve the list of audit for the given criterias.
     *
     * @param auditTargetName the type of audit to retrive (Message, User, etc...).
     * @param action          the type of action to retrieve (DEL/ADD/MODD).
     * @param user            the users that did the modifications.
     * @param from            the lower bound of the modification date.
     * @param to              the higher bound of the modification date.
     * @param start           the pagination start at.
     * @param max             the page number of reccords.
     * @return a list of audit.
     */
    List<AuditLog> listAudit(Set<String> auditTargetName,
                             Set<String> action,
                             Set<String> user,
                             Date from,
                             Date to,
                             int start,
                             int max,
                             boolean domain);

    /**
     * Count the number of audit for the given criterias.
     *
     * @param auditTargetName the type of audit to retrive (Message, User, etc...).
     * @param action          the type of action to retrieve (DEL/ADD/MODD).
     * @param user            the users that did the modifications.
     * @param from            the lower bound of the modification date.
     * @param to              the higher bound of the modification date.
     * @return the audit count.
     */
    Long countAudit(Set<String> auditTargetName,
                    Set<String> action,
                    Set<String> user,
                    Date from,
                    Date to,
                    boolean domain);

    /**
     * Entities targeted by the auditing system have a logical name.{@link RevisionLogicalName}
     *
     * @return the logical names.
     */
    List<String> listAuditTarget();

    /**
     * Add message download audit for a message.
     *
     * @param messageId the id of the message.
     * @param mshRole
     */
    void addMessageDownloadedAudit(String messageId, MSHRole mshRole);

    /**
     * Add download audit for a pmode.
     *
     * @param entityId the id of the downloaded pmode.
     */
    void addPModeDownloadedAudit(long entityId);

    /**
     * Add download audit for a pmode Archive
     *
     * @param entityId the id of the downloaded pmode archive.
     */
    void addPModeArchiveDownloadedAudit(long entityId);

    /**
     * Add message resent audit for a message.
     *
     * @param messageId the id of the message.
     */
    void addMessageResentAudit(String messageId);

    /**
     * Add message deleted audit for a jms message.
     *
     * @param messageId the id of the message.
     * @param fromQueue the queue from which the message was deleted.
     */
    void addJmsMessageDeletedAudit(
            String messageId,
            String fromQueue,
            String domainCode);

    /**
     * Add message moved audit for a message.
     *
     * @param messageId  the id of the message.
     * @param fromQueue  the queue from which the message was moved.
     * @param toQueue    the queue to which the message was moved.
     * @param domainCode
     */
    void addJmsMessageMovedAudit(
            String messageId,
            String fromQueue, String toQueue, String domainCode);

    /**
     * Add keystore downloaded
     *
     * @param id    the id of the {@link eu.domibus.core.crypto.TruststoreEntity}
     */
    void addKeystoreDownloadedAudit(String id);

    /**
     * Add Certificate added to TLS
     *
     * @param id    the id of the {@link eu.domibus.core.crypto.TruststoreEntity}
     */
    void addCertificateAddedAudit(String id);

    /**
     * Add Certificate removed from TLS
     *
     * @param id    the id of the {@link eu.domibus.core.crypto.TruststoreEntity}
     */
    void addCertificateRemovedAudit(String id);

    /**
     * Add download audit for am envelope of user or signal messages
     *
     * @param messageId        the id of the message.
     * @param modificationType the modification type: either user or signal MessageEnvelopeDownload
     */
    void addMessageEnvelopesDownloadedAudit(String messageId, ModificationType modificationType);

    void addStoreReplacedAudit(String storeName);

}

