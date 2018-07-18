package com.ubtechinc.alpha.service;


import android.app.IProcessObserver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @data 创建时间：2018/5/4
 * @author：bob.xu
 * @Description :进程守护模块
 */
public class ProcessLifeKeyguard {
    private static List<ProcessDiedObserver> observers = new ArrayList<>();
    private ProcessRegisterBroadcastRvr broadcastRvr;
    private Context context;
    private Map<Integer, UbtComponentInfo> keyguardPidMap = new HashMap<>(); //需要守护的进程列表<pid,pkg>

    public ProcessLifeKeyguard(Context context) {
        this.context = context;
    }

    public static void subscribeProcessDied(ProcessDiedObserver observer) {
        observers.add(observer);
    }

    public static void unsubscribeProcessDied(ProcessDiedObserver observer) {
        observers.remove(observer);
    }

    public void start() {
        broadcastRvr = new ProcessRegisterBroadcastRvr(context);
        broadcastRvr.startRegister();
        registerProcessObserver();
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
            UbtComponentInfo componentInfo = keyguardPidMap.get(pid);
            //立马重启进程
            if (componentInfo != null) {
                if (componentInfo.componentType.equals("Service")) {
                    try {
                        Intent serviceIntent = new Intent();
                        serviceIntent.setClassName(componentInfo.pkgName, componentInfo.componentName);
                        context.startService(serviceIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (componentInfo.componentType.equals("Activity")) {
                    try {
                        Intent activityIntent = new Intent();
                        activityIntent.setClassName(componentInfo.pkgName, componentInfo.componentName);
                        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(activityIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            //删除记录
            keyguardPidMap.remove(pid);

        }
    };

    public interface ProcessDiedObserver {
        void onProcessDied(int pid, int uid);
    }

    /**
     * @data 创建时间：2018/5/4
     * @author：bob.xu
     * @Description: 需要守护的进程，通过发广播的方式向其报备包名与pid
     */
    public class ProcessRegisterBroadcastRvr extends BroadcastReceiver {
        private Context context;
        public final static String PROCESS_REGISTER_ACTION = "com.ubt.process.register.action";
        private final static String PKG_NAME = "pkg_name";
        private final static String PID = "pid";
        private final static String RESTART_COMPONENT_NAME = "restart_comp_name"; //需要重启的组件的名称
        private final static String RESTART_COMPONENT_TYPE = "restart_comp_type"; //需要重启的组件的类型， 支持Actvity、Service


        public ProcessRegisterBroadcastRvr(Context context) {
            this.context = context;
        }

        public void startRegister() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(PROCESS_REGISTER_ACTION);
            context.registerReceiver(this, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String pkgName = intent.getStringExtra(PKG_NAME);
            int pid = intent.getIntExtra(PID, 0);
            String componentName = intent.getStringExtra(RESTART_COMPONENT_NAME);
            String componentType = intent.getStringExtra(RESTART_COMPONENT_TYPE);
            UbtComponentInfo componentInfo = new UbtComponentInfo(pkgName, componentName, componentType);
            keyguardPidMap.put(pid, componentInfo);
        }
    }

    private class UbtComponentInfo {
        String pkgName;
        String componentName;
        String componentType;

        public UbtComponentInfo(String pkgName, String componentName, String componentType) {
            this.pkgName = pkgName;
            this.componentName = componentName;
            this.componentType = componentType;
        }
    }
}
