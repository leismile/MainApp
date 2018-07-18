package com.ubtechinc.alpha.appmanager;

import android.support.annotation.NonNull;
import android.util.Log;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtech.utilcode.utils.notification.NotificationCenter;
import com.ubtech.utilcode.utils.thread.ThreadPool;
import com.ubtechinc.alpha.appmanager.model.FaceRegisterFinshEvent;
import com.ubtechinc.alpha.service.SkillHelper;
import com.ubtechinc.alpha.utils.AlphaUtils;
import com.ubtechinc.alpha.utils.MouthUtils;
import com.ubtechinc.alpha.utils.PowerUtils;
import com.ubtechinc.alpha.utils.SkillUtils;
import com.ubtechinc.alpha.utils.SystemPropertiesUtils;
import com.ubtrobot.action.ActionApi;
import com.ubtrobot.action.receiver.ActionStoppedReceiver;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.interactor.MasterInteractor;
import com.ubtrobot.master.skill.SkillInfo;
import com.ubtrobot.master.skill.SkillStopCause;
import com.ubtrobot.masterevent.protos.RobotGestures;
import com.ubtrobot.masterevent.protos.SysMasterEvent;
import com.ubtrobot.mini.libs.scenes.Scene;
import com.ubtrobot.mini.libs.scenes.SceneListener;
import com.ubtrobot.mini.libs.scenes.SceneUtils;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.motion.protos.Sys;
import com.ubtrobot.motor.MotorApi;
import com.ubtrobot.speech.SpeechApi;
import com.ubtrobot.speech.protos.Speech;
import com.ubtrobot.speech.receivers.TTsStateReceiver;
import com.ubtrobot.speech.receivers.WakeupReceiver;
import com.ubtrobot.sys.SysApi;
import com.ubtrobot.transport.message.CallException;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import event.master.ubtrobot.com.sysmasterevent.SysEventApi;
import event.master.ubtrobot.com.sysmasterevent.event.HeadEvent;
import event.master.ubtrobot.com.sysmasterevent.event.PowerButtonEvent;
import event.master.ubtrobot.com.sysmasterevent.event.SysActiveEvent;
import event.master.ubtrobot.com.sysmasterevent.event.VolumeEvent;
import event.master.ubtrobot.com.sysmasterevent.event.base.KeyEvent;
import event.master.ubtrobot.com.sysmasterevent.listener.publish.PublishSysActiveStatusListener;
import event.master.ubtrobot.com.sysmasterevent.receiver.KeyEventReceiver;
import event.master.ubtrobot.com.sysmasterevent.receiver.SingleClickReceiver;
import event.master.ubtrobot.com.sysmasterevent.receiver.SysActiveStatusEventReceiver;
import ubtechinc.com.standupsdk.StandUpApi;

/**
 * 系统活跃 待唤醒状态  待机状态管理
 * wll
 */
public class SysStatusManager {
    private static final String TAG = SysStatusManager.class.getSimpleName();
    private static final int FIFTEEN_SECONDS = 20 * 1000;
    private static final int TWO_SECONDS = 2 * 1000;
    private MasterInteractor mInteractor;
    private SysActiveEvent mCurrentStatusData;

    private SysStatusManager() {
    }

    private static class SysStatusManagerHolder {
        public static SysStatusManager instance = new SysStatusManager();
    }

    public static SysStatusManager getInstance() {
        return SysStatusManagerHolder.instance;
    }

    public void init() {
        SpeechApi.get().subscribeEvent(mWakeupReceiver);
        SpeechApi.get().subscribeEvent(mTTsStateReceiver);
        SysEventApi.get().subscribe(SysActiveEvent.newInstance(), mActiveStatusHandler);
        SysEventApi.get().subscribe(HeadEvent.newInstance().setPriority(SysMasterEvent.Priority.LOW), mRacketHeadHandler);
        SysEventApi.get().subscribe(PowerButtonEvent.newInstance().setPriority(SysMasterEvent.Priority.LOW), powerKeyReceiver);
        ActionApi.get().subscribeEvent(actionStoppedReceiver);
        String packageName = com.ubtech.utilcode.utils.Utils.getContext().getPackageName();
        mInteractor = Master.get().getOrCreateInteractor("robot:" + packageName);
        registerSkill();
    }

