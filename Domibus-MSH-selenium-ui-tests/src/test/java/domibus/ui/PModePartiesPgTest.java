package domibus.ui;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import domibus.BaseUXTest;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.TestServicePage;
import pages.pmode.*;
import utils.Generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class PModePartiesPgTest extends BaseUXTest {

    private static String partyName = "Party Name";
    private static String endpoint = "End Point";
    private static String partyID = "Party Id";
    private static String process = "Process (I=Initiator, R=Responder, IR=Both)";
    private static String partyElement = "party";


    @Test(description = "PMP-1", groups = {"multiTenancy", "singleTenancy"})
    public void openPModePartiesPage() throws Exception {

        rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);

        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_PARTIES);

        PModePartiesPage page = new PModePartiesPage(driver);

        soft.assertTrue(page.filters().getNameInput().isEnabled(), "Page contains filter for party name");
        soft.assertTrue(page.filters().getEndpointInput().isEnabled(), "Page contains filter for party endpoint");
        soft.assertTrue(page.filters().getPartyIDInput().isEnabled(), "Page contains filter for party id");
        soft.assertTrue(page.filters().getProcessRoleSelect().isDisplayed(), "Page contains filter for process role");

        List<HashMap<String, String>> partyInfo = page.grid().getAllRowInfo();
        soft.assertTrue(partyInfo.size() == 2, "Grid contains both the parties described in PMode file");

        soft.assertTrue(!page.getCancelButton().isEnabled(), "Cancel button is NOT enabled");
        soft.assertTrue(!page.getSaveButton().isEnabled(), "Save button is NOT enabled");
        soft.assertTrue(!page.getEditButton().isEnabled(), "Edit button is NOT enabled");
        soft.assertTrue(!page.getDeleteButton().isEnabled(), "Delete button is NOT enabled");
        soft.assertTrue(page.getNewButton().isEnabled(), "New button is enabled");

        soft.assertAll();

    }

    @Test(description = "PMP-1.1", groups = {"multiTenancy", "singleTenancy"})
    public void selectRow() throws Exception {

        rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);

        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_PARTIES);

        PModePartiesPage page = new PModePartiesPage(driver);

        soft.assertTrue(!page.getEditButton().isEnabled(), "Edit button is not enabled");
        soft.assertTrue(!page.getDeleteButton().isEnabled(), "Delete button is not enabled");

        page.grid().selectRow(0);

        soft.assertTrue(page.getEditButton().isEnabled(), "Edit button is enabled after select row");
        soft.assertTrue(page.getDeleteButton().isEnabled(), "Delete button is enabled after select row");

        soft.assertAll();

    }

    @Test(description = "PMP-2", groups = {"multiTenancy", "singleTenancy"})
    public void filterParties() throws Exception {

        rest.uploadPMode("pmodes/multipleParties.xml", null);

        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_PARTIES);

        PModePartiesPage page = new PModePartiesPage(driver);

        soft.assertTrue(page.filters().getNameInput().isEnabled(), "Page contains filter for party name");

        HashMap<String, String> firstParty = page.grid().getRowInfo(0);

        page.filters().getNameInput().fill(firstParty.get(partyName));
        page.filters().getEndpointInput().fill(firstParty.get(endpoint));
        page.filters().getPartyIDInput().fill(firstParty.get(partyID));
        page.filters().getSearchButton().click();

        page.grid().waitForRowsToLoad();

        soft.assertEquals(page.grid().getRowsNo(), 1, "1 rows returned");
        soft.assertEquals(page.grid().getRowInfo(0).get(partyName), firstParty.get(partyName), "first party is returned");


        soft.assertAll();
    }

    @Test(description = "PMP-3", groups = {"multiTenancy", "singleTenancy"})
    public void doubleClickRow() throws Exception {

        rest.uploadPMode("pmodes/multipleParties.xml", null);

        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_PARTIES);

        PModePartiesPage page = new PModePartiesPage(driver);

        HashMap<String, String> firstParty = page.grid().getRowInfo(0);
        page.grid().doubleClickRow(0);

        PartyModal modal = new PartyModal(driver);

        soft.assertEquals(firstParty.get(partyName), modal.getNameInput().getText(), "Listed party name is correct");
        soft.assertEquals(firstParty.get(endpoint), modal.getEndpointInput().getText(), "Listed party endpoint is correct");

        List<String> toCompare = new ArrayList<>();
        for (HashMap<String, String> info : modal.getIdentifierTable().getAllRowInfo()) {
            soft.assertTrue(firstParty.get(partyID).contains(info.get("Party Id")), "id is listed");
        }

        soft.assertAll();
    }

    @Test(description = "PMP-4", groups = {"multiTenancy", "singleTenancy"})
    public void deleteParty() throws Exception {

        rest.uploadPMode("pmodes/multipleParties.xml", null);

        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_PARTIES);

        PModePartiesPage page = new PModePartiesPage(driver);

        HashMap<String, String> firstParty = page.grid().getRowInfo(0);
        page.grid().selectRow(0);
        page.getDeleteButton().click();

        soft.assertTrue(page.getSaveButton().isEnabled(), "Save button is active");
        soft.assertTrue(page.getCancelButton().isEnabled(), "Cancel button is active");

        page.getCancelButton().click();
        soft.assertEquals(page.grid().getRowInfo(0).get(partyName), firstParty.get(partyName), "After cancel party is still present in grid");

        page.grid().selectRow(0);
        page.getDeleteButton().click();
        page.getSaveButton().click();

        soft.assertTrue(page.grid().scrollTo(partyName, firstParty.get(partyName)) == -1, "After save party is NOT present in grid");


        soft.assertAll();
    }

    @Test(description = "PMP-5", groups = {"multiTenancy", "singleTenancy"})
    public void createParty() throws Exception {

        rest.uploadPMode("pmodes/multipleParties.xml", null);
        String newPatyName = Generator.randomAlphaNumeric(5);
        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_PARTIES);

        PModePartiesPage page = new PModePartiesPage(driver);

        soft.assertTrue(page.getNewButton().isEnabled(), "New button is enabled");
        page.getNewButton().click();

        PartyModal modal = new PartyModal(driver);
        modal.fillNewPartyForm(newPatyName, "http://test.com", "pid");
        modal.clickOK();

        page.wait.forXMillis(1000);
        page.getSaveButton().click();
        page.wait.forXMillis(5000);


        soft.assertTrue(!page.getAlertArea().isError(), "page shows success message");
        soft.assertTrue(StringUtils.equalsIgnoreCase(page.getAlertArea().getAlertMessage(),
                DMessages.PMODE_PARTIES_UPDATE_SUCCESS), "page shows correct success message");


        soft.assertTrue(page.grid().scrollTo(partyName, newPatyName) >= 0, "party is shown in grid");

        soft.assertAll();
    }

    @Test(description = "PMP-6", groups = {"multiTenancy", "singleTenancy"})
    public void editParty() throws Exception {

        rest.uploadPMode("pmodes/multipleParties.xml", null);
        String newPatyName = Generator.randomAlphaNumeric(5);

        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_PARTIES);

        PModePartiesPage page = new PModePartiesPage(driver);

        page.grid().selectRow(0);

        page.getEditButton().click();

        PartyModal modal = new PartyModal(driver);

        modal.getNameInput().fill(newPatyName);
        modal.getEndpointInput().fill("http://" + newPatyName.toLowerCase() + ".com");
        modal.clickOK();

        page.wait.forXMillis(1000);
        page.getSaveButton().click();
        page.wait.forXMillis(5000);


        soft.assertTrue(!page.getAlertArea().isError(), "Success message is shown");

        soft.assertTrue(page.grid().scrollTo(partyName, newPatyName) >= 0, "New name is visible in grid");
        soft.assertTrue(page.grid().scrollTo(endpoint, "http://" + newPatyName + ".com") >= 0, "New endpoint is visible in grid");

        soft.assertAll();
    }

    @Test(description = "PMP-7", groups = {"multiTenancy", "singleTenancy"})
    public void editPartyAndCancel() throws Exception {

        rest.uploadPMode("pmodes/multipleParties.xml", null);
        String newPatyName = Generator.randomAlphaNumeric(5);

        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_PARTIES);

        PModePartiesPage page = new PModePartiesPage(driver);

        page.grid().selectRow(0);

        page.getEditButton().click();

        PartyModal modal = new PartyModal(driver);

        modal.getNameInput().fill(newPatyName);
        modal.getEndpointInput().fill("http://" + newPatyName.toLowerCase() + ".com");
        modal.clickOK();
        page.getCancelButton().click();


        soft.assertTrue(page.grid().scrollTo(partyName, newPatyName) < 0, "New name is NOT visible in grid");
        soft.assertTrue(page.grid().scrollTo(endpoint, "http://" + newPatyName + ".com") < 0, "New endpoint is NOT visible in grid");

        soft.assertAll();
    }

    @Test(description = "PMP-8", groups = {"multiTenancy"})
    public void domainSegregation() throws Exception {

        String domainName = rest.getDomainNames().get(1);
        String domainCode = rest.getDomainCodeForName(domainName);

        rest.uploadPMode("pmodes/multipleParties.xml", null);
        rest.uploadPMode("pmodes/doNothingSelfSending.xml", domainCode);

        SoftAssert soft = new SoftAssert();
        login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.PMODE_PARTIES);

        PModePartiesPage page = new PModePartiesPage(driver);

        int noOfParties = page.grid().getRowsNo();

        page.getDomainSelector().selectOptionByText(domainName);

        int domainNoOfParties = page.grid().getRowsNo();
        soft.assertTrue(noOfParties != domainNoOfParties, "Number of parties doesn't coincide");


        soft.assertAll();
    }

    @Test(description = "PMP-23", groups = {"multiTenancy"})
    public void PmodePartyAddition() throws Exception {
        log.info("upload pmode");
        rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        PModePartiesPage Ppage = new PModePartiesPage(driver);
        PModeArchivePage Apage = new PModeArchivePage(driver);
        PModeCurrentPage Cpage = new PModeCurrentPage(driver);
        log.info("Navigate to Pmode parties page");
        page.getSidebar().gGoToPage(PAGES.PMODE_PARTIES);
        log.info("Validate whether New button is enabled ");
        soft.assertTrue(Ppage.getNewButton().isEnabled(), "New button is enabled");
        log.info("Click on New button");
        Ppage.getNewButton().click();
        log.info("Generate random New Party Name");
        String newPatyName = Generator.randomAlphaNumeric(5);
        PartyModal modal = new PartyModal(driver);
        log.info("Fill New Party Form");
        modal.fillNewPartyForm(newPatyName, "http://test.com", "pid");
        log.info("Click On Ok Button");
        modal.clickOK();
        Ppage.wait.forXMillis(1000);
        log.info("Click on Save button");
        Ppage.getSaveButton().click();
        Ppage.wait.forXMillis(5000);
        log.info("Validate Success Message");
        soft.assertTrue(!Ppage.getAlertArea().isError(), "page shows success message");
        log.info("Validate presence of New Party in Grid");
        soft.assertTrue(Ppage.grid().scrollTo(partyName, newPatyName) >= 0, "party is shown in grid");
        log.info("Navigate to Pmode current page");
        page.getSidebar().gGoToPage(PAGES.PMODE_CURRENT);
        page.waitForTitle();
        soft.assertTrue(Apage.getPage().getTextArea().isPresent(), "Current pmode is available");
        String UpdatedPmode = Apage.getPage().getTextArea().getText();
        log.info("Current Pmode is :" + UpdatedPmode);
        log.info("Validate presence of new party name in Current Pmode");
        soft.assertTrue(UpdatedPmode.contains(newPatyName), "New party is shown in Current pmode");
        soft.assertAll();

    }

    @Test(description = "PMP-24", groups = {"multiTenancy"})
    public void PmodePartyRemoval() throws Exception {
        log.info("upload pmode");
        rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        PModePartiesPage Ppage = new PModePartiesPage(driver);
        PModeArchivePage Apage = new PModeArchivePage(driver);
        PModeCurrentPage Cpage = new PModeCurrentPage(driver);
        log.info("Navigate to Current Pmode");
        page.getSidebar().gGoToPage(PAGES.PMODE_CURRENT);
        String DefaultPmode = Apage.getPage().getTextArea().getText();
        log.info("validate presence of Current system party name");
        soft.assertTrue(DefaultPmode.contains("<ns2:configuration xmlns:ns2=\"http://domibus.eu/configuration\" party=\"blue_gw\">"));
        page.getSidebar().gGoToPage(PAGES.PMODE_PARTIES);
        log.info("Select row other than current system party name");
        for (int i = 0; i < Ppage.grid().getRowsNo(); i++) {
            if (!Ppage.grid().getRowInfo(i).containsValue("blue_gw")) {
                Ppage.grid().selectRow(i);
            }
        }
        log.info("Click on Delete button");
        Ppage.getDeleteButton().click();
        log.info("Click on Save button");
        Ppage.getSaveButton().click();
        log.info(page.getAlertArea().getAlertMessage());
        log.info("Navigate to Pmode Current page");
        page.getSidebar().gGoToPage(PAGES.PMODE_CURRENT);
        page.waitForTitle();
        soft.assertTrue(Apage.getPage().getTextArea().isPresent(), "Current pmode is available");
        String UpdatedPmode = Apage.getPage().getTextArea().getText();
        log.info("Current Pmode is :" + UpdatedPmode);
        log.info("Validate absence of party name :red_gw ");
        soft.assertFalse(UpdatedPmode.contains("<party name=\"red_gw\""), "red_gw party is not shown in Current pmode");
        soft.assertAll();

    }

    @Test(description = "PMP-25", groups = {"multiTenancy"})
    public void ResponderInitiatorRemoval() throws Exception {
        log.info("upload Pmode");
        rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
        DomibusPage page = new DomibusPage(driver);
        SoftAssert soft = new SoftAssert();
        PModeArchivePage Apage = new PModeArchivePage(driver);
        log.info("Navigate to Pmode Current page");
        page.getSidebar().gGoToPage(PAGES.PMODE_CURRENT);
        String defaultPmode = Apage.getPage().getTextArea().getText();
        log.info("Validate presence of red_gw as initiator partyin current pmode");
        soft.assertTrue(defaultPmode.contains("<initiatorParty name=\"red_gw\"/>"), "red_gw initiator party is present in pmode");
        log.info("Validate presence of red_gw as responder party in current pmode");
        soft.assertTrue(defaultPmode.contains("<responderParty name=\"red_gw\"/>\n"), "red_gw responder party is present in pmode");
        PModePartiesPage Ppage = new PModePartiesPage(driver);
        PModeCurrentPage Cpage = new PModeCurrentPage(driver);
        log.info("Navigate to Pmode parties page");
        page.getSidebar().gGoToPage(PAGES.PMODE_PARTIES);
        log.info("Find index of row having party name red_gw on Pmode parties page and select row");
        Ppage.grid().selectRow(Ppage.grid().getIndexOf(0, "red_gw"));
        log.info("Click on Edit button");
        Ppage.getEditButton().click();
        PartyModal PMpage = new PartyModal((driver));
        log.info("Validate Ok button is enabled");
        soft.assertTrue(PMpage.getOkButton().isEnabled());
        log.info("Uncheck Initiator & Responder checkbox");
        PMpage.clickIRCheckboxes();
        log.info("Click on Save button");
        Ppage.getSaveButton().click();
        System.out.println(Ppage.getAlertArea().getAlertMessage());
        page.waitForTitle();
        log.info("Naviagte to Pmode Current page");
        page.getSidebar().gGoToPage(PAGES.PMODE_CURRENT);
        String updatedPmode = Apage.getPage().getTextArea().getText();
        log.info("Validate absence of red_gw as Initiator party");
        soft.assertFalse(updatedPmode.contains("<initiatorParty name=\"red_gw\"/>"), "red_gw initiator party is not present in pmode");
        log.info("Validate absence of red_gw as Responder party");
        soft.assertFalse(updatedPmode.contains("<responderParty name=\"red_gw\"/>"), "red_gw responder party is not present in pmode");
        soft.assertAll();
    }

    @Test(description = "PMP-26", groups = {"multiTenancy"})
    public void ResponderInitiatorAddition() throws Exception {
        log.info("upload pmode");
        rest.uploadPMode("pmodes/NoResponderInitiator.xml", null);
        DomibusPage page = new DomibusPage(driver);
        SoftAssert soft = new SoftAssert();
        PModeArchivePage Apage = new PModeArchivePage(driver);
        log.info("Nvaiagte to Pmode current page");
        page.getSidebar().gGoToPage(PAGES.PMODE_CURRENT);
        String defaultPmode = Apage.getPage().getTextArea().getText();
        log.info("Validate absence of red_gw as Initiator party");
        soft.assertFalse(defaultPmode.contains("<initiatorParty name=\"red_gw\"/>"), "red_gw initiator party is present in pmode");
        log.info("Validate absence of red_gw as Responder party");
        soft.assertFalse(defaultPmode.contains("<responderParty name=\"red_gw\"/>\n"), "red_gw responder party is present in pmode");
        PModePartiesPage Ppage = new PModePartiesPage(driver);
        PModeCurrentPage Cpage = new PModeCurrentPage(driver);
        log.info("Naviagte to Pmode Parties page");
        page.getSidebar().gGoToPage(PAGES.PMODE_PARTIES);
        log.info("Find row number for party with nae red_gw and select it");
        Ppage.grid().selectRow(Ppage.grid().getIndexOf(0, "red_gw"));
        log.info("Click on Edit button");
        Ppage.getEditButton().click();
        PartyModal PMpage = new PartyModal((driver));
        log.info("Validate Ok button is enabled");
        soft.assertTrue(PMpage.getOkButton().isEnabled());
        log.info("select checkbox for Initiator & Responder");
        PMpage.clickIRCheckboxes();
        log.info("Click on Save button");
        Ppage.getSaveButton().click();
        log.info(Ppage.getAlertArea().getAlertMessage());
        page.waitForTitle();
        log.info("Naviagte to Pmode current page");
        page.getSidebar().gGoToPage(PAGES.PMODE_CURRENT);
        String updatedPmode = Apage.getPage().getTextArea().getText();
        log.info("Validate presence of red_gw as Initiator party ");
        soft.assertTrue(updatedPmode.contains("<initiatorParty name=\"red_gw\"/>"), "red_gw initiator party is  present in pmode");
        log.info("Validate presence of red_gw as responder party");
        soft.assertTrue(updatedPmode.contains("<responderParty name=\"red_gw\"/>"), "red_gw responder party is  present in pmode");
        soft.assertAll();
    }

    @Test(description = "PMP-28", groups = {"multiTenancy"})
    public void InitiatorResponderRemovalCurrentPmode() throws Exception {
        // String domainName = rest.getDomainNames().get(1);
        log.info("upload Pmode");
        rest.uploadPMode("pmodes/doNothingInvalidRed.xml", null);
        DomibusPage page = new DomibusPage(driver);
        SoftAssert soft = new SoftAssert();
        PModePartiesPage Ppage = new PModePartiesPage(driver);
        PModeCurrentPage PCpage = new PModeCurrentPage(driver);
        log.info("Navigate to Pmode Current page");
        page.getSidebar().gGoToPage(PAGES.PMODE_CURRENT);
        String defaultPmode = Ppage.getPage().getTextArea().getText();
        log.info("Replace initiator from red to green");
        String updatedPmodeInit = defaultPmode.replaceAll("\\t", " ").replaceAll("<initiatorParty name=\"red_gw\"/>", "<initiatorParty name=\"green_gw\"/>");
        log.info("Edit current text");
        PCpage.getTextArea().fill(updatedPmodeInit);
        log.info("Click on save button");
        PCpage.getSaveBtn().click();
        PModeCofirmationModal modal = new PModeCofirmationModal(driver);
        log.info("Enter comment");
        Ppage.getModal().getDescriptionTextArea().fill("Initiator party name is updated");
        log.info("Click on Ok button");
        modal.clickOK();
        log.info("Validate non presence of red_gw");
        soft.assertFalse(Ppage.getPage().getTextArea().getText().contains("<initiatorParty name=\"red_gw\"/>"));
        log.info("navigate to Pmode parties page");
        page.getSidebar().gGoToPage(PAGES.PMODE_PARTIES);
        log.info("Get index of row  with party detail red_gw");
        Ppage.grid().selectRow(Ppage.grid().getIndexOf(0, "red_gw"));
        log.info("Click on Edit button");
        Ppage.getEditButton().click();
        PartyModal PMpage = new PartyModal((driver));
        log.info("Validate initiator checkbox status");
        soft.assertFalse(PMpage.getCheckboxStatus("Initiator"), "Initiator Checkbox is unchecked");
        PMpage.refreshPage();
        PMpage.waitForTitle();
        page.getSidebar().gGoToPage(PAGES.PMODE_CURRENT);
        log.info("Replace responder party name");
        String updatedPmodeRes = updatedPmodeInit.replaceAll("\\t", " ").replaceAll("<responderParty name=\"red_gw\"/>", "<responderParty name=\"green_gw\"/>");
        log.info("Update current pmode");
        Ppage.getPage().getTextArea().fill(updatedPmodeRes);
        log.info("Click on save button");
        Ppage.getPage().getSaveBtn().click();
        log.info("Enetr comment");
        Ppage.getModal().getDescriptionTextArea().fill("Responder party name is updated");
        log.info("click on ok");
        modal.clickOK();
        log.info("Validate non presence of responder red_gw");
        soft.assertFalse(Ppage.getPage().getTextArea().getText().contains("<responderParty name=\"red_gw\"/>"), "red_gw is not present as Responder");
        log.info("Navigate to Pmode parties");
        page.getSidebar().gGoToPage(PAGES.PMODE_PARTIES);
        log.info("Get index of row with party id red_gw");
        Ppage.grid().selectRow(Ppage.grid().getIndexOf(0, "red_gw"));
        log.info("click on Edit button");
        Ppage.getEditButton().click();
        log.info("Validate checkbox status of responder");
        soft.assertFalse(PMpage.getCheckboxStatus("Responder"), "Responder checkbox is unchecked");
        soft.assertAll();
    }

    @Test(description = "PMP-27", groups = {"multiTenancy"})
    public void PartyRemovalCurrentPmode() throws Exception {
        log.info("upload Pmode");
        rest.uploadPMode("pmodes/Edelivery-blue.xml", null);
        DomibusPage page = new DomibusPage(driver);
        SoftAssert soft = new SoftAssert();
        PModePartiesPage Ppage = new PModePartiesPage(driver);
        log.info("Navigate to Pmode Current page");
        log.info("Navigate to Pmode current page");
        page.getSidebar().gGoToPage(PAGES.PMODE_CURRENT);
        String defaultPmode = Ppage.getPage().getTextArea().getText();
        log.info("Validate presence of Domibus-blue");
        soft.assertTrue(defaultPmode.contains("domibus-blue"), "blue_gw partyId is present");
        log.info("Validate presence of Domibus-red");
        soft.assertTrue(defaultPmode.contains("domibus-red"), "red_gw partyId is present");
        String uploadedPmode = Ppage.getPage().getTextArea().getText();
        log.info("Replace domibus-red by domibus-blue");
        String newPmode = uploadedPmode.replaceAll("\\t", " ").replaceAll("domibus-red", "domibus-green");
        page.getSidebar().gGoToPage(PAGES.PMODE_CURRENT);
        log.info("Update new pmode");
        Ppage.getPage().getTextArea().fill(newPmode);
        log.info("Click on save button");
        Ppage.getPage().getSaveBtn().click();
        log.info("Enter comment");
        Ppage.getModal().getDescriptionTextArea().fill("New Pmode is uploaded");
        PModeCofirmationModal modal = new PModeCofirmationModal(driver);
        log.info("Click on Ok button");
        modal.clickOK();
        log.info("Success message :" + page.getAlertArea().getAlertMessage());
        page.waitForTitle();
        soft.assertFalse(newPmode.contains("domibus-red"), "Red party id is not present");
        TestServicePage Tpage = new TestServicePage(driver);
        log.info("Navigate to Test service page");
        page.getSidebar().gGoToPage(PAGES.TEST_SERVICE);
        log.info("Get all options from Responder drop down");
        List<String> options = Tpage.getPartySelector().getOptionsTexts();
        log.info("Validate presence of Domibus-blue");
        soft.assertTrue(options.contains("domibus-blue"), "blue party is present");
        log.info("Validate absence of Domibus-green");
        soft.assertFalse(options.contains("domibus-red"), "Red party is not present");
        soft.assertAll();

    }
}

