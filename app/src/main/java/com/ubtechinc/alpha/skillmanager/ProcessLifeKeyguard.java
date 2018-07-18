package com.ubtechinc.alpha.skillmanager;


import android.app.IProcessObserver;
import android.os.RemoteException;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @data 创建时间：2018/5/4
 * @author：bob.xu
 * @Description :进程守护模块
 */
public class ProcessLifeKeyguard {

    private boolean mIsStarted = false;
    private static List<ProcessDiedObserver> observers = new ArrayList<>();

    public static void subscribeProcessDied(ProcessDiedObserver observer) {
        observers.add(observer);
    }

    public static void unsubscribeProcessDied(ProcessDiedObserver observer) {
        observers.remove(observer);
    }

    public void start() {
        if (!mIsStarted) {
            registerProcessObserver();
        }
    }

    boolean registerProcessObserver() {
        try {
            Class<?> activityManagerNative = Class.forName("android.app.ActivityManagerNative");
            Method getDefaultMethod = activityManagerNative.getMethod("getDefault");
            Object iActivityManager = getDefaultMethod.invoke((Object) null, (Object[]) null);
            if (iActivityManager != null) {
                Method registerMethod =
                        activityManagerNative.getMethod("registerProcessObserver", IProcessObserver.class);

                registerMethod.invoke(iActivityManager, mProcessObserver);
                mIsStarted = true;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    void unregisterProcessObserver() {
        try {
            Class<?> activityManagerNative = Class.forName("android.app.ActivityManagerNative");
            Method getDefaultMethod = activityManagerNative.getMethod("getDefault");
            Object iActivityManager = getDefaultMethod.invoke((Object) null, (Object[]) null);
            if (iActivityManager != null) {

                Method registerMethod =
                        activityManagerNative.getMethod("unregisterProcessObserver", IProcessObserver.class);

                registerMethod.invoke(iActivityManager, mProcessObserver);
                mIsStarted = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private IProcessObserver mProcessObserver = new IProcessObserver.Stub() {

        @Override
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            //Log.i(TAG, "onForegroundActivitiesChanged: pid " + pid + " uid " + uid);
        }

        @Override
        public void onProcessStateChanged(int pid, int uid, int procState)
                throws RemoteException {
            //Log.i(TAG, "onProcessStateChanged: pid=" + pid + " uid=" + uid + " state=" + procState);
        }

        @Override
        public void onProcessDied(int pid, int uid) {
            Log.i("ProcessLifeKeyguard", "onProcessDied: pid " + pid + " uid " + uid);

            if (observers.size() != 0) {
                for (ProcessDiedObserver observer :
                        observers) {
                    observer.onProcessDied(pid, uid);
                }
            }
        }
    };

    public interface ProcessDiedObserver {
        void onProcessDied(int pid, int uid);
    }

}
