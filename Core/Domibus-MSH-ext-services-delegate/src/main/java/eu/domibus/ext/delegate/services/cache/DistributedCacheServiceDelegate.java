package eu.domibus.ext.delegate.services.cache;

import eu.domibus.api.cache.distributed.DistributedCacheService;
import eu.domibus.ext.domain.CacheEntryDTO;
import eu.domibus.ext.exceptions.CacheExtServiceException;
import eu.domibus.ext.services.DistributedCacheExtService;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Delegate service for the distributed cache
 *
 * @author Cosmin baciu
 * @since 5.1
 */
@Service
public class DistributedCacheServiceDelegate implements DistributedCacheExtService {

    protected DistributedCacheService distributedCacheService;

    public DistributedCacheServiceDelegate(DistributedCacheService distributedCacheService) {
        this.distributedCacheService = distributedCacheService;
    }

    @Override
    public void createCache(String cacheName) {
        distributedCacheService.createCache(cacheName);
    }

    @Override
    public void createCache(String cacheName, int cacheSize, int timeToLiveSeconds, int maxIdleSeconds) {
        distributedCacheService.createCache(cacheName, cacheSize, timeToLiveSeconds, maxIdleSeconds);
    }

    @Override
    public void createCache(String cacheName, int cacheSize, int timeToLiveSeconds, int maxIdleSeconds, int nearCacheSize, int nearCacheTimeToLiveSeconds, int nearCacheMaxIdleSeconds) {
        distributedCacheService.createCache(cacheName, cacheSize, timeToLiveSeconds, maxIdleSeconds, nearCacheSize, nearCacheTimeToLiveSeconds, nearCacheMaxIdleSeconds);
    }

    @Override
    public void addEntryInCache(String cacheName, String key, Object value) throws CacheExtServiceException {
        distributedCacheService.addEntryInCache(cacheName, key, value);
    }

    @Override
    public Object getEntryFromCache(String cacheName, String key) throws CacheExtServiceException {
        return distributedCacheService.getEntryFromCache(cacheName, key);
    }

    @Override
    public void evictEntryFromCache(String cacheName, String key) throws CacheExtServiceException {
        distributedCacheService.evictEntryFromCache(cacheName, key);
    }

    @Override
    public List<String> getDistributedCacheNames() {
        return distributedCacheService.getDistributedCacheNames();
    }

    @Override
    public void addEntry(String cacheName, String key, Object value) {
        distributedCacheService.addEntryInCache(cacheName, key, value);
    }

    @Override
    public List<CacheEntryDTO> getEntriesFromCache(String cacheName) {
        final Map<String, Object> entriesFromCache = distributedCacheService.getEntriesFromCache(cacheName);
        if (MapUtils.isEmpty(entriesFromCache)) {
            return new ArrayList<>();
        }
        List<CacheEntryDTO> result = new ArrayList<>();
        entriesFromCache.entrySet().stream().forEach(entry -> result.add(new CacheEntryDTO(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null)));
        return result;
    }
}
