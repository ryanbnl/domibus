package eu.domibus.web.rest;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.DomibusUserDetails;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.DomainRO;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 5.0
 * <p>
 * Rest for managing user domains
 */
@RestController
@RequestMapping(value = "/rest/userdomains")
public class UserDomainResource {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(UserDomainResource.class);

    private final DomainService domainService;

    private final DomibusCoreMapper coreMapper;

    private final AuthUtils authUtils;

    public UserDomainResource(DomainService domainService,
                              DomibusCoreMapper coreMapper, AuthUtils authUtils) {
        this.domainService = domainService;
        this.coreMapper = coreMapper;
        this.authUtils = authUtils;
    }

    /**
     * @return the list of user domains
     */
    @GetMapping(value = "")
    public List<DomainRO> getDomains() {
        LOG.debug("Getting user domains");
        DomibusUserDetails userDetails = authUtils.getUserDetails();
        if (userDetails == null) {
            LOG.info("Could not get user details to get domains.");
            return new ArrayList<>();
        }

        List<Domain> availableDomains = userDetails.getAvailableDomainCodes().stream()
                .map(domainService::getDomain)
                .sorted(Comparator.comparing(Domain::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        return coreMapper.domainListToDomainROList(availableDomains);
    }

}
