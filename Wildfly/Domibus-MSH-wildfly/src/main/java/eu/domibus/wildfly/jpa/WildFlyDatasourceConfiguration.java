package eu.domibus.wildfly.jpa;

import eu.domibus.api.datasource.DataSourceConstants;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.sql.DataSource;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class WildFlyDatasourceConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(WildFlyDatasourceConfiguration.class);

    @Bean(DataSourceConstants.DOMIBUS_JDBC_DATA_SOURCE)
    public JndiObjectFactoryBean domibusDatasource(DomibusPropertyProvider domibusPropertyProvider) {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setExpectedType(DataSource.class);
        String jndiName = domibusPropertyProvider.getProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_JDBC_DATASOURCE_JNDI_NAME);

        LOGGER.debug("Configured property [{}] with [{}]", DomibusPropertyMetadataManagerSPI.DOMIBUS_JDBC_DATASOURCE_JNDI_NAME, jndiName);
        jndiObjectFactoryBean.setJndiName(jndiName);
        return jndiObjectFactoryBean;
    }

    @Bean(DataSourceConstants.DOMIBUS_JDBC_QUARTZ_DATA_SOURCE)
    public JndiObjectFactoryBean quartzDatasource(DomibusPropertyProvider domibusPropertyProvider) {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setExpectedType(DataSource.class);
        String jndiName = domibusPropertyProvider.getProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_JDBC_DATASOURCE_QUARTZ_JNDI_NAME);

        LOGGER.debug("Configured property [{}] with [{}]", DomibusPropertyMetadataManagerSPI.DOMIBUS_JDBC_DATASOURCE_QUARTZ_JNDI_NAME, jndiName);
        jndiObjectFactoryBean.setJndiName(jndiName);
        return jndiObjectFactoryBean;
    }
}