    // fix subscribe !!
    private void subscribeVolumeEvent() {
        SingleClickReceiver singclickReceive = new SingleClickReceiver() {
            @Override
            public boolean onSingleClick(KeyEvent keyEvent) {
                handleVolumeEvent(this);
                return true;
            }
        };
        SysEventApi.get().subscribe(VolumeEvent.newInstance().setPriority(SysMasterEvent.Priority.NORMAL), singclickReceive);
    }

    public void unsbscribeEvents() {
        if (mWakeupReceiver != null) {
            SpeechApi.get().unsubscribeEvent(mWakeupReceiver);
        }
        if (mActiveStatusHandler != null) {
            SysEventApi.get().unsubscribe(mActiveStatusHandler);
        }
        if (mRacketHeadHandler != null) {
            SysEventApi.get().unsubscribe(mRacketHeadHandler);
        }
        if (mTTsStateReceiver != null) {
            SpeechApi.get().unsubscribeEvent(mTTsStateReceiver);
        }
        if (actionStoppedReceiver != null) {
            ActionApi.get().unsubscribeEvent(actionStoppedReceiver);
        }
        if (powerKeyReceiver != null) {
            SysEventApi.get().unsubscribe(powerKeyReceiver);
        }
    }


    private void handlePowerKeyDown() {
        PowerUtils.get().show();
        repackSendActiveStatus();
    }

    //fix unsubscribe !!
    public void handleVolumeEvent(SingleClickReceiver receiver) {
        if (mCurrentStatusData != null) {
            if (mCurrentStatusData.getNewStatus() == SysMasterEvent.ActivieStatusType.STANDBY) {
                PowerUtils.get().backToNormal();
                stopActiveTimer();
                startIntoStandupStandbyTimer();
                publishSysActiveStatus(SysMasterEvent.ActivieStatusType.ACTIVE);
            }
        }
        // receiver
        SysEventApi.get().unsubscribe(receiver);
    }

    /**
     * 获取当前状态
     *
     * @return
     */
    public SysActiveEvent getmCurrentStatusData() {
        return mCurrentStatusData;
    }

    /**
     * 监听skill 开启和退出
     */
    public void registerSkill() {
        if (mInteractor != null) {
            mInteractor.registerSkillLifecycleCallbacks(mLifecycleCallbacks);
        }
    }

    /**
     * 取消监听skill的开启和退出
     */
    public void unRegisterSkill() {
        if (mInteractor != null) {
            mInteractor.unregisterSkillLifecycleCallbacks(mLifecycleCallbacks);
        }
    }


    /**
     * 判断是否还有skill在运行
     */
    private boolean isNotSkillExcute() {
        List<SkillInfo> skillInfos = mInteractor.getStartedSkills();
        LogUtils.i(TAG, "skillList====" + skillInfos.toString());
        return skillInfos.size() == 0;
    }

