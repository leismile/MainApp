package com.ubtechinc.alpha.im.msghandler;

import com.ubtech.utilcode.utils.notification.NotificationCenter;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.GetRobotWifiList;
import com.ubtechinc.alpha.event.GetWifiListEvent;
import com.ubtechinc.nets.phonerobotcommunite.ProtoBufferDispose;

/**
 * @author：wululin
 * @date：2017/11/13 10:46
 * @modifier：ubt
 * @modify_date：2017/11/13 10:46
 * [A brief description]
 * 获取机器人WiFi列表
 */

public class GetWifiListHandler implements IMsgHandler {

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {

        byte[] receiveData =  request.getBodyData().toByteArray();
        GetRobotWifiList.GetRobotWifiListRequest connectRobotWifiRequest = (GetRobotWifiList.GetRobotWifiListRequest) ProtoBufferDispose
                .unPackData(GetRobotWifiList.GetRobotWifiListRequest.class, receiveData);

        GetWifiListEvent getWifiListEvent = new GetWifiListEvent();
        getWifiListEvent.peer = peer;
        getWifiListEvent.requestSerial = request.getHeader().getSendSerial();
        getWifiListEvent.responseCmdID = responseCmdId;
        getWifiListEvent.status = connectRobotWifiRequest.getStatus();
        NotificationCenter.defaultCenter().publish(getWifiListEvent);
    }

}
