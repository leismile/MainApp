package com.ubtechinc.alpha.im.msghandler;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmFaceUpdate;
import com.ubtechinc.alpha.GetMultiConvState;
import com.ubtechinc.alpha.SetMultiConvState;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.im.IMCmdId;
import com.ubtechinc.nets.im.modules.IMJsonMsg;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ProtoBufferDispose;
import com.ubtrobot.speech.SpeechApiExtra;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/6/6.
 */

public class SetMultiConversationHandler implements IMsgHandler {

    private String TAG = "AccountApplyMsgHandler";


    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        ByteString requestBody = request.getBodyData();
        byte[] bodyBytes = requestBody.toByteArray();
        long requestSerial = request.getHeader().getSendSerial();
        SetMultiConvState.SetMultiConvStateRequest setMultiConvStateRequest = (SetMultiConvState.SetMultiConvStateRequest) ProtoBufferDispose.unPackData(SetMultiConvState.SetMultiConvStateRequest.class, bodyBytes);
        boolean state = setMultiConvStateRequest.getState();
        SpeechApiExtra.get().setTVSConfig("multiConversation",state);
        SetMultiConvState.SetMultiConvStateResponse stateResponse = SetMultiConvState.SetMultiConvStateResponse.newBuilder().setResult(true).build();
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId,"1",requestSerial, stateResponse ,peer,null);
    }

}
