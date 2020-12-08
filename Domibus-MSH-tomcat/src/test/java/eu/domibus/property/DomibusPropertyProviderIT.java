package eu.domibus.property;

import eu.domibus.AbstractIT;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.cache.DomibusCacheService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public class DomibusPropertyProviderIT extends AbstractIT {

    @Autowired
    org.springframework.cache.CacheManager cacheManager;

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    Domain defaultDomain = new Domain("default", "Default");

    @Test
    public void testCacheDomain() {
        String propertyName = DOMIBUS_UI_TITLE_NAME;

        //not in cache now
        String cachedValue = getCachedValue(defaultDomain, propertyName);
        //ads to cache
        String actualValue = domibusPropertyProvider.getProperty(defaultDomain, propertyName);
        Assert.assertNotEquals(actualValue, cachedValue);

        //gets the cached value now
        cachedValue = getCachedValue(defaultDomain, propertyName);
        Assert.assertEquals(actualValue, cachedValue);
    }

    @Test
    public void testCacheNoDomain() {
        String propertyName = DOMIBUS_UI_REPLICATION_QUEUE_CONCURENCY;

        //not in cache now
        String cachedValue = getCachedValue(propertyName);
        //ads to cache
        String actualValue = domibusPropertyProvider.getProperty(propertyName);
        Assert.assertNotEquals(actualValue, cachedValue);

        //gets the cached value now
        cachedValue = getCachedValue(propertyName);
        Assert.assertEquals(actualValue, cachedValue);
    }

    @Test
    public void testCacheEvict() {
        String propertyName = DOMIBUS_UI_SUPPORT_TEAM_NAME;

        String cachedValue = getCachedValue(defaultDomain, propertyName);
        Assert.assertNull(cachedValue);
        //ads to cache
        String actualValue = domibusPropertyProvider.getProperty(defaultDomain, propertyName);
        //gets the cached value now
        cachedValue = getCachedValue(defaultDomain, propertyName);
        Assert.assertNotNull(cachedValue);

        String newValue = actualValue + "MODIFIED";
        //evicts from cache
        domibusPropertyProvider.setProperty(defaultDomain, propertyName, newValue);
        //so not in cache
        cachedValue = getCachedValue(defaultDomain, propertyName);
        Assert.assertNull(cachedValue);

        //ads to cache again
        actualValue = domibusPropertyProvider.getProperty(defaultDomain, propertyName);
        //finds it there
        cachedValue = getCachedValue(defaultDomain, propertyName);
        Assert.assertEquals(newValue, actualValue);
    }

    private String getCachedValue(Domain domain, String propertyName) {
        if (domain == null) {
            domain = domainContextProvider.getCurrentDomainSafely();
        }
        String domainCode = domain == null ? "global" : domain.getCode();

        String key = domainCode + ":" + propertyName;
        return cacheManager.getCache(DomibusCacheService.DOMIBUS_PROPERTY_CACHE).get(key, String.class);
    }

    private String getCachedValue(String propertyName) {
        return getCachedValue(null, propertyName);
    }
}
