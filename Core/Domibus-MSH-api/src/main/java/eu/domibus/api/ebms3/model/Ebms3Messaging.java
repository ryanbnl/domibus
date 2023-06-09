package eu.domibus.api.ebms3.model;

import javax.validation.constraints.AssertTrue;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The eb:Messaging element is the top element of ebMS-3 headers, and it is
 * placed within the SOAP Header element (either SOAP 1.1 or SOAP 1.2). The
 * eb:Messaging element may contain several instances of eb:SignalMessage and
 * eb:UserMessage elements. However in the core part of the ebMS-3
 * specification, only one instance of either eb:UserMessage or eb:SignalMessage
 * must be present. As Domibus does only support ebMS3 core and AS4 profile this
 * implementation is limited to a SINGLE eb:SignalMessage and eb:UserMessage.
 *
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Messaging", propOrder = {"signalMessage", "userMessage", "any"})
@XmlRootElement(name = "Messaging")
public class Ebms3Messaging {


    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;

    @XmlElement(name = "SignalMessage")
    protected Ebms3SignalMessage signalMessage;

    @XmlElement(name = "UserMessage")
    protected Ebms3UserMessage userMessage;

    @XmlAttribute(name = "mustUnderstand", namespace = "http://www.w3.org/2003/05/soap-envelope")
    @AssertTrue
    protected Boolean s12MustUnderstand = true;

    @XmlAnyAttribute
    //According to how we read the spec those attributes serve no purpose in the AS4 profile, therefore they are discarded
    private final Map<QName, String> otherAttributes = new HashMap<>();

    @XmlAnyElement(lax = true)
    //According to how we read the spec those attributes serve no purpose in the AS4 profile, therefore they are discarded
    protected List<Object> any; //NOSONAR

    /**
     * The OPTIONAL element is named
     * after a type of Signal message. It contains all header information for the Signal message. If this
     * element is not present, an element describing a User message MUST be present. Three types of
     * Signal messages are specified in this document: Pull signal (eb:PullRequest), Error signal
     * (eb:Error) and Receipt signal (eb:Receipt).
     *
     * @return the SignalMessage
     */
    public Ebms3SignalMessage getSignalMessage() {
        return this.signalMessage;
    }

    /**
     * The OPTIONAL element is named
     * after a type of Signal message. It contains all header information for the Signal message. If this
     * element is not present, an element describing a User message MUST be present. Three types of
     * Signal messages are specified in this document: Pull signal (eb:PullRequest), Error signal
     * (eb:Error) and Receipt signal (eb:Receipt).
     *
     * @param ebms3SignalMessage the SignalMessage
     */
    public void setSignalMessage(final Ebms3SignalMessage ebms3SignalMessage) {
        this.signalMessage = ebms3SignalMessage;
    }

    /**
     * The OPTIONAL UserMessage element contains all header
     * information for a User message. If this element is not present, an element describing a Signal
     * message MUST be present.
     *
     * @return the UserMessage
     */
    public Ebms3UserMessage getUserMessage() {
        return this.userMessage;
    }

    /**
     * The OPTIONAL UserMessage element contains all header
     * information for a User message. If this element is not present, an element describing a Signal
     * message MUST be present.
     *
     * @param ebms3UserMessage the UserMessage
     */
    public void setUserMessage(final Ebms3UserMessage ebms3UserMessage) {
        this.userMessage = ebms3UserMessage;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is {@link String }
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is {@link String }
     */
    public void setId(final String value) {
        this.id = value;
    }

/*    *//**
     * indicates whether the contents of the element MUST
     * be understood by the MSH. This attribute is REQUIRED, with namespace qualified to the SOAP
     * namespace (http://schemas.xmlsoap.org/soap/envelope/). It MUST have value of '1' (true)
     * indicating the element MUST be understood or rejected.
     *
     * @return possible object is {@link String }
     *//*
    public Boolean isS11MustUnderstand() {
        return s11MustUnderstand;
    }

    *//**
     * indicates whether the contents of the element MUST
     * be understood by the MSH. This attribute is REQUIRED, with namespace qualified to the SOAP
     * namespace (http://schemas.xmlsoap.org/soap/envelope/). It MUST have value of '1' (true)
     * indicating the element MUST be understood or rejected.
     *
     * @param value allowed object is {@link String }
     *//*
    public void setS11MustUnderstand(final Boolean value) {
        this.s11MustUnderstand = value;
    }*/

    /**
     * if SOAP 1.2 is being used, this attribute is required
     *
     * @return possible object is {@link Boolean }
     */
    public boolean isS12MustUnderstand() {
        if (this.s12MustUnderstand == null) {
            return false;
        } else {
            return this.s12MustUnderstand;
        }
    }

    /**
     * Sets the value of the s12MustUnderstand property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setS12MustUnderstand(final Boolean value) {
        this.s12MustUnderstand = value;
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
