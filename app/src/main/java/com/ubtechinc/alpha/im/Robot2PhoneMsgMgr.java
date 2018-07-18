package com.ubtechinc.alpha.im;

import android.support.annotation.NonNull;

import com.google.protobuf.GeneratedMessageLite;
import com.ubtech.utilcode.utils.LogUtils;
import com.ubtech.utilcode.utils.notification.NotificationCenter;
import com.ubtech.utilcode.utils.notification.Subscriber;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.event.PowerOffEvent;
import com.ubtechinc.alpha.event.SidEvent;
import com.ubtechinc.alpha.robotinfo.RobotState;
import com.ubtechinc.nets.phonerobotcommunite.ICallback;
import com.ubtechinc.nets.phonerobotcommunite.RobotCommuniteManager;


/**
 * Created by Administrator on 2017/5/25.
 */

public class Robot2PhoneMsgMgr {
    private static Robot2PhoneMsgMgr sInstance;
    private static String TAG = "Robot2PhoneMsgMgr";

    public static Robot2PhoneMsgMgr getInstance() {
        if (sInstance == null) {
            synchronized (Robot2PhoneMsgMgr.class) {
                if (sInstance == null) {
                    sInstance = new Robot2PhoneMsgMgr();
                }
            }
        }
        return sInstance;
    }

    public synchronized void init() {
        LogUtils.i("init");
        RobotCommuniteManager.getInstance().init();
        RobotCommuniteManager.getInstance().setMsgDispathcer(new ImMainServiceMsgDispatcher());
        NotificationCenter.defaultCenter().subscriber(PowerOffEvent.class, powerOffSubscriber);
        NotificationCenter.defaultCenter().subscriber(SidEvent.class, sidSubscriber);
    }

    Subscriber<PowerOffEvent> powerOffSubscriber = new Subscriber<PowerOffEvent>() {
        @Override
        public void onEvent(PowerOffEvent o) {
            TecentIMManager.getInstance(AlphaApplication.getContext()).logout();
        }
    };

    Subscriber<SidEvent> sidSubscriber = new Subscriber<SidEvent>() {
        @Override
        public void onEvent(SidEvent event) {
            TecentIMManager.getInstance(AlphaApplication.getContext()).init(RobotState.get().getSid());
        }
    };

    public void sendData(int cmdId, String version, @NonNull GeneratedMessageLite requestBody, String peer, @NonNull ICallback<AlphaMessageOuterClass.AlphaMessage> dataCallback) {
        RobotCommuniteManager.getInstance().sendData(cmdId, version, requestBody, peer, dataCallback);
    }

}
