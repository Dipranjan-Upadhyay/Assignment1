package com.flipkart.base;

import com.flipkart.utils.DriverFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public abstract class BaseTest {

    protected final Logger log = LogManager.getLogger(getClass());
    protected WebDriver driver;

    // browser is opened once per class (not per method) because the login steps
    // continue in the same session
    @BeforeClass(alwaysRun = true)
    public void setUp() {
        ThreadContext.put("testName", getClass().getSimpleName() + "_setup");
        try {
            log.info("Starting browser for {}", getClass().getSimpleName());
            DriverFactory.initDriver();
            driver = DriverFactory.getDriver();
        } finally {
            ThreadContext.remove("testName");
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        ThreadContext.put("testName", getClass().getSimpleName() + "_teardown");
        try {
            log.info("Closing browser for {}", getClass().getSimpleName());
            DriverFactory.quitDriver();
        } finally {
            ThreadContext.remove("testName");
        }
    }
}
