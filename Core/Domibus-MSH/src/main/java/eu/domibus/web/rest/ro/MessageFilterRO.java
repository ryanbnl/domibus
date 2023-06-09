package eu.domibus.web.rest.ro;

import eu.domibus.api.routing.RoutingCriteria;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * @author Tiago Miguel
 * @since 3.3
 */

public class MessageFilterRO implements Serializable {

    private String entityId;

    private int index;

    private List<RoutingCriteria> routingCriterias;

    private String backendName;

    private boolean isPersisted;

    private boolean active;

    private String enabledPropertyName;

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<RoutingCriteria> getRoutingCriterias() {
        return routingCriterias;
    }

    public void setRoutingCriterias(List<RoutingCriteria> routingCriterias) {
        this.routingCriterias = routingCriterias;
    }

    public String getBackendName() {
        return backendName;
    }

    public void setBackendName(String backendName) {
        this.backendName = backendName;
    }

    public boolean isPersisted() {
        return isPersisted;
    }

    public void setPersisted(boolean isPersisted) {
        this.isPersisted = isPersisted;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getEnabledPropertyName() {
        return enabledPropertyName;
    }

    public void setEnabledPropertyName(String enabledPropertyName) {
        this.enabledPropertyName = enabledPropertyName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessageFilterRO that = (MessageFilterRO) o;

        return new EqualsBuilder()
                .append(entityId, that.entityId)
                .append(index, that.index)
                .append(isPersisted, that.isPersisted)
                .append(routingCriterias, that.routingCriterias)
                .append(backendName, that.backendName)
                .append(enabledPropertyName, that.enabledPropertyName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(entityId)
                .append(index)
                .append(routingCriterias)
                .append(backendName)
                .append(enabledPropertyName)
                .append(isPersisted)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "MessageFilterRO{" +
                "entityId='" + entityId + '\'' +
                ", index=" + index +
                ", routingCriterias=" + routingCriterias +
                ", backendName='" + backendName + '\'' +
                ", isPersisted=" + isPersisted +
                ", enabledPropertyName=" + enabledPropertyName +
                '}';
    }
}
