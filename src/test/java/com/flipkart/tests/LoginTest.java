package com.flipkart.tests;

import com.flipkart.base.BaseTest;
import com.flipkart.pages.HomePage;
import com.flipkart.pages.LoginPage;
import com.flipkart.utils.ConfigReader;
import com.flipkart.utils.ExtentManager;
import com.flipkart.utils.WaitUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

// Login flow - each step depends on the previous one, so if one fails the rest get skipped
public class LoginTest extends BaseTest {

    private HomePage homePage;
    private LoginPage loginPage;

    @Test(priority = 1, description = "Launch browser and open Flipkart")
    public void navigateToFlipkart() {
        homePage = new HomePage(driver);
        homePage.open(ConfigReader.get("app.url"));
        Assert.assertTrue(homePage.isLoaded(), "Flipkart home page not loaded");
    }

    @Test(priority = 2, dependsOnMethods = "navigateToFlipkart", description = "Wait for the login popup")
    public void waitForLoginPopup() {
        loginPage = new LoginPage(driver);
        boolean popupShown = loginPage.waitForLoginPopup(ConfigReader.getInt("wait.popup", 10));

        // popup doesn't always come on first load, in that case open it from header Login link
        if (!popupShown) {
            log.warn("Login popup not shown, clicking Login from header");
            ExtentManager.logInfo("Login popup did not appear, clicked Login from header");
            homePage.clickLoginLink();
            popupShown = loginPage.waitForLoginPopup(WaitUtils.defaultTimeout());
        }
        Assert.assertTrue(popupShown, "Login popup not displayed");
    }

    @Test(priority = 3, dependsOnMethods = "waitForLoginPopup", description = "Enter mobile number and click Continue")
    public void enterMobileNumberAndContinue() {
        // on retry the number might be already submitted and otp screen open, so don't enter it again
        if (loginPage.isOtpScreenDisplayed(2)) {
            log.info("OTP screen already open, skipping mobile number entry");
            return;
        }
        loginPage.enterMobileNumber(ConfigReader.get("login.mobile"));
        loginPage.clickContinue();
        Assert.assertTrue(loginPage.isOtpScreenDisplayed(), "OTP screen not displayed after Continue");
    }

    @Test(priority = 4, dependsOnMethods = "enterMobileNumberAndContinue", description = "Enter OTP and click Verify")
    public void enterOtpAndVerify() {
        loginPage.enterOtp(ConfigReader.get("login.otp"));
        loginPage.clickVerify();

        // otp in config is a dummy one so flipkart will reject it.
        // not asserting here, just logging whatever message comes after verify
        loginPage.getVerificationMessage().ifPresentOrElse(
                msg -> {
                    log.info("Message after verify: {}", msg);
                    ExtentManager.logInfo("Message after verify: <b>" + msg + "</b>");
                },
                () -> log.info("No message shown after clicking verify"));
    }
}
