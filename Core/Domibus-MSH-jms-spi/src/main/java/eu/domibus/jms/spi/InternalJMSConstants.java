package eu.domibus.jms.spi;

/**
 * @author François Gautier
 * @since 5.0
 */
public final class InternalJMSConstants {

   public static final String QUEUE = "Queue";

   public static final  String PROP_MAX_BROWSE_SIZE = "domibus.jms.queue.maxBrowseSize";

   /** in multi-tenancy mode domain admins should not see any count of messages so we set this value */
   public static final  long NB_MESSAGES_ADMIN = -1L;

   public static final String JMS_PRIORITY = "JMSPriority";

   /**
     * Queue names in Domibus
     */
   public static final String SEND_MESSAGE_QUEUE = "sendMessageQueue";
   public static final String SEND_LARGE_MESSAGE_QUEUE = "sendLargeMessageQueue";
   public static final String SPLIT_AND_JOIN_QUEUE = "splitAndJoinQueue";
   public static final String PULL_MESSAGE_QUEUE = "pullMessageQueue";
   public static final String SEND_PULL_RECEIPT_QUEUE = "sendPullReceiptQueue";
   public static final String RETENTION_MESSAGE_QUEUE = "retentionMessageQueue";
   public static final String ALERT_MESSAGE_QUEUE = "alertMessageQueue";
   public static final String EARCHIVE_QUEUE = "eArchiveQueue";
   public static final String EARCHIVE_NOTIFICATION_QUEUE = "eArchiveNotificationQueue";
   public static final String EARCHIVE_NOTIFICATION_DLQ = "eArchiveNotificationDLQ";
   public static final String NOTIFY_BACKEND_QUEUE = "notifyBackendQueue";
   public static final String UNKNOWN_RECEIVER_QUEUE = "unknownReceiverQueue";
   public static final String CLUSTER_COMMAND_TOPIC = "clusterCommandTopic";
   public static final String CRITERIA_JMS_TYPE = "JMSType";
   public static final String CRITERIA_JMS_TIMESTAMP_FROM = "JMSTimestamp_from";
   public static final String CRITERIA_JMS_TIMESTAMP_TO = "JMSTimestamp_to";
   public static final String CRITERIA_SELECTOR_CLAUSE = "selectorClause";
}
