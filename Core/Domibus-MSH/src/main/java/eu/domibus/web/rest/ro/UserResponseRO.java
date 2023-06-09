package eu.domibus.web.rest.ro;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.domibus.api.validators.SkipWhiteListed;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public class UserResponseRO {
    // order of the fields is important for CSV generation

    @Size(min = 4, max = 255)
    @Pattern(regexp = "^[a-zA-Z0-9\\.@_]*$")
    private String userName;

    private String roles = StringUtils.EMPTY;

    private String email;

    @SkipWhiteListed
    private String password;

    private boolean active;

    private List<String> authorities;

    private String status;

    private boolean suspended;

    private String domain;

    private String domainName;

    private boolean deleted;

    private LocalDateTime expirationDate;

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void updateRolesField() {
        int count = 0;
        String separator = StringUtils.EMPTY;
        roles = StringUtils.EMPTY;
        for (String authority : authorities) {
            if (count > 0) {
                separator = ",";
            }
            count++;
            roles += separator + authority;
        }
    }

    public List<String> getAuthorities() {
        return authorities;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }

    public String getRoles() {
        return roles;
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

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    @JsonIgnore
    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("userName", userName)
                .append("email", email)
                .append("active", active)
                .append("authorities", authorities)
                .append("roles", roles)
                .append("status", status)
                .append("domain", domain)
                .append("domain", domainName)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        UserResponseRO that = (UserResponseRO) o;

        return new EqualsBuilder()
                .append(active, that.active)
                .append(userName, that.userName)
                .append(email, that.email)
                .append(authorities, that.authorities)
                .append(roles, that.roles)
                .append(status, that.status)
                .append(password, that.password)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(userName)
                .append(email)
                .append(active)
                .append(authorities)
                .append(roles)
                .append(status)
                .append(password)
                .toHashCode();
    }
}
