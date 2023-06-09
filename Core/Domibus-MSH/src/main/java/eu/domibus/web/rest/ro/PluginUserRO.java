package eu.domibus.web.rest.ro;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.domibus.api.security.AuthType;
import eu.domibus.api.validators.CustomWhiteListed;
import eu.domibus.api.validators.SkipWhiteListed;
import eu.domibus.web.rest.validators.SizeIfPresent;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
public class PluginUserRO {

    private String entityId;

    @SizeIfPresent(min = 4, max = 255)
    @Pattern(regexp = "^[a-zA-Z0-9\\.@_]*$")
    private String userName;

    @SkipWhiteListed
    private String password;

    @CustomWhiteListed(permitted = "=,:")
    private String certificateId;

    private String originalUser;

    private String authRoles;

    private String authenticationType;

    private String status;

    private boolean active;

    private boolean suspended;

    private String domain;

    private LocalDateTime expirationDate;

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    /**
     * Returns the name of the user but only for basic type of users
     * @return the name
     */
    public String getUserName() {
        return StringUtils.equals(AuthType.BASIC.name(), authenticationType) ? userName : null;
    }

    public void setUserName(String username) {
        this.userName = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the id of the certificate of the user but only for certificate type of users
     * @return the certificate id
     */
    public String getCertificateId() {
        return StringUtils.equals(AuthType.CERTIFICATE.name(), authenticationType) ? certificateId : null;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public String getOriginalUser() {
        return originalUser;
    }

    public void setOriginalUser(String originalUser) {
        this.originalUser = originalUser;
    }

    public String getAuthRoles() {
        return authRoles;
    }

    public void setAuthRoles(String authRoles) {
        this.authRoles = authRoles;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    @JsonIgnore
    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

}
