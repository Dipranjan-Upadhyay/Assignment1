package com.flipkart.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

// explicit waits, default timeout is wait.explicit from config
public final class WaitUtils {

    private WaitUtils() {
    }

    private static WebDriverWait getWait(int seconds) {
        return new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(seconds));
    }

    public static int defaultTimeout() {
        return ConfigReader.getInt("wait.explicit", 15);
    }

    public static WebElement visible(By locator) {
        return visible(locator, defaultTimeout());
    }

    public static WebElement visible(By locator, int seconds) {
        return getWait(seconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement clickable(By locator) {
        return getWait(defaultTimeout()).until(ExpectedConditions.elementToBeClickable(locator));
    }

    // for buttons which get enabled only after the form is valid
    public static WebElement enabled(By locator) {
        return getWait(defaultTimeout()).until(d -> {
            WebElement element = d.findElement(locator);
            return element.isDisplayed() && element.isEnabled() && element.getAttribute("disabled") == null
                    ? element : null;
        });
    }

    // true if the input value becomes equal to expected within the given time
    public static boolean valueIs(By locator, String expected, int seconds) {
        try {
            return getWait(seconds).until(d -> expected.equals(d.findElement(locator).getAttribute("value")));
        } catch (TimeoutException e) {
            return false;
        }
    }

    // same as visible() but returns false instead of throwing exception
    public static boolean isVisible(By locator, int seconds) {
        try {
            visible(locator, seconds);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public static void pageLoaded() {
        getWait(ConfigReader.getInt("wait.pageLoad", 40)).until(d ->
                "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
    }
}
