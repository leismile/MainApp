package com.ubtechinc.alpha.im.msghandler;

import android.text.TextUtils;

import com.google.protobuf.ByteString;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmFaceList;
import com.ubtechinc.alpha.CmFaceUpdate;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.im.IMCmdId;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ProtoBufferDispose;
import com.ubtechinc.sauron.api.FaceInfo;
import com.ubtechinc.sauron.api.SauronApi;

import java.util.ArrayList;

/**
 * @author：wushiyi
 * @date：2017/12/13 16:24
 * [A brief description]
 * version
 */

public class FaceListHandler implements IMsgHandler {

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
        CmFaceList.CmFaceListRequest request1 = (CmFaceList.CmFaceListRequest) ProtoBufferDispose.unPackData(CmFaceList.CmFaceListRequest.class, bodyBytes);
        int page = request1.getPage();
        int pageSize = request1.getPageSize();
        SauronApi.get(AlphaApplication.getContext()).query(page,pageSize, new SauronApi.QueryResult() {

            @Override
            public void onSuccess(ArrayList<FaceInfo> arrayList) {
                querySuccess(arrayList);
            }

            @Override
            public void onError(int i) {
                queryError(i);
            }
        });
    }

    private void querySuccess(ArrayList<FaceInfo> arrayList) {
       CmFaceList.CmFaceListResponse.Builder builder = CmFaceList.CmFaceListResponse.newBuilder();
        for (FaceInfo faceInfo : arrayList) {
            CmFaceList.CMFaceInfo.Builder faceInfobuilder = CmFaceList.CMFaceInfo.newBuilder();
            faceInfobuilder.setId(faceInfo.getId());
            faceInfobuilder.setName(faceInfo.getName());
            if(!TextUtils.isEmpty(faceInfo.getAvtar())){
                faceInfobuilder.setAvatar(faceInfo.getAvtar());
            }
            builder.addFaceList(faceInfobuilder.build());
        }
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, requestSerial, builder.build(), peer, null);
    }

    private void queryError(int code) {
        CmFaceUpdate.CmFaceUpdateResponse.Builder builder = CmFaceUpdate.CmFaceUpdateResponse.newBuilder();
        builder.setResultCode(code);
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, requestSerial, builder.build(), peer, null);

    }
}
