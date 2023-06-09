package eu.domibus.core.crypto.spi.dss;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.cache.Cache;

import static org.junit.Assert.*;

/**
 * @author Thomas Dussart
 * @since 4.2
 */

@RunWith(JMockit.class)
public class DssCacheTest {

    @Test
    public void addToCache(@Mocked Cache cache) {
        DssCache dssCache = new DssCache(cache);
        String key = "key";
        Boolean valid = true;

        dssCache.addToCache(key, valid);

        new Verifications() {{
            Object valueActual;
            Object keyActual;
            cache.putIfAbsent(keyActual = withCapture(), valueActual = withCapture());
            assertEquals(key, keyActual);
            assertEquals(valid, valueActual);
        }};
    }

    @Test
    public void isChainValidTrue(@Mocked Cache cache, @Mocked Cache.ValueWrapper valueWrapper) {
        DssCache dssCache = new DssCache(cache);
        String key = "key";

        new Expectations() {{
            cache.get(key);
            result = valueWrapper;
        }};
        assertTrue(dssCache.isChainValid(key));
    }

    @Test
    public void isChainValidFalse(@Mocked Cache cache) {
        DssCache dssCache = new DssCache(cache);
        String key = "key";
        new Expectations() {{
            cache.get(key);
            result = null;
        }};
        assertFalse(dssCache.isChainValid(key));
    }

    @Test
    public void clear(@Mocked Cache cache) {
        DssCache dssCache = new DssCache(cache);
        dssCache.clear();
        new Verifications() {{
            cache.clear();
        }};
    }
}