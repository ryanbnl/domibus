package ddsl.dobjects;

import ddsl.dcomponents.DComponent;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Select extends DComponent {


    public WebElement selectContainer;
    protected List<String> optionIDs = new ArrayList<String>();

    @FindBy(css = "[class*=\"select-arrow\"]")
    protected WebElement expandBtn;

    private By options = By.cssSelector(".mat-select-panel > mat-option");
    private By selectedOption = By.cssSelector("[class*=\"-select-value\"]");

    public Select(WebDriver driver, WebElement container) {
        super(driver);
        log.debug("initialize select");
        wait.forXMillis(100);
        PageFactory.initElements(new AjaxElementLocatorFactory(container, data.getTIMEOUT()), this);

        this.selectContainer = container;
        try {
            if (isDisplayed() && isEnabled()) {
                expand();
                extractOptionIDs();
                contract();
            }
        } catch (Exception e) {
        }
    }

    public boolean isDisplayed() throws Exception {
        try {
            return weToDButton(expandBtn).isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isEnabled() {
//		aria-disabled="true"
        boolean isEnabled = true;
        try {
            isEnabled = !Boolean.valueOf(weToDobject(selectContainer).getAttribute("aria-disabled"));
        } catch (Exception e) {
        }

        return isEnabled;
    }

    public String getSelectedValue() throws Exception {
        try {
            return this.selectContainer.findElement(selectedOption).getText();
        } catch (Exception e) {
            log.debug("Could not read selected value ", e);
        }
        return null;
    }

    private void extractOptionIDs() throws Exception {
        wait.forAttributeToContain(selectContainer, "aria-owns", "mat-option");
        String[] ids = selectContainer.getAttribute("aria-owns").split(" ");
        optionIDs.addAll(Arrays.asList(ids));

        log.debug(optionIDs.size() + " option ids identified : " + optionIDs);
    }

    private void expand() throws Exception {
        try {
            weToDButton(expandBtn).click();
            wait.forAttributeNotEmpty(selectContainer, "aria-owns");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void contract() throws Exception {
        try {
            wait.forXMillis(200);
            selectContainer.sendKeys(Keys.ESCAPE);
            wait.forXMillis(200);

        } catch (Exception e) {
        }
    }

//	-----------------------------------------------------------------

    protected List<DObject> getOptionElements() throws Exception {
        expand();

        List<DObject> optionObj = new ArrayList<>();

        for (int i = 0; i < optionIDs.size(); i++) {
            String optionId = optionIDs.get(i);
            WebElement option = driver.findElement(By.id(optionId));
            optionObj.add(new DObject(driver, option));
        }
        return optionObj;
    }

    public boolean selectOptionByText(String text) throws Exception {
        log.debug("selecting option by text: " + text);

        if (StringUtils.isEmpty(text)) {
            return false;
        }

        List<DObject> optionObj = getOptionElements();
        if (optionObj.size() == 0) {
            log.warn("select has no options ");
        }

        for (DObject dObject : optionObj) {
            if (StringUtils.equalsIgnoreCase(dObject.getText(), text)) {
                dObject.click();
                return true;
            }
        }

        return false;
    }

    public boolean selectOptionByIndex(int index) throws Exception {
        if (index >= optionIDs.size() || index < 0) {
            throw new Exception(String.format("Could not select, index is %s and we got %s options", index, optionIDs.size()));
        }

        log.info("selectOptionByIndex : " + index);
        getOptionElements().get(index).click();
        return true;
    }

    public List<String> getOptionsTexts() throws Exception {
        List<String> texts = new ArrayList<>();
        List<DObject> options = getOptionElements();

        for (int i = 0; i < options.size(); i++) {
            texts.add(options.get(i).getText());
        }
        contract();
        return texts;
    }


}
