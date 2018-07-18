package com.ubtechinc.alpha.im.msghandler;

import android.support.annotation.NonNull;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmFaceCheckState;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.im.IMCmdId;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.sauron.api.FaceApi;
import com.ubtechinc.sauron.api.SauronApi;
import com.ubtrobot.commons.ResponseListener;

/**
 * @author：wushiyi
 * @date：2017/12/13 16:24
 * [A brief description]
 * version
 */

public class FaceCheckStateHandler implements IMsgHandler {

    private long requestSerial;
    private String peer;
    private int responseCmdId;

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        requestSerial = request.getHeader().getSendSerial();
        this.peer = peer;
        this.responseCmdId = responseCmdId;
        FaceApi.get().checkFaceInsertState(new ResponseListener<Void>() {
            @Override
            public void onResponseSuccess(Void aVoid) {
                checkSuccess();
            }

            @Override
            public void onFailure(int i, @NonNull String s) {
                checkError(i,s);
            }
        });
    }

    private void checkSuccess() {
        CmFaceCheckState.CmFaceCheckStateResponse.Builder builder = CmFaceCheckState.CmFaceCheckStateResponse.newBuilder();
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, requestSerial, builder.build(), peer, null);
    }

    private void checkError(int code,String s) {
        CmFaceCheckState.CmFaceCheckStateResponse.Builder builder = CmFaceCheckState.CmFaceCheckStateResponse.newBuilder();
        builder.setResultCode(code);
        builder.setErrorMsg(s);
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, requestSerial, builder.build(), peer, null);

    }
}
