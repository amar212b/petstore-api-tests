package com.petstore.test.extent;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ExtentReportListener implements ITestListener {
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    @Override
    public void onStart(ITestContext context) {
        ExtentSparkReporter spark = new ExtentSparkReporter("target/extent-reports/extent-report.html");
        spark.config().setReportName("Petstore API Test Report");
        spark.config().setDocumentTitle("Petstore API Tests");
        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Environment", System.getProperty("env", "dev"));
        extent.setSystemInfo("Thread Count", String.valueOf(context.getSuite().getXmlSuite().getThreadCount()));
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest extentTest = extent.createTest(result.getMethod().getMethodName());
        extentTest.assignCategory(result.getTestClass().getName());
        extentTest.info("Thread-" + Thread.currentThread().getId() + ": Starting test");
        test.set(extentTest);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        test.get().pass("Test passed");
        test.get().info("Thread-" + Thread.currentThread().getId() + ": Completed successfully");
        logResponseTimes(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        test.get().fail(result.getThrowable());
        test.get().info("Thread-" + Thread.currentThread().getId() + ": Failed");
        logResponseTimes(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        test.get().skip(result.getThrowable());

        test.get().info("Thread-" + Thread.currentThread().getId() + ": Skipped");
        logResponseTimes(result);
    }

    private void logResponseTimes(ITestResult result) {
        // Placeholder for response times; actual times logged via SLF4J in TestSuite
        test.get().info("Check logs/petstore-tests.log for API response times");
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }
}