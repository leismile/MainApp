package com.ubtechinc.bluetoothrobot;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtech.utilcode.utils.notification.NotificationCenter;
import com.ubtech.utilcode.utils.notification.Subscriber;
import com.ubtech.utilcode.utils.thread.HandlerUtils;
import com.ubtechinc.alpha.BleBindOrSwitchWifi;
import com.ubtechinc.bluetoothlibrary.BleSender;
import com.ubtechinc.bluetoothlibrary.BleStatusEvent;
import com.ubtechinc.bluetoothlibrary.BleUtil;
import com.ubtechinc.bluetoothlibrary.UbtBluetoothConnManager;
import com.ubtechinc.bluetoothrobot.old.BleConstants;
import com.ubtechinc.bluetoothrobot.old.JsonAbstractBleCommandFactory;
import com.ubtechinc.bluetoothrobot.old.LocalTTSHelper;
import com.ubtechinc.nets.im.business.LoginBussiness;
import com.ubtechinc.protocollibrary.communite.old.IAbstractBleCommandFactory;
import com.ubtechinc.protocollibrary.communite.old.ICommandEncode;
import com.ubtechinc.protocollibrary.communite.old.ICommandProduce;
import com.ubtechinc.protocollibrary.protocol.CmdId;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.net.wifi.WifiManager.SUPPLICANT_STATE_CHANGED_ACTION;

/**
 * @author：wululin
 * @date：2017/10/24 14:21
 * @modifier：ubt
 * @modify_date：2017/10/24 14:21
 * [A brief description]
 * 蓝牙配网管理类
 */

