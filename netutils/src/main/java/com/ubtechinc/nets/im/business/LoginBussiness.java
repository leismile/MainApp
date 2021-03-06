package com.ubtechinc.nets.im.business;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMConnListener;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMSdkConfig;
import com.tencent.imsdk.TIMUserConfig;
import com.tencent.imsdk.TIMUserStatusListener;
import com.tencent.imsdk.ext.message.TIMManagerExt;
import com.tencent.imsdk.ext.message.TIMUserConfigMsgExt;
import com.ubtech.utilcode.utils.LogUtils;
import com.ubtech.utilcode.utils.MD5Utils;
import com.ubtech.utilcode.utils.network.NetworkHelper;
import com.ubtech.utilcode.utils.notification.NotificationCenter;
import com.ubtechinc.alpha.event.IMLoginResultEvent;
import com.ubtechinc.nets.BuildConfig;
import com.ubtechinc.nets.ResponseListener;
import com.ubtechinc.nets.http.HttpProxy;
import com.ubtechinc.nets.http.ThrowableWrapper;
import com.ubtechinc.nets.im.event.IMStateChange;
import com.ubtechinc.nets.im.model.UserInfo;
import com.ubtechinc.nets.im.modules.FindBindModule;
import com.ubtechinc.nets.utils.PingUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

//import com.ubtech.utilcode.utils.Log;


/**
 * Created by Administrator on 2016/11/29.
 */
public class LoginBussiness {

    public static final String TAG = "LoginBussiness";
    private static final String IM_GETINFO_URL = "im/getInfo";
    private static LoginBussiness sInstance;
    private Context context;
    private UserInfo userInfo;
    private boolean loginSucc = false;

    public static final int MSG_WHAT_PING = 100;

    private HandlerThread handlerThread;

    private Handler netCheckHandle;

    NetworkHelper.NetworkInductor netWorkInductor = null;  //持有一下，防止被回收 ，NetworkHelper内部是弱引用持有