    MasterInteractor.SkillLifecycleCallbacks mLifecycleCallbacks = new MasterInteractor.SkillLifecycleCallbacks() {
        @Override
        public void onSkillStarted(final SkillInfo skillInfo) {
            LogUtils.i(TAG, "onSkillStarted======" + skillInfo.toString());
            String skillName = skillInfo.getName();
            boolean isInNotActiveSkills = SkillUtils.isInNotActiveSkills(skillName);
//            LogUtils.i(TAG, "开启的skill是否触发活跃状态======" + isInNotActiveSkills);
            if (!isInNotActiveSkills) {
                if (mCurrentStatusData != null) {
                    if (mCurrentStatusData.getNewStatus() == SysMasterEvent.ActivieStatusType.STANDBY) {
                        PowerUtils.get().backToNormal();
                    }
                }
                stopIntoStandupStandbyTimer();
                stopActiveTimer();
                publishSysActiveStatus(SysMasterEvent.ActivieStatusType.ACTIVE);
            }
        }

        @Override
        public void onSkillStopped(final SkillInfo skillInfo, SkillStopCause skillStopCause) {
            LogUtils.i(TAG, "onSkillStopped======" + skillInfo.toString());
            String skillName = skillInfo.getName();
            String firstStartStep = SystemPropertiesUtils.getFirststartStep();
            if(firstStartStep.equals(FirstStartManager.ONE_STEP)){

            }else if(firstStartStep.equals(FirstStartManager.TWO_STEP)){

            }else if(firstStartStep.equals(FirstStartManager.THREE_STEP)){
                if(skillName.equals("face_register_skill")){
                    FaceRegisterFinshEvent event = new FaceRegisterFinshEvent();
                    NotificationCenter.defaultCenter().publish(event);
                }
            }else if(firstStartStep.equals(FirstStartManager.FOUR_STEP)){

            }else {
                boolean isInNotActiveSkills = SkillUtils.isInNotActiveSkills(skillName);
//            LogUtils.i(TAG, "开启的skill是否触发活跃状态======" + isInNotActiveSkills);
                if (!isInNotActiveSkills) {
                    boolean isNotSkillExcute = isNotSkillExcute();
//                LogUtils.i(TAG, "是否还有skill在运行======" + isNotSkillExcute);
                    if (isNotSkillExcute) {
                        stopActiveTimer();
                        startIntoStandupStandbyTimer();
                    }
                }
            }
        }
    };

    ActionStoppedReceiver actionStoppedReceiver = new ActionStoppedReceiver() {
        @Override
        protected void onActionStopped(Cause cause) {
//            LogUtils.i(TAG, "动作停止的原因====" + cause.toString());
            if (cause == Cause.FINISHED) {
                MotorApi.get().unlockMotor(MotorManager.armMotorIds, Priority.LOW, new com.ubtrobot.commons.ResponseListener<Boolean>() {
                    @Override
                    public void onResponseSuccess(Boolean aBoolean) {
                    }

                    @Override
                    public void onFailure(int i, @NonNull String s) {
                    }
                });
            }
        }
    };

    TTsStateReceiver mTTsStateReceiver = new TTsStateReceiver() {
        @Override
        public void onStateChange(Speech.TTsState state) {
//            LogUtils.i(TAG, "ttsstate======" + state);
            if (state == Speech.TTsState.BEGIN) {
                MouthUtils.ttsMouthLed();
                if (!SysApi.get().isStarted()) {
                    if (mCurrentStatusData != null) {
                        if (mCurrentStatusData.getNewStatus() == SysMasterEvent.ActivieStatusType.STANDBY) {
                            PowerUtils.get().backToNormal();
                            stopActiveTimer();
                            startIntoStandupStandbyTimer();
                            publishSysActiveStatus(SysMasterEvent.ActivieStatusType.ACTIVE);
                        }
                    }
                }
            } else if (state == Speech.TTsState.END) {
                MouthUtils.normalMouthLed();
            }
        }
    };

