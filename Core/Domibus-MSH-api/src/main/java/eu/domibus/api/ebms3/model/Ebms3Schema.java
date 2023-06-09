
package eu.domibus.api.ebms3.model;

import javax.xml.bind.annotation.*;

/**
 * This element occurs zero or more times. It refers to schema(s) that define the instance document
 * identified in the parent PartInfo element. If the item being referenced has schema(s) of some kind
 * that describe it (e.g. an XML Schema, DTD and/or a database schema), then the Schema
 * element SHOULD be present as a child of the PartInfo element. It provides a means of identifying
 * the schema and its version defining the payload object identified by the parent PartInfo element.
 * This metadata MAY be used to validate the Payload Part to which it refers, but the MSH is NOT
 * REQUIRED to do so. The Schema element contains the following attributes:
 *
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Schema")
public class Ebms3Schema {

    @XmlAttribute(name = "location", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String location;

    @XmlAttribute(name = "version")
    protected String version;

    @XmlAttribute(name = "namespace")
    protected String namespace;

    /**
     * Gets the REQUIRED URI of the schema
     *
     * @return possible object is {@link String }
     */
    public String getLocation() {
        return this.location;
    }

    /**
     * Sets the REQUIRED URI of the schema
     *
     * @param value allowed object is {@link String }
     */
    public void setLocation(final String value) {
        this.location = value;
    }

    /**
     * Gets an OPTIONAL version identifier of the schema.
     *
     * @return possible object is {@link String }
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Sets an OPTIONAL version identifier of the schema.
     *
     * @param value allowed object is {@link String }
     */
    public void setVersion(final String value) {
        this.version = value;
    }

    /**
     * Gets the OPTIONAL target namespace of the schema
     *
     * @return possible object is {@link String }
     */
    public String getNamespace() {
        return this.namespace;
    }

    /**
     * Sets the OPTIONAL target namespace of the schema
     *
     * @param value allowed object is {@link String }
     */
    public void setNamespace(final String value) {
        this.namespace = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Ebms3Schema)) return false;

        final Ebms3Schema ebms3Schema = (Ebms3Schema) o;

        if (location != null ? !location.equalsIgnoreCase(ebms3Schema.location) : ebms3Schema.location != null) return false;
        if (namespace != null ? !namespace.equalsIgnoreCase(ebms3Schema.namespace) : ebms3Schema.namespace != null) return false;
        if (version != null ? !version.equalsIgnoreCase(ebms3Schema.version) : ebms3Schema.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = location != null ? location.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
        return result;
    }
}
