package eu.domibus.core.util;

import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.common.model.configuration.AsymmetricSignatureAlgorithm;
import eu.domibus.common.model.configuration.SecurityProfile;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Component;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Enumeration;


/**
 * Provides functionality for security certificates configuration
 *
 * @author Lucian FURCA
 * @since 5.1
 */
@Component
public class SecurityUtilImpl {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SecurityUtilImpl.class);

    public String getSecurityAlgorithm(SecurityProfile profile) {

        if (profile == null) {
            LOG.info("No security profile was specified so the default RSA_SHA256 algorithm is used.");
            return AsymmetricSignatureAlgorithm.RSA_SHA256.getAlgorithm();
        }

        switch (profile) {
            case RSA:
                return AsymmetricSignatureAlgorithm.RSA_SHA256.getAlgorithm();
            case ECC:
                return AsymmetricSignatureAlgorithm.ECC_SHA256.getAlgorithm();
            default:
                LOG.error("Profile [{}] is not a valid profile. No security profile was set.", profile);
        }

        return null;
    }

    public boolean areKeystoresIdentical(KeyStore store1, KeyStore store2) {
        if (store1 == null && store2 == null) {
            LOG.debug("Identical keystores: both are null");
            return true;
        }
        if (store1 == null || store2 == null) {
            LOG.debug("Different keystores: [{}] vs [{}]", store1, store2);
            return false;
        }
        try {
            if (store1.size() != store2.size()) {
                LOG.debug("Different keystores: [{}] vs [{}] entries", store1.size(), store2.size());
                return false;
            }
            final Enumeration<String> aliases = store1.aliases();
            while (aliases.hasMoreElements()) {
                final String alias = aliases.nextElement();
                if (!store2.containsAlias(alias)) {
                    LOG.debug("Different keystores: [{}] alias is not found in both", alias);
                    return false;
                }
                if (!store1.getCertificate(alias).equals(store2.getCertificate(alias))) {
                    LOG.debug("Different keystores: [{}] certificate is different", alias);
                    return false;
                }
            }
            return true;
        } catch (KeyStoreException e) {
            throw new DomibusCertificateException("Invalid keystore", e);
        }
    }
}
