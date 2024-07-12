package utilities;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
 
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestStepFinished;
 
public class ExtentReportListener implements ConcurrentEventListener {
    private ExtentHtmlReporter htmlReporter;
    private ExtentReports extent;
    private ExtentTest test;
 
    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestRunStarted.class, this::onTestRunStarted);
        publisher.registerHandlerFor(TestCaseStarted.class, this::onTestCaseStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::onTestStepFinished);
        publisher.registerHandlerFor(TestRunFinished.class, this::onTestRunFinished);
        // More event handlers can be registered as needed
    }
 
    private void onTestRunStarted(TestRunStarted event) {
        htmlReporter = new ExtentHtmlReporter(".//target/extentReport.html");
        htmlReporter.config().setTheme(Theme.DARK);
        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);
    }
 
    private void onTestCaseStarted(TestCaseStarted event) {
        test = extent.createTest(event.getTestCase().getName());
    }
 
    private void onTestStepFinished(TestStepFinished event) {
        // Capture results of each step
        if (event.getResult().getStatus().equals(Status.PASSED)) {
            test.pass("Details");
        } else if (event.getResult().getStatus().equals(Status.FAILED)) {
            test.fail("Details");
        }
        // Add more conditions as needed
    }
 
    private void onTestRunFinished(TestRunFinished event) {
        extent.flush();
    }
 
    // Additional methods as required
}