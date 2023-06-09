package eu.domibus.core.message;

import eu.domibus.AbstractIT;
import eu.domibus.ITTestsService;
import eu.domibus.api.model.MessageType;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.common.MessageDaoTestUtil;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.web.rest.ro.MessageLogRO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Transactional
public class MessagesLogServiceImplTestIT extends AbstractIT {

    @Autowired
    MessagesLogServiceImpl messagesLogService;

    @Autowired
    MessageDaoTestUtil messageDaoTestUtil;

    @Autowired
    ITTestsService itTestsService;

    @Autowired
    protected RoutingService routingService;


    @Before
    public void before() throws IOException, XmlProcessingException {
        uploadPmode();
    }

    @Test
    public void countMessages() {
    }

    @Test
    public void countAndFindPaged() {
        final HashMap<String, Object> filters = new HashMap<>();
        filters.put("receivedTo", new Date());
        messagesLogService.countAndFindPaged(MessageType.USER_MESSAGE, 0, 10, "received", false, filters);
    }

}
