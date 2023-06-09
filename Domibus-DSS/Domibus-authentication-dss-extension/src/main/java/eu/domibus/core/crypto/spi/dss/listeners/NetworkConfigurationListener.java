package eu.domibus.core.crypto.spi.dss.listeners;

import com.google.common.collect.Sets;
import eu.domibus.core.crypto.spi.dss.ProxyHelper;
import eu.domibus.ext.exceptions.DomibusPropertyExtException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;

import java.util.Set;

import static eu.domibus.core.crypto.spi.dss.DssExtensionPropertyManager.*;

/**
 * @author Thomas Dussart
 * @since 4.2
 *
 * Listener for proxy configuration property change.
 */
public class NetworkConfigurationListener implements PluginPropertyChangeListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(NetworkConfigurationListener.class);

    private final Set<String> properties = Sets.newHashSet(
            AUTHENTICATION_DSS_PROXY_HTTP_HOST,
            AUTHENTICATION_DSS_PROXY_HTTP_PORT,
            AUTHENTICATION_DSS_PROXY_HTTP_USER,
            AUTHENTICATION_DSS_PROXY_HTTP_PASSWORD,
            AUTHENTICATION_DSS_PROXY_HTTP_EXCLUDEDHOSTS,
            AUTHENTICATION_DSS_PROXY_HTTPS_HOST,
            AUTHENTICATION_DSS_PROXY_HTTPS_PORT,
            AUTHENTICATION_DSS_PROXY_HTTPS_USER,
            AUTHENTICATION_DSS_PROXY_HTTPS_PASSWORD,
            AUTHENTICATION_DSS_PROXY_HTTPS_EXCLUDEDHOSTS);

    private CommonsDataLoader dataLoader;

    private ProxyHelper proxyHelper;

    public NetworkConfigurationListener(CommonsDataLoader dataLoader, ProxyHelper proxyHelper) {
        this.dataLoader = dataLoader;
        this.proxyHelper = proxyHelper;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        boolean matchingProperty = properties.contains(propertyName);
        if(matchingProperty){
            LOG.info("Dss Property:[{}] changed",propertyName);
        }
        return matchingProperty;
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) throws DomibusPropertyExtException {
        LOG.debug("Reloading proxy configuration due to property change: domain code:[{}], property name:[{}], property value:[{}]",domainCode,propertyName,propertyValue);
        dataLoader.setProxyConfig(proxyHelper.getProxyConfig());
    }
}
