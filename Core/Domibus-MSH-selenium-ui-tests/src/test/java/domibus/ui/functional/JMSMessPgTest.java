package domibus.ui.functional;

import ddsl.dcomponents.grid.Pagination;
import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.jms.JMSMonitoringPage;
import pages.jms.JMSMoveMessageModal;
import utils.Gen;
import utils.TestRunData;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class JMSMessPgTest extends SeleniumTest {

    /* EDELIVERY-5134 - JMS-7 - Delete message */
    @Test(description = "JMS-7", groups = {"multiTenancy", "singleTenancy"})
    public void deleteJMSMessage() throws Exception {
        SoftAssert soft = new SoftAssert();

        String qWMess = rest.jms().getRandomQNameWithMessages();
        if (StringUtils.isEmpty(qWMess)) {
            throw new SkipException("No queue has messages");
        } else {
            Reporter.log("Navigate to JMS Messages page");
            log.info("Navigate to JMS Messages page");

            JMSMonitoringPage page = new JMSMonitoringPage(driver);
            page.getSidebar().goToPage(PAGES.JMS_MONITORING);

            int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
            page.grid().waitForRowsToLoad();

            Reporter.log("deleting first message listed");
            log.info("deleting first message listed");

            HashMap<String, String> rowInfo = page.grid().getRowInfo(0);
            page.grid().selectRow(0);
            page.getDeleteButton().click();
            Reporter.log("cancel delete");
            log.info("cancel delete");
            page.getCancelButton().click();
            new Dialog(driver).confirm();

            soft.assertTrue(page.grid().scrollTo("ID", rowInfo.get("ID")) >= 0, "Message still present in the grid after user cancels delete operation");

            Reporter.log("deleting first message listed");
            log.info("deleting first message listed");
            HashMap<String, String> rowInfo2 = page.grid().getRowInfo(0);
            page.grid().selectRow(0);
            Reporter.log("click delete");
            log.info("click delete");
            page.getDeleteButton().click();
            Reporter.log("saving ");
            log.info("saving ");
            page.getSaveButton().click();
            new Dialog(driver).confirm();

            soft.assertTrue(!page.getAlertArea().isError(), "Success message is shown");

            page.grid().waitForRowsToLoad();
            Reporter.log("check message is deleted from grid");
            log.info("check message is deleted from grid");
            soft.assertTrue(page.grid().scrollTo("ID", rowInfo2.get("ID")) < 0, "Message NOT present in the grid after delete operation");

        }

        soft.assertAll();
    }

    //	Disabled because functionality change and it needs to be updated
    /* EDELIVERY-5135 - JMS-8 - Move message */
    @Test(description = "JMS-8", groups = {"multiTenancy", "singleTenancy"})
    public void moveMessage() throws Exception {
        SoftAssert soft = new SoftAssert();

        String qWMess = rest.jms().getRandomQNameWithMessages();
        if (StringUtils.isEmpty(qWMess)) {
            throw new SkipException("No queue has messages");
        } else {
            Reporter.log("Navigate to JMS Messages page");
            log.info("Navigate to JMS Messages page");

            JMSMonitoringPage page = new JMSMonitoringPage(driver);
            page.getSidebar().goToPage(PAGES.JMS_MONITORING);
            page.grid().waitForRowsToLoad();
            int noOfMessInDQL = page.grid().getPagination().getTotalItems();

            int noOfMessages = page.filters().getJmsQueueSelect().selectQueueWithMessages();
            page.grid().waitForRowsToLoad();


            String queuename = page.filters().getJmsQueueSelect().getSelectedValue();


            Reporter.log("moving the first message");
            log.info("moving the first message");
            page.grid().selectRow(0);
            page.getMoveButton().click();

            Reporter.log("canceling");
            log.info("canceling");
            JMSMoveMessageModal modal = new JMSMoveMessageModal(driver);
            modal.getQueueSelect().selectDLQQueue();
            modal.clickCancel();

            Reporter.log("checking the number of messages");
            log.info("checking the number of messages");
            soft.assertEquals(noOfMessages, page.grid().getPagination().getTotalItems(), "Number of messages in current queue is not changed");

            page.filters().getJmsQueueSelect().selectDLQQueue();
            page.grid().waitForRowsToLoad();

            Reporter.log("getting no of messages in DLQ queue");
            log.info("getting no of messages in DLQ queue");
            soft.assertEquals(noOfMessInDQL, page.grid().getPagination().getTotalItems(), "Number of messages in DLQ message queue is not changed");

            Reporter.log("selecting queue " + queuename);
            log.info("selecting queue " + queuename);
            page.filters().getJmsQueueSelect().selectOptionByText(queuename);
            page.grid().waitForRowsToLoad();

            Reporter.log("getting info on row 0");
            log.info("getting info on row 0");
            HashMap<String, String> rowInfo = page.grid().getRowInfo(0);
            page.grid().selectRow(0);
            Reporter.log("moving message on row 0 to DLQ queue");
            log.info("moving message on row 0 to DLQ queue");
            page.getMoveButton().click();

            modal.getQueueSelect().selectDLQQueue();
            modal.clickOK();

            page.grid().waitForRowsToLoad();

            Reporter.log("checking success message");
            log.info("checking success message");
            soft.assertTrue(!page.getAlertArea().isError(), "Success message is shown");
            soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.JMS_MOVE_MESSAGE_SUCCESS, "Correct message is shown");

            Reporter.log("checking number of listed messages for this queue");
            log.info("checking number of listed messages for this queue");
            soft.assertTrue(page.grid().getPagination().getTotalItems() == noOfMessages - 1, "Queue has one less message");

            Reporter.log("selecting DLQ queue");
            log.info("selecting DLQ queue");
            page.filters().getJmsQueueSelect().selectDLQQueue();
            page.grid().waitForRowsToLoad();

            Reporter.log("checking no of messages in DLQ queue");
            log.info("checking no of messages in DLQ queue");
            soft.assertEquals(noOfMessInDQL + 1, page.grid().getPagination().getTotalItems(), "DQL queue has one more message after the move");

            int index = page.grid().scrollTo("ID", rowInfo.get("ID"));
            Reporter.log("checking the moved message is present in the grid");
            log.info("checking the moved message is present in the grid");
            soft.assertTrue(index > -1, "DQL queue contains the new message");
        }

        soft.assertAll();
    }

    /* EDELIVERY-5136 - JMS-9 - Domain admin logs in and views messages */
    @Test(description = "JMS-9", groups = {"multiTenancy"})
    public void adminOpenJMSMessagesPage() throws Exception {
        SoftAssert soft = new SoftAssert();
        String domainName = rest.getNonDefaultDomain();
        String domainCode = rest.getDomainCodeForName(domainName);
        Reporter.log("checking for domain " + domainCode);
        log.info("checking for domain " + domainCode);
        JSONObject user = rest.getUser(domainCode, DRoles.ADMIN, true, false, false);

        login(user.getString("userName"), data.defaultPass());
        Reporter.log("logging in with admin " + user.getString("userName"));
        log.info("logging in with admin " + user.getString("userName"));

        JMSMonitoringPage page = new JMSMonitoringPage(driver);
        page.getSidebar().goToPage(PAGES.JMS_MONITORING);

        Reporter.log("checking domain name in the title");
        log.info("checking domain name in the title");
        soft.assertEquals(page.getDomainFromTitle(), domainName, "Page title shows correct domain");
        soft.assertTrue(page.filters().isLoaded(), "Filters are loaded and visible");

        List<String> sources = page.filters().getJmsQueueSelect().getOptionsTexts();
        Reporter.log("checking message numbers are missing from queue source names");
        log.info("checking message numbers are missing from queue source names");
        for (String source : sources) {
            soft.assertTrue(!source.matches("\\(\\d\\)"), "Message numbers are not shown when admin is logged in");
        }

        List<HashMap<String, String>> allInfo = page.grid().getAllRowInfo();
        Reporter.log("checking messages contain domain name in Custom prop field");
        log.info("checking messages contain domain name in Custom prop field");
        for (HashMap<String, String> info : allInfo) {
            soft.assertTrue(info.get("Custom prop").contains(domainCode));
        }

        soft.assertAll();
    }

    private String getQWithMessages() throws Exception {

        String source = null;

        JSONArray queues = rest.jms().getQueues();
        for (int i = 0; i < queues.length(); i++) {
            if (queues.getJSONObject(i).getString("name").contains("DLQ")) {
                continue;
            } else if (queues.getJSONObject(i).getInt("numberOfMessages") > 0) {
                source = queues.getJSONObject(i).getString("name");
            }
        }

        if (null == source) {
            throw new SkipException("No messages found to move");
        }

        return source;
    }

    private String getDLQName() throws Exception {

        String destination = null;

        JSONArray queues = rest.jms().getQueues();
        for (int i = 0; i < queues.length(); i++) {
            if (queues.getJSONObject(i).getString("name").contains("DLQ")) {
                destination = queues.getJSONObject(i).getString("name");
            }
        }

        if (null == destination) {
            throw new SkipException("Could not find DLQ Q");
        }
        return destination;
    }

    /* EDELIVERY-5137 - JMS-10 - Super admin logs in and views messages for a selected domain, selects 1 message, and changes domain */
    @Test(description = "JMS-10", groups = {"multiTenancy"})
    public void changeDomainAfterSelection() throws Exception {
        SoftAssert soft = new SoftAssert();

        String q = rest.jms().getRandomQNameWithMessages();
        if (StringUtils.isEmpty(q)) {
            throw new SkipException("no queue has messages");
        }

        JMSMonitoringPage page = new JMSMonitoringPage(driver);
        Reporter.log("Login into application and navigate to JMS Monitoring page");
        log.info("Login into application and navigate to JMS Monitoring page");
        page.getSidebar().goToPage(PAGES.JMS_MONITORING);
        page.grid().waitForRowsToLoad();

        Reporter.log("Choose domain name from page title");
        log.info("Choose domain name from page title");
        String domain = selectRandomDomain();

        page.filters().getJmsQueueSelect().selectQueueByName(q);

        Reporter.log("select any message queue having some messages");
        log.info("select any message queue having some messages");
        Reporter.log("wait for grid row to load");
        log.info("wait for grid row to load");
        page.grid().waitForRowsToLoad();

        Reporter.log("select first row");
        log.info("select first row");
        page.grid().selectRow(0);

        Reporter.log("Confirm status of Move button and Delete button");
        log.info("Confirm status of Move button and Delete button");
        soft.assertTrue(page.moveButton.isEnabled(), "Move button is enabled on row selection");
        soft.assertTrue(page.deleteButton.isEnabled(), "Delete button is enabled on row selection");

        Reporter.log("select other domain from domain selector");
        log.info("select other domain from domain selector");
        String otherDomain = page.getDomainSelector().selectAnotherDomain();

        Reporter.log("Wait for page title");
        log.info("Wait for page title");
        page.waitForPageTitle();

        Reporter.log("Wait for grid row to load");
        log.info("Wait for grid row to load");
        page.grid().waitForRowsToLoad();

        Reporter.log("Compare old and new domain name");
        log.info("Compare old and new domain name");
        soft.assertTrue(page.getDomainFromTitle().equals(otherDomain), "Current domain differs from old domain");

        Reporter.log("Check status of move button and delete button");
        log.info("Check status of move button and delete button");
        soft.assertFalse(page.moveButton.isEnabled(), "Move button is not enabled");
        soft.assertFalse(page.deleteButton.isEnabled(), "Delete button is not enabled");


        soft.assertAll();
    }

    /* EDELIVERY-5138 - JMS-11 - Super admin logs in and views messages for a selected domain, navigates to second page of messages and changes domain */
    @Test(description = "JMS-11", groups = {"multiTenancy"})
    public void changeDomainFromSecondPage() throws Exception {
        SoftAssert soft = new SoftAssert();

        String q = rest.jms().getRandomQNameWithMessages();
        Reporter.log(String.format("found queue: {}", q));
        log.debug(String.format("found queue: {}", q));
        if (StringUtils.isEmpty(q)) {
            throw new SkipException("no queue has messages");
        }

        JMSMonitoringPage page = new JMSMonitoringPage(driver);
        Reporter.log("Login into application and navigate to JMS Monitoring page");
        log.info("Login into application and navigate to JMS Monitoring page");
        page.getSidebar().goToPage(PAGES.JMS_MONITORING);
        page.grid().waitForRowsToLoad();

        String domain = page.getDomainFromTitle();

        page.filters().getJmsQueueSelect().selectQueueByName(q);

        Reporter.log("select any message queue having some messages");
        log.info("select any message queue having some messages");
        Reporter.log("wait for grid row to load");
        log.info("wait for grid row to load");
        page.grid().waitForRowsToLoad();

        Reporter.log("going to next page");
        log.info("going to next page");
        Pagination p = page.grid().getPagination();
        if (!p.hasNextPage()) {
            throw new SkipException("not enough messages to have another page");
        }
        p.goToNextPage();


        Reporter.log("select other domain from domain selector");
        log.info("select other domain from domain selector");
        String otherDomain = page.getDomainSelector().selectAnotherDomain();

        Reporter.log("Wait for grid row to load");
        log.info("Wait for grid row to load");
        page.grid().waitForRowsToLoad();

        soft.assertTrue(p.getActivePage() == 1, "Pagination reset to first page");

        soft.assertAll();
    }


    /* EDELIVERY-5139 - JMS-12 - Super admin selects a message and chooses to delete it */
    @Test(description = "JMS-12", groups = {"multiTenancy"})
    public void jmsMsgDelOnDomainChange() throws Exception {
        SoftAssert soft = new SoftAssert();

        String q = rest.jms().getRandomQNameWithMessages();
        if (StringUtils.isEmpty(q)) {
            throw new SkipException("no queue has messages");
        }

        JMSMonitoringPage page = new JMSMonitoringPage(driver);
        Reporter.log("Login into application and navigate to JMS Monitoring page");
        log.info("Login into application and navigate to JMS Monitoring page");
        page.getSidebar().goToPage(PAGES.JMS_MONITORING);

        String domain = page.getDomainFromTitle();
        page.grid().waitForRowsToLoad();
        page.filters().getJmsQueueSelect().selectQueueByName(q);

        Reporter.log("select any message queue having some messages");
        log.info("select any message queue having some messages");
        Reporter.log("wait for grid row to load");
        log.info("wait for grid row to load");
        page.grid().waitForRowsToLoad();
        page.grid().selectRow(0);

        Reporter.log("Verify status of Move button and Delete button");
        log.info("Verify status of Move button and Delete button");
        soft.assertTrue(page.getMoveButton().isEnabled(), "Move button is enabled on selection");
        soft.assertTrue(page.getDeleteButton().isEnabled(), "Delete button is enabled on selection");

        String otherDomain = page.getDomainSelector().selectAnotherDomain();

        Reporter.log("Check message count in queue");
        log.info("Check message count in queue");
        page.grid().waitForRowsToLoad();

        page.filters().getJmsQueueSelect().selectQueueByName(q);

        Reporter.log("After domain chenge select any message queue having some messages");
        log.info("After domain chenge select any message queue having some messages");
        Reporter.log("wait for grid row to load");
        log.info("wait for grid row to load");
        page.grid().waitForRowsToLoad();

        int totalCount = page.grid().getPagination().getTotalItems();
        Reporter.log("Current message count is " + totalCount);
        log.info("Current message count is " + totalCount);

        Reporter.log("Select first row");
        log.info("Select first row");
        page.grid().selectRow(0);

        Reporter.log("Click on delete button");
        log.info("Click on delete button");
        page.getDeleteButton().click();

        Reporter.log("Click on save button");
        log.info("Click on save button");
        page.getSaveButton().click();
        new Dialog(driver).confirm();

        Reporter.log("Check presence of success message on deletion");
        log.info("Check presence of success message on deletion");
        soft.assertTrue(page.getAlertArea().getAlertMessage().contains("success"), "Success message is shown on deletion");

        Reporter.log("Verify queue message count as 1 less than before");
        log.info("Verify queue message count as 1 less than before");
        soft.assertTrue(page.grid().getPagination().getTotalItems() == totalCount - 1, "Queue message count is 1 less");

        soft.assertAll();
    }

    /* EDELIVERY-5148 - JMS-21 - Max date for Received Up To field    */
    @Test(description = "JMS-21", groups = {"multiTenancy", "singleTenancy"})
    public void checkReceivedUpTo() throws Exception {
        SoftAssert soft = new SoftAssert();

        String domain = selectRandomDomain();

        JMSMonitoringPage page = new JMSMonitoringPage(driver);
        page.getSidebar().goToPage(PAGES.JMS_MONITORING);
        page.grid().waitForRowsToLoad();

        Reporter.log("getting expectyed date");
        log.info("getting expectyed date");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        Date date = cal.getTime();
        String expectedDateStr = TestRunData.DATEWIDGET_DATE_FORMAT.format(date).trim();
        Reporter.log("expected date = " + expectedDateStr);
        log.debug("expected date = " + expectedDateStr);

        String pageDateStr = page.filters().getJmsToDatePicker().getSelectedDate();
        Reporter.log("Got date str from page: " + pageDateStr);
        log.info("Got date str from page: " + pageDateStr);

        Reporter.log("checking dates");
        log.info("checking dates");
        soft.assertEquals(pageDateStr, expectedDateStr, "Date string is as expected");

        soft.assertAll();

    }


    /* EDELIVERY-5150 - JMS-23 - Check queue message  count against each JMS queue in Search filter Source field in case of Admin */
    @Test(description = "JMS-23", groups = {"multiTenancy", "singleTenancy"})
    public void queueMsgCountOnInputFilter() throws Exception {
        SoftAssert soft = new SoftAssert();

        Reporter.log("navigate to JMS Monitoring page");
        log.info("navigate to JMS Monitoring page");
        JMSMonitoringPage page = new JMSMonitoringPage(driver);
        page.getSidebar().goToPage(PAGES.JMS_MONITORING);

        List<String> queues = page.filters().getJmsQueueSelect().getOptionsTexts();
        verifyQueueHasMessageCount(queues, true, soft);

        if (data.isMultiDomain()) {

            String domain = selectRandomDomain();

            String username = Gen.randomAlphaNumeric(10);
            rest.users().createUser(username, DRoles.ADMIN, data.defaultPass(), domain);
            Reporter.log("login with admin and navigate to JMS Monitoring page");
            log.info("login with admin and navigate to JMS Monitoring page");
            login(username, data.defaultPass()).getSidebar().goToPage(PAGES.JMS_MONITORING);
            page.grid().waitForRowsToLoad();

            queues = page.filters().getJmsQueueSelect().getOptionsTexts();
            verifyQueueHasMessageCount(queues, false, soft);
        }

        soft.assertAll();
    }

    /* EDELIVERY-5151 - JMS-24 - Check queue message count against each queue in destination on Move pop up in case of admin  */
    @Test(description = "JMS-24", groups = {"multiTenancy", "singleTenancy"})
    public void queueMsgCountOnMovePopUp() throws Exception {
        SoftAssert soft = new SoftAssert();

        String q = rest.jms().getRandomQNameWithMessages();
        if (StringUtils.isEmpty(q)) {
            throw new SkipException("no queue has messages");
        }

        JMSMonitoringPage page = new JMSMonitoringPage(driver);
        Reporter.log("Login into application and navigate to JMS Monitoring page");
        log.info("Login into application and navigate to JMS Monitoring page");
        page.getSidebar().goToPage(PAGES.JMS_MONITORING);

        Reporter.log("selecting queue with name " + q);
        log.info("selecting queue with name " + q);
        page.grid().waitForRowsToLoad();
        page.filters().getJmsQueueSelect().selectQueueByName(q);

        Reporter.log("Selecting row 0");
        log.info("Selecting row 0");
        page.grid().selectRow(0);

        Reporter.log("push move button");
        log.info("push move button");
        page.getMoveButton().click();

        JMSMoveMessageModal modal = new JMSMoveMessageModal(driver);
        List<String> queues = modal.getQueueSelect().getOptionsTexts();

        verifyQueueHasMessageCount(queues, true, soft);

        if (data.isMultiDomain()) {
            Reporter.log("Create Admin user for default domain");
            log.info("Create Admin user for default domain");
            String user = Gen.randomAlphaNumeric(10);
            rest.users().createUser(user, DRoles.ADMIN, data.defaultPass(), null);

            Reporter.log("Login into application with domain admin and navigate to JMS Monitoring page");
            log.info("Login into application with domain admin and navigate to JMS Monitoring page");
            login(user, data.defaultPass());
            page.getSidebar().goToPage(PAGES.JMS_MONITORING);

            Reporter.log("selecting queue with name " + q);
            log.info("selecting queue with name " + q);
            page.grid().waitForRowsToLoad();
            page.filters().getJmsQueueSelect().selectQueueByName(q);

            Reporter.log("Selecting row 0");
            log.info("Selecting row 0");
            page.grid().selectRow(0);

            Reporter.log("push move button");
            log.info("push move button");
            page.getMoveButton().click();

            modal = new JMSMoveMessageModal(driver);
            queues = modal.getQueueSelect().getOptionsTexts();

            verifyQueueHasMessageCount(queues, false, soft);
        }
        soft.assertAll();
    }

    /* EDELIVERY-5152 - JMS-25 - Check queue message  count against each JMS queue in Search filter Source field in case of Super  Admin */
    @Test(description = "JMS-25", groups = {"multiTenancy"})
    public void queueMsgCountForSuperAdmin() throws Exception {
        SoftAssert soft = new SoftAssert();

        selectRandomDomain();

        JMSMonitoringPage page = new JMSMonitoringPage(driver);
        Reporter.log("Login into application and navigate to Jms Monitoring Page");
        log.info("Login into application and navigate to Jms Monitoring Page");
        login(data.getAdminUser()).getSidebar().goToPage(PAGES.JMS_MONITORING);


        Reporter.log("Wait for grid row to load");
        log.info("Wait for grid row to load");
        page.grid().waitForRowsToLoad();


        List<String> queues = page.filters().getJmsQueueSelect().getOptionsTexts();
        verifyQueueHasMessageCount(queues, true, soft);

        soft.assertAll();
    }

    /* EDELIVERY-5153 - JMS-26 - Check queue message count against each queue in destination on Move pop up in case of Super admin */
    @Test(description = "JMS-26", groups = {"multiTenancy"})
    public void msgCountOnMoveForSuperAdmin() throws Exception {
        SoftAssert soft = new SoftAssert();

        String q = rest.jms().getRandomQNameWithMessages();
        if (StringUtils.isEmpty(q)) {
            throw new SkipException("no queue has messages");
        }

        JMSMonitoringPage page = new JMSMonitoringPage(driver);
        Reporter.log("Login into application and navigate to JMS Monitoring page");
        log.info("Login into application and navigate to JMS Monitoring page");
        page.getSidebar().goToPage(PAGES.JMS_MONITORING);

        Reporter.log("selecting queue with name " + q);
        log.info("selecting queue with name " + q);
        page.grid().waitForRowsToLoad();
        page.filters().getJmsQueueSelect().selectQueueByName(q);
        page.grid().waitForRowsToLoad();

        Reporter.log("Selecting row 0");
        log.info("Selecting row 0");
        page.grid().selectRow(0);

        Reporter.log("push move button");
        log.info("push move button");
        page.getMoveButton().click();

        JMSMoveMessageModal modal = new JMSMoveMessageModal(driver);
        List<String> queues = modal.getQueueSelect().getOptionsTexts();

        verifyQueueHasMessageCount(queues, true, soft);

        soft.assertAll();
    }

    private void verifyQueueHasMessageCount(List<String> queues, Boolean expectCount, SoftAssert soft) {
        Reporter.log("Checking message count in queue names is present");
        log.info("Checking message count in queue names is present");
        Reporter.log("Expecting count = " + expectCount);
        log.info("Expecting count = " + expectCount);

        for (String queue : queues) {
            soft.assertTrue(expectCount == queue.matches(".+\\(\\d+\\)$"),
                    String.format("Expecting count = %s not as expected for queue %s name: ", expectCount, queue));
        }
    }


}

