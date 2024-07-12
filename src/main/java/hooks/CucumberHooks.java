package hooks;

import base.BaseClass;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.BeforeStep;
import java.io.IOException;


public class CucumberHooks extends BaseClass {

    @Before
    public void beforeScenario() throws IOException {
		System.out.println("Driver started");
    	getDriver();
    }

    @After
    public void afterScenario() {
		System.out.println("Driver stopped");
        stopDriver();
    }

	@BeforeStep
	public void beforeStep() {
		System.out.println("Before Step");
	}
	
	@AfterStep
	public void afterStep() {
		System.out.println("After Step");
	}

	@BeforeAll
	public static void init() {
		System.out.println("Test Suite Setup");
	}

	@AfterAll
	public static void cleanup() {
		System.out.println("Test Suite Cleanup");
	}

	@Before()
	public void beforeLoginScenario() {
		System.out.println("Runs only before scenarios tagged with @login");
	}


}
