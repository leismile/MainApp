package com.ubtechinc.alpha.im.msghandler;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtechinc.nets.im.modules.IMJsonMsg;
import com.ubtechinc.sauron.api.ObjectDetectApi;
import com.ubtechinc.sauron.api.ObjectType;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.interactor.MasterInteractor;
import com.ubtrobot.master.skill.SkillsProxy;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;

/**
 * Created by bob.xu on 2018/1/17.
 */

public class FlowerRecognitionHandler implements IMJsonMsgHandler{
    public static final String TAG = "PushFaceCollectionHandler";
    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, IMJsonMsg jsonRequest, String peer) {
        String bodyData = jsonRequest.bodyData;
        LogUtils.d(TAG, "handle msg" + bodyData);
        ObjectDetectApi.get().whatIsThis(ObjectType.FLOWER, 10, new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {

            }

            @Override
            public void onFailure(Request request, CallException e) {

            }
        });
    }
}
