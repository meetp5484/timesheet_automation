package utils;

import config.ConfigManager;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static config.XPath.*;
import static utils.CommonUtils.logInfo;

public class ExcelUtility {
    private static final String FILE_PATH = ConfigManager.get("excelFilePath");
    private static final String SHEET_NAME = "Sheet1";

    public static ArrayList<String> getDatesFromExcel() throws Exception {
        ArrayList<String> dateList = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // Change format if needed

        FileInputStream file = new FileInputStream(new File(FILE_PATH));
        Workbook workbook = WorkbookFactory.create(file);
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Start from row 2 (index 1)
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell cell = row.getCell(0); // First column
                if (cell != null) {
                    String dateValue = null;
                    if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                        dateValue = sdf.format(cell.getDateCellValue());
                    } else {
                        // If not date formatted, read as string
                        cell.setCellType(CellType.STRING);
                        dateValue = cell.getStringCellValue().trim();
                    }
                    if (dateValue != null && !dateValue.isEmpty() && seen.add(dateValue)) {
                        dateList.add(dateValue);
                    }
                }
            }
        }

        workbook.close();
        file.close();
        return dateList;
    }

    public static void openDashboardFillData(WebDriver driver, ArrayList<String> dates) throws InterruptedException, IOException {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter xpathFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        for (String dateStr : dates) {
            try {
                String xpathDate = LocalDate.parse(dateStr, inputFormatter).format(xpathFormatter);
                punchInMissingPopUp(driver);
                if (!trySelectDateCell(driver, xpathDate, dateStr)) {
                    continue;
                }
                boolean punchPopupPresent = isPunchPopupPresent(driver);
                int[] dateRange = getRowRangeForDate(dateStr);
                if (dateRange == null) {
                    logInfo("Date not found: " + dateStr);
                    continue;
                }
                logInfo("Date range: " + dateRange[0] + " -> " + dateRange[1]);
                int startRow = dateRange[0];
                int endRow = dateRange[1];
                int totalRows = endRow - (startRow - 1);
                logInfo("Total rows to fill for date " + dateStr + ": " + totalRows);
                fillRowsForDate(driver, startRow, endRow, punchPopupPresent);
            } catch (Exception e) {
                logInfo("Could not open date: " + dateStr + " | Error: " + e.getMessage());
            }
            Thread.sleep(2000);
        }
    }

    // Helper: Try to select/click the date cell, handle disabled and navigation, return true if date is clickable, false if disabled or not found
    private static boolean trySelectDateCell(WebDriver driver, String xpathDate, String dateStr) throws IOException, InterruptedException {
        List<WebElement> dateCells = driver.findElements(By.xpath("//td[@data-value='" + xpathDate + "']"));
        if (!dateCells.isEmpty() && dateCells.get(0).isDisplayed()) {
            WebElement dateElement = dateCells.get(0);
            String cellClass = dateElement.getAttribute("class");
            if (!cellClass.contains("dx-calendar-empty-cell")) {
                dateElement.click();
                logInfo("Clicked on date: " + xpathDate);
                logInfo("Filling entries for date: " + xpathDate);
                return true;
            } else {
                logInfo("Date " + xpathDate + " is disabled. Cannot click.");
                writeDateDisabledToExcel(dateStr);
                return false;
            }
        } else {
            int maxAttempts = 12;
            boolean found = false;
            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                String tempSelectedFullDate = driver.findElement(By.cssSelector(SELECTED_DATE)).getAttribute("data-value");
                logInfo("Current calendar selected date: " + tempSelectedFullDate);
                LocalDate target = LocalDate.parse(xpathDate, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                LocalDate selected = LocalDate.parse(tempSelectedFullDate, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                if (target.isBefore(selected)) {
                    driver.findElement(By.xpath(NAVIGATE_PREVIOUS)).click();
                    logInfo("Date not found so clicked Previous button");
                } else if (target.isAfter(selected)) {
                    driver.findElement(By.xpath(NAVIGATE_NEXT)).click();
                    logInfo("Date not found so clicked next button");

                } else {
                    break;
                }
                Thread.sleep(500);
                dateCells = driver.findElements(By.xpath("//td[@data-value='" + xpathDate + "']"));
                if (!dateCells.isEmpty() && dateCells.get(0).isDisplayed()) {
                    WebElement dateElement = dateCells.get(0);
                    String cellClass = dateElement.getAttribute("class");
                    if (!cellClass.contains("dx-calendar-empty-cell")) {
                        dateElement.click();
                        found = true;
                        logInfo("Clicked on date: " + xpathDate);
                        logInfo("Filling entries for date: " + xpathDate);
                        break;
                    } else {
                        logInfo("Date " + xpathDate + " is disabled. Cannot click.");
                        writeDateDisabledToExcel(dateStr);
                        return false;
                    }
                }
            }
            if (!found) {
                logInfo("Date not found in calendar after navigating: " + dateStr);
                return false;
            }
            return true;
        }
    }

    // Helper: Write 'Date is disabled. Cannot click.' to Excel for the first row of this date
    private static void writeDateDisabledToExcel(String dateStr) throws IOException {
        int[] dateRange = getRowRangeForDate(dateStr);
        if (dateRange != null) {
            FileInputStream fis = new FileInputStream(FILE_PATH);
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheet(SHEET_NAME);
            int firstRow = dateRange[0] - 1;
            Row row = sheet.getRow(firstRow);
            if (row != null) {
                Cell statusCell = row.getCell(8);
                if (statusCell == null) statusCell = row.createCell(8);
                statusCell.setCellValue("Date is disabled. Cannot click.");
                FileOutputStream fos = new FileOutputStream(FILE_PATH);
                workbook.write(fos);
                fos.close();
            }
            workbook.close();
            fis.close();
        }
    }

    // Helper: Check if punch-in missing popup is present
    private static boolean isPunchPopupPresent(WebDriver driver) {
        try {
            Thread.sleep(2000);
            return driver.findElement(By.xpath(POPUP_TEXT)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // Helper: Fill all rows for a date
    private static void fillRowsForDate(WebDriver driver, int startRow, int endRow, boolean punchPopupPresent) throws IOException, InterruptedException {
        FileInputStream fis = new FileInputStream(FILE_PATH);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheet(SHEET_NAME);
        boolean punchChecked = false;
        String punchMissTypeFirstRow = "";
        String lastProjectName = "";
        for (int rowNum = startRow - 1; rowNum < endRow; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) continue;
            // Only check punch-in missing for the first row of the date, and only if popup is present
            if (!punchChecked && punchPopupPresent) {
                Cell punchMissTypeCell = row.getCell(6);
                Cell punchReasonCell = row.getCell(7);
                String punchMissType = (punchMissTypeCell != null && punchMissTypeCell.getCellType() != CellType.BLANK) ? punchMissTypeCell.toString().trim().toLowerCase() : "";
                String punchReason = (punchReasonCell != null && punchReasonCell.getCellType() != CellType.BLANK) ? punchReasonCell.toString().trim().toLowerCase() : "";
                punchMissTypeFirstRow = punchMissType;
                boolean missingType = punchMissType.isEmpty();
                boolean missingReason = punchReason.isEmpty();
                if (missingType || missingReason) {
                    // Close popup
                    try {
                        WebElement cancelBtn = driver.findElement(By.xpath(POPUP_CANCEL));
                        if (cancelBtn.isDisplayed()) {
                            cancelBtn.click();
                            Thread.sleep(500);
                        }
                    } catch (Exception e) { /* ignore */ }
                    // Write missing messages
                    if (missingType && missingReason) {
                        Cell iCell = row.getCell(8);
                        if (iCell == null) iCell = row.createCell(8);
                        iCell.setCellValue("punching type is missing & punching reason is missing");
                    } else {
                        if (missingType) {
                            Cell gCell = row.getCell(8);
                            if (gCell == null) gCell = row.createCell(8);
                            gCell.setCellValue("missing punch type");
                        }
                        if (missingReason) {
                            Cell hCell = row.getCell(8);
                            if (hCell == null) hCell = row.createCell(8);
                            hCell.setCellValue("missing punch reason");
                        }
                    }
                    FileOutputStream fos = new FileOutputStream(FILE_PATH);
                    row.getSheet().getWorkbook().write(fos);
                    fos.close();
                    punchChecked = true;
                    continue;
                } else {
                    punchInMissing(driver, punchMissType, punchReason);
                }
                punchChecked = true;
            }
            // If punchMissType is 'leave', skip project/job/etc. checks for all rows of this date
            if (!punchMissTypeFirstRow.isEmpty() && punchMissTypeFirstRow.equals("leave")) {
                continue;
            }
            // Project name logic: copy from above if empty
            String project = "";
            Cell projectCell = row.getCell(1);
            if (projectCell != null && !projectCell.toString().trim().isEmpty()) {
                project = projectCell.toString().trim();
                lastProjectName = project;
            } else {
                project = lastProjectName;
            }
            // Job name as is
            String job = (row.getCell(2) != null) ? row.getCell(2).toString().trim() : "";
            String workItem = (row.getCell(3) != null) ? row.getCell(3).toString().trim() : "";
            String description = (row.getCell(4) != null) ? row.getCell(4).toString().trim() : "";
            String hoursStr = (row.getCell(5) != null) ? row.getCell(5).toString().trim() : "";
            logInfo("  Adding row " + (rowNum - (startRow - 2)) + " for project " + project);
            Thread.sleep(1000);
            // Check total hours before any UI actions
            WebElement totalHoursElement = driver.findElement(By.xpath(EXISTING_HOURS));
            double existingHours = Double.parseDouble(totalHoursElement.getText().split(":")[1].trim());
            double rowHours = hoursStr.isEmpty() ? 0 : Double.parseDouble(hoursStr);
            if (rowHours + existingHours > 9) {
                logInfo("Cannot save: total hours exceed daily limit! Skipping row before filling project/job.");
                // Optionally, write to Excel status column
                Cell statusCell = row.getCell(6);
                if (statusCell == null) {
                    statusCell = row.createCell(6);
                }
                statusCell.setCellValue("Total hours exceed daily limit");
                FileOutputStream fos = new FileOutputStream(FILE_PATH);
                workbook.write(fos);
                fos.close();
                continue;
            }
            // 1. Click "Add Row" button
            WebElement addRowBtn = driver.findElement(By.xpath(ADD_ROW));
            addRowBtn.click();
            Thread.sleep(1000); // wait for row to be added
            // After clicking Add Row, if popup appears, click Cancel
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
                WebElement popup = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(POPUP_TEXT)));
                if (popup.isDisplayed()) {
                    WebElement cancelBtn = driver.findElement(By.xpath(POPUP_CANCEL));
                    if (cancelBtn.isDisplayed()) {
                        cancelBtn.click();
                        Thread.sleep(500);
                    }
                }
            } catch (Exception e) {
                // Popup not present, continue
            }
            // 2. Fill project name
            if (!project.isEmpty() && !selectFromDropdown(driver, project, PROJECT_NAME)) {
                logInfo("Skipping row because project not found: " + project);
                // Write 'Project not found' in column I (index 8)
                Cell statusCell = row.getCell(8);
                if (statusCell == null) {
                    statusCell = row.createCell(8);
                }
                statusCell.setCellValue("Project not found");
                // Save workbook after writing
                FileOutputStream fos = new FileOutputStream(FILE_PATH);
                workbook.write(fos);
                fos.close();
                continue; // skip this row only
            }
            // 3. Fill job name
            if (!job.isEmpty() && !selectFromDropdown(driver, job, JOB_NAME)) {
                logInfo("Skipping job because not found: " + job);
                // Write 'Job not found' in column I (index 8)
                Cell statusCell = row.getCell(8);
                if (statusCell == null) {
                    statusCell = row.createCell(8);
                }
                statusCell.setCellValue("Job not found");
                // Save workbook after writing
                FileOutputStream fos = new FileOutputStream(FILE_PATH);
                workbook.write(fos);
                fos.close();
                continue; // skip this row only
            }
            // 4. Fill work item, description, hours from Excel
            if (workItem == null || workItem.isEmpty()) {
                logInfo("Skipping row because work item is missing");
                // Write 'work items is missing' in column I (index 8)
                Cell statusCell = row.getCell(8);
                if (statusCell == null) {
                    statusCell = row.createCell(8);
                }
                statusCell.setCellValue("work items is missing");
                // Save workbook after writing
                FileOutputStream fos = new FileOutputStream(FILE_PATH);
                workbook.write(fos);
                fos.close();
                continue; // skip this row only
            }
            if (description == null || description.isEmpty()) {
                logInfo("Skipping row because description is missing");
                // Write 'Description is missing' in column I (index 8)
                Cell statusCell = row.getCell(8);
                if (statusCell == null) {
                    statusCell = row.createCell(8);
                }
                statusCell.setCellValue("Description is missing");
                // Save workbook after writing
                FileOutputStream fos = new FileOutputStream(FILE_PATH);
                workbook.write(fos);
                fos.close();
                continue; // skip this row only
            }
            if (hoursStr == null || hoursStr.isEmpty()) {
                logInfo("Skipping row because hours is missing");
                // Write 'hours is missing' in column I (index 8)
                Cell statusCell = row.getCell(8);
                if (statusCell == null) {
                    statusCell = row.createCell(8);
                }
                statusCell.setCellValue("hours is missing");
                // Save workbook after writing
                FileOutputStream fos = new FileOutputStream(FILE_PATH);
                workbook.write(fos);
                fos.close();
                continue; // skip this row only
            }
            WebElement workItemInput = driver.findElement(By.xpath(WORK_ITEM));
            workItemInput.clear();
            workItemInput.sendKeys(workItem);
            WebElement descriptionInput = driver.findElement(By.xpath(DESCRIPTION));
            descriptionInput.clear();
            descriptionInput.sendKeys(description);
            WebElement hoursInput = driver.findElement(By.xpath(HOURS));
            hoursInput.clear();
            hoursInput.sendKeys(hoursStr);
            // Now save as before
            logInfo("Total hours OK. Clicking Save...");
            WebElement saveBtn = driver.findElement(By.xpath(SAVE));
            saveBtn.click();
        }
        workbook.close();
        fis.close();
    }


    // Method 1: Get row range for a given date (already discussed)
    public static int[] getRowRangeForDate(String dateStr) throws IOException {
        FileInputStream fis = new FileInputStream(FILE_PATH);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheet(SHEET_NAME);

        int startRow = -1;
        int endRow = -1;
        int lastRow = sheet.getLastRowNum();

        // Start from row 1 (second row in Excel) instead of 0
        for (int i = 1; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            Cell cell = row.getCell(0); // Check first column
            if (cell == null) continue;

            String cellValue = cell.toString().trim();

            if (cellValue.equals(dateStr)) {
                if (startRow == -1) startRow = i + 1; // Add 1 for Excel-style row number
            } else if (startRow != -1 && !cellValue.isEmpty()) {
                endRow = i; // End before the next date
                break;
            }
        }

        if (startRow != -1 && endRow == -1) {
            // For the last date, set endRow to the last row in the sheet that has a non-empty job name (3rd column, index 2)
            int lastJobRow = -1;
            for (int i = startRow - 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Cell jobCell = row.getCell(2); // 3rd column (index 2)
                if (jobCell != null && !jobCell.toString().trim().isEmpty()) {
                    lastJobRow = i; // 0-based index
                }
            }
            if (lastJobRow != -1) {
                endRow = lastJobRow + 1; // endRow is exclusive in your main loop
            } else {
                endRow = startRow; // No job name found, so only the start row
            }
        }

        workbook.close();
        fis.close();

        return (startRow != -1) ? new int[]{startRow, endRow} : null;
    }

    public static boolean selectFromDropdown(WebDriver driver, String selectName, String selectDropDown) throws InterruptedException {
        // 1. Open the dropdown
        WebElement dropdownButton = driver.findElement(By.xpath(selectDropDown));
        dropdownButton.click();
        Thread.sleep(1000); // wait for dropdown items to appear

        // 2. Locate all items in the scrollable dropdown
        List<WebElement> items = driver.findElements(By.xpath(DROPDOWN_OPTION));

        // 3. Match with our target name and click
        for (WebElement item : items) {
            String name = item.getAttribute("title").trim();
            if (name.equals(selectName)) {
                item.click();
                logInfo("Selected from dropdown: " + selectName);
                return true; // found and selected
            }
        }

        logInfo("Not found in dropdown: " + selectName);
        return false; // not found
    }

    public static void punchInMissingPopUp(WebDriver driver) {

        List<WebElement> attendanceHeaders = driver.findElements(By.xpath(POPUP_TEXT));

        if (!attendanceHeaders.isEmpty() && attendanceHeaders.get(0).isDisplayed()) {
            driver.findElement(By.xpath(POPUP_CANCEL)).click();
        } else {
            logInfo("No punch-in missing popup detected, continuing as normal.");
        }


    }

    public static void punchInMissing(WebDriver driver, String PunchMissType, String punchReason) throws InterruptedException {
        try {
            // Wait for popup (customize selector as needed)
            Thread.sleep(1000); // Use WebDriverWait in production

            if (PunchMissType.equals("work from home")) {
                // Click Work From Home radio and OK
                Thread.sleep(1000);
                driver.findElement(By.xpath(WFH_RADIO)).click();
                driver.findElement(By.xpath(POPUP_MSG)).sendKeys(punchReason);
                driver.findElement(By.xpath(POPUP_OK)).click();
                logInfo("Selected Work From Home for punch-in missing.");
                // continue with project/job filling
            } else if (PunchMissType.equals("leave")) {
                Thread.sleep(1000);
                driver.findElement(By.xpath(LEAVE_RADIO)).click();
                driver.findElement(By.xpath(POPUP_OK)).click();
                Thread.sleep(10000);
                logInfo("Selected Leave for punch-in missing.");
                // skip to next date (use continue in your date loop)
            } else if (PunchMissType.equals("official travel")) {
                driver.findElement(By.xpath(OFFICIAL_TRAVEL_RADIO)).click();
                driver.findElement(By.xpath(POPUP_MSG)).sendKeys(punchReason);
                driver.findElement(By.xpath(POPUP_OK)).click();
                logInfo("Selected Official Travel for punch-in missing.");
                // continue with project/job filling
            } else {
                logInfo("No valid punch-in reason found, continuing as normal.");
                driver.findElement(By.xpath(POPUP_CANCEL)).click();

                // continue as normal
            }
        } catch (Exception e) {
            logInfo("Punch-in missing popup not found or error: " + e.getMessage());
        }
    }
}