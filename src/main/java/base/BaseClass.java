package base;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

public class BaseClass {

    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static Properties prop = new Properties();
    private static Logger logger;

    // Static initializer for loading properties and setting up the logger
    static {
        loadProperties();
        setupLogger();
    }

    private static void loadProperties() {
        try (FileInputStream fis = new FileInputStream("Properties/application.properties")) {
            prop.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties", e);
        }
    }

    private static void setupLogger() {
        logger = Logger.getLogger("_3E");
        PropertyConfigurator.configure("Properties/log4j.properties");
    }

    public static synchronized void startDriver() {
        String browserType = prop.getProperty("Browser").toLowerCase();
        WebDriver localDriver;
        switch (browserType) {
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                localDriver = new FirefoxDriver();
                break;
            case "edge":
                WebDriverManager.edgedriver().setup();
                localDriver = new EdgeDriver();
                break;
            case "chrome":
            default:
                WebDriverManager.chromedriver().setup();
                localDriver = new ChromeDriver();
                break;
        }

        localDriver.manage().window().maximize();
        localDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.set(localDriver);
    }

    public static WebDriver getDriver() {
        if (driver.get() == null) {
            startDriver();
        }
        return driver.get();
    }

    public static Logger getLogger() {
        return logger;
    }

    public static Properties getProperties() {
        return prop;
    }

    public static void stopDriver() {
        WebDriver localDriver = driver.get();
        if (localDriver != null) {
            localDriver.quit();
            driver.remove();
        }
    }
}
