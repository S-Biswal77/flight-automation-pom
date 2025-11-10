# ✈️ Flight Automation Framework (Java + Selenium + Maven)

A **Page Object Model (POM)** based UI automation framework built using **Java**, **Selenium WebDriver**, **TestNG**, and **Maven**.  
It automates flight search flows on a travel booking site (Cleartrip) — including departure and return date selection, and fetching the **cheapest & second-cheapest** flight results.

---

## 🚀 Features

✅ Built using **Java + Maven + TestNG + Selenium 4**  
✅ Implements the **Page Object Model (POM)** design pattern  
✅ Uses **WebDriverManager** for automatic browser driver setup  
✅ Supports **configurable selectors** via a `config.properties` file  
✅ Takes **screenshots** during runtime for debugging  
✅ Prints the **cheapest and second-cheapest** flights from search results  
✅ Demonstrates **tab switching** (opens Google in a new tab as part of test flow)  

---

## 🧩 Tech Stack

| Category | Tools |
|-----------|-------|
| Language  | Java 21 |
| Build Tool | Maven 3.9+ |
| Framework | Selenium WebDriver, TestNG |
| Design Pattern | Page Object Model (POM) |
| IDE | VS Code 
| Reporting | Console + Logs + Screenshots |

---



## 🧱 Project Structure

flight-automation-pom/
│
├── pom.xml # Maven dependencies & plugins
├── config.properties # App selectors & config data
├── .gitignore
├── src/
│ ├── main/java/com/example/framework/
│ │ ├── drivers/ # WebDriver setup
│ │ ├── pages/ # Page classes (Home, Flights, Results)
│ │ └── utils/ # ConfigReader, WaitUtils, ScreenshotUtils
│ └── test/java/com/example/tests/
│ └── FlightSearchTest.java # Main test flow
│
└── target/ # Maven build output (ignored in Git)


---

## 🧠 Test Flow Overview

1. Launch the travel website  
2. Navigate to **Flights** section  
3. Enter source and destination  
4. Select **departure date (next month)**  
5. Select **return date (8th Dec)**  
6. Click **Search**  
7. Extract all flight prices and identify the **cheapest two**  
8. Open a new tab and go to **Google.com**  
9. Print test results in console  

---

## ⚙️ Setup & Run

### Prerequisites
- Install **Java 21+**
- Install **Maven 3.9+**
- Ensure **Chrome** is installed

### Commands
```bash
# Clean and build project
mvn clean compile

# Run tests
mvn clean test


