package com.ubtechinc.bluetoothrobot;

import android.support.annotation.NonNull;

import com.google.protobuf.GeneratedMessageV3;
import com.ubtech.utilcode.utils.LogUtils;
import com.ubtechinc.bluetoothrobot.communite.ImRobotMsgDispatcher;
import com.ubtechinc.bluetoothrobot.communite.ReceiveMessageBussinesss;
import com.ubtechinc.bluetoothrobot.communite.SendMessageBusiness;
import com.ubtechinc.bluetoothrobot.old.OldMsgDispather;
import com.ubtechinc.protocollibrary.communite.ICallback;
import com.ubtechinc.protocollibrary.communite.RobotPhoneCommuniteProxy;


/**
* @Description 通信类
* @Author tanghongyu
* @Time  2018/3/15 9:38
*/
public class Robot2PhoneMsgMgr<T> {
    private static Robot2PhoneMsgMgr sInstance;
    private static String TAG = "Robot2PhoneMsgMgr";
    private RobotPhoneCommuniteProxy mConnection;
    public static Robot2PhoneMsgMgr get() {
        if (sInstance == null) {
            synchronized (Robot2PhoneMsgMgr.class) {
                if (sInstance == null) {
                    sInstance = new Robot2PhoneMsgMgr();
                }
            }
        }
        return sInstance;
    }
    /**
    * @Description 初始化通信代理类和消息分发处理类
    * @Param
    */
    public synchronized void init() {
        LogUtils.i("init");
        mConnection = RobotPhoneCommuniteProxy.getInstance();
        mConnection.init(new SendMessageBusiness(), new ReceiveMessageBussinesss());
        mConnection.setIMsgDispatcher(new ImRobotMsgDispatcher());
        mConnection.setOldMsgDispatcher(new OldMsgDispather());
    }

    /**
     * @Description 发送消息
     * @param cmdId 消息ID
     * @param data 消息内容
     * @param dataCallback 消息回调
     */
    public void sendData(short cmdId, byte version, @NonNull GeneratedMessageV3 data, String peer, @NonNull ICallback<T> dataCallback) {
        RobotPhoneCommuniteProxy.getInstance().sendMessage2Robot(cmdId, version,data, peer, dataCallback);
    }

    /**
     * @Description 发送回复的消息
     * @param cmdId
     * @param version
     * @param responseSerial
     * @param data
     * @param peer
     */
    public void sendResponseData(short cmdId, byte version, int responseSerial,  GeneratedMessageV3 data, Object peer) {
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(cmdId, version, responseSerial, data, peer, null);
    }

    public void sendResponseDataOld(byte[] data, Object peer) {
        RobotPhoneCommuniteProxy.getInstance().sendResponseDataOld( data, peer);
    }


}
