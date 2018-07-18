package com.ubtechinc.alpha.appmanager;

import android.support.annotation.NonNull;
import android.util.Log;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtechinc.alpha.service.SkillHelper;
import com.ubtechinc.alpha.utils.AlphaUtils;
import com.ubtechinc.alpha.utils.Constants;
import com.ubtechinc.alpha.utils.EyesControlUtils;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.commons.ResponseListener;

import com.ubtrobot.masterevent.protos.RobotGestures;
import com.ubtrobot.masterevent.protos.SysMasterEvent;
import com.ubtrobot.mini.libs.behaviors.Behavior;
import com.ubtrobot.mini.libs.behaviors.BehaviorInflater;
import com.ubtrobot.mini.properties.sdk.PropertiesApi;
import com.ubtrobot.motor.MotorApi;
import com.ubtrobot.speech.SpeechApi;
import com.ubtrobot.speech.SpeechApiExtra;
import com.ubtrobot.sys.SysApi;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;


import java.io.FileNotFoundException;

import event.master.ubtrobot.com.sysmasterevent.event.SysActiveEvent;
import ubtechinc.com.standupsdk.StandUpApi;

/**
 * Created by lulin.wu on 2018/3/26.
 */

public class SysStatusHelpManager {
    private static final String TAG = "SysStatusHelpManager";

    private SysStatusHelpManager() {
    }

    private static class SysStatusHelpManagerHolder {
        public static SysStatusHelpManager instance = new SysStatusHelpManager();
    }

    public static SysStatusHelpManager get() {
        return SysStatusHelpManager.SysStatusHelpManagerHolder.instance;
    }

    /**
     * 进入站立待命状态时的表现
     */
    private boolean isExcuteSmallAction = false;
    private long intoStandbuStandbyTime;//进入站着待命的时间

    public long getIntoStandbuStandbyTime() {
        return intoStandbuStandbyTime;
    }

    public void intoStandupStandby() {
        Log.i(TAG, "intoStandupStandby==============");
        intoStandbuStandbyTime = System.currentTimeMillis();
        SmallActionManager.get().startInfraRed();
    }

    private long intoSitdownStandbyTime;

    public long getIntoSitdownStandbyTime() {
        return intoSitdownStandbyTime;
    }

    public void setIntoSitdownStandbyTime(long intoSitdownStandbyTime) {
        this.intoSitdownStandbyTime = intoSitdownStandbyTime;
    }

    /**
     * 活跃状态进入坐着待命状态
     */
    public void activeIntoSitdownStandby() {
        intoSitdownStandbyTime = System.currentTimeMillis();
        SitdownStandbyManager.get().intoSitdownStandbyManager();
    }

    /**
     * 站着待命进入坐着待命状态时的表现
     */
    public void standupIntoSitdownStandby() {
        intoSitdownStandbyTime = System.currentTimeMillis();
        RobotGestures.GestureType gestureType = StandUpApi.getInstance().getRobotGesture();
        Log.i(TAG, "进入坐下待命时机器人的姿态=============" + gestureType);
        if (gestureType == RobotGestures.GestureType.STAND) {
            AlphaUtils.playBehavior(Constants.STANDBYSQUAT_0002, Priority.MAXHIGH, new Behavior.BehaviorListener() {
                @Override
                public void onCompleted() {
                    if (MotorManager.getInstance().isUnlockAllMotorWithGesture()) {
                        MotorManager.getInstance().unlockMotorIsNotHeader();
                    }
                    SysActiveEvent activeStatusData = SysStatusManager.getInstance().getmCurrentStatusData();
                    if (activeStatusData.getNewStatus() == SysMasterEvent.ActivieStatusType.SITDOWN_STANDBY) {
                        SitdownStandbyManager.get().intoSitdownStandbyManager();
                    }
                }
            });
        }

    }

