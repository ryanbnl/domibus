package eu.domibus.core.alerts.configuration.generic;

import eu.domibus.core.alerts.configuration.common.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.common.BaseConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;

/**
 * Default alert config manager generated automatically for an alert type that has frequency and delay properties ( if not overridden)
 *
 * @author Ion Perpegel
 * @since 5.1
 */
public abstract class FrequencyAlertConfigurationManager
        extends BaseConfigurationManager<FrequencyAlertConfiguration>
        implements AlertConfigurationManager {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(FrequencyAlertConfigurationManager.class);

    public FrequencyAlertConfigurationManager(AlertType alertType) {
        super(alertType);
    }

    public FrequencyAlertConfiguration readConfiguration() {
        FrequencyAlertConfiguration config = super.readConfiguration();
        if (!config.isActive()) {
            return config;
        }

        try {
            final Integer frequency = domibusPropertyProvider.getIntegerProperty(getFrequencyPropertyName());
            config.setFrequency(frequency);

            return config;
        } catch (Exception ex) {
            LOG.warn("Error while configuring alerts of type [{}] notifications for domain:[{}].",
                    alertType, domainContextProvider.getCurrentDomainSafely(), ex);
            return createNewInstance(alertType);
        }
    }

    protected String getFrequencyPropertyName() {
        return domibusPropertiesPrefix + ".frequency_days";
    }

    @Override
    protected FrequencyAlertConfiguration createNewInstance(AlertType alertType) {
        return new FrequencyAlertConfiguration(alertType);
    }
}
