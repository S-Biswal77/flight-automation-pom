package com.example.framework.pages;

import com.example.framework.utils.ConfigReader;
import com.example.framework.utils.WaitUtils;
import org.openqa.selenium.*;
import java.util.*;
import java.util.stream.Collectors;

public class SearchResultsPage {
    private WebDriver driver;
    private WaitUtils wait;

    public SearchResultsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
    }

    private By get(String key) {
        String raw = ConfigReader.get(key);
        if (raw.startsWith("css:")) return By.cssSelector(raw.substring(4));
        if (raw.startsWith("xpath:")) return By.xpath(raw.substring(6));
        return By.cssSelector(raw);
    }

    public List<Map<String, String>> getFlights() {
        wait.waitForPageLoad();
        List<WebElement> results = driver.findElements(get("selector.resultContainer"));
        List<Map<String, String>> flights = new ArrayList<>();

        for (WebElement r : results) {
            try {
                Map<String, String> f = new HashMap<>();
                f.put("airline", r.findElement(get("selector.resultAirline")).getText());
                f.put("depart", r.findElement(get("selector.resultDepart")).getText());
                f.put("arrive", r.findElement(get("selector.resultArrive")).getText());
                f.put("price", r.findElement(get("selector.resultPrice")).getText().replaceAll("[^0-9]", ""));
                flights.add(f);
            } catch (Exception ignored) {}
        }
        return flights;
    }

    public void printCheapestTwo() {
        List<Map<String, String>> flights = getFlights();
        flights.sort(Comparator.comparingInt(f -> Integer.parseInt(f.get("price"))));

        System.out.println("\n=== Cheapest Flights ===");
        for (int i = 0; i < Math.min(2, flights.size()); i++) {
            Map<String, String> f = flights.get(i);
            System.out.println((i + 1) + ". " + f.get("airline") + " | " + f.get("depart") + "-" + f.get("arrive") + " | ₹" + f.get("price"));
        }
    }
}