    public static LoginBussiness getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LoginBussiness.class) {
                if (sInstance == null) {
                    sInstance = new LoginBussiness(context);
                }
            }
        }
        return sInstance;
    }

    public void login(String userId) {
        loginSucc = false;
        LogUtils.d(TAG, "login --- userId = " + userId);
        if (userInfo == null) {
            userInfo = new UserInfo();
        }
//        initHandleThread();
        userInfo.setId(userId);
        queryUserInfo();
    }

    public LoginBussiness(Context context) {
        this.context = context;
        init();
    }

    TIMConnListener timConnListener = new TIMConnListener() {
        @Override
        public void onConnected() {
            loginSucc = true;
            EventBus.getDefault().post(new IMStateChange(IMStateChange.STATE_CONNECTED));
            LogUtils.d(TAG, "IM---onConnected");
            cancelCheckNetIsAvailable();
        }

        @Override
        public void onDisconnected(int i, String s) {
            EventBus.getDefault().post(new IMStateChange(IMStateChange.STATE_DISCONNECTED));
            boolean netAvailable = NetworkHelper.sharedHelper().isNetworkAvailable();
            LogUtils.d(TAG, "IM---onDisconnected--i=" + i + ", msg = " + s);
            if (netAvailable) {
                checkNetIsAvailable();
            }
            loginSucc = false;
        }

        @Override
        public void onWifiNeedAuth(String s) {

            EventBus.getDefault().post(new IMStateChange(IMStateChange.STATE_WIFI_NEED_AUTH));
            LogUtils.d(TAG, "IM---onWifiNeedAuth-- msg = " + s);
        }

    };

    TIMUserStatusListener userStatusListener = new TIMUserStatusListener() {
        @Override
        public void onForceOffline() { //被踢下线

            EventBus.getDefault().post(new IMStateChange(IMStateChange.STATE_FORCE_OFFLINE));
            LogUtils.d(TAG, "IM--onForceOffline");
        }

        @Override
        public void onUserSigExpired() {
            LogUtils.d(TAG, "IM--onUserSigExpired---票据过期");
            loginSucc = false;
            queryUserInfo();
            EventBus.getDefault().post(new IMStateChange(IMStateChange.STATE_USER_SIG_EXPIRED));
        }
    };

    private void init() {
        TIMUserConfig timUserConfig = new TIMUserConfig();
        timUserConfig.setConnectionListener(timConnListener)
                .setUserStatusListener(userStatusListener);
        TIMUserConfigMsgExt timUserConfigMsgExt = new TIMUserConfigMsgExt(timUserConfig);
        timUserConfigMsgExt.enableAutoReport(false);
        timUserConfigMsgExt.enableRecentContact(false);
        TIMManager.getInstance().setUserConfig(timUserConfigMsgExt);
        NetworkHelper.sharedHelper().addNetworkInductor(netWorkInductor = new NetworkHelper.NetworkInductor() {

            @Override
            public void onNetworkChanged(NetworkHelper.NetworkStatus networkStatus) {
                LogUtils.d(TAG, "onNetworkChanged----net available :" + NetworkHelper.sharedHelper().isNetworkAvailable());
                if (NetworkHelper.sharedHelper().isNetworkAvailable()) {
                    if (!loginSucc) {
                        if (needQueryFromServer()) {
                            queryUserInfo(); //先从后台获取信息，再登录IM
                        } else {
                            realLoginIM(); //IM sdk自身会在连上网络后进行重新登录
                        }
                    }
                } else {
                    loginSucc = false;
                }
            }
        });
    }


    private boolean needQueryFromServer() {
        if (userInfo == null || !userInfo.hasGetAllInfo()) {
            return true;
        }
        return false;
    }

    private void realLoginIM() {
        if (isLoginIM()) {
            return;
        }
        TIMSdkConfig timSdkConfig = new TIMSdkConfig(Integer.valueOf(userInfo.getAppidAt3rd()));
        timSdkConfig.setAccoutType(userInfo.getAccountType());
        timSdkConfig.enableCrashReport(false);
        TIMManager.getInstance().init(context, timSdkConfig);
        //发起登录请求
        if (!TextUtils.isEmpty(TIMManager.getInstance().getLoginUser())) {
            TIMManager.getInstance().logout(new TIMCallBack(){

                @Override
                public void onError(int i, String s) {
                    TIMManager.getInstance().login(userInfo.getId(), userInfo.getUserSig(), mLoginCallBack);
                }

                @Override
                public void onSuccess() {
                    TIMManager.getInstance().login(userInfo.getId(), userInfo.getUserSig(), mLoginCallBack);
                }
            });
        }else{
            TIMManager.getInstance().login(userInfo.getId(), userInfo.getUserSig(), mLoginCallBack);
        }
    }

    public boolean isLoginIM() {
//        boolean isLoginIM =false;
//
//        if(TIMManager.getInstance().getLoginUser() == null){
//            Log.i(TAG, "userInfo getLoginUser为null");
//        }
//        if(userInfo != null && userInfo.getId()!= null && TIMManager.getInstance().getLoginUser() != null){
//            isLoginIM =true;
//        }
//        Log.d(TAG,"isLoginIM : "+isLoginIM);
//        return isLoginIM;
        return loginSucc;
    }

    private int hasTryCount = 0;

    //需要监听网络变化，当连上网络时需要再次重试
    private void queryUserInfo() {

        LogUtils.d(TAG, "queryUserInfo----CHANNEL_ID = "+BuildConfig.CHANNEL_ID);

        if (userInfo == null) {
            return;
        }
        final FindBindModule.Request bindRequest = new FindBindModule.Request();
        bindRequest.equipmentId = userInfo.getId();

        HashMap<String, String> map = new HashMap<>();
        long time = System.currentTimeMillis();
        String md5 = MD5Utils.md5("IM$SeCrET" + time, 32);
        map.put("signature", md5);
        map.put("time", String.valueOf(time));
        map.put("userId", userInfo.getId());
        map.put("channel", BuildConfig.CHANNEL_ID);
        HttpProxy.get().doGet(IM_GETINFO_URL, map, new ResponseListener<FindBindModule.Response>() {

            @Override
            public void onError(ThrowableWrapper e) {
                if (NetworkHelper.sharedHelper().isNetworkAvailable()) {
                    hasTryCount++;
                    if (hasTryCount <= 3) {

                        LogUtils.e(TAG, "get---im/getInfo--onError--- try again--hasTryCount = " + hasTryCount);
                        try {
                            Thread.sleep(500 * (hasTryCount + 1));
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        queryUserInfo();
                    } else {
                        LogUtils.e(TAG, "get---im/getInfo--onError--- hasTryCount " + hasTryCount);
                    }
                } else {
                    LogUtils.e(TAG, "get---im/getInfo--onError--- no network");

                }
                e.getCause().printStackTrace();
            }

            @Override
            public void onSuccess(FindBindModule.Response response) {

                LogUtils.d(TAG, "get---im/getInfo--onSuccess---");

                hasTryCount = 0;
                if (response != null) {
                    fillUserInfo(response.returnMap);
                    realLoginIM();
                }
            }

        });

    }

    private void fillUserInfo(FindBindModule.RobotData userData) {

        if (userInfo == null) {
            userInfo = new UserInfo();
        }
        userInfo.setUserSig(userData.userSig);
        userInfo.setAccountType(userData.accountType);
        userInfo.setAppidAt3rd(userData.appidAt3rd);
        Log.d(TAG, "fillUserInfo--userInfo = " + userInfo);
    }

    private TIMCallBack mLoginCallBack = new TIMCallBack() {
        @Override
        public void onSuccess() {

            LogUtils.d(TAG, "Login tencent IM success");

            loginSucc = true;
            //通过消息通信告知上层
            IMLoginResultEvent event = new IMLoginResultEvent();
            event.success = true;
            NotificationCenter.defaultCenter().publish(event);
        }

        @Override
        public void onError(int code, java.lang.String desc) {

            LogUtils.e(TAG, "Login tencent IM failed. code: " + code + "" + " errmsg: " + desc);

            IMLoginResultEvent event = new IMLoginResultEvent();
            loginSucc = false;
            event.success = false;
            //登录失败，不断重试

            NotificationCenter.defaultCenter().publish(event);
            //如果失败，则重试
            if (NetworkHelper.sharedHelper().isNetworkAvailable()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                realLoginIM();
            }
        }
    };

    public void setUserId(String userId) {
        if (userInfo == null) {
            userInfo = new UserInfo();
        }
        userInfo.setId(userId);
    }

    public void logout() {
        TIMManager.getInstance().logout(new TIMCallBack() {
            @Override
            public void onError(int code, String desc) {
                LogUtils.d(TAG, "logout--onError-- code : " + code + ", desc = " + desc);
            }

            @Override
            public void onSuccess() {
                LogUtils.d(TAG, "logout---onSuccess");
            }
        });
    }

    private void checkNetIsAvailable() {
        LogUtils.d(TAG, "checkNetIsAvailable");
        initHandleThread();
        netCheckHandle.sendEmptyMessageDelayed(MSG_WHAT_PING, 60000);
    }

    private void cancelCheckNetIsAvailable(){
        LogUtils.d(TAG, "cancelCheckNetIsAvailable");
        if(netCheckHandle != null && netCheckHandle.hasMessages(MSG_WHAT_PING)){
            netCheckHandle.removeMessages(MSG_WHAT_PING);
        }
    }

    private void initHandleThread() {
        if (handlerThread == null) {
            handlerThread = new HandlerThread("net_check_thread");
            handlerThread.start();
            netCheckHandle = new Handler(handlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (MSG_WHAT_PING == msg.what) {
                        if(!loginSucc){
                            LogUtils.d(TAG, "ping net Available start");
                            boolean result = PingUtil.pingBaidu();
                            LogUtils.d(TAG, "ping net Available result = " + result);
                            if(!loginSucc){
                                if (result) {
                                    realLoginIM();
                                } else {
                                    checkNetIsAvailable();
                                }
                            }
                        }

                    }
                }
            };
        }

    }
}
