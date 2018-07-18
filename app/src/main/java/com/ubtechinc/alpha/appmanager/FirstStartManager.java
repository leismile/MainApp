package com.ubtechinc.alpha.appmanager;

import android.media.FaceDetector;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtech.utilcode.utils.notification.NotificationCenter;
import com.ubtech.utilcode.utils.notification.Subscriber;
import com.ubtechinc.alpha.appmanager.model.BandingSuccessEvent;
import com.ubtechinc.alpha.appmanager.model.FaceRegisterFinshEvent;
import com.ubtechinc.alpha.event.IMLoginResultEvent;
import com.ubtechinc.alpha.service.SkillHelper;
import com.ubtechinc.alpha.service.sysevent.SysEventService;
import com.ubtechinc.alpha.utils.AlphaUtils;
import com.ubtechinc.alpha.utils.SystemPropertiesUtils;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.context.ContextRunnable;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.master.event.EventReceiver;
import com.ubtrobot.masterevent.protos.RobotGestures;
import com.ubtrobot.masterevent.protos.SysMasterEvent;
import com.ubtrobot.mini.libs.behaviors.Behavior;
import com.ubtrobot.mini.libs.behaviors.BehaviorInflater;
import com.ubtrobot.mini.libs.scenes.Scene;
import com.ubtrobot.mini.libs.scenes.SceneListener;
import com.ubtrobot.mini.libs.scenes.SceneUtils;
import com.ubtrobot.mini.properties.sdk.PropertiesApi;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.speech.SpeechApi;
import com.ubtrobot.transport.message.Event;

import java.io.FileNotFoundException;
import java.util.Timer;
import java.util.TimerTask;

import ubtechinc.com.standupsdk.StandUpApi;

/**
 * Created by lulin.wu on 2018/7/10.
 * 首次开机管理类
 */

public class FirstStartManager {
    public static final String ZERO_STEP = "0";
    public static final String ONE_STEP = "1";
    public static final String TWO_STEP = "2";
    public static final String THREE_STEP = "3";
    public static final String FOUR_STEP = "4";

    private static final String TAG = "FirstStartManager";
    private static int ONE_MIN = 60 * 1000;

    private FirstStartManager() {
    }

    private static class FirstStartManagerHolder {
        public static FirstStartManager instance = new FirstStartManager();
    }

    public static FirstStartManager get() {
        return FirstStartManager.FirstStartManagerHolder.instance;
    }

    /**
     * 首次开机第一步 自我介绍
     */
    public void playIntroduceTTs() {
        SystemPropertiesUtils.setFirststartStep(ONE_STEP);
        AlphaUtils.playBehavior("fristboot_0001", Priority.HIGH, new Behavior.BehaviorListener() {
            @Override
            public void onCompleted() {
                LogUtils.i(TAG,"playIntroduceTTs====onCompleted===");
                checkPower();
            }
        });
        MotorManager.getInstance().resetLetMotors(new ResponseListener() {
            @Override
            public void onResponseSuccess(Object o) {
            }

            @Override
            public void onFailure(int i, @NonNull String s) {
            }
        });
    }

    /**
     * 蓝牙配网成功回调
     */
    Subscriber<BandingSuccessEvent> bandingSuccessEventSubscriber = new Subscriber<BandingSuccessEvent>() {

        @Override
        public void onEvent(BandingSuccessEvent bandingSuccessEvent) {
            NotificationCenter.defaultCenter().unsubscribe(IMLoginResultEvent.class, bandingSuccessEventSubscriber);
            String firstStartStep = SystemPropertiesUtils.getFirststartStep();
            Log.i(TAG,"firstStartStep========" + firstStartStep);
            if (firstStartStep.equals("2")) {
                intoFaceRegisterStep();
            }
        }
    };

    /**
     * 人脸录入结束回调
     */
    Subscriber<FaceRegisterFinshEvent> faceRegisterFinshEventSubscriber = new Subscriber<FaceRegisterFinshEvent>() {

        @Override
        public void onEvent(FaceRegisterFinshEvent bandingSuccessEvent) {
            intoRobotDance();
        }
    };

