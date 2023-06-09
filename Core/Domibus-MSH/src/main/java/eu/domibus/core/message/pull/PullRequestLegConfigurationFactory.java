package eu.domibus.core.message.pull;

import eu.domibus.api.ebms3.model.Ebms3Error;
import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.ebms3.model.Ebms3PullRequest;
import eu.domibus.api.model.Messaging;
import eu.domibus.api.model.PullRequest;
import eu.domibus.core.ebms3.receiver.leg.AbstractMessageLegConfigurationFactory;
import eu.domibus.core.ebms3.receiver.leg.LegConfigurationExtractor;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@Component
@Qualifier("pullRequestLegConfigurationFactory")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PullRequestLegConfigurationFactory extends AbstractMessageLegConfigurationFactory {

    private final static Logger LOG = DomibusLoggerFactory.getLogger(PullRequestLegConfigurationFactory.class);

    @Override
    protected LegConfigurationExtractor getConfiguration(SoapMessage soapMessage, Ebms3Messaging messaging) {
        PullRequestLegConfigurationExtractor legConfigurationExtractor = null;
        if (LOG.isTraceEnabled()) {
            final List<Header> headers = soapMessage.getHeaders();
            if (headers != null) {
                LOG.trace("Soap message: ");
                for (Header header : headers) {
                    LOG.trace("Header [{}], object[{}]", header.getName(), header.getObject());
                }
            }
            LOG.trace("User message:[{}] ", messaging.getUserMessage());
            LOG.trace("Signal message:[{}] ", messaging.getSignalMessage());
            if (messaging.getSignalMessage() != null) {
                LOG.trace("Pull request:[{}]", messaging.getSignalMessage().getPullRequest());
                LOG.trace("Receipt:[{}]", messaging.getSignalMessage().getReceipt());
                final Set<Ebms3Error> errors = messaging.getSignalMessage().getError();
                if (errors != null) {
                    for (Ebms3Error error : errors) {
                        LOG.trace("Error code:[{}], detail[{}]", error.getErrorCode(), error.getErrorDetail());
                    }
                }
            }
        }
        Ebms3PullRequest pullRequest = messaging.getSignalMessage().getPullRequest();
        if (pullRequest != null) {
            legConfigurationExtractor = new PullRequestLegConfigurationExtractor(soapMessage, messaging);
        }
        return legConfigurationExtractor;
    }
}
