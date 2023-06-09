package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.MailSender;
import eu.domibus.core.alerts.configuration.global.CommonConfigurationManager;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.model.service.MailModel;
import eu.domibus.core.user.ui.User;
import eu.domibus.core.user.ui.UserDao;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Thomas Dussart, Cosmin Baciu
 * @since 4.0
 */
@Service
public class AlertMethodEmail implements AlertMethod {

    static final String USERNAME_EVENT_PROPERTY = "USER";

    private static final Logger LOG = DomibusLoggerFactory.getLogger(AlertMethodEmail.class);

    @Autowired
    private AlertService alertService;

    @Autowired
    private MailSender mailSender;

    @Autowired
    private UserDao userDao;

    @Autowired
    private CommonConfigurationManager alertConfigurationManager;

    @Override
    public void sendAlert(Alert alert) {
        final MailModel<Map<String, String>> mailModelForAlert = alertService.getMailModelForAlert(alert);
        LOG.debug("Sending alert by email [{}]", alert);

        String from = alertConfigurationManager.getConfiguration().getSendFrom();
        String to = alertConfigurationManager.getConfiguration().getSendTo();
        mailSender.sendMail(mailModelForAlert, from, to);

        // if the alert is created from an event related to a user, send the email to the user address also
        Stream<Event> userEvents = alert.getEvents().stream().filter(event -> event.getType().isUserRelated());
        userEvents.forEach(event -> {
            if (!event.getType().getProperties().contains(USERNAME_EVENT_PROPERTY)) {
                LOG.debug("Event type [{}] should have [{}] property.", event.getType(), USERNAME_EVENT_PROPERTY);
                return;
            }

            Optional<String> userNameProp = event.findOptionalProperty(USERNAME_EVENT_PROPERTY);
            if (!userNameProp.isPresent()) {
                LOG.error("Event [{}] should have [{}] property set.", event, USERNAME_EVENT_PROPERTY);
                return;
            }

            String userName = userNameProp.get();
            if (StringUtils.isBlank(userName)) {
                LOG.error("Event [{}] should have [{}] property set to a non-empty value.", event, USERNAME_EVENT_PROPERTY);
                return;
            }

            User user = userDao.loadUserByUsername(userName);
            if (user == null) {
                LOG.error("Could not find a console user with the name [{}] .", userName);
                return;
            }

            String userEmail = user.getEmail();
            if (StringUtils.isBlank(userEmail)) {
                LOG.debug("User [{}] does not have an email to send an alert to.", userName);
                return;
            }
            mailSender.sendMail(mailModelForAlert, from, userEmail);
        });
    }

}
