package core.managers;


import io.appium.java_client.service.local.AppiumDriverLocalService;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ParallelManager {

    private static Logger logger = Logger.getLogger(ParallelManager.class);

    private static Map<Long, Object> driverPool = new HashMap<>();
    private static Map<Long, AppiumDriverLocalService> servicePool = new HashMap<>();
    public static ConcurrentHashMap<String, Boolean> dataMapping = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> dataMappingThread = new ConcurrentHashMap<>();
    private static Map<Long, String> deviceIDPool = new HashMap<>();


    public static void setDriver(Object driver) {
        driverPool.put(Thread.currentThread().getId(), driver);
    }

    public static Object getDriver() {
        return driverPool.get(Thread.currentThread().getId());
    }


    public synchronized static void setAppiumService(AppiumDriverLocalService service) {
        servicePool.put(Thread.currentThread().getId(), service);
    }

    public synchronized static AppiumDriverLocalService getAppiumService() {
        return servicePool.get(Thread.currentThread().getId());
    }

    public synchronized static void stopAppiumService() {
        logger.info("Stop appium service...");
        servicePool.get(Thread.currentThread().getId()).stop();
    }

    public static void setDeviceID(String deviceId) {
        deviceIDPool.put(Thread.currentThread().getId(), deviceId);
    }

    public static String getDeviceID() {
        return deviceIDPool.get(Thread.currentThread().getId());
    }
}
