package utilities;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import base.BaseClass;


public class SeleniumMethods extends BaseClass{

	public StringSelection stringSelection;
	public Clipboard clipboard;
	// Define objects
	public WebDriver driver;
	private WebDriverWait wait;

	// Declare objects
	public SeleniumMethods() {
        this.driver = getDriver();  // Fetch the thread-local driver instance from the base class
    }

	// Handle click action
	public void clickOn(By locator) {
		WebElement el = driver.findElement(locator);
		el.click();
	}

	// Handle send keys action
	public void sendKeys(By locator, String str) {
		WebElement el = driver.findElement((locator));
		el.clear();
		el.sendKeys(str);
	}

	// Store text from a locatorl
	public String getText(By locator) {
		String text = driver.findElement(locator).getText();
		return text;
	}

	public void verifyTitle(String title) {
		String actualTitle = driver.getTitle();
		Assert.assertTrue(actualTitle.contains(title));
	}

	public void mouseHover(By locator) {
		WebElement el = driver.findElement(locator);
		Actions action = new Actions(driver);
		action.moveToElement(el).build().perform();
	}

	public void navigateURL(String url) {
		System.out.println(url);
		driver.navigate().to(url);
	}

	// to compare two lists
	public boolean verifyListOptions(By locator, List<String> al) {
		List<WebElement> list = driver.findElements(locator);
		ArrayList<String> arrayList = new ArrayList<String>();
		for (WebElement e : list) {
			String text = e.getText();
			arrayList.add(text);
		}
		Collections.sort(arrayList);
		Collections.sort(al);
		boolean bool = arrayList.equals(al);
		return bool;
	}

	// Wait for element to be visible
	public WebElement waitForElementVisible(By locator, int timeoutInSeconds) {
		wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
		return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
	}

	// Wait for element to be clickable
	public WebElement waitForElementClickable(By locator, int timeoutInSeconds) {
		wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
		return wait.until(ExpectedConditions.elementToBeClickable(locator));
	}

	// Navigate to URL
	public void navigateTo(String url) {
		driver.get(url);
	}

	// Navigate back
	public void navigateBack() {
		driver.navigate().back();
	}

	// Navigate forward
	public void navigateForward() {
		driver.navigate().forward();
	}

	// Refresh page
	public void refreshPage() {
		driver.navigate().refresh();
	}

	// Find element
	public WebElement findElement(By by) {
		return driver.findElement(by);
	}

	// Click on element
	public void clickElement(By by) {
		findElement(by).click();
	}

	// Type text into element
	public void typeText(By by, String text) {
		WebElement element = findElement(by);
		element.clear();
		element.sendKeys(text);
	}

	// Accept alert
	public void acceptAlert() {
		driver.switchTo().alert().accept();
	}

	// Dismiss alert
	public void dismissAlert() {
		driver.switchTo().alert().dismiss();
	}

	// Execute JavaScript
	// public void executeScript(String script, Object... args) {
	// 	((JavascriptExecutor) driver).executeScript(script, args);
	// }

	// Close current window
	public void closeWindow() {
		driver.close();
	}

	// Quit browser session
	public void quitBrowser() {
		driver.quit();
	}

	// Switch to window
	public void switchToWindow(String windowHandle) {
		driver.switchTo().window(windowHandle);
	}

	public void openNewTab(String url) {
		driver.switchTo().newWindow(WindowType.TAB);
		navigateURL(url);
	}
	public void scrollDown() {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("window.scrollBy(0,250)", "");
	}
	// Get all cookies
	// public Set<Cookie> getAllCookies() {
	// 	return driver.manage().getCookies();
	// }

	// Add a cookie
	// public void addCookie(Cookie cookie) {
	// 	driver.manage().addCookie(cookie);
	// }

	// Delete a cookie by name
	public void deleteCookieNamed(String name) {
		driver.manage().deleteCookieNamed(name);
	}

	// Take screenshot
	// public void takeScreenshot(String filePath) {
	// 	try {
	// 		File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
	// 		FileUtils.copyFile(srcFile, new File(filePath));
	// 	} catch (Exception e) {
	// 		e.printStackTrace();
	// 	}
	// }

	// Set implicit wait
	public void setImplicitWait(int timeInSeconds) {
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(timeInSeconds));
	}

}