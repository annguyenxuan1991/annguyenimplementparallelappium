package core.managers;


import org.openqa.selenium.remote.service.DriverService;

import java.util.HashMap;
import java.util.Map;

public class ParallelManager {
    private Map<String, Object> driverPool = new HashMap<String, Object>();
    private Map<String, DriverService> servicePool = new HashMap<String, DriverService>();


    public void setDriver(Object driver) {
        driverPool.put(Thread.currentThread().getName(), driver);
    }

    public Object getDriver() {
        return driverPool.get(Thread.currentThread().getName());
    }

    public void setAppiumService(DriverService service) {
        servicePool.put(Thread.currentThread().getName(), service);
    }

    public DriverService getAppiumService() {
        return servicePool.get(Thread.currentThread().getName());
    }
}
