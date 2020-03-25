package eu.domibus.core.message.pull;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.nonrepudiation.RawEnvelopeLogDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.model.MessageState;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.ebms3.sender.retry.UpdateRetryLoggingService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(JMockit.class)

public class PullMessageServiceTest {

    private final static String MESSAGE_ID = "MESSAGE_ID";

    private final static String MPC = "MPC";

    @Tested
    private PullMessageServiceImpl pullService;

    @Injectable
    private MessagingLockDao messagingLockDao;

    @Injectable
    MpcService mpcService;

    @Injectable
    private BackendNotificationService backendNotificationService;

    @Injectable
    private MessagingDao messagingDao;

    @Injectable
    private PullMessageStateService pullMessageStateService;

    @Injectable
    private UpdateRetryLoggingService updateRetryLoggingService;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private RawEnvelopeLogDao rawEnvelopeLogDao;

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    private java.util.Properties domibusProperties;

    @Injectable
    private UIReplicationSignalService uiReplicationSignalService;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    UserMessageLogDefaultService userMessageLogDefaultService;

    @Test
    @Ignore
    public void pullMessageForTheFirstTime() {
        final String initiator = "initiator";


        new Expectations() {
            {
                messagingLockDao.getNextPullMessageToProcess(6L);
                result = MESSAGE_ID;
            }
        };
        String pullMessageId = pullService.getPullMessageId(initiator, MPC);
        assertEquals(MESSAGE_ID, pullMessageId);
    }

    @Test
    @Ignore
    public void addSearchInFormation(@Mocked final PartyIdExtractor partyIdExtractor) {

        final String partyId = "partyId";
        new Expectations() {
            {
                partyIdExtractor.getPartyId();
                result = partyId;
            }
        };
        pullService.addPullMessageLock(partyIdExtractor, "", null);
        new Verifications() {{
            MessagingLock messagingLock = new MessagingLock();
            // messagingLockDao.releaseLock(messagingLock = withCapture());
            //assertEquals(MESSAGE_ID, messagingLock.getMessageId());
            assertEquals(MessageState.READY, messagingLock.getMessageState());
            assertEquals(MessagingLock.PULL, messagingLock.getMessageType());
            assertEquals(MPC, messagingLock.getMpc());
        }};
    }

    @Test
    public void delete() {
        pullService.deletePullMessageLock(MESSAGE_ID);
        new Verifications() {{
            messagingLockDao.delete(MESSAGE_ID);
        }};
    }
}