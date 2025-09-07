package stepdefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import pages.LoginPage;

public class LoginSteps {
    private LoginPage loginPage;

    public LoginSteps() {
        this.loginPage = new LoginPage();
    }

    @Given("User is on login page")
    public void user_is_on_login_page() throws Exception {
        loginPage.userOnLoginPage();
    }

    @And("Enter User name and password")
    public void enterUserNameAndPassword() throws InterruptedException {
        loginPage.enterCedetials();
    }

    @And("Click on Microsoft login button")
    public void clickOnMicrosoftLoginButton() throws InterruptedException {
        loginPage.clickOnMicrosoftLoginButton();
    }

    @Then("Click on Login Button")
    public void clickOnLoginButton() {
        loginPage.clickOnLoginButton();
    }

    @And("Verify User On Dashboard Page")
    public void verifyUserOnDashboardPage() {
        loginPage.userOnDashboardPage();
    }
}
