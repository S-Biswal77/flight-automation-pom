package com.example.tests;

import com.example.framework.drivers.DriverFactory;
import com.example.framework.pages.*;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.testng.annotations.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class FlightSearchTest {
    private WebDriver driver;
    private HomePage home;
    private FlightsPage flights;
    private SearchResultsPage results;

    @BeforeClass
    public void setup() {
        driver = DriverFactory.getDriver();
        home = new HomePage(driver);
        flights = new FlightsPage(driver);
        results = new SearchResultsPage(driver);
    }

  @Test
public void testFlightSearch() throws InterruptedException {
    driver = DriverFactory.getDriver();
    home = new HomePage(driver);
    flights = new FlightsPage(driver);
    results = new SearchResultsPage(driver);

    // 1. Open flights page
    home.goTo();

    // 2. Enter from/to
    flights.enterFrom("Bengaluru");
    flights.enterTo("New Delhi");

    // 3. Select departure date (use existing method - picks a day in next month)
    LocalDate nextMonth = LocalDate.now().plusMonths(1);
    int departDay = Math.min(15, nextMonth.lengthOfMonth()); // change if you want a specific depart day
    flights.selectNextMonthDate(departDay);

    // 4. Open Return field and set return to 8 Dec 2025
    // ensure round-trip mode if required (click return field)
    // if you have an explicit openReturnDateField(), call it; otherwise selectSpecificDate will open calendar
    // open return field (click the return input)
    try {
        // try clicking a return placeholder/input first
        List<WebElement> retInputs = ((JavascriptExecutor) driver).executeScript(
                "return Array.from(document.querySelectorAll('input[placeholder], div')).filter(e => (e.placeholder && e.placeholder.toLowerCase().includes('return')) || (e.textContent && e.textContent.toLowerCase().includes('return')));"
        ) == null ? List.of() : List.of();
    } catch (Exception ignored) {}

    // Directly call selectSpecificDate for return - it will try to open the calendar and set the date
    flights.selectSpecificDate(2025, 12, 8);

    // 5. Click Search
    flights.clickSearch();

    // 6. Collect and print cheapest two flights
    results.printCheapestTwo();

    // 7. Open Google tab as required by the assignment
    driver.switchTo().newWindow(WindowType.TAB);
    driver.get("https://www.google.com");
    System.out.println("Opened Google in new tab successfully!");
}



    @AfterClass
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}
