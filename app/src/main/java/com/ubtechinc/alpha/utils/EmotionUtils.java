package com.ubtechinc.alpha.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtrobot.mini.libs.scenes.EmotionStore;

import java.util.Calendar;

/**
 * Created by logic on 17-12-20.
 */

public final class EmotionUtils {
    private static final String TAG = "Emotion";
    /**
     * 机器人一个月开机总时长：月度时间值,单位（分）
     */
    private static final String RUNNING_TIME_OF_MONTH = "running_time_of_one_month";
    /**
     * 辅助记录系统运行时长：系统可能没有低电保护，当系统非正常关机，无法收到关机广播，通过“TIME_TICK”广播每隔一分钟备份一次，单位（分）
     */
    private static final String ELAPSE_REAL_TIME = "elapse_real_time";

    /**
     * 根据月度时间值计算新契合度
     *
     * @param context Context
     */
    public synchronized static void tryUpdateFitness(Context context) {
        final ContentResolver resolver = context.getContentResolver();
        Calendar now = Calendar.getInstance();
        Calendar modifyTime = Calendar.getInstance();
        modifyTime.setTime(EmotionStore.queryFitnessModifyTime(resolver,
                EmotionStore.getMasterId()));
        //出现年份、月份与上次契合度修改时间不一致认为更新一次契合度
        if (now.get(Calendar.MONTH) != modifyTime.get(Calendar.MONTH) ||
                now.get(Calendar.YEAR) != modifyTime.get(Calendar.YEAR)) {
            //RUNNING_TIME单位是分钟
            //当满足年份和月份不一致时，累计月度时间和本次运行时间作为上月的总的开机时长，并以此值计算当月的契合值
            int time = SharedPreferenceUtil.readInt(context, RUNNING_TIME_OF_MONTH)
                    + SharedPreferenceUtil.readInt(context, ELAPSE_REAL_TIME);
            Log.v(TAG, "RUNNING_TIME_OF_MONTH = " + time);
            //按小时计算
            EmotionStore.updateFitnessByUserId(resolver,
                    EmotionStore.getMasterId(), (float) (time / 60.0));
            SharedPreferenceUtil.saveInt(context, RUNNING_TIME_OF_MONTH, 0);//清零月度时间值
            SharedPreferenceUtil.saveInt(context, ELAPSE_REAL_TIME, 0);//清零系统运行时间值
        } else {
            LogUtils.d(TAG, "running time = " + SharedPreferenceUtil.readInt(context, RUNNING_TIME_OF_MONTH));
        }
    }

    /**
     * <p>结算一次月度时间值，规则如下：<p/>
     * 1、开关机时:（在没有低电保护，关机可能不会调用该方法，所以开机的时候做一次冗余调用）
     * 2、只在开关机时候结算一次
     *
     * @param context Context
     */
    public synchronized static void calculateRobotRunningTime(Context context) {
        //开、关机时，更新月度时间值
        int elapse_time = SharedPreferenceUtil.readInt(context, ELAPSE_REAL_TIME);
        int running_time = SharedPreferenceUtil.readInt(context, RUNNING_TIME_OF_MONTH);
        int time = elapse_time + running_time;
        SharedPreferenceUtil.saveInt(context, RUNNING_TIME_OF_MONTH, time);
        SharedPreferenceUtil.saveInt(context, ELAPSE_REAL_TIME, 0);//合并到月度时间内则清零
        LogUtils.d(TAG, "save time = " + time + ", elapse_time = " + elapse_time + ", running_time =" + running_time);
    }

    /**
     * TIME_TICK广播，每个1分钟备份系统运行时间
     *
     * @param context Context
     */
    public synchronized static void backupElapseRealTime(Context context) {
        int time = (int) (SystemClock.elapsedRealtime() / 60000.0);
        Log.v(TAG, "backup elapse time = " + time);
        SharedPreferenceUtil.saveInt(context, ELAPSE_REAL_TIME, time);
    }
}
