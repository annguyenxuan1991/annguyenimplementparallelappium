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
import org.openqa.selenium.remote.service.DriverService;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

public class DriverManager {

    private static Logger logger = Logger.getLogger(DriverManager.class);

    private DriverService service = null;

    private String deviceID;

    private int generateRandomPort(int min, int max) {
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }


    public DriverService createService() throws MalformedURLException {
        //Random appium port from 1000 to 9999
        int port = generateRandomPort(1000, 9999);

        logger.info("Creating Appium service with IP address/port: " + ConfigPath.APPIUM_IPADDRESS + ":" + port);
        service = new AppiumServiceBuilder()
                .usingDriverExecutable(new File(ConfigPath.NODEJS_PATH))
                .withAppiumJS(new File(ConfigPath.APPIUM_PATH))
                .withIPAddress(ConfigPath.APPIUM_IPADDRESS)
                .usingPort(port)
                .withArgument(Arg.TIMEOUT, "120")
                .withArgument(Arg.LOG_LEVEL, "warn")
                .build();
        return service;
    }

    public synchronized DesiredCapabilities getCapsAndroidDevice() {
        getAvailableDevice();
        logger.info("Creating driver caps for device: "+deviceID);
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("version", new ADB(deviceID).getAndroidVersion());
        caps.setCapability("deviceName", deviceID);
        caps.setCapability("platformName", "Android");
        caps.setCapability("appPackage", "org.zwanoo.android.speedtest");
        caps.setCapability("appActivity", "com.ookla.speedtest.softfacade.MainActivity");
        return caps;
    }

    public Object createDriver() {
        if (new ADB(deviceID).getOSName().equals("Android")) {
            return new AndroidDriver((AppiumDriverLocalService) ParallelManager.getAppiumService(), getCapsAndroidDevice());
        }
        return null;
    }

    private synchronized void getAvailableDevice() {
        ArrayList<String> devices = getConntectedDevices();
        for(String device : devices){
            try{
                deviceID = device;
                if(useDevice(deviceID)){
                    queueUp();
                    gracePeriod();
                    leaveQueue();
                    break;
                }
            }catch (Exception e){
                e.printStackTrace();
                //Ignore and try next device
            }
        }
    }

    private ArrayList<String> getConntectedDevices(){
        logger.info("Checking for available devices");
        ArrayList<String> availableDevices = new ArrayList<>();
        ArrayList connectedDevices = ADB.getConnectedDevices();
        for(Object connectedDevice: connectedDevices){
            String device = connectedDevice.toString();
            availableDevices.add(device);
        }
        if(availableDevices.size() == 0) throw new RuntimeException("Not a single device is available for testing at this time");
        return availableDevices;
    }

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
        try {
            JSONObject jsonQueue = Resources.getQueue();
            jsonQueue.remove(deviceID);
            ServerManager.writeFile(new File(Resources.QUEUE), jsonQueue.toString());
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