    /**
     * 活跃进入待机状态时的表现
     */
    public void activeIntoStandby() {
        AlphaUtils.setSystemSleepTime(30 * 1000);
        RobotGestures.GestureType gestureType = StandUpApi.getInstance().getRobotGesture();
        LogUtils.i(TAG, "活跃状态进入待机状态时机器人姿态======" + gestureType);
        if (gestureType == RobotGestures.GestureType.STAND) {
            excuteIntoStandbyBehavior(Constants.SLEEPMODE_0002_2);
        } else {
            if (gestureType == RobotGestures.GestureType.BEND || gestureType == RobotGestures.GestureType.DEFAULT) {
                MotorManager.getInstance().resetLetMotors(new ResponseListener() {
                    @Override
                    public void onResponseSuccess(Object o) {
                        RobotGestures.GestureType gestureType = StandUpApi.getInstance().getRobotGesture();
                        LogUtils.i(TAG, "活跃状态进入待机状态时复位后机器人姿态======" + gestureType);
                        if (gestureType == RobotGestures.GestureType.STAND) {
                            excuteIntoStandbyBehavior(Constants.SLEEPMODE_0002_2);
                        } else {
                            excuteIntoStandbyBehavior(Constants.SLEEPMODE_0002_3);
                        }
                    }

                    @Override
                    public void onFailure(int i, @NonNull String s) {
                    }
                });
            } else {
                excuteIntoStandbyBehavior(Constants.SLEEPMODE_0002_3);
            }

        }
    }

    /**
     * 坐下待命到待机状态
     */
    public void sitdownIntoStandby() {
        AlphaUtils.setSystemSleepTime(30 * 1000);
        SmallActionManager.get().stopSmallAction();
        SmallActionManager.get().stopExcuteSitdownExpress();
        RobotGestures.GestureType gestureType = StandUpApi.getInstance().getRobotGesture();
        LogUtils.i(TAG, "坐着待命状态进入待机状态时机器人姿态======" + gestureType);
        if (gestureType == RobotGestures.GestureType.STAND) {
            excuteIntoStandbyBehavior(Constants.SLEEPMODE_0001_2);
        } else {
            if (gestureType == RobotGestures.GestureType.BEND || gestureType == RobotGestures.GestureType.DEFAULT) {
                MotorManager.getInstance().resetLetMotors(new ResponseListener() {
                    @Override
                    public void onResponseSuccess(Object o) {
                        RobotGestures.GestureType gestureType = StandUpApi.getInstance().getRobotGesture();
                        LogUtils.i(TAG, "坐着待命状态进入待机状态时复位后机器人姿态======" + gestureType);
                        if (gestureType == RobotGestures.GestureType.STAND) {
                            excuteIntoStandbyBehavior(Constants.SLEEPMODE_0001_2);

                        } else {
                            excuteIntoStandbyBehavior(Constants.SLEEPMODE_0001_3);
                        }
                    }

                    @Override
                    public void onFailure(int i, @NonNull String s) {
                    }
                });
            } else {
                excuteIntoStandbyBehavior(Constants.SLEEPMODE_0001_3);
            }
        }
    }

    /**
     * 执行进入待机时的表现
     *
     * @param behaviorName
     */
    private Behavior mStandbyBehavior;

    private void excuteIntoStandbyBehavior(String behaviorName) {
        try {
            mStandbyBehavior = BehaviorInflater.loadBehaviorFromXml(
                    PropertiesApi.getRootPath() + "/behaviors/" + behaviorName + ".xml");
            mStandbyBehavior.setBehaviorListener(new Behavior.BehaviorListener() {
                @Override
                public void onCompleted() {
                    mStandbyBehavior = null;
                    unlockAllMorotAndCloseEye();
                }
            });
            mStandbyBehavior.setPriority(Priority.MAXHIGH);
            mStandbyBehavior.start();
        } catch (FileNotFoundException e) {
            SkillHelper.stopStandbySkill();
            e.printStackTrace();
        }
    }

