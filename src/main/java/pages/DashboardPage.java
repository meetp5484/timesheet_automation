package pages;

import config.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ExcelUtility;

import java.time.Duration;
import java.util.*;

import static config.XPath.*;
import static utils.CommonUtils.logInfo;

public class DashboardPage {
    private final WebDriver driver = DriverFactory.getDriver();
    ArrayList<String> allDates;
    List<String> caseIds;
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));


    public void theUserOnDashboardPage() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(DASHBOARD_LOGO)));
        logInfo("User on Dashboard Page");
    }


    public void readExcelSheetAndFillTheTimesheet() throws Exception {
        allDates = ExcelUtility.getDatesFromExcel();
        logInfo("Extracted Dates from Excel: " + allDates);
        ExcelUtility.openDashboardFillData(driver, allDates);
    }
}