package eu.domibus.api.user;

import eu.domibus.api.security.AuthRole;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public class User implements UserBase {
    private String userName;
    private String email;
    private boolean active;
    private List<String> authorities;
    private String status;
    private String password;
    private String domain;
    private boolean suspended;
    private boolean deleted;
    private LocalDateTime expirationDate;
    private Boolean defaultPassword = false;

    public User() {
        authorities = new LinkedList<>();
    }

    @Override
    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    public List<String> getAuthorities() {
        return authorities;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String getPassword() {
        return password;
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

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUniqueIdentifier() {
        return getUserName();
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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isSuperAdmin() {
        return hasRole(AuthRole.ROLE_AP_ADMIN);
    }

    public boolean hasRole(AuthRole role) {
        if (authorities == null) {
            return false;
        }
        return authorities.contains(role.name());
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean isNew() {
        return UserState.NEW.name().equals(getStatus());
    }

    public Boolean hasDefaultPassword() {
        return this.defaultPassword;
    }
    public void setDefaultPassword(Boolean defaultPassword) {
        this.defaultPassword = defaultPassword;
    }
}
