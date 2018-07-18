package com.ubtechinc.alpha.service.jimucar;

/**
 * @Deseription
 * @Author tanghongyu
 * @Time 2018/3/19 16:21
 */

public class JimuConstants {


    public static final String UUID_SERVICE = "49535343-fe7d-4ae5-8fa9-9fafd205e455";
    public static final String UUID_READ_CHARACTERISTIC = "49535343-1e4d-4bd9-ba61-23c647249616";
    public static final String UUID_WRITE_CHARACTERISTIC = "49535343-8841-43f4-a8d4-ecbe34729bb3";


    //握手
    public static final byte CMD_HAND_SHAKE = 0x01;
    //心跳
    public static final byte CMD_HEART_BEAT = 0x03;
    //自检
    public static final byte CMD_SELF_INSPECTION = 0x05;

    public static final byte CMD_CONTROL_CIRCLE_SEVRO = 0x07; //控制舵机连续转动

    //舵机角度控制
    public static final byte CMD_CONTROL_SEVRO_ANGLE = 0x09;

    public static final byte CMD_WRITE_SENSOR = 0x78;//发送数据到传感器模块

    //马达控制
    public static final byte CMD_CONTROL_MOTOR = (byte) 0x90;

    //马达控制
    public static final byte CMD_CONTROL_STOP_MOTOR = (byte) 0x91;

    //读取主板信息
    public static final byte CMD_READ_MAINBOARD_INFO = 0x08;
    //读取电池信息
    public static final byte CMD_READ_POWER_INFO = 0x27;
    //读取主板外设连接信息：0x39（此功能为自动分配ID的主板专用，旧主板回复：EE）
    public static final byte CMD_READ_DEVICES_ID = 0x39;

    //读取传感器模块的数据
    public static final byte CMD_READ_SENSOR_DATA = 0x7E;

    public static final byte CMD_OPEN_OR_CLOSE_SENSOR = 0x71;

    public static final byte CMD_CHECK_SENSOR = 0x73;


    public static final byte SENSOR_TYPE_ALL = 0x00;
    public static final byte SENSOR_TYPE_IR = 0x01;
    public static final byte SENSOR_TYPE_TOUCH = 0x02;
    public static final byte SENSOR_TYPE_GM = 0x03;
    public static final byte SENSOR_TYPE_LIGHT = 0x04;
    public static final byte SENSOR_TYPE_G = 0x05;
    public static final byte SENSOR_TYPE_ULTRASOUND = 0x06;
    public static final byte SENSOR_TYPE_DG = 0x07;
    public static final byte SENSOR_TYPE_SPEAKER = 0x08;
    public static final byte SENSOR_TYPE_COLOR = 0x09;


    //主板错误
    public static final byte DATA_MAINBOARD_ERROR = 0x01;
    //主板正在初始化
    public static final byte DATA_MAINBOARD_INIT = (byte) 0xEE;
    //其他，忽略
    public static final byte DATA_MAINBOARD_OTHER = 0x00;

    //低电量
    public static final byte DATA_INSPECTION_LOW_POWER = 0x01;
    //舵机问题
    public static final byte DATA_INSPECTION_SERVO_PROBLEM = 0x02;
    //自检成功
    public static final byte DATA_INSPECTION_SUCCESS = 0x00;


}
