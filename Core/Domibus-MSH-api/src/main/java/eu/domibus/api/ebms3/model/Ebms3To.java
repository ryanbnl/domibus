package eu.domibus.api.ebms3.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.HashSet;
import java.util.Set;

/**
 * The REQUIRED element occurs
 * once, and contains information describing the destination party. *
 *
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "To", propOrder = {"partyId", "role"})
public class Ebms3To {

    public static final String DEFAULT_ROLE = Ebms3From.DEFAULT_ROLE;

    @XmlElement(name = "PartyId", required = true)
    protected Set<Ebms3PartyId> partyId;

    @XmlElement(name = "Role", required = true, defaultValue = Ebms3To.DEFAULT_ROLE)
    protected String role;

    /**
     * The
     * REQUIRED PartyId element occurs one or more times. If it occurs multiple times, each instance
     * MUST identify the same organization.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the partyId property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPartyId().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Ebms3PartyId }
     *
     * @return a reference to the live list of party id
     */
    public Set<Ebms3PartyId> getPartyId() {
        if (this.partyId == null) {
            this.partyId = new HashSet<>();
        }
        return this.partyId;
    }

    public String getFirstPartyId() {
        if(this.partyId == null || this.partyId.isEmpty()) {
            return null;
        }
        return this.partyId.iterator().next().getValue();
    }

    /**
     * The REQUIRED
     * eb:Role element occurs once, and identifies the authorized role (fromAuthorizedRole or
     * toAuthorizedRole) of the Party sending (when present as a child of the From element) or receiving
     * (when present as a child of the To element) the message. The value of the Role element is a nonempty
     * string, with a default value of
     * http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultRole .
     * Other possible values are subject to partner agreement.
     *
     * @return possible object is {@link String }
     */
    public String getRole() {
        return this.role;
    }

    /**
     * The REQUIRED
     * eb:Role element occurs once, and identifies the authorized role (fromAuthorizedRole or
     * toAuthorizedRole) of the Party sending (when present as a child of the From element) or receiving
     * (when present as a child of the To element) the message. The value of the Role element is a nonempty
     * string, with a default value of
     * http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultRole .
     * Other possible values are subject to partner agreement.
     *
     * @param value allowed object is {@link String }
     */
    public void setRole(final String value) {
        this.role = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Ebms3To)) return false;

        final Ebms3To ebms3To = (Ebms3To) o;

        if (!this.partyId.equals(ebms3To.partyId)) return false;
        return !(this.role != null ? !this.role.equalsIgnoreCase(ebms3To.role) : ebms3To.role != null);

    }

    @Override
    public int hashCode() {
        int result = this.partyId.hashCode();
        result = 31 * result + (this.role != null ? this.role.hashCode() : 0);
        return result;
    }
}
