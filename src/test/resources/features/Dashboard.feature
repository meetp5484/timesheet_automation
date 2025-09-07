Feature: Dashboard Testcase

  Background: Login to Timesheet Application
    Given User is on login page
    And Click on Microsoft login button
    And Enter User name and password
    Then Click on Login Button
    And Verify User On Dashboard Page

    Scenario: enter data into timesheet
        Given User on Dashboard Page
        When Read excelsheet and fill the timesheet