package pages.Audit;

import ddsl.dcomponents.FilterArea;
import ddsl.dobjects.Checkbox;
import ddsl.dobjects.DatePicker;
import ddsl.dobjects.multi_select.MultiSelect;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.Date;

public class AuditFilters extends FilterArea {
    @FindBy(css = "#table_id")
    public WebElement tableFilterContainer;
    @FindBy(css = "#user_id:nth-of-type(2)")
    public WebElement userFilterContainer;
    @FindBy(css = "#action_id:nth-of-type(3)")
    public WebElement actionFilterContainer;
    @FindBy(css = "#from_id")
    public WebElement changedFrom;
    @FindBy(css = "#to_id")
    public WebElement changedTo;

    @FindBy(css = "#isDomain_id")
    public WebElement isDomainChk;

    public AuditFilters(WebDriver driver) {
        super(driver);
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
    }

    public MultiSelect getTableFilter() {
        return weToMultiSelect(tableFilterContainer);
    }

    public MultiSelect getUserFilter() {
        return weToMultiSelect(userFilterContainer);
    }

    public MultiSelect getActionFilter() {
        return weToMultiSelect(actionFilterContainer);
    }

    public DatePicker getChangedFrom() {
        return weToDatePicker(changedFrom);
    }

    public DatePicker getChangedTo() {
        return weToDatePicker(changedTo);
    }

    public Checkbox getIsDomainChk() {
        return weToCheckbox(isDomainChk);
    }

    public void simpleFilter(String table, String user, String action, String changeFrom, String changeTo) throws Exception {
        log.debug("table = " + table);
        log.debug("user = " + user);
        log.debug("action = " + action);
        log.debug("changeFrom = " + changeFrom);
        log.debug("changeTo = " + changeTo);

        getTableFilter().selectOptionByText(table);
        getUserFilter().selectOptionByText(user);
        getActionFilter().selectOptionByText(action);

        if (!StringUtils.isEmpty(changeFrom)) {
            expandArea();
            getChangedFrom().selectDate(changeFrom);
        }
        if (!StringUtils.isEmpty(changeTo)) {
            expandArea();
            getChangedTo().selectDate(changeTo);
        }

        clickSearch();
    }


    public void advancedFilter(String table, String user, String action, Date changeFrom, Date changeTo) throws Exception {
        log.debug("table = " + table);
        log.debug("user = " + user);
        log.debug("action = " + action);
        log.debug("changeFrom = " + changeFrom);
        log.debug("changeTo = " + changeTo);

        getTableFilter().selectOptionByText(table);
        getUserFilter().selectOptionByText(user);
        getActionFilter().selectOptionByText(action);

        expandArea();

        if (changeFrom != null) {
            getChangedFrom().selectDate(changeFrom);
        }
        if (changeTo != null) {
            getChangedTo().selectDate(changeTo);
        }

        clickSearch();
    }


}
