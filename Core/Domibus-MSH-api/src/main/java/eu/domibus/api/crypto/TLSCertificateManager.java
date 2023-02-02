package eu.domibus.api.crypto;

import eu.domibus.api.multitenancy.DomainsAware;
import eu.domibus.api.pki.KeyStoreContentInfo;
import eu.domibus.api.pki.KeystorePersistenceInfo;
import eu.domibus.api.security.TrustStoreEntry;

import java.util.List;

/**
 * "Orchestrator"/facade class that connects the dots between clientauthentication.xml properties and certificate server
 *
 * @author Ion Perpegel
 * @since 5.0
 */
public interface TLSCertificateManager extends DomainsAware {

    public final static String TLS_TRUSTSTORE_NAME = "TLS.truststore";

    /**
     * Replaces the truststore pointed by the store info with the one provided as parameters
     *
     * @param storeInfo all neede info about the store
     * @throws CryptoException
     */
    void replaceTrustStore(KeyStoreContentInfo storeInfo);

    /**
     * Returns the certificate entries found in the tls truststore pointed by the clientauthentication.xml file
     *
     * @return
     */
    List<TrustStoreEntry> getTrustStoreEntries();

    /**
     * Returns the tls truststore content pointed by the clientauthentication.xml file
     *
     * @return
     */
    KeyStoreContentInfo getTruststoreContent();

    /**
     * Adds the specified certificate to the tls truststore content pointed by the clientauthentication.xml file
     * @param certificateData
     * @param alias
     * @return
     */
    boolean addCertificate(byte[] certificateData, final String alias);

    /**
     * Removes the specified certificate from the tls truststore content pointed by the clientauthentication.xml file
     * @param alias
     * @return
     */
    boolean removeCertificate(String alias);

    /**
     * Reads the truststore from a disk location and persists it in the DB if not already there
     */
    void saveStoresFromDBToDisk();

    KeystorePersistenceInfo getPersistenceInfo();

    String getStoreFileExtension();
}
