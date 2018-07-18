package com.ubtechinc.alpha.im.msghandler;

import android.support.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmFaceDetect;
import com.ubtechinc.alpha.im.IMCmdId;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ProtoBufferDispose;
import com.ubtechinc.sauron.api.FaceApi;
import com.ubtrobot.commons.ResponseListener;

/**
 * @author：wushiyi
 * @date：2017/12/13 16:24
 * [A brief description]
 * version
 */

public class FaceRegisterHandler implements IMsgHandler {

    private long requestSerial;
    private String peer;
    private int responseCmdId;

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        requestSerial = request.getHeader().getSendSerial();
        this.peer = peer;
        this.responseCmdId = responseCmdId;
        ByteString requestBody = request.getBodyData();
        byte[] bodyBytes = requestBody.toByteArray();
        CmFaceDetect.CmFaceDetectRequest request1 = (CmFaceDetect.CmFaceDetectRequest) ProtoBufferDispose.unPackData(CmFaceDetect.CmFaceDetectRequest.class, bodyBytes);
        String name = request1.getName();
        FaceApi.get().startRegister(peer, name, new ResponseListener<String>() {
            @Override
            public void onResponseSuccess(String s) {
                insertSuccess(s);
            }

            @Override
            public void onFailure(int i, @NonNull String s) {
                insertError(i, s);
            }
        });
    }

    private void insertSuccess(String id) {
        CmFaceDetect.CmFaceDetectResponse.Builder builder = CmFaceDetect.CmFaceDetectResponse.newBuilder();
        builder.setId(id);
        builder.setResultCode(0);
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, requestSerial, builder.build(), peer, null);
    }

    private void insertError(int code, String msg) {
        CmFaceDetect.CmFaceDetectResponse.Builder builder = CmFaceDetect.CmFaceDetectResponse.newBuilder();
        builder.setResultCode(code).setErrMsg(msg);
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, requestSerial, builder.build(), peer, null);

    }
}
