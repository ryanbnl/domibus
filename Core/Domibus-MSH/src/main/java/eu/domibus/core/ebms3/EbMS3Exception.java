package eu.domibus.core.ebms3;

import eu.domibus.api.ebms3.model.Ebms3Description;
import eu.domibus.api.ebms3.model.Ebms3Error;
import eu.domibus.api.model.Description;
import eu.domibus.api.model.MSHRole;
import eu.domibus.common.ErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.dom.DOMDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.WebFault;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Christian Koch, Stefan Mueller
 *         This is the implementation of a ebMS3 Error Message
 */
@WebFault(name = "ebMS3Error")
public class EbMS3Exception extends Exception {

    /**
     * Default locale for error messages
     */
    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    /**
     * Default ResourceBundle name for error messages
     */
    public static final String RESOURCE_BUNDLE_NAME = "messages.ebms3.codes.MessagesBundle";
    /**
     * Default ResourceBundle for error messages
     */
    public static final ResourceBundle DEFAULT_MESSAGES = ResourceBundle.getBundle(EbMS3Exception.RESOURCE_BUNDLE_NAME, EbMS3Exception.DEFAULT_LOCALE);
    /**
     * Default value for recoverable
     */
    public static final boolean DEFAULT_RECOVERABLE = true;

    private final ErrorCode.EbMS3ErrorCode ebMS3ErrorCode;
    /**
     * "This OPTIONAL attribute provides a short description of the error that can be reported in a log, in order to facilitate readability."
     * (OASIS ebXML Messaging Services Version 3.0: Part 1, Core Features, 1 October 2007)
     */
    private String errorDetail;
    private String refToMessageId;
    private MSHRole mshRole;
    private boolean recoverable = DEFAULT_RECOVERABLE;
    private String signalMessageId;

    protected EbMS3Exception(final ErrorCode.EbMS3ErrorCode ebMS3ErrorCode, final String message, final Throwable cause) {
        super(message, cause);
        this.ebMS3ErrorCode = ebMS3ErrorCode;
    }

    public boolean isRecoverable() {
        return this.recoverable;
    }

    public void setRecoverable(final boolean recoverable) {
        this.recoverable = recoverable;
    }

    public Description getDescription() {
        return this.getDescription(EbMS3Exception.DEFAULT_MESSAGES);
    }

    public Description getDescription(final ResourceBundle bundle) {
        final Description description = new Description();
        description.setValue(bundle.getString(this.ebMS3ErrorCode.getCode().name()));
        description.setLang(bundle.getLocale().getLanguage());

        return description;
    }

    public Description getDescription(final Locale locale) {
        return this.getDescription(ResourceBundle.getBundle(EbMS3Exception.RESOURCE_BUNDLE_NAME, locale));
    }

    public String getErrorDetail() {
        return StringUtils.abbreviate(this.errorDetail, 255);
    }

    public void setErrorDetail(final String errorDetail) {
        this.errorDetail = errorDetail;
    }

    public String getOrigin() {
        return this.ebMS3ErrorCode.getCode().getOrigin();
    }

    public ErrorCode.EbMS3ErrorCode getErrorCode() {
        return ebMS3ErrorCode;
    }

    public ErrorCode getErrorCodeObject() {
        return ebMS3ErrorCode.getCode().getErrorCode();
    }

    public String getShortDescription() {
        return this.ebMS3ErrorCode.getShortDescription();
    }
    public String getSeverity() {
        return this.ebMS3ErrorCode.getSeverity();
    }

    public String getCategory() {
        return this.ebMS3ErrorCode.getCategory().name();
    }

    //this is a hack to avoid a classCastException in @see WebFaultOutInterceptor
    public Source getFaultInfo() {
        Document document = new DOMDocument("Empty_document");
        final Element firstElement = document.createElement("Empty_child");
        document.appendChild(firstElement);
        return new DOMSource(document);
    }

    public Ebms3Error getFaultInfoError() {

        final Ebms3Error ebMS3Error = new Ebms3Error();

        ebMS3Error.setOrigin(this.ebMS3ErrorCode.getCode().getOrigin());
        ebMS3Error.setErrorCode(this.ebMS3ErrorCode.getCode().getErrorCode().getErrorCodeName());
        ebMS3Error.setSeverity(this.ebMS3ErrorCode.getSeverity());
        ebMS3Error.setErrorDetail((this.errorDetail != null ? getErrorDetail() : ""));
        ebMS3Error.setCategory(this.ebMS3ErrorCode.getCategory().name());
        ebMS3Error.setRefToMessageInError(this.refToMessageId);
        ebMS3Error.setShortDescription(this.getShortDescription());
        Ebms3Description ebms3Description = new Ebms3Description();
        ebms3Description.setValue(this.getDescription().getValue());
        ebms3Description.setLang(this.getDescription().getLang());
        ebMS3Error.setDescription(ebms3Description);


        return ebMS3Error;
    }

    public String getRefToMessageId() {
        return this.refToMessageId;
    }

    public void setRefToMessageId(final String refToMessageId) {
        this.refToMessageId = refToMessageId;
    }

    public MSHRole getMshRole() {
        return this.mshRole;
    }

    public void setMshRole(final MSHRole mshRole) {
        this.mshRole = mshRole;
    }

    public String getSignalMessageId() {
        return this.signalMessageId;
    }

    public void setSignalMessageId(final String signalMessageId) {
        this.signalMessageId = signalMessageId;
    }


}