    /**
     * 首次开机第二步 检测电量
     */
    private void checkPower() {
        SystemPropertiesUtils.setFirststartStep(TWO_STEP);
        SysMasterEvent.BatteryStatusData batteryStatusData = UbtBatteryManager.getInstance().getBatteryInfo();
        int batteryLevel = batteryStatusData.getLevel();
        int levelStatus = batteryStatusData.getLevelStatus();
        int status = batteryStatusData.getStatus();
        SpeechApi.get().startRecognize();
        if (batteryLevel <= 10 && status != android.os.BatteryManager.BATTERY_PLUGGED_AC) {
            LowPowerShutdowmManager.getInstance().startShutdownTimer();
        } else if (levelStatus == 0) {
            Master.get().execute(SysEventService.class, new ContextRunnable<SysEventService>() {
                @Override
                public void run(SysEventService sysEventService) {
                    sysEventService.lowPowerStatus(UbtBatteryManager.getInstance().getBatteryStatsParam());
                }
            });
        }
        NotificationCenter.defaultCenter().subscriber(BandingSuccessEvent.class, bandingSuccessEventSubscriber);
        stopIntoStatusTimer();
        startIntoStatusTimer();
    }

    /**
     * 播报联网提示tts
     */
    public void playBleNetworkTTs() {
        SysStatusManager.getInstance().publishSysActiveStatus(SysMasterEvent.ActivieStatusType.ACTIVE);
        if (UbtBatteryManager.getInstance().isLowPower()) {
            VoicePool.get().playLocalTTs("fristboot_002", Priority.HIGH, new VoiceListener() {
                @Override
                public void onCompleted() {
                    stopIntoStatusTimer();
                    startIntoStatusTimer();
                }

                @Override
                public void onError(int i, String s) {
                }
            });
        } else {
            RobotGestures.GestureType gestureType = StandUpApi.getInstance().getRobotGesture();
            if (gestureType == RobotGestures.GestureType.STAND) {
                playSence("fristboot_networking1");
            } else {
                playSence("fristboot_networking2");
            }
        }
    }

    /**
     * 首次开机第四步 让机器人跳舞
     */
    public void intoRobotDance() {
        SystemPropertiesUtils.setFirststartStep(FOUR_STEP);
        AlphaUtils.playBehavior("fristboot_0015", Priority.HIGH, new Behavior.BehaviorListener() {
            @Override
            public void onCompleted() {
                playDance();
            }
        });
    }

    private Behavior mDanceBehavior;

    private void playDance() {
        try {
            mDanceBehavior = BehaviorInflater.loadBehaviorFromXml(
                    PropertiesApi.getRootPath() + "/behaviors/dance_0005.xml");
            mDanceBehavior.setBehaviorListener(new Behavior.BehaviorListener() {
                @Override
                public void onCompleted() {
                    stopDance();
                }
            });
            mDanceBehavior.setPriority(Priority.HIGH);
            mDanceBehavior.start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void stopDance() {
        SystemPropertiesUtils.setFirststartStep(ZERO_STEP);
        AlphaUtils.playBehavior("fristboot_0016",Priority.HIGH,null);
        if (mDanceBehavior != null) {
            mDanceBehavior.stop();
            mDanceBehavior = null;
        }
    }

    /**
     * 首次开机第三步  人脸录入
     */
    private void intoFaceRegisterStep() {
        stopIntoStatusTimer();
        SystemPropertiesUtils.setFirststartStep(THREE_STEP);
        SystemPropertiesUtils.setFirststart(false);
        AlphaUtils.playBehavior("fristboot_0009", Priority.HIGH, new Behavior.BehaviorListener() {
            @Override
            public void onCompleted() {
                NotificationCenter.defaultCenter().subscriber(FaceRegisterFinshEvent.class, faceRegisterFinshEventSubscriber);
            }
        });
    }

    private Scene mScene;

    private void playSence(String sceneName) {
        if (mScene == null) {
            try {
                mScene = SceneUtils.loadScene(sceneName);
                mScene.display(Priority.HIGH, new SceneListener() {
                    @Override
                    public void onCompleted() {
                        mScene = null;
                        stopIntoStatusTimer();
                        startIntoStatusTimer();
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private Timer mTimer;
    private TimerTask mTask;

    private void startIntoStatusTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mTask == null) {
            mTask = new TimerTask() {
                @Override
                public void run() {
                    SysStatusManager.getInstance().startIntoStandupStandbyTimer();
                }
            };
            mTimer.schedule(mTask, ONE_MIN);
        }
    }

    private void stopIntoStatusTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
    }
}
