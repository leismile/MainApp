/*
 *
 *  *
 *  * Copyright (c) 2008-2016 UBT Corporation.  All rights reserved.  Redistribution,
 *  *  modification, and use in source and binary forms are not permitted unless otherwise authorized by UBT.
 *  *
 *
 */

package com.ubtechinc.alpha.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtech.utilcode.utils.network.NetworkHelper;
import com.ubtech.utilcode.utils.notification.NotificationCenter;
import com.ubtech.utilcode.utils.thread.HandlerUtils;
import com.ubtech.utilcode.utils.thread.ThreadPool;
import com.ubtechinc.alpha.SystemProperties;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.appmanager.FirstStartManager;
import com.ubtechinc.alpha.appmanager.GetWeatherManager;
import com.ubtechinc.alpha.appmanager.LowPowerShutdowmManager;
import com.ubtechinc.alpha.appmanager.MainServiceStartManager;
import com.ubtechinc.alpha.appmanager.MotorManager;
import com.ubtechinc.alpha.appmanager.StateManager;
import com.ubtechinc.alpha.appmanager.SysStatusManager;
import com.ubtechinc.alpha.appmanager.UbtBatteryManager;
import com.ubtechinc.alpha.appmanager.UpgradeClient;
import com.ubtechinc.alpha.appmanager.VolumeManager;
import com.ubtechinc.alpha.appmanager.model.BandingSuccessEvent;
import com.ubtechinc.alpha.event.SidEvent;
import com.ubtechinc.alpha.im.msghandler.AccountApplyMsgHandler;
import com.ubtechinc.alpha.network.module.CheckBindRobotModule;
import com.ubtechinc.alpha.receiver.DateTimeReceiver;
import com.ubtechinc.alpha.robotinfo.RobotState;
import com.ubtechinc.alpha.service.sysevent.SysEventService;
import com.ubtechinc.alpha.utils.AlphaUtils;
import com.ubtechinc.alpha.utils.PowerUtils;
import com.ubtechinc.alpha.utils.SysTimeUtils;
import com.ubtechinc.alpha.utils.SystemPropertiesUtils;
import com.ubtechinc.alpha.utils.WifiUtils;
import com.ubtechinc.bluetoothlibrary.BleUtil;
import com.ubtechinc.bluetoothlibrary.UbtBluetoothConnManager;
import com.ubtechinc.bluetoothrobot.BleStatusManager;
import com.ubtechinc.bluetoothrobot.Robot2PhoneMsgMgr;
import com.ubtechinc.bluetoothrobot.UbtBleNetworkManager;
import com.ubtechinc.bluetoothrobot.old.BleConstants;
import com.ubtechinc.bluetoothrobot.old.LocalTTSHelper;
import com.ubtechinc.bluetoothrobot.old.OldUbtBleNetworkManager;
import com.ubtechinc.contact.Contact;
import com.ubtechinc.contact.IGetRobotId;
import com.ubtechinc.contact.PhoneListenerService;
import com.ubtechinc.nets.http.HttpProxy;
import com.ubtechinc.nets.http.ThrowableWrapper;
import com.ubtechinc.nets.im.business.LoginBussiness;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.express.listeners.AnimationListener;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.context.ContextRunnable;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.master.event.EventReceiver;
import com.ubtrobot.masterevent.protos.SysMasterEvent;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.speech.SpeechApi;
import com.ubtrobot.speech.protos.Speech;
import com.ubtrobot.speech.receivers.InitResultReceiver;
import com.ubtrobot.speech.receivers.TokenStateReceiver;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Event;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.zhaiyifan.appinit.Flow;
import cn.zhaiyifan.appinit.Init;
import cn.zhaiyifan.appinit.Task;
import event.master.ubtrobot.com.sysmasterevent.event.SysActiveEvent;
import ubtechinc.com.standupsdk.StandUpApi;

import static android.net.wifi.WifiManager.SUPPLICANT_STATE_CHANGED_ACTION;

/**
 * @author paul.zhang@ubtrobot.com
 * @date 2016/12/28
 * @Description 系统启动机器人主服务
 * @modifier logic.peng@ubtrobot.com
 * @modify_time 2017/4/19
 */

public class MainService /*extends ServiceBindable*/ implements IGetRobotId {

