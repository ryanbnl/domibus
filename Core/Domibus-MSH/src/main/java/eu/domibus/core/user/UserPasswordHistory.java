package eu.domibus.core.user;

import eu.domibus.api.model.AbstractBaseEntity;
import eu.domibus.api.user.UserEntityBase;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

@MappedSuperclass
public class UserPasswordHistory<U extends UserEntityBase> extends AbstractBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private U user;

    @NotNull
    @Column(name = "USER_PASSWORD")
    private String password; //NOSONAR

    @Column(name = "PASSWORD_CHANGE_DATE")
    private LocalDateTime passwordChangeDate;

    public String getPasswordHash() {
        return password;
    }

    public UserPasswordHistory() {
    }

    public UserPasswordHistory(U user, String password, LocalDateTime passwordChangeDate) {
        this.user = user;
        this.password = password;
        this.passwordChangeDate = passwordChangeDate;
    }

}
