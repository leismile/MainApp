package com.ubtechinc.protocollibrary.communite;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * Im 超时实现类
 * Created by ubt on 2017/12/2.
 */

public class ImTimeoutEngine extends ITimeoutEngine {

    private static ImTimeoutEngine instance;

    public static final long TIME_OUT = 30000;

    private HandlerThread timerThread;
    private Handler timerHandler;

    private Map<Integer,String> request2RobotIdMap = new HashMap<>();

    public static ImTimeoutEngine get() {
        if (instance == null) {
            synchronized (ImTimeoutEngine.class) {
                if (instance == null) {
                    instance = new ImTimeoutEngine();
                }
            }
        }
        return instance;
    }

    private ImTimeoutEngine() {
        timerThread = new HandlerThread("im_timeout_thread");
        timerThread.start();
        timerHandler = new Handler(timerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                onTimeout(msg.what);
            }
        };
    }

    @Override
    public void onSetTimeout(short cmId, int requestId) {
        Message msg = Message.obtain();
        msg.what = requestId;
        msg.arg1 = cmId;
        timerHandler.sendMessageDelayed(msg, TIME_OUT);
    }

    @Override
    public void onCancelTimeout(Long requestId) {
        int what = requestId.intValue();
        if (timerHandler.hasMessages(what)) {
            timerHandler.removeMessages(what);
        }

    }

    @Override
    public void release() {
        super.release();
        if (timerThread != null) {
            timerThread.quit();
        }
        request2RobotIdMap.clear();
        instance = null;
    }


}
