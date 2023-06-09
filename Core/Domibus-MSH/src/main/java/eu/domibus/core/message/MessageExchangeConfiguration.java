package eu.domibus.core.message;

import eu.domibus.api.pmode.PModeConstants;

/**
 * @author Thomas Dussart
 * @since 3.3
 * Class in charge of keeping track of the exchange information.
 */
public class MessageExchangeConfiguration {

    private final String agreementName;
    private final String senderParty;
    private final String receiverParty;
    private final String service;
    private final String action;
    private final String leg;
    private final String mpc;
    private final String pmodeKey;
    private final String reversePmodeKey;

    public MessageExchangeConfiguration(final String agreementName, final String senderParty, final String receiverParty, final String service, final String action, final String leg, final String mpc) {
        this.agreementName = agreementName;
        this.senderParty = senderParty;
        this.receiverParty = receiverParty;
        this.service = service;
        this.action = action;
        this.mpc = mpc;
        this.leg = leg;
        this.pmodeKey = senderParty + PModeConstants.PMODEKEY_SEPARATOR + receiverParty + PModeConstants.PMODEKEY_SEPARATOR + service + PModeConstants.PMODEKEY_SEPARATOR + action + PModeConstants.PMODEKEY_SEPARATOR + agreementName + PModeConstants.PMODEKEY_SEPARATOR + leg;
        this.reversePmodeKey = receiverParty + PModeConstants.PMODEKEY_SEPARATOR + senderParty + PModeConstants.PMODEKEY_SEPARATOR + service + PModeConstants.PMODEKEY_SEPARATOR + action + PModeConstants.PMODEKEY_SEPARATOR + agreementName + PModeConstants.PMODEKEY_SEPARATOR + leg;
    }

    public MessageExchangeConfiguration(final String agreementName, final String senderParty, final String receiverParty, final String service, final String action, final String leg) {
        this(agreementName, senderParty, receiverParty, service, action, leg, null);
    }

    public String getAgreementName() {
        return agreementName;
    }

    public String getSenderParty() {
        return senderParty;
    }

    public String getReceiverParty() {
        return receiverParty;
    }

    public String getService() {
        return service;
    }

    public String getAction() {
        return action;
    }

    public String getLeg() {
        return leg;
    }

    public String getPmodeKey() {
        return pmodeKey;
    }

    public String getReversePmodeKey() {
        return reversePmodeKey;
    }

    public String getMpc() {
        return mpc;
    }

    @Override
    public String toString() {
        return "MessageExchangeConfiguration{" +
                "agreementName='" + agreementName + '\'' +
                ", senderParty='" + senderParty + '\'' +
                ", receiverParty='" + receiverParty + '\'' +
                ", service='" + service + '\'' +
                ", action='" + action + '\'' +
                ", mpc='" + mpc + '\'' +
                ", leg='" + leg + '\'' +
                ", pmodeKey='" + pmodeKey + '\'' +
                ", reversePmodeKey='" + reversePmodeKey + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageExchangeConfiguration that = (MessageExchangeConfiguration) o;

        if (agreementName != null ? !agreementName.equalsIgnoreCase(that.agreementName) : that.agreementName != null)
            return false;
        if (!senderParty.equalsIgnoreCase(that.senderParty)) return false;
        if (!receiverParty.equalsIgnoreCase(that.receiverParty)) return false;
        if (!service.equalsIgnoreCase(that.service)) return false;
        if (!action.equalsIgnoreCase(that.action)) return false;
        if (!mpc.equalsIgnoreCase(that.mpc)) return false;
        if (!leg.equalsIgnoreCase(that.leg)) return false;
        return pmodeKey.equalsIgnoreCase(that.pmodeKey);
    }

    @Override
    public int hashCode() {
        int result = agreementName != null ? agreementName.hashCode() : 0;
        result = 31 * result + senderParty.hashCode();
        result = 31 * result + receiverParty.hashCode();
        result = 31 * result + service.hashCode();
        result = 31 * result + action.hashCode();
        result = 31 * result + mpc.hashCode();
        result = 31 * result + leg.hashCode();
        result = 31 * result + pmodeKey.hashCode();
        return result;
    }
}
