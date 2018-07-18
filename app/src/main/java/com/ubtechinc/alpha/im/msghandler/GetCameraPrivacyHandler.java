package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmCameraPrivacy;
import com.ubtechinc.alpha.utils.SystemPropertiesUtils;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;

/**
 * Created by lulin.wu on 2018/7/13.
 */

public class GetCameraPrivacyHandler implements IMsgHandler {
    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        long requestSerial = request.getHeader().getSendSerial();
        CmCameraPrivacy.CmCameraPrivacyResponse.Builder getCameraPrivacyResponse = CmCameraPrivacy.CmCameraPrivacyResponse.newBuilder();
        Log.i("GetCameraPrivacyHandler","type=====" + SystemPropertiesUtils.getCameraPrivacyType());
        getCameraPrivacyResponse.setType(SystemPropertiesUtils.getCameraPrivacyType());
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId,"1",requestSerial, getCameraPrivacyResponse.build() ,peer,null);
    }
}
