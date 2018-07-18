package com.ubtechinc.bluetoothrobot.communite;


import com.ubtech.utilcode.utils.LogUtils;
import com.ubtechinc.bluetoothlibrary.UbtBluetoothConnManager;
import com.ubtechinc.protocollibrary.communite.IMsgHandleEngine;
import com.ubtechinc.protocollibrary.communite.IReceiveMsg;
import com.ubtechinc.protocollibrary.communite.ImMsgDispathcer;
import com.ubtechinc.protocollibrary.communite.MsgHandleTask;
import com.ubtechinc.protocollibrary.communite.old.IOldMsgDispather;
import com.ubtechinc.protocollibrary.communite.old.OldMsgHandleEngine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2017/5/25.
 */

public class ReceiveMessageBussinesss implements IReceiveMsg {

    private ExecutorService receiveMsgThreadPool;

    private static String TAG = "ReceiveMessageBussinesss";

    public ReceiveMessageBussinesss() {
        init();
    }


    @Override
    public void init() {

        UbtBluetoothConnManager.getInstance().setDataCallback(onRevieveMessageListenenr);
        receiveMsgThreadPool = Executors.newCachedThreadPool();
    }

    @Override
    public void setIMsgDispatcher(ImMsgDispathcer msgDispathcer){
        IMsgHandleEngine.getInstance().setIMsgDispatcher(msgDispathcer);
    }

    @Override
    public void setOldMsgDispatcher(IOldMsgDispather oldMsgDispatcher) {
        OldMsgHandleEngine.Companion.getInstance().setOldMsgDispatcher(oldMsgDispatcher);
    }

    private UbtBluetoothConnManager.IBluetoothDataCallback onRevieveMessageListenenr = new UbtBluetoothConnManager.IBluetoothDataCallback() {
        @Override
        public void onReceiveData(byte[] data, Object from) {
            LogUtils.d("onNewMessages---");
            handleReceivedMessage(data, from);
        }

    };

    /**
     * 处理接收到的IM消息
     *客户端与机器人互传的是byte[]格式，可转成Protobuffer
     * byte[] ---> String --->byte[]并不一定是等价的，会存在数据丢失
     * @param data
     */
    private void handleReceivedMessage(byte[] data, Object from) {
        LogUtils.d(TAG,"handleReceivedMessage");
        MsgHandleTask parserTask = new MsgHandleTask(data, from);
        receiveMsgThreadPool.execute(parserTask);
    }
}
