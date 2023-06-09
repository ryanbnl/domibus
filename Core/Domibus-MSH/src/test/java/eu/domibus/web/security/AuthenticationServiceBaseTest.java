package eu.domibus.web.security;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskException;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.DomibusUserDetails;
import eu.domibus.core.user.UserService;
import eu.domibus.core.user.multitenancy.AllUsersManagementServiceImpl;
import eu.domibus.core.user.ui.UserManagementServiceImpl;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Catalin Enache
 * @since 4.1
 */
@RunWith(JMockit.class)
public class AuthenticationServiceBaseTest {

    @Tested
    AuthenticationServiceBase authenticationServiceBase;

    @Injectable
    DomainService domainService;

    @Injectable
    @Qualifier(AllUsersManagementServiceImpl.BEAN_NAME)
    private UserService allUserManagementService;

    @Injectable
    @Qualifier(UserManagementServiceImpl.BEAN_NAME)
    private UserService userManagementService;

    @Injectable
    private AuthUtils authUtils;

    private List<Domain> domains = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        final Domain domain1 = new Domain("domain1", "domain 1");
        final Domain domain2 = new Domain("domain2", "domain 2");

        domains.add(domain1);
        domains.add(domain2);
    }

    @Test
    public void test_changeDomain_DomainDoesntExists() {
        final String domainCode = "domain3";


        new Expectations() {{
            domainService.getDomains();
            result = domains;

        }};

        try {
            //tested method
            authenticationServiceBase.changeDomain(domainCode);
            Assert.fail("Exception expected");
        } catch (Exception e) {
            Assert.assertEquals(DomainTaskException.class, e.getClass());
        }

        new FullVerifications() {{
        }};
    }

    @Test
    public void test_changeDomain_DomainEmpty() {
        final String domainCode = StringUtils.EMPTY;

        new Expectations() {{
        }};

        try {
            //tested method
            authenticationServiceBase.changeDomain(domainCode);
            Assert.fail("Exception expected");
        } catch (Exception e) {
            Assert.assertEquals(DomainTaskException.class, e.getClass());
        }

        new FullVerifications() {{
        }};
    }

    @Test
    public void testChangePassword(@Mocked DomibusUserDetailsImpl loggedUser) {
        String currentPass = "old", newPass = "new";

        new Expectations(authenticationServiceBase) {{
            authenticationServiceBase.getLoggedUser();
            result = loggedUser;

            authUtils.isSuperAdmin();
            result = false;
        }};

        authenticationServiceBase.changePassword(currentPass, newPass);

        new Verifications() {{
            userManagementService.changePassword(loggedUser.getUsername(), currentPass, newPass);
            times = 1;
            allUserManagementService.changePassword(loggedUser.getUsername(), currentPass, newPass);
            times = 0;
        }};

    }
}
