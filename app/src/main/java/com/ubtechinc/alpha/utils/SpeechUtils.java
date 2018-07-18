/*
 *
 *  *
 *  * Copyright (c) 2008-2016 UBT Corporation.  All rights reserved.  Redistribution,
 *  *  modification, and use in source and binary forms are not permitted unless otherwise authorized by UBT.
 *  *
 *
 */

package com.ubtechinc.alpha.utils;

import android.content.Context;

import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.speech.SpeechApi;
import com.ubtrobot.speech.listener.TTsListener;

import java.util.Locale;


/**
 * @author paul.zhang@ubtrobot.com
 * @date 2016/12/27
 * @Description 工具类、主要提供静态方法
 * @modifier
 * @modify_time
 */

public class SpeechUtils {
	private static final int PI = 180;
	/**
	 * @Description 获取当前系统语言
	 * @param context 下文
	 * @return 当前系统语言
	 * @throws
	 */

	public static String getSystemLanguage(Context context) {
		Locale locale = context.getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		return language;
	}
	/**
	 * @Description 根据当前角度获取相应弧度
	 * @param angle 角度
	 * @return 弧度 pi
	 * @throws
	 */

	public static float getRadian(int angle) {
		return (float) (angle * (Math.PI / 180));
	}
	/**
	 * @Description 根据ROS腰部可旋转范围获取一个合理的旋转角度
	 * @param a 唤醒角度
	 * @return
	 * @throws
	 */

	public static int getAngle(int a){
		int angle = a;
		if (a > PI) {
			angle = 2 * PI - a;
			angle = -angle;
		}
		return angle ;
	}

	/**
	 * 播放开机问候语
	 */
	public static void speekOn(){
		String[] datas = {"早上好","上午好","中午好","下午好","晚上好"};
		int period = SysTimeUtils.periodOfTheCurrentTime();
		VoicePool.get().playLocalTTs(datas[period], Priority.NORMAL, new VoiceListener() {
			@Override
			public void onCompleted() {
			}

			@Override
			public void onError(int i, String s) {
			}
		});

	}
}
