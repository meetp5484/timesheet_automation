package stepdefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import pages.DashboardPage;

import java.io.IOException;

public class DashboardStep {

    private DashboardPage dashboardPage;

    public DashboardStep() {
        this.dashboardPage = new DashboardPage();
    }

    @Given("User on Dashboard Page")
    public void userOnDashboardPage() {
        dashboardPage.theUserOnDashboardPage();
    }

    @When("Read excelsheet and fill the timesheet")
    public void readExcelSheetAndFillTheTimesheet() throws Exception {
        dashboardPage.readExcelSheetAndFillTheTimesheet();
    }
}
