package utils;


import java.io.File;

public class WaitUtil {



    public static void waitForFile(String filePath, int timeoutInSeconds) {
        File file = new File(filePath);
        long endTime = System.currentTimeMillis() + (timeoutInSeconds * 1000);

        while (System.currentTimeMillis() < endTime) {
            if (file.exists() && file.length() > 0) {
                return; // File found
            }
            try {
                Thread.sleep(1000); // Check every second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted while waiting for file: " + filePath, e);
            }
        }
        throw new RuntimeException("File not found within " + timeoutInSeconds + " seconds: " + filePath);
    }

}
