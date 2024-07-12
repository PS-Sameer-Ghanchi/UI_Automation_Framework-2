package bdd.pageObjects;

import utilities.SeleniumMethods;

public class LoginPage extends SeleniumMethods  {
	/*
	private By usernameText = By.xpath("//input[@type='text']");
	private By passwordText = By.xpath("//input[@type='password']");
	private By loginbutton = By.xpath("//button[@id='logout-button']");
	private By HomePage = By.id("listHome");
	*/
	
	public void RMS(String url){
		navigateURL(url);
	}

}