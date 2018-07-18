package com.ubtechinc.protocollibrary.communite;


/**
 * Created by Administrator on 2017/5/25.
 */

public interface ISendMsg {
    void init();
    void sendMsg( int requestSerialId,  Object peer, byte[] data, final RobotPhoneCommuniteProxy.Callback callback);
}
