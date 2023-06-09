package eu.domibus.core.message;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.springframework.stereotype.Service;

@Service
public class UserMessageContextKeyProviderImpl implements UserMessageContextKeyProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageContextKeyProviderImpl.class);

    @Override
    public void setKeyOnTheCurrentMessage(String key, String value) {
        if(PhaseInterceptorChain.getCurrentMessage() == null) {
            LOG.debug("Could not set key [{}] value [{}], current message does not exist.", key, value);
            return;
        }
        PhaseInterceptorChain.getCurrentMessage().getExchange().put(key, value);
    }

    @Override
    public String getKeyFromTheCurrentMessage(String key) {
        if(PhaseInterceptorChain.getCurrentMessage() == null) {
            LOG.debug("Could not get value for key [{}], current message does not exist.", key);
            return null;
        }
        return (String) PhaseInterceptorChain.getCurrentMessage().getExchange().get(key);
    }

    @Override
    public void setObjectOnTheCurrentMessage(String key, Object value) {
        if(PhaseInterceptorChain.getCurrentMessage() == null) {
            LOG.debug("Could not set key [{}] value [{}], current message does not exist.", key, value);
            return;
        }
        PhaseInterceptorChain.getCurrentMessage().getExchange().put(key, value);
    }

    @Override
    public Object getObjectFromTheCurrentMessage(String key) {
        if(PhaseInterceptorChain.getCurrentMessage() == null) {
            LOG.debug("Could not get value for key [{}], current message does not exist.", key);
            return null;
        }
        return PhaseInterceptorChain.getCurrentMessage().getExchange().get(key);
    }
}
