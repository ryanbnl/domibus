package eu.domibus.api.ebms3.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.annotation.*;

/**
 * The
 * REQUIRED PartyId element occurs one or more times. If it occurs multiple times, each instance
 * MUST identify the same organization.
 *
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PartyId", propOrder = "value")
public class Ebms3PartyId implements Comparable<Ebms3PartyId> {

    @XmlValue
    protected String value;

    @XmlAttribute(name = "type")
    protected String type;

    /**
     * gets the party identifier.
     *
     * @return string value content that identifies a party, or that is one of the identifiers of this party.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the party identifier.
     *
     * @param value string value content that identifies a party, or that is one of the identifiers of this party.
     */
    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * The type attribute indicates the domain of names to which the string in the
     * content of the PartyId element belongs. It is RECOMMENDED that the value of the type attribute be a
     * URI. It is further RECOMMENDED that these values be taken from the EDIRA , EDIFACT or ANSI ASC
     * X12 registries. Technical specifications for the first two registries can be found at and [ISO6523] and
     * [ISO9735], respectively. Further discussion of PartyId types and methods of construction can be found in
     * an appendix of [ebCPPA21]. The value of any given @type attribute MUST be unique within a list of
     * PartyId elements.
     *
     * @return possible object is {@link String }
     */
    public String getType() {
        return this.type;
    }

    /**
     * The type attribute indicates the domain of names to which the string in the
     * content of the PartyId element belongs. It is RECOMMENDED that the value of the type attribute be a
     * URI. It is further RECOMMENDED that these values be taken from the EDIRA , EDIFACT or ANSI ASC
     * X12 registries. Technical specifications for the first two registries can be found at and [ISO6523] and
     * [ISO9735], respectively. Further discussion of PartyId types and methods of construction can be found in
     * an appendix of [ebCPPA21]. The value of any given @type attribute MUST be unique within a list of
     * PartyId elements.
     *
     * @param value allowed object is {@link String }
     */
    public void setType(final String value) {
        this.type = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Ebms3PartyId)) return false;

        final Ebms3PartyId ebms3PartyId = (Ebms3PartyId) o;

        if (this.type != null ? !this.type.equalsIgnoreCase(ebms3PartyId.type) : ebms3PartyId.type != null) return false;
        return this.value.equalsIgnoreCase(ebms3PartyId.value);

    }

    @Override
    public int hashCode() {
        int result = 31 + this.value.hashCode();
        result = 31 * result + (this.type != null ? this.type.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(final Ebms3PartyId o) {
        return this.hashCode() - o.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("value", value)
                .append("type", type)
                .toString();
    }
}
