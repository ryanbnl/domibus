package eu.domibus.api.multitenancy;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface DomainContextProvider {

    String HEADER_DOMIBUS_DOMAIN = "DOMIBUS-DOMAIN";

    Domain getCurrentDomain();

    /**
     * Get the current domain. Does not throw exceptions in the attempt to get the domain.
     * @return
     */
    Domain getCurrentDomainSafely();

    void setCurrentDomain(String domainCode);

    void setCurrentDomainWithValidation(String domainCode);

    void setCurrentDomain(Domain domain);

    void clearCurrentDomain();
}
