package com.ubtechinc.alpha.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ubtechinc.alpha.utils.EmotionUtils;

/**
 * 日期时间改变
 * Created by logic on 17-12-20.
 */

public final class DateTimeReceiver extends BroadcastReceiver {

    private static final String TIMEZONE_CHANGED = Intent.ACTION_TIMEZONE_CHANGED;
    /**
     * 已经广播过的日期改变，在这之前的日期改变就不会再广播了
     */
    private static final String DATE_CHANGED = Intent.ACTION_DATE_CHANGED;
    /**
     * 每次用户调整了系统时间触发广播
     */
    private static final String TIME_CHANGED = Intent.ACTION_TIME_CHANGED;
    /**
     * 每1分广播触发
     */
    private static final String TIME_TICK = Intent.ACTION_TIME_TICK;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (DATE_CHANGED.equals(action) ||
                TIME_CHANGED.equals(action) ||
                TIMEZONE_CHANGED.equals(action)) {
            //日期、时区、系统时间设置改变
            EmotionUtils.backupElapseRealTime(context);
            EmotionUtils.tryUpdateFitness(context);
            //再尝试更新契合度
        } else if (TIME_TICK.equals(action)) {
            EmotionUtils.backupElapseRealTime(context);
        }
    }
}
