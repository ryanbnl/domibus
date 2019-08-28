package eu.domibus.api.scheduler;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

public class DomibusSchedulerException extends DomibusCoreException {

    public DomibusSchedulerException(String message, Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, message, cause);
    }

    public DomibusSchedulerException(String message) {
        super(DomibusCoreErrorCode.DOM_001, message);
    }

    public DomibusSchedulerException(Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, cause.getMessage(), cause);
    }
}

