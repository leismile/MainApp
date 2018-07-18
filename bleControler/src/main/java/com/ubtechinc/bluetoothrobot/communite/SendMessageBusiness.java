package com.ubtechinc.bluetoothrobot.communite;


import com.ubtechinc.bluetoothlibrary.IDataEngine;
import com.ubtechinc.bluetoothlibrary.UbtBluetoothConnManager;
import com.ubtechinc.protocollibrary.communite.ISendMsg;
import com.ubtechinc.protocollibrary.communite.RobotPhoneCommuniteProxy;
import com.ubtechinc.protocollibrary.protocol.MiniDataEngine;


/**
 * Created by Administrator on 2016/12/17.
 */
public class SendMessageBusiness implements ISendMsg {

    public final String TAG  = "SendRevMsgBusiness";
    private IDataEngine iDataEngine;
    private static SendMessageBusiness sInstance;
    public static SendMessageBusiness getInstance() {
        if (sInstance == null) {
            synchronized (SendMessageBusiness.class) {
                sInstance = new SendMessageBusiness();
            }
        }
        return sInstance;
    }

    public SendMessageBusiness() {
        init();
    }


    public void init() {
        iDataEngine = new MiniDataEngine();
    }

    @Override
    public void sendMsg(int requestSerialId, Object peer, byte[] data, RobotPhoneCommuniteProxy.Callback callback) {
        sendMessageByBle(requestSerialId, peer, data, callback);
    }





    private void sendMessageByBle(final int requestSerialId, Object peer, byte[] data, final RobotPhoneCommuniteProxy.Callback callback) {
        if (data == null) {
            return;
        }
        UbtBluetoothConnManager.getInstance().sendBleData( iDataEngine.spliteData(data), peer);
    }


}
