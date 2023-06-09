package eu.domibus.core.audit.envers;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Indicates which type of modification has been done on the entity.
 */
public enum ModificationType {
    /**
     * Indicates that the entity was added.
     */
    ADD("Created", 1),
    /**
     * Indicates that the entity was modified.
     */
    MOD("Modified", 2),
    /**
     * Indicates that the entity was deleted.
     */
    DEL("Deleted", 0),
    /**
     * Indicates the message was downloaded.
     */
    DOWNLOADED("Downloaded", 3),
    /**
     * Indicates the message was resent.
     */
    RESENT("Resent", 4),
    /**
     * Indicates the message was moved from a queue.
     */
    MOVED("Moved", 5),
    /**
     * Indicates the user message envelope was downloaded.
     */
    USER_MESSAGE_ENVELOPE_DOWNLOADED("UserMessageEnvelopeDownloaded", 6),
    /**
     * Indicates the signal message envelope was downloaded.
     */
    SIGNAL_MESSAGE_ENVELOPE_DOWNLOADED("SignalMessageEnvelopeDownloaded", 7);

    private final String label;

    private int order;

    ModificationType(final String label, final int order) {
        this.label = label;
        this.order = order;
    }

    public String getLabel() {
        return label;
    }

    public int getOrder() {
        return order;
    }
}
