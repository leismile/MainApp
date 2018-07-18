package com.ubtechinc.alpha.task;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.ubtech.utilcode.utils.notification.NotificationCenter;
import com.ubtech.utilcode.utils.notification.Subscriber;
import com.ubtechinc.alpha.GetRobotWifiList;
import com.ubtechinc.alpha.event.GetWifiListEvent;
import com.ubtechinc.alpha.im.IMCmdId;
import com.ubtechinc.bluetoothrobot.BleNetworkWifiManager;
import com.ubtechinc.bluetoothrobot.old.BleConstants;
import com.ubtechinc.bluetoothrobot.old.ProtoUtil;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * @author：wululin
 * @date：2017/11/13 11:22
 * @modifier：ubt
 * @modify_date：2017/11/13 11:22
 * [A brief description]
 * version
 */

public class ProxyGetWifiListImpl extends AbstractProxyService {
    private static final int TWO_SECONDS = 20 * 1000;
    private Context mContext;
    private BleNetworkWifiManager mNetworkWifiManager;
    private volatile String wifiString = "[]";
    private GetWifiListEvent mEvent;
    public ProxyGetWifiListImpl(Context context){
        this.mContext = context;
    }

    private Subscriber<GetWifiListEvent> getWifiListEventSubscriber = new Subscriber<GetWifiListEvent>() {
        @Override
        public void onEvent(GetWifiListEvent event) {
            int status = event.status;
            if(status == 0){//开始获取WiFi列表
                mWifiList = new ArrayList<>();
                mTampWifList = new ArrayList<>();
                mNetworkWifiManager = new BleNetworkWifiManager(mContext);
                mEvent = event;
                stopScanWifi();
                startScanWifi();
            }else if(status == 1){//停止获取wifi列表
                stopScanWifi();
            }

        }
    };
    private Timer mScanWifiTimer;
    private TimerTask mScanWifiTask;
    private List<ScanResult> mWifiList;
    private List<ScanResult> mTampWifList;

    @Override
    public void registerEvent() {
        NotificationCenter.defaultCenter().subscriber(GetWifiListEvent.class, getWifiListEventSubscriber);
    }

    @Override
    public void unregisterEvent() {
        NotificationCenter.defaultCenter().unsubscribe(GetWifiListEvent.class, getWifiListEventSubscriber);
    }

    public void startScanWifi() {
        if(mWifiList != null){
            mWifiList.clear();
        }
        if(mTampWifList != null) {
            mTampWifList.clear();
        }
        mScanWifiTimer = new Timer();
        mScanWifiTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    sendScanDiffWifi();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        mScanWifiTimer.schedule(mScanWifiTask, 0, TWO_SECONDS);
    }

    public void stopScanWifi() {
        if (mScanWifiTimer != null) {
            mScanWifiTimer.cancel();
            mScanWifiTimer = null;
        }
        if (mScanWifiTask != null) {
            mScanWifiTask.cancel();
            mScanWifiTask = null;
        }
    }

    private void sendScanDiffWifi() throws JSONException {
        Log.i(TAG, "sendScanDiffWifi=========");
        startToScanWifi();
        List<ScanResult> wifiResultList = mNetworkWifiManager.getScanResult();
        List<ScanResult> wifList = ProtoUtil.filterResultList(wifiResultList);
        mTampWifList = wifList;
        List<ScanResult> results = ProtoUtil.deleteSameSSID(wifList, mWifiList);
        Log.i(TAG, "results=====" + wifList.size());
        if (results.size() != 0) {
            sendWifiListToPhone(results);
        }
        mWifiList = mTampWifList;
    }

    private void startToScanWifi() {
        if (!mNetworkWifiManager.isEnable()) {
            mNetworkWifiManager.openNetCard();
            mNetworkWifiManager.wifiStartScan();
            while (mNetworkWifiManager.wifiCheckState() != WifiManager.WIFI_STATE_ENABLED) {
                for (int i = 0; i < 15; i++) {//
                    try {
                        Thread.sleep(200);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        mNetworkWifiManager.wifiStartScan();
    }

    /**
     * 发送WiFi列表给手机
     *
     * @throws JSONException
     */
    private void sendWifiListToPhone(List<ScanResult> list) throws JSONException {
        String wifiStr;
        if (list != null) {
            wifiStr = ProtoUtil.getWifiJsonArray(list);
        } else {
            wifiStr = wifiString;
        }
        Log.i(TAG,"wifiStr=======" + wifiStr);
        JSONObject reply = new JSONObject();
        reply.put(BleConstants.WIFILIST, wifiStr);
        GetRobotWifiList.GetRobotWifiListResponse.Builder builder =  GetRobotWifiList.GetRobotWifiListResponse.newBuilder();
        builder.setWifilist(reply.toString());
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(mEvent.responseCmdID, IMCmdId.IM_VERSION,mEvent.requestSerial,builder.build(),mEvent.peer,null);
    }

}   
