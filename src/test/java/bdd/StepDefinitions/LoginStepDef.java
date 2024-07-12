package bdd.StepDefinitions;

import io.cucumber.java.en.*;
import bdd.pageObjects.LoginPage;
import java.util.Properties;
import base.BaseClass;

public class LoginStepDef {
    private Properties prop;
    public LoginPage loginPageObj;

    public LoginStepDef() {
        // Initialize driver and properties at each step definition instantiation
        this.prop = BaseClass.getProperties();
        this.loginPageObj = new LoginPage();
    }


    @Given("User is on Google1 site")
    public void user_is_on_Google1_site() throws  InterruptedException {
            System.out.println("url:- "+prop.getProperty("URL"));
            loginPageObj.RMS(prop.getProperty("URL"));
            Thread.sleep(2000);
    }
    @Given("User is on Google2 site")
    public void user_is_on_Google2_site() throws  InterruptedException {
            System.out.println("url:- "+prop.getProperty("URL"));
            loginPageObj.RMS(prop.getProperty("URL2"));
            Thread.sleep(8000);
    }
}
