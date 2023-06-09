package eu.domibus.core.jms.multitenancy;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainMessageListenerContainer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;

/**
 * @author Ion Perpegel
 * @since 5.0
 * <p>
 * Domibus specific MessageListenerContainer, created for a specific domain;
 * We need this because we manage them explicitly and we need also to replace one at runtime
 */
public class PluginDomainMessageListenerContainerAdapter implements DomainMessageListenerContainer {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginDomainMessageListenerContainerAdapter.class);

    private final MessageListenerContainer messageListenerContainer;
    private final Domain domain;
    private final String name;
    private final String pluginName;

    public PluginDomainMessageListenerContainerAdapter(MessageListenerContainer messageListenerContainer, Domain domain, String name, String pluginName) {
        this.domain = domain;
        this.messageListenerContainer = messageListenerContainer;
        this.name = name;
        this.pluginName = pluginName;
    }

    @Override
    public MessageListenerContainer get() {
        return messageListenerContainer;
    }

    /**
     * The name of the plugin it belongs to
     * @return
     */
    public String getPluginName() {
        return pluginName;
    }

    /**
     * The name of the bean we adapt
     * @return
     */
    public String getName() {
        return name;
    }

    public Domain getDomain() {
        return this.domain;
    }

    @Override
    public void shutdown() {
        if (messageListenerContainer instanceof DefaultMessageListenerContainer) {
            ((DefaultMessageListenerContainer) messageListenerContainer).shutdown();
        } else {
            LOG.warn("Could not shutdown message listener [{}] for domain [{}] because it is not a DefaultMessageListenerContainer",
                    name, domain);
        }
    }

    @Override
    public void setConcurrency(String concurrency) {
        if (messageListenerContainer instanceof DefaultMessageListenerContainer) {
            ((DefaultMessageListenerContainer) messageListenerContainer).setConcurrency(concurrency);
        } else {
            LOG.warn("Could not set concurrency [{}] for message listener [{}] for domain [{}] because it is not a DefaultMessageListenerContainer",
                    concurrency, name, domain);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(messageListenerContainer)
                .append("name", name)
                .append("domain", domain)
                .toString();
    }
}
