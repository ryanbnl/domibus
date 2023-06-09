package eu.domibus.core.pmode;

import eu.domibus.api.ebms3.MessageExchangePattern;
import eu.domibus.common.JPAConstants;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.message.MessageExchangeConfiguration;
import eu.domibus.plugin.BackendConnector;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

import static eu.domibus.common.model.configuration.Process.*;

/**
 * @author Thomas Dussart
 * @since 3.3
 * {@inheritDoc}
 */
@Repository
public class ProcessDaoImpl extends BasicDao<Process> implements ProcessDao {

    private static final String LEG = "leg";
    private static final String INITIATOR_NAME = "initiatorName";
    private static final String RESPONDER_NAME = "responderName";
    private static final String MEP_BINDING = "mepBinding";
    private static final String INITIATOR = "initiator";
    private static final String MPC_NAME = "mpcName";

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    public ProcessDaoImpl() {
        super(Process.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Process> findPullProcessesByMessageContext(final MessageExchangeConfiguration messageExchangeConfiguration) {
        TypedQuery<Process> processQuery = entityManager.createNamedQuery(RETRIEVE_PULL_PROCESS_FROM_MESSAGE_CONTEXT, Process.class);
        processQuery.setParameter(LEG, messageExchangeConfiguration.getLeg());
        processQuery.setParameter(RESPONDER_NAME, messageExchangeConfiguration.getSenderParty());
        processQuery.setParameter(INITIATOR_NAME, messageExchangeConfiguration.getReceiverParty());
        processQuery.setParameter(MEP_BINDING, MessageExchangePattern.ONE_WAY_PULL.getUri());
        return processQuery.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Process> findPullProcessesByInitiator(final Party party) {
        TypedQuery<Process> processQuery = entityManager.createNamedQuery(FIND_PULL_PROCESS_TO_INITIATE, Process.class);
        processQuery.setParameter(MEP_BINDING, MessageExchangePattern.ONE_WAY_PULL.getUri());
        processQuery.setParameter(INITIATOR, party);
        return processQuery.getResultList();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Process> findPullProcessByMpc(final String mpc) {
        TypedQuery<Process> processQuery = entityManager.createNamedQuery(FIND_PULL_PROCESS_FROM_MPC, Process.class);
        processQuery.setParameter(MEP_BINDING, MessageExchangePattern.ONE_WAY_PULL.getUri());
        processQuery.setParameter(MPC_NAME, mpc);
        return processQuery.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Process> findPullProcessByLegName(final String legName) {
        final TypedQuery<Process> processQuery = this.entityManager.createNamedQuery(Process.FIND_PULL_PROCESS_FROM_LEG_NAME, Process.class);
        processQuery.setParameter("legName", legName);
        processQuery.setParameter(MEP_BINDING, MessageExchangePattern.ONE_WAY_PULL.getUri());
        return processQuery.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    public List<Process> findProcessByLegName(String legName) {
        final TypedQuery<Process> processQuery = this.entityManager.createNamedQuery(Process.FIND_PROCESS_FROM_LEG_NAME, Process.class);
        processQuery.setParameter("legName", legName);
        return processQuery.getResultList();
    }

    @Override
    public List<Process> findAllProcesses() {
        return this.entityManager.createNamedQuery(FIND_ALL_PROCESSES, Process.class).getResultList();
    }


}
