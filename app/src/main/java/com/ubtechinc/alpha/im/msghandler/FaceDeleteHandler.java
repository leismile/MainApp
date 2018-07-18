package com.ubtechinc.alpha.im.msghandler;

import com.google.protobuf.ByteString;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmFaceDelete;
import com.ubtechinc.alpha.CmFaceUpdate;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.im.IMCmdId;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ProtoBufferDispose;
import com.ubtechinc.sauron.api.SauronApi;

import java.util.ArrayList;
import java.util.List;

/**
 * @author：wushiyi
 * @date：2017/12/13 16:24
 * [A brief description]
 * version
 */

public class FaceDeleteHandler implements IMsgHandler {

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
        CmFaceDelete.CmFaceDeleteRequest request1 = (CmFaceDelete.CmFaceDeleteRequest) ProtoBufferDispose.unPackData(CmFaceDelete.CmFaceDeleteRequest.class, bodyBytes);
        List<String> ids = request1.getIdsList();
        ArrayList<String> idArrays = new ArrayList<>(ids.size());
        for (String id : ids) {
            idArrays.add(id);
        }
        SauronApi.get(AlphaApplication.getContext()).delete(idArrays, new SauronApi.OperateResult() {
            @Override
            public void onSuccess() {
                deleteSuccess();
            }

            @Override
            public void onError(int i) {
                deleteError(i);
            }
        });
    }

    private void deleteSuccess() {
        CmFaceUpdate.CmFaceUpdateResponse.Builder builder = CmFaceUpdate.CmFaceUpdateResponse.newBuilder();
        builder.setResultCode(0);
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, requestSerial, builder.build(), peer, null);
    }

    private void deleteError(int code) {
        CmFaceUpdate.CmFaceUpdateResponse.Builder builder = CmFaceUpdate.CmFaceUpdateResponse.newBuilder();
        builder.setResultCode(code);
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, requestSerial, builder.build(), peer, null);

    }
}
