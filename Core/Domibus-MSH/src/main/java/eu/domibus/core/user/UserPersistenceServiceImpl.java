package eu.domibus.core.user;

import com.google.common.collect.Collections2;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.multitenancy.UserSessionsService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.DomibusUserDetails;
import eu.domibus.api.user.UserBase;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.api.user.UserState;
import eu.domibus.core.converter.AuthCoreMapper;
import eu.domibus.core.user.ui.User;
import eu.domibus.core.user.ui.UserDao;
import eu.domibus.core.user.ui.UserRole;
import eu.domibus.core.user.ui.UserRoleDao;
import eu.domibus.core.user.ui.security.ConsoleUserSecurityPolicyManager;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
@Service
public class UserPersistenceServiceImpl implements UserPersistenceService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserPersistenceServiceImpl.class);

    protected final UserDao userDao;

    protected final UserRoleDao userRoleDao;

    protected final BCryptPasswordEncoder bCryptEncoder;

    protected final AuthCoreMapper authCoreMapper;

    protected final UserDomainService userDomainService;

    protected final ConsoleUserSecurityPolicyManager securityPolicyManager;

    protected final UserSessionsService userSessionsService;

    protected final DomainContextProvider domainContextProvider; //NOSONAR: not necessary to be transient or serializable

    protected final DomibusConfigurationService domibusConfigurationService;

    protected final AuthUtils authUtils;

    public UserPersistenceServiceImpl(UserDao userDao, UserRoleDao userRoleDao, BCryptPasswordEncoder bCryptEncoder, AuthCoreMapper authCoreMapper,
                                      UserDomainService userDomainService, ConsoleUserSecurityPolicyManager securityPolicyManager,
                                      UserSessionsService userSessionsService, DomainContextProvider domainContextProvider,
                                      DomibusConfigurationService domibusConfigurationService, AuthUtils authUtils) {
        this.userDao = userDao;
        this.userRoleDao = userRoleDao;
        this.bCryptEncoder = bCryptEncoder;
        this.authCoreMapper = authCoreMapper;
        this.userDomainService = userDomainService;
        this.securityPolicyManager = securityPolicyManager;
        this.userSessionsService = userSessionsService;
        this.domainContextProvider = domainContextProvider;
        this.domibusConfigurationService = domibusConfigurationService;
        this.authUtils = authUtils;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateUsers(List<eu.domibus.api.user.User> users) {
        // insertion
        Collection<eu.domibus.api.user.User> newUsers = filterNewUsers(users);
        LOG.debug("New users: [{}]", newUsers.size());
        insertNewUsers(newUsers);

        // update
        Collection<eu.domibus.api.user.User> noPasswordChangedModifiedUsers = filterModifiedUserWithoutPasswordChange(users);
        LOG.debug("Modified users without password change: [{}]", noPasswordChangedModifiedUsers.size());
        updateUsers(noPasswordChangedModifiedUsers, false);

        Collection<eu.domibus.api.user.User> passwordChangedModifiedUsers = filterModifiedUserWithPasswordChange(users);
        LOG.debug("Modified users with password change: [{}]", passwordChangedModifiedUsers.size());
        updateUsers(passwordChangedModifiedUsers, true);

        // deletion
        List<eu.domibus.api.user.User> deletedUsers = filterDeletedUsers(users);
        LOG.debug("Users to delete: [{}]", deletedUsers.size());
        deleteUsers(deletedUsers);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void changePassword(String userName, String currentPassword, String newPassword) {
        User userEntity = userDao.loadUserByUsername(userName);
        changePassword(userEntity, currentPassword, newPassword);
        userDao.update(userEntity);
    }

    protected void updateUsers(Collection<eu.domibus.api.user.User> users, boolean withPasswordChange) {
        for (eu.domibus.api.user.User user : users) {
            updateUser(withPasswordChange, user);
        }
    }

    protected void updateUser(boolean withPasswordChange, eu.domibus.api.user.User user) {
        setUserDomainForMultiTenancy(user);
        User existing = userDao.loadUserByUsername(user.getUserName());

        checkCanUpdateIfCurrentUser(user, existing);

        securityPolicyManager.applyLockingPolicyOnUpdate(user, existing);
        existing.setActive(user.isActive());

        if (withPasswordChange) {
            changePassword(existing, user.getPassword());
        }

        existing.setEmail(user.getEmail());

        updateRolesIfNecessary(user, existing);

        userDao.update(existing);

        if (user.getAuthorities() != null && user.isSuperAdmin()) {
            userDomainService.setPreferredDomainForUser(user.getUserName(), user.getDomain());
        }
    }

    private void setUserDomainForMultiTenancy(eu.domibus.api.user.User user) {
        if (StringUtils.isNotEmpty(user.getDomain()) && domibusConfigurationService.isMultiTenantAware() && (domainContextProvider.getCurrentDomainSafely() != null)) {
            LOG.debug("Setting current domain:[{}]", user.getDomain());
            domainContextProvider.setCurrentDomain(user.getDomain());
        }
    }

    protected void checkCanUpdateIfCurrentUser(eu.domibus.api.user.User user, User existing) {
        DomibusUserDetails loggedUser = authUtils.getUserDetails();
        if (loggedUser == null || !StringUtils.equals(loggedUser.getUsername(), user.getUserName())) {
            LOG.debug("No need to validate the permission to update a user if it is different than the logged-in user [{}]; exiting.", user.getUserName());
            return;
        }
        if (existing.isActive() != user.isActive()) {
            throw new UserManagementException("Cannot change the active status of the logged-in user.");
        }
        if (!sameRoles(user, existing)) {
            throw new UserManagementException("Cannot change the role of the logged-in user.");
        }
    }

    protected void updateRolesIfNecessary(eu.domibus.api.user.User user, User existing) {
        if (sameRoles(user, existing)) {
            LOG.trace("Role didn't change for user [{}], no updates needed.", user.getUserName());
            return;
        }

        //if downgrade role then invalidate session
        if (existing.hasRole(AuthRole.ROLE_AP_ADMIN) || user.hasRole(AuthRole.ROLE_USER)) {
            LOG.trace("Downgrading user role, invalidate session of user [{}].", user.getUserName());
            userSessionsService.invalidateSessions(existing);
        }

        //roles have changed so update
        existing.clearRoles();
        addRoleToUser(user.getAuthorities(), existing);
    }

    protected boolean sameRoles(eu.domibus.api.user.User user, User existing) {
        String newRoles = user.getAuthorities().toString();
        String existingRoles = existing.getRoles().stream().map(role -> role.getName()).collect(Collectors.toList()).toString();
        return newRoles.equals(existingRoles);
    }

    protected void changePassword(User user, String currentPassword, String newPassword) {
        //check if old password matches the persisted one
        if (!bCryptEncoder.matches(currentPassword, user.getPassword())) {
            throw new UserManagementException("The current password does not match the provided one.");
        }

        changePassword(user, newPassword);
    }

    protected void changePassword(User user, String newPassword) {
        securityPolicyManager.changePassword(user, newPassword);
    }

    protected void insertNewUsers(Collection<eu.domibus.api.user.User> newUsers) {
        for (UserBase user : newUsers) {
            // validate user not already in general schema
            securityPolicyManager.validateUniqueUser(user);
        }

        for (eu.domibus.api.user.User user : newUsers) {
            setUserDomainForMultiTenancy(user);
            if (!user.hasDefaultPassword()) {
                securityPolicyManager.validateComplexity(user.getUserName(), user.getPassword());
            }
            User userEntity = authCoreMapper.userApiToUserSecurity(user);

            userEntity.setPassword(bCryptEncoder.encode(userEntity.getPassword()));
            addRoleToUser(user.getAuthorities(), userEntity);
            userDao.create(userEntity);

            if (user.isSuperAdmin()) {
                userDomainService.setPreferredDomainForUser(user.getUserName(), user.getDomain());
            } else {
                userDomainService.setDomainForUser(user.getUserName(), user.getDomain());
            }
        }
    }

    protected void deleteUsers(List<eu.domibus.api.user.User> usersToDelete) {
        for (eu.domibus.api.user.User userToDelete : usersToDelete) {
            setUserDomainForMultiTenancy(userToDelete);
            User user = userDao.loadUserByUsername(userToDelete.getUserName());
            if (user != null) {
                userDao.delete(user);
                userSessionsService.invalidateSessions(user);
            }
        }
    }

    protected void addRoleToUser(List<String> authorities, User userEntity) {
        if (authorities == null || userEntity == null) {
            return;
        }
        for (String authority : authorities) {
            UserRole userRole = userRoleDao.findByName(authority);
            userEntity.addRole(userRole);
        }
    }

    private Collection<eu.domibus.api.user.User> filterNewUsers(List<eu.domibus.api.user.User> users) {
        return Collections2.filter(users, user -> UserState.NEW.name().equals(user.getStatus()));
    }

    protected Collection<eu.domibus.api.user.User> filterModifiedUserWithoutPasswordChange(List<eu.domibus.api.user.User> users) {
        return Collections2.filter(users, user -> isUpdated(user) && !isPasswordChanged(user));
    }

    protected Collection<eu.domibus.api.user.User> filterModifiedUserWithPasswordChange(List<eu.domibus.api.user.User> users) {
        return Collections2.filter(users, user -> isUpdated(user) && isPasswordChanged(user));
    }

    protected boolean isUpdated(eu.domibus.api.user.User user) {
        return UserState.UPDATED.name().equals(user.getStatus());
    }

    protected boolean isPasswordChanged(eu.domibus.api.user.User user) {
        return StringUtils.isNotEmpty(user.getPassword());
    }

    private List<eu.domibus.api.user.User> filterDeletedUsers(List<eu.domibus.api.user.User> users) {
        return Collections2.filter(users, user -> UserState.REMOVED.name().equals(user.getStatus()))
                .stream().collect(Collectors.toList());
    }

}
