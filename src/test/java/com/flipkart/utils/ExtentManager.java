package com.flipkart.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

// one report for the whole run, current test is stored per thread
public final class ExtentManager {

    private static final Logger log = LogManager.getLogger(ExtentManager.class);
    private static final String REPORT_FOLDER = "reports";
    private static final ThreadLocal<ExtentTest> currentTest = new ThreadLocal<>();
    private static ExtentReports extent;

    private ExtentManager() {
    }

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            try {
                Files.createDirectories(Paths.get(REPORT_FOLDER));
            } catch (IOException e) {
                log.error("Unable to create reports folder", e);
            }
            String reportPath = REPORT_FOLDER + "/ExtentReport_" + CommonUtils.timestamp() + ".html";

            ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
            spark.config().setDocumentTitle(ConfigReader.get("report.title", "Automation Report"));
            spark.config().setReportName(ConfigReader.get("report.name", "Test Execution"));
            spark.config().setTheme(Theme.STANDARD);
            spark.config().setTimeStampFormat("dd-MM-yyyy HH:mm:ss");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Application URL", ConfigReader.get("app.url"));
            extent.setSystemInfo("Browser", ConfigReader.get("browser.name", "chrome"));
            extent.setSystemInfo("Headless", ConfigReader.get("browser.headless", "false"));
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("Java", System.getProperty("java.version"));
            log.info("Report path: {}", reportPath);
        }
        return extent;
    }

    public static ExtentTest createTest(String name, String description) {
        ExtentTest test = getInstance().createTest(name, description);
        currentTest.set(test);
        return test;
    }

    public static ExtentTest getTest() {
        return currentTest.get();
    }

    public static void removeTest() {
        currentTest.remove();
    }

    // adds a step in the report for the running test
    public static void logInfo(String message) {
        ExtentTest test = currentTest.get();
        if (test != null) {
            test.info(message);
        }
    }

    public static synchronized void flush() {
        if (extent != null) {
            extent.flush();
            log.info("Report generated");
        }
    }
}
