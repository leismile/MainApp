package com.ubtechinc.alpha.im.msghandler;

import com.google.protobuf.ByteString;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmFaceDetect;
import com.ubtechinc.alpha.CmFaceUpdate;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.im.IMCmdId;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ProtoBufferDispose;
import com.ubtechinc.sauron.api.SauronApi;

/**
 * @author：wushiyi
 * @date：2017/12/13 16:24
 * [A brief description]
 * version
 */

public class FaceUpdateHandler implements IMsgHandler {

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
        CmFaceUpdate.CmFaceUpdateRequest request1 = (CmFaceUpdate.CmFaceUpdateRequest) ProtoBufferDispose.unPackData(CmFaceUpdate.CmFaceUpdateRequest.class, bodyBytes);
        String id = request1.getId();
        String name = request1.getName();
        SauronApi.get(AlphaApplication.getContext()).update(id,name, new SauronApi.OperateResult() {
            @Override
            public void onSuccess() {
                updateSuccess();
            }

            @Override
            public void onError(int i) {
                updateError(i);
            }
        });
    }

    private void updateSuccess() {
        CmFaceUpdate.CmFaceUpdateResponse.Builder builder = CmFaceUpdate.CmFaceUpdateResponse.newBuilder();
        builder.setResultCode(0);
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, requestSerial, builder.build(), peer, null);
    }

    private void updateError(int code) {
        CmFaceUpdate.CmFaceUpdateResponse.Builder builder = CmFaceUpdate.CmFaceUpdateResponse.newBuilder();
        builder.setResultCode(code);
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, requestSerial, builder.build(), peer, null);

    }
}
