package com.ubtechinc.alpha.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.ubtechinc.alpha.app.AlphaApplication;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * @author：wululin
 * @date：2017/11/10 16:34
 * @modifier：ubt
 * @modify_date：2017/11/10 16:34
 * [A brief description]
 * version
 */

public class WifiUtils {
    private static final String TAG = WifiUtils.class.getSimpleName();

    /**
     * 获取机器人当前连接的WiFi
     * @return
     */
    public static String getCurrConnWifi(){

        WifiManager wifiMgr = (WifiManager) AlphaApplication.getContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiMgr.getConnectionInfo();
        String wifiId = info != null ? info.getSSID() : null;
        return wifiId;
    }

    /**
     * 打开wifi
     */
    public static void openWifi(){
        WifiManager wifiMgr = (WifiManager) AlphaApplication.getContext().getSystemService(Context.WIFI_SERVICE);
        if(!wifiMgr.isWifiEnabled()){
            wifiMgr.setWifiEnabled(true);
        }
    }
    /**
     * 通过网络接口取
     * @return
     */
    public static String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return null;
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
