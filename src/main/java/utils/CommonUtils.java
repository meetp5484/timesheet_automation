package utils;

import java.util.logging.Level;
import java.util.logging.Logger;


public class CommonUtils {
    private static final Logger logger = Logger.getLogger(CommonUtils.class.getName());

    public static void logInfo(String message) {
        logger.log(Level.INFO, message);
    }

}
