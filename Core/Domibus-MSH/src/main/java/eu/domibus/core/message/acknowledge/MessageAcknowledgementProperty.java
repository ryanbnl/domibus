package eu.domibus.core.message.acknowledge;

import eu.domibus.api.model.AbstractBaseEntity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
@Entity
@Table(name = "TB_MESSAGE_ACKNW_PROP")
public class MessageAcknowledgementProperty extends AbstractBaseEntity {

    @Column(name = "PROPERTY_NAME")
    private String name;

    @Column(name = "PROPERTY_VALUE")
    private String value;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "MESSAGE_ACK_ID_FK")
    protected MessageAcknowledgementEntity acknowledgementEntity;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public MessageAcknowledgementEntity getAcknowledgementEntity() {
        return acknowledgementEntity;
    }

    public void setAcknowledgementEntity(MessageAcknowledgementEntity acknowledgementEntity) {
        this.acknowledgementEntity = acknowledgementEntity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessageAcknowledgementProperty property = (MessageAcknowledgementProperty) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(name, property.name)
                .append(value, property.value)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(name)
                .append(value)
                .toHashCode();
    }
}
