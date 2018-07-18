package com.ubtechinc.alpha.im.msghandler;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmFaceUpdate;
import com.ubtechinc.alpha.GetMultiConvState;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.im.IMCmdId;
import com.ubtechinc.nets.im.modules.IMJsonMsg;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ProtoBufferDispose;
import com.ubtrobot.speech.SpeechApiExtra;
import com.ubtrobot.speech.listener.ResultCallback;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/6/6.
 */

public class GetMultiConversationHandler implements IMsgHandler {

    private String TAG = "AccountApplyMsgHandler";


    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {

        long requestSerial = request.getHeader().getSendSerial();

        SpeechApiExtra.get().getTVSConfig("multiConversation", new ResultCallback() {
            @Override
            public void onResult(boolean state) {
                GetMultiConvState.GetMultiConvStateResponse stateResponse = GetMultiConvState.GetMultiConvStateResponse.newBuilder().setState(state).build();
                RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId,"1",requestSerial, stateResponse ,peer,null);
            }

            @Override
            public void onError(int errorCode) {

            }
        });
    }
}
