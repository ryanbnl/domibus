package eu.domibus.api.user;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Ion Perpegel
 * @since 4.1
 * Base class for console and plugin users entities
 * It derives from an even narrower interface to be able to use some common code between these entities and the api.User class
 */
public interface UserEntityBase extends UserBase {

    long getEntityId();

    UserEntityBase.Type getType();

    String getUserName();

    String getPassword();

    void setPassword(String password);

    LocalDateTime getPasswordChangeDate();

    Boolean hasDefaultPassword();

    void setDefaultPassword(Boolean defaultPassword);

    Integer getAttemptCount();

    void setAttemptCount(Integer i);

    boolean isActive();

    void setActive(boolean b);

    Date getSuspensionDate();

    void setSuspensionDate(Date suspensionDate);

    enum Type {
        CONSOLE("console_user", "Console User"),
        PLUGIN("plugin_user", "Plugin User");

        private final String code;
        private final String name;

        Type(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {

            return code;
        }

        public String getName() {
            return name;
        }
    }
}
