package com.flipkart.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public final class ScreenshotUtils {

    private static final Logger log = LogManager.getLogger(ScreenshotUtils.class);
    private static final String SCREENSHOT_FOLDER = "screenshots";

    private ScreenshotUtils() {
    }

    // saves screenshot in screenshots folder and returns the file path
    public static String capture(String name) {
        File src = ((TakesScreenshot) DriverFactory.getDriver()).getScreenshotAs(OutputType.FILE);
        Path dest = Paths.get(SCREENSHOT_FOLDER, name + "_" + CommonUtils.timestamp() + ".png");
        try {
            Files.createDirectories(dest.getParent());
            Files.copy(src.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Screenshot not saved", e);
        }
        log.info("Screenshot saved at {}", dest);
        return dest.toString();
    }

    // base64 is used for the report so the screenshot still shows if report is shared with someone
    public static String captureBase64() {
        return ((TakesScreenshot) DriverFactory.getDriver()).getScreenshotAs(OutputType.BASE64);
    }
}
