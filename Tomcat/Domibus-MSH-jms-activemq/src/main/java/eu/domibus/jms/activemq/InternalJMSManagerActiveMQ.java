package eu.domibus.jms.activemq;

import eu.domibus.api.cluster.CommandProperty;
import eu.domibus.api.jms.JMSDestinationHelper;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.server.ServerInfoService;
import eu.domibus.jms.spi.*;
import eu.domibus.jms.spi.helper.JMSSelectorUtil;
import eu.domibus.jms.spi.helper.JmsMessageCreator;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Queue;
import javax.jms.*;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;
import java.util.*;

import static eu.domibus.jms.spi.InternalJMSConstants.*;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
@Component
public class InternalJMSManagerActiveMQ implements InternalJMSManager {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(InternalJMSManagerActiveMQ.class);

    private final DomibusJMSActiveMQConnectionManager domibusJMSActiveMQConnectionManager;

    private final JMSDestinationHelper jmsDestinationHelper;

    private final JmsOperations jmsSender;

    private final JMSSelectorUtil jmsSelectorUtil;

    private final AuthUtils authUtils;

    private final DomibusConfigurationService domibusConfigurationService;

    private final ServerInfoService serverInfoService;

    public InternalJMSManagerActiveMQ(DomibusJMSActiveMQConnectionManager domibusJMSActiveMQConnectionManager,
                                      JMSDestinationHelper jmsDestinationHelper,
                                      @Qualifier("jmsSender") JmsOperations jmsSender,
                                      JMSSelectorUtil jmsSelectorUtil,
                                      AuthUtils authUtils,
                                      DomibusConfigurationService domibusConfigurationService,
                                      ServerInfoService serverInfoService) {
        this.domibusJMSActiveMQConnectionManager = domibusJMSActiveMQConnectionManager;
        this.jmsDestinationHelper = jmsDestinationHelper;
        this.jmsSender = jmsSender;
        this.jmsSelectorUtil = jmsSelectorUtil;
        this.authUtils = authUtils;
        this.domibusConfigurationService = domibusConfigurationService;
        this.serverInfoService = serverInfoService;
    }

    @Override
    public Map<String, InternalJMSDestination> findDestinationsGroupedByFQName() {
        Map<String, InternalJMSDestination> destinationMap = new TreeMap<>();

        try {
            for (ObjectName name : getQueueMap().values()) {
                QueueViewMBean queueMbean = domibusJMSActiveMQConnectionManager.getQueueViewMBean(name);
                InternalJMSDestination internalJmsDestination = createInternalJmsDestination(queueMbean);
                destinationMap.put(queueMbean.getName(), internalJmsDestination);
            }
            return destinationMap;
        } catch (Exception e) {
            throw new InternalJMSException("Error getting destinations", e);
        }
    }

    protected InternalJMSDestination createInternalJmsDestination(QueueViewMBean queueMbean) {
        InternalJMSDestination internalJmsDestination = new InternalJMSDestination();
        internalJmsDestination.setName(queueMbean.getName());
        internalJmsDestination.setInternal(jmsDestinationHelper.isInternal(queueMbean.getName()));
        internalJmsDestination.setType(InternalJMSDestination.QUEUE_TYPE);
        internalJmsDestination.setNumberOfMessages(getMessagesTotalCount(queueMbean));
        return internalJmsDestination;
    }

    protected long getMessagesTotalCount(QueueViewMBean queueMbean) {
        if (domibusConfigurationService.isMultiTenantAware() && !authUtils.isSuperAdmin()) {
            //in multi-tenancy mode we show the number of messages only to super admin
            return NB_MESSAGES_ADMIN;
        }
        return queueMbean.getQueueSize();
    }
    
    protected Map<String, ObjectName> getQueueMap() {
        return domibusJMSActiveMQConnectionManager.getQueueMap();
    }

    @Override
    public void sendMessage(InternalJmsMessage message, String destination) {
        sendMessage(message, destination, jmsSender);
    }

    @Override
    public void sendMessage(InternalJmsMessage message, String destination, JmsOperations jmsOperations) {
        ActiveMQQueue activeMQQueue = new ActiveMQQueue(destination);
        sendMessage(message, activeMQQueue, jmsOperations);
    }

    @Override
    public void sendMessage(InternalJmsMessage message, Destination destination) {
        sendMessage(message, destination, jmsSender);
    }

    @Override
    public void sendMessage(InternalJmsMessage message, Destination destination, JmsOperations jmsOperations) {
        jmsOperations.send(destination, new JmsMessageCreator(message));
    }

    @Override
    public void sendMessageToTopic(InternalJmsMessage internalJmsMessage, Topic destination) {
        sendMessageToTopic(internalJmsMessage, destination, false);
    }

    @Override
    public void sendMessageToTopic(InternalJmsMessage internalJmsMessage, Topic destination, boolean excludeOrigin) {
        if (excludeOrigin) {
            internalJmsMessage.setProperty(CommandProperty.ORIGIN_SERVER, serverInfoService.getServerName());
        }
        sendMessage(internalJmsMessage, destination);
    }

