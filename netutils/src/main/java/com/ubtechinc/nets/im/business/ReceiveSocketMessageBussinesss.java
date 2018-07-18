package com.ubtechinc.nets.im.business;

import com.ubtechinc.alpha.im.IMsgHandleEngine;
import com.ubtechinc.alpha.im.ImMsgDispathcer;
import com.ubtechinc.nets.im.service.MsgHandleTask;
import com.ubtechinc.nets.phonerobotcommunite.IReceiveMsg;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2017/5/25.
 */

public class ReceiveSocketMessageBussinesss implements IReceiveMsg {

    private ExecutorService receiveMsgThreadPool = Executors.newCachedThreadPool();

    private static ReceiveSocketMessageBussinesss sInstance;
    private static String TAG = "ReceiveSocketMessageBussinesss";
    public static ReceiveSocketMessageBussinesss getInstance() {
        if (sInstance == null) {
            synchronized (ReceiveSocketMessageBussinesss.class) {
                sInstance = new ReceiveSocketMessageBussinesss();
            }
        }
        return sInstance;
    }

    private ReceiveSocketMessageBussinesss() {
    }

    @Override
    public void init() {
    }

    @Override
    public void setIMsgDispatcher(ImMsgDispathcer msgDispathcer) {
        IMsgHandleEngine.getInstance().setIMsgDispatcher(msgDispathcer);
    }

    public void handleReceivedMessage(byte[] data ,String peer) {
       // LogUtils.d(TAG, "handleReceivedMessage");
       // LogUtils.d(TAG, "收到消息--type : custom,msg:" + data+peer);
        MsgHandleTask parserTask = new MsgHandleTask(data, peer);
        receiveMsgThreadPool.execute(parserTask);
    }

}
