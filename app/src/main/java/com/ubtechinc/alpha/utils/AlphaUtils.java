package com.ubtechinc.alpha.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.appmanager.FaceDetectManager;
import com.ubtechinc.alpha.appmanager.FirstStartManager;
import com.ubtechinc.alpha.appmanager.InfraRedManager;
import com.ubtechinc.alpha.appmanager.MotorManager;
import com.ubtechinc.alpha.appmanager.SmallActionManager;
import com.ubtechinc.alpha.appmanager.SysStatusManager;
import com.ubtechinc.alpha.appmanager.UbtBatteryManager;
import com.ubtechinc.alpha.appmanager.VolumeManager;
import com.ubtechinc.alpha.service.SkillHelper;
import com.ubtechinc.nets.im.business.LoginBussiness;
import com.ubtrobot.action.ActionApi;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.masterevent.protos.RobotGestures;
import com.ubtrobot.mini.libs.behaviors.Behavior;
import com.ubtrobot.mini.libs.behaviors.BehaviorInflater;
import com.ubtrobot.mini.properties.sdk.PropertiesApi;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.motion.protos.Motion;
import com.ubtrobot.motor.MotorApi;
import com.ubtrobot.motor.MotorServiceException;
import com.ubtrobot.speech.SpeechApi;
import com.ubtrobot.speech.listener.TTsListener;
import com.ubtrobot.sys.SysApi;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;

import java.io.FileNotFoundException;

import ubtechinc.com.standupsdk.StandUpApi;

/**
 * @desc : alpha util
 * @author: Logic
 * @email : logic.peng@ubtech.com
 * @time : 2017/6/6
 * @modifier:
 * @modify_time:
 */

public final class AlphaUtils {
    private static final String TAG = "AlphaUtils";
    private static final String KEY_SCREEN_TIMEOUT = "screen_timeout";
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 30000; // 默认值
    public static final String STAND_SPLITS_RIGHT_NAME = "028";//站着时右脚在前劈叉
    public static final String STAND_SQUATDOWN_NAME = "031"; //站着时蹲着动作文件

    public static void interruptAlphaNoIntent(Context mContext) {
        SpeechApi.get().stopTTs();
        ActionApi.get().stopAction(null);
    }

