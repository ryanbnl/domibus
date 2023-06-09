package pages.messages;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DButton;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.DFileUtils;

import java.io.File;


public class MessagesPage extends DomibusPage {


	@FindBy(id = "pageGridId")
	public WebElement gridContainer;
	@FindBy(id = "downloadbutton_id")
	public WebElement downloadBtn;
	@FindBy(id = "resendbutton_id")
	public WebElement resendBtn;
	@FindBy(css = "#receivedto_id > div >button")
	public WebElement receivedToClock;
	@FindBy(css = "#receivedto_id")
	public WebElement receivedTo;

	public MessagesPage(WebDriver driver) {
		super(driver);
		log.debug("Messages page init");
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);

	}

	public DGrid grid() {
		return new DGrid(driver, gridContainer);
	}

	public DButton getDownloadButton() {
		return new DButton(driver, downloadBtn);
	}

	public DButton getResendButton() {
		return new DButton(driver, resendBtn);
	}

	public MessageFilterArea getFilters() {
		return new MessageFilterArea(driver);
	}

	public boolean isLoaded() {
		return (getDownloadButton().isPresent()
				&& getResendButton().isPresent()
				&& null != grid()
				&& null != getFilters());
	}

	public String getCssofRowSpecificActionIcon(int rowNumber, String iconName) {
		if (iconName.equals("Download")) {
			return "#downloadButtonRow" + rowNumber + "_id";
		}
		if (iconName.equals("Resend")) {
			return "#resendButtonRow" + rowNumber + "_id";
		}
		if (iconName.equals("downloadEnvelopes")) {
			return "#downloadEnvelopesButtonRow" + rowNumber + "_id";
		}
		return "";
	}

	public Boolean getActionIconStatus(int rowNumber, String iconName) {
		WebElement iconElement = driver.findElement(By.cssSelector(getCssofRowSpecificActionIcon(rowNumber, iconName)));
		wait.forElementToBeVisible(iconElement);
		return iconElement.isEnabled();
	}

	public Boolean isActionIconPresent(int rowNumber, String iconName) {
		try {
			driver.findElement(By.cssSelector(getCssofRowSpecificActionIcon(rowNumber, iconName)));
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public String downloadMessage(int rowNo) throws Exception {
		log.info("Customized location for download");
		String filePath = data.downloadFolderPath();

		log.info("Clean given directory");
		FileUtils.cleanDirectory(new File(filePath));

		log.info("selecting row");
		grid().selectRow(rowNo);

		log.info("Click on download button");
		getDownloadButton().click();

		log.info("Wait for download to complete");

		wait.forFileToBeDownloaded(filePath);

		log.info("Check if file is downloaded at given location");
		if (!DFileUtils.isFileDownloaded(filePath)) {
			throw new Exception("Could not find file");
		}

		return DFileUtils.getCompleteFileName(data.downloadFolderPath());
	}

	public String downloadMessageEnvelop(int rowNo) throws Exception {
		log.info("Customized location for download");
		String filePath = data.downloadFolderPath();

		log.info("Clean given directory");
		FileUtils.cleanDirectory(new File(filePath));

		log.info("Click on download Message Envelop button");
		WebElement iconElement = driver.findElement(By.cssSelector(getCssofRowSpecificActionIcon(rowNo, "downloadEnvelopes")));
		iconElement.click();

		log.info("Wait for Message envelop download to complete");

		wait.forFileToBeDownloaded(filePath);

		log.info("Check if file is downloaded at given location");
		if (!DFileUtils.isFileDownloaded(filePath)) {
			throw new Exception("Could not find file");
		}

		return DFileUtils.getCompleteFileName(data.downloadFolderPath());
	}


}
