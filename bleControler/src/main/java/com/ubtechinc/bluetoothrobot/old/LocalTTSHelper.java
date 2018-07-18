package com.ubtechinc.bluetoothrobot.old;

import android.util.Log;

import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.speech.listener.TTsListener;

/**
 * @desc : 本地语音播报助手类
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/4/10
 */

public class LocalTTSHelper {

    private static final String TAG = "LocalTTSHelper";
    private static final String LOCAL_TTS_PLAYBANDING_SUC = "networking_001";
    private static final String LOCAL_ERROR_PASSWORD = "networking_002";
    private static final String LOCAL_TTS_CONNECT_TIMEOUT = "networking_003";
    private static final String LOCAL_NETWORK_NOT_AVAILABLE = "networking_004";
    private static final String LOCAL_TTS_CHOOSE_WIFI_SUC = "networking_005";
    private static final String LOCAL_CONNECT_BEFORE_WIFI = "networking_006";
    private static final String LOCAL_BANGDING_SUCC_TIP_FOR_MUSIC = "networking_007";


    public static void playBandingSuc() {
        Log.d(TAG, " playBandingSuc ");
        VoicePool.get().playLocalTTs(LOCAL_TTS_PLAYBANDING_SUC, Priority.HIGH,new VocieListenerAdatper());
    }

    public static void playErrorPassword() {
        Log.d(TAG, " playErrorPassword ");
        VoicePool.get().playLocalTTs(LOCAL_ERROR_PASSWORD,Priority.HIGH,new VocieListenerAdatper());
    }

    /**
     * Wi-Fi不可用
     */
    public static void playConnectTimeout() {
        Log.d(TAG, " playConnectTimeout ");
        VoicePool.get().playLocalTTs(LOCAL_TTS_CONNECT_TIMEOUT,Priority.HIGH,new VocieListenerAdatper());
    }

    public static void playChooseWifiSuc() {
        Log.d(TAG, " playChooseWifiSuc ");
        VoicePool.get().playLocalTTs(LOCAL_TTS_CHOOSE_WIFI_SUC,Priority.HIGH,new VocieListenerAdatper());
    }

    public static void playNetworkNotAvailable() {
        Log.d(TAG, " playNetworkNotAvailable ");
        VoicePool.get().playLocalTTs(LOCAL_NETWORK_NOT_AVAILABLE,Priority.HIGH,new VocieListenerAdatper());
    }

    public static void playConnectBeforeWifi() {
        Log.d(TAG, " playConnectBeforeWifi ");
        VoicePool.get().playLocalTTs(LOCAL_CONNECT_BEFORE_WIFI,Priority.HIGH,new VocieListenerAdatper());
    }

    public static void playTipsForMusicVip() {
        VoicePool.get().playLocalTTs(LOCAL_BANGDING_SUCC_TIP_FOR_MUSIC,Priority.HIGH,new VocieListenerAdatper());
    }

    private static class VocieListenerAdatper implements VoiceListener {

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(int i, String s) {

        }
    }

    private static class TTsListenerAdapter implements TTsListener {

        @Override
        public void onTtsBegin() {

        }

        @Override
        public void onTtsVolumeChange(int i) {

        }

        @Override
        public void onTtsCompleted() {

        }

        @Override
        public void onError(int i, String s) {

        }
    }
}
