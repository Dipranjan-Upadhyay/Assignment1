package com.flipkart.listeners;

import com.flipkart.utils.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

// retries a failed test based on retry.count in config (0 means no retry)
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LogManager.getLogger(RetryAnalyzer.class);
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        int maxRetry = ConfigReader.getInt("retry.count", 0);
        if (retryCount < maxRetry) {
            retryCount++;
            log.warn("Retrying {} ({}/{})", result.getMethod().getMethodName(), retryCount, maxRetry);
            return true;
        }
        return false;
    }
}
