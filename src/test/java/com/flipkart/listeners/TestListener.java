package com.flipkart.listeners;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.flipkart.utils.ConfigReader;
import com.flipkart.utils.DriverFactory;
import com.flipkart.utils.ExtentManager;
import com.flipkart.utils.ScreenshotUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

// handles extent report, separate log file for each test and screenshot on failure
public class TestListener implements ITestListener {

    private static final Logger log = LogManager.getLogger(TestListener.class);

    @Override
    public void onStart(ITestContext context) {
        log.info("========== Suite started: {} ==========", context.getName());
        ExtentManager.getInstance();
    }

    @Override
    public void onTestStart(ITestResult result) {
        startTest(result);
        log.info("----- Started: {} -----", getName(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = getOrCreateTest(result);
        log.info("PASSED: {} ({} ms)", getName(result), timeTaken(result));
        test.pass("Passed in " + timeTaken(result) + " ms");
        endTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = getOrCreateTest(result);
        log.error("FAILED: {}", getName(result), result.getThrowable());
        test.fail(result.getThrowable());
        addScreenshot(test, result);
        endTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = getOrCreateTest(result);
        if (result.wasRetried()) {
            // the failed attempt is removed from report, only the final retry result is kept
            log.warn("Retrying: {}", getName(result));
            ExtentManager.getInstance().removeTest(test);
        } else {
            String reason = result.getThrowable() != null
                    ? result.getThrowable().getMessage()
                    : "Skipped as the dependent test failed or was skipped";
            log.warn("SKIPPED: {} - {}", getName(result), reason);
            test.skip(reason);
        }
        endTest();
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("========== Suite finished: {} | Passed: {} | Failed: {} | Skipped: {} ==========",
                context.getName(),
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());
        ExtentManager.flush();
    }

    private void startTest(ITestResult result) {
        ThreadContext.put("testName", result.getTestClass().getRealClass().getSimpleName() + "_" + getName(result));
        ExtentManager.createTest(getName(result), result.getMethod().getDescription())
                .assignCategory(result.getTestClass().getRealClass().getSimpleName());
    }

    // onTestStart is not called for skipped tests, so the report entry has to be created here
    private ExtentTest getOrCreateTest(ITestResult result) {
        if (ExtentManager.getTest() == null) {
            startTest(result);
        }
        return ExtentManager.getTest();
    }

    private void addScreenshot(ExtentTest test, ITestResult result) {
        if (!ConfigReader.getBoolean("screenshot.on.failure", true) || !DriverFactory.hasDriver()) {
            return;
        }
        try {
            String path = ScreenshotUtils.capture(getName(result));
            String base64 = ScreenshotUtils.captureBase64();
            test.fail("Screenshot (" + path + ")",
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());
        } catch (Exception e) {
            log.error("Failed to take screenshot", e);
        }
    }

    private void endTest() {
        ExtentManager.removeTest();
        ThreadContext.remove("testName");
    }

    private static String getName(ITestResult result) {
        return result.getMethod().getMethodName();
    }

    private static long timeTaken(ITestResult result) {
        return result.getEndMillis() - result.getStartMillis();
    }
}
