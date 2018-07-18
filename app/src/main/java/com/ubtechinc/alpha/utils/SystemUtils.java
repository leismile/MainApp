package com.ubtechinc.alpha.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.services.alphamini.R;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static android.os.BatteryManager.BATTERY_PLUGGED_USB;

/**
 * @author：wululin
 * @date：2017/11/21 11:29
 * @modifier：ubt
 * @modify_date：2017/11/21 11:29
 * [A brief description]
 * version
 */

public class SystemUtils {

    /**
     * 获得SD卡总大小
     *
     * @return
     */
    public static String getSDTotalSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return Formatter.formatFileSize(AlphaApplication.getContext(), blockSize * totalBlocks);
    }

    /**
     * 获得sd卡剩余容量，即可用大小
     *
     * @return
     */
    public static  String getSDAvailableSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return Formatter.formatFileSize(AlphaApplication.getContext(), blockSize * availableBlocks);
    }


    public static String  getSysVersion(){
        return android.os.Build.VERSION.RELEASE;
    }

    public static String getMainService(){
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = AlphaApplication.getContext().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(AlphaApplication.getContext().getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

    /**
     * logic.peng
     * BatteryManager会通过一个包含充电状态的持续 Intent广播所有的电池详情和充电详情。
     * 由于这是个持续 intent，因此无需通过将传入 null 的 registerReceiver
     * 作为接收器直接调用（如下一代码段所示）来注册 BroadcastReceiver，系统会返回当前电池状态 intent。
     * 通过电池状态，判断是否链接USB
     */
    public static boolean isUSBConnected(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        if (batteryStatus != null) {
            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            return chargePlug == BATTERY_PLUGGED_USB;
        }else {
            return false;
        }
    }

    /**
     * logic.peng
     * BatteryManager会通过一个包含充电状态的持续 Intent广播所有的电池详情和充电详情。
     * 由于这是个持续 intent，因此无需通过将传入 null 的 registerReceiver
     * 作为接收器直接调用（如下一代码段所示）来注册 BroadcastReceiver，系统会返回当前电池状态 intent。
     * 通过电池状态，判断是否在充电
     */
    public static boolean isCharging(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        if (batteryStatus != null) {
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
        }else {
            return false;
        }
    }

    /**
     * @return true 开启调试，false 未开启调试
     * @Description 是否是usb调试模式
     */
    public static boolean getUsbDebugEnable(Context context) {
        boolean enableAdb = (Settings.Secure.getInt(
                context.getContentResolver(), Settings.Secure.ADB_ENABLED, 0) > 0);
        return enableAdb;
    }

    public static void switchAdb(Context context,boolean status) {
        Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.ADB_ENABLED, status ? 1 : 0);
    }

    public static String getProvidersName(Context context){
        String providersName = null;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String IMSI = telephonyManager.getSubscriberId();
        if( IMSI == null){
            return context.getString(R.string.no_service);
        }

        if(IMSI.startsWith("46000") || IMSI.startsWith("46002")){
            providersName = context.getString(R.string.cmcc);
        }else if(IMSI.startsWith("46001")){
            providersName = context.getString(R.string.cucc);
        }else if (IMSI.startsWith("46003")) {
            providersName = context.getString(R.string.ctcc);
        }

        try {
            providersName = URLEncoder.encode(providersName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return providersName;
    }

    public static String getIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    public static String getMEID(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId(TelephonyManager.PHONE_TYPE_CDMA);
    }
}
