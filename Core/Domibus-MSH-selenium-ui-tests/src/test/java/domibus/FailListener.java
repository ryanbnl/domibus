package domibus;

import domibus.ui.SeleniumTest;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FailListener implements ITestListener {

	static int test_count = 0;
	static int passed_count = 0;
	static int failed_count = 0;
	static int skipped_count = 0;
	static int total_test_count = 0;

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public void onStart(ITestContext context) {
		total_test_count = context.getSuite().getAllMethods().size();
		log.info("Tests methods to run - " + total_test_count);
	}

	@Override
	public void onFinish(ITestContext iTestContext) {

	}

	@Override
	public void onTestStart(ITestResult iTestResult) {
		ITestContext context = iTestResult.getTestContext();
		total_test_count = context.getSuite().getAllMethods().size();
		log.info("Tests methods to run - " + total_test_count);
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		test_count++;
		passed_count++;
		logTestCounts();
	}

	@Override
	public void onTestFailure(ITestResult result) {
		try {
			result.getThrowable().printStackTrace();
		} catch (Exception e) {
		}

		takeScreenshot(result);
		test_count++;
		failed_count++;
		logTestCounts();
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		try {
			result.getThrowable().printStackTrace();
		} catch (Exception e) {
		}
		takeScreenshot(result);
		test_count++;
		skipped_count++;
		logTestCounts();
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {

	}

	private void takeScreenshot(ITestResult result) {
		String time = new SimpleDateFormat("dd-MM_HH-mm-ss").format(Calendar.getInstance().getTime());
		String testMeth = result.getName();
		String className = result.getTestClass().getRealClass().getSimpleName();
		String outputPath = ((SeleniumTest) result.getInstance()).data.getReportsFolder();
		String filename = String.format("%s%s_%s_%s.png", outputPath, className, testMeth, time);

		try {
			WebDriver driver = ((SeleniumTest) result.getInstance()).driver;
			((SeleniumTest) result.getInstance()).log.info("copying screenshot to " + filename);
			TakesScreenshot scrShot = ((TakesScreenshot) driver);
			File srcFile = scrShot.getScreenshotAs(OutputType.FILE);
			FileUtils.copyFile(srcFile, new File(filename));
		} catch (Exception e) {
			log.error("EXCEPTION: ", e);
		}
	}


	private void logTestCounts() {
		log.info(String.format("-------- Passed - %s --------", passed_count));
		log.info(String.format("-------- Failed - %s --------", failed_count));
		log.info(String.format("-------- Skipped - %s --------", skipped_count));
		log.info(String.format("-------- Ran %s tests out of %s --------", test_count, total_test_count));
	}


}
