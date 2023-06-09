package eu.domibus.core.property.listeners;

import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SMART_RETRY_ENABLED;


/**
 * @author Razvan Cretu
 * @since 5.1
 * <p>
 * Handles the change of smart retry property, validating that only known party identifiers are used.
 */
@Service
public class SmartRetryChangeListener implements DomibusPropertyChangeListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SmartRetryChangeListener.class);

    protected PModeProvider pModeProvider;

    public SmartRetryChangeListener(PModeProvider pModeProvider) {
        this.pModeProvider = pModeProvider;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return DOMIBUS_SMART_RETRY_ENABLED.equalsIgnoreCase(propertyName);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        List<String> newPartyIds = parsePropertyValue(propertyValue);

        List<Party> knownParties = pModeProvider.findAllParties();

        newPartyIds.forEach(partyId -> {
            LOG.trace("Checking that [{}] is a known party", partyId);
            if (knownParties.stream().noneMatch(party ->
                    party.getIdentifiers().stream().anyMatch(identifier -> partyId.equalsIgnoreCase(identifier.getPartyId())))) {
                throw new DomibusPropertyException("Could not change the list of parties for smart retry feature: "
                        + partyId + " is not configured in Pmode");
            }
        });
    }

    protected List<String> parsePropertyValue(String propertyValue) {
        String[] propertyValueParts = StringUtils.split(StringUtils.trimToEmpty(propertyValue), ',');
        return Arrays.stream(propertyValueParts)
                .map(name ->  name.trim().toLowerCase())
                .filter(name -> StringUtils.isNotBlank(name))
                .distinct()
                .collect(Collectors.toList());
    }
}
