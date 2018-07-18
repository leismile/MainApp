package com.ubtechinc.alpha.im.msghandler;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtechinc.alpha.service.SkillHelper;
import com.ubtechinc.nets.im.modules.IMJsonMsg;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.interactor.MasterInteractor;
import com.ubtrobot.master.skill.SkillsProxy;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;

/**
 * Created by lulin.wu on 2018/3/16.
 */

public class PushStartObjectRecognitionHandler implements IMJsonMsgHandler {
    public static final String TAG = PushStartObjectRecognitionHandler.class.getSimpleName();
    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, IMJsonMsg jsonRequest, String peer) {
        String bodyData = jsonRequest.bodyData;
        LogUtils.d(TAG, "handle msg" + bodyData);

        String packageName = com.ubtech.utilcode.utils.Utils.getContext().getPackageName();
        MasterInteractor interactor = Master.get().getOrCreateInteractor("robot:" + packageName);
        SkillsProxy aSkillsProxy = interactor.createSkillsProxy();

        aSkillsProxy.call("/object/what_is_this",  new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {
                LogUtils.d(TAG, "/object/what_is_this success ！！！");
            }

            @Override
            public void onFailure(Request request, CallException e) {
                LogUtils.d(TAG, "/object/what_is_this fail ！！！");
                SkillHelper.fordReasonHandler(e);
            }
        });
    }
}
