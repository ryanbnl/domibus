package eu.domibus.archive.client.webhook.model;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public enum BatchStatusType {
    /**
     * Batch had been created
     * CHANGE starting to QUEUED
     */
    QUEUED,
    /**
     * Batch had been picked up by for processing
     */
    STARTED,
    /**
     * Batch has failed
     */
    RETRIED,
    /**
     * Batch has failed to export all messages
     */
    FAILED,
    /**
     * Batch was completed
     */
    EXPORTED,
    /**
     * End status of client does not notify ARCHIVED in time
     */
    EXPIRED,
    /**
     * Batch client finished the archived the batch
     */
    ARCHIVED,
    /**
     * Batch client notified of a failure in the archiving process
     */
    ARCHIVE_FAILED,
    /**
     * Deleted by the Domibus if the batch is expired, or ARCHIVED or failed to archived by client
     *
     * Deletes only filesystem data!
     */
    DELETED,
}