public class UbtBleNetworkManager {
    private static final String TAG = UbtBleNetworkManager.class.getSimpleName();
    private static final int TWO_SECONDS = 20 * 1000;
    private static final int PING_MSG_WATH = 0x001;
    private static final int WIFI_LOAD_TASK = 0x002;
    //    private static final int START_PING_MSG_WATH = 0x003;
    private Context mContext;
    private String mProductId;
    private String mSerialNumber;
    private BleNetworkWifiManager mNetworkWifiManager;
    private volatile String wifiString = "[]";
    private GetMessageFormMobileListener mGetMessageFormMobileListener;
    private ExecutorService mThreadExecutor;
    private WifiConnectState alphaWifiState = WifiConnectState.IDLE;
    private IAbstractBleCommandFactory abstractBleCommandFactory;
    private ICommandEncode commandEncode;
    private ICommandProduce commandProduce;
    private BleNetworkSkillListener mBleNetworkSkillListener;
    private boolean isBandingWifi = true;
    private WifiLinkStatus wifiLinkStatus = new WifiLinkStatus();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PING_MSG_WATH:
                    boolean isWifiOK = (boolean) msg.obj;
                    Log.d(TAG, "isWifiOK=====" + isWifiOK);
                    stopConnectWifiTimer();
                    if (isWifiOK) {
                        sendWifiResultToMobile();
                    } else {
                        sendErrorCodeToPhone(BleConstants.PING_ERROR_CODE);
                    }
                    break;
                case WIFI_LOAD_TASK:
                    try {
                        sendScanDiffWifi();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
//                case START_PING_MSG_WATH:
//                    startPing();
//                    break;
            }
        }
    };

    private static class UbtDealBleCommandManagerHolder {
        public static UbtBleNetworkManager instance = new UbtBleNetworkManager();
    }

    private UbtBleNetworkManager() {
    }

    public static UbtBleNetworkManager getInstance() {
        return UbtDealBleCommandManagerHolder.instance;
    }

    public void init(Context context, String serialNumber) {
        this.mContext = context;
        this.mSerialNumber = serialNumber;
        mNetworkWifiManager = new BleNetworkWifiManager(mContext);
        mThreadExecutor = Executors.newCachedThreadPool();
        abstractBleCommandFactory = new JsonAbstractBleCommandFactory(mSerialNumber);
        commandEncode = abstractBleCommandFactory.getCommandEncode();
        commandProduce = abstractBleCommandFactory.getCommandProduce();

        NotificationCenter.defaultCenter().subscriber(BleStatusEvent.class, subscriber);

    }

    Subscriber<BleStatusEvent> subscriber = new Subscriber<BleStatusEvent>() {
        @Override
        public void onEvent(BleStatusEvent bleStatusEvent) {
                if (bleStatusEvent.getType()== bleStatusEvent.getTYPE_CONNECTED()) {


                }else if(bleStatusEvent.getType()== bleStatusEvent.getTYPE_FAILED()) {
                    LogUtils.d("connect fail");
                    SkillManager.Companion.getInstance().exitNetworkSkill(new ResponseCallback() {
                        @Override
                        public void onResponse(Request request, Response response) {

                        }

                        @Override
                        public void onFailure(Request request, CallException e) {

                        }
                    });
                    stopScanWifi();
                } else {
                    LogUtils.d("disconnect");
                    SkillManager.Companion.getInstance().exitNetworkSkill(new ResponseCallback() {
                        @Override
                        public void onResponse(Request request, Response response) {

                        }

                        @Override
                        public void onFailure(Request request, CallException e) {

                        }
                    });
                    stopScanWifi();
                }
            }

    };

    public void setProductId(String productId) {
        this.mProductId = productId;
    }

    private void registerWifiBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(SUPPLICANT_STATE_CHANGED_ACTION);
        mContext.registerReceiver(mWifiStateReceiver, filter);
    }

    private void unRegisterWifiBroadcast() {
        if(mWifiStateReceiver != null){
            mContext.unregisterReceiver(mWifiStateReceiver);
        }
    }


    public void setGetMessageFormMobileListener(GetMessageFormMobileListener getMessageFormMobile) {
        this.mGetMessageFormMobileListener = getMessageFormMobile;
    }


    public void parseResult(String result, Object peer) {
        Log.i(TAG, "receverData======" + result);
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject infoContent = new JSONObject(result);
                stopConnectWifiTimer();
                int commandType = infoContent.getInt(BleConstants.JSON_COMMAND);
                Log.d("0403", "receverData -- commandType : " + commandType);
                if (commandType == BleConstants.WIFI_NAME_TRANS) {//wifi名称指令
                    wifiLinkStatus.stop();
                    registerWifiBroadcast();
                    stopScanWifi();
                    receiverWifiName(infoContent);
                    isBandingWifi = true;
                } else if(commandType == BleConstants.WIFI_INFO_TRANS_FOR_BINDING) {
                    Log.d(TAG, "开始连接   WIFI_INFO_TRANS_FOR_BINDING  ");
                    wifiLinkStatus.stop();
                    registerWifiBroadcast();
                    stopScanWifi();
                    receiverWifiName(infoContent);
                    isBandingWifi = false;
                }else if (commandType == BleConstants.WIFI_LIST_TRANS) {//获取wifi列表指令
                    stopScanWifi();
                    startScanWifi();
                    sendWifiListsToPhone();
                } else if (commandType == BleConstants.CLIENTID_TRANS) { //clientId指令
                    if (mGetMessageFormMobileListener != null) {
                        String clientId = infoContent.getString(BleConstants.CLIENTID);
                        String qm = null;
                        try {
                            qm = infoContent.getString(BleConstants.QM);
                        } catch (Exception e) {
                            //兼容下老的客户端
                        }
                        boolean canFetchMusicVip = !TextUtils.isEmpty(qm) && qm.equals("1") ? true:false;
                        mGetMessageFormMobileListener.onGetClientId(clientId,canFetchMusicVip);
                    }
                } else if (commandType == BleConstants.CONNECT_SUCCESS) {//收到手机端蓝牙连接成功的指令
                    if(UbtBluetoothConnManager.getInstance().isOtherDeviceConnect()){
                        LogUtils.w("isOtherDeviceConnect refuse !");
                        robotConnectToOtherDeviecs(peer);
                    }else {
                        LogUtils.i("start config network");
                        SkillManager.Companion.getInstance().startNetworkSkill(new ResponseCallback() {
                            @Override
                            public void onResponse(Request request, Response response) {
                            }

                            @Override
                            public void onFailure(Request request, CallException e) {

                            }
                        });
                        loadWifiList();
                        sendConnectSuccess(0);
                    }
                } else if (commandType == BleConstants.ROBOT_IS_WIFI_TRANS) { //收到手机端获取WiFi是否连接指令
                    sendWifiIsOkMsgToPhone();
                }else if(commandType == BleConstants.BLE_DISCONNECT_TRANS){
                    UbtBluetoothConnManager.getInstance().cancelCurrentDevices();
                } else if(commandType == BleConstants.ROBOT_NETWORK_NOT_AVAILABLE) {
                    LocalTTSHelper.playNetworkNotAvailable();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            SkillManager.Companion.getInstance().exitNetworkSkill(new ResponseCallback() {
                @Override
                public void onResponse(Request request, Response response) {

                }

                @Override
                public void onFailure(Request request, CallException e) {

                }
            });
        }

        Log.d("0403", "receverData -- end");
    }

    void robotConnectToOtherDeviecs(final Object peer){
        try {
            JSONObject reply=new JSONObject();
            reply.put(BleConstants.JSON_COMMAND,BleConstants.BLE_NETWORK_ERROR);
            reply.put(BleConstants.RESPONSE_CODE, BleConstants.ALEARDY_CONNECT_ERROR_CODE);
            Robot2PhoneMsgMgr.get().sendResponseData(CmdId.BL_BIND_OR_SWITCH_WIFI_RESPONSE, Statics.PROTO_VERSION, 0,BleBindOrSwitchWifi.BindOrSwitchWifiResponse.newBuilder().setData(reply.toString()).build() , new BleSender(peer, 1));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HandlerUtils.runUITask(new Runnable() {
            @Override
            public void run() {
                try {
                    BleSender bleSender = (BleSender) peer;

                    UbtBluetoothConnManager.getInstance().cancelDevice((BluetoothDevice) bleSender.getPeer());
                }catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 2000);

    }
    /**
     * 发送Wifi连接成功给手机
     * 包括productId和序列号
     */
    private void sendWifiResultToMobile() {


        Robot2PhoneMsgMgr.get().sendResponseData(CmdId.BL_BIND_OR_SWITCH_WIFI_RESPONSE, Statics.PROTO_VERSION, 0,BleBindOrSwitchWifi.BindOrSwitchWifiResponse.newBuilder().setData(commandProduce.getWifiSuc(mSerialNumber, mProductId)).build() , new BleSender("", 1));

    }

    /**
     * 收到手机端发送过来的wifi名称和密码
     *
     * @param infoContent
     * @throws JSONException
     */
    private void receiverWifiName(JSONObject infoContent) throws JSONException {
        String ssid = infoContent.getString(BleConstants.JSON_SSID);
        String pwd = infoContent.getString(BleConstants.JSON_PWD);
        String secure = infoContent.getString(BleConstants.JSON_SECURE);
        connectedBegin(ssid, pwd, secure);
    }

    /**
     * 打开wifi开关后自动连接wifi
     */
    private List<ScanResult> mWifiList = new ArrayList<>();
    private List<ScanResult> mTampWifList = new ArrayList<>();

    private void loadWifiList() {
        stopScanWifi();
        startToScanWifi();
    }

    private void sendScanDiffWifi() throws JSONException {
        Log.i(TAG, "sendScanDiffWifi=========");
        startToScanWifi();
        List<ScanResult> wifiResultList = mNetworkWifiManager.getScanResult();
        Log.i(TAG,"wifiResultList======" + wifiResultList.toString());
        List<ScanResult> wifList = BleUtil.filterResultList(wifiResultList);
        Log.i(TAG,"wifList======" + wifList.toString());
        mTampWifList = wifList;
        List<ScanResult> results = BleUtil.deleteSameSSID(wifList, mWifiList);
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
     * 分批发送WiFi列表
     *
     * @throws JSONException
     */
    private void sendWifiListsToPhone() throws JSONException {
        List<ScanResult> wifiResultList = mNetworkWifiManager.getScanResult();
        mWifiList = BleUtil.filterResultList(wifiResultList);
        if (mWifiList != null && mWifiList.size() != 0) {
            List<List<ScanResult>> listArr = BleUtil.getArrWifiList(mWifiList);
            for (List<ScanResult> list : listArr) {
                sendWifiListToPhone(list);
            }
        }
    }

    private void sendConnectSuccess(int code) {
        Robot2PhoneMsgMgr.get().sendResponseData(CmdId.BL_BIND_OR_SWITCH_WIFI_RESPONSE, Statics.PROTO_VERSION, 0,BleBindOrSwitchWifi.BindOrSwitchWifiResponse.newBuilder().setData(commandProduce.getConnectSuc(code)).build() , new BleSender("", 1));
    }

    /**
     * 发送WiFi是否连接成功指令给手机
     */
    private void sendWifiIsOkMsgToPhone() {

        if ( LoginBussiness.getInstance(mContext).isLoginIM()) {//wifi可用
            Robot2PhoneMsgMgr.get().sendResponseData(CmdId.BL_BIND_OR_SWITCH_WIFI_RESPONSE, Statics.PROTO_VERSION, 0,
                    BleBindOrSwitchWifi.BindOrSwitchWifiResponse.newBuilder().setData(commandProduce.getWifiAvailable(mSerialNumber, mProductId)).build() , new BleSender("", 1));
        } else {
            Robot2PhoneMsgMgr.get().sendResponseData(CmdId.BL_BIND_OR_SWITCH_WIFI_RESPONSE, Statics.PROTO_VERSION, 0,
                    BleBindOrSwitchWifi.BindOrSwitchWifiResponse.newBuilder().setData(commandProduce.getWifiInvalid()).build() , new BleSender("", 1));
        }
    }

    /**
     * 发送WiFi列表给手机
     *
     * @throws JSONException
     */
    private void sendWifiListToPhone(List<ScanResult> list) throws JSONException {
        Robot2PhoneMsgMgr.get().sendResponseData(CmdId.BL_BIND_OR_SWITCH_WIFI_RESPONSE, Statics.PROTO_VERSION, 0,
                BleBindOrSwitchWifi.BindOrSwitchWifiResponse.newBuilder().setData(commandProduce.getWifiList(list)).build() , new BleSender("", 1));
    }

    /**
     * 向手机端发送绑定成功消息
     */
    public void sendMsgBingdingSuccessToPhone() throws JSONException {
        Robot2PhoneMsgMgr.get().sendResponseData(CmdId.BL_BIND_OR_SWITCH_WIFI_RESPONSE, Statics.PROTO_VERSION, 0,
                BleBindOrSwitchWifi.BindOrSwitchWifiResponse.newBuilder().setData(commandProduce.getBindingSuc()).build() , new BleSender("", 1));
//        LocalTTSHelper.playBandingSuc();
    }

    /**
     * 发送错误码给手机
     *
     * @param errorCode
     */
    public void sendErrorCodeToPhone(int errorCode) {
        Robot2PhoneMsgMgr.get().sendResponseData(CmdId.BL_BIND_OR_SWITCH_WIFI_RESPONSE, Statics.PROTO_VERSION, 0,
                BleBindOrSwitchWifi.BindOrSwitchWifiResponse.newBuilder().setData(commandProduce.getErrorCode(errorCode)).build() , new BleSender("", 1));
    }


    /**
     * 开始连接wifi
     *
     * @param ssidName
     * @param password
     * @param cap
     */
    private void connectedBegin(String ssidName, String password, String cap) {
        Log.e(TAG, "开始连接   ssidName  " + ssidName + "  password " + password + "cap==" + cap);
        Log.d(TAG, "开始连接   ssidName  " + ssidName + "  password " + password + "cap==" + cap);
        startConnectWifiTimer();
        alphaWifiState = WifiConnectState.CONNECTING;
        mThreadExecutor.execute(new ConnectRunnable(ssidName, password, cap));
    }

    private BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
                Log.d(TAG,  " onReceive -- WIFI_STATE_CHANGED_ACTION -- wifistate : " + wifistate);
                if (wifistate == WifiManager.WIFI_STATE_ENABLED) {
                    Log.i(TAG, "系统开启wifi");
                    mNetworkWifiManager.wifiStartScan();
                }
            } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if(networkInfo != null) {
                    Log.d(TAG, " wifiLinkStatus : " + wifiLinkStatus + " networkInfo.getExtraInfo() : " + networkInfo.getExtraInfo() + " networkInfo.isConnected() : " + networkInfo.isConnected());
                    if (!wifiLinkStatus.check(networkInfo.getExtraInfo()) && !networkInfo.isConnected()) {
                        Log.d(TAG, " wifiLinkStatus : " + wifiLinkStatus + " networkInfo.isConnected() : " + networkInfo.isConnected());
                        if(!networkInfo.isConnected()) {
                            bandingEnd();
                        }
                    }
                }
                Log.d(TAG,  " onReceive -- NETWORK_STATE_CHANGED_ACTION -- intent : " + intent + " networkInfo : " + networkInfo);
                wifiConnectState(intent);
            } else if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
                int linkWifiResult = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 123);
                SupplicantState supplicantState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                Log.d(TAG,  " onReceive -- SUPPLICANT_STATE_CHANGED_ACTION -- linkWifiResult : " + linkWifiResult + " supplicantState : " + supplicantState);
                if (linkWifiResult == WifiManager.ERROR_AUTHENTICATING) {
                    Log.i(TAG, "密码错误=================");
                    bandingEnd();
                }
            }
        }

        private void bandingEnd() {
            Log.d(TAG, " bandingEnd ");
            wifiLinkStatus.stop();
            isBandingWifi = false;
            LocalTTSHelper.playErrorPassword();
            if (alphaWifiState == WifiConnectState.CONNECTING) {
                alphaWifiState = WifiConnectState.DISCONNECTED;
                unRegisterWifiBroadcast();
                stopConnectWifiTimer();
                sendErrorCodeToPhone(BleConstants.PASSWORD_VALIDATA_ERROR_CODE);
            }
        }
    };

