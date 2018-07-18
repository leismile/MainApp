package com.ubtechinc.alpha.appmanager;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtechinc.alpha.utils.MouthUtils;
import com.ubtechinc.alpha.utils.SpeechUtils;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.express.listeners.AnimationListener;
import com.ubtrobot.motor.MotorApi;
import com.ubtrobot.speech.SpeechApi;
import com.ubtrobot.speech.protos.Speech;
import com.ubtrobot.speech.receivers.InitResultReceiver;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;

import ubtechinc.com.standupsdk.StandUpApi;

/**
 * 主服务开启时机器人的表现管理
 * Created by lulin.wu on 2018/3/1.
 */

public class MainServiceStartManager {
    private static final String TAG = "MainServiceStartManager";
    private MainServiceStartManager(){}
    private static class MainServiceStartManagerHolder {
        public static MainServiceStartManager instance = new MainServiceStartManager();
    }
    public static MainServiceStartManager getInstance(){
        return MainServiceStartManagerHolder.instance;
    }

    public void start(){
        final long startTime = System.currentTimeMillis();
        Log.i(TAG,"start===11111============" + System.currentTimeMillis());
        MouthUtils.normalMouthLed();
        ExpressApi.get().doExpress("mainService_loop", 100,false , Priority.HIGH,new AnimationListener() {
            @Override
            public void onAnimationStart() {
                Log.i(TAG,"onAnimationStart==========" + (System.currentTimeMillis() - startTime));
            }
            @Override
            public void onAnimationEnd() {
                Log.i(TAG,"onAnimationEnd========" + (System.currentTimeMillis() - startTime));
//                ExpressApi.get().doExpress("mainService_loop",100,false , Priority.HIGH,null);
            }
            @Override
            public void onAnimationRepeat(int loopNumber) {
            }
        });
    }
}
