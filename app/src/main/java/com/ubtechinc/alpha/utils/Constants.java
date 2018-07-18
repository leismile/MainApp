/*
 *
 *  *
 *  *  *
 *  *  * Copyright (c) 2008-2017 UBT Corporation.  All rights reserved.  Redistribution,
 *  *  *  modification, and use in source and binary forms are not permitted unless otherwise authorized by UBT.
 *  *  *
 *  *
 *
 */

package com.ubtechinc.alpha.utils;

import android.os.Environment;

import com.ubtrobot.mini.properties.sdk.PropertiesApi;

/**
 * @desc : 常量
 * @author: Logic
 * @email : logic.peng@ubtech.com
 * @time : 2017/4/20
 * @modifier:
 * @modify_time:
 */

public final class Constants {

    //sys
    public static final String ALPHA_MIC_HARDWARE_VERSION = "ro.hardware.version";
    public static final String ROBOT_SYSTEM_VERSION = "ro.build.description";//[ro.build.description]
    public static final String MIC5_VERSION = "alpha2_10005";
    public static final String MIC2_VERSION = "alpha2_10002";
    public static final String LYNX_SYSTEM_VERSION= "Lynx";
    //动作文件存储路径
    public static String ACTION_PATH = PropertiesApi.getRootPath() + "/actions";

    //binder name
    public static final String ACTION_BINDER_NAME="action";
    public static final String SPEECH_BINDER_NAME="speech";
    public static final String LED_BINDER_NAME="led";
    public static final String MOTOR_BINDER_NAME="motor";
    public static final String SYSINFO_BINDER_NAME = "sysinfo";

    //shareperferences key
    public static  final  String MASTER_NAME = "MASTER_NAME";

    public static final String SIT_DOWN_NAME = "027"; //坐到地板上动作文件名称
    public static final String STANDUP_FROM_FLOOR_NAME = "007";//从地板上站起动作文件名称
    public static final String SPLITS_RIGHT_NAME = "028";//右脚在前劈叉
    public static final String SQUATDOWN_NAME = "031"; //蹲着动作文件
    public static final String STANDBYSQUAT_0001 = "standbysquat_0001";//未插线，从站立待命进入蹲下待命状态的表现
    public static final String STANDBYSQUAT_0002 = "standbysquat_0002";//插线，从站立待命进入蹲下待命状态的表现
    public static final String SLEEPMODE_0001_1 = "sleepmode_0001_1";//未插线，从蹲下待命长时间未见到人，进入待机状态
    public static final String SLEEPMODE_0001_2 = "sleepmode_0001_2";//插线，从蹲下待命长时间未见到人，进入待机状态
    public static final String SLEEPMODE_0001_3 = "sleepmode_0001_3";//从蹲下待命长时间未见到人，且当前是非站立状态，则掉电，进入待机状态
    public static final String SLEEPMODE_0002_1 = "sleepmode_0002_1";//未插线，语音让机器人待机的表现
    public static final String SLEEPMODE_0002_2 = "sleepmode_0002_2";//插线，语音让机器人待机的表现
    public static final String SLEEPMODE_0002_3 = "sleepmode_0002_3";//语音让机器人待机，机器人为非站立状态，则掉电进入待机状态
}
