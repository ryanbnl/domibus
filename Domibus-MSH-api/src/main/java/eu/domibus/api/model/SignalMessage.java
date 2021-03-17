package eu.domibus.api.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Entity
@Table(name = "TB_SIGNAL_MESSAGE")
@NamedQueries({
        @NamedQuery(name = "SignalMessage.findSignalMessageIdByRefMessageId",
                query = "select signalMessage.messageInfo.messageId from SignalMessage signalMessage where signalMessage.messageInfo.refToMessageId = :ORI_MESSAGE_ID"),
        @NamedQuery(name = "SignalMessage.findSignalMessageByRefMessageId",
                query = "select signalMessage from SignalMessage signalMessage where signalMessage.messageInfo.refToMessageId = :ORI_MESSAGE_ID"),
        @NamedQuery(name = "SignalMessage.findReceiptIdsByMessageIds",
                query = "select signalMessage.receipt.entityId from SignalMessage signalMessage where signalMessage.messageInfo.messageId IN :MESSAGEIDS"),
})
public class SignalMessage extends AbstractNoGeneratedPkEntity {

    @Column(name = "MESSAGE_ID", nullable = false, unique = true, updatable = false)
    @NotNull
    protected String signalMessageId;

    @Column(name = "REF_TO_MESSAGE_ID")
    protected String refToMessageId;

    @Column(name = "EBMS3_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private UserMessage userMessage;

    public String getSignalMessageId() {
        return signalMessageId;
    }

    public void setSignalMessageId(String messageId) {
        this.signalMessageId = messageId;
    }

    public String getRefToMessageId() {
        return refToMessageId;
    }

    public void setRefToMessageId(String refToMessageId) {
        this.refToMessageId = refToMessageId;
    }

    public UserMessage getUserMessage() {
        return userMessage;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setUserMessage(UserMessage userMessage) {
        this.userMessage = userMessage;
    }
}