    @Override
    public int deleteMessages(String source, String[] messageIds) {
        try {
            QueueViewMBean queue = domibusJMSActiveMQConnectionManager.getQueueViewMBean(source);
            return queue.removeMatchingMessages(jmsSelectorUtil.getSelector(messageIds));
        } catch (Exception e) {
            throw new InternalJMSException("Failed to delete messages from source [" + source + "]:" + Arrays.toString(messageIds), e);
        }
    }

    @Override
    public void deleteAllMessages(String source) {
        try {
            QueueViewMBean queue = domibusJMSActiveMQConnectionManager.getQueueViewMBean(source);
            queue.purge();
        } catch (Exception e) {
            throw new InternalJMSException("Failed to delete messages from source [" + source + "]", e);
        }
    }

    @Override
    public InternalJmsMessage getMessage(String source, String messageId) {
        try {
            QueueViewMBean queue = domibusJMSActiveMQConnectionManager.getQueueViewMBean(source);
            CompositeData messageMetaData = queue.getMessage(messageId);
            return convertCompositeData(messageMetaData);
        } catch (OpenDataException e) {
            throw new InternalJMSException("Failed to get message with id [" + messageId + "]", e);
        }
    }

    @Override
    public List<InternalJmsMessage> browseClusterMessages(String source, String selector) {
        return browseMessages(source, null, null, null, selector);
    }

    @Override
    public List<InternalJmsMessage> browseMessages(String source, String jmsType, Date fromDate, Date toDate, String selectorClause) {
        if (StringUtils.isEmpty(source)) {
            throw new InternalJMSException("Source has not been specified");
        }
        InternalJMSDestination destination = findDestinationsGroupedByFQName().get(source);
        if (destination == null) {
            throw new InternalJMSException("Could not find destination for [" + source + "]");
        }

        return getInternalJmsMessages(source, jmsType, fromDate, toDate, selectorClause, destination.getType());

    }

    private List<InternalJmsMessage> getInternalJmsMessages(String source, String jmsType, Date fromDate, Date toDate, String selectorClause, String destinationType) {
        List<InternalJmsMessage> internalJmsMessages = new ArrayList<>();
        if (QUEUE.equals(destinationType)) {
            String selector = getSelector(jmsType, fromDate, toDate, selectorClause);
            try {
                QueueViewMBean queue = domibusJMSActiveMQConnectionManager.getQueueViewMBean(source);
                CompositeData[] browse = queue.browse(selector);
                internalJmsMessages.addAll(convertCompositeData(browse));
            } catch (Exception e) {
                throw new InternalJMSException("Error getting messages for [" + source + "] with selector [" + selector + "]", e);
            }
        } else {
            throw new InternalJMSException("Unrecognized destination type [" + destinationType + "]");
        }
        return internalJmsMessages;
    }

    private String getSelector(String jmsType, Date fromDate, Date toDate, String selectorClause) {
        Map<String, Object> criteria = new HashMap<>();
        if (jmsType != null) {
            criteria.put(CRITERIA_JMS_TYPE, jmsType);
        }
        if (fromDate != null) {
            criteria.put(CRITERIA_JMS_TIMESTAMP_FROM, fromDate.getTime());
        }
        if (toDate != null) {
            criteria.put(CRITERIA_JMS_TIMESTAMP_TO, toDate.getTime());
        }
        if (selectorClause != null) {
            criteria.put(CRITERIA_SELECTOR_CLAUSE, selectorClause);
        }

        String selector = jmsSelectorUtil.getSelector(criteria);
        if (StringUtils.isEmpty(selector)) {
            selector = "true";
        }
        return selector;
    }

    protected List<InternalJmsMessage> convertCompositeData(CompositeData[] browse) {
        if (browse == null) {
            return Collections.emptyList();
        }
        List<InternalJmsMessage> result = new ArrayList<>();
        for (CompositeData compositeData : browse) {
            try {
                InternalJmsMessage internalJmsMessage = convertCompositeData(compositeData);
                result.add(internalJmsMessage);
            } catch (Exception e) {
                LOG.error("Error converting message [" + compositeData + "]", e);
            }
        }
        return result;
    }

    protected <T> T getCompositeValue(CompositeData data, String name) {
        if (data.containsKey(name)) {
            return (T) data.get(name);
        }
        return null;
    }