    public static void sendInterruptIntent(Context mContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //mContext.sendBroadcastAsUser(new Intent(SdkConstants.ACTION_ALPHA_INTERRUPT_BUSINESS), UserHandle.getUserHandleForUid(-1));
        } else {
            //mContext.sendBroadcast(new Intent(SdkConstants.ACTION_ALPHA_INTERRUPT_BUSINESS));
        }
    }

    public static void interruptAlpha(Context mContext) {
        SpeechApi.get().stopTTs();
        ActionApi.get().stopAction(null);
        sendInterruptIntent(mContext);
    }

    public static void stopService() {
        LoginBussiness.getInstance(AlphaApplication.getContext()).logout();
        SpeechApi.get().stopRecording();
        FaceDetectManager.getInstance().stopFaceDetect();
        VolumeManager.getInstance().unsubscribeVolumeEvent();
        SysStatusManager.getInstance().unRegisterSkill();
        SysStatusManager.getInstance().unsbscribeEvents();
        SmallActionManager.get().stopSmallAction();
        SmallActionManager.get().stopExcuteSitdownExpress();
        InfraRedManager.get().stopObjectDetection();
        UbtBatteryManager.getInstance().unregisterBatteryChangeReceive();
    }

    /**
     * 关机
     *
     * @param isLowPower true 电量低的时候关机  false  正常光机
     */
    public static boolean isShutDowning = false;
    private static Handler mHandler = new Handler(Looper.getMainLooper());

    public static void shutDown(final boolean isLongpressPower) {
        if (!isShutDowning) {
            if(!SystemPropertiesUtils.getFirststart()){
                SystemPropertiesUtils.setFirststartStep(FirstStartManager.ZERO_STEP);
            }
            isShutDowning = true;
            stopService();
            RobotGestures.GestureType gestureType = StandUpApi.getInstance().getRobotGesture();
            LogUtils.i(TAG, "机器人关机时姿态====" + gestureType);
            if (gestureType == RobotGestures.GestureType.BEND) {
                try {
                    MotorApi.get().reset(Priority.MAXHIGH, new ResponseListener<Boolean>() {
                        @Override
                        public void onResponseSuccess(Boolean aBoolean) {
                            Log.i(TAG, "onResponseSuccess========" + aBoolean);
                            RobotGestures.GestureType gestureType = StandUpApi.getInstance().getRobotGesture();
                            shutDownBehavior(isLongpressPower, gestureType);
                        }

                        @Override
                        public void onFailure(int i, @NonNull String s) {
                            Log.i(TAG, "关机时复位机器人失败========" + s);
                            RobotGestures.GestureType gestureType = StandUpApi.getInstance().getRobotGesture();
                            shutDownBehavior(isLongpressPower, gestureType);
                        }
                    });
                } catch (MotorServiceException e) {
                    e.printStackTrace();
                }
            } else {
                shutDownBehavior(isLongpressPower, gestureType);
            }
        }
    }

    private static void shutDownBehavior(boolean isLongpressPower, RobotGestures.GestureType gestureType) {
        if (isLongpressPower) {
            AlphaUtils.playBehavior("shuttingdown_0001", Priority.MAXHIGH, behaviorListener);
            excuteSquatdownAction(gestureType);
        } else {
            AlphaUtils.playBehavior("shuttingdown_0002", Priority.MAXHIGH, behaviorListener);
            excuteSquatdownAction(gestureType);
        }
    }

    private static void excuteSquatdownAction(RobotGestures.GestureType gestureType) {
        LogUtils.i(TAG, "机器人执行关机动作时的姿态===" + gestureType);
        if (gestureType == RobotGestures.GestureType.STAND) {
            excuteShutDownWithUsb();
        } else {
            if (gestureType == RobotGestures.GestureType.DEFAULT) {
                MotorManager.getInstance().resetLetMotors(new ResponseListener() {
                    @Override
                    public void onResponseSuccess(Object o) {
                        RobotGestures.GestureType gestureType1 = StandUpApi.getInstance().getRobotGesture();
                        LogUtils.i(TAG, "关机时复位后的姿态===" + gestureType1);
                        if (gestureType1 == RobotGestures.GestureType.STAND) {
                            excuteShutDownWithUsb();
                        } else {
                            unclockAllMotor();
                        }
                    }

                    @Override
                    public void onFailure(int i, @NonNull String s) {
                    }
                });
            } else {
                unclockAllMotor();
            }
        }
    }

    private static void excuteShutDownWithUsb() {
        ActionApi.get().playAction(STAND_SPLITS_RIGHT_NAME, Priority.MAXHIGH, new ResponseListener<Void>() {
            @Override
            public void onResponseSuccess(Void aVoid) {
                unclockAllMotor();
            }

            @Override
            public void onFailure(int i, @NonNull String s) {
                com.ubtech.utilcode.utils.LogUtils.i(TAG, "s======" + s);
            }
        });

    }

    private static void unclockAllMotor() {
        MotorApi.get().unlockAllMotor(new ResponseListener<Boolean>() {
            @Override
            public void onResponseSuccess(Boolean aBoolean) {

            }

            @Override
            public void onFailure(int i, @NonNull String s) {

            }
        });
    }


    /**
     * 调用系统关机
     */
    private static void sysShutDown() {
        Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
        intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        AlphaApplication.getContext().startActivity(intent);
        isShutDowning = false;
    }

    private static Behavior.BehaviorListener behaviorListener = new Behavior.BehaviorListener() {
        @Override
        public void onCompleted() {
            SysApi.get().shutdown();
            sysShutDown();
            SkillHelper.stopShutDownSkill();
        }
    };

    public static void ttsMessage(String ttsName) {
        VoicePool.get().playLocalTTs(ttsName, Priority.HIGH, new VoiceListener() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    public static void playBehavior(String name, Priority priority, Behavior.BehaviorListener behaviorListener) {
        if (behaviorListener == null) {
            behaviorListener = new Behavior.BehaviorListener() {
                @Override
                public void onCompleted() {
                }
            };
        }
        try {
            Behavior behavior = BehaviorInflater.loadBehaviorFromXml(
                    PropertiesApi.getRootPath() + "/behaviors/" + name + ".xml");
            behavior.setBehaviorListener(behaviorListener);
            behavior.setPriority(priority);
            behavior.start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void setSystemSleepTime(int time) {
        //设置系统休眠时间
        try {
            Settings.System.putInt(AlphaApplication.getContext().getContentResolver(), android.provider.Settings.System.SCREEN_OFF_TIMEOUT, time);
        } catch (NumberFormatException e) {
            Log.e("AlphaUtil", "could not persist screen timeout setting", e);
        }
    }

    public static long getSystemSleepTime() {
        final long currentTimeout = Settings.System.getLong(AlphaApplication.getContext().getContentResolver(),
                android.provider.Settings.System.SCREEN_OFF_TIMEOUT, FALLBACK_SCREEN_TIMEOUT_VALUE);
        return currentTimeout;
    }
}
