package utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CommonUtils {
    private static final Logger logger = Logger.getLogger(CommonUtils.class.getName());

    public static void logInfo(String message) {
        logger.log(Level.INFO, message);
    }

}
