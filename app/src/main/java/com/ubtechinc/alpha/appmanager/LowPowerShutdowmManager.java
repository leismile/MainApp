package com.ubtechinc.alpha.appmanager;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ubt.alpha2.download.util.LogUtils;
import com.ubtechinc.alpha.service.SkillHelper;
import com.ubtechinc.alpha.utils.AlphaUtils;
import com.ubtechinc.alpha.utils.PlayBehaviorUtil;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by lulin.wu on 2018/5/31.
 * 低电关机管理类
 */

public class LowPowerShutdowmManager {
    private static final String TAG = LowPowerShutdowmManager.class.getSimpleName();
    private static final long ONE_MIN = 1000*60;
    private Timer mShutDownTimer;
    private TimerTask mShutDownTask;
    private LowPowerShutdowmManager(){}
    private static class LowPowerShutdowmManagerHolder {
        public static LowPowerShutdowmManager instance = new LowPowerShutdowmManager();
    }
    public static LowPowerShutdowmManager getInstance(){
        return LowPowerShutdowmManager.LowPowerShutdowmManagerHolder.instance;
    }
    private final byte[] lock = new byte[0];
    public void stopShutdownTimer(){
        synchronized (lock){
            if(mShutDownTimer != null){
                mShutDownTimer.cancel();
                mShutDownTimer = null;
            }
            if (mShutDownTask != null) {
                mShutDownTask.cancel();
                mShutDownTask = null;
            }
        }
    }

    public void startShutdownTimer(){
        synchronized (lock){
            if (mShutDownTimer == null) {
                mShutDownTimer = new Timer();
            }
            if (mShutDownTask == null) {
                mShutDownTask = new TimerTask() {
                    @Override
                    public void run() {
                        SkillHelper.startShutDownSkill(false);
                    }
                };
                mShutDownTimer.schedule(mShutDownTask,ONE_MIN);
                AlphaUtils.playBehavior("low-power_0002",Priority.MAXHIGH,null);
            }
        }
    }
}
