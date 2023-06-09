package eu.domibus.archive.client.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.api.earchive.DomibusEArchiveException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.proxy.DomibusProxy;
import eu.domibus.api.proxy.DomibusProxyService;
import eu.domibus.archive.client.api.ArchiveWebhookApi;
import eu.domibus.archive.client.invoker.ApiClient;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import static eu.domibus.api.property.DomibusGeneralConstants.JSON_MAPPER_BEAN;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

/**
 * @author Gabriel Maier
 * @since 5.1
 */
@Configuration
public class EArchiveConfiguration {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveConfiguration.class);

    public static final String EARCHIVING_CLIENT_BEAN = "eArchiveClientApi";

    public static final String EARCHIVING_REST_TEMPLATE_BEAN = "eArchiveRestTemplate";

    private final DomibusPropertyProvider domibusPropertyProvider;

    private final DomibusProxyService domibusProxyService;

    private final ObjectMapper objectMapper;

    private final DomainContextProvider domainContextProvider;

    public EArchiveConfiguration(DomibusPropertyProvider domibusPropertyProvider,
                                 DomibusProxyService domibusProxyService,
                                 @Qualifier(JSON_MAPPER_BEAN) ObjectMapper objectMapper, DomainContextProvider domainContextProvider) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domibusProxyService = domibusProxyService;
        this.objectMapper = objectMapper;
        this.domainContextProvider = domainContextProvider;
    }

    // EDELIVERY-10563 to tro to get rid ofprimary annotation
    @Bean(EARCHIVING_CLIENT_BEAN)
    @Scope(SCOPE_PROTOTYPE)
    @Primary
    public ArchiveWebhookApi getEarchivingClientApi() {
        String restUrl = domibusPropertyProvider.getProperty(DOMIBUS_EARCHIVE_NOTIFICATION_URL);
        Boolean isEarchiveActive = domibusPropertyProvider.getBooleanProperty(DOMIBUS_EARCHIVE_ACTIVE);
        if (BooleanUtils.isTrue(isEarchiveActive) && StringUtils.isBlank(restUrl)) {
            throw new DomibusEArchiveException("eArchive client endpoint not configured");
        }
        LOG.debug("Initializing eArchive client api with endpoint [{}]...", restUrl);

        RestTemplate restTemplate = restTemplate();
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(restUrl);

        ArchiveWebhookApi eArchivingClientApi = new ArchiveWebhookApi();
        eArchivingClientApi.setApiClient(apiClient);

        String username = domibusPropertyProvider.getProperty(DOMIBUS_EARCHIVE_NOTIFICATION_USERNAME);
        if (StringUtils.isNotBlank(username)) {
            eArchivingClientApi.getApiClient().setUsername(username);
            String password = domibusPropertyProvider.getProperty(DOMIBUS_EARCHIVE_NOTIFICATION_PASSWORD);
            eArchivingClientApi.getApiClient().setPassword(password);
        }
        return eArchivingClientApi;
    }

    // EDELIVERY-10563 to tro to remove the bean annotation altogether
    @Bean(EARCHIVING_REST_TEMPLATE_BEAN)
    @Scope(SCOPE_PROTOTYPE)
    public RestTemplate restTemplate() {
        if (domainContextProvider.getCurrentDomainSafely() == null) {
            return new RestTemplate();
        }

        int timeout = domibusPropertyProvider.getIntegerProperty(DOMIBUS_EARCHIVE_NOTIFICATION_TIMEOUT);
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();

        HttpClientBuilder clientBuilder = HttpClientBuilder.create().setDefaultRequestConfig(config);

        Boolean useProxy = domibusPropertyProvider.getBooleanProperty(DOMIBUS_EARCHIVE_NOTIFICATION_USEPROXY);
        if (useProxy && domibusProxyService.useProxy()) {
            DomibusProxy domibusProxy = domibusProxyService.getDomibusProxy();

            LOG.debug("Using proxy at [{}:{}] to notify e-archiving client", domibusProxy.getHttpProxyHost(), domibusProxy.getHttpProxyPort());
            clientBuilder.setProxy(new HttpHost(domibusProxy.getHttpProxyHost(), domibusProxy.getHttpProxyPort()));

            if (BooleanUtils.isTrue(domibusProxyService.isProxyUserSet())) {
                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(
                        new AuthScope(domibusProxy.getHttpProxyHost(), domibusProxy.getHttpProxyPort()),
                        new UsernamePasswordCredentials(domibusProxy.getHttpProxyUser(), domibusProxy.getHttpProxyPassword())
                );
                clientBuilder.setDefaultCredentialsProvider(credsProvider);
            }
        }

        CloseableHttpClient client = clientBuilder.build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(client);
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        restTemplate.getMessageConverters().add(0, converter);

        return restTemplate;
    }
}
