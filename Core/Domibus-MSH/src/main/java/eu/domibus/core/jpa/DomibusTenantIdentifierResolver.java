package eu.domibus.core.jpa;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Conditional(MultiTenantAwareEntityManagerCondition.class)
@Component
public class DomibusTenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    public static final String GENERAL_SCHEMA = "general_schema";

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Override
    public String resolveCurrentTenantIdentifier() {
        final Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        if (currentDomain != null) {
            return currentDomain.getCode();
        }
        return GENERAL_SCHEMA;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}