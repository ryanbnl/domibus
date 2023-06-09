package domibus.ui.ux;

import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.json.JSONObject;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.Alert.AlertPage;
import utils.Gen;
import utils.TestUtils;

import java.util.HashMap;
import java.util.List;

public class AlertPgUXTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.ALERTS);


	// EDELIVERY-7154 - ALRT-42 - Modify no of visible rows
	@Test(description = "ALRT-42", groups = {"multiTenancy", "singleTenancy"})
	public void modifyVisibleColumns() throws Exception {
		SoftAssert soft = new SoftAssert();

		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);

		page.grid().waitForRowsToLoad();
		page.grid().checkModifyVisibleColumns(soft);


		soft.assertAll();

	}

	// EDELIVERY-7154 - ALRT-42 - Modify no of visible rows
	@Test(description = "ALRT-42", groups = {"multiTenancy", "singleTenancy"})
	public void modifyVisibleRows() throws Exception {
		SoftAssert soft = new SoftAssert();

		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);

		page.grid().waitForRowsToLoad();
		page.grid().checkChangeNumberOfRows(soft);


		soft.assertAll();

	}

	/*  ALRT-35 - Delete domain alert as super  */
	@Test(description = "ALRT-35", groups = {"multiTenancy"})
	public void delDomainAlertbySuperAdmin() throws Exception {

		SoftAssert soft = new SoftAssert();

		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		page.filters().getShowDomainCheckbox().click();
		page.filters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		int beforeCount = page.grid().getPagination().getTotalItems();
		if (beforeCount <= 1) {

			new SkipException("no grid data available");
		} else {
			page.deleteAlertAndVerify(0, Boolean.FALSE, soft);
			Reporter.log("Mark One unprocessed alert as processed");
			log.info("Mark One unprocessed alert as processed");
			page.alertsGrid().markAsProcessed(1);
			page.getSaveButton().click();
			new Dialog(driver).confirm();
			page.deleteAlertAndVerify(0, Boolean.TRUE, soft);
		}

		soft.assertAll();

	}

	// EDELIVERY-6960 - ALRT-36 - Delete SUPER alert as super
	@Test(description = "ALRT-36", groups = {"multiTenancy"}, enabled = false)
	public void delSuperAlert() throws Exception {

		SoftAssert soft = new SoftAssert();

		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		page.grid().waitForRowsToLoad();

		int beforeCount = page.grid().getPagination().getTotalItems();
		if (beforeCount <= 1) {

			new SkipException("no grid data available");
		} else {
			page.deleteAlertAndVerify(0, Boolean.FALSE, soft);
			Reporter.log("Mark one unprocessed alert as processed");
			log.info("Mark one unprocessed alert as processed");
			page.alertsGrid().markAsProcessed(1);
			page.getSaveButton().click();
			new Dialog(driver).confirm();
			page.deleteAlertAndVerify(0, Boolean.TRUE, soft);
		}
		soft.assertAll();

	}

	// EDELIVERY-6958 - ALRT-34 - Delete domain alert as admin
