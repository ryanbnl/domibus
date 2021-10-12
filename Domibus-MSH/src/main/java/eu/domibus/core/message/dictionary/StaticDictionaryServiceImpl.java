package eu.domibus.core.message.dictionary;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.NotificationStatus;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.core.jms.DomainsAware;
import eu.domibus.core.message.MessageStatusDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class StaticDictionaryServiceImpl implements StaticDictionaryService, DomainsAware {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(StaticDictionaryServiceImpl.class);

    protected MessageStatusDao messageStatusDao;
    protected NotificationStatusDao notificationStatusDao;
    protected MshRoleDao mshRoleDao;
    protected DomibusConfigurationService domibusConfigurationService;
    protected DomainTaskExecutor domainTaskExecutor;
    protected DomainService domainService;
    protected PlatformTransactionManager transactionManager;

    public StaticDictionaryServiceImpl(MessageStatusDao messageStatusDao,
                                       NotificationStatusDao notificationStatusDao,
                                       MshRoleDao mshRoleDao,
                                       DomibusConfigurationService domibusConfigurationService,
                                       DomainTaskExecutor domainTaskExecutor,
                                       DomainService domainService,
                                       PlatformTransactionManager transactionManager) {
        this.messageStatusDao = messageStatusDao;
        this.notificationStatusDao = notificationStatusDao;
        this.mshRoleDao = mshRoleDao;
        this.domibusConfigurationService = domibusConfigurationService;
        this.domainTaskExecutor = domainTaskExecutor;
        this.domainService = domainService;
        this.transactionManager = transactionManager;
    }

    @Transactional
    public void createStaticDictionaryEntries() {
        LOG.debug("Start checking and creating static dictionary entries if missing");

        Runnable createEntriesCall = createEntriesCall();

        if (domibusConfigurationService.isSingleTenantAware()) {
            LOG.debug("Start checking and creating static dictionary entries in single tenancy mode");
            createEntriesCall.run();
            return;
        }

        final List<Domain> domains = domainService.getDomains();
        createEntries(domains);
    }

    @Override
    public void domainsChanged(final List<Domain> added, final List<Domain> removed) {
        createEntries(added);
    }

    private void createEntries(List<Domain> domains) {
        Runnable createEntriesCall = createEntriesCall();
        Runnable transactionWrappedCall = transactionWrappedCall(createEntriesCall);
        for (Domain domain : domains) {
            LOG.debug("Start checking and creating static dictionary entries for domain [{}]", domain);
            domainTaskExecutor.submit(transactionWrappedCall, domain, true, 1L, TimeUnit.MINUTES);
        }
    }

    private Runnable transactionWrappedCall(Runnable createEntriesCall) {
        return () -> {
            new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    try {
                        createEntriesCall.run();
                    } catch (Exception e) {
                        LOG.error("Error while creating static dictionary entries", e);
                    }
                }
            });
        };
    }

    private Runnable createEntriesCall() {
        return () -> {
            Arrays.stream(MessageStatus.values()).forEach(messageStatus -> messageStatusDao.findOrCreate(messageStatus));
            Arrays.stream(NotificationStatus.values()).forEach(notificationStatus -> notificationStatusDao.findOrCreate(notificationStatus));
            Arrays.stream(MSHRole.values()).forEach(mshRole -> mshRoleDao.findOrCreate(mshRole));
        };
    }

}
