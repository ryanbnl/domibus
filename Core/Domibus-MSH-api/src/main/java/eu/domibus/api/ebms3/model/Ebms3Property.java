package eu.domibus.api.ebms3.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.annotation.*;
import java.util.regex.Pattern;

/**
 * An eb:Property element is of xs:anySimpleType (e.g. string, URI) and has two attributes:
 * • @name: The value of this REQUIRED attribute must be agreed upon between partners.
 * • @type: This OPTIONAL attribute allows for resolution of conflicts between properties with the
 * same name, and may also help with Property grouping, e.g. various elements of an address.
 * Its actual semantics is beyond the scope of this specification. The element is intended to be consumed
 * outside the ebMS-specified functions. It may contain some information that qualifies or abstracts message
 * data, or that allows for binding the message to some business process. A representation in the header of
 * such properties allows for more efficient monitoring, correlating, dispatching and validating functions (even
 * if these are out of scope of ebMS specification) that do not require payload access.
 *
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Property", propOrder = "value")
public class Ebms3Property implements Comparable<Ebms3Property> {

    public static final String MIME_TYPE = "MimeType";
    public static final String CHARSET = "CharacterSet";
    public static final Pattern CHARSET_PATTERN = Pattern.compile("[A-Za-z]([A-Za-z0-9._-])*");
    public static final int VALUE_MAX_SIZE = 1024;

    @XmlValue
    protected String value;

    @XmlAttribute(name = "name", required = true)
    protected String name;

    @XmlAttribute(name = "type", required = false)
    protected String type;

    /**
     * Gets the value of the value property.
     *
     * @return possible object is {@link String }
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is {@link String }
     */
    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     */
    public void setName(final String value) {
        this.name = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Ebms3Property ebms3Property = (Ebms3Property) o;

        return new EqualsBuilder()
                .append(value, ebms3Property.value)
                .append(name, ebms3Property.name)
                .append(type, ebms3Property.type)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(value)
                .append(name)
                .append(type)
                .toHashCode();
    }

    @Override
    public int compareTo(final Ebms3Property o) {
        return this.hashCode() - o.hashCode();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("value", value)
                .append("name", name)
                .append("type", type)
                .toString();
    }
}
