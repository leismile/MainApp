package com.ubtechinc.protocollibrary.protocol;

/**
 * @Deseription
 * @Author tanghongyu
 * @Time 2018/3/14 20:36
 */

public class



CmdId {
    public static final byte IM_VERSION = 0x01;
    //request
    public static final short RESPONSE_BASE = 1000;
    /**
     * 手机端发送获取Action列表指令
     */
    public final static short BL_GET_ACTION_LIST_REQUEST = 1;

    /**
     * 机器人端向手机端发送Action列表
     */
    public final static short BL_GET_ACTION_LIST_RESPONSE = BL_GET_ACTION_LIST_REQUEST + RESPONSE_BASE;

    /**
     * 收到手机端发送过来的动作名称
     */
    public final static short BL_PLAY_ACTION_REQUEST=2;

    /**
     * 机器人播放动作结果
     */
    public final static short BL_PLAY_ACTION_RESPONSE = BL_PLAY_ACTION_REQUEST + RESPONSE_BASE;

    /**
     * 控制TTS
     */
    public final static short BL_CONTROL_TTS_REQUEST = 3;

    public final static short BL_CONTROL_TTS_RESPONSE = BL_CONTROL_TTS_REQUEST + RESPONSE_BASE;

    /**
     * 动作停止
     */
    public final static short BL_STOP_ACTION_REQUEST = 4;

    public final static short BL_STOP_ACTION_RESPONSE = BL_STOP_ACTION_REQUEST + RESPONSE_BASE;

    /**
     * 人脸个数
     */
    public final static short BL_GET_RECOGNISE_FACE_COUNT_REQUEST = 5;

    public final static short BL_GET_RECOGNISE_FACE_COUNT_RESPONSE = BL_GET_RECOGNISE_FACE_COUNT_REQUEST + RESPONSE_BASE;

    /**
     * 人脸性别
     */
    public final static short BL_GET_FACE_ANALYZE_REQUEST = 6;

    public final static short BL_GET_RECOGNISE_FACE_GENDER_RESPONSE = BL_GET_FACE_ANALYZE_REQUEST + RESPONSE_BASE;

    /**
     * 水果识别
     */
    public final static short BL_GET_RECOGNISE_OBJECT_REQUEST = 7;

    public final static short BL_GET_RECOGNISE_OBJECT_RESPONSE = BL_GET_RECOGNISE_OBJECT_REQUEST + RESPONSE_BASE;
    /**
     * 获取机器人序列号
     */
    public final static short BL_GET_ROBOT_SID_REQUEST = 8;

    public final static short BL_GET_ROBOT_SID_RESPONSE = BL_GET_ROBOT_SID_REQUEST + RESPONSE_BASE;

    /**
     * 获取机器人表情列表
     */
    public final static short BL_GET_EXPRESS_LIST_REQUEST = 9;

    public final static short BL_GET_EXPRESS_LIST_RESPONSE = BL_GET_EXPRESS_LIST_REQUEST + RESPONSE_BASE;

    /**
     * 执行表情
     */
    public final static short BL_PLAY_EXPRESS_REQUEST = 10;

    public final static short BL_PLAY_EXPRESS_RESPONSE = BL_PLAY_EXPRESS_REQUEST + RESPONSE_BASE;
    /**
     * 获取红外距离
     */
    public final static short BL_GET_DISTANCE_REQUEST = 11;

    public final static short BL_GET_DISTANCE_RESPONSE = BL_GET_DISTANCE_REQUEST + RESPONSE_BASE;

    /**
     * 控制嘴巴灯
     */
    public final static short BL_CONTROL_MOUTH_LAMP_REQUEST = 12;

    public final static short BL_CONTROL_MOUTH_LAMP_RESPONSE = BL_CONTROL_MOUTH_LAMP_REQUEST + RESPONSE_BASE;
    /**
     * 设置嘴巴灯颜色
     */
    public final static short BL_SET_MOUTH_LAMP_REQUEST = 13;

    public final static short BL_SET_MOUTH_LAMP_RESPONSE = BL_SET_MOUTH_LAMP_REQUEST + RESPONSE_BASE;

    /**
     * 监听音量键
     */
    public final static short BL_OBSERVE_VOLUME_KEY_PRESS_REQUEST = 14;

    public final static short BL_OBSERVE_VOLUME_KEY_PRESS_RESPONSE = BL_OBSERVE_VOLUME_KEY_PRESS_REQUEST + RESPONSE_BASE;

    /**
     * 监听电池状态
     */
    public final static short BL_OBSERVE_BATTERY_STATUS_REQUEST = 15;


    public final static short BL_OBSERVE_BATTERY_STATUS_RESPONSE = BL_OBSERVE_BATTERY_STATUS_REQUEST + RESPONSE_BASE;

    /**
     * 监听拍头状态
     */
    public final static short BL_OBSERVE_RACKET_HEAD_REQUEST = 16;


    public final static short BL_OBSERVE_RACKET_HEAD_RESPONSE = BL_OBSERVE_RACKET_HEAD_REQUEST + RESPONSE_BASE;


    /**
     * 监听跌倒爬起状态
     */
    public final static short BL_OBSERVE_FALL_DOWN_REQUEST = 17;


    public final static short BL_OBSERVE_FALL_DOWN_RESPONSE = BL_OBSERVE_FALL_DOWN_REQUEST + RESPONSE_BASE;

    /**
     * 监听机器人姿态变化
     */
    public final static short BL_OBSERVE_ROBOT_POSTURE_REQUEST = 18;


    public final static short BL_OBSERVE_ROBOT_POSTURE_RESPONSE = BL_OBSERVE_ROBOT_POSTURE_REQUEST + RESPONSE_BASE;

    /**
     * 监听红外距离
     */
    public final static short BL_OBSERVE_DISTANCE_REQUEST = 19;

    public final static short BL_OBSERVE_DISTANCE_RESPONSE = BL_OBSERVE_DISTANCE_REQUEST + RESPONSE_BASE;


    /**
     * 拍照
     */
    public final static short BL_TAKE_PICTURE_REQUEST = 20;

    public final static short BL_TAKE_PICTURE_RESPONSE = BL_TAKE_PICTURE_REQUEST + RESPONSE_BASE;

    /**
     * 人脸识别
     */
    public final static short BL_FACE_RECOGNISE_REQUEST = 21;

    public final static short BL_FACE_RECOGNISE_RESPONSE = BL_FACE_RECOGNISE_REQUEST + RESPONSE_BASE;

    /**
     * 人脸操作
     */
    public final static short BL_CONTROL_FIND_FACE_REQUEST = 22;

    public final static short BL_CONTROL_FIND_FACE_RESPONSE = BL_CONTROL_FIND_FACE_REQUEST + RESPONSE_BASE;

    /**
     * 移动机器人
     */
    public final static short BL_MOVE_ROBOT_REQUEST = 23;

    public final static short BL_MOVE_ROBOT_RESPONSE = BL_MOVE_ROBOT_REQUEST + RESPONSE_BASE;

    /**
     * 翻译模式切换
     */
    public final static short BL_TRANSLATE_MODEL_SWITCH_REQUEST = 24;

    public final static short BL_TRANSLATE_MODEL_SWITCH_RESPONSE = BL_TRANSLATE_MODEL_SWITCH_REQUEST + RESPONSE_BASE;

    /**
     * Behavior播放
     */
    public final static short BL_CONTROL_BEHAVIOR_REQUEST = 25;

    public final static short BL_CONTROL_BEHAVIOR_RESPONSE = BL_CONTROL_BEHAVIOR_REQUEST + RESPONSE_BASE;

    /**
     * 注册人脸
     */
    public final static short BL_CONTROL_REGISTER_FACE_REQUEST = 26;

    public final static short BL_CONTROL_REGISTER_FACE_RESPONSE = BL_CONTROL_REGISTER_FACE_REQUEST + RESPONSE_BASE;

    /**
     * 获取已注册人脸
     */
    public final static short BL_GET_REGISTER_FACES_REQUEST = 27;

    public final static short BL_GET_REGISTER_FACES_RESPONSE = BL_GET_REGISTER_FACES_REQUEST + RESPONSE_BASE;

    /**
     * 恢复初始状态
     */
    public final static short BL_REVERT_ORIGINAL_REQUEST = 28;

    public final static short BL_REVERT_ORIGINAL_RESPONSE = BL_REVERT_ORIGINAL_REQUEST + RESPONSE_BASE;

    /**
     * 握手
     */
    public final static short BL_HAND_SHAKE_REQUEST = 29;

    public final static short BL_HAND_SHAKE_RESPONSE = BL_HAND_SHAKE_REQUEST + RESPONSE_BASE;

    /**
     * 心跳
     */
    public final static short BL_HEART_BEAT_REQUEST = 30;

    public final static short BL_HEART_BEAT_RESPONSE = BL_HEART_BEAT_REQUEST + RESPONSE_BASE;

    /**
     * 退出编程模式
     */
    public final static short BL_EXIT_CODING_REQUEST = 31;

    public final static short BL_EXIT_CODING_RESPONSE = BL_EXIT_CODING_REQUEST + RESPONSE_BASE;

    /**
     * 配网绑定相关
     */
    public final static short BL_BIND_OR_SWITCH_WIFI_REQUEST = 32;

    public final static short BL_BIND_OR_SWITCH_WIFI_RESPONSE = BL_BIND_OR_SWITCH_WIFI_REQUEST + RESPONSE_BASE;

    /**
     * 控制进入运行状态
     */
    public final static short BL_CONTROL_RUNNING_PROGRAM_REQUEST = 33;

    public final static short BL_CONTROL_RUNNING_PROGRAM_RESPONSE = BL_CONTROL_RUNNING_PROGRAM_REQUEST + RESPONSE_BASE;
}