    private static final String TAG = "MainService";
    private static final int MSG_WATH_INIT_SYS_ENENT = 0x003;
    private static final int MSG_WATH_INIT_BLUETOOTH = 0x004;
    private Context mContext;
    private TokenStateReceiver tokenStateReceiver;
    private DateTimeReceiver dateTimeReceiver;
    private ProcessLifeKeyguard processLifeKeyguard;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WATH_INIT_SYS_ENENT:
                    initSysEventService();
                    break;
                case MSG_WATH_INIT_BLUETOOTH:
                    String sid = (String) msg.obj;
                    initBluetooth(sid);
                    break;
            }
        }
    };

    public void onStartOnce(Context context) {
        if(!SystemPropertiesUtils.getFirststart()){
            SystemPropertiesUtils.setFirststartStep("0");
        }
        if(isReallyPoweron()){
            MainServiceStartManager.getInstance().start();
        }
        UbtBatteryManager.getInstance().init(context);
        mContext = context;
        LogUtils.i("MainService---onStartOnce");
        ProxyServiceManager.get(context).initProxyService();
        SysStatusManager.getInstance().init();
        registerWifiBroadcast();
        registerDateTimeReceiver();
        openSenson();
        GetWeatherManager.getInstance().init();
        GetWeatherManager.getInstance().getCityWeather();
        StateManager.getInstance(context).init();
        init();
        registerFactoryReceiver();
    }

    private void registerFactoryReceiver() {
        final BroadcastReceiver factoryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final Bundle extras = intent.getExtras();
                final Boolean isOpened_test = (Boolean) extras.get("isOpened_test");
                Log.d("factoryReceiver", "isOpened_test:" + isOpened_test);
                if (isOpened_test) {
                    opendFactoryTest();
                } else {
                    closeFactoryTest();
                }
            }
        };

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.ubtechinc.factory.interrupt");
        mContext.registerReceiver(factoryReceiver, intentFilter);
    }

    private void opendFactoryTest() {
        SysActiveEvent currentStatusData = SysStatusManager.getInstance().getmCurrentStatusData();
        if (currentStatusData != null) {
            if (currentStatusData.getNewStatus() == SysMasterEvent.ActivieStatusType.STANDBY) {
                PowerUtils.get().backToNormal();
                MotorManager.getInstance().switchBoard(true, new ResponseListener() {
                    @Override
                    public void onResponseSuccess(Object o) {
                    }

                    @Override
                    public void onFailure(int i, @NonNull String s) {
                    }
                });
            }
        }
        AlphaUtils.stopService();
        SysStatusManager.getInstance().stopIntoStandupStandbyTimer();
        SysStatusManager.getInstance().stopActiveTimer();

    }

    private void closeFactoryTest() {
        String sid = RobotState.get().getSid();
        if (!TextUtils.isEmpty(sid)) {
            LoginBussiness.getInstance(mContext).login(sid);
        }
        SysStatusManager.getInstance().init();
        VolumeManager.getInstance().subscribeVolumeEvent();
        SpeechApi.get().startRecording();
        SysStatusManager.getInstance().startIntoStandupStandbyTimer();

    }

    /**
     * 打开蓝牙，wifi 和 4G
     */
    private void openSenson() {
        if (!BleUtil.isOpenBle()) {
            BleUtil.openBle();
        }
        WifiUtils.openWifi();
//        DefaultContactFunc.setMobileData(mContext, true);
    }


    private void initBluetooth(String sid) {
        UbtBluetoothConnManager.getInstance().init(mContext, "Mini_", sid);
        BleStatusManager.Companion.getInstance();
        Robot2PhoneMsgMgr.get().init();
        UbtBleNetworkManager.getInstance().init(mContext, sid);
        UbtBleNetworkManager.getInstance().setProductId("b0851325-3056-4853-921b-dcba21b491a3:8c901ad100ad44d98b6276adeb861058");
        OldUbtBleNetworkManager.getInstance().init(mContext, sid);
        OldUbtBleNetworkManager.getInstance().setProductId("b0851325-3056-4853-921b-dcba21b491a3:8c901ad100ad44d98b6276adeb861058");
        tokenStateReceiver = new TokenStateReceiver() {
            @Override
            public void onStateChange(Speech.TokenState tokenState, int errCode, String errMsg) {
                Log.i(TAG, "tokenState=====" + tokenState + "errCode====" + errCode + ";;;errMsg===" + errMsg);
                if (tokenState != Speech.TokenState.Success) {
                    if (!NetworkHelper.sharedHelper().isNetworkAvailable()) {
                        UbtBleNetworkManager.getInstance().sendErrorCodeToPhone(BleConstants.CONNENT_WIFI_FIALED_ERROR_CODE);
                        OldUbtBleNetworkManager.getInstance().sendErrorCodeToPhone(BleConstants.CONNENT_WIFI_FIALED_ERROR_CODE);
                    } else {
                        UbtBleNetworkManager.getInstance().sendErrorCodeToPhone(BleConstants.TVS_ERROR_CODE);
                        OldUbtBleNetworkManager.getInstance().sendErrorCodeToPhone(BleConstants.TVS_ERROR_CODE);
                    }
                } else {
                    try {
                        UbtBleNetworkManager.getInstance().sendMsgBingdingSuccessToPhone();
                        OldUbtBleNetworkManager.getInstance().sendMsgBingdingSuccessToPhone();
                        boolean isFirstStart = SystemPropertiesUtils.getFirststart();
                        LogUtils.i(TAG,"isFirstStart=====" + isFirstStart);
                        if(isFirstStart){
                            BandingSuccessEvent event = new BandingSuccessEvent();
                            NotificationCenter.defaultCenter().publish(event);
                        }else {
                            LocalTTSHelper.playBandingSuc();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        OldUbtBleNetworkManager.getInstance().setGetMessageFormMobileListener((clientId, canFetchQQMusicVip) -> {
            LogUtils.i(TAG, "clientId========" + clientId);
            Bundle bundle = new Bundle();
            bundle.putString("client_id", clientId);
            bundle.putString("scene", "bind");
            setTVSAccessToken(bundle);
            SpeechApi.get().subscribeEvent(tokenStateReceiver);
        });

        UbtBleNetworkManager.getInstance().setGetMessageFormMobileListener((clientId, canFetchQQMusicVip) -> {
            LogUtils.i(TAG, "clientId========" + clientId);
            Bundle bundle = new Bundle();
            bundle.putString("client_id", clientId);
            bundle.putString("scene", "bind");
            setTVSAccessToken(bundle);
            SpeechApi.get().subscribeEvent(tokenStateReceiver);
        });
    }

    public void init() {
        //TODO:线程数，可以根据实际情况确定
        Init.setThreadPoolSize(4);
        Init.init(mContext);
        //TODO: 暂时用不到
//        Task task0 = new Task("task0", true) {
//            @Override
//            protected void start() {
//                ConfigurationLoad.get(getApplicationContext()).load();
//                StringUtil.setLanguage(getApplicationContext(), RobotConfiguration.get().asr_Language);
//            }
//        };

        final Task task6 = new Task("task6", false) {
            @Override
            protected void start() {
                //todo: 获取胸口版版本号不要放在这里，此操作会导致较长时间的等待
//                VersionCollector.get(getApplicationContext()).requestVersion();
                try {
                    LogUtils.d(TAG, this.getName() + "run--------");
                    String sid = RobotState.get().getSid();
                    LogUtils.i("初始化客户端与机器人通信模块（IM）-- 序列号：" + RobotState.get().getSid());
                    //发出已获取到sid的事件后来触发腾讯IM初始化
                    NotificationCenter.defaultCenter().publish(new SidEvent());
                } catch (Exception e) {

                }
            }
        };

        //启动RobotState
        final Task task8 = new Task("task8", false) {

            @Override
            protected void start() {
                //打开robotState
                LogUtils.d(TAG, this.getName() + "run--------");
                try {
                    Intent service = new Intent();
                    service.setClassName("com.ubtrobot.master.policy", "alphamini.ubt.com.robotstatemanager.MainService");
                    mContext.startService(service);
                } catch (Exception e) {

                }
            }
        };
        final Task task15 = new Task("task15", false) {

            @Override
            protected void start() {
                LogUtils.d(TAG, this.getName() + "run--------");
                SysTimeUtils.autoUpdateTimeAndTimeZoo(mContext);
            }
        };


        final Task task18 = new Task("task18", false, 10000) {

            @Override
            protected void start() {
                LogUtils.d(TAG, this.getName() + "run--------");
                //启动电话服务
                // UserContact
                try {
                    Contact.getInstance().init(mContext.getApplicationContext(), MainService.this);

                    //放入MainService里启动
                    LogUtils.d(TAG, " AlphaApplication -- onCreate-----end");
                    mContext.startService(new Intent(mContext, PhoneListenerService.class));
                } catch (Exception e) {

                }
            }
        };

        final Task task19 = new Task("task19", false) {

            @Override
            protected void start() {
                LogUtils.d(TAG, this.getName() + "run--------");
                try {
                    //TODO:VolumeManager初始化可以适当往后放，因为SoundVolumesUtils会从assert里load音频文件
                    VolumeManager.getInstance();
                } catch (Exception e) {

                }
            }
        };

        final Task task20 = new Task("task20", false) {

            @Override
            protected void start() {
                LogUtils.d(TAG, this.getName() + "run--------");
                try {
                    initFallclimbServie();
                } catch (Exception e) {

                }
            }
        };

        final Task task21 = new Task("task21", false) {

            @Override
            protected void start() {
                LogUtils.d(TAG, this.getName() + "run--------");
                try {
                    startSpeechService();
                } catch (Exception e) {

                }
            }
        };

        final Task task22 = new Task("task22", false) {
            @Override
            protected void start() {
                LogUtils.d(TAG, this.getName() + "run--------");
                try {
                    String sid = RobotState.get().getSid();
                    if (TextUtils.isEmpty(sid)) {
                        Log.i(TAG, "获取不到序列号===========");
                        return;
                    }
                    Message message = new Message();
                    message.what = MSG_WATH_INIT_BLUETOOTH;
                    message.obj = sid;
                    mHandler.sendMessage(message);
                } catch (Exception e) {

                }
            }
        };

        final Task task23 = new Task("task23", false) {
            @Override
            protected void start() {
                try {
                    LogUtils.d(TAG, this.getName() + "run--------");
                    processLifeKeyguard = new ProcessLifeKeyguard(mContext.getApplicationContext());
                    processLifeKeyguard.start();
                } catch (Exception e) {

                }
            }
        };

        Flow flow = new Flow("main service init task");
        flow.addTask(1, task6)
                //.addTask(1, task7)
                .addTask(1, task8)
//                .addTask(1, task15)
                .addTask(1, task18)
                .addTask(1, task19)
                .addTask(1, task20)
                .addTask(1, task21)
                .addTask(1, task22)
                .addTask(1, task23)
        ;
//        Init.start(flow);

        ThreadPool.getInstance().submit(new ThreadPool.Job<Object>() {
            @Override
            public Object run(ThreadPool.JobContext jobContext) {
                task6.run();
                task15.run();
                return null;
            }
        });

//        ThreadPool.getInstance().submit(new ThreadPool.Job<Object>() {
//            @Override
//            public Object run(ThreadPool.JobContext jobContext) {
//                task20.run();
//                task21.run();
//                return null;
//            }
//        });

        ThreadPool.getInstance().submit(new ThreadPool.Job<Object>() {
            @Override
            public Object run(ThreadPool.JobContext jobContext) {
                task22.run();
                task23.run();
                return null;
            }
        });

        HandlerUtils.getMainHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ThreadPool.getInstance().submit(new ThreadPool.Job<Object>() {
                    @Override
                    public Object run(ThreadPool.JobContext jobContext) {
                        task20.run();
                        task21.run();
                        task8.run();
                        return null;
                    }
                });
            }
        }, 1000);

        HandlerUtils.getMainHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ThreadPool.getInstance().submit(new ThreadPool.Job<Object>() {
                    @Override
                    public Object run(ThreadPool.JobContext jobContext) {

                        task18.run();
                        task19.run();
                        return null;
                    }
                });
            }
        }, 10000);

    }

    /**
     * 初始化跌倒爬起服务
     */
    private void initFallclimbServie() {
        LogUtils.d(TAG, "initFallclimbServie");
        StandUpApi.getInstance().subscribeFallClimbServiceEvent(eventReceiver);
        StandUpApi.getInstance().startFallclimbService(new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {

            }

            @Override
            public void onFailure(Request request, CallException e) {

            }
        });
    }

    private EventReceiver eventReceiver = new EventReceiver() {
        @Override
        public void onReceive(MasterContext masterContext, Event event) {
            Log.i(TAG, "跌倒爬起服务开启成功==========");
            if (!fallClampReady) { //防止跌倒爬起服务重启，多次执行OnReady()
                fallClampReady = true;
                tryToCallOnReady();
            }
        }
    };

    private boolean isReallyPoweron() {
        String initStr = SystemProperties.getProperty("com.ubt.mainApp.initsucc", "no");
        LogUtils.d(TAG, "isReallyPoweron---initStr = " + initStr);
        if (initStr.equals("yes")) {
            return false;
        } else {
            return true;
        }
    }


    private void recordInitSucc() {
        //机器重启后，这个记录会清除掉
        SystemProperties.setProperty("com.ubt.mainApp.initsucc", "yes");
    }

    /**
     * 初始化系统状态管理
     */
    private void initSysEventService() {
        SysMasterEvent.BatteryStatusData batteryStatusData = UbtBatteryManager.getInstance().getBatteryInfo();
        int batteryLevel = batteryStatusData.getLevel();
        int levelStatus = batteryStatusData.getLevelStatus();
        int status = batteryStatusData.getStatus();
        Log.i(TAG, "开机时的电量=============" + batteryLevel + ";;levelStatus======" + levelStatus);
        Log.i(TAG, "isReallyPoweron===========" + isReallyPoweron());
        if (isReallyPoweron()) {
            ExpressApi.get().doExpress("bootSuccess", 1, false, Priority.HIGH, new AnimationListener() {
                @Override
                public void onAnimationStart() {
                }

                @Override
                public void onAnimationEnd() {
                    Log.i(TAG,"onAnimationEnd===11111======");
                }

                @Override
                public void onAnimationRepeat(int i) {
                }
            });
        }
        boolean isFirstStart = SystemPropertiesUtils.getFirststart();
        LogUtils.i(TAG, "isFirstStart======" + isFirstStart);
        if(isFirstStart){
            //TODO 执行首次开机流程
            FirstStartManager.get().playIntroduceTTs();
        }else {
            if (!AlphaUtils.isShutDowning) {
                if (batteryLevel <= 10 && status != android.os.BatteryManager.BATTERY_PLUGGED_AC) {
                    LowPowerShutdowmManager.getInstance().startShutdownTimer();
                    SysStatusManager.getInstance().startIntoStandupStandbyTimer();
                } else if (levelStatus == 0) {
                    SpeechApi.get().startRecognize();
                    Master.get().execute(SysEventService.class, new ContextRunnable<SysEventService>() {
                        @Override
                        public void run(SysEventService sysEventService) {
                            sysEventService.lowPowerStatus(UbtBatteryManager.getInstance().getBatteryStatsParam());
                            SysStatusManager.getInstance().startIntoStandupStandbyTimer();
                        }
                    });
                } else {
                    SpeechApi.get().startRecognize();
                    MotorManager.getInstance().powerOnMotorsStatus();
                    if (UpgradeClient.get().isUpgradeSuccess()) {
                        AlphaUtils.ttsMessage("升级成功，我又变得更聪明了呢！");
                    } else {
                        if (isReallyPoweron()) {
                            AlphaUtils.ttsMessage("ready_001");
                        }
                    }
                    SysStatusManager.getInstance().startIntoStandupStandbyTimer();
                }
            }
        }
        recordInitSucc();

    }


    /**
     * 开启语音服务
     */
    private void startSpeechService() {
        LogUtils.d(TAG, "startSpeechService");
        SpeechApi.get().subscribeEvent(initResultReceiver);
    }

    public InitResultReceiver initResultReceiver = new InitResultReceiver() {
        @Override
        public void onResult(Speech.InitResult data) {
            LogUtils.d(TAG, "语音服务初始化结果 ： " + (data.getResultCode() == 0));
            if (!speechReady) { //防止多次语音服务重启，多次执行OnReady()
                speechReady = true;
                tryToCallOnReady();
            }

            checkBindStatus();
            listenNetworkStatus();
        }
    };


    NetworkHelper.NetworkInductor mNetworkInductor = new NetworkHelper.NetworkInductor() {
        @Override
        public void onNetworkChanged(NetworkHelper.NetworkStatus networkStatus) {
            if (networkStatus != NetworkHelper.NetworkStatus.NetworkNotReachable) {
                checkBindStatus();
            }
        }
    };

    /**
     * 监听网络变化
     */
    private void listenNetworkStatus() {
        NetworkHelper.sharedHelper().addNetworkInductor(mNetworkInductor);
    }

    private void unListenNetworkStatus() {
        NetworkHelper.sharedHelper().removeNetworkInductor(mNetworkInductor);
    }

    /**
     * 语音服务初始化后重新检查bind状态
     */
    private void checkBindStatus() {
        final CheckBindRobotModule.Request request = new CheckBindRobotModule.Request(getRobotId());

        HttpProxy.get().doGet(request, new com.ubtechinc.nets.ResponseListener<CheckBindRobotModule.Response>() {
            @Override
            public void onError(ThrowableWrapper e) {
                LogUtils.d(TAG, String.format("checkBindStatus onError -- e : %s", Log.getStackTraceString(e)));
            }

            @Override
            public void onSuccess(CheckBindRobotModule.Response response) {
                if (response != null) {
                    List<CheckBindRobotModule.User> users = response.getData().getResult();
                    if (users == null || users.size() == 0) {
                        LogUtils.d(TAG, "checkBindStatus no BindUsers, unbindTVSToken.");
                        unbindTVSToken();
                    } else {
                        LogUtils.d(TAG, "checkBindStatus Bind Users: " + users.size() + " users.");
                    }
                }
            }
        });

    }

    /**
     * unbind tvs accesstoken, from {@link AccountApplyMsgHandler#unbindTVSToken()}
     */
    public void unbindTVSToken() {
        //TODO:临时改成广播方案， 后续等接入了终端通信模块再改过来
        Intent intent = new Intent("tvs.unbind.accesstoken");
        AlphaApplication.getContext().sendBroadcast(intent);
    }

    private boolean speechReady = false;
    private boolean fallClampReady = false;

    private void tryToCallOnReady() {
        if (speechReady && fallClampReady) {
            onReady();
        }
    }

    private void onReady() {
        mHandler.sendEmptyMessage(MSG_WATH_INIT_SYS_ENENT);
    }

    public void onDestroy() {
        if (mWifiStateReceiver != null) {
            mContext.unregisterReceiver(mWifiStateReceiver);
        }
        if (dateTimeReceiver != null) {
            mContext.unregisterReceiver(dateTimeReceiver);
            dateTimeReceiver = null;
        }
        if (tokenStateReceiver != null) {
            SpeechApi.get().unsubscribeEvent(tokenStateReceiver);
        }
        if (initResultReceiver != null) {
            SpeechApi.get().unsubscribeEvent(initResultReceiver);
        }
        if (eventReceiver != null) {
            StandUpApi.getInstance().unsubscribeFallClimbServiceEvent(eventReceiver);
        }

        unListenNetworkStatus();
    }


    public void setTVSAccessToken(Bundle bundle) {
        //TODO:临时改成广播方案， 后续等接入了终端通信模块再改过来
        Intent intent = new Intent("tvs.set.accesstoken");
        intent.putExtra("access_token", bundle);
        AlphaApplication.getContext().sendBroadcast(intent);
        LogUtils.d("TVSAccessToken", "setTVSAccessToken---Has Send Broadcast");
    }

    private void registerWifiBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(SUPPLICANT_STATE_CHANGED_ACTION);
        mContext.registerReceiver(mWifiStateReceiver, filter);
    }

    private void registerDateTimeReceiver() {
        dateTimeReceiver = new DateTimeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        mContext.registerReceiver(dateTimeReceiver, filter);
    }

    private NetworkInfo.State mState = NetworkInfo.State.CONNECTED;
    private Timer mDisconnectedTimer;
    private TimerTask mDisconnectedTask;
    private boolean isPlayDisconnectTTs = false;

    private void startDisconnectTimer() {
        mDisconnectedTimer = new Timer();
        mDisconnectedTask = new TimerTask() {
            @Override
            public void run() {
                if (mState == NetworkInfo.State.DISCONNECTED && !isPlayDisconnectTTs) {
                    isPlayDisconnectTTs = true;
                    VoicePool.get().playTTs("网络连接中断", Priority.NORMAL, new VoiceListener() {

                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(int i, String s) {
                        }
                    });
                    stopDisconnectTimer();
                }
            }
        };
        mDisconnectedTimer.schedule(mDisconnectedTask, 5 * 1000);
    }

    private void stopDisconnectTimer() {
        if (mDisconnectedTimer != null) {
            mDisconnectedTimer.cancel();
            mDisconnectedTimer = null;
        }
        if (mDisconnectedTask != null) {
            mDisconnectedTask.cancel();
            mDisconnectedTask = null;
        }
    }

    private BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "action========" + action);
            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                Parcelable parcelableExtra = intent
                        .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (null != parcelableExtra) {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    mState = networkInfo.getState();
                    Log.i(TAG, "state=======" + mState);
                    EventBus.getDefault().post(mState);
                    if (mState == NetworkInfo.State.CONNECTED) {
                        isPlayDisconnectTTs = false;
                        GetWeatherManager.getInstance().getCityWeather();
                    }
                    stopDisconnectTimer();
                    startDisconnectTimer();
                }
            }
        }
    };

    @Override
    public String getRobotId() {
        return RobotState.get().getSid();
    }
}
