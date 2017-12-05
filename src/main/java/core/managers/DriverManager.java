package core.managers;

import core.ADB;
import core.Timer;
import core.constants.Arg;
import core.constants.ConfigPath;
import core.constants.Resources;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DriverManager {

    private static Logger logger = Logger.getLogger(DriverManager.class);

    private static ConcurrentHashMap<String, Boolean> dataMapping = ParallelManager.dataMapping;

    private static int generateRandomPort(int min, int max) {
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }


    public synchronized static AppiumDriverLocalService createService() throws MalformedURLException {
        setAvailableDevice();
        //Random appium port from 1000 to 9999
        int port = generateRandomPort(1000, 9999);

        logger.info("Creating Appium service with IP address/port: " + ConfigPath.APPIUM_IPADDRESS + ":" + port);
        AppiumDriverLocalService service = new AppiumServiceBuilder()
                .usingDriverExecutable(new File(ConfigPath.NODEJS_PATH))
                .withAppiumJS(new File(ConfigPath.APPIUM_PATH))
                .withIPAddress(ConfigPath.APPIUM_IPADDRESS).usingPort(port)
                .withArgument(Arg.TIMEOUT, "120")
                .withArgument(Arg.LOG_LEVEL, "warn")
                .build();
        service.start();
        return service;
    }

    private static synchronized DesiredCapabilities getCapsAndroidDevice() {
        String deviceID = ParallelManager.getDeviceID();
        logger.info("Creating driver caps for device: "+deviceID);
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("version", new ADB(deviceID).getAndroidVersion());
        caps.setCapability("deviceName", deviceID);
        caps.setCapability("platformName", "Android");
        caps.setCapability("appPackage", "org.zwanoo.android.speedtest");
        caps.setCapability("appActivity", "com.ookla.speedtest.softfacade.MainActivity");
        return caps;
    }

    public static Object createDriver() {
        String deviceID = ParallelManager.getDeviceID();
        if (true) {
            return new AndroidDriver(ParallelManager.getAppiumService(), getCapsAndroidDevice());
        } else if(new ADB(deviceID).getOSName().contains("iOS"))
            return null;
        return null;
    }

    private static synchronized void setAvailableDevice() {
        ConcurrentHashMap<String, String> dataMappingThread = ParallelManager.dataMappingThread;
        ConcurrentHashMap.KeySetView<String, Boolean> dataIDList = dataMapping.keySet();

        String threadId = String.valueOf(Thread.currentThread().getId());
        for (String dataID : dataIDList) {
            if (dataMapping.get(dataID)
                    && (dataMappingThread.get(dataID) == null
                    || dataMappingThread.get(dataID).equals(threadId))) {
                dataMapping.put(dataID, false);
                dataMappingThread.put(dataID, threadId);

                ParallelManager.setDeviceID(dataID);
            }
        }
    }

    public static void setUpDevice() {
        ArrayList<String> connectedDevices = getConntectedDevices();
        for(String device: connectedDevices) {
            dataMapping.put(device, true);
        }
    }

    public static void freeDevice()
    {
        String deviceID = ParallelManager.getDeviceID();
        dataMapping.put(deviceID, true);
    }

    private static ArrayList<String> getConntectedDevices(){
        logger.info("Checking for available devices");
        ArrayList<String> availableDevices = new ArrayList<>();
        ArrayList connectedDevices = ADB.getConnectedDevices();
        for(Object connectedDevice: connectedDevices){
            String device = connectedDevice.toString();
            availableDevices.add(device);
        }
        if(availableDevices.size() == 0) throw new RuntimeException("Not a single device is connected for testing at this time");
        return availableDevices;
    }


    /*    private synchronized void getAvailableDevice() {
        ArrayList<String> devices = getConntectedDevices();
        for(String device : devices){
                if(useDevice(device)){
                    deviceID = device;
                    queueUp();
                    gracePeriod();
                    leaveQueue();
                    break;
                }
        }
    }*/

    private boolean useDevice(String deviceID) {
        JSONObject json = Resources.getQueue();
        if(json.containsKey(deviceID)){
            JSONObject deviceJson = (JSONObject) json.get(deviceID);
            long time = (long) deviceJson.get("queued_at");
            int diff = Timer.getDifference(time, Timer.getTimeStamp());
            if(diff >= 30) return true;
            else return false;
        } else return true;
    }

    private void queueUp() {
        try {
            String deviceID = ParallelManager.getDeviceID();
            logger.info("Queueing Up: "+deviceID);
            JSONObject json = new JSONObject();
            json.put("queued_at", Timer.getTimeStamp());
            JSONObject jsonQueue = Resources.getQueue();
            jsonQueue.put(deviceID, json);
            logger.info("JSON Queue: "+jsonQueue);
            ServerManager.writeFile(new File(Resources.QUEUE), jsonQueue.toString());
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
    private void gracePeriod(){
        String deviceID = ParallelManager.getDeviceID();
        int waitTime = 0;
        try {
            JSONObject  json = Resources.getQueue();
            Set keys = json.keySet();

            JSONObject ourDeviceJson = (JSONObject) json.get(deviceID);
            json.remove(deviceID);
            long weQueuedAt = (long) ourDeviceJson.get("queued_at");

            for(Object key : keys){
                JSONObject deviceJson = (JSONObject) json.get(key);
                long theyQueuedAt = (long) deviceJson.get("queued_at");
                //If we did not queue first we need to wait for the other device to initialize driver so there is no collision
                if(weQueuedAt > theyQueuedAt) {
                    //Be queued first and recently, otherwise we can assume device was already initialized or no longer being used
                    int diff = Timer.getDifference(theyQueuedAt, Timer.getTimeStamp());
                    if(diff < 50){
                        logger.info("Device: "+key+" queued first, I will need to give it extra time to initialize");
                        waitTime += 15;
                    }
                }
            }
            try {Thread.sleep(waitTime);} catch (InterruptedException e) {e.printStackTrace();}
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    private void leaveQueue(){
        String deviceID = ParallelManager.getDeviceID();
        try {
            JSONObject jsonQueue = Resources.getQueue();
            jsonQueue.remove(deviceID);
            ServerManager.writeFile(new File(Resources.QUEUE), jsonQueue.toString());
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
