package com.ubtechinc.alpha.im.msghandler;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.TVSGetRobotProductId;
import com.ubtechinc.alpha.robotinfo.RobotState;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;

/**
 * Created by bob.xu on 2017/8/9.
 */

public class GetTVSProductIdMsgHandler implements IMsgHandler {
    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {

        long requestSerial = request.getHeader().getSendSerial();
        //send Response
        TVSGetRobotProductId.TVSGetRobotProductIdResponse.Builder builder = TVSGetRobotProductId.TVSGetRobotProductIdResponse.newBuilder();
        builder.setProductId("b0851325-3056-4853-921b-dcba21b491a3:8c901ad100ad44d98b6276adeb861058"); //具体见TVSConfig
        builder.setDsn(RobotState.get().getSid());

        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId,"1",requestSerial, builder.build() ,peer,null);
    }
}
