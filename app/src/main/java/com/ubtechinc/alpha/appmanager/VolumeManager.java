package com.ubtechinc.alpha.appmanager;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;

import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.utils.AlphaUtils;
import com.ubtechinc.alpha.utils.SoundVolumesUtils;
import com.ubtrobot.action.ActionApi;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.masterevent.protos.SysMasterEvent;
import com.ubtrobot.mini.libs.behaviors.Behavior;
import com.ubtrobot.mini.libs.behaviors.BehaviorInflater;
import com.ubtrobot.mini.properties.sdk.PropertiesApi;

import java.io.FileNotFoundException;
import java.util.Timer;
import java.util.TimerTask;

import event.master.ubtrobot.com.sysmasterevent.SysEventApi;
import event.master.ubtrobot.com.sysmasterevent.action.ReceiverAction;
import event.master.ubtrobot.com.sysmasterevent.data.EventData;
import event.master.ubtrobot.com.sysmasterevent.data.KeyCode;
import event.master.ubtrobot.com.sysmasterevent.event.HeadEvent;
import event.master.ubtrobot.com.sysmasterevent.event.VolumeEvent;
import event.master.ubtrobot.com.sysmasterevent.event.base.KeyEvent;
import event.master.ubtrobot.com.sysmasterevent.handler.EventHandler;
import event.master.ubtrobot.com.sysmasterevent.receiver.KeyEventReceiver;
import event.master.ubtrobot.com.sysmasterevent.receiver.SingleClickReceiver;

/**
 * @author：wululin
 * @date：2017/11/21 20:01
 * @modifier：ubt
 * @modify_date：2017/11/21 20:01
 * [A brief description]
 * [机器人音量管理类]
 */

public class VolumeManager {
    private static final String TAG = VolumeManager.class.getSimpleName();
    //    // 音量减
//    public static final String ACTION_KEYCODE_VOLUME_DOWN = "ubtechinc.intent.action.volumn.down";
//
//    // 音量加
//    public static final String ACTION_KEYCODE_VOLUME_UP= "ubtechinc.intent.action.volumn.up";
    private static final String MAX_VOLUME_NAME = "volume_0010";
    private static VolumeManager instance;
    private Context mContext;
    private AudioManager mAudioManager;
    private int mMaxVolume;
    private SoundVolumesUtils mSoundVolumesUtils;
    private Timer mAddVolumnTimer;
    private TimerTask mAddVolumnTask;
    private Timer mMulVolumnTimer;
    private TimerTask mMulVolumnTask;
    private EventHandler upKeyDownHandler;
    private EventHandler upKeyUpHandler;
    private EventHandler downKeyDownHandler;
    private EventHandler downKeyUpHandler;

    private VolumeManager() {
        this.mContext = AlphaApplication.getContext();
        mAudioManager = (AudioManager) mContext.getSystemService(Service.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mSoundVolumesUtils = SoundVolumesUtils.get(mContext);
        subscribeVolumeEvent();
    }

    public static VolumeManager getInstance() {
        if (instance == null) {
            synchronized (VolumeManager.class) {
                if (instance == null) {
                    instance = new VolumeManager();
                }
            }
        }
        return instance;
    }


    public void subscribeVolumeEvent() {
        SysEventApi.get().subscribe(VolumeEvent.newInstance().setPriority(SysMasterEvent.Priority.NORMAL), keyEventReceiver);
    }
    private KeyEventReceiver keyEventReceiver = new SingleClickReceiver(){

        @Override
        public boolean onSingleClick(KeyEvent keyEvent) {
            KeyCode keycode = keyEvent.keyCode();
            if(keyEvent.keyCode() == KeyCode.KEYCODE_VOLUME_UP_KEY_DOWN) {
                if (mSoundVolumesUtils.getVolumeLevel() == mSoundVolumesUtils.getMaxVolume()) {
                    try {
                        if (behavior == null) {
                            behavior = BehaviorInflater.loadBehaviorFromXml(
                                    PropertiesApi.getRootPath() + "/behaviors/" + MAX_VOLUME_NAME + ".xml");
                            behavior.setBehaviorListener(new Behavior.BehaviorListener() {
                                @Override
                                public void onCompleted() {
                                    behavior = null;
                                }
                            });
                            behavior.setPriority(Priority.HIGH);
                            behavior.start();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    stopAddVolumnTimer();
                    startAddVolumnTimer();
                }
            }

            if (keycode == KeyCode.KEYCODE_VOLUME_DOWN_KEY_DOWN) {
                if (mSoundVolumesUtils.getVolumeLevel() != 0) {
                    stopMulVolumnTimer();
                    startMulVolumnTimer();
                } else {
                    ExpressApi.get().doExpress("volume_01", 1, false, Priority.HIGH);
                }
            }
            if (keycode == KeyCode.KEYCODE_VOLUME_UP_KEY_UP) {
                stopAddVolumnTimer();
            }
            if (keycode == KeyCode.KEYCODE_VOLUME_DOWN_KEY_UP) {
                stopMulVolumnTimer();
            }
            return false;
        }

    };

    public void unsubscribeVolumeEvent() {
        SysEventApi.get().unsubscribe(keyEventReceiver);
    }


    private Behavior behavior;
    private void startAddVolumnTimer() {
        mAddVolumnTimer = new Timer();
        mAddVolumnTask = new TimerTask() {
            @Override
            public void run() {
                if (mSoundVolumesUtils.getVolumeLevel() == mSoundVolumesUtils.getMaxVolume()) {
                    stopAddVolumnTimer();
                } else {
                    mSoundVolumesUtils.addVolume(2);
                }
            }
        };
        mAddVolumnTimer.schedule(mAddVolumnTask, 0, 500);
    }

    private void stopAddVolumnTimer() {
        if (mAddVolumnTimer != null) {
            mAddVolumnTimer.cancel();
            mAddVolumnTimer = null;
        }
        if (mAddVolumnTask != null) {
            mAddVolumnTask.cancel();
            mAddVolumnTask = null;
        }
    }

    private void startMulVolumnTimer() {
        mMulVolumnTimer = new Timer();
        mMulVolumnTask = new TimerTask() {
            @Override
            public void run() {
                if (mSoundVolumesUtils.getVolumeLevel() == 0) {
                    stopMulVolumnTimer();
                } else {
                    mSoundVolumesUtils.mulVolume(2);
                }
            }
        };
        mMulVolumnTimer.schedule(mMulVolumnTask, 0, 500);
    }

    private void stopMulVolumnTimer() {
        if (mMulVolumnTimer != null) {
            mMulVolumnTimer.cancel();
            mMulVolumnTimer = null;
        }

        if (mMulVolumnTask != null) {
            mMulVolumnTask.cancel();
            mMulVolumnTask = null;
        }
    }

}
