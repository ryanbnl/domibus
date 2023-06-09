package eu.domibus.core.user.plugin;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.security.*;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.DecoderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;

@Component(value = "domibusAuthenticationService")
public class AuthenticationDefaultService implements AuthenticationService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuthenticationDefaultService.class);

    public static final String BASIC_AUTH_HEADER_KEY = "Authorization";
    public static final String BASIC_AUTH_SCHEME_PREFIX = "Basic ";
    public static final String CLIENT_CERT_ATTRIBUTE_KEY = "javax.servlet.request.X509Certificate";
    public static final String CLIENT_CERT_HEADER_KEY = "Client-Cert";

    @Autowired
    protected AuthUtils authUtils;

    @Autowired
    @Qualifier("securityCustomAuthenticationProvider")
    private AuthenticationProvider authenticationProvider;

    @Autowired
    protected UserDomainService userDomainService;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    private DatabaseUtil databaseUtil;

    @Override
    public Authentication basicAuthenticate(String user, String password) {
        LOG.debug("Authenticating user [{}] using basic authentication", user);
        final String domainForUser = userDomainService.getDomainForUser(user);
        if (domainForUser == null) {
            throw new ConfigurationException("Could not find domain for user [" + user + "].");
        }
        domainContextProvider.setCurrentDomain(domainForUser);
        BasicAuthentication authentication = new BasicAuthentication(user, password);
        return authenticate(authentication);
    }

    @Override
    public void authenticate(HttpServletRequest httpRequest) throws AuthenticationException {

        /* if domibus allows unsecure login, do not authenticate anymore, just go on */
        if (authUtils.isUnsecureLoginAllowed()) {
            LOG.putMDC(DomibusLogger.MDC_USER, databaseUtil.getDatabaseUserName());
            LOG.securityInfo(DomibusMessageCode.SEC_UNSECURED_LOGIN_ALLOWED);
            return;
        }

        this.enforceAuthentication(httpRequest);
    }

    @Override
    public void enforceAuthentication(HttpServletRequest httpRequest) throws AuthenticationException {
        LOG.debug("Authenticating for [{}]", httpRequest.getRequestURI());

        final Object certificateAttribute = httpRequest.getAttribute(CLIENT_CERT_ATTRIBUTE_KEY);
        final String certHeaderValue = httpRequest.getHeader(CLIENT_CERT_HEADER_KEY);
        final String basicHeaderValue = httpRequest.getHeader(BASIC_AUTH_HEADER_KEY);

        if (basicHeaderValue != null) {
            LOG.debug("Basic authentication header found: [{}]", basicHeaderValue);
        }
        if (certificateAttribute != null) {
            LOG.debug("CertificateAttribute found: " + certificateAttribute.getClass());
        }
        if (certHeaderValue != null) {
            LOG.debug("Client certificate in header found: [{}]", certHeaderValue);
        }

        Authentication authenticationResult;
        if (basicHeaderValue != null && basicHeaderValue.startsWith(BASIC_AUTH_SCHEME_PREFIX)) {
            LOG.securityInfo(DomibusMessageCode.SEC_BASIC_AUTHENTICATION_USE);

            String basicAuthCredentials;
            try {
                basicAuthCredentials = new String(Base64.decode(basicHeaderValue.substring(BASIC_AUTH_SCHEME_PREFIX.length())));
            } catch (DecoderException ex) {
                throw new AuthenticationException("Could not decode authorization header", ex);
            }
            LOG.trace("Basic authentication: [{}]", basicAuthCredentials);
            int index = basicAuthCredentials.indexOf(":");
            String user = basicAuthCredentials.substring(0, index);
            String password = basicAuthCredentials.substring(index + 1);
            LOG.securityInfo(DomibusMessageCode.SEC_CONNECTION_ATTEMPT, httpRequest.getRemoteHost(), httpRequest.getRequestURL());
            authenticationResult = basicAuthenticate(user, password);
        } else if ("https".equalsIgnoreCase(httpRequest.getScheme())) {
            if (certificateAttribute == null) {
                throw new AuthenticationException("No client certificate present in the request");
            }
            if (!(certificateAttribute instanceof X509Certificate[])) {
                throw new AuthenticationException("Request value is not of type X509Certificate[] but of " + certificateAttribute.getClass());
            }
            LOG.securityInfo(DomibusMessageCode.SEC_X509CERTIFICATE_AUTHENTICATION_USE);
            final X509Certificate[] certificates = (X509Certificate[]) certificateAttribute;
            Authentication authentication = new X509CertificateAuthentication(certificates);
            String user = ((X509CertificateAuthentication) authentication).getCertificateId();
            final String domainForUser = userDomainService.getDomainForUser(user);
            domainContextProvider.setCurrentDomainWithValidation(domainForUser);
            authenticationResult = authenticate(authentication);
        } else if ("http".equalsIgnoreCase(httpRequest.getScheme())) {
            if (certHeaderValue == null) {
                throw new AuthenticationException(DomibusCoreErrorCode.DOM_002, "There is no valid authentication in this request and unsecure login is not allowed.");
            }
            LOG.securityInfo(DomibusMessageCode.SEC_BLUE_COAT_AUTHENTICATION_USE);
            Authentication authentication = new BlueCoatClientCertificateAuthentication(certHeaderValue);
            String user = ((BlueCoatClientCertificateAuthentication) authentication).getCertificateId();
            final String domainForUser = userDomainService.getDomainForUser(user);
            domainContextProvider.setCurrentDomainWithValidation(domainForUser);
            authenticationResult = authenticate(authentication);
        } else {
            throw new AuthenticationException("There is no valid authentication in this request and unsecure login is not allowed.");
        }

        if (authenticationResult.isAuthenticated()) {
            LOG.securityInfo(DomibusMessageCode.SEC_AUTHORIZED_ACCESS, httpRequest.getRemoteHost(), httpRequest.getRequestURL(), authenticationResult.getAuthorities());
        } else {
            LOG.securityInfo(DomibusMessageCode.SEC_UNAUTHORIZED_ACCESS, httpRequest.getRemoteHost(), httpRequest.getRequestURL());
        }
    }

    private Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Authentication authenticationResult;
        try {
            authenticationResult = authenticationProvider.authenticate(authentication);
        } catch (org.springframework.security.core.AuthenticationException exc) {
            throw new AuthenticationException("Error while authenticating " + authentication.getName(), exc);
        }

        if (authenticationResult.isAuthenticated()) {
            LOG.debug("Request authenticated. Storing the authentication result in the security context");
            LOG.debug("Authentication result: [{}]", authenticationResult);
            SecurityContextHolder.getContext().setAuthentication(authenticationResult);
            LOG.putMDC(DomibusLogger.MDC_USER, authenticationResult.getName());
        } else {
            throw new AuthenticationException("The certificate is not valid or is not present or the basic authentication credentials are invalid");
        }

        return authenticationResult;
    }
}
