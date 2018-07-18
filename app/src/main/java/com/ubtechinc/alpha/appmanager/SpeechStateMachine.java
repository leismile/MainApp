package com.ubtechinc.alpha.appmanager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtech.utilcode.utils.thread.HandlerUtils;
import com.ubtech.utilcode.utils.thread.ThreadPool;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.express.listeners.AnimationListener;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.speech.SpeechApi;

/**
 * 语音服务状态机
 */
public class SpeechStateMachine extends StateMachineBase<SpeechStateEnum> {
  private static SpeechStateMachine instance;
  public static final String TAG = "SpeechStateMachine";
  private Handler normalHandler;
  private volatile boolean think1IsRunning = false;
  private volatile boolean think2IsRunning = false;
  private Context context;
  private int wakeupAngel;

  public static SpeechStateMachine get(Context context) {
    if (instance == null) {
      synchronized (SpeechStateMachine.class) {
        if (instance == null) {
          instance = new SpeechStateMachine(context);
        }
      }
    }
    return instance;
  }

  private SpeechStateMachine(Context context) {
    this.context = context;
  }

  public void start() {
    enterNormalState();
  }

  public void setWakeupAngel(int angel) {
    wakeupAngel = angel;
  }

  @Override protected void init() {
    normalHandler = new Handler(Looper.getMainLooper());
    normalRunnable = new NormalRunnable();
    setState(SpeechStateEnum.Init);
  }

  @Override protected void stateChange(SpeechStateEnum oldState, SpeechStateEnum newState) {
    switch (newState) {
      case HasWakeup:
        LogUtils.i(TAG,"HasWakeup=========");
        handleWakeup();
        if (oldState == SpeechStateEnum.Recording
            || oldState == SpeechStateEnum.WaitAsrResult) { //一次语音交互未结束，就被唤醒打断
          //TODO:
        }
        break;
      case Recording: //开始录音
        LogUtils.i(TAG,"Recording=========");
        if (!isWakeupExpressRunning) {
          doListeningExpress();
        } else {
            waitWakupEnd = true;
        }
        break;
      case WaitAsrResult: //录音结束，等待语义识别结果
        waitWakupEnd = false; //录音结束了就不用了
        if (oldState == SpeechStateEnum.Recording) {
          waitForResult();
        }
        break;
      case Init:
        waitWakupEnd = false;
        if (oldState == SpeechStateEnum.WaitAsrResult) {
            handleAsrEnd();
        } else if (oldState == SpeechStateEnum.Recording) { //在录音阶段出错
          ExpressApi.get().doExpress("normal_1", 1, false, Priority.MAXHIGH, new AnimationListener() {
              @Override
              public void onAnimationStart() {
              }

              @Override
              public void onAnimationEnd() {
              }

              @Override
              public void onAnimationRepeat(int loopNumber) {
              }
          });
        }
        enterNormalState();
    }
  }

  private void doListeningExpress() {
      ExpressApi.get().doExpress("listening",  16, false, Priority.MAXHIGH, new AnimationListener() {
        @Override public void onAnimationStart() {

        }

        @Override public void onAnimationEnd() {

        }

        @Override public void onAnimationRepeat(int i) {

        }
      });
  }

  private boolean isWakeupExpressRunning = false;
  private boolean waitWakupEnd = false;
  private void handleWakeup() {
    LogUtils.d(TAG, "handleWakeup---眨一下眼睛");
    isWakeupExpressRunning = true;
    ExpressApi.get().doExpress("wakeup",  1, false,Priority.MAXHIGH, new AnimationListener() {
      @Override public void onAnimationStart() {

      }

      @Override public void onAnimationEnd() {
          isWakeupExpressRunning = false;
          if (waitWakupEnd && SpeechStateMachine.get(context).getCurrentState() == SpeechStateEnum.Recording) {
              doListeningExpress();
              waitWakupEnd = false;
          }
      }

      @Override public void onAnimationRepeat(int i) {

      }
    });
    //        SoundVolumesUtils.get(context).playWakeupSound();
    VoicePool.get().stopTTs(Priority.MAXHIGH,null);
    //        AlphaUtils.interruptAll(context);
  }

  public synchronized void waitForResult() {
    LogUtils.d(TAG, "handleRecordEnd---思考1");
    quitNormalState();
    think1IsRunning = true;
    ExpressApi.get().doExpress("normal_1", 1, false, Priority.MAXHIGH, new AnimationListener() {
      @Override
      public void onAnimationStart() {

      }

      @Override
      public void onAnimationEnd() {
           showThink1();
      }

      @Override
      public void onAnimationRepeat(int loopNumber) {

      }
    });
  }

  public synchronized void handleAsrEnd() {
    LogUtils.d(TAG, "handleAsrEnd----think1IsRunning = " + think1IsRunning);
//    if (think1IsRunning || think2IsRunning) { //等待“思考1”执行完或者“思考2”至少完整的执行一次后再执行“思考3”
//      return;
//    } else {
//      showThink3();
//    }
    showThink3();
  }

  private void showThink1() {
    ExpressApi.get().doExpress("thinking_1", 1, false,Priority.NORMAL, new AnimationListener() {
      @Override public void onAnimationStart() {

      }

      @Override public void onAnimationEnd() {
        think1IsRunning = false;
        LogUtils.d(TAG, "思考1--onAnimationEnd --currState = " + getCurrentState());
        if (getCurrentState() == SpeechStateEnum.WaitAsrResult) {
          showThink2();
        } else if (getCurrentState() == SpeechStateEnum.Init) { //已经收到结果，恢复init状态
          showThink3();
        } else {
          //TODO:其他状态不处理
        }
      }

      @Override public void onAnimationRepeat(int loopNumber) {

      }
    });
  }



  private void showThink2() {
    LogUtils.d(TAG, "handleRecordEnd---思考2");
    think2IsRunning = true;
    ExpressApi.get().doExpress("thinking_2",  10, false, Priority.NORMAL,new AnimationListener() {
      @Override public void onAnimationStart() {

      }

      @Override public void onAnimationEnd() {
        think2IsRunning = false;
      }

      @Override public void onAnimationRepeat(int loopNumber) {
        if (getCurrentState() == SpeechStateEnum.Init) { //已经收到结果，恢复init状态
          showThink3();
        }
      }
    });
  }

  private void showThink3() {
    LogUtils.d(TAG, "handleAsrEnd---思考3"); //这个表情允许被抢占
    ExpressApi.get().doExpress("thinking_3", 1, false,Priority.NORMAL, new AnimationListener() {
      @Override public void onAnimationStart() {

      }

      @Override public void onAnimationEnd() {

      }

      @Override public void onAnimationRepeat(int loopNumber) {

      }
    });
  }

  private Runnable normalRunnable;

  private class NormalRunnable implements Runnable {
    @Override public void run() {
      LogUtils.d(TAG, "normalRunnable---眨一下眼睛");
      ExpressApi.get().doExpress("眨一下眼睛");
      normalHandler.postDelayed(normalRunnable, 5000);
    }
  }

  //TODO:临时去掉
  private void enterNormalState() {
    LogUtils.d(TAG, "enterNormalState");
    //        normalHandler.removeCallbacks(normalRunnable);
    //        normalHandler.postDelayed(normalRunnable, 5000);
  }

  private void quitNormalState() {
    //        normalHandler.removeCallbacks(normalRunnable);
  }
}
