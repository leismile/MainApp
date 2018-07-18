package com.ubtechinc.contact.phone;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.ubtechinc.contact.R;
import com.ubtechinc.contact.util.ConditionExecutor;
import com.ubtechinc.contact.util.Constant;
import com.ubtechinc.contact.util.TTSPlayUtil;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.master.Master;
import com.ubtrobot.mini.properties.sdk.PropertiesApi;
import com.ubtrobot.mini.voice.MiniMediaPlayer;
import com.ubtrobot.mini.voice.protos.VoiceProto;
import com.ubtrobot.speech.SpeechApi;

import java.util.Properties;

/**
 * @desc : 默认的铃声接口
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/3/17
 */

public class DefaultRing implements IRing, MiniMediaPlayer.OnPreparedListener {

    private static final String TAG = "DefaultRing";
    private static final String CALL_RING_URL =  "file:///" + PropertiesApi.getRootPath() + "/localTts/callring.mp3";
    private static final String END_RING_URL = "file:///" + PropertiesApi.getRootPath() + "/localTts/endcall.mp3";
    private static final String RING_NAME = "callring.mp3";
    private static final String ENDCALL_NAME = "endcall.mp3";
    private Context context;
    private IRingListener ringListener;
    private ConditionExecutor conditionExecutor;
    private ConditionExecutor conditionExecutorEndRing;
    private MediaPlayer mediaPlayerRing;
    private MiniMediaPlayer voicePlayer;
    private MiniMediaPlayer mediaPlayerEndRing;
    private TTSPlayListener ttsPlayListener;
    private boolean isRing;
    public DefaultRing(Context context) {
        this.context = context;
        conditionExecutor = new ConditionExecutor(new Runnable() {
            @Override
            public void run() {
                if(ringListener != null) {
                    ringListener.onRingCompletely();
                }
                isRing = false;
            }
        });
        conditionExecutorEndRing = new ConditionExecutor(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, " runnable ringListener : " + ringListener);
                if(ringListener != null) {
                    ringListener.onEndRingCompletely();
                }
            }
        });
        ttsPlayListener = new TTSPlayListener();

    }

    @Override
    public void playCommingRing(String name, boolean wakeupState) {
        Log.d(TAG, " playCommingRing ");
        isRing = true;
        conditionExecutor.reset();
        voicePlayer = playMedia(CALL_RING_URL, new MiniMediaPlayer.OnCompletionListener() {
            public void onCompletion(MiniMediaPlayer mp) {
                Log.d(TAG, " onCompletion ");
                conditionExecutor.secondConidtionTwoReach();
                if(voicePlayer != null) {
                    try {
                        voicePlayer.stop();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    voicePlayer.release();
                    voicePlayer = null;
                }
            }
        });
        SpeechApi.get().setTTsVolume(100);
        if(wakeupState) {
            TTSPlayUtil.playTTS(context.getResources().getString(R.string.phone_call, name), Constant.LOCAL_TTSNAME_COMIING, ttsPlayListener);
        } else {
            TTSPlayUtil.playTTS(context.getResources().getString(R.string.phone_call_wakeup, name), Constant.LOCAL_TTSNAME_COMIING, ttsPlayListener);
        }
    }

    @Override
    public void stopCommingRing() {
        Log.d(TAG, " stopCommingRing ");
        stopCommingRingInner();
        isRing = false;
        conditionExecutor.cancle();
    }

    private void stopCommingRingInner() {
        Log.d(TAG, " stopCommingRingInner ");
        if(voicePlayer != null) {
            if(voicePlayer != null) {
                voicePlayer.stop();
                voicePlayer.release();
            }
            voicePlayer = null;
        }
        SpeechApi.get().stopTTs();
    }

    @Override
    public void playEndRing() {
        conditionExecutorEndRing.reset();
        Log.d(TAG, " playEndRing ");
        mediaPlayerEndRing = playMedia(END_RING_URL, new MiniMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MiniMediaPlayer mp) {
                Log.d(TAG, " onCompletion ");
                if(mediaPlayerEndRing != null) {
                    try {
                        mediaPlayerEndRing.stop();
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                    mediaPlayerEndRing.release();
                    mediaPlayerEndRing = null;
                }
                conditionExecutorEndRing.secondConditionOneReach();
            }
        });
        TTSPlayUtil.playLocalTTs(Constant.LOCAL_TTSNAME_END, new TTSPlayUtil.IPlayListener() {
            @Override
            public void onError() {
                conditionExecutorEndRing.secondConidtionTwoReach();
            }

            @Override
            public void onFinish() {
                conditionExecutorEndRing.secondConidtionTwoReach();
            }
        });
    }

    @Override
    public void stopEndRing() {
        if(mediaPlayerEndRing != null) {
            try {
                mediaPlayerEndRing.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaPlayerEndRing.release();
            mediaPlayerEndRing = null;
        }
        SpeechApi.get().stopTTs();
    }

    @Override
    public void setRingListener(IRingListener ringListener) {
        this.ringListener = ringListener;
    }

    @Override
    public boolean isOnRing() {
        return false;
    }

    private MiniMediaPlayer playMedia(String fileName, MiniMediaPlayer.OnCompletionListener onCompletionListener) {
        MiniMediaPlayer player = null;
        try {
            player = MiniMediaPlayer.create(Master.get().getGlobalContext(), VoiceProto.Source.RING);
            player.reset();
            player.setDataSource(fileName);
            player.prepareAsync();
            player.setOnPreparedListener(this);
            player.setOnCompletionListener(onCompletionListener);
        } catch (Exception e) {
            e.printStackTrace();
            if (player != null) {
                player.release();
            }
        }
        return player;
    }

    @Override
    public void onPrepared(MiniMediaPlayer mp) {
        mp.start();
    }

    private class TTSPlayListener implements TTSPlayUtil.IPlayListener{

        @Override
        public void onError() {
            conditionExecutor.secondConditionOneReach();

        }

        @Override
        public void onFinish() {
            conditionExecutor.mainConditionReach();
            stopCommingRingInner();
        }
    }


}
