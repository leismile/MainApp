package com.ubtechinc.alpha.deskclock;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtech.utilcode.utils.thread.HandlerUtils;
import com.ubtechinc.alpha.utils.AlphaUtils;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.speech.SpeechApi;
import com.ubtrobot.speech.listener.TTsListener;

/**
 * @desc : 闹钟服务
 * @author: Logic
 * @email : logic.peng@ubtech.com
 * @time : 2017/8/5
 * @modifier:
 * @modify_time:
 */

public final class AlarmService extends IntentService {
    private volatile int count = 3;
    private int totaleTime = 3000 * 3;

    public AlarmService() {
        super("alarmservice");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        handleAlarmBusiness(intent.getStringExtra("alarm_label"));
        while (count >= 0 && totaleTime > 0) {
            try {
                totaleTime -= 3000;
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleAlarmBusiness(final String content) {
        if (TextUtils.isEmpty(content)) return;
        AlphaUtils.interruptAlpha(getApplicationContext());
        LogUtils.d("DeskclockManager", "alarmservice count =" + count);
        count--;
        if (count >= 0) {
            VoicePool.get().playTTs(content, Priority.NORMAL, new VoiceListener() {
                @Override
                public void onCompleted() {
                    HandlerUtils.runUITask(new Runnable() {
                        @Override
                        public void run() {
                            handleAlarmBusiness(content);
                        }
                    }, 3000);
                }

                @Override
                public void onError(int i, String s) {

                }
            });

        }
    }
}
