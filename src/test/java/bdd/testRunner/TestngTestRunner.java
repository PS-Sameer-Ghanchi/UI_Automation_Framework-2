package bdd.testRunner;
import io.cucumber.junit.Cucumber;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src\\test\\resources\\features",
    //tags = "@login","@Home",
    glue = {"bdd.StepDefinitions", "hooks"},
    monochrome = true,
    plugin = {
        "pretty",
        "html:target/cucumber.html",
        "json:target/cucumber.json",
        "junit:target/cucumber-results.xml",
        "utilities.ExtentReportListener"
    }
)
public class TestngTestRunner extends AbstractTestNGCucumberTests {
    
}
