package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.Binding;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.core.pmode.validation.PModeValidator;
import eu.domibus.api.ebms3.MessageExchangePattern;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static eu.domibus.api.ebms3.Ebms3Constants.ONEWAY_MEP_VALUE;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * One-Way mep with binding pushAndPush, pullAndPush and pushAndPull is not valid.
 * This combination generates a warning, not an error, for backward compatibility.
 * <p>
 */
@Component
@Order(6)
public class OneWayMepValidator implements PModeValidator {

    private static final List<String> notAcceptedBindings = Arrays.asList(
            MessageExchangePattern.TWO_WAY_PUSH_PUSH.getUri(),
            MessageExchangePattern.TWO_WAY_PUSH_PULL.getUri(),
            MessageExchangePattern.TWO_WAY_PULL_PUSH.getUri()
    );

    @Override
    public List<ValidationIssue> validate(Configuration configuration) {
        List<ValidationIssue> issues = new ArrayList<>();

        configuration.getBusinessProcesses().getProcesses().forEach(process -> {
            if (process.getMep() != null && ONEWAY_MEP_VALUE.equalsIgnoreCase(process.getMep().getValue())) {
                Binding binding = process.getMepBinding();
                if (binding != null && binding.getValue() != null) {
                    ValidationIssue issue = validateBinding(binding, process.getName());
                    if (issue != null) {
                        issues.add(issue);
                    }
                }
            }
        });
        return Collections.unmodifiableList(issues);
    }

    protected ValidationIssue validateBinding(Binding binding, String processName) {
        String bindingValue = binding.getValue();
        if (notAcceptedBindings.stream().anyMatch(bindingValue::equalsIgnoreCase)) {
            String message = String.format("One-way mep with binding [%s] is not valid for process [%s].",
                    binding.getName(), processName);
            return new ValidationIssue(message, ValidationIssue.Level.WARNING);
        }

        return null;
    }
}
