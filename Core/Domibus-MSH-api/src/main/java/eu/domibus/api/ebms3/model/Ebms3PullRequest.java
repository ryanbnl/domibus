package eu.domibus.api.ebms3.model;

import javax.xml.bind.annotation.*;
import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This transport-channel-bound MEP involves the transfer of a single ebMS User Message unit (label:
 * "oneway"). This MEP is initiated by the Receiving MSH, over a two-way underlying transport protocol. The
 * first leg of the protocol exchange carries a Pull Signal message. The second leg returns the pulled User
 * Message unit. To conform to this MEP the pulled User Message unit MUST NOT include an
 * eb:RefToMessageId element. In case no message is available for pulling, an ebMS error signal of severity
 * level "warning" and short description of "EmptyMessagePartitionChannel", as listed in Section 6.7.1,
 * MUST be returned over the response leg.
 *
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PullRequest", propOrder = "any")
public class Ebms3PullRequest {

    @XmlAnyAttribute
    //According to how we read the spec those attributes serve no purpose in the AS4 profile, therefore they are discarded
    private final Map<QName, String> otherAttributes = new HashMap<>();

    @XmlAnyElement(lax = true)
    //According to how we read the spec those attributes serve no purpose in the AS4 profile, therefore they are discarded
    protected List<Object> any;

    @XmlAttribute(name = "mpc")
    @XmlSchemaType(name = "anyURI")
    protected String mpc;

    /**
     * This OPTIONAL attribute identifies the Message Partition Channel from which the message is to
     * be pulled. The absence of this attribute indicates the default MPC.
     *
     * @return possible object is {@link String }
     */
    public String getMpc() {
        return this.mpc;
    }

    /**
     * This OPTIONAL attribute identifies the Message Partition Channel from which the message is to
     * be pulled. The absence of this attribute indicates the default MPC.
     *
     * @param value allowed object is {@link String }
     */
    public void setMpc(final String value) {
        this.mpc = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed
     * property on this class.
     *
     * <p>
     * the map is keyed by the name of the attribute and the value is the string
     * value of the attribute.
     * <p>
     * the map returned by this method is live, and you can add new attribute by
     * updating the map directly. Because of this design, there's no setter.
     *
     * @return always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return this.otherAttributes;
    }
}
