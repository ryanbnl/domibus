package eu.domibus.core.message.attempt;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.converter.MessageCoreMapper;
import eu.domibus.core.message.UserMessageDao;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RunWith(JMockit.class)
public class MessageAttemptDefaultServiceTest {

    @Tested
    MessageAttemptDefaultService messageAttemptDefaultService;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    MessageAttemptDao messageAttemptDao;

    @Injectable
    MessageCoreMapper messageCoreConverter;

    @Injectable
    UserMessageDao userMessageDao;

    @Test
    public void testGetAttemptsHistory(@Injectable final List<MessageAttemptEntity> entities) throws Exception {
        final String messageId = "1";

        new Expectations() {{
            messageAttemptDao.findByMessageId(messageId, MSHRole.SENDING);
            result = entities;
        }};

        messageAttemptDefaultService.getAttemptsHistory(messageId);

        new Verifications() {{
            messageCoreConverter.messageAttemptEntityListToMessageAttemptList(entities);
        }};
    }

    @Test
    public void testCreate(@Injectable final MessageAttemptEntity entity, @Injectable final MessageAttempt attempt) throws Exception {
        new Expectations(messageAttemptDefaultService) {{
            messageAttemptDefaultService.isMessageAttemptAuditDisabled();
            result = false;

            messageCoreConverter.messageAttemptToMessageAttemptEntity(attempt);
            result = entity;

        }};

        messageAttemptDefaultService.create(attempt);

        new Verifications() {{
            messageAttemptDao.create(entity);
        }};
    }
}