// EDELIVERY-8455- jira issue reported for delete operation
	/*  ALRT-34 - Delete domain alert as admin  */
	@Test(description = "ALRT-34", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void delDomainAlertByAdmin() throws Exception {

		SoftAssert soft = new SoftAssert();

		if (data.isMultiDomain()) {
			logout();
			String username = Gen.randomAlphaNumeric(10);
			rest.users().createUser(username, DRoles.ADMIN, data.defaultPass(), null);
			login(username, data.defaultPass());
			Reporter.log(String.format("Created user %s with role %s", username, DRoles.ADMIN));
			log.info(String.format("Created user %s with role %s", username, DRoles.ADMIN));
		}
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		page.grid().waitForRowsToLoad();

		int beforeCount = page.grid().getPagination().getTotalItems();
		if (beforeCount <= 1) {

			new SkipException("no grid data available");
		} else {

			Reporter.log("Delete unprocessed super alerts");
			log.info("Delete unprocessed super alerts");
			page.deleteAlertAndVerify(0, Boolean.FALSE, soft);
			Reporter.log("Mark one unprocessed alert as processed");
			log.info("Mark one unprocessed alert as processed");
			page.alertsGrid().markAsProcessed(1);
			page.getSaveButton().click();
			new Dialog(driver).confirm();

			page.deleteAlertAndVerify(0, Boolean.TRUE, soft);
			soft.assertAll();
		}
	}

	// EDELIVERY-7148 - ALRT-43 - Super admin marks domain alert as processed
	@Test(description = "ALRT-43", groups = {"multiTenancy", "singleTenancy"})
	public void markDomainAlrtProcessed() throws Exception {

		SoftAssert soft = new SoftAssert();
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		if (data.isMultiDomain()) {
			page.filters().getShowDomainCheckbox().click();
			page.filters().getSearchButton().click();
		}
		page.grid().waitForRowsToLoad();

		Reporter.log("Mark one unprocessed alert as processed and save");
		log.info("Mark one unprocessed alert as processed and save");

		page.processAlertAndVerify(soft, Boolean.TRUE);
		Reporter.log("index" + page.grid().getSelectedRowIndex());
		log.info("index" + page.grid().getSelectedRowIndex());
		soft.assertTrue(page.grid().getSelectedRowIndex() < 0, "Row is still not present");
		soft.assertFalse(page.getSaveButton().isEnabled(), "Save button is not enabled");
		soft.assertFalse(page.getCancelButton().isEnabled(), "Cancel button is not enabled");

		Reporter.log("Mark one unprocessed alert as processed and cancel");
		log.info("Mark one unprocessed alert as processed and cancel");
		page.processAlertAndVerify(soft, Boolean.FALSE);
		soft.assertTrue(page.grid().getSelectedRowIndex() > 0, "Row is still present");

		soft.assertTrue(page.getSaveButton().isEnabled(), "Save button is enabled");
		soft.assertTrue(page.getCancelButton().isEnabled(), "Cancel button is enabled");
		soft.assertAll();

	}

	// EDELIVERY-7150 - ALRT-38 - Super admin views domain alerts and changes domain
	/*  ALRT-38 - Super admin views domain alerts and changes domain  */
	@Test(description = "ALRT-38", groups = {"multiTenancy"})
	public void changeDomain() throws Exception {

		SoftAssert soft = new SoftAssert();
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);

		Reporter.log("showing domain alerts");
		log.info("showing domain alerts");
		page.filters().getShowDomainCheckbox().click();
		page.filters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		Reporter.log("Getting all listed message info");
		log.info("Getting all listed message info");
		List<HashMap<String, String>> dom1RowInfo = page.grid().getListedRowInfo();

		if (dom1RowInfo.size() == 0) {
			throw new SkipException("not enough alerts to perform test");
		}

		Reporter.log("changing domain");
		log.info("changing domain");
		page.getDomainSelector().selectAnotherDomain();

		Reporter.log("showing domain alerts");
		log.info("showing domain alerts");
		page.filters().getShowDomainCheckbox().click();
		page.filters().getSearchButton().click();
		page.grid().waitForRowsToLoad();

		List<HashMap<String, String>> dom2rowInfo = page.grid().getListedRowInfo();

		soft.assertFalse(TestUtils.isEqualMapListContent(dom1RowInfo, dom2rowInfo), "Listed info for the 2 domains is different");

		soft.assertAll();

	}

	// EDELIVERY-7149 - ALRT-37 - Super admin filters super alerts
	@Test(description = "ALRT-37", groups = {"multiTenancy"})
	public void filterSuperAlert() throws Exception {
		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);

		SoftAssert soft = new SoftAssert();
		page.grid().waitForRowsToLoad();

		int noOfAlerts = page.grid().getPagination().getTotalItems();
		Reporter.log("Total number of records : " + noOfAlerts);
		log.info("Total number of records : " + noOfAlerts);

		if (noOfAlerts < 3) {
			throw new SkipException("not enough alerts to test");
		}

		Reporter.log("Getting alert info for first row");
		log.info("Getting alert info for first row");

		HashMap<String, String> firstRowAlert = page.grid().getRowInfo(0);

		Reporter.log("Search on the basis of first row data " + firstRowAlert);
		log.info("Search on the basis of first row data " + firstRowAlert);
		page.filters().basicFilterBy(null, firstRowAlert.get("Alert Type"), firstRowAlert.get("Alert Status"),
				firstRowAlert.get("Alert level"), firstRowAlert.get("Creation Time"), null);

		page.grid().waitForRowsToLoad();
		soft.assertFalse(page.filters().getShowDomainCheckbox().isChecked(), "Show domain checkbox is unchecked after search");

		List<HashMap<String, String>> allResultInfo = page.grid().getAllRowInfo();

		soft.assertTrue(allResultInfo.size() >= 1, "Grid has search result present");

		for (HashMap<String, String> currentAlert : allResultInfo) {
			soft.assertEquals(currentAlert.get("Alert Type"), firstRowAlert.get("Alert Type"), "Both have same value for Alert Type");
			soft.assertEquals(currentAlert.get("Alert Status"), firstRowAlert.get("Alert Status"), "Both have same value for Alert Status");
			soft.assertEquals(currentAlert.get("Alert level"), firstRowAlert.get("Alert level"), "Both have same value for Alert Level");
		}

		soft.assertAll();
	}

	// EDELIVERY-7151 - ALRT-39 - Super admin filters domain alerts
	/*  ALRT-39 - Super admin filters domain alerts  */
	@Test(description = "ALRT-39", groups = {"multiTenancy"})
	public void filterDomainAlert() throws Exception {
		SoftAssert soft = new SoftAssert();

		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);
		page.filters().getShowDomainCheckbox().click();
		page.filters().getSearchButton().click();

		page.grid().waitForRowsToLoad();

		Reporter.log("Total number of records : " + page.grid().getPagination().getTotalItems());
		log.info("Total number of records : " + page.grid().getPagination().getTotalItems());
		Reporter.log("Getting alert info for first row");
		log.info("Getting alert info for first row");

		HashMap<String, String> firstRowAlert = page.grid().getRowInfo(0);

		Reporter.log("Search on the basis of first row data " + firstRowAlert);
		log.info("Search on the basis of first row data " + firstRowAlert);
		page.filters().basicFilterBy(null, firstRowAlert.get("Alert Type"), firstRowAlert.get("Alert Status"),
				firstRowAlert.get("Alert level"), firstRowAlert.get("Creation Time"), null);

		page.grid().waitForRowsToLoad();
		soft.assertTrue(page.filters().getShowDomainCheckbox().isChecked(), "Show domain checkbox is checked after search");

		List<HashMap<String, String>> allResultInfo = page.grid().getAllRowInfo();

		soft.assertTrue(allResultInfo.size() >= 1, "Grid has search result present");

		for (HashMap<String, String> currentAlert : allResultInfo) {
			soft.assertEquals(currentAlert.get("Alert Type"), firstRowAlert.get("Alert Type"), "Both have same value for Alert Type");
			soft.assertEquals(currentAlert.get("Alert Status"), firstRowAlert.get("Alert Status"), "Both have same value for Alert Status");
			soft.assertEquals(currentAlert.get("Alert level"), firstRowAlert.get("Alert level"), "Both have same value for Alert Level");
		}

		soft.assertAll();
	}


	/*EDELIVERY-8216 - ALRT-54 - Check Ui when Plugin Alert is selected in search filter*/
	@Test(description = "ALRT-54", groups = {"multiTenancy", "singleTenancy"})
	public void selectPluginTypeAlert() throws Exception {
		SoftAssert soft = new SoftAssert();

		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);

		page.filters().getAlertTypeSelect().selectOptionByText("Plugin");

		soft.assertFalse(page.getAlertArea().isShown(), "Alert area is not displayed");
		soft.assertTrue(page.grid().isPresent(), "Grid is displayed");

		soft.assertAll();
	}


}

