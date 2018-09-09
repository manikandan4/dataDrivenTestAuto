package com.drivers;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
//import org.testng.*;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CreateDriver {
    private static final int IMPLICT_TIMEOUT = 0;
    private static CreateDriver instance = null;
    private String browserHandle = null;
    private ThreadLocal<WebDriver> webDriver = new ThreadLocal<WebDriver>();
    private ThreadLocal<AppiumDriver<MobileElement>> mobileDriver = new ThreadLocal<AppiumDriver<MobileElement>>();
    private ThreadLocal<String> sessionId = new ThreadLocal<String>();
    private ThreadLocal<String> sessionBrowser = new ThreadLocal<String>();
    private ThreadLocal<String> sessionPlatform = new ThreadLocal<String>();
    private ThreadLocal<String> sessionVersion = new ThreadLocal<String>();
    private String getEvn = null;

    private CreateDriver() {

    }

    private static CreateDriver getInstance() {
        if (instance == null) {
            instance = new CreateDriver();
        }
        return instance;
    }

    @SafeVarargs
    public final void setDriver(String browser,
                                String platform,
                                String environment,
                                Map<String, Object>... optPreferences) throws Exception {
        DesiredCapabilities caps = null;
        String localHub = "http://127.0.0.1:4723/wd/hub";
        String getPlatform = null;

        switch (browser) {
            case "firefox":
                FirefoxProfile ffProfile = new FirefoxProfile();
                ffProfile.setPreference("browser.autofocus", true);

                if(  optPreferences.length > 0){
                    processFFProfile(ffProfile,optPreferences);
                }

                caps = DesiredCapabilities.firefox();
                caps.setCapability(FirefoxDriver.PROFILE, ffProfile);
                caps.setCapability("marionette", true);

                FirefoxOptions ffOpts = new FirefoxOptions();
                webDriver.set(new FirefoxDriver(ffOpts.merge(caps)));

                System.setProperty("webdriver.gecko.driver","/usr/local/bin/geckodriver");

                break;
            case "chrome":
                Map<String,Object> ChromePrefs = new HashMap<String,Object>();
                ChromePrefs.put("credentials_enable_service",false);

                ChromeOptions chOptions = new ChromeOptions();
                chOptions.setExperimentalOption("prefs",ChromePrefs);
                chOptions.addArguments( "--disable-plugins",
                                        "--disable-extensions",
                                        "--disable-popup-blocking",
                                        "--disable-gpu",
                                        "--no-sandbox",
                                        "--allow-insecure-localhost",
                                        "window-size=1280,960");
                if(  optPreferences.length > 0){
                    processchOptions(chOptions,optPreferences);
                }

                caps = DesiredCapabilities.chrome();
                caps.setCapability(ChromeOptions.CAPABILITY,chOptions);
                caps.setCapability("applicationCacheEnabled",false);

                webDriver.set(new ChromeDriver(chOptions.merge(caps)));
                System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
                break;
            case "iphone":
            case "ipad":
                if (browser.equalsIgnoreCase("ipad")) {
                    caps = DesiredCapabilities.ipad();
                } else {
                    caps = DesiredCapabilities.iphone();
                }
                caps.setCapability("device","iPhone");
                caps.setCapability("udid","123456789");// physical device
                caps.setCapability("appName","https://myapp.com/myApp.zip");

                mobileDriver.set(new IOSDriver<MobileElement>(new URL(localHub), caps));

                break;
            case "android":
                caps = DesiredCapabilities.android();
                caps.setCapability("device","Android");
                caps.setCapability("udid","123456789");// physical device
                caps.setCapability("appName","https://myapp.com/myApp.apk");
                mobileDriver.set(new AndroidDriver<MobileElement>(new URL(localHub), caps));
                break;
        }
        getEvn = "local";
        getPlatform = platform;

        if (browser.equalsIgnoreCase("iphone") ||
                browser.equalsIgnoreCase("ipad")) {
            sessionId.set(((IOSDriver<MobileElement>) mobileDriver.get()).getSessionId().toString());
            sessionBrowser.set(browser);
            sessionPlatform.set(platform);
            sessionVersion.set(caps.getCapability("deviceName").toString()); // ??
        } else if (browser.equalsIgnoreCase("android")) {
            sessionId.set(((AndroidDriver<MobileElement>) mobileDriver.get()).getSessionId().toString());
            sessionBrowser.set(browser);
            sessionPlatform.set(platform);
            sessionVersion.set(caps.getCapability("deviceName").toString()); // ??
        } else {
            sessionId.set(((RemoteWebDriver) webDriver.get()).getSessionId().toString());
            sessionVersion.set(caps.getVersion());
            sessionBrowser.set(browser);
            sessionPlatform.set(platform);
        }
    }

    private void processchOptions(ChromeOptions chOptions, Map<String,Object>[] optPreferences) {
    }

    private void processFFProfile(FirefoxProfile ffProfile, Map<String,Object>[] optPreferences) {
    }


    /**
     * Overloaded setDriver method to switch drier to a specific Webdriver,in case of concurrent drivers running
     *
     * @param driver WebDriver instance to switch to
     */
    public void setDriver(WebDriver driver) {
        webDriver.set(driver);
        sessionId.set(((RemoteWebDriver) webDriver.get()).getSessionId().toString());
        sessionBrowser.set(((RemoteWebDriver) webDriver.get()).getCapabilities().getBrowserName());
        sessionPlatform.set(((RemoteWebDriver) webDriver.get()).getCapabilities().getPlatform().toString());
        //setBrowserHandle(getDriver().getWindowsHandle());
    }

    /**
     * Overloaded setDriver method to switch drier to a specific AppiumDriver,in case of concurrent drivers running
     *
     * @param driver AppiumDriver instance to switch to
     */
    public void setDriver(AppiumDriver<MobileElement> driver) {
        mobileDriver.set(driver);
        sessionId.set(mobileDriver.get().getSessionId().toString());
        sessionBrowser.set(mobileDriver.get().getCapabilities().getBrowserName());
        sessionPlatform.set(mobileDriver.get().getCapabilities().getPlatform().toString());
    }

    public void setBrowserHandle() {
    }

    public void getBrowserHandle() {
    }

    //getters
    public WebDriver getDriver() {
        return webDriver.get();
    }

    public AppiumDriver<MobileElement> getDriver(boolean mobile) {
        return mobileDriver.get();
    }

    public WebDriver getCurrentDriver() {
        if (getInstance().getSessionBrowser().contains("iphone") ||
                getInstance().getSessionBrowser().contains("iphone") ||
                getInstance().getSessionBrowser().contains("iphone")) {
            return getDriver(true);
        } else {
            return getDriver();
        }
    }

    public String getSessionId() {
        return sessionId.get();
    }

    public String getSessionBrowser() {
        return sessionBrowser.get();
    }

    public String getSessionVersion() {
        return sessionVersion.get();
    }

    public String getSessionPlatform() {
        return sessionPlatform.get();
    }

    //other driver actions
    public void driverWait(long seconds) {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(seconds));
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    public void driverRefresh() {
        getCurrentDriver().navigate().refresh();
    }

    public void closerDriver() {
        try {
            getCurrentDriver().close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