    protected InternalJmsMessage convertCompositeData(CompositeData data) {
        InternalJmsMessage result = new InternalJmsMessage();
        String jmsType = getCompositeValue(data, CRITERIA_JMS_TYPE);
        result.setType(jmsType);
        Date jmsTimestamp = getCompositeValue(data, "JMSTimestamp");
        result.setTimestamp(jmsTimestamp);
        String jmsMessageId = getCompositeValue(data, "JMSMessageID");
        result.setId(jmsMessageId);

        Map<String, String> properties = new HashMap<>();

        Integer priority = getCompositeValue(data, JMS_PRIORITY);
        result.setPriority(priority);
        if (priority != null) {
            properties.put(JMS_PRIORITY, String.valueOf(priority));
        }

        String textValue = getCompositeValue(data, "Text");
        result.setContent(textValue);


        Set<String> allPropertyNames = data.getCompositeType().keySet();
        for (String propertyName : allPropertyNames) {
            Object propertyValue = data.get(propertyName);
            if (StringUtils.startsWith(propertyName, "JMS")) {
                //TODO add other types of properties
                if (propertyValue instanceof String) {
                    properties.put(propertyName, (String) propertyValue);
                }
            }
            if (propertyValue instanceof Map) {
                Collection<CompositeDataSupport> values = ((Map) propertyValue).values();
                for (CompositeDataSupport compositeDataSupport : values) {
                    String key = (String) compositeDataSupport.get("key");
                    Object value = compositeDataSupport.get("value");
                    properties.put(key, String.valueOf(value));
                }
            }
        }

        result.setProperties(properties);
        return result;
    }

    @Override
    public int moveMessages(String source, String destination, String[] messageIds) {
        try {
            QueueViewMBean queue = domibusJMSActiveMQConnectionManager.getQueueViewMBean(source);
            return queue.moveMatchingMessagesTo(jmsSelectorUtil.getSelector(messageIds), destination);
        } catch (Exception e) {
            throw new InternalJMSException("Failed to move messages from source [" + source + "] to destination [" + destination + "]:" + Arrays.toString(messageIds), e);
        }
    }

    @Override
    public int moveAllMessages(String source, String jmsType, Date fromDate, Date toDate, String selectorClause, String destination){
        String selector = getSelector(jmsType, fromDate, toDate, selectorClause);
        try {
            QueueViewMBean queue = domibusJMSActiveMQConnectionManager.getQueueViewMBean(source);
            return queue.moveMatchingMessagesTo(selector, destination);
        } catch (Exception e) {
            throw new InternalJMSException(String.format("Failed to move messages from source [%s] to destination [%s] with selector [%s]", source, destination, selector), e);
        }
    }

    @Transactional
    @Override
    public InternalJmsMessage consumeMessage(String source, String customMessageId) {

        String selector = "MESSAGE_ID='" + customMessageId + "' AND NOTIFICATION_TYPE ='MESSAGE_RECEIVED'";

        InternalJmsMessage intJmsMsg = null;
        try {
            QueueViewMBean queueViewMBean = domibusJMSActiveMQConnectionManager.getQueueViewMBean(source);
            List<InternalJmsMessage> messages = getMessagesFromDestination(source, selector);
            if (!messages.isEmpty()) {
                intJmsMsg = messages.get(0);
                // Deletes it
                queueViewMBean.removeMatchingMessages(selector);
            }
        } catch (Exception ex) {
            throw new InternalJMSException("Failed to consume message [" + customMessageId + "] from source [" + source + "]", ex);
        }
        return intJmsMsg;
    }

    @Override
    public long getDestinationCount(InternalJMSDestination internalJMSDestination) {
        QueueViewMBean queueMbean = domibusJMSActiveMQConnectionManager.getQueueViewMBean(internalJMSDestination.getName());

        return getMessagesTotalCount(queueMbean);
    }

    protected List<InternalJmsMessage> getMessagesFromDestination(String destination, String selector) throws JMSActiveMQException {
        Queue queue;

        try {
            queue = getQueue(destination);
        } catch (Exception ex) {
            throw new JMSActiveMQException(ex);
        }
        if (queue == null) {
            LOG.warn("Couldn't find queue [{}]", destination);
            return new ArrayList<>();
        }

        return jmsSender.browseSelected(queue, selector, (session, browser) -> {
            List<InternalJmsMessage> result = new ArrayList<>();
            Enumeration enumeration = browser.getEnumeration();
            while (enumeration.hasMoreElements()) {
                TextMessage textMessage = null;
                try {
                    textMessage = (TextMessage) enumeration.nextElement();
                    result.add(convert(textMessage));
                } catch (Exception e) {
                    LOG.error("Error converting message [" + textMessage + "]", e);
                }

            }
            return result;
        });
    }

    protected InternalJmsMessage convert(TextMessage textMessage) throws JMSException {
        InternalJmsMessage result = new InternalJmsMessage();
        result.setContent(textMessage.getText());
        result.setId(textMessage.getJMSMessageID());
        result.setTimestamp(new Date(textMessage.getJMSTimestamp()));
        result.setType(textMessage.getJMSType());
        result.setPriority(textMessage.getJMSPriority());
        Enumeration propertyNames = textMessage.getPropertyNames();

        Map<String, String> properties = new HashMap<>();
        while (propertyNames.hasMoreElements()) {
            String name = (String) propertyNames.nextElement();
            String value = textMessage.getStringProperty(name);
            properties.put(name, value);
        }
        result.setProperties(properties);
        return result;
    }

    protected Queue getQueue(String queueName) {
        final InternalJMSDestination internalJMSDestination = findDestinationsGroupedByFQName().get(queueName);
        if (internalJMSDestination == null) {
            return null;
        }
        return new ActiveMQQueue(internalJMSDestination.getName());
    }

}
