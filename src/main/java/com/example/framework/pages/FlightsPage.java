package com.example.framework.pages;

import com.example.framework.utils.ConfigReader;
import com.example.framework.utils.WaitUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class FlightsPage {
    private WebDriver driver;
    private WaitUtils wait;

    public FlightsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
    }

    private void sleepMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private By get(String key) {
        String raw = ConfigReader.get(key);
        if (raw == null) throw new RuntimeException("Missing config key: " + key);
        if (raw.startsWith("css:")) return By.cssSelector(raw.substring(4));
        if (raw.startsWith("xpath:")) return By.xpath(raw.substring(6));
        return By.cssSelector(raw);
    }

    private void dismissOverlays() {
        // try ESC and body-click first
        try { driver.switchTo().activeElement().sendKeys(Keys.ESCAPE); } catch (Exception ignored) {}
        try { driver.findElement(By.tagName("body")).click(); } catch (Exception ignored) {}
        sleepMs(300);

        // Try common close buttons for dialogs/modals
        String[] closeSelectors = new String[] {
                "button[aria-label='Close']",
                "button[aria-label='close']",
                "button[aria-label='Dismiss']",
                "button[class*='close']",
                "button[class*='Close']",
                "button[data-testid*='close']",
                ".modal-close, .close, .rc-modal-close, .dialog__close, .popup__close"
        };

        for (String sel : closeSelectors) {
            try {
                List<WebElement> els = driver.findElements(By.cssSelector(sel));
                for (WebElement e : els) {
                    if (e.isDisplayed() && e.getSize().getHeight() > 0) {
                        try {
                            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", e);
                            e.click();
                            sleepMs(250);
                        } catch (Exception ex) {
                            try {
                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", e);
                                sleepMs(250);
                            } catch (Exception ignored) {}
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        // fallback: find any visible dialog with an 'x' button and click it
        try {
            List<WebElement> possibleX = driver.findElements(By.xpath("//button[contains(., '×') or contains(., 'X') or contains(., 'x')]"));
            for (WebElement x : possibleX) {
                if (x.isDisplayed()) {
                    try { x.click(); sleepMs(200); } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}
    }

    // Location entry with suggestion handling
    public void enterFrom(String from) {
        enterLocation("selector.fromInput", from);
    }

    public void enterTo(String to) {
        enterLocation("selector.toInput", to);
    }

    private void enterLocation(String configKey, String value) {
        By inputBy = get(configKey);
        WebElement input = wait.waitForElement(inputBy);
        // clear & focus
        try { input.click(); } catch (Exception ignored) {}
        try {
            // prefer chord for select-all
            input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        } catch (Exception ignored) {}
        input.sendKeys(Keys.DELETE);
        input.sendKeys(value);

        // give site a moment to load suggestions
        sleepMs(400);

        // Try several common suggestion selectors (most travel sites use a list or divs)
        String token = value.split(" ")[0];
        String[] suggestionXPaths = new String[] {
                "//ul[@role='listbox']//li[.//text()[contains(., '" + token + "')]]",
                "//li[contains(., '" + token + "')]",
                "//div[contains(@class,'suggestion') or contains(@class,'autocomplete') or contains(@class,'airport')][contains(., '" + token + "')]",
                "//p[contains(., '" + token + "')]"
        };

        boolean chosen = false;
        for (String xpath : suggestionXPaths) {
            try {
                By candidate = By.xpath(xpath);
                WebElement suggestion = (new WebDriverWait(driver, Duration.ofSeconds(5)))
                        .until(ExpectedConditions.elementToBeClickable(candidate));
                suggestion.click();
                chosen = true;
                break;
            } catch (Exception ignored) {}
        }

        // Final fallback: press ENTER to accept the typed text
        if (!chosen) {
            try { input.sendKeys(Keys.ENTER); } catch (Exception ignored) {}
        }

        // small wait before next action
        sleepMs(300);
    }

    // Robust date picker opener + selector (tries multiple approaches)
    public void selectNextMonthDate(int day) {
        // Dismiss overlays first
        dismissOverlays();
        sleepMs(150);

        // Try configured picker from config
        String configured = ConfigReader.get("selector.datePicker");
        if (configured != null) {
            try {
                By cfgBy = configured.startsWith("css:") ? By.cssSelector(configured.substring(4))
                        : configured.startsWith("xpath:") ? By.xpath(configured.substring(6))
                        : By.cssSelector(configured);
                WebElement cfg = (new WebDriverWait(driver, Duration.ofSeconds(5)))
                        .until(ExpectedConditions.elementToBeClickable(cfgBy));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", cfg);
                try { cfg.click(); } catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cfg); }
                sleepMs(400);
            } catch (Exception ignored) {}
        }

        // Common opener selectors
        String[] openers = new String[] {
                "css:div[aria-label*='Depart']",
                "css:input[placeholder*='Depart']",
                "css:input[type='date']",
                "css:div[data-testid*='depart']",
                "css:button[aria-label*='calendar']",
                "css:div[role='button']"
        };

        boolean opened = false;
        for (String raw : openers) {
            try {
                By by = raw.startsWith("css:") ? By.cssSelector(raw.substring(4)) : By.xpath(raw.substring(6));
                WebElement el = (new WebDriverWait(driver, Duration.ofSeconds(3)))
                        .until(ExpectedConditions.elementToBeClickable(by));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", el);
                try { el.click(); } catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el); }
                sleepMs(350);
                opened = true;
                break;
            } catch (Exception ignored) {}
        }

        // If not opened, find visible element with month text (e.g. "Mon, Nov 10")
        if (!opened) {
            String monthsCondition = "contains(.,'Jan') or contains(.,'Feb') or contains(.,'Mar') or contains(.,'Apr') or contains(.,'May') or contains(.,'Jun') or contains(.,'Jul') or contains(.,'Aug') or contains(.,'Sep') or contains(.,'Oct') or contains(.,'Nov') or contains(.,'Dec')";
            String xpath = "//*[contains(text(),',') and (" + monthsCondition + ")]";
            List<WebElement> candidates = driver.findElements(By.xpath(xpath));
            for (WebElement c : candidates) {
                try {
                    if (c.isDisplayed() && c.getSize().getHeight() > 0 && c.getText().trim().length() > 0) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", c);
                        try { c.click(); } catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", c); }
                        sleepMs(350);
                        opened = true;
                        break;
                    }
                } catch (Exception ignored) {}
            }
        }

        // Click next-month button if available
        try {
            By nextBtn = get("selector.nextMonthButton");
            (new WebDriverWait(driver, Duration.ofSeconds(2)))
                    .until(ExpectedConditions.elementToBeClickable(nextBtn)).click();
            sleepMs(250);
        } catch (Exception ignored) {}

        // Try pattern-based day selection from config
        try {
            String pattern = ConfigReader.get("selector.dateCellPattern");
            if (pattern != null && pattern.contains("{DAY}")) {
                String replaced = pattern.replace("{DAY}", String.valueOf(day));
                By dayBy = replaced.startsWith("xpath:") ? By.xpath(replaced.substring(6)) : By.cssSelector(replaced.substring(4));
                (new WebDriverWait(driver, Duration.ofSeconds(5)))
                        .until(ExpectedConditions.elementToBeClickable(dayBy)).click();
                return;
            }
        } catch (Exception ignored) {}

        // Loose attempt: click any visible element containing the day number
        try {
            By loose = By.xpath("//*[normalize-space(text())='" + day + "' or contains(normalize-space(.), '" + day + "')]");
            (new WebDriverWait(driver, Duration.ofSeconds(4)))
                    .until(ExpectedConditions.elementToBeClickable(loose)).click();
            return;
        } catch (Exception ignored) {}

        // Final fallback: set input value via JS
        try {
            java.time.LocalDate nm = java.time.LocalDate.now().plusMonths(1);
            int validDay = Math.min(day, nm.lengthOfMonth());
            java.time.LocalDate target = nm.withDayOfMonth(validDay);
            String iso = target.toString();
            List<WebElement> inputs = driver.findElements(By.cssSelector("input[type='date'], input[name*='depart'], input[placeholder*='Depart']"));
            if (!inputs.isEmpty()) {
                WebElement inp = inputs.get(0);
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input')); arguments[0].dispatchEvent(new Event('change'));",
                        inp, iso
                );
                sleepMs(300);
                return;
            }
        } catch (Exception ignored) {}

        throw new RuntimeException("selectNextMonthDate: unable to open/select date. Inspect page and update selectors in config.properties.");
    }


    /**
 * Select a specific date (year, month, day) in the calendar.
 * month: 1..12 (Jan=1, Dec=12)
 */
public void selectSpecificDate(int year, int month, int day) {
    // dismiss overlays first
    dismissOverlays();
    sleepMs(150);

    // Try to open calendar if not already open (reuse opener logic)
    try {
        // try configured picker
        String configured = ConfigReader.get("selector.datePicker");
        if (configured != null) {
            By cfgBy = configured.startsWith("css:") ? By.cssSelector(configured.substring(4))
                    : configured.startsWith("xpath:") ? By.xpath(configured.substring(6))
                    : By.cssSelector(configured);
            try {
                WebElement cfg = (new WebDriverWait(driver, Duration.ofSeconds(4)))
                        .until(ExpectedConditions.elementToBeClickable(cfgBy));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", cfg);
                try { cfg.click(); } catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cfg); }
                sleepMs(300);
            } catch (Exception ignored) {}
        }
    } catch (Exception ignored) {}

    // Compute an ISO-like string and try direct input first (works if there's an input[type=date])
    try {
        java.time.LocalDate target = java.time.LocalDate.of(year, month, Math.min(day, java.time.LocalDate.of(year, month, 1).lengthOfMonth()));
        String iso = target.toString(); // yyyy-MM-dd

        List<WebElement> dateInputs = driver.findElements(By.cssSelector("input[type='date'], input[name*='return'], input[placeholder*='Return'], input[placeholder*='return']"));
        if (!dateInputs.isEmpty()) {
            WebElement inp = dateInputs.get(0);
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input')); arguments[0].dispatchEvent(new Event('change'));",
                    inp, iso
            );
            sleepMs(300);
            return;
        }
    } catch (Exception ignored) {}

    // Otherwise attempt to navigate calendar UI:
    // 1) try to open month picker and navigate to requested month/year using next/prev controls.
    // We try repeatedly clicking Next Month until the calendar shows the requested month (by searching month name).
    String[] monthNames = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
    String targetMonthName = monthNames[month - 1];

    // open calendar if needed: find visible month label to detect calendar open
    boolean calendarOpen = false;
    try {
        List<WebElement> monthLabels = driver.findElements(By.xpath("//*[contains(@class,'month') or contains(@aria-label,'Month') or contains(@class,'calendar')]//text()"));
        if (!monthLabels.isEmpty()) calendarOpen = true;
    } catch (Exception ignored) {}

    // try a loose approach: click month navigation next until we see target month text on page
    int attempts = 0;
    while (attempts < 7) { // at most 7 clicks (safety)
        try {
            // if target month appears anywhere on page, break
            List<WebElement> found = driver.findElements(By.xpath("//*[contains(text(), '" + targetMonthName + "') and (contains(@class,'month') or contains(@class,'calendar') or contains(@aria-label,'Month') or contains(text(), '" + targetMonthName + "'))]"));
            if (!found.isEmpty()) break;
        } catch (Exception ignored) {}

        // click next-month button if available
        try {
            By nextBtn = get("selector.nextMonthButton");
            new WebDriverWait(driver, Duration.ofSeconds(2)).until(ExpectedConditions.elementToBeClickable(nextBtn)).click();
            sleepMs(300);
        } catch (Exception e) {
            // try a generic next button
            try {
                WebElement genericNext = driver.findElement(By.xpath("//button[contains(@aria-label,'Next') or contains(.,'›') or contains(.,'>')]"));
                if (genericNext.isDisplayed()) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", genericNext);
                    try { genericNext.click(); } catch (Exception ex) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", genericNext); }
                    sleepMs(300);
                } else {
                    break;
                }
            } catch (Exception ignored) {
                break;
            }
        }
        attempts++;
    }

    // Finally, click on the day cell using a loose selector matching the number.
    try {
        By dayBy = By.xpath("//*[normalize-space(text())='" + day + "' or contains(normalize-space(.), '" + day + "')]");
        WebElement dayEl = new WebDriverWait(driver, Duration.ofSeconds(4)).until(ExpectedConditions.elementToBeClickable(dayBy));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", dayEl);
        try { dayEl.click(); } catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dayEl); }
        sleepMs(200);
        return;
    } catch (Exception ignored) {}

    throw new RuntimeException("selectSpecificDate: unable to set date " + year + "-" + month + "-" + day + ". Inspect page and add a specific selector.");
}


    // Resilient click for Search button
    public void clickSearch() {
        By searchBy = get("selector.searchButton");
        try {
            wait.waitForElement(searchBy).click();
            wait.waitForPageLoad();
            return;
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            // fallthrough to fallback steps
        } catch (Exception e) {
            // generic fallback
        }

        // fallback: try dismissing overlays and click again
        try { driver.switchTo().activeElement().sendKeys(Keys.ESCAPE); } catch (Exception ignored) {}
        try { WebElement body = driver.findElement(By.tagName("body")); body.click(); } catch (Exception ignored) {}
        sleepMs(400);

        try {
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(5));
            w.until(ExpectedConditions.elementToBeClickable(searchBy)).click();
            wait.waitForPageLoad();
            return;
        } catch (Exception ignored) {}

        // final resort - JS click
        try {
            WebElement btn = driver.findElement(searchBy);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            wait.waitForPageLoad();
        } catch (Exception e) {
            throw new RuntimeException("Unable to click Search button: " + e.getMessage(), e);
        }
    }
}
