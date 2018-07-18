package com.ubtechinc.alpha.im.msghandler;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmFaceExit;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.im.IMCmdId;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.sauron.api.SauronApi;

/**
 * @author：wushiyi
 * @date：2017/12/13 16:24
 * [A brief description]
 * version
 */

public class FaceRegisterExitHandler implements IMsgHandler {

    private long requestSerial;
    private String peer;
    private int responseCmdId;

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        requestSerial = request.getHeader().getSendSerial();
        this.peer = peer;
        this.responseCmdId = responseCmdId;
        SauronApi.get(AlphaApplication.getContext()).stopRegister(peer,new SauronApi.OperateResult() {
            @Override
            public void onSuccess() {
                exitSuccess();
            }

            @Override
            public void onError(int i) {
                exitError(i);
            }
        });
    }

    private void exitSuccess() {
        CmFaceExit.CmFaceExitResponse.Builder builder = CmFaceExit.CmFaceExitResponse.newBuilder();
        builder.setResultCode(0);
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, requestSerial, builder.build(), peer, null);
    }

    private void exitError(int code) {
        CmFaceExit.CmFaceExitResponse.Builder builder = CmFaceExit.CmFaceExitResponse.newBuilder();
        builder.setResultCode(code);
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, requestSerial, builder.build(), peer, null);

    }
}
