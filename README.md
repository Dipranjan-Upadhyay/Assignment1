# Flipkart Login Automation

Selenium automation for the Flipkart login flow. Built using Java, Selenium 4, TestNG and Maven.
Logs are done with Log4j2 and reports are generated using Extent Reports. CI is set up on GitHub Actions.

## Scenario

1. Open Chrome and go to flipkart.com
2. Wait for the login popup (if it doesn't come, click on Login from the header)
3. Enter mobile number and click Continue
4. Enter OTP and click Verify

Note: the OTP in the config file is a dummy one, so Flipkart will show an invalid OTP message at the last step.
The test doesn't fail for this, it just logs the message in the report. For a real login you would have to
pass the actual OTP that comes on your phone.

## Tools used

- Java 17
- Selenium 4.25
- TestNG 7.10
- Maven
- Log4j2
- Extent Reports 5
- GitHub Actions

## Folder structure

```
src/test/java/com/flipkart
    base/        BaseTest - opens and closes the browser
    pages/       page classes (HomePage, LoginPage) with locators and actions
    tests/       LoginTest
    listeners/   TestListener (report, logs, screenshots), RetryAnalyzer, AnnotationTransformer
    utils/       config reader, driver factory, waits, element actions, screenshot, extent manager
src/test/resources
    config.properties
    log4j2.xml
testng.xml
pom.xml
```

## Before running

- JDK 17 or above
- Maven (check with `mvn -v`)
- Chrome browser. Driver is downloaded automatically by Selenium, no need to download chromedriver.

## How to run

Run from the project folder:

```
mvn clean test
```

To pass your own mobile number:

```
mvn clean test "-Dlogin.mobile=9xxxxxxxxx"
```

Some other options:

```
mvn clean test "-Dbrowser.headless=true"
mvn clean test "-Dbrowser.name=edge"
```

Any key from `config.properties` can be passed like this. (Quotes are needed in PowerShell, in cmd/bash it works without quotes.)

You can also run `testng.xml` directly from Eclipse/IntelliJ (right click > Run As > TestNG Suite).

## Config

All settings are in `src/test/resources/config.properties` - url, browser, test data, waits, retry count etc.

- `retry.count` - how many times a failed test is retried (currently 1)
- `wait.explicit` - default wait time in seconds
- `screenshot.on.failure` - take screenshot when a test fails

## Reports and logs

After the run these folders get created:

- `reports/` - Extent HTML report (open in browser)
- `logs/automation.log` - complete log of the run, older ones go to `logs/archive/`
- `logs/tests/` - separate log file for each test
- `screenshots/` - screenshots of failed tests (these are also attached in the report)

## CI

The workflow file is `.github/workflows/ci.yml`. It runs on push to `main` and `feature/**` branches, on pull requests
to `main`, and can also be triggered manually from the Actions tab. Tests run in headless chrome and the reports,
logs and screenshots are uploaded as an artifact.

For the mobile number and OTP, add `LOGIN_MOBILE` and `LOGIN_OTP` in repo secrets. If not added it uses the values
from config.properties.

## Notes

- Locators are based on text/labels and not on class names, since Flipkart's class names are auto generated and
  change frequently.
- The Continue button is disabled until a valid number is entered, so the code waits for it to get enabled before
  clicking.
- If one step fails, the next steps are marked as skipped (dependsOnMethods is used).
