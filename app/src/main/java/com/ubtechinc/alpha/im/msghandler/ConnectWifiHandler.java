package com.ubtechinc.alpha.im.msghandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.util.Log;

import com.ubtech.utilcode.utils.notification.NotificationCenter;
import com.ubtech.utilcode.utils.notification.Subscriber;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.ConnectRobotWifi;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.event.IMLoginResultEvent;
import com.ubtechinc.alpha.im.IMCmdId;
import com.ubtechinc.alpha.service.SkillHelper;
import com.ubtechinc.bluetoothrobot.BleNetworkWifiManager;
import com.ubtechinc.bluetoothrobot.old.LocalTTSHelper;
import com.ubtechinc.nets.im.business.LoginBussiness;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ProtoBufferDispose;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author：wululin
 * @date：2017/11/10 19:05
 * @modifier：ubt
 * @modify_date：2017/11/10 19:05
 * [A brief description]
 * version
 */

public class ConnectWifiHandler implements IMsgHandler {
    private static final String TAG = ConnectWifiHandler.class.getSimpleName();
    private static final String CHANGE_WIFI_SUCCESS_CODE = "0";//切换wifi成功
    private static final String PWS_ERROR_CODE = "1";//切换WiFi密码错误
    private static final String TIME_OUT_CODE = "2";//切换WiFi超时
    private static final String ARLEADY_CONNECT_WIFI = "3";//已经连接该网络
    private BleNetworkWifiManager mNetworkWifiManager;
    private boolean isBeginConnect = false;
    private int responseCmdId;
    private long requestSerial;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private String peer;
    private WifiInfo mCurrentWifiInfo;
    private String mSsid;
    private String connectSsid;
    private boolean isTimeOut = false;
    private String mCurrentSsid;
    private ExecutorService mThreadExecutor;

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        this.responseCmdId = responseCmdId;
        this.requestSerial = request.getHeader().getSendSerial();
        this.peer = peer;
        byte[] receiveData =  request.getBodyData().toByteArray();
        ConnectRobotWifi.ConnectRobotWifiRequest connectRobotWifiRequest = (ConnectRobotWifi.ConnectRobotWifiRequest) ProtoBufferDispose.unPackData(ConnectRobotWifi.ConnectRobotWifiRequest.class, receiveData);
        mSsid = connectRobotWifiRequest.getSsid();
        String cap = connectRobotWifiRequest.getCap();
        String pwd = connectRobotWifiRequest.getPwd();
        mThreadExecutor = Executors.newCachedThreadPool();
        Log.i(TAG,"ssid====" + mSsid + ";;cap====" + cap + "pwd=====" + pwd);
        mNetworkWifiManager = new BleNetworkWifiManager(AlphaApplication.getContext());
        mCurrentWifiInfo = mNetworkWifiManager.getCurrWifiInfo();
        mCurrentSsid = mCurrentWifiInfo.getSSID().replace("\"","");
        if(mCurrentSsid.equals(mSsid)){
            responseMsgToPhone(ARLEADY_CONNECT_WIFI);
        }else {
            SkillHelper.startBleNetwrokSkill();
            connect(mSsid,cap,pwd);
            registerWifiStatus();
            NotificationCenter.defaultCenter().unsubscribe(IMLoginResultEvent.class, loginResultSubscriber);
            NotificationCenter.defaultCenter().subscriber(IMLoginResultEvent.class, loginResultSubscriber);
        }

    }

    private void startConnectTimer(){
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                //连接超时，连接之前的WiFi
                Log.i(TAG,"连接超时========");
                isTimeOut = true;
                connectBeforeWifi();
                SkillHelper.stopBleNetworkSkill();
            }
        };
        mTimer.schedule(mTimerTask,30 * 10000);
    }

    private void stopConnectTimer(){
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
        if(mTimerTask != null){
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    private void registerWifiStatus() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        AlphaApplication.getContext().registerReceiver(mWifiStateReceiver,intentFilter);
    }

    private void unregisterWifiReceiver(){
        AlphaApplication.getContext().unregisterReceiver(mWifiStateReceiver);
    }

    private ConnectRunnable mConnectRunnbale;
    private void connect(final String ssid, final String cap, final String pwd){
        isBeginConnect = true;
        isTimeOut = false;
        startConnectTimer();
        if(mConnectRunnbale != null){
            mThreadExecutor.shutdownNow();
            mConnectRunnbale = null;
        }

//        mConnectRunnbale = new ConnectRunnable(ssid, pwd, cap);
//        mThreadExecutor.execute(mConnectRunnbale);
        mNetworkWifiManager.connect(ssid, pwd, cap);
    }

    private  class ConnectRunnable  implements  Runnable{
        private String ssid;
        private String pwd;
        private String cap;
        public ConnectRunnable(String ssid,String pwd,String cap){
            this.ssid= ssid;
            this.pwd = pwd;
            this.cap = cap;
        }
        @Override
        public void run() {
            mNetworkWifiManager.connect(ssid, pwd, cap);
        }
    }
    private BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                Parcelable parcelableExtra = intent
                        .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (null != parcelableExtra) {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    NetworkInfo.State state = networkInfo.getState();
                    Log.i(TAG,"state=====" + state );
                    if(state == NetworkInfo.State.CONNECTED){
                        connectSsid = wifiInfo.getSSID().replace("\"","");
                        boolean isLogin = LoginBussiness.getInstance(AlphaApplication.getContext()).isLoginIM();
                        Log.i(TAG,"isLogin======" + isLogin + ";;connectSsid====" + connectSsid + ";;mCurrentSsid=====" + mCurrentSsid);
                        if(isLogin){
                            if(connectSsid.equals(mSsid)){
                                revertAndSendCodeToPhone(CHANGE_WIFI_SUCCESS_CODE);
                            }else if(connectSsid.equals(mCurrentSsid)){
                                if(isTimeOut){
                                    revertAndSendCodeToPhone(TIME_OUT_CODE);
                                }else {
                                    revertAndSendCodeToPhone(PWS_ERROR_CODE);
                                }
                                LocalTTSHelper.playConnectBeforeWifi();
                            }
                        }
                        Log.i(TAG,"ssid=====" + connectSsid);
                    }
                }
            } else if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
                int linkWifiResult = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 123);
                if (linkWifiResult == WifiManager.ERROR_AUTHENTICATING) {
                    //密码错误  连接之前连接的WiFi
                    Log.i(TAG,"密码错误===========");
                    connectBeforeWifi();
                }
            }
        }
    };

    private void revertAndSendCodeToPhone(final String code) {
            if (isBeginConnect) {
                SkillHelper.stopBleNetworkSkill();
                isBeginConnect = false;
                unregisterWifiReceiver();
                stopConnectTimer();
                responseMsgToPhone(code);
            }
    }

    /**
     * 连接之前的WiFi
     */
    private void connectBeforeWifi(){
        List<WifiConfiguration> wifiConfigurations = mNetworkWifiManager.getConfiguration();
        int networkId = mNetworkWifiManager.getWifiConfigurated(wifiConfigurations,mCurrentSsid);
        if(networkId != -1){
            mNetworkWifiManager.connectWifi(networkId);
        }else {
            Log.i(TAG,"找不到之前的ID===========");
            unregisterWifiReceiver();
            stopConnectTimer();
        }
    }

    Subscriber<IMLoginResultEvent> loginResultSubscriber = new Subscriber<IMLoginResultEvent>(){

        @Override
        public void onEvent(IMLoginResultEvent imLoginResultEvent) {
            if(imLoginResultEvent.success) {
                Log.d(TAG,"IM Login Success");
                if(connectSsid.equals(mSsid)){
                    revertAndSendCodeToPhone(CHANGE_WIFI_SUCCESS_CODE);
                }else if(connectSsid.equals(mCurrentSsid)){
                    if(isTimeOut){
                        revertAndSendCodeToPhone(TIME_OUT_CODE);
                    }else {
                        revertAndSendCodeToPhone(PWS_ERROR_CODE);
                    }
                }
            } else {
                Log.d(TAG,"IM Login Error");
                SkillHelper.stopBleNetworkSkill();
            }
            NotificationCenter.defaultCenter().unsubscribe(IMLoginResultEvent.class, loginResultSubscriber);
        }
    };

    private void responseMsgToPhone(String code){
        ConnectRobotWifi.ConnectRobotWifiResponse.Builder builder =  ConnectRobotWifi.ConnectRobotWifiResponse.newBuilder();
        builder.setCode(code);
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION,requestSerial,builder.build(),peer,null);
    }

}
