package com.example.framework.pages;

import com.example.framework.utils.ConfigReader;
import com.example.framework.utils.WaitUtils;
import org.openqa.selenium.*;

public class HomePage {
    private WebDriver driver;
    private WaitUtils wait;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
    }

    private By parseSelector(String key) {
        String raw = ConfigReader.get(key);
        if (raw.startsWith("css:")) return By.cssSelector(raw.substring(4));
        if (raw.startsWith("xpath:")) return By.xpath(raw.substring(6));
        return By.cssSelector(raw);
    }

    public void goTo() {
        driver.get(ConfigReader.get("target.url")); // opens the flights search page directly
        wait.waitForPageLoad();
    }

    


    public void goToFlights() {
        By flights = parseSelector("selector.flightsMenu");
        try {
            // wait for visible and clickable then click
            wait.waitForElement(flights).click();
            wait.waitForPageLoad();
            return;
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            // fall through to retry logic below
        } catch (Exception e) {
            // if some other transient issue, try fallback below
        }
    
        // Fallback attempts when overlay blocks the click
        try {
            // 1) send ESC to close any modal/popup
            driver.switchTo().activeElement().sendKeys(org.openqa.selenium.Keys.ESCAPE);
            // 2) click on body to remove overlays that close on outside click
            try {
                WebElement body = driver.findElement(By.tagName("body"));
                body.click();
            } catch (Exception ignored) {}
    
            // small sleep to allow UI to settle (use short)
            Thread.sleep(500);
    
            // try normal click again (wait until clickable)
            org.openqa.selenium.support.ui.WebDriverWait w =
                new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(5));
            w.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(flights)).click();
            wait.waitForPageLoad();
            return;
        } catch (Exception ex) {
            // last resort: javascript click (bypasses overlay issues)
            try {
                WebElement elem = driver.findElement(flights);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", elem);
                wait.waitForPageLoad();
                return;
            } catch (Exception jsEx) {
                throw new RuntimeException("Failed to navigate to Flights: " + jsEx.getMessage(), jsEx);
            }
        }
    }
    
}
