package eu.domibus.web.rest.ro;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import java.util.Date;

/**
 * TestService Message object
 * @author Tiago Miguel
 * @since 4.0
 */
public class TestServiceMessageInfoRO {

    String partyId;

    String accessPoint;

    Date timeReceived;

    String messageId;

    MSHRole mshRole;

    MessageStatus messageStatus;

    TestErrorsInfoRO errorInfo;

    public TestErrorsInfoRO getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(TestErrorsInfoRO errorInfo) {
        this.errorInfo = errorInfo;
    }

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public String getAccessPoint() {
        return accessPoint;
    }

    public void setAccessPoint(String accessPoint) {
        this.accessPoint = accessPoint;
    }

    public Date getTimeReceived() {
        return timeReceived;
    }

    public void setTimeReceived(Date timeReceived) {
        this.timeReceived = timeReceived;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public MessageStatus getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(MessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    public MSHRole getMshRole() {
        return mshRole;
    }

    public void setMshRole(MSHRole mshRole) {
        this.mshRole = mshRole;
    }
}
