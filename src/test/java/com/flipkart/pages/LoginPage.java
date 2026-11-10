package com.flipkart.pages;

import com.flipkart.utils.CommonUtils;
import com.flipkart.utils.ElementUtils;
import com.flipkart.utils.ExtentManager;
import com.flipkart.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.List;
import java.util.Optional;

public class LoginPage extends BasePage {

    // phone field is <input type="number" id="1"> and the "Phone Number" label points to it using for="1".
    // older UI had a "Mobile number" label after the input, kept that also just in case
    private static final String MOBILE_LABEL =
            "label[contains(normalize-space(),'Phone Number') or contains(normalize-space(),'Mobile number')]";
    private static final By MOBILE_INPUT = By.xpath(
            "//input[@id=//" + MOBILE_LABEL + "/@for]"
            + " | //" + MOBILE_LABEL + "/preceding-sibling::input");

    private static final By CONTINUE_BUTTON = By.xpath(
            "//button[normalize-space()='Request OTP' or normalize-space()='CONTINUE' or normalize-space()='Continue']");

    // otp screen opens in the same popup - "Please enter the verification code we've sent you on..."
    // the 6 boxes are just underlines, there is no visible input, so the screen is checked using this text
    private static final String OTP_TEXT_XPATH = "//*[text()[contains(.,'verification code') or contains(.,'Enter OTP')]]";
    private static final By OTP_TEXT = By.xpath(OTP_TEXT_XPATH);

    // input behind the underline boxes (can be hidden), plus old UI patterns
    private static final String OTP_INPUTS_XPATH =
            OTP_TEXT_XPATH + "/following::input[not(@type='hidden') and not(@name='q')]"
            + " | //input[@maxlength='1' and (@type='text' or @type='tel' or @type='number')]"
            + " | //label[contains(.,'OTP')]/preceding-sibling::input | //input[@maxlength='6']";
    private static final By OTP_INPUTS = By.xpath(OTP_INPUTS_XPATH);
    private static final By OTP_SCREEN = By.xpath(OTP_TEXT_XPATH + " | " + OTP_INPUTS_XPATH);

    private static final By VERIFY_BUTTON = By.xpath(
            "//button[contains(normalize-space(),'Verify') or normalize-space()='Login' or normalize-space()='Signup']");

    // xpath 1.0 has no lower-case() so using translate for case insensitive match
    private static final String LOWER = "translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')";
    private static final By OTP_MESSAGE = By.xpath(
            "//*[not(*) and contains(" + LOWER + ",'otp') and (contains(" + LOWER + ",'valid')"
            + " or contains(" + LOWER + ",'incorrect') or contains(" + LOWER + ",'wrong'))]");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public boolean waitForLoginPopup(int seconds) {
        log.info("Waiting for login popup ({} sec)", seconds);
        boolean displayed = WaitUtils.isVisible(MOBILE_INPUT, seconds);
        log.info("Login popup displayed: {}", displayed);
        return displayed;
    }

    public void enterMobileNumber(String mobile) {
        ElementUtils.type(MOBILE_INPUT, mobile, "Mobile number field", true);
    }

    public void clickContinue() {
        // continue button stays disabled till a valid 10 digit number is entered
        log.info("Waiting for Continue button to get enabled");
        WaitUtils.enabled(CONTINUE_BUTTON);
        ElementUtils.click(CONTINUE_BUTTON, "Continue button");
    }

    public boolean isOtpScreenDisplayed() {
        return isOtpScreenDisplayed(WaitUtils.defaultTimeout());
    }

    public boolean isOtpScreenDisplayed(int seconds) {
        boolean displayed = WaitUtils.isVisible(OTP_SCREEN, seconds);
        log.info("OTP screen displayed: {}", displayed);
        return displayed;
    }

    public void enterOtp(String otp) {
        WaitUtils.visible(OTP_TEXT);
        log.info("Entering OTP {}", CommonUtils.mask(otp));
        ExtentManager.logInfo("Enter OTP <b>" + CommonUtils.mask(otp) + "</b>");

        List<WebElement> inputs = driver.findElements(OTP_INPUTS);
        List<WebElement> visibleBoxes = inputs.stream().filter(WebElement::isDisplayed).toList();

        if (visibleBoxes.size() >= otp.length()) {
            // old UI - one visible box per digit
            for (int i = 0; i < otp.length(); i++) {
                visibleBoxes.get(i).click();
                visibleBoxes.get(i).sendKeys(String.valueOf(otp.charAt(i)));
            }
            return;
        }

        // new UI - no visible input. focus the (hidden) input if there is one, otherwise the first box
        // gets focus by itself after Continue. then type the otp from keyboard like a user would
        if (!inputs.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].focus();", inputs.get(0));
        }
        new Actions(driver).sendKeys(otp).perform();
    }

    public void clickVerify() {
        // verify is also disabled till all the otp digits are entered
        WaitUtils.enabled(VERIFY_BUTTON);
        ElementUtils.click(VERIFY_BUTTON, "Verify button");
    }

    // returns the message shown after verify (like invalid otp), empty if nothing comes up
    public Optional<String> getVerificationMessage() {
        if (WaitUtils.isVisible(OTP_MESSAGE, 5)) {
            return Optional.of(ElementUtils.getText(OTP_MESSAGE));
        }
        return Optional.empty();
    }
}
