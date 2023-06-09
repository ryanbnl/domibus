package eu.domibus.web.rest;

import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.jms.JMSDestination;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.jms.MessagesActionRequest;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.jms.spi.InternalJMSException;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.*;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class JmsResourceTest {

    public static final List<String> MESSAGES_IDS = Arrays.asList("message1", "message2");
    public static final String DOMIBUS_QUEUE_1 = "domibus.queue1";
    public static final String SOURCE_1 = "source1";

    @Mocked
    private MessagesActionRequestRO messagesActionRequestRO;

    @Mocked
    private MessagesActionRequest messagesActionRequest;

    @Tested
    private JmsResource jmsResource;

    @Injectable
    private JMSManager jmsManager;

    @Injectable
    private AuditService auditService;

    @Injectable
    private CsvServiceImpl csvServiceImpl;

    @Injectable
    private ErrorHandlerService errorHandlerService;

    @Injectable
    DomibusCoreMapper coreMapper;

    @Test
    public void testDestinations() {
        SortedMap<String, JMSDestination> dests = new TreeMap<>();
        // Given
        new Expectations() {{
            jmsManager.getDestinations();
            result = dests;
        }};

        // When
        DestinationsResponseRO destinations = jmsResource.destinations();

        // Then
        Assert.assertNotNull(destinations);
        Assert.assertEquals(dests, destinations.getJmsDestinations());

        new FullVerifications() {
        };
    }

    @Test
    public void testAction_wrongAction() {
        // Given
        new Expectations() {{
            messagesActionRequestRO.getAction();
            result = null;
        }};

        // When
        try {
            jmsResource.action(messagesActionRequestRO);
            Assert.fail();
        } catch (RequestValidationException e) {
            //do nothing
        }

        new FullVerifications() {
        };
    }

    @Test
    public void testActionMove_ok(final @Mocked SortedMap<String, JMSDestination> dests,
                                  final @Mocked JMSDestination queue1,
                                  final @Mocked JMSDestination queue2) {
        // Given
        new Expectations() {{
            messagesActionRequestRO.getSelectedMessages();
            result = MESSAGES_IDS;

            messagesActionRequestRO.getAction();
            result = MessagesActionRequestRO.Action.MOVE;

            messagesActionRequestRO.getDestination();
            result = DOMIBUS_QUEUE_1;

            messagesActionRequestRO.getSource();
            result = SOURCE_1;
        }};
        // When
        MessagesActionResponseRO responseEntity = jmsResource.action(messagesActionRequestRO);

        // Then
        Assert.assertNotNull(responseEntity);
        Assert.assertEquals("Success", responseEntity.getOutcome());

        new FullVerifications() {{
            jmsManager.moveMessages(SOURCE_1, DOMIBUS_QUEUE_1, MESSAGES_IDS.toArray(new String[0]));
            times = 1;
        }};
    }

    @Test
    public void testActionRemove() {
        // Given
        new Expectations() {{
            messagesActionRequestRO.getSelectedMessages();
            result = MESSAGES_IDS;

            messagesActionRequestRO.getAction();
            result = MessagesActionRequestRO.Action.REMOVE;

            messagesActionRequestRO.getSource();
            result = "source1";
        }};

        // When
        MessagesActionResponseRO responseEntity = jmsResource.action(messagesActionRequestRO);

        // Then
        Assert.assertNotNull(responseEntity);
        Assert.assertEquals("Success", responseEntity.getOutcome());

        new FullVerifications() {{
            jmsManager.deleteMessages(anyString, (String[]) any);
            times = 1;
        }};
    }

    @Test
    public void testActionRemove_InternalJMSException() {
        // Given
        new Expectations() {{
            messagesActionRequestRO.getSelectedMessages();
            result = Arrays.asList("message1", "message2");

            messagesActionRequestRO.getAction();
            result = MessagesActionRequestRO.Action.REMOVE;

            messagesActionRequestRO.getSource();
            result = "source1";

            jmsManager.deleteMessages(anyString, (String[]) any);
            times = 1;
            result = new InternalJMSException();
        }};
        // When
        try {
            jmsResource.action(messagesActionRequestRO);
        } catch (InternalJMSException e) {
            //Do nothing
        }
        // Then
        new FullVerifications() {
        };
    }

}
