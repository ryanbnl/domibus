package eu.domibus.core.security;

import eu.domibus.api.security.*;
import eu.domibus.api.user.plugin.AuthenticationEntity;
import eu.domibus.core.alerts.service.PluginUserAlertsServiceImpl;
import eu.domibus.core.user.plugin.AuthenticationDAO;
import eu.domibus.core.user.plugin.security.PluginUserSecurityPolicyManager;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component(value = "securityCustomAuthenticationProvider")
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CustomAuthenticationProvider.class);

    @Autowired
    @Qualifier("securityAuthenticationDAO")
    private AuthenticationDAO securityAuthenticationDAO;

    @Autowired
    @Qualifier("securityBlueCoatCertificateServiceImpl")
    private BlueCoatCertificateService blueCoatCertificateService;

    @Autowired
    @Qualifier("securityX509CertificateServiceImpl")
    private X509CertificateService x509CertificateService;

    @Autowired
    private BCryptPasswordEncoder bcryptEncoder;

    @Autowired
    PluginUserSecurityPolicyManager pluginUserPasswordManager;

    @Autowired
    PluginUserAlertsServiceImpl userAlertsService;

    @Autowired
    protected AuthUtils authUtils;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            if (authentication instanceof X509CertificateAuthentication) {
                LOG.debug("Authenticating using the X509 certificate from the request");
                boolean clientX509CertificateValid = isClientX509CertificateValid((X509Certificate[]) authentication.getCredentials());
                authentication.setAuthenticated(clientX509CertificateValid);

                AuthenticationEntity authenticationEntity = securityAuthenticationDAO.findByCertificateId(authentication.getName());
                ((X509CertificateAuthentication) authentication).setOriginalUser(authenticationEntity.getOriginalUser());
                List<AuthRole> authRoles = securityAuthenticationDAO.getRolesForCertificateId(authentication.getName());
                setAuthority(authentication, authRoles);
            } else if (authentication instanceof BlueCoatClientCertificateAuthentication) {
                LOG.debug("Authenticating using the decoded certificate in the http header");
                authentication.setAuthenticated(blueCoatCertificateService.isBlueCoatClientCertificateValid((CertificateDetails) authentication.getCredentials()));

                AuthenticationEntity authenticationEntity = securityAuthenticationDAO.findByCertificateId(authentication.getName());
                ((BlueCoatClientCertificateAuthentication) authentication).setOriginalUser(authenticationEntity.getOriginalUser());
                List<AuthRole> authRoles = securityAuthenticationDAO.getRolesForCertificateId(authentication.getName());
                setAuthority(authentication, authRoles);
            } else if (authentication instanceof BasicAuthentication) {
                LOG.debug("Authenticating using the Basic authentication");

                String userName = authentication.getName();
                String password = (String) authentication.getCredentials();
                AuthenticationEntity user = securityAuthenticationDAO.findByUser(userName);

                boolean isAuthenticated = checkUserAccount(user, password);
                if (isAuthenticated) {
                    pluginUserPasswordManager.handleCorrectAuthentication(userName);
                    //check expired; it does not produce an alert here
                    isAuthenticated = isAuthenticated && !isPasswordExpired(user);
                } else {
                    // there is no security context when the user failed to login -> we're creating one
                    authUtils.runWithDomibusSecurityContext(() -> pluginUserPasswordManager.handleWrongAuthentication(userName), AuthRole.ROLE_ADMIN, true);
                }

                authentication.setAuthenticated(isAuthenticated);
                String originalUser = user != null ? user.getOriginalUser() : null;
                ((BasicAuthentication) authentication).setOriginalUser(originalUser);
                List<AuthRole> authRoles = securityAuthenticationDAO.getRolesForUser(authentication.getName());
                setAuthority(authentication, authRoles);
            }
        } catch (final Exception exception) {
            throw new AuthenticationServiceException("Couldn't authenticate the principal " + authentication.getPrincipal(), exception);
        }
        return authentication;
    }

    private boolean isClientX509CertificateValid(X509Certificate[] credentials) {
        x509CertificateService.validateClientX509Certificates(credentials);
        return true;
    }

    private boolean checkUserAccount(AuthenticationEntity user, String password) {
        if (user == null) {
            return false;
        }

        //check if password is correct
        boolean isPasswordCorrect = bcryptEncoder.matches(password, user.getPassword());
        if (!isPasswordCorrect) {
            return false;
        }

        //check locked; if true, declare that it is not authenticated, so that handle method works
        if (!user.isActive()) {
            return false;
        }

        return true;
    }

    public boolean isPasswordExpired(AuthenticationEntity user) {
        boolean isDefaultPassword = user.hasDefaultPassword();
        LocalDateTime passwordChangeDate = user.getPasswordChangeDate();
        try {
            pluginUserPasswordManager.validatePasswordExpired(user.getUserName(), isDefaultPassword, passwordChangeDate);
            return false;
        } catch (CredentialsExpiredException ex) {
            LOG.trace("Credentials expired for user [{}]", user.getUserName(), ex);
            return true;
        }
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return X509CertificateAuthentication.class.equals(clazz) || BlueCoatClientCertificateAuthentication.class.equals(clazz) || BasicAuthentication.class.equals(clazz);
    }

    private void setAuthority(Authentication authentication, List<AuthRole> authRoles) {
        if (authRoles == null || authRoles.isEmpty())
            return;

        List<GrantedAuthority> authorityList = new ArrayList<>();
        for (AuthRole role : authRoles) {
            authorityList.add(new SimpleGrantedAuthority(role.name()));
        }
        if (authentication instanceof BasicAuthentication) {
            ((BasicAuthentication) authentication).setAuthorityList(Collections.unmodifiableList(authorityList));
        } else if (authentication instanceof X509CertificateAuthentication) {
            ((X509CertificateAuthentication) authentication).setAuthorityList(Collections.unmodifiableList(authorityList));
        } else if (authentication instanceof BlueCoatClientCertificateAuthentication) {
            ((BlueCoatClientCertificateAuthentication) authentication).setAuthorityList(Collections.unmodifiableList(authorityList));
        }
    }
}
