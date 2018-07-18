package com.ubtechinc.nets;

import android.content.Context;
import android.os.Build;

import com.ubtech.utilcode.utils.AppUtils;
import com.ubtech.utilcode.utils.DeviceUtils;
import com.ubtechinc.nets.http.HeaderInterceptor;
import com.ubtechinc.nets.utils.JsonUtil;
import com.ubtrobot.sys.SysApi;

import java.util.HashMap;

/**
 * @desc : ubx通用请求头
 * @author: Logic
 * @email : logic.peng@ubtech.com
 * @time : 2017/5/16
 * @modifier:
 * @modify_time:
 */

public final class GenericHeaders {
    private static final String ROBOT_PACKAGE_NAME = "";
    private static final String COMMON_INFO = "common-info";
    private static final String DEVICE_INFO = "device-info";

    //机器人和客户端都传字段
    public static final String APP_ID = "appId";
    public static final String APP_NAME = "appName";
    public static final String APP_VERSION_CODE = "appVersionCode";
    public static final String APP_VERSION_NAME = "appVersionName";
    public static final String DEV_TYPE = "dev_type";
    public static final String PROTOCOL_VERSION_CODE = "protocolVersionCode";
    public static final String SYS_LANGUAGE = "sysLanguage";
    public static final String SYS_VERSION_CODE = "sysVersionCode";
    public static final String SYS_VERSION_NAME = "sysVersionName";

    //机器人端独有通用头
    public static final String ROBOT_SN = "robotSn";
    public static final String CHEST_SOFTWARE_VERSION = "chestSoftwareVersion";
    public static final String CHEST_HARDWARE_VERSION = "chestHardwareVersion";
    public static final String CHEST_BOOT_VERSION = "chestBootVersion";
    public static final String HEADER_SOFTWARE_VERSION = "headerSoftwareVersion";
    public static final String HEADER_HARDWARE_VERSION = "headerHardwareVersion";
    public static final String HEADER_BOOT_VERSION = "headerBootVersion";

    //手机端通用头
    public static final String IMEI = "imei";
    public static final String DEVICE_MODE = "deviceMode";
    public static final String DEVICE_MANUFACTOR = "deviceManufactor";
    public static final String DEVICE_SCREEN_WIDTH = "deviceScreenWidth";
    public static final String DEVICE_SCREEN_HEIGHT = "deviceScreenHeight";
    public static final String CHANNEL_ID = "channelId";
    public static final String MAC_ADDRESS = "macAddress";

    public static HeaderInterceptor createGenericHeaders(Context context){
        HashMap<String,String> headers = new HashMap<>();
        headers.put(COMMON_INFO, JsonUtil.map2Json(getCommonHeaders(context)));
        headers.put(DEVICE_INFO,JsonUtil.map2Json(getRobotDeviceHeaders(context)));
       return new HeaderInterceptor(headers);
    }


    public static HashMap<String ,String > getCommonHeaders(Context context){
        HashMap<String, String> headers = new HashMap<>();
        headers.put(APP_ID, AppUtils.getAppPackageName(context));
        headers.put(APP_NAME, AppUtils.getAppName(context));
        headers.put(APP_VERSION_CODE, AppUtils.getAppVersionCode(context)+"");
        headers.put(APP_VERSION_NAME, AppUtils.getAppVersionName(context));

        headers.put(DEV_TYPE,  "robot");

        headers.put(PROTOCOL_VERSION_CODE, "1");
        headers.put(SYS_LANGUAGE, context.getResources().getConfiguration().locale.getLanguage());
        headers.put(SYS_VERSION_CODE, DeviceUtils.getSDKVersion()+"");
        headers.put(SYS_VERSION_NAME, Build.VERSION.SDK);
        headers.put(CHANNEL_ID, "");
        return headers;
    }

//    public static HashMap<String, String>  getPhoneDeviceHeaders(Context context){
//        HashMap<String, String> headers = new HashMap<>();
//        final String imei = PhoneUtils.getIMEI();
//        if (!TextUtils.isEmpty(imei)) {
//            headers.put(IMEI, imei);
//        }
//        headers.put(DEVICE_MODE, DeviceUtils.getModel());
//        headers.put(DEVICE_MANUFACTOR, DeviceUtils.getManufacturer());
//        headers.put(DEVICE_SCREEN_WIDTH, ScreenUtils.getScreenWidth()+"");
//        headers.put(DEVICE_SCREEN_HEIGHT, ScreenUtils.getScreenHeight()+"");
//        final String mac = DeviceUtils.getMacAddress();
//        if (!TextUtils.isEmpty(mac)) {
//            headers.put(MAC_ADDRESS, mac);
//        }
//        return headers;
//    }

    @Deprecated
    public static HashMap<String, String> getRobotDeviceHeaders(Context context){
        HashMap<String, String> headers = new HashMap<>(0);
        headers.put(ROBOT_SN, SysApi.get().readRobotSid());
        return headers;
    }
}
