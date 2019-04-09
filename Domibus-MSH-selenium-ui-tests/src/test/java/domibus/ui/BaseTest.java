package domibus.ui;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DOMIBUS_PAGES;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;
import pages.login.LoginPage;
import pages.plugin_users.PluginUsersPage;
import rest.DomibusRestClient;
import utils.DriverManager;
import utils.PROPERTIES;
import utils.TestDataProvider;
import utils.customReporter.ExcelTestReporter;
import utils.soap_client.MyMessageSender;

import java.util.HashMap;

/**
 * @author Catalin Comanici
 * @version 4.1
*/

@Listeners(ExcelTestReporter.class)
public class BaseTest {

	public static WebDriver driver;
	public static TestDataProvider data = new TestDataProvider();
	public static DomibusRestClient rest = new DomibusRestClient();
	public static MyMessageSender messageSender = new MyMessageSender();

	protected final Logger log = LoggerFactory.getLogger(this.getClass().getName());


	/**
	 * Starts the browser and navigates to the homepage. This happens once before the test
	 * suite and the browser window is reused for all tests in suite
	 */
	@BeforeClass(alwaysRun = true)
	public void beforeClass(){
		log.info("-------- Starting -------");
		driver = DriverManager.getDriver();
		driver.get(PROPERTIES.UI_BASE_URL);
	}


	/**After the test suite is done we close the browser*/
	@AfterClass(alwaysRun = true)
	@AfterSuite(alwaysRun = true)
	public void afterClassSuite(){
		log.info("-------- Quitting -------");

		try {
			driver.quit();
		} catch (Exception e) {
			log.warn("Closing the driver failed");
			e.printStackTrace();
		}
	}

	/**After each test method page is refreshed and logout is attempted*/
	@AfterMethod(alwaysRun = true)
	protected void logout() throws Exception{
		DomibusPage page = new DomibusPage(driver);

		/*refresh will close any remaining opened modals*/
		page.refreshPage();
		if (page.getSandwichMenu().isLoggedIn()) {
			log.info("Logging out");
			page.getSandwichMenu().logout();
		}
	}

	protected DomibusPage login(HashMap<String, String> user){
		System.out.println("login started");
		LoginPage loginPage = new LoginPage(driver);
		try {
			loginPage.login(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new DomibusPage(driver);
	}

	protected DomibusPage login(String user, String pass){

		HashMap<String, String> userInfo = new HashMap<>();
		userInfo.put("username", user);
		userInfo.put("pass", data.getDefaultTestPass());

		login(userInfo);

		return new DomibusPage(driver);
	}




}
