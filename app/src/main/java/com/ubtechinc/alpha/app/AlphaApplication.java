/*
 *
 *  *
 *  * Copyright (c) 2008-2016 UBT Corporation.  All rights reserved.  Redistribution,
 *  *  modification, and use in source and binary forms are not permitted unless otherwise authorized by UBT.
 *  *
 *
 */

package com.ubtechinc.alpha.app;

import android.content.Context;
import android.util.Log;

import com.ubtech.utilcode.utils.FileUtils;
import com.ubtech.utilcode.utils.LogUtils;
import com.ubtech.utilcode.utils.ProcessUtils;
import com.ubtech.utilcode.utils.app.ACContext;
import com.ubtech.utilcode.utils.bugly.CrashReporter;
import com.ubtech.utilcode.utils.network.NetworkHelper;
import com.ubtechinc.alpha.im.Robot2PhoneMsgMgr;
import com.ubtechinc.alpha.utils.ServiceUtils;
import com.ubtechinc.contact.Contact;
import com.ubtechinc.contact.IGetRobotId;
import com.ubtechinc.services.alphamini.BuildConfig;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.log.InfrequentLoggerFactory;
import com.ubtrobot.master.policy.BasePolicyApplication;
import com.ubtrobot.mini.libs.scenes.EmotionStore;
import com.ubtrobot.mini.libs.scenes.SceneScaner;
import com.ubtrobot.mini.properties.sdk.Path;
import com.ubtrobot.mini.properties.sdk.PropertiesApi;

import com.ubtrobot.mini.properties.sdk.Path;
import com.ubtrobot.mini.properties.sdk.PropertiesApi;
import com.ubtrobot.sys.SysApi;
import com.ubtrobot.ulog.FwLoggerFactory;
import java.io.File;
import java.util.Map;

import event.master.ubtrobot.com.sysmasterevent.SysEventApi;
import timber.log.Timber;


/**
 * @author paul.zhang@ubtrobot.com
 * @date 2016/12/26
 * @Description 全局实例, 应用程序入口
 * @modifier logic.peng
 * @modify_time 2017/5/11
 */

public class AlphaApplication extends BasePolicyApplication {
    private static final String TAG = "AlphaApplication";
    private static Context mContext = null;


    public static Context getContext() {
        return mContext;
    }

    private synchronized void setContext(Context context) {
        AlphaApplication.mContext = context;
    }

    @Override
    public void onApplicationCreate() {
        super.onApplicationCreate();
        if (ProcessUtils.isMainProcess(this)) {
            if (BuildConfig.SDResourceEnable) {
                PropertiesApi.setRootPath(Path.DIR_MINI_FILES_SDCARD_ROOT);
            }
            LogUtils.init(true, true, "MainApp");
            LogUtils.d(TAG, " AlphaApplication -- onCreate-----start");

            // 初始化服务总线
            Master.initialize(this);
            FwLoggerFactory.setup(new InfrequentLoggerFactory());
            //设置全局上下文
            setContext(this.getApplicationContext());
            //setprop log.tag.alpha VERBOSE 可打开v级别，默认是i级别

            initTimber();
            //实例化文件夹
            ACContext.initInstance(getContext());
            //情感体系初始化
            initEmotionSystem();

            //注册网络状态广播
            NetworkHelper.sharedHelper().registerNetworkSensor(this);
            //im初始化
            Robot2PhoneMsgMgr.getInstance().init();
            ServiceUtils.startService(this);
            Contact.getInstance().init(this, new IGetRobotId() {
                @Override
                public String getRobotId() {
                    return "";
                }
            });
//            Master.get().setLoggerFactory(new AndroidLoggerFactory());
            LogUtils.d(TAG, " AlphaApplication -- onCreate-----end");
            CrashReporter.init(this, "9c1afa2631", BuildConfig.DEBUG, 10000, new CrashReporter.onCrashHandler() {
                @Override
                public String getRobotId() {
                    return SysApi.get().readRobotSid();
                }

                @Override
                public Map<String, String> onCrashHappend() {
                    return null;
                }
            });
        }
    }

    private void initEmotionSystem() {
        final File ACTION_ROOT_DIR = new File(PropertiesApi.getRootPath(), "actions");
        final File EXPRESS_ROOT_DIR = new File(PropertiesApi.getRootPath(), "expresss");
        final File BEHAVIOR_ROOT_DIR = new File(PropertiesApi.getRootPath(), "behaviors");
        final File SCENE_ROOT_DIR = new File(PropertiesApi.getRootPath(), "scenes");
        final File MUSIC_ROOT_DIR = new File(PropertiesApi.getRootPath(), "music");
        FileUtils.createOrExistsDir(ACTION_ROOT_DIR);
        FileUtils.createOrExistsDir(EXPRESS_ROOT_DIR);
        FileUtils.createOrExistsDir(BEHAVIOR_ROOT_DIR);
        FileUtils.createOrExistsDir(SCENE_ROOT_DIR);
        FileUtils.createOrExistsDir(MUSIC_ROOT_DIR);
        EmotionStore.setMaster(0);
        SceneScaner.scan();
    }



    private void initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new Timber.Tree() {
                @Override
                protected void log(int priority, String tag, String message, Throwable t) {
                    if (priority >= Log.WARN) {
                        LogUtils.w(message);
                    }
                }
            });
        }
    }

}