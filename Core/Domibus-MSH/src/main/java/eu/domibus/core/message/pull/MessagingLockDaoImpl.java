package eu.domibus.core.message.pull;

import eu.domibus.api.model.MessageState;
import eu.domibus.api.property.DataBaseEngine;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.JPAConstants;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static eu.domibus.core.message.pull.PullMessageState.EXPIRED;
import static eu.domibus.core.message.pull.PullMessageState.RETRY;

/**
 * @author Thomas Dussart
 * @since 3.3.4
 */
@Repository
public class MessagingLockDaoImpl implements MessagingLockDao {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagingLockDaoImpl.class);

    private static final String MESSAGE_ID = "MESSAGE_ID";

    protected static final String IDPK = "idpk";

    private static final String MPC = "MPC";

    private static final String INITIATOR = "INITIATOR";

    protected static final String MESSAGE_STATE = "MESSAGE_STATE";

    private static final String CURRENT_TIMESTAMP = "CURRENT_TIMESTAMP";

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    private final DateUtil dateUtil;

    private final DomibusConfigurationService domibusConfigurationService;

    public MessagingLockDaoImpl(DateUtil dateUtil, DomibusConfigurationService domibusConfigurationService) {
        this.dateUtil = dateUtil;
        this.domibusConfigurationService = domibusConfigurationService;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PullMessageId getNextPullMessageToProcess(final String initiator, final String mpc) {
        try {
            String sqlString = (DataBaseEngine.ORACLE == domibusConfigurationService.getDataBaseEngine() ? "MessagingLock.lockQuerySkipBlocked_Oracle" :
                    "MessagingLock.lockQuerySkipBlocked_MySQL");
            Query q = entityManager.createNamedQuery(sqlString, MessagingLock.class);
            q.setParameter(MPC, mpc);
            q.setParameter(INITIATOR, initiator);
            q.setParameter(CURRENT_TIMESTAMP, dateUtil.getUtcDate());
            final MessagingLock messagingLock = (MessagingLock) q.getSingleResult();
            LOG.debug("[getNextPullMessageToProcess]:id[{}] locked", messagingLock.getEntityId());
            final String messageId = messagingLock.getMessageId();
            final int sendAttempts = messagingLock.getSendAttempts();
            final int sendAttemptsMax = messagingLock.getSendAttemptsMax();
            final Date messageStaled = messagingLock.getStaled();

            final Timestamp currentDate = new Timestamp(System.currentTimeMillis());
            LOG.debug("expiration date[{}], current date[{}] ", messageStaled, currentDate);
            if (messageStaled.compareTo(currentDate) < 0) {
                messagingLock.setMessageState(MessageState.DEL);
                return new PullMessageId(messageId, EXPIRED, String.format("Maximum time to send the message has been reached:[%tc]", messageStaled));
            }
            LOG.debug("sendattempts[{}], sendattemptsmax[{}]", sendAttempts, sendAttemptsMax);
            if (sendAttempts >= sendAttemptsMax) {
                messagingLock.setMessageState(MessageState.DEL);
                return new PullMessageId(messageId, EXPIRED, String.format("Maximum number of attempts to send the message has been reached:[%d]", sendAttempts));
            }
            if (sendAttempts >= 0) {
                messagingLock.setMessageState(MessageState.PROCESS);
            }
            if (sendAttempts > 0) {
                return new PullMessageId(messageId, RETRY);
            }
            return new PullMessageId(messageId);
        } catch (NoResultException ne) {
            LOG.trace("No message to lock found for for mpc=[{}], initiator=[{}]", mpc, initiator, ne);
            return null;
        } catch (Exception e) {
            LOG.error("MessageLock lock could not be acquired for mpc=[{}], initiator=[{}]", mpc, initiator, e);
            return null;
        }


    }

    public MessagingLock getLock(final String messageId) {
        try {
            LOG.debug("Message[{}] Getting lock", messageId);
            TypedQuery<MessagingLock> q = entityManager.createNamedQuery("MessagingLock.lockByMessageId", MessagingLock.class);
            q.setParameter(1, messageId);
            return q.getSingleResult();
        } catch (NoResultException nr) {
            LOG.trace("Message:[{}] lock not found. It is has been removed by another process.", messageId, nr);
            return null;
        } catch (Exception ex) {
            LOG.warn("Message:[{}] lock could not be acquire. It is probably handled by another process.", messageId, ex);
            return null;
        }
    }

    @Override
    public void save(final MessagingLock messagingLock) {
        entityManager.persist(messagingLock);
    }

    @Override
    public void delete(final String messageId) {
        Query query = entityManager.createNamedQuery("MessagingLock.delete");
        query.setParameter(MESSAGE_ID, messageId);
        query.executeUpdate();
    }

    @Override
    public void delete(final MessagingLock messagingLock) {
        entityManager.remove(messagingLock);
    }

    @Override
    public MessagingLock findMessagingLockForMessageId(final String messageId) {
        TypedQuery<MessagingLock> namedQuery = entityManager.createNamedQuery("MessagingLock.findForMessageId", MessagingLock.class);
        namedQuery.setParameter(MESSAGE_ID, messageId);
        try {
            return namedQuery.getSingleResult();
        } catch (NoResultException nr) {
            return null;
        }
    }

    @Override
    public List<MessagingLock> findStaledMessages() {
        TypedQuery<MessagingLock> query = entityManager.createNamedQuery("MessagingLock.findStalledMessages", MessagingLock.class);
        query.setParameter(CURRENT_TIMESTAMP, dateUtil.getUtcDate());
        return query.getResultList();
    }

    @Override
    public List<MessagingLock> findDeletedMessages() {
        TypedQuery<MessagingLock> query = entityManager.createNamedQuery("MessagingLock.findDeletedMessages", MessagingLock.class);
        return query.getResultList();
    }

    @Override
    public List<MessagingLock> findWaitingForReceipt() {
        final TypedQuery<MessagingLock> namedQuery = entityManager.createNamedQuery("MessagingLock.findWaitingForReceipt", MessagingLock.class);
        namedQuery.setParameter(CURRENT_TIMESTAMP, dateUtil.getUtcDate());
        return namedQuery.getResultList();
    }


}
