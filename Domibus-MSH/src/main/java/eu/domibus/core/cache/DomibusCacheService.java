package eu.domibus.core.cache;

import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public interface DomibusCacheService {

    String USER_DOMAIN_CACHE = "userDomain";
    String PREFERRED_USER_DOMAIN_CACHE = "preferredUserDomain";
    String ALL_DOMAINS_CACHE = "allDomains";
    String DOMAIN_BY_CODE_CACHE = "domainByCode";
    String DOMAIN_BY_SCHEDULER_CACHE = "domainByScheduler";
    String DYNAMIC_DISCOVERY_ENDPOINT = "lookupInfo";
    String DISPATCH_CLIENT = "dispatchClient";
    String CRL_BY_CERT = "crlByCert";
    String DOMIBUS_PROPERTY_CACHE = "domibusProperties";

    void clearCache(String refreshCacheName);

    void clearAllCaches() throws DomibusCoreException;

}
