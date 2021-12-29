package eu.domibus.core.message;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.*;
import eu.domibus.core.message.dictionary.*;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import eu.domibus.test.common.MessageTestUtility;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;


public class UserMessageDaoTestIT extends AbstractIT {

    @Autowired
    UserMessageDao userMessageDao;

    @Autowired
    protected MpcDao mpcDao;

    @Autowired
    protected MshRoleDao mshRoleDao;

    @Autowired
    protected PartyIdDao partyIdDao;

    @Autowired
    protected PartyRoleDao partyRoleDao;

    @Autowired
    protected ActionDao actionDao;

    @Autowired
    protected ServiceDao serviceDao;

    @Autowired
    protected AgreementDao agreementDao;
    
    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    @Transactional
    public void testSaveUserMessage() {
        final MessageTestUtility messageTestUtility = new MessageTestUtility();
        final UserMessage userMessage = messageTestUtility.createSampleUserMessage();

//        messageProperties.add(createProperty("originalSender", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1", STRING_TYPE));
//        messageProperties.add(createProperty("finalRecipient", FINAL_RECEIPIENT_VALUE, STRING_TYPE));

        final List<PartInfo> partInfoList = messageTestUtility.createPartInfoList(userMessage);

        PartyId senderPartyId = messageTestUtility.createSenderPartyId();
        partyIdDao.create(senderPartyId);
        userMessage.getPartyInfo().getFrom().setFromPartyId(senderPartyId);

        final PartyRole senderPartyRole = messageTestUtility.createSenderPartyRole();
        partyRoleDao.create(senderPartyRole);
        userMessage.getPartyInfo().getFrom().setFromRole(senderPartyRole);

        final PartyId receiverPartyId = messageTestUtility.createReceiverPartyId();
        partyIdDao.create(receiverPartyId);
        userMessage.getPartyInfo().getTo().setToPartyId(receiverPartyId);

        final PartyRole receiverPartyRole = messageTestUtility.createReceiverPartyRole();
        partyRoleDao.create(receiverPartyRole);
        userMessage.getPartyInfo().getTo().setToRole(receiverPartyRole);

        final ActionEntity actionEntity = messageTestUtility.createActionEntity();
        actionDao.create(actionEntity);
        userMessage.setAction(actionEntity);

        final ServiceEntity serviceEntity = messageTestUtility.createServiceEntity();
        serviceDao.create(serviceEntity);
        userMessage.setService(serviceEntity);

        final AgreementRefEntity agreementRefEntity = messageTestUtility.createAgreementRefEntity();
        agreementDao.create(agreementRefEntity);
        userMessage.setAgreementRef(agreementRefEntity);

        final MpcEntity mpcEntity = messageTestUtility.createMpcEntity();
        mpcDao.create(mpcEntity);
        userMessage.setMpc(mpcEntity);

        userMessageDao.create(userMessage);

        final UserMessage dbUserMessage = userMessageDao.findByEntityId(userMessage.getEntityId());
        final Set<MessageProperty> messageProperties = dbUserMessage.getMessageProperties();
        messageProperties.forEach(messageProperty -> messageProperty.getValue());

        userMessage.getPartyInfo().getFrom().getFromRole().getValue();

        System.out.println(userMessage);
    }
}