    WakeupReceiver mWakeupReceiver = new WakeupReceiver() {
        @Override
        public void onWakeup(Speech.WakeupParam data) {
            String firststartStep = SystemPropertiesUtils.getFirststartStep();
            LogUtils.i(TAG,"firststartStep==========" + firststartStep);
             if(firststartStep.equals(FirstStartManager.ONE_STEP)){
            }else if(firststartStep.equals(FirstStartManager.TWO_STEP)){
                FirstStartManager.get().playBleNetworkTTs();
            }else if(firststartStep.equals(FirstStartManager.THREE_STEP)){
            }else if(firststartStep.equals(FirstStartManager.FOUR_STEP)){
                FirstStartManager.get().stopDance();
            }else {
                VoicePool.get().stopTTs(Priority.MAXHIGH, new com.ubtrobot.commons.ResponseListener<Void>() {
                    @Override
                    public void onResponseSuccess(Void aVoid) {
                    }

                    @Override
                    public void onFailure(int i, @NonNull String s) {
                    }
                });
                stopIntoStandupStandbyTimer();
                startActiveTimer();
                publishSysActiveStatus(SysMasterEvent.ActivieStatusType.ACTIVE);
            }

        }
    };

    /**
     * 开始进入站立待命状态定时器
     */
    private Timer mStandupStandbyTimer;
    private TimerTask mStandupStandbyTask;

    private final byte[] lockStartInto = new byte[0];

    public void startIntoStandupStandbyTimer() {
        synchronized (lockStartInto) {
            Log.d(TAG, "startIntoStandupStandbyTimer=====");
            if(SystemPropertiesUtils.getFirststartStep().equals(FirstStartManager.FOUR_STEP)){
                return;
            }
            if (isNotSkillExcute()) {
                if (mStandupStandbyTimer != null) {
                    stopIntoStandupStandbyTimer();
                }
                mStandupStandbyTimer = new Timer();
                mStandupStandbyTask = new TimerTask() {
                    @Override
                    public void run() {
                        if (isNotSkillExcute()) {
                            intoStandupOrSitdownStandby();
                        }
                    }
                };
                mStandupStandbyTimer.schedule(mStandupStandbyTask, FIFTEEN_SECONDS);
            }
        }
    }

    private void intoStandupOrSitdownStandby() {
        RobotGestures.GestureType gestureType = StandUpApi.getInstance().getRobotGesture();
        if (gestureType == RobotGestures.GestureType.STAND) { //站着的话进入站立待命状态
            publishSysActiveStatus(SysMasterEvent.ActivieStatusType.STANDUP_STANDBY);
        } else { //进入坐下待命状态
            publishSysActiveStatus(SysMasterEvent.ActivieStatusType.SITDOWN_STANDBY);
        }
    }


    public void stopIntoStandupStandbyTimer() {
        synchronized (lockStartInto) {
            if (mStandupStandbyTimer != null) {
                mStandupStandbyTimer.cancel();
                mStandupStandbyTimer = null;
            }
            if (mStandupStandbyTask != null) {
                mStandupStandbyTask.cancel();
                mStandupStandbyTask = null;
            }
        }
    }

    private Timer mActiveTimer;
    private TimerTask mActiveTask;
    private final byte[] lockStartActive = new byte[0];

    private void startActiveTimer() {
        Log.i(TAG, "startActiveTimer=========");
        synchronized (lockStartActive) {
            if (isNotSkillExcute()) {
                if (mActiveTimer != null) {
                    stopActiveTimer();
                }
                mActiveTimer = new Timer();
                mActiveTask = new TimerTask() {
                    @Override
                    public void run() {
                        if (isNotSkillExcute()) {
                            ExpressApi.get().doExpress("normal_1", 1, Priority.HIGH, null);
                            intoStandupOrSitdownStandby();
                        }
                    }
                };
                mActiveTimer.schedule(mActiveTask, FIFTEEN_SECONDS);
            }

        }
    }

    public void stopActiveTimer() {
        synchronized (lockStartActive) {
            if (mActiveTimer != null) {
                mActiveTimer.cancel();
                mActiveTimer = null;
            }
            if (mActiveTask != null) {
                mActiveTask.cancel();
                mActiveTask = null;
            }
        }
    }

    SysActiveStatusEventReceiver mActiveStatusHandler = new SysActiveStatusEventReceiver() {
        @Override
        public boolean onReceive(final SysActiveEvent sysActiveEvent) {
            handleStatusChanged(sysActiveEvent);
            return false;
        }


    };

