package eu.domibus.core.multitenancy.dao;


import eu.domibus.core.dao.BasicDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;


/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Repository
public class UserDomainDaoImpl extends BasicDao<UserDomainEntity> implements UserDomainDao {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserDomainDaoImpl.class);

    public UserDomainDaoImpl() {
        super(UserDomainEntity.class);
    }

    @Override
    public String findDomain(String userName) {
        UserDomainEntity userDomainEntity = findUserDomainEntity(userName);
        if (userDomainEntity == null) {
            return null;
        }
        return userDomainEntity.getDomain();
    }

    @Override
    public String findPreferredDomain(String userName) {
        UserDomainEntity userDomainEntity = findUserDomainEntity(userName);
        if (userDomainEntity == null) {
            return null;
        }
        return userDomainEntity.getPreferredDomain();
    }

    @Override
    public List<UserDomainEntity> listPreferredDomains() {
        TypedQuery<UserDomainEntity> namedQuery = em.createNamedQuery("UserDomainEntity.findPreferredDomains", UserDomainEntity.class);
        return namedQuery.getResultList();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateOrCreateUserDomain(String userName, String domainCode) {
        UserDomainEntity userDomainEntity = findUserDomainEntity(userName);
        if (userDomainEntity != null) {
            userDomainEntity.setDomain(domainCode);
            this.update(userDomainEntity);
        } else {
            userDomainEntity = new UserDomainEntity();
            userDomainEntity.setUserName(userName);
            userDomainEntity.setDomain(domainCode);
            this.create(userDomainEntity);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateOrCreateUserPreferredDomain(String userName, String preferredDomainCode) {
        UserDomainEntity userDomainEntity = findUserDomainEntity(userName);
        if (userDomainEntity != null) {
            userDomainEntity.setPreferredDomain(preferredDomainCode);
            this.update(userDomainEntity);
        } else {
            userDomainEntity = new UserDomainEntity();
            userDomainEntity.setUserName(userName);
            userDomainEntity.setPreferredDomain(preferredDomainCode);
            this.create(userDomainEntity);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void deleteUserDomain(String userName) {
        UserDomainEntity userDomainEntity = findUserDomainEntity(userName);
        if (userDomainEntity != null) {
            this.delete(userDomainEntity);
        }
    }

    private UserDomainEntity findUserDomainEntity(String userName) {
        TypedQuery<UserDomainEntity> namedQuery = em.createNamedQuery("UserDomainEntity.findByUserName", UserDomainEntity.class);
        namedQuery.setParameter("USER_NAME", userName);
        try {
            return namedQuery.getSingleResult();
        } catch (NoResultException e) {
            LOG.trace("User domain not found for [{}]", userName);
            return null;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int deleteByDomain(String domain) {
        Query query = em.createNamedQuery("UserDomainEntity.deleteByDomain");
        query.setParameter("DOMAIN", domain);
        return query.executeUpdate();
    }

}

