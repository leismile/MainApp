package com.ubtechinc.alpha.appmanager;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtech.utilcode.utils.thread.HandlerUtils;
import com.ubtechinc.alpha.SystemProperties;
import com.ubtechinc.alpha.utils.MouthUtils;
import com.ubtechinc.alpha.utils.SystemPropertiesUtils;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.speech.SpeechApi;
import com.ubtrobot.speech.protos.Speech;
import com.ubtrobot.speech.receivers.AsrStateReceiver;
import com.ubtrobot.speech.receivers.TTsStateReceiver;
import com.ubtrobot.speech.receivers.WakeupReceiver;

/**
 * description: 机器人状态管理类，管理 wifi, tts播报， 语音收录，是否是低电，是否在充电等各种状态信息
 * * autour: bob.xu
 * date: 2017/8/4 13:38
 * update: 2017/8/4
 * version: a
 */
public class StateManager {
    //TODO:状态变化要有队列的概念，要串行处理保证时序正确。
    private static StateManager sInstance;
    private Context context;

    public static final String TAG = "RobotStateManager";
    private HandlerThread normalStateThread;
    private Handler normalHandler;

    public static StateManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (StateManager.class) {
                if (sInstance == null) {
                    sInstance = new StateManager(context);
                }
            }
        }
        return sInstance;
    }

    private StateManager(Context context) {
        this.context = context;
        normalStateThread = new HandlerThread("normalStateThread");
        normalStateThread.start();
        normalHandler = new Handler(normalStateThread.getLooper());
    }

    public void init() {
        SpeechStateMachine.get(context).start();
        SpeechApi.get().subscribeEvent(new WakeupReceiver() {
            @Override
            public void onWakeup(Speech.WakeupParam wakeupParam) {
                handleWakeup(wakeupParam);
            }
        });

        SpeechApi.get().subscribeEvent(new TTsStateReceiver() {

            @Override
            public void onStateChange(Speech.TTsState tTsState) {
                handleTtsState(tTsState);
            }
        });

        SpeechApi.get().subscribeEvent(new AsrStateReceiver() {

            @Override
            public void onStateChange(Speech.ASRState asrState, int errCode) {
                handleAsrState(asrState, errCode);
            }
        });
    }

    public void handleAsrState(Speech.ASRState asrState, int errCode) {
        switch (asrState) {
            case RECORD_BEGIN:
                LogUtils.d(TAG, "ASR State Change-----RECORD_BEGIN");
                SpeechStateMachine.get(context).setState(SpeechStateEnum.Recording);
                break;
            case RECORD_END:
                LogUtils.d(TAG, "ASR State Change-----RECORD_END");
                SpeechStateMachine.get(context).setState(SpeechStateEnum.WaitAsrResult);
                break;
            case ASR_RESULT:
                LogUtils.d(TAG, "ASR State Change-----ASR_RESULT");
                SpeechStateMachine.get(context).setState(SpeechStateEnum.Init);
                break;
            case ASR_FAIL:
                LogUtils.d(TAG, "ASR State Change-----ASR_FAIL---errorCode = " + errCode);
                if (errCode == Speech.ErrorCode.No_Voice_VALUE
                        || errCode == Speech.ErrorCode.No_AccessToken_VALUE
                        || errCode == Speech.ErrorCode.REFRESH_TOKEN_ERROR_VALUE
                        || errCode == Speech.ErrorCode.No_Network_VALUE) { //录音阶段异常结束
                    SpeechStateMachine.get(context).setState(SpeechStateEnum.Init); //失败后进入初始态
                } else { //语义识别阶段异常
                    SpeechStateMachine.get(context).setState(SpeechStateEnum.Init);
                }
                errorHandler(errCode);
                break;
            default:
                break;
        }
    }

    private void handleTtsState(Speech.TTsState tTsState) {
        if (tTsState == Speech.TTsState.BEGIN) {
            LogUtils.d(TAG, "TTS---stateChange---Begin");
//      ExpressApi.get().doExpress("awake_004", 1, 65535, true, new AnimationListener() {
//        @Override public void onAnimationStart() {
//
//        }
//
//        @Override public void onAnimationEnd() {
//
//        }
//
//        @Override public void onAnimationRepeat(int i) {
//
//        }
//      });
            MouthUtils.ttsMouthLed();
        } else if (tTsState == Speech.TTsState.END) {
            LogUtils.d(TAG, "TTS---stateChange---End");
//      ExpressApi.get().doExpressTween(new AnimationListener() {
//        @Override public void onAnimationStart() {
//
//        }
//
//        @Override public void onAnimationEnd() {
//
//        }
//
//        @Override public void onAnimationRepeat(int i) {
//
//        }
//      });
            MouthUtils.normalMouthLed();
        }
    }

    private void handleWakeup(final Speech.WakeupParam wakeupParam) {
        LogUtils.d(TAG, "Wakeup----");
        SpeechStateMachine.get(context).setWakeupAngel(wakeupParam.getAngle());
        SpeechStateMachine.get(context).setState(SpeechStateEnum.HasWakeup);
    }

    private void errorHandler(int errorCode) {
        if (errorCode == Speech.ErrorCode.No_Network_VALUE) {
            //            SpeechApi.get().playTTs("请检查网络是否正常",nullTTsListener);
            HandlerUtils.getMainHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    VoicePool.get().playLocalTTs("network_not_available_001", Priority.HIGH, nullTTsListener);
                }
            }, 1500);

        } else if (errorCode == Speech.ErrorCode.No_AccessToken_VALUE) {
            boolean isFirstStart = SystemPropertiesUtils.getFirststart();
            LogUtils.i(TAG,"isFirstStart=====" + isFirstStart);
            if (!isFirstStart) {
                HandlerUtils.getMainHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        VoicePool.get().playLocalTTs("not_logged_in_002", Priority.HIGH, nullTTsListener);
                    }
                }, 1500);

            } else if (errorCode
                    == Speech.ErrorCode.No_Response_VALUE/* || errorCode == Speech.ErrorCode.No_Voice_VALUE*/) {
                //SpeechApi.get().playTTs("没有听到你的声音,大点声哦",nullTTsListener);
            } else if (errorCode == Speech.ErrorCode.REFRESH_TOKEN_ERROR_VALUE) {
                VoicePool.get().playLocalTTs("not_logged_in_001", Priority.HIGH, nullTTsListener);
            } else if (errorCode == Speech.ErrorCode.Speech_Service_Error_VALUE) {
                VoicePool.get().playLocalTTs("speechservice_error_002", Priority.HIGH, nullTTsListener);
            } else if (errorCode == Speech.ErrorCode.Dns_Tvs_Resolve_Error_VALUE) {
                VoicePool.get().playLocalTTs("speechservice_error_004", Priority.HIGH, nullTTsListener);
            } else if (errorCode == Speech.ErrorCode.Skill_Not_Allow_VALUE) {
                VoicePool.get().playLocalTTs("standup_001", Priority.HIGH, nullTTsListener);
            } else if (errorCode == Speech.ErrorCode.Connect_Error_VALUE) {
                VoicePool.get().playLocalTTs("speechservice_error_001", Priority.HIGH, nullTTsListener);
            } else if (errorCode == Speech.ErrorCode.Request_Timeout_VALUE) {
                VoicePool.get().playLocalTTs("network_not_available_002", Priority.HIGH, nullTTsListener);
            } else if (errorCode == Speech.ErrorCode.No_XXX_VALUE) {
                VoicePool.get().playLocalTTs("speechservice_error_006", Priority.HIGH, nullTTsListener);
            }
        }

        LogUtils.d(TAG, "errorHandler---errorCode = " + errorCode);
    }

    VoiceListener nullTTsListener = new VoiceListener() {


        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(int errCode, String errMsg) {

        }
    };
}