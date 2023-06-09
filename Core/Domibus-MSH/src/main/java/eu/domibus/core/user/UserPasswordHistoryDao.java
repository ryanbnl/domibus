package eu.domibus.core.user;

import eu.domibus.api.user.UserEntityBase;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public interface UserPasswordHistoryDao<U extends UserEntityBase> {

    void savePassword(final U user, String passwordHash, LocalDateTime passwordDate);

    void removePasswords(final U user, int oldPasswordsToKeep);

    List<UserPasswordHistory> getPasswordHistory(final U user, int entriesCount);

}

