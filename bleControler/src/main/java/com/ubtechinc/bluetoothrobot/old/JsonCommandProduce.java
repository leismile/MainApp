package com.ubtechinc.bluetoothrobot.old;

import android.net.wifi.ScanResult;

import com.ubtechinc.protocollibrary.communite.old.ICommandProduce;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;



/**
 * @desc : 以Json形式封装指令
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/4/3
 */

public class JsonCommandProduce implements ICommandProduce {

    private volatile String EMPTY_WIFI_LIST = "[]";

    @Override
    public String getWifiSuc(String serialNumber, String productId) {
        return ProtoUtil.wifiSuccessTrans(serialNumber, productId);
    }

    @Override
    public String getConnectSuc(int code) {
        JSONObject reply = new JSONObject();
        try {
            reply.put(BleConstants.JSON_COMMAND, BleConstants.ROBOT_CONNECT_SUCCESS);
            reply.put(BleConstants.CODE, code);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return reply.toString();
    }

    @Override
    public String getWifiAvailable(String serialNumber, String productId) {
        return ProtoUtil.networkAvailable(serialNumber, productId);
    }

    @Override
    public String getWifiInvalid() {
        return ProtoUtil.networkNotAvailable();
    }

    @Override
    public String getWifiList(List<ScanResult> list) {
        String wifiStr;
        if (list != null) {
            wifiStr = ProtoUtil.getWifiJsonArray(list);
        } else {
            wifiStr = EMPTY_WIFI_LIST;
        }
        JSONObject reply = new JSONObject();
        try {
            reply.put(BleConstants.JSON_COMMAND, BleConstants.WIFI_LIST_TO_MOBILE_TRANS);
            reply.put(BleConstants.WIFILIST, wifiStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return reply.toString();
    }

    @Override
    public String getBindingSuc() {
        JSONObject reply = new JSONObject();
        try {
            reply.put(BleConstants.JSON_COMMAND,BleConstants.BINGDANG_SUCCESS_TRANS);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return reply.toString();
    }

    @Override
    public String getErrorCode(int errorCode) {
        JSONObject reply = new JSONObject();
        try {
            reply.put(BleConstants.JSON_COMMAND, BleConstants.BLE_NETWORK_ERROR);
            reply.put(BleConstants.RESPONSE_CODE, errorCode);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return reply.toString();
    }
}
