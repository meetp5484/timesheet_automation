package pages;

import config.ConfigManager;
import config.DriverFactory;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ExcelUtility;

import java.time.Duration;
import java.util.ArrayList;

import static config.XPath.*;
import static utils.CommonUtils.logInfo;

public class LoginPage {
    private final WebDriver driver = DriverFactory.getDriver();
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

    public void userOnLoginPage() throws Exception {
        driver.get(ConfigManager.get("baseUrl"));

    }

    public void enterCedetials() throws InterruptedException {

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(USERNAME_FIELD)));
        driver.findElement(By.xpath(USERNAME_FIELD)).sendKeys(ConfigManager.get("UserName"));
        driver.findElement(By.xpath(SIGNIN_NEXT_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(PASSWORD_FIELD)));
        driver.findElement(By.xpath(PASSWORD_FIELD)).sendKeys(ConfigManager.get("Password"));
    }


    public void clickOnMicrosoftLoginButton() throws InterruptedException {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(MICROSOFT_BUTTON)));
        driver.findElement(By.xpath(MICROSOFT_BUTTON)).click();
        logInfo("Clicked on Microsoft login button");
    }

    public void clickOnLoginButton() {
        driver.findElement(By.xpath(SIGNIN_NEXT_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(NO_SELECT_STAY_SIGNED_IN)));
        driver.findElement(By.xpath(NO_SELECT_STAY_SIGNED_IN)).click();
    }

    public void userOnDashboardPage() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(DASHBOARD_LOGO)));
    }
}
