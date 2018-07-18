package com.ubtechinc.bluetoothrobot;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * @author：wululin
 * @date：2017/10/24 15:25
 * @modifier：ubt
 * @modify_date：2017/10/24 15:25
 * [A brief description]
 * 蓝牙配网WiFi连接管理类
 */

public class BleNetworkWifiManager {
    private static final String TAG = BleNetworkWifiManager.class.getSimpleName();
    private WifiManager mWifiManager;
    // 网络连接列表
    private List<WifiConfiguration> mWifiConfiguration;
    public BleNetworkWifiManager(Context context){
        Context mContext = context.getApplicationContext();
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

    }

    /**
     * 打开Wifi网卡
     */
    public void openNetCard() {
        Log.i(TAG,"mWifiManager.isWifiEnabled()======" + mWifiManager.isWifiEnabled());
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 扫描wifi
     */
    public void wifiStartScan() {
        mWifiManager.startScan();
    }

    /**
     * 获取当前WiFi连接信息
     */
    public WifiInfo getCurrWifiInfo(){
        return mWifiManager.getConnectionInfo();
    }

    /**
     * 连接WiFi
     */
    public boolean connect(String ssid, String password, String type){
        int networkId;
        if(mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED){
            WifiConfiguration wifiConfig = this.createWifiInfo(ssid, password, type);
            disableNetworkConfig(ssid);

            // Add WiFi configuration to list of recognizable networks
            if ((networkId =  mWifiManager.addNetwork(wifiConfig)) == -1) {
                Log.d(TAG, "连接WiFi失败!!!");
                return false;
            }
            if (!disconnectFromWifi()) {
                Log.d(TAG, "wifi 没有打开!");
                return false;
            }
            if (! mWifiManager.enableNetwork(networkId, true)) {
                Log.d(TAG, "网络不可用!");
                return false;
            }
            if (! mWifiManager.reconnect()) {
                Log.d(TAG, "Failed to connect!");
                return false;
            }
            mWifiManager.saveConfiguration();

            Log.i(TAG,"wifi 开启成功！！");
            return true;
        }else {
            Log.i(TAG,"wifi 没有打开");
            return false;
        }
    }
    private static final int WIFICIPHER_NOPASS = 0;
    private static final int WIFICIPHER_WEP = 1;
    private static final int WIFICIPHER_WPA = 2;
    private static final int WIFICIPHER_WPA2 = 3;
    private WifiConfiguration createWifiInfo(String ssid, String password, String strSecure) {
        int type;

        if (strSecure.contains("WEP")) {
            type = WIFICIPHER_WEP;
        }else if(strSecure.contains("WPA2")){
            type = WIFICIPHER_WPA2;
        } else if (strSecure.contains("WPA")) {
            type = WIFICIPHER_WPA;
        }  else {
            type = WIFICIPHER_NOPASS;
        }
        Log.i(TAG,"type=======" + type);
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";

        if(type == WIFICIPHER_NOPASS) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if(type == WIFICIPHER_WEP) {
            config.hiddenSSID = true;
            config.wepKeys[0]= "\""+password+"\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if(type == WIFICIPHER_WPA) {
            config.preSharedKey = "\""+password+"\"";
            config.hiddenSSID = false;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }else if(type == WIFICIPHER_WPA2){
            config.preSharedKey = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    public WifiConfiguration isExist(String ssid) {
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();

        for (WifiConfiguration config : configs) {
            if (config.SSID.equals("\""+ssid+"\"")) {
                return config;
            }
        }
        return null;
    }

    private void disableNetworkConfig(String ssidName) {
        List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).SSID != null && list.get(i).SSID.equals(ssidName)) {
                mWifiManager.disableNetwork(list.get(i).networkId);
                break;
            }
        }
    }

    /**
     * 是否打开WIFI
     *
     */
    public boolean isEnable() {
        return mWifiManager.isWifiEnabled();
    }

    private boolean disconnectFromWifi() {
        return mWifiManager.disconnect();
    }

    /**
     * 根据之前配置好的WiFi ID  进行联网
     */
    public void connectWifi(int networkId){
        mWifiManager.enableNetwork(networkId,true);
    }

    /**
     * 移除wifiConfiguration中保存的wifi
     */
    public boolean clearWifiInfo(String ssid) {
        boolean flag = false;
        getConfiguration();
        if (mWifiConfiguration == null){
            return false;
        }
        for(int i = 0; i < mWifiConfiguration.size(); i++){
            String ssidNew = mWifiConfiguration.get(i).SSID.replace("\"", "");//wifi ssid 获取到的是加了双引号..要去掉此双引号
            if (!TextUtils.isEmpty(ssid) && ssid.equalsIgnoreCase(ssidNew)){
                WifiConfiguration configuration = mWifiConfiguration.get(i);
                flag = mWifiManager.removeNetwork(configuration.networkId);
            }
        }
        return flag;
    }

    // 得到配置好的网络
    public List<WifiConfiguration> getConfiguration() {
        return mWifiConfiguration = mWifiManager.getConfiguredNetworks();
    }

    /**
     * 判断要连接的wifi名是否已经配置过了
     * @return 返回要连接的wifi的ID，如果找不到就返回-1
     */
    public int getWifiConfigurated(List<WifiConfiguration> wifiConfigList,String wifi_SSID) {
        int id = -1;
        Log.i(TAG,"wifi_ssid=======" + wifi_SSID);
        if (wifiConfigList != null) {
            for (int j = 0; j < wifiConfigList.size(); j++) {
                Log.i(TAG,"ssid======" + wifiConfigList.get(j).SSID);
                if (wifiConfigList.get(j).SSID.equals("\"" + wifi_SSID + "\"")) {
                    //如果要连接的wifi在已经配置好的列表中，那就设置允许链接，并且得到id
                    id = wifiConfigList.get(j).networkId;
                    break;
                }
            }
        }
        return id;
    }

    /**
     * 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）
     */
    public static boolean ping() {
        try {
            HttpURLConnection urlc = (HttpURLConnection) (new URL("https://www.baidu.com").openConnection());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(10000);
            urlc.connect();
            return (urlc.getResponseCode() == 200);
        } catch (IOException e) {
            Log.e(TAG, "Error checking internet connection", e);
        }

        return false;
    }


    public static boolean pingWithCommad(){
            //return true;
            String result = null;
            try {
                String ip = "www.ubtrobot.com";// ping 的地址，可以换成任何一种可靠的外网
                Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);// ping网址3次
                // 读取ping的内容，可以不加
                InputStream input = p.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(input));
                StringBuilder stringBuffer = new StringBuilder();
                String content;
                while ((content = in.readLine()) != null) {
                    stringBuffer.append(content);
                }
                Log.d("------ping-----",
                        "result content : " + stringBuffer.toString());
                // ping的状态
                int status = p.waitFor();
                if (status == 0) {
                    result = "success";
                    return true;
                } else {
                    result = "failed";
                }
            } catch (IOException e) {
                result = "IOException";
            } catch (InterruptedException e) {
                result = "InterruptedException";
            } finally {
                Log.d("----result---", "result = " + result);
            }
            return false;
    }

    public List<ScanResult> getScanResult(){
        return mWifiManager.getScanResults();
    }

    /**
     * 检查WIFI状态
     */
    public int wifiCheckState() {
        return mWifiManager.getWifiState();
    }
}
