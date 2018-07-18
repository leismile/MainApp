package com.ubtechinc.bluetoothrobot;

/**
 * @Deseription
 * @Author tanghongyu
 * @Time 2018/4/2 14:55
 */

public class Statics {

    public static final byte PROTO_VERSION = 0x01;

    public static final byte MOUTH_LAMP_MODLE_BREATH = 0x01;
    public static final short MOUTH_LAMP_COLOR_RED = 1;
    public static final short MOUTH_LAMP_COLOR_GREEN = 2;
    public static final short MOUTH_LAMP_COLOR_BLUE = 3;

    public static final byte HEAD_RACKET_CLICK = 0x01;
    public static final byte HEAD_RACKET_LONGPRESS = 0x02;
    public static final byte HEAD_RACKET_DOUBLECLICK = 0x03;
    public static final byte HEAD_RACKET_UNRECOGNIZED = 0x00;

    public static final byte VOLUME_KEY_UP = 0x01;
    public static final byte VOLUME_KEY_DOWN = 0x00;

    public static final byte TAKE_PIC_IMMEDIATELY = 0x00;
    public static final byte TAKE_PIC_WITH_FACE_DETECT = 0x01;

    public static final byte FIND_FACE_START = 0x01;
    public static final byte FIND_FACE_PAUSE = 0x02;
    public static final byte FIND_FACE_STOP = 0x03;
    public static final byte FIND_FACE_CHANGE = 0x04;

    public static final byte DIRECTION_LEFT = 0x01;
    public static final byte DIRECTION_RIGHT = 0x02;
    public static final byte DIRECTION_FORWARD = 0x03;
    public static final byte DIRECTION_BACKWARD = 0x04;

    public static final byte BEHAVIOR_PLAY = 0x01;
    public static final byte BEHAVIOR_STOP = 0x00;

    public static final byte FACE_REGISTER_START = 0x01;
    public static final byte FACE_REGISTER_STOP = 0x00;

    public static final byte TTS_PLAY = 0x01;
    public static final byte TTS_STOP = 0x00;
}
