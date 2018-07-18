package com.ubtechinc.alpha.appmanager;

/**
 * Created by ubt on 2017/11/27.
 */

public enum SpeechStateEnum {
    Init, //初始态
    HasWakeup, //已唤醒
    Recording, //录音中
    WaitAsrResult, //语音结束，等待语义识别结果
}
