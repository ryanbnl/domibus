package eu.domibus.core.user;

import eu.domibus.api.user.UserEntityBase;
import eu.domibus.core.dao.BasicDao;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

@Repository
public abstract class UserPasswordHistoryDaoImpl<U extends UserEntityBase, UPH extends UserPasswordHistory> extends BasicDao<UPH>
        implements UserPasswordHistoryDao<U> {

    private final Class<UPH> typeOfUPH;
    private String passwordHistoryQueryName;

    protected abstract UPH createNew(final U user, String passwordHash, LocalDateTime passwordDate);

    public UserPasswordHistoryDaoImpl(Class<UPH> typeOfUPH, String passwordHistoryQueryName) {
        super(typeOfUPH);
        this.typeOfUPH = typeOfUPH;
        this.passwordHistoryQueryName = passwordHistoryQueryName;
    }

    @Override
    public void savePassword(final U user, String passwordHash, LocalDateTime passwordDate) {
        UPH entry = createNew(user, passwordHash, passwordDate);
        this.update(entry);
    }

    @Override
    public void removePasswords(final U user, int oldPasswordsToKeep) {
        List<UserPasswordHistory> oldEntries = getPasswordHistory(user, 0);
        if (oldEntries.size() > oldPasswordsToKeep) {
            oldEntries.stream().skip(oldPasswordsToKeep).forEach(entry -> this.delete((UPH) entry)); // NOSONAR
        }
    }

    @Override
    public List<UserPasswordHistory> getPasswordHistory(U user, int entriesCount) {
        TypedQuery<UserPasswordHistory> namedQuery = em.createNamedQuery(passwordHistoryQueryName, UserPasswordHistory.class);
        namedQuery.setParameter("USER", user);
        if (entriesCount > 0) {
            namedQuery = namedQuery.setMaxResults(entriesCount);
        }
        return namedQuery.getResultList();
    }
}

