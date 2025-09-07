package config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;

import static utils.CommonUtils.logInfo;

public class DriverFactory {

    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static final String browser = ConfigManager.get("browser");
    private static final String downloadPath = ConfigManager.get("download.casepath.assigned.path");
    private static String extraFolder = null; // Optional runtime folder

    public static void setExtraDownloadFolder(String folderName) {
        extraFolder = folderName; // Set at runtime if needed
    }

    public static void initDriver() {
        if (driver.get() == null) {
            try {
                if (browser == null || browser.isEmpty()) {
                    throw new IllegalArgumentException("⚠️ 'browser' property not set in application.properties");
                }
                switch (browser.toLowerCase()) {
                    case "chrome":
                        WebDriverManager.chromedriver().setup();
                        ChromeOptions chromeOptions = new ChromeOptions();

                        // Set download directory
                        HashMap<String, Object> chromePrefs = new HashMap<>();
                        chromePrefs.put("profile.password_manager_leak_detection",false);

                        chromeOptions.setExperimentalOption("prefs", chromePrefs);
                        driver.set(new ChromeDriver(chromeOptions));
                        break;

                    case "firefox":
                        WebDriverManager.firefoxdriver().setup();
                        FirefoxProfile profile = new FirefoxProfile();

                        // Set download directory
                        profile.setPreference("browser.download.folderList", 2);
                        profile.setPreference("browser.download.dir", downloadPath);
                        profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
                                "application/pdf,application/octet-stream,application/vnd.ms-excel,text/csv");
                        profile.setPreference("pdfjs.disabled", true);

                        FirefoxOptions firefoxOptions = new FirefoxOptions();
                        firefoxOptions.setProfile(profile);
                        driver.set(new FirefoxDriver(firefoxOptions));
                        break;

                    case "edge":
                        WebDriverManager.edgedriver().setup();
                        EdgeOptions edgeOptions = new EdgeOptions();

                        // Set download directory
                        HashMap<String, Object> edgePrefs = new HashMap<>();
                        edgePrefs.put("profile.default_content_settings.popups", 0);
                        edgePrefs.put("download.prompt_for_download", false);
                        edgePrefs.put("safebrowsing.enabled", true);

                        edgeOptions.setExperimentalOption("prefs", edgePrefs);
                        driver.set(new EdgeDriver(edgeOptions));
                        break;

                    default:
                        throw new RuntimeException("Unsupported browser: " + browser);
                }
                WebDriver webDriver = driver.get();
                webDriver.manage().window().maximize();

            } catch (Exception e) {
                logInfo("Failed to initialize WebDriver for browser: " + browser);
                e.printStackTrace();
                throw new RuntimeException("WebDriver initialization failed", e);
            }
        }
        driver.get();
    }

    public static WebDriver getDriver() {
        if (driver.get() == null) {
            throw new IllegalStateException("Driver is not initialized. Call initDriver() first.");
        }
        return driver.get();
    }

    public static void quitDriver() {
        try {
            if (driver.get() != null) {
                driver.get().quit();
                driver.remove();
            }
        } catch (Exception e) {
            logInfo("Failed to quit WebDriver");
            e.printStackTrace();
        }
    }
}
