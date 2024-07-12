package bdd.StepDefinitions;

import io.cucumber.java.en.*;
import bdd.pageObjects.LoginPage;
import java.util.Properties;
import base.BaseClass;

public class HomeStepDef {
    private Properties prop;
    public LoginPage loginPageObj;

    public HomeStepDef() {
        // Initialize driver and properties at each step definition instantiation
        this.prop = BaseClass.getProperties();
        this.loginPageObj = new LoginPage();
    }


    @Given("User is on home site")
    public void user_is_on_Google1_site() throws  InterruptedException {
            System.out.println("url:- "+prop.getProperty("URL1"));
            loginPageObj.RMS(prop.getProperty("URL1"));
            Thread.sleep(2000);
    }
}
