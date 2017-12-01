package core.managers;


import org.apache.log4j.Logger;
import org.openqa.selenium.remote.service.DriverService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ParallelManager {

    private static Logger logger = Logger.getLogger(ParallelManager.class);

    private static Map<Long, Object> driverPool = new HashMap<>();
    private static Map<Long, DriverService> servicePool = new HashMap<>();


    public static void setDriver(Object driver) {
        driverPool.put(Thread.currentThread().getId(), driver);
    }

    public static Object getDriver() {
        return driverPool.get(Thread.currentThread().getId());
    }


    public static void setAppiumService(DriverService service) {
        servicePool.put(Thread.currentThread().getId(), service);
    }

    public static DriverService getAppiumService() {
        return servicePool.get(Thread.currentThread().getId());
    }

    public static void startAppiumService() throws IOException {
        logger.info("Start appium service...");
        servicePool.get(Thread.currentThread().getId()).start();
        if(!servicePool.get(Thread.currentThread().getId()).isRunning())
            logger.error("Start appium service failed");
    }

    public static void stopAppiumService() {
        logger.info("Stop appium service...");
        servicePool.get(Thread.currentThread().getId()).stop();
    }
}
