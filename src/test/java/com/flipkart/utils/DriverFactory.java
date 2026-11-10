package com.flipkart.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;
import java.util.List;

// driver is kept in ThreadLocal so it won't break if tests are run in parallel later.
// no need of WebDriverManager, selenium manager downloads the driver by itself
public final class DriverFactory {

    private static final Logger log = LogManager.getLogger(DriverFactory.class);
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    private DriverFactory() {
    }

    public static void initDriver() {
        String browser = ConfigReader.get("browser.name", "chrome").toLowerCase();
        boolean headless = ConfigReader.getBoolean("browser.headless", false);
        log.info("Launching {} (headless: {})", browser, headless);

        WebDriver webDriver = switch (browser) {
            case "chrome" -> new ChromeDriver(getChromeOptions(headless));
            case "edge" -> new EdgeDriver(getEdgeOptions(headless));
            case "firefox" -> new FirefoxDriver(getFirefoxOptions(headless));
            default -> throw new IllegalArgumentException("Browser not supported: " + browser);
        };

        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigReader.getInt("wait.implicit", 0)));
        webDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getInt("wait.pageLoad", 40)));
        if (!headless && ConfigReader.getBoolean("browser.maximize", true)) {
            webDriver.manage().window().maximize();
        }

        driver.set(webDriver);
        log.info("Browser launched");
    }

    public static WebDriver getDriver() {
        WebDriver webDriver = driver.get();
        if (webDriver == null) {
            throw new IllegalStateException("Driver is null, initDriver() not called");
        }
        return webDriver;
    }

    public static boolean hasDriver() {
        return driver.get() != null;
    }

    public static void quitDriver() {
        WebDriver webDriver = driver.get();
        if (webDriver != null) {
            try {
                webDriver.quit();
                log.info("Browser closed");
            } finally {
                driver.remove();
            }
        }
    }

    private static ChromeOptions getChromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications", "--no-sandbox", "--disable-dev-shm-usage");
        // removes the "chrome is being controlled by automated software" bar
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        if (headless) {
            options.addArguments("--headless=new", "--window-size=1920,1080");
        }
        return options;
    }

    private static EdgeOptions getEdgeOptions(boolean headless) {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--disable-notifications");
        if (headless) {
            options.addArguments("--headless=new", "--window-size=1920,1080");
        }
        return options;
    }

    private static FirefoxOptions getFirefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless", "--width=1920", "--height=1080");
        }
        return options;
    }
}
