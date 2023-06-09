package eu.domibus.core.audit.model;

import eu.domibus.api.audit.envers.RevisionLogicalName;
import eu.domibus.core.audit.envers.ModificationType;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Date;


/**
 * @author Thomas Dussart
 * @since 4.0
 *
 * Entity used to track actions on the Admin console Message log page.
 */
@Entity
@DiscriminatorValue("Message")
@RevisionLogicalName("Message")
public class MessageAudit extends AbstractGenericAudit {


    public MessageAudit() {
    }

    public MessageAudit(
            final String id,
            final String userName,
            final Date revisionDate,
            final ModificationType modificationType) {
        super(id, userName, revisionDate, modificationType);
    }
}
