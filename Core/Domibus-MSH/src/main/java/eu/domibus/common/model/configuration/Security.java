package eu.domibus.common.model.configuration;

import eu.domibus.api.model.AbstractBaseEntity;
import eu.domibus.api.security.SecurityProfile;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="policy" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="signatureMethod" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 * @author Christian Koch, Stefan Mueller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@Entity
@Table(name = "TB_PM_SECURITY")
public class Security extends AbstractBaseEntity {

    @XmlAttribute(name = "name", required = true)
    @Column(name = "NAME")
    protected String name;
    @XmlAttribute(name = "policy", required = true)
    @Column(name = "POLICY")
    protected String policy;
    @XmlAttribute(name = "signatureMethod", required = true)
    @Enumerated(EnumType.STRING)
    @Column(name = "SIGNATURE_METHOD")
    protected AsymmetricSignatureAlgorithm signatureMethod;
    @XmlAttribute(name = "profile", required = true)
    @Enumerated(EnumType.STRING)
    @Column(name = "PROFILE")
    protected SecurityProfile profile;

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(final String value) {
        this.name = value;
    }

    /**
     * Gets the value of the policy property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPolicy() {
        return this.policy;
    }

    /**
     * Sets the value of the policy property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPolicy(final String value) {
        this.policy = value;
    }

    /**
     * Gets the value of the signatureMethod property.
     *
     * @return possible object is
     * {@link AsymmetricSignatureAlgorithm }
     */
    public AsymmetricSignatureAlgorithm getSignatureMethod() {
        return this.signatureMethod;
    }

    /**
     * Sets the value of the signatureMethod property.
     *
     * @param value allowed object is
     *              {@link AsymmetricSignatureAlgorithm }
     */
    public void setSignatureMethod(final AsymmetricSignatureAlgorithm value) {
        this.signatureMethod = value;
    }

    public SecurityProfile getProfile() {
        return profile;
    }

    public void setProfile(SecurityProfile securityProfile) {
        this.profile = securityProfile;
    }

    public void init(final Configuration configuration) {

    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Security)) return false;
        if (!super.equals(o)) return false;

        final Security security = (Security) o;

        if (!name.equalsIgnoreCase(security.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