    /**
     * 停止进入待命时的表现
     */
    public void stopStandbyBehavior() {
        if (mStandbyBehavior != null) {
            mStandbyBehavior.stop();
            mStandbyBehavior = null;
        }
    }

    /**
     * 站着待命进入活跃状态
     */
    public void standupStandbyIntoActive() {
        MotorManager.getInstance().lockHeaderMotors();
        SmallActionManager.get().stopSmallAction();
    }

    /**
     * 坐着待命进入活跃状态
     */
    public void sitdownStandbyIntoActive() {
        MotorManager.getInstance().lockHeaderMotors();
        SitdownStandbyManager.get().stopFaceDeceteAndExpress();
    }

    /**
     * 待机状态进入活跃状态时的表现
     */
    public void standbyIntoActive() {
        if (!SysApi.get().isStarted()) {
            AlphaUtils.setSystemSleepTime(0);
            SpeechApiExtra.get().enableMicArray(new ResponseListener<Void>() {
                @Override
                public void onResponseSuccess(Void aVoid) {
                    LogUtils.i(TAG, "麦克风开启成功======");
                }

                @Override
                public void onFailure(int i, @NonNull String s) {
                    LogUtils.i(TAG, "麦克风开启失败======");
                }
            });
            UbtLocationManager.getInstance().openGPSSettings(true);
            MotorManager.getInstance().switchBoard(true, new ResponseListener() {
                @Override
                public void onResponseSuccess(Object o) {
                    LogUtils.i(TAG, "胸口板开启成功=====");
                    MotorManager.getInstance().lockHeaderMotors();
                    StandUpApi.getInstance().startGetRobotGestures(new ResponseCallback() {
                        @Override
                        public void onResponse(Request request, Response response) {
                        }

                        @Override
                        public void onFailure(Request request, CallException e) {
                        }
                    });
                }

                @Override
                public void onFailure(int i, @NonNull String s) {
                    LogUtils.i(TAG, "胸口板开启失败=====");
                }
            });
        }
    }


    private void unlockAllMorotAndCloseEye() {
        SysActiveEvent activeStatusData = SysStatusManager.getInstance().getmCurrentStatusData();
        Log.i(TAG, "newStatus=========" + activeStatusData.getNewStatus() + ";;oldStatus=====" + activeStatusData.getOldStatus());
        if (activeStatusData.getNewStatus() == SysMasterEvent.ActivieStatusType.STANDBY) {
            if (MotorManager.getInstance().isUnlockAllMotorWithGesture()) {
                MotorApi.get().unlockAllMotor(new ResponseListener<Boolean>() {
                    @Override
                    public void onResponseSuccess(Boolean aBoolean) {
                        SkillHelper.stopStandbySkill();
                    }

                    @Override
                    public void onFailure(int i, @NonNull String s) {
                        SkillHelper.stopStandbySkill();
                    }
                });
            } else {
                SkillHelper.stopStandbySkill();
            }
            MotorManager.getInstance().switchBoard(false, null);
            StandUpApi.getInstance().stopGetRobotGesutres(new ResponseCallback() {
                @Override
                public void onResponse(Request request, Response response) {
                }

                @Override
                public void onFailure(Request request, CallException e) {
                }
            });
            SpeechApiExtra.get().disableMicArray(new ResponseListener<Void>() {
                @Override
                public void onResponseSuccess(Void aVoid) {
                    LogUtils.i(TAG, "关闭麦克风成功=======");
                }

                @Override
                public void onFailure(int i, @NonNull String s) {
                    LogUtils.i(TAG, "关闭麦克风失败=======");
                }
            });
            UbtLocationManager.getInstance().openGPSSettings(false);
        } else {
            MotorManager.getInstance().resetLetMotors(new ResponseListener() {
                @Override
                public void onResponseSuccess(Object o) {
                }

                @Override
                public void onFailure(int i, @NonNull String s) {
                }
            });
            SkillHelper.stopStandbySkill();
        }
    }

}