    private void handleStatusChanged(final SysActiveEvent sysActiveEvent) {
        mCurrentStatusData = sysActiveEvent;
        SysMasterEvent.ActivieStatusType oldStatus = mCurrentStatusData.getOldStatus();
        SysMasterEvent.ActivieStatusType newStatus = mCurrentStatusData.getNewStatus();
//        LogUtils.i(TAG, "oldStatus=====" + oldStatus + ";;;newStatus======" + newStatus);
        if (newStatus == SysMasterEvent.ActivieStatusType.ACTIVE && oldStatus == SysMasterEvent.ActivieStatusType.ACTIVE) {
            SysStatusHelpManager.get().standbyIntoActive();
        }
        if (newStatus == SysMasterEvent.ActivieStatusType.ACTIVE && oldStatus != SysMasterEvent.ActivieStatusType.ACTIVE) {
            SysStatusHelpManager.get().stopStandbyBehavior();
            PowerUtils.get().backToNormal();
        }
        if (oldStatus == SysMasterEvent.ActivieStatusType.ACTIVE && newStatus == SysMasterEvent.ActivieStatusType.STANDUP_STANDBY) {
            LogUtils.d(TAG, "由活跃状态进入站着待命状态");
            SysStatusHelpManager.get().intoStandupStandby();
        }

        if (oldStatus == SysMasterEvent.ActivieStatusType.ACTIVE && newStatus == SysMasterEvent.ActivieStatusType.SITDOWN_STANDBY) {
            LogUtils.i(TAG, "有活跃状态进入坐下待命状态");
            SysStatusHelpManager.get().activeIntoSitdownStandby();
        }

        if (oldStatus == SysMasterEvent.ActivieStatusType.STANDUP_STANDBY && newStatus == SysMasterEvent.ActivieStatusType.SITDOWN_STANDBY) {
            LogUtils.d(TAG, "由站着待命状态进入坐着待命状态");
            SysStatusHelpManager.get().standupIntoSitdownStandby();
        }

        if (oldStatus == SysMasterEvent.ActivieStatusType.SITDOWN_STANDBY && newStatus == SysMasterEvent.ActivieStatusType.STANDBY) {
            LogUtils.d(TAG, "由坐着待命状态进入待机状态");
            subscribeVolumeEvent();
            SysStatusHelpManager.get().sitdownIntoStandby();
        }

        if (oldStatus == SysMasterEvent.ActivieStatusType.STANDUP_STANDBY && newStatus == SysMasterEvent.ActivieStatusType.ACTIVE) {
            LogUtils.d(TAG, "由站着待命状态进入活跃状态");
            SysStatusHelpManager.get().standupStandbyIntoActive();
        }

        if (oldStatus == SysMasterEvent.ActivieStatusType.SITDOWN_STANDBY && newStatus == SysMasterEvent.ActivieStatusType.ACTIVE) {
            LogUtils.d(TAG, "由坐着待命状态进入活跃状态");
            if(SystemPropertiesUtils.getFirststart()){
                MotorManager.getInstance().resetLetMotors(new ResponseListener() {
                    @Override
                    public void onResponseSuccess(Object o) {
                    }
                    @Override
                    public void onFailure(int i, @NonNull String s) {
                    }
                });
            }
            SysStatusHelpManager.get().sitdownStandbyIntoActive();
        }

        if (oldStatus == SysMasterEvent.ActivieStatusType.STANDBY && newStatus == SysMasterEvent.ActivieStatusType.ACTIVE) {
            LogUtils.d(TAG, "由待机状态进入活跃状态");
            if(SystemPropertiesUtils.getFirststart()){
                MotorManager.getInstance().resetLetMotors(new ResponseListener() {
                    @Override
                    public void onResponseSuccess(Object o) {
                    }
                    @Override
                    public void onFailure(int i, @NonNull String s) {
                    }
                });
            }
            SysStatusHelpManager.get().standbyIntoActive();
        }

        if (oldStatus == SysMasterEvent.ActivieStatusType.ACTIVE && newStatus == SysMasterEvent.ActivieStatusType.STANDBY) {
            LogUtils.d(TAG, "由活跃状态进入待机状态");
            stopIntoStandupStandbyTimer();
            stopActiveTimer();
            subscribeVolumeEvent();
            SysStatusHelpManager.get().activeIntoStandby();
        }
    }


