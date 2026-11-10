package com.flipkart.pages;

import com.flipkart.utils.ElementUtils;
import com.flipkart.utils.ExtentManager;
import com.flipkart.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HomePage extends BasePage {

    // not using class names, flipkart's classes are generated and keep changing
    private static final By LOGIN_LINK = By.xpath(
            "//a[contains(@href,'/account/login')] | //span[normalize-space()='Login']/ancestor::a[1]");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public void open(String url) {
        log.info("Opening {}", url);
        ExtentManager.logInfo("Open " + url);
        driver.get(url);
        WaitUtils.pageLoaded();
    }

    public boolean isLoaded() {
        String url = driver.getCurrentUrl();
        String title = driver.getTitle();
        log.info("URL: {}, Title: {}", url, title);
        return url.contains("flipkart.com") || title.toLowerCase().contains("flipkart");
    }

    // used only when login popup doesn't open by itself
    public void clickLoginLink() {
        ElementUtils.click(LOGIN_LINK, "Login link in header");
    }
}
