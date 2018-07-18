package com.ubt.lancommunicationhelper;

import android.content.Context;
import android.util.Log;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.ubtechinc.alpha.PushMessage;
import com.ubtrobot.im.robot.CommChannelManager;
import com.ubtrobot.im.robot.SocketChannelHub;
import com.ubtrobot.im.robot.SocketConnection;
import com.ubtrobot.im.robot.callback.ResultCallback;
import com.ubtrobot.lib.robot.proto.ProtoClass;
import com.ubtrobot.master.param.ProtoParam;


import java.util.ArrayList;

/**
 * Created by bob.xu on 2018/1/15.
 */

public class LanCommunicationProxy {
    private static LanCommunicationProxy instance;
    private Context context;
    private SocketChannelHub socketChannelHub;
    private static final String TAG = "LanCommunicationProxy";
    public static LanCommunicationProxy getInstance(Context context) {
        if (instance == null) {
            synchronized (LanCommunicationProxy.class) {
                if (instance == null) {
                    instance = new LanCommunicationProxy(context);
                }
            }
        }
        return instance;
    }

    private LanCommunicationProxy(Context context) {
        if (context != null) {
            this.context = context.getApplicationContext();
        }
        init();
    }

    private void init() {
        CommChannelManager.init(context);
        socketChannelHub = CommChannelManager.getSocketChannel();
    }

    public void testPush() {
        ProtoClass.ProtoText text = ProtoClass.ProtoText.newBuilder().setText("你好吗？").build();
        pushMsg("test",text);
    }

    public void pushMsg(String messageId,Message message) {
        ArrayList<SocketConnection> list = socketChannelHub.getSocketConnections();

        Log.i(TAG, "Send list:" + list);
        PushMessage.MessageWrapper messageWrapper = PushMessage.MessageWrapper.newBuilder().setBody(Any.pack(message)).setMessageId(messageId).build();
        ProtoParam param = ProtoParam.create(messageWrapper);

        for(SocketConnection connection: list) {
            connection.push(param, new ResultCallback() {
                @Override
                public void onSuccess() {
                    Log.i(TAG, "Push msg by lan successful.");
                }

                @Override
                public void onFail(int code, String message) {
                    Log.e(TAG, "Push msg by lan fail. errCode:" + code + " errMsg:" + message);
                }
            });
        }
    }
}
