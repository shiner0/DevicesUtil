package com.rn.devicesinfo;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.input.InputManager;
import android.net.Proxy;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.os.ConfigurationCompat;
import androidx.core.os.LocaleListCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DevicesInfoTools {

    static Map<String, Object> map = new HashMap<>();

    @SuppressLint("MissingPermission")
    public static Map<String, Object> getDevicesInfo(final Activity activity) throws JSONException {
        map.clear();

        map.put("batteryLevelMa", (Double.parseDouble(BatteryTools.getBatteryCapacity(activity)) / 100) * BatteryTools.getSystemBatteryLevel(activity));
        map.put("batteryMaxMa", BatteryTools.getBatteryCapacity(activity) + "mAh");
        map.put("isAcCharge", getBatteryStatus(activity).getString("is_ac_charge"));
        map.put("isUsbCharge", getBatteryStatus(activity).getString("is_usb_charge"));
        map.put("currentSystemTime", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new java.util.Date()));
        map.put("isUsingProxyPort", isWifiProxy(activity) + "");
        map.put("isUsingVpn", isDeviceInVPN() + "");
        map.put("locale_display_language", Locale.getDefault().getLanguage());
        map.put("locale_iso_3_country", getCountry());
        map.put("locale_iso_3_language", getLanguage());
        map.put("sensor_list", getSensorList(activity).toString());
        map.put("keyboard", getKeyboard(activity));

        map.put("albs", "");
        map.put("idfv", "");
        map.put("idfa", "");
        map.put("productionDate", Build.TIME);
        map.put("audio_external", getAudioExternalNumber(activity));
        map.put("audio_internal", getAudioInternalNumber(activity));
        map.put("video_external", getVideoExternalNumber(activity));
        map.put("video_internal", getVideoInternalNumber(activity));
        map.put("images_external", getImagesExternalNumber(activity));
        map.put("images_internal", getImagesInternalNumber(activity));
        map.put("download_files", getDownloadFileNumber());
        map.put("contact_group", getContactsGroupNumber(activity));
        map.put("pic_count", Integer.parseInt((String) map.get("images_external")) + Integer.parseInt((String) map.get("images_internal")) + "");


        map.put("appScreenWidth", AppTools.getScreenWidth(activity) + "");
        map.put("appScreenHeight", AppTools.getScreenHeight(activity) + "");
        map.put("screenDensity", AppTools.getScreenDensity(activity, ""));
        map.put("screenDensityDpi", AppTools.getScreenDensity(activity, "dpi"));
        map.put("fullScreen", AppTools.isFullScreen(activity) + "");
        map.put("landscape", AppTools.isLandscape(activity) + "");

        map.put("lastUpdateTime", SystemTools.getLastUpdateTime(activity));
        map.put("appPath", SystemTools.getAppPath(activity));
        map.put("sha1", SystemTools.getAppSignatureSHA1(activity, "SHA-1"));
        map.put("sha256", SystemTools.getAppSignatureSHA1(activity, "SHA256"));
        map.put("md5", SystemTools.getAppSignatureSHA1(activity, "MD5"));
        map.put("uid", SystemTools.getAppUid(activity) + "");
        map.put("screenWidth", AppTools.getScreenWidths(activity) + "");
        map.put("screenHeight", AppTools.getScreenHeights(activity) + "");
        map.put("debug", SystemTools.isAppDebug(activity) + "");
        map.put("sleepDuration", AppTools.getSleepDuration(activity) + "");
        map.put("autoBrightnessEnabled", AppTools.isAutoBrightnessEnabled(activity) + "");
        map.put("brightness", AppTools.getBrightness(activity) + "");
        map.put("isPhone", AppTools.isPhone(activity) + "");
        map.put("phoneType", AppTools.getPhoneType(activity) + "");
        map.put("simCardReady", AppTools.getSimState(activity) + "");
        map.put("simOperatorName", AppTools.getSimOperatorName(activity));
        map.put("simOperatorByMnc", AppTools.getSimOperator(activity));
        map.put("simCountryIso", AppTools.getSimCountryIso(activity));
        map.put("networkCountryIso", SystemTools.getNetworkCountryIso(activity));
        map.put("systemApp", SystemTools.isSystemApp(activity) + "");
        map.put("foreground", !SystemTools.isAppBackground(activity) + "");
        map.put("running", "true");
        map.put("packageName", SystemTools.getAppPackageName(activity));
        map.put("name", SystemTools.getAppName(activity));
        map.put("versionName", SystemTools.getAppVersionName(activity));
        map.put("versionCode", SystemTools.getAppVersionCode(activity) + "");
        map.put("firstInstallTime", SystemTools.getFirstInstallTime(activity));
        map.put("portrait", AppTools.isPortrait(activity) + "");
        map.put("screenRotation", AppTools.getScreenRotation(activity) + "");
        map.put("screenLock", AppTools.isScreenLock(activity) + "");
        map.put("networkOperator", SystemTools.getNetworkOperator(activity));
        map.put("simSerialNumber", AppTools.getSimSerialNumber(activity));
        map.put("networkOperatorName", SystemTools.getNetworkOperatorName(activity));
        map.put("deviceId", AppTools.getDeviceId(activity));
        map.put("serial", AppTools.getSerial());
        String imei = (AppTools.getIMEIOne(activity) + "," + AppTools.getIMEITwo(activity)).equals(",") ? AppTools.getIMEI(activity) : AppTools.getIMEIOne(activity) + "," + AppTools.getIMEITwo(activity);
        if (TextUtils.equals(",", imei)) {
            imei = "";
        }
        if (!TextUtils.isEmpty(imei)) {
            Log.e("imei", "imei = " + imei);
            if (imei.endsWith(",")) {
                imei = imei.substring(0, imei.length() - 1);
            }

            if (imei.startsWith(",")) {
                imei = imei.substring(1);
            }
        }
        map.put("imei", imei);
        map.put("meid", AppTools.getMEID(activity));
        map.put("imsi", AppTools.getIMSI(activity));
        map.put("board", Build.BOARD);
        map.put("buildId", Build.ID);
        map.put("host", Build.HOST);
        map.put("display", Build.DISPLAY);
        map.put("radioVersion", Build.getRadioVersion());
        map.put("fingerprint", Build.FINGERPRINT);
        map.put("device", Build.DEVICE);
        map.put("product", Build.PRODUCT);
        map.put("type", Build.TYPE);
        map.put("buildUser", Build.USER);
        map.put("cpuAbi", Build.CPU_ABI);
        map.put("cpuAbi2", Build.CPU_ABI2);
        map.put("baseOS", Build.VERSION.BASE_OS);
        map.put("bootloader", Build.BOOTLOADER);
        map.put("brand", Build.BRAND);
        map.put("time", Build.TIME);
        map.put("hardware", Build.HARDWARE);
        map.put("language", AppTools.getCountryByLanguage());
        map.put("country", AppTools.getCountryCodeByLanguage("Default"));
        map.put("sdkVersionName", Build.VERSION.RELEASE);
        map.put("sdkVersionCode", Build.VERSION.SDK_INT + "");
        map.put("androidID", AppTools.getAndroidId(activity));
        map.put("macAddress", AppTools.getMacAddress(activity));
        map.put("manufacturer", AppTools.getBuildMANUFACTURER());
        map.put("model", AppTools.getBuildBrandModel());
        map.put("abis", Arrays.asList(AppTools.getABIs()) + "");
        map.put("isTablet", AppTools.isTablet() + "");
        map.put("isEmulator", AppTools.isEmulator(activity) + "");
        map.put("sameDevice", "true");
        map.put("connected", SystemTools.isNetworkAvailable(activity) + "");
        map.put("mobileDataEnabled", SystemTools.getMobileDataEnabled(activity) + "");
        String type = SystemTools.getNetWorkType(activity);
        map.put("mobileData", (type.equals("NETWORK_2G") || type.equals("NETWORK_3G") || type.equals("NETWORK_4G") || type.equals("NETWORK_5G")) + "");
        map.put("is4G", SystemTools.is4G(activity) + "");
        map.put("is5G", SystemTools.is5G(activity) + "");
        map.put("wifiConnected", SystemTools.isWifiConnected(activity) + "");
        map.put("networkType", SystemTools.getNetWorkType(activity) + "");
        map.put("ipAddress", SystemTools.getIPAddress(true) + "");
        map.put("ipv6Address", SystemTools.getIPAddress(false));
        map.put("ipAddressByWifi", SystemTools.getWifiInfo(activity, "ipAddress"));
        map.put("gatewayByWifi", SystemTools.getWifiInfo(activity, "gateway"));
        map.put("netMaskByWifi", SystemTools.getWifiInfo(activity, "netmask"));
        map.put("serverAddressByWifi", SystemTools.getWifiInfo(activity, "serverAddress"));
        map.put("broadcastIpAddress", SystemTools.getBroadcastIpAddress());
        map.put("ssid", SystemTools.getSSID(activity));
        map.put("root", SystemTools.isAppRoot() + "");
        if (Build.VERSION.SDK_INT >= 17) {
            map.put("adbEnabled", AppTools.isAdbEnabled(activity) + "");
        }
        map.put("sdCardEnableByEnvironment", FileTools.sdCardIsAvailable() + "");
        map.put("sdCardPathByEnvironment", FileTools.getSDCardPath());
        map.put("sdCardInfo", AppTools.getSDCardInfo(activity).toString());
        map.put("mountedSdCardPath", AppTools.getMountedSDCardPath(activity).toString());
        map.put("externalTotalSize", FileTools.byte2FitMemorySize(AppTools.getExternalTotalSize(), 2));
        map.put("externalAvailableSize", FileTools.byte2FitMemorySize(AppTools.getExternalAvailableSize(), 2));
        map.put("internalTotalSize", FileTools.byte2FitMemorySize(AppTools.getInternalTotalSize(), 2));
        map.put("internalAvailableSize", FileTools.byte2FitMemorySize(AppTools.getInternalAvailableSize()));
        map.put("batteryLevel", BatteryTools.getSystemBatteryLevel(activity));
        map.put("batterySum", BatteryTools.getSystemBatterySum(activity));
        map.put("batteryPercent", BatteryTools.getSystemBattery(activity) + "%");
        map.put("percentValue", AppTools.getUsedPercentValue(activity));
        map.put("availableMemory", AppTools.getAvailableMemory(activity));
        map.put("processCpuRate", AppTools.getCurProcessCpuRate());
        map.put("cpuRate", AppTools.getTotalCpuRate());
        map.put("time", SystemTools.getuptime());
        map.put("timezone", SystemTools.getTimezone());
        map.put("gpsEnabled", SystemTools.isGpsEnabled(activity));
        map.put("bootTime", SystemTools.getBoottime());
        map.put("batteryStatus", SystemTools.getBatteryStatus(activity));
        map.put("batterytemp", SystemTools.getBatterytemp(activity));
        map.put("isPlugged", SystemTools.isPlugged(activity));
        map.put("wifiBSSID", SystemTools.getWifiBSSID(activity));
        map.put("arpList", SystemTools.readArp(activity));
        map.put("bluetoothAddress", SystemTools.getBluetoothAddress(activity));
        map.put("countryZipCode", SystemTools.getCountryZipCode(activity));
        map.put("cellLocation", SystemTools.getCellLocation(activity));
        map.put("defaultHost", getDefaultHost());


        map.put("ramTotalSize", AppInfoTools.getRAMTotalMemorySize(activity));
        map.put("ramUsableSize", AppInfoTools.getRAMUsableMemorySize(activity));
        map.put("memoryCardSize", AppInfoTools.getTotalExternalMemorySize() + "");
        map.put("memoryCardUsableSize", AppInfoTools.getAvailableExternalMemorySize() + "");
        map.put("memoryCardSizeUse", AppInfoTools.getTotalExternalMemorySize() - AppInfoTools.getAvailableExternalMemorySize() + "");
        map.put("internalStorageUsable", AppInfoTools.getAvailableInternalMemorySize() + "");
        map.put("internalStorageTotal", AppInfoTools.getTotalInternalMemorySize() + "");
        map.put("network", AppInfoTools.getNetworkData(activity) + "");
        map.put("cpuNum", AppInfoTools.getNumberOfCPUCores());
        map.put("appMaxMemory", AppInfoTools.getAPPMaxMemory(activity) + "M");
        map.put("appAvailableMemory", AppInfoTools.getAPPAvailableMemory(activity) + "M");
        map.put("appFreeMemory", AppInfoTools.getAPPFreeMemory(activity) + "M");
        map.put("physicalSize", AppInfoTools.getScreenSizeOfDevice(activity));
        map.put("totalBootTimeWake", SystemClock.uptimeMillis());
        map.put("totalBootTime", SystemClock.elapsedRealtime());

        map.put("voiceMailNumber", SystemTools.getVoiceMailNumber(activity));
        map.put("available", SystemTools.isAvailable(activity) + "");
        map.put("availableByPing", SystemTools.isAvailableByPing() + "");
        map.put("availableByDns", SystemTools.isAvailableByDns() + "");
        map.put("wifiAvailable", SystemTools.isWifiAvailable(activity) + "");
        map.put("wifiSignal", getWifiRssi(activity) + "");
        map.put("cellularSignal", getMobileDbm(activity) + "");

        return map;
    }

    /**
     * 获取手机信号强度，需添加权限 android.permission.ACCESS_COARSE_LOCATION <br>
     * API要求不低于17 <br>
     *
     * @return 当前手机主卡信号强度, 单位 dBm（-1是默认值，表示获取失败）
     */
    @SuppressLint("MissingPermission")
    public static int getMobileDbm(Context context) {
        int dbm = -1;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        List<CellInfo> cellInfoList;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            cellInfoList = tm.getAllCellInfo();
            if (null != cellInfoList) {
                for (CellInfo cellInfo : cellInfoList) {
                    if (cellInfo instanceof CellInfoGsm) {
                        CellSignalStrengthGsm cellSignalStrengthGsm = ((CellInfoGsm) cellInfo).getCellSignalStrength();
                        dbm = cellSignalStrengthGsm.getDbm();
                    } else if (cellInfo instanceof CellInfoCdma) {
                        CellSignalStrengthCdma cellSignalStrengthCdma =
                                ((CellInfoCdma) cellInfo).getCellSignalStrength();
                        dbm = cellSignalStrengthCdma.getDbm();
                    } else if (cellInfo instanceof CellInfoWcdma) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            CellSignalStrengthWcdma cellSignalStrengthWcdma =
                                    ((CellInfoWcdma) cellInfo).getCellSignalStrength();
                            dbm = cellSignalStrengthWcdma.getDbm();
                        }
                    } else if (cellInfo instanceof CellInfoLte) {
                        CellSignalStrengthLte cellSignalStrengthLte = ((CellInfoLte) cellInfo).getCellSignalStrength();
                        dbm = cellSignalStrengthLte.getDbm();
                    }
                }
            }
        }
        return dbm;
    }

    public static String getDefaultHost() {
        String proHost = "";
        int proPort = 0;
        try {
            proHost = Proxy.getDefaultHost();
            proPort = Proxy.getDefaultPort();
        } catch (Exception var3) {
        }
        return proHost + " " + proPort;
    }

    public static int getWifiRssi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo info = wifiManager.getConnectionInfo();
            if (info != null) {
                return info.getRssi();
            }
        }
        return 0;
    }

    public static String getAudioExternalNumber(Context context) {
        int result = 0;
        Cursor cursor;
        for (cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{"date_added", "date_modified", "duration", "mime_type", "is_music", "year", "is_notification", "is_ringtone", "is_alarm"}, (String) null, (String[]) null, (String) null); cursor != null && cursor.moveToNext(); ++result) {
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return String.valueOf(result);
    }

    public static String getAudioInternalNumber(Context context) {
        int result = 0;

        Cursor cursor;
        for (cursor = context.getContentResolver().query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, new String[]{"date_added", "date_modified", "duration", "mime_type", "is_music", "year", "is_notification", "is_ringtone", "is_alarm"}, (String) null, (String[]) null, "title_key"); cursor != null && cursor.moveToNext(); ++result) {
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return String.valueOf(result);
    }

    public static String getVideoExternalNumber(Context context) {
        int result = 0;
        String[] arrayOfString = new String[]{"date_added"};
        Cursor cursor;
        for (cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, arrayOfString, (String) null, (String[]) null, (String) null); cursor != null && cursor.moveToNext(); ++result) {
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return String.valueOf(result);
    }

    public static String getVideoInternalNumber(Context context) {
        int result = 0;
        String[] arrayOfString = new String[]{"date_added"};

        Cursor cursor;
        for (cursor = context.getContentResolver().query(MediaStore.Video.Media.INTERNAL_CONTENT_URI, arrayOfString, (String) null, (String[]) null, (String) null); cursor != null && cursor.moveToNext(); ++result) {
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return String.valueOf(result);
    }

    public static String getImagesExternalNumber(Context context) {
        int result = 0;

        Cursor cursor;
        for (cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{"datetaken", "date_added", "date_modified", "height", "width", "latitude", "longitude", "mime_type", "title", "_size"}, (String) null, (String[]) null, (String) null); cursor != null && cursor.moveToNext(); ++result) {
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return String.valueOf(result);
    }

    public static String getImagesInternalNumber(Context context) {
        int result = 0;

        Cursor cursor;
        for (cursor = context.getContentResolver().query(MediaStore.Images.Media.INTERNAL_CONTENT_URI, new String[]{"datetaken", "date_added", "date_modified", "height", "width", "latitude", "longitude", "mime_type", "title", "_size"}, (String) null, (String[]) null, (String) null); cursor != null && cursor.moveToNext(); ++result) {
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return String.valueOf(result);
    }


    public static String getDownloadFileNumber() {
        int result = 0;
        File[] files = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).listFiles();
        if (files != null) {
            result = files.length;
        }

        return String.valueOf(result);
    }

    public static String getContactsGroupNumber(Context context) {
        try {
            int result = 0;
            Uri uri = ContactsContract.Groups.CONTENT_URI;
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor;
            for (cursor = contentResolver.query(uri, (String[]) null, (String) null, (String[]) null, (String) null); cursor != null && cursor.moveToNext(); ++result) {
            }
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            return String.valueOf(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static JSONObject getBatteryStatus(Context context) {
        JSONObject jSONObject = new JSONObject();
        try {
            Intent intent = context.registerReceiver((BroadcastReceiver) null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
            int k = intent.getIntExtra("plugged", -1);
            switch (k) {
                case 1:
                    jSONObject.put("is_usb_charge", "false");
                    jSONObject.put("is_ac_charge", "true");
                    jSONObject.put("is_charging", "true");
                    return jSONObject;
                case 2:
                    jSONObject.put("is_usb_charge", "true");
                    jSONObject.put("is_ac_charge", "false");
                    jSONObject.put("is_charging", "true");
                    return jSONObject;
                default:
                    jSONObject.put("is_usb_charge", "false");
                    jSONObject.put("is_ac_charge", "false");
                    jSONObject.put("is_charging", "false");
                    return jSONObject;
            }
        } catch (JSONException e) {
            Log.i("异常", e.toString());
        }
        return jSONObject;
    }

    private static boolean isWifiProxy(Context context) {
        final boolean IS_ICS_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
        String proxyAddress;
        int proxyPort;
        if (IS_ICS_OR_LATER) {
            proxyAddress = System.getProperty("http.proxyHost");
            String portStr = System.getProperty("http.proxyPort");
            proxyPort = Integer.parseInt((portStr != null ? portStr : "-1"));
        } else {
            proxyAddress = Proxy.getHost(context);
            proxyPort = Proxy.getPort(context);
        }
        return (!TextUtils.isEmpty(proxyAddress)) && (proxyPort != -1);
    }

    //判断网络接口名字包含 ppp0 或 tun0
    public static boolean isDeviceInVPN() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (nif.getName().equals("tun0") || nif.getName().equals("ppp0")) {
                    Log.i("TAG", "isDeviceInVPN  current device is in VPN.");
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressLint("NewApi")
    private static JSONArray getSensorList(Context context) {
        // 获取传感器管理器
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        // 获取全部传感器列表
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        JSONArray jsonArray = new JSONArray();
        for (Sensor item : sensors) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("type", item.getType());
                jsonObject.put("name", item.getName());
                jsonObject.put("version", item.getVersion());
                jsonObject.put("vendor", item.getVendor());
                jsonObject.put("maxRange", item.getMaximumRange());
                jsonObject.put("minDelay", item.getMinDelay());
                jsonObject.put("power", item.getPower());
                jsonObject.put("resolution", item.getResolution());
            } catch (JSONException e) {
                Log.i("json异常", e.toString());
            }
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    public static String getCountry() {
        String country = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleListCompat listCompat = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration());
            for (int i = 0; i < listCompat.size(); i++) {
                country = listCompat.get(i).getCountry();
            }
        } else {
            Locale locale = Locale.getDefault();
            country = locale.getCountry();
        }
        return country;
    }

    public static String getLanguage() {
        String language = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleListCompat listCompat = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration());
            for (int i = 0; i < listCompat.size(); i++) {
                language = listCompat.get(i).getLanguage();
            }
        } else {
            Locale locale = Locale.getDefault();
            language = locale.getLanguage();
        }
        return language;
    }

    public static int getKeyboard(Context context) {
        InputManager inputManager = (InputManager) context.getSystemService(Context.INPUT_SERVICE);
        int[] inputDeviceIds = inputManager.getInputDeviceIds();
        return inputDeviceIds.length;
    }

}