//    private void startPing() {
//        mThreadExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
//                boolean isWifiOK = mNetworkWifiManager.pingWithCommad();
//                Message msg = new Message();
//                msg.what = PING_MSG_WATH;
//                msg.obj = isWifiOK;
//                mHandler.sendMessage(msg);
//            }
//        });
//
//    }

    protected void wifiConnectState(Intent intent) {
        Parcelable parcelableExtra = intent
                .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (null != parcelableExtra) {
            NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
            NetworkInfo.State state = networkInfo.getState();
            Log.i(TAG, "wifiConnectState====" + state + "alphaWifiState====" + alphaWifiState);
            if (state == NetworkInfo.State.CONNECTED) {
                if (alphaWifiState == WifiConnectState.CONNECTING) {
                    alphaWifiState = WifiConnectState.CONNECTED;
                    unRegisterWifiBroadcast();
                    stopConnectWifiTimer();
                    sendWifiResultToMobile();
                    if(isBandingWifi) {
                        LocalTTSHelper.playChooseWifiSuc();
                    }
                }
            }
        }
    }

    private Timer mConnTimeOutTimer;
    private TimerTask mConnTimeOutTask;

    private void startConnectWifiTimer() {
        mConnTimeOutTimer = new Timer("ConnWifi_Timer");
        mConnTimeOutTask = new TimerTask() {
            @Override
            public void run() {
                stopConnectWifiTimer();
                LocalTTSHelper.playConnectTimeout();
                if (alphaWifiState == WifiConnectState.CONNECTING) {
                    alphaWifiState = WifiConnectState.DISCONNECTED;
                    unRegisterWifiBroadcast();
                    sendErrorCodeToPhone(BleConstants.CONNECT_TIME_OUT_ERROR_CODE);
                }
            }
        };
        mConnTimeOutTimer.schedule(mConnTimeOutTask, 60 * 1000);
    }

    private void stopConnectWifiTimer() {
        if (mConnTimeOutTimer != null) {
            mConnTimeOutTimer.cancel();
            mConnTimeOutTimer = null;
        }
        if (mConnTimeOutTask != null) {
            mConnTimeOutTask.cancel();
            mConnTimeOutTask = null;
        }
    }

    public enum WifiConnectState {
        IDLE, CONNECTING, CONNECTED, DISCONNECTED
    }

    private Timer mScanWifiTimer;
    private TimerTask mScanWifiTask;

    public void startScanWifi() {
        mScanWifiTimer = new Timer();
        mScanWifiTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(WIFI_LOAD_TASK);
            }
        };
        mScanWifiTimer.schedule(mScanWifiTask, TWO_SECONDS, TWO_SECONDS);
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

    private class ConnectRunnable implements Runnable {
        private String ssid;
        private String password;
        private String cap;

        ConnectRunnable(String ssid, String password, String cap) {
            this.ssid = ssid;
            this.password = password;
            this.cap = cap;
        }

        @Override
        public void run() {
            mNetworkWifiManager.connect(ssid, password, cap);
            wifiLinkStatus.start(ssid);
        }
    }

    public interface GetMessageFormMobileListener {
        void onGetClientId(String clientId,boolean canFetchQQMusicVip);
    }


    public interface LoginSuccessListener{
        boolean isLoginSuccess();
    }

    public interface BleNetworkSkillListener {
        void onStartBleNetworkSkill();
        void onStopBleNetworkSkill();
    }

    private static class WifiLinkStatus {
        private int ALLOW_RETRY_COUNT = 2;
        private String name;
        private boolean startLink;
        private volatile boolean start;
        // 允许两次不等于对应的wifi名，出现wifi名改变然后又连接成功的情况
        private int count = ALLOW_RETRY_COUNT;

        void start(String name) {
            Log.d(TAG, " wifiName : start");
            startLink = false;
            this.name = name;
            start = true;
            count = ALLOW_RETRY_COUNT;
        }

        void stop() {
            start = false;
        }

        // 返回false表示连接失败
        boolean check(String wifiName) {
            Log.d(TAG, " wifiName : " + wifiName + " name : " + name);
            if(!start) {
                return true;
            }
            if(wifiName.startsWith("\""))
            {
                wifiName = wifiName.substring(1);
            }
            if(wifiName.endsWith("\"")) {
                wifiName = wifiName.substring(0, wifiName.length() - 1);
            }
            Log.d(TAG, " 2 wifiName : " + wifiName + " name : " + name);
            if (startLink) {
                if (wifiName != null && wifiName.equals(name)) {
                    return true;
                } else {
                    Log.d(TAG, " check false count ：" + count);
                    if(count-- < 0) {
                        start = false;
                        return false;
                    }
                }
            } else {
                if (wifiName != null && wifiName.equals(name)) {
                    Log.d(TAG, " wifiName : " + wifiName + " name : " + name);
                    startLink = true;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            return "WifiLinkStatus{" +
                    "name='" + name + '\'' +
                    ", startLink=" + startLink +
                    '}';
        }
    }
}
