package com.ubtechinc.alpha.im.msghandler;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmSetErrorLogData;
import com.ubtechinc.alpha.upload.log.LogUploadByCommand;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;


/**
 * Created by Administrator on 2017/6/5 0005.
 */

public class SetErrorLogDataMsgHandler implements IMsgHandler {
    private static final String TAG = "SetErrorLogDataMsgHandler";

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        final long requestSerial = request.getHeader().getSendSerial();

        //需要在闲聊中添加开关
        int minute = LogUploadByCommand.getInstance().start();

        CmSetErrorLogData.CmSetErrorLogDataResponse.Builder responseBodyBuilder= CmSetErrorLogData.CmSetErrorLogDataResponse.newBuilder();
        responseBodyBuilder.setMinute2Wait(minute);
        responseBodyBuilder.setIsSuccess(true);
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId,"1",requestSerial, responseBodyBuilder.build(), peer, null);

    }
}
