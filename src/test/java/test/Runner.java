package test;

import core.managers.ParallelManager;
import io.appium.java_client.android.AndroidDriver;
import org.testng.annotations.Test;

public class Runner extends Hook {

    @Test
    public void firstTest() {
        AndroidDriver androidDriver = (AndroidDriver) ParallelManager.getDriver();
        System.out.println("First Test: " + Thread.currentThread().getId());
        System.out.println("Appium running, right?: "+ParallelManager.getAppiumService().isRunning());
        androidDriver.quit();
    }

}
