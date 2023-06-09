package eu.domibus.common.model.configuration;

import eu.domibus.api.model.AbstractBaseEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Christian Koch, Stefan Mueller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@Entity
@Table(name = "TB_PM_MPC")
@NamedQueries({@NamedQuery(name = "Mpc.countForQualifiedName", query = "select count(mpc) from Mpc mpc where mpc.qualifiedName=:QUALIFIED_NAME"),
        @NamedQuery(name = "Mpc.findByName", query = "select mpc from Mpc mpc where mpc.name=:NAME"),
        @NamedQuery(name = "Mpc.findByQualifiedName", query = "select mpc from Mpc mpc where mpc.qualifiedName=:QUALIFIED_NAME"),
        @NamedQuery(name = "Mpc.getAllNames", query = "select mpc.name from Mpc mpc"),
        @NamedQuery(name = "Mpc.getAllURIs", query = "select mpc.qualifiedName from Mpc mpc")})


public class Mpc extends AbstractBaseEntity {

    @XmlAttribute(name = "name", required = true)
    @Column(name = "NAME")
    protected String name;
    @XmlAttribute(name = "retention_downloaded", required = true)
    @Column(name = "RETENTION_DOWNLOADED")
    protected int retentionDownloaded;

    @XmlAttribute(name = "retention_undownloaded", required = true)
    @Column(name = "RETENTION_UNDOWNLOADED")
    protected int retentionUndownloaded;

    @XmlAttribute(name = "retention_sent")
    @Column(name = "RETENTION_SENT")
    protected int retentionSent = -1;

    @XmlAttribute(name = "retention_metadata_offset")
    @Column(name = "RETENTION_METADATA_OFFSET")
    protected int metadataRetentionOffset;

    @XmlAttribute(name = "delete_message_metadata")
    @Column(name = "DELETE_MESSAGE_METADATA")
    protected boolean deleteMessageMetadata = false;

    @XmlAttribute(name = "max_batch_delete")
    @Column(name = "MAX_BATCH_DELETE")
    protected int maxBatchDelete = -1;

    @XmlAttribute(name = "default", required = true)
    @Column(name = "DEFAULT_MPC")
    protected boolean defaultMpc;
    @XmlAttribute(name = "enabled", required = true)
    @Column(name = "IS_ENABLED")
    protected boolean enabled;
    @XmlAttribute(name = "qualifiedName", required = true)
    @Column(name = "QUALIFIED_NAME")
    protected String qualifiedName;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Mpc)) return false;
        if (!super.equals(o)) return false;

        final Mpc mpc = (Mpc) o;

        if (!name.equalsIgnoreCase(mpc.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

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
     * Gets the value of the retentionDownloaded property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public int getRetentionDownloaded() {
        return this.retentionDownloaded;
    }

    /**
     * Sets the value of the retentionDownloaded property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setRetentionDownloaded(final int value) {
        this.retentionDownloaded = value;
    }

    /**
     * Gets the value of the retentionUndownloaded property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public int getRetentionUndownloaded() {
        return this.retentionUndownloaded;
    }

    /**
     * Sets the value of the retentionUndownloaded property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setRetentionUndownloaded(final int value) {
        this.retentionUndownloaded = value;
    }

    /**
     * @return the number of minutes to keep the metadata after the payload is deleted
     */
    public int getMetadataRetentionOffset() {
        return metadataRetentionOffset;
    }

    /**
     * Sets the number of minutes to keep the metadata after the payload is deleted
     */
    public void setMetadataRetentionOffset(int retentionDownloadedMetadataOffset) {
        this.metadataRetentionOffset = retentionDownloadedMetadataOffset;
    }

    /**
     * Gets the value of the default property.
     * @return the value of the default property.
     */
    public boolean isDefault() {
        return this.defaultMpc;
    }

    /**
     * Sets the value of the default property.
     * @param value the new value of the default property.
     */
    public void setDefault(final boolean value) {
        this.defaultMpc = value;
    }

    /**
     * Gets the value of the enabled property.
     * @return the value of the enabled property.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Sets the value of the enabled property.
     * @param value the new value of the enabled property.
     */
    public void setEnabled(final boolean value) {
        this.enabled = value;
    }

    /**
     * Gets the value of the qualifiedName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getQualifiedName() {
        return this.qualifiedName;
    }

    /**
     * Sets the value of the qualifiedName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setQualifiedName(final String value) {
        this.qualifiedName = value;
    }

    public int getRetentionSent() {
        return retentionSent;
    }

    public void setRetentionSent(int retentionSent) {
        this.retentionSent = retentionSent;
    }

    public boolean isDeleteMessageMetadata() {
        return deleteMessageMetadata;
    }

    public void setDeleteMessageMetadata(boolean deleteMessageMetadata) {
        this.deleteMessageMetadata = deleteMessageMetadata;
    }

    public int getMaxBatchDelete() {
        return maxBatchDelete;
    }

    public void setMaxBatchDelete(int maxBatchDelete) {
        this.maxBatchDelete = maxBatchDelete;
    }

}