    private volatile Scene mScene;
    KeyEventReceiver mRacketHeadHandler = new KeyEventReceiver() {
        @Override
        public boolean onSingleClick(KeyEvent keyEvent) {
            String firststartStep = SystemPropertiesUtils.getFirststartStep();
             if(firststartStep.equals(FirstStartManager.ONE_STEP)){
            }else if(firststartStep.equals(FirstStartManager.TWO_STEP)){
                FirstStartManager.get().playBleNetworkTTs();
            }else if(firststartStep.equals(FirstStartManager.THREE_STEP)){
                FirstStartManager.get().intoRobotDance();
            }else if(firststartStep.equals(FirstStartManager.FOUR_STEP)){
                FirstStartManager.get().stopDance();
            } else {
                ThreadPool.getInstance().submit(new ThreadPool.Job<Object>() {
                    @Override
                    public Object run(ThreadPool.JobContext jobContext) {
                        handleHeadsingleTap();
                        return null;
                    }
                });
            }
            return false;
        }

        @Override
        public boolean onDoubleClick(KeyEvent keyEvent) {
            return false;
        }

        @Override
        public boolean onLongClick(KeyEvent keyEvent) {
            return false;
        }
    };

    KeyEventReceiver powerKeyReceiver = new KeyEventReceiver() {
        @Override
        public boolean onSingleClick(KeyEvent keyEvent) {
            ThreadPool.getInstance().submit(new ThreadPool.Job<Object>() {
                @Override
                public Object run(ThreadPool.JobContext jobContext) {
                    handlePowerKeyDown();
                    return null;
                }
            });
            return false;
        }

        @Override
        public boolean onDoubleClick(KeyEvent keyEvent) {
            return false;
        }

        @Override
        public boolean onLongClick(KeyEvent keyEvent) {
            LogUtils.i(TAG, "长按电源键关机==========");
            SkillHelper.startShutDownSkill(true); // 长按电源键
            return true;
        }
    };


    //处理拍头事件
    private void handleHeadsingleTap() {
        Log.i(TAG, "handleHeadsingleTap=======");
        repackSendActiveStatus();
        if (mCurrentStatusData != null) {
            if (mCurrentStatusData.getNewStatus() == SysMasterEvent.ActivieStatusType.STANDBY) {
                PowerUtils.get().backToNormal();
            } else {
                try {
                    if (!UbtBatteryManager.getInstance().isLowPower()) {
                        Log.i("BehaviorSet", "mScene=======");
                        if (isNotSkillExcute()) {
                            if (mScene == null) {
                                mScene = SceneUtils.loadScene("w_head_001");
                                mScene.display(Priority.NORMAL, new SceneListener() {
                                    @Override
                                    public void onCompleted() {
                                        //TODO 可能会跟进入活跃状态时机器人的表现有冲突。
                                        Log.d("BehaviorSet", "handleHeadsingleTap========onCompleted" + (mScene != null ? mScene.toString() : "null"));
                                        mScene = null;
                                    }
                                });
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void repackSendActiveStatus() {
        stopActiveTimer();
        startIntoStandupStandbyTimer();
        publishSysActiveStatus(SysMasterEvent.ActivieStatusType.ACTIVE);
    }

    public void publishSysActiveStatus(SysMasterEvent.ActivieStatusType status) {
        SysEventApi.get().publishSysActiveStatus(status, new PublishSysActiveStatusListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFaild(CallException e) {

            }
        });
    }

}
