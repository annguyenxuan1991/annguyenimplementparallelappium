package test;

import core.managers.DriverManager;
import core.managers.ParallelManager;
import org.testng.annotations.*;

import java.io.IOException;

public class Hook {


    @BeforeSuite
    public void beforeSuite() {


    }

    @BeforeTest
    public void beforeTest() throws IOException {
        //2 threads
        System.out.println("before Test: " + Thread.currentThread().getId());
        ParallelManager.setAppiumService(new DriverManager().createService());
        ParallelManager.startAppiumService();
        ParallelManager.setDriver(new DriverManager().createDriver());
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
    }
}
