package eu.domibus.plugin.webService.backend.dispatch;

import eu.domibus.ext.domain.metrics.Counter;
import eu.domibus.ext.domain.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.backend.WSBackendMessageLogDao;
import eu.domibus.plugin.webService.backend.WSBackendMessageLogEntity;
import eu.domibus.plugin.webService.backend.WSBackendMessageStatus;
import eu.domibus.plugin.webService.backend.reliability.WSPluginBackendReliabilityService;
import eu.domibus.plugin.webService.backend.rules.WSPluginDispatchRule;
import eu.domibus.plugin.webService.backend.rules.WSPluginDispatchRulesService;
import org.springframework.stereotype.Service;

/**
 * Common logic for sending messages to C1/C4 from WS Plugin
 *
 * @author François Gautier
 * @since 5.0
 */
@Service
public class WSPluginMessageSender {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginMessageSender.class);

    protected final WSPluginBackendReliabilityService reliabilityService;

    protected final WSBackendMessageLogDao wsBackendMessageLogDao;

    protected final WSPluginDispatchRulesService rulesService;

    protected final WSPluginMessageBuilder messageBuilder;

    protected final WSPluginDispatcher dispatcher;

    public WSPluginMessageSender(WSPluginBackendReliabilityService reliabilityService,
                                 WSBackendMessageLogDao wsBackendMessageLogDao,
                                 WSPluginDispatchRulesService rulesService,
                                 WSPluginMessageBuilder messageBuilder,
                                 WSPluginDispatcher dispatcher) {
        this.reliabilityService = reliabilityService;
        this.wsBackendMessageLogDao = wsBackendMessageLogDao;
        this.rulesService = rulesService;
        this.messageBuilder = messageBuilder;
        this.dispatcher = dispatcher;
    }

    /**
     * Send notification to the backend service with reliability feature
     *
     * @param backendMessage persisted message
     */
    @Timer(clazz = WSPluginMessageSender.class, value = "wsplugin_outgoing_backend_message_notification")
    @Counter(clazz = WSPluginMessageSender.class, value = "wsplugin_outgoing_backend_message_notification")
    public void sendNotification(final WSBackendMessageLogEntity backendMessage) {
        reliabilityService.changeStatus(backendMessage, WSBackendMessageStatus.SEND_IN_PROGRESS);
        WSPluginDispatchRule oneRule = rulesService.getOneRule(backendMessage.getRuleName());

        String endpoint = oneRule.getEndpoint();
        LOG.info("Send backend notification [{}] for domibus id [{}] to [{}]. wsBackendMessageLogEntity [{}]",
                backendMessage.getType(),
                backendMessage.getMessageId(),
                endpoint,
                backendMessage);
        try {
            dispatcher.dispatch(messageBuilder.buildSOAPMessage(backendMessage), endpoint);
            reliabilityService.changeStatus(backendMessage, WSBackendMessageStatus.SENT);
            LOG.info("Backend notification [{}] for domibus id [{}] sent to [{}] successfully",
                    backendMessage.getType(),
                    backendMessage.getMessageId(),
                    endpoint);
        } catch (Throwable t) {//NOSONAR: Catching Throwable is done on purpose in order to even catch out of memory exceptions.
            reliabilityService.handleReliability(backendMessage, oneRule);
            LOG.error("Error occurred when sending message with ID [{}]", backendMessage.getMessageId(), t);
            throw t;
        }
    }

}
