package com.flipkart.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

// common actions like click/type, every action is logged in log file and extent report
public final class ElementUtils {

    private static final Logger log = LogManager.getLogger(ElementUtils.class);

    private ElementUtils() {
    }

    public static void click(By locator, String elementName) {
        log.info("Click on {}", elementName);
        ExtentManager.logInfo("Click on <b>" + elementName + "</b>");
        try {
            WaitUtils.clickable(locator).click();
        } catch (ElementClickInterceptedException e) {
            // some overlay is on top of the element, js click works in this case
            log.warn("Click intercepted on {}, trying with js click", elementName);
            jsClick(WaitUtils.visible(locator));
        }
    }

    // sensitive = true masks the value in logs and report (used for mobile number and otp)
    public static void type(By locator, String text, String elementName, boolean sensitive) {
        String valueToLog = sensitive ? CommonUtils.mask(text) : text;
        log.info("Enter {} in {}", valueToLog, elementName);
        ExtentManager.logInfo("Enter <b>" + valueToLog + "</b> in " + elementName);

        WebElement element = WaitUtils.clickable(locator);
        element.click();
        // clear() was not working properly on flipkart's react inputs, so using ctrl+a and delete
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        element.sendKeys(text);

        // check the value actually got entered, otherwise set it using js
        if (!WaitUtils.valueIs(locator, text, 3)) {
            log.warn("Value not entered in {}, trying with js", elementName);
            jsSetValue(element, text);
            if (!WaitUtils.valueIs(locator, text, 3)) {
                throw new IllegalStateException("Not able to enter value in " + elementName);
            }
        }
    }

    public static String getText(By locator) {
        String text = WaitUtils.visible(locator).getText().trim();
        log.debug("Text of {}: {}", locator, text);
        return text;
    }

    public static void jsClick(WebElement element) {
        ((JavascriptExecutor) DriverFactory.getDriver()).executeScript("arguments[0].click();", element);
    }

    // simply doing element.value = x doesn't update react state,
    // so calling the native value setter and then firing input/change events
    public static void jsSetValue(WebElement element, String text) {
        ((JavascriptExecutor) DriverFactory.getDriver()).executeScript(
                "const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set;"
                + "setter.call(arguments[0], arguments[1]);"
                + "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));"
                + "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
                element, text);
    }
}
