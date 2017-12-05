package test;

import core.managers.DriverManager;
import core.managers.ParallelManager;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.IOException;

public class Hook implements ITestListener {


    @BeforeSuite
    public void beforeSuite() {
        DriverManager.setUpDevice();
    }

    @BeforeTest
    public void beforeTest() throws IOException {
        //2 threads
        System.out.println("before Test: " + Thread.currentThread().getId());
        ParallelManager.setAppiumService(DriverManager.createService());
        ParallelManager.setDriver(DriverManager.createDriver());
    }

    @BeforeClass
    public void beforeClass() throws IOException {

    }

    @BeforeMethod
    public void beforeMethod() {
        //2 threads
    }

    @AfterTest
    public void afterTest() {
        //2 threads
    }

    @AfterSuite
    public void afterSuite() {
        //1 thread (main)
    }


    @AfterClass
    public void afterClass() {
        ParallelManager.stopAppiumService();
        System.out.println("Appium running, right?: "+ParallelManager.getAppiumService().isRunning());

    }

    @AfterMethod
    public void afterMethod() {
        //2 thread
        DriverManager.freeDevice();
    }

    @Override
    public void onTestStart(ITestResult iTestResult) {

    }

    @Override
    public void onTestSuccess(ITestResult iTestResult) {

    }

    @Override
    public void onTestFailure(ITestResult iTestResult) {

    }

    @Override
    public void onTestSkipped(ITestResult iTestResult) {

    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {

    }

    @Override
    public void onStart(ITestContext iTestContext) {

    }

    @Override
    public void onFinish(ITestContext iTestContext) {

    }
}
