package com.ubtechinc.alpha.skillmanager;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.google.protobuf.StringValue;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.skill.MasterSkill;
import com.ubtrobot.master.skill.SkillStopCause;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CustomerSkill extends MasterSkill {

    public static final String TAG = "CustomerSkill";

    private List<AppInfo> mApps = new ArrayList<>();
    private ProcessLifeKeyguard mProcessLifeKeyguard;

    private ProcessLifeKeyguard.ProcessDiedObserver mProcessDiedObserver = new ProcessLifeKeyguard.ProcessDiedObserver() {
        @Override
        public void onProcessDied(int pid, int uid) {
            synchronized (mApps) {
                mApps.remove(new AppInfo("", pid));
                Log.d(TAG, "ManagerSkill - Process " + pid + " died.");
                if (mApps.isEmpty()) {
                    stopSkill();
                }
            }
        }
    };

    @Override
    protected void onSkillStart() {
        if (mProcessLifeKeyguard == null) {
            mProcessLifeKeyguard = new ProcessLifeKeyguard();
            mProcessLifeKeyguard.start();
        }
        ProcessLifeKeyguard.subscribeProcessDied(mProcessDiedObserver);
    }

    @Override
    protected void onSkillStop(SkillStopCause skillStopCause) {
        sendBroadcast(new Intent("customer.skill.stop"));
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO: 2018/6/23 10s后kill所有apps
                SystemClock.sleep(10000);
                for (AppInfo app : mApps) {
                    // TODO: 2018/6/23 kill all apps
                    stopPackage(app.packageName);
                }
                if (mProcessLifeKeyguard != null) {
                    mProcessLifeKeyguard.unregisterProcessObserver();
                    mProcessLifeKeyguard = null;
                }
            }
        }).start();

    }

    private void stopPackage(String packageName) {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        try {
            Method forceStopPackage = ActivityManager.class.getMethod("forceStopPackage", String.class);
            forceStopPackage.invoke(activityManager, packageName);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onCall(Request request, Responder responder) {

    }

    @Call(path = "/customer/start")
    public void onStartX(Request request, final Responder responder) {
        Log.d(TAG, "onStartX: " + request.getPath() + ", " + request.getId());
        try {
            StringValue stringValue = ProtoParam.from(request.getParam(), StringValue.class).getProtoMessage();
            String s = stringValue.getValue();
            addApp(s);
        } catch (ProtoParam.InvalidProtoParamException e) {
            e.printStackTrace();
        }

        responder.respondSuccess();
    }

    private void addApp(String s) {
        if (!TextUtils.isEmpty(s)) {
            String[] split = s.split(":");
            if (split.length == 2) {
                AppInfo appInfo = new AppInfo(split[0], Integer.parseInt(split[1]));
                synchronized (mApps) {
                    if (!mApps.contains(appInfo)) {
                        Log.d(TAG, "ManagerSkill - addApp:  " + appInfo);
                        mApps.add(appInfo);
                    }
                }

            }
        }
    }

    private static class AppInfo {
        public int pid;
        public String packageName;

        public AppInfo(String packageName, int pid) {
            this.packageName = packageName;
            this.pid = pid;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof AppInfo) {
                AppInfo name = (AppInfo) obj;
                return (pid == name.pid);
            }
            return super.equals(obj);
        }

        @Override
        public String toString() {
            return String.format("AppInfo@{packageName = %s, pid = %d}", packageName, pid);
        }
    }
}
