package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;

import com.google.protobuf.ByteString;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmCameraPrivacy;
import com.ubtechinc.alpha.appmanager.InfraRedManager;
import com.ubtechinc.alpha.utils.SystemPropertiesUtils;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ProtoBufferDispose;

/**
 * Created by lulin.wu on 2018/7/13.
 */

public class SetCameraPrivacyHandler implements IMsgHandler {
    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        long requestSerial = request.getHeader().getSendSerial();
        ByteString requestBody = request.getBodyData();
        byte[] bodyBytes = requestBody.toByteArray();
        CmCameraPrivacy.CmCameraPrivacyRequest  accessTokenRequest = (CmCameraPrivacy.CmCameraPrivacyRequest) ProtoBufferDispose.unPackData(
                CmCameraPrivacy.CmCameraPrivacyRequest.class, bodyBytes);
        CmCameraPrivacy.CameraPrivacyType type = accessTokenRequest.getType();
        Log.i("SetCameraPrivacyHandler","type========" + type);
        SystemPropertiesUtils.setCameraPrivacyType(type);
        InfraRedManager.get().setAllowStartInfraRad();
        CmCameraPrivacy.CmCameraPrivacyResponse response = CmCameraPrivacy.CmCameraPrivacyResponse.newBuilder().setType(type).build();
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, "1", requestSerial, response, peer, null);
    }
}
