package eu.domibus.core.message.signal;

import com.google.common.collect.ImmutableMap;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.message.MessageLogInfoFilterTest;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(JMockit.class)
public class SignalMessageLogInfoFilterTest {

    @Injectable
    private DomibusPropertyProvider domibusProperties;

    public static final String QUERY = "select new eu.domibus.core.message.MessageLogInfo(log, partyFrom.value, partyTo.value, propsFrom.value, propsTo.value, info.refToMessageId) from SignalMessageLog log, " +
            "SignalMessage message " +
            "left join log.messageInfo info " +
            "left join message.messageProperties.property propsFrom " +
            "left join message.messageProperties.property propsTo " +
            "left join message.partyInfo.from.fromPartyId partyFrom " +
            "left join message.partyInfo.to.toPartyId partyTo " +
            "where message.messageInfo = info and propsFrom.name = 'originalSender'" +
            "and propsTo.name = 'finalRecipient'";

    @Tested
    SignalMessageLogInfoFilter signalMessageLogInfoFilter;

    private static HashMap<String, Object> filters = new HashMap<>();

    @BeforeClass
    public static void before() {
        filters = MessageLogInfoFilterTest.returnFilters();
    }

    @Test
    public void createSignalMessageLogInfoFilter() {

        new Expectations(signalMessageLogInfoFilter) {{
            signalMessageLogInfoFilter.filterQuery(anyString,anyString,anyBoolean,filters);
            result = QUERY;

            signalMessageLogInfoFilter.isFourCornerModel();
            result = true;
        }};

        String query = signalMessageLogInfoFilter.filterMessageLogQuery("column", true, filters);

        Assert.assertEquals(QUERY, query);
    }

    @Test
    public void testGetHQLKeyConversationId() {
        Assert.assertEquals("", signalMessageLogInfoFilter.getHQLKey("conversationId"));
    }

    @Test
    public void testGetHQLKeyMessageId() {
        Assert.assertEquals("signal.signalMessageId", signalMessageLogInfoFilter.getHQLKey("messageId"));
    }

    @Test
    public void testFilterQuery() {
        StringBuilder resultQuery = signalMessageLogInfoFilter.filterQuery("select * from table where column = ''","messageId", true, filters);
        String resultQueryString = resultQuery.toString();

        Assert.assertFalse(resultQueryString.contains("log.notificationStatus.status = :notificationStatus"));
        Assert.assertTrue(resultQueryString.contains("partyFrom.value = :fromPartyId"));
        Assert.assertFalse(resultQueryString.contains("log.sendAttemptsMax = :sendAttemptsMax"));
        Assert.assertTrue(resultQueryString.contains("propsFrom.value = :originalSender"));
        Assert.assertTrue(resultQueryString.contains("log.received <= :receivedTo"));
        Assert.assertTrue(resultQueryString.contains("signal.signalMessageId = :messageId"));
        Assert.assertTrue(resultQueryString.contains("signal.refToMessageId = :refToMessageId"));
        Assert.assertTrue(resultQueryString.contains("log.received = :received"));
        Assert.assertFalse(resultQueryString.contains("log.sendAttempts = :sendAttempts"));
        Assert.assertTrue(resultQueryString.contains("propsTo.value = :finalRecipient"));
        Assert.assertFalse(resultQueryString.contains("log.nextAttempt = :nextAttempt"));
        Assert.assertTrue(resultQueryString.contains("log.messageStatus.messageStatus = :messageStatus"));
        Assert.assertTrue(resultQueryString.contains("log.deleted = :deleted"));
        Assert.assertTrue(resultQueryString.contains("log.received >= :receivedFrom"));
        Assert.assertTrue(resultQueryString.contains("partyTo.value = :toPartyId"));
        Assert.assertTrue(resultQueryString.contains("log.mshRole.role = :mshRole"));
        Assert.assertTrue(resultQueryString.contains("order by signal.signalMessageId asc"));

        Assert.assertFalse(resultQueryString.contains("conversationId"));
        Assert.assertFalse(resultQueryString.contains("message.collaborationInfo.conversationId"));
    }

    @Test
    public void createFromClause_MessageTableNotDirectly() {
        Map<String, Object> filters = ImmutableMap.of(
                "messageId", "111",
                "fromPartyId", "222",
                "originalSender", "333");

        String messageTable = "join log.signalMessage signal join signal.userMessage message";
        String partyFromTable = "left join message.partyInfo.from.fromPartyId partyFrom ";
        String propsCriteria = "and propsFrom.name = 'originalSender' ";

        String result = signalMessageLogInfoFilter.getCountQueryBody(filters);

        Assert.assertTrue(result.contains(signalMessageLogInfoFilter.getMainTable()));
        Assert.assertTrue(result.contains(messageTable));
        Assert.assertTrue(result.contains(partyFromTable));
        Assert.assertTrue(result.contains(propsCriteria));
    }
}
