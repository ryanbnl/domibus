package eu.domibus.api.multitenancy;

import org.springframework.jms.listener.MessageListenerContainer;

public interface DomainMessageListenerContainer {
    MessageListenerContainer get();

    String getName();

    Domain getDomain();

    void shutdown();

    void setConcurrency(String concurrency);
}
