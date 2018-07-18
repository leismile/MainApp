package com.ubtechinc.contact.util;

import android.support.annotation.NonNull;
import android.util.Log;

import com.ubtechinc.contact.Contact;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;

/**
 * @desc :语音播报助手，若播放网络音频失败，则播放本地音频
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/3/23
 */

public class TTSPlayUtil {

    private static final String TAG = "TTSPlayUtil";
    public static void playTTS(String text) {
        playTTS(text, null, null);
    }

    public static void playTTS(String text, @NonNull final IPlayListener playListener) {
        playTTS(text, null, playListener);
    }

    public static void playTTS(String text, final String lockTTSName) {
        playTTS(text, lockTTSName, null);
    }
        // 优先播放TTS，如果播放失败播放本地音频文件
    public static void playTTS(String text, final String localTTSName, @NonNull final IPlayListener playListener) {
        Log.d(TAG, " playTTS -- text : " + text + " lockTTSname " + localTTSName);
        if(!NetworkUtil.isNetworkConnected(Contact.getInstance().getContext())) {
            Log.d(TAG, " network off ");
            playLocalTTs(localTTSName, playListener);
            return ;
        }
        Log.d(TAG, " network on ");

        VoicePool.get().playTTs(text, Priority.NORMAL, new VoiceListener() {
            @Override
            public void onCompleted() {
                if(playListener != null) {
                    playListener.onFinish();
                }
            }

            @Override
            public void onError(int i, String s) {
                if(localTTSName != null && i == 2) {
                    playLocalTTs(localTTSName, playListener);
                } else {
                    if(playListener != null) {
                        playListener.onError();
                    }
                }
            }
        });
    }

    public static void playLocalTTs(String fileName) {
        Log.d(TAG, " playLocalTTS -- fileName : " + fileName);
        VoicePool.get().playLocalTTs(fileName, Priority.NORMAL, new VoiceListener() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(int i, String s) {
            }
        });
    }

    public static void playLocalTTs(String fileName, @NonNull final IPlayListener playListener) {
        VoicePool.get().playLocalTTs(fileName, Priority.NORMAL, new VoiceListener() {
            @Override
            public void onCompleted() {
                if(playListener != null) {
                    playListener.onFinish();
                }
            }

            @Override
            public void onError(int i, String s) {
                if(playListener != null) {
                    playListener.onError();
                }
            }
        });
    }

    public interface IPlayListener {
        void onError();
        void onFinish();
    }
}
