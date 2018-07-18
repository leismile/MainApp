package com.ubtechinc.bluetoothrobot.old;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;


/**
 * @author：wululin
 * @date：2017/10/19 16:29
 * @modifier：ubt
 * @modify_date：2017/10/19 16:29
 * [A brief description]
 * version
 */

public class ProtoUtil {
    private static final String TAG = ProtoUtil.class.getSimpleName();
    private static final int SEND_WIFI_LIST_SIZE = 2; //分批发送WiFi列表的个数

    /**
     * 过滤ssid的wifi
     * @return
     */
    public static List<ScanResult> filterResultList(List<ScanResult> results){

        List<ScanResult> mWifiList = new ArrayList();
        for(ScanResult result : results){
            Log.i(TAG,"ssid=====" + result.SSID);
            if (result.SSID == null || result.SSID.length() == 0 || result.capabilities.contains("[IBSS]")) {
                continue;
            }
            boolean found = false;
            for(ScanResult item:mWifiList){
                if(item.SSID.equals(result.SSID)&&item.capabilities.equals(result.capabilities)){
                    found = true;
                    break;
                }
            }
            if(!found){
                if(!result.SSID.contains("NVRAM WARNING:")){
                    mWifiList.add(result);
                }
            }
        }

        Collections.sort(mWifiList, new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult scanResult, ScanResult t1) {
                return new Integer(scanResult.level).compareTo(new Integer(t1.level));
            }
        });

        return mWifiList;
    }



    public static String getWifiJsonArray(List<ScanResult> mScanResultList) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (int i = 0; i < mScanResultList.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(BleConstants.LEVEL, mScanResultList.get(i).level);
                jsonObject.put(BleConstants.SSID, mScanResultList.get(i).SSID);
                String strSecure = mScanResultList.get(i).capabilities;
                if (strSecure.contains("WEP")) {
                    strSecure = "WEP";
                } else if (strSecure.contains("WPA2")) {
                    strSecure = "WPA2";
                } else if (strSecure.contains("WPA")) {
                    strSecure = "WPA";
                } else {
                    // 表示其他
                    strSecure = "N";
                }
                jsonObject.put(BleConstants.CAPABILITIES, strSecure);
                jsonArray.put(i, jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray.toString();
    }

    /**
     * 拼接wifi连接成功指令
     * @return
     */
    public static String wifiSuccessTrans(String serialId,String productId){
        try{
            JSONObject reply=new JSONObject();
            reply.put(BleConstants.JSON_COMMAND,BleConstants.CONNECT_WIFI_SUCCESS_TRANS);
            reply.put(BleConstants.PRODUCTID, productId);
            reply.put(BleConstants.SERISAL_NUMBER,serialId);
            return reply.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 拼接WiFi可用指令
     * @param serialId
     * @param productId
     * @return
     */
    public static String networkAvailable(String serialId,String productId){
        try{
            JSONObject reply=new JSONObject();
            reply.put(BleConstants.JSON_COMMAND,BleConstants.REPLY_ROBOT_IS_WIFI_TRANS);
            reply.put(BleConstants.CODE,BleConstants.WIFI_OK);
            reply.put(BleConstants.PRODUCTID, productId);
            reply.put(BleConstants.SERISAL_NUMBER,serialId);
            return reply.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 拼接WiFi连接不可用指令
     * @return
     */
    public static String networkNotAvailable(){
        try{
            JSONObject reply=new JSONObject();
            reply.put(BleConstants.JSON_COMMAND,BleConstants.REPLY_ROBOT_IS_WIFI_TRANS);
            reply.put(BleConstants.CODE,BleConstants.WIFI_NOT_OK);
            return reply.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 将WiFi分成5个一组
     * @param mWifiList
     * @return
     */
    public static List<List<ScanResult>> getArrWifiList(List<ScanResult> mWifiList){
        sortList(mWifiList);
        List<List<ScanResult>> listArr = new ArrayList<>();
        int arrSize = mWifiList.size()%SEND_WIFI_LIST_SIZE==0?mWifiList.size()/SEND_WIFI_LIST_SIZE:mWifiList.size()/SEND_WIFI_LIST_SIZE+1;
        for(int i=0;i<arrSize;i++) {
            List<ScanResult>  sub = new ArrayList<>();
            for(int j=i*SEND_WIFI_LIST_SIZE;j<=SEND_WIFI_LIST_SIZE*(i+1)-1;j++) {
                if(j<=mWifiList.size()-1) {
                    sub.add(mWifiList.get(j));
                }
            }
            listArr.add(sub);
        }
        return listArr;
    }

    public static void sortList(List<ScanResult> mList){
        Collections.sort(mList, new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult scanResult, ScanResult t1) {
                return new Integer(t1.level).compareTo(new Integer(scanResult.level));
            }
        });
    }

    public static List<ScanResult> deleteSameSSID(List<ScanResult> list1,List<ScanResult> list2){
        List<ScanResult> scanResults = new ArrayList<>();
        for (ScanResult result: list1){
            String ssid1 = result.SSID;
            boolean isHas = false;
            for (ScanResult result1: list2){
                String ssid2 = result1.SSID;
                if(ssid1.equals(ssid2)){
                    isHas = true;
                }
            }
            if(!isHas){
                scanResults.add(result) ;
            }
        }
        return scanResults;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    public static void openBle(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!bluetoothAdapter.isEnabled()){
            bluetoothAdapter.enable();
        }
    }

    public static boolean isOpenBle(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter.isEnabled();
    }

    public static void closeBle(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter.isEnabled()){
            bluetoothAdapter.disable();
        }
    }

    public static void setWifi(boolean isEnable,Context context) {

        WifiManager mWm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        System.out.println("wifi===="+mWm.isWifiEnabled());
        if (isEnable) {// 开启wifi

            if (!mWm.isWifiEnabled()) {

                mWm.setWifiEnabled(true);

            }
        } else {
            // 关闭 wifi
            if (mWm.isWifiEnabled()) {
                mWm.setWifiEnabled(false);
            }
        }

    }
}
