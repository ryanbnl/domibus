package eu.domibus.common;

/**
 * @since 5.0
 * @author Catalin Enache
 */
public final class DomibusJMSConstants {

    private DomibusJMSConstants() {}

    /**
     * Bean name for the JMS connection factory that is to be used when working with Spring's
     * {@link org.springframework.jms.core.JmsOperations JmsTemplate}.
     *
     * <p><b>Note: for performance reasons, the actual bean instance must implement caching of message producers (e.g.
     * {@link org.springframework.jms.connection.CachingConnectionFactory})<b/></p>
     */
    public static final String DOMIBUS_JMS_CACHING_CONNECTION_FACTORY = "domibusJMS-CachingConnectionFactory";

    /**
     * Bean name for the JMS connection factory that is to be used when working with Spring
     * {@link org.springframework.jms.listener.DefaultMessageListenerContainer message listener containers}.
     *
     * <p><b>Note: the actual bean instance must take advantage of the caching provided by the message listeners
     * themselves and must not be used in conjunction with the
     * {@link org.springframework.jms.connection.CachingConnectionFactory} if dynamic scaling is required.<b/></p>
     */
    public static final String DOMIBUS_JMS_CONNECTION_FACTORY = "domibusJMS-ConnectionFactory";

}
