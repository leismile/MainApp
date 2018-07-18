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
 * Created by bob.xu on 2018/1/17.
 */

public class PushBookReadStartHandler implements IMJsonMsgHandler{
    public static final String TAG = "PushFaceCollectionHandler";
    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, IMJsonMsg jsonRequest, String peer) {
        String bodyData = jsonRequest.bodyData;
        LogUtils.d(TAG, "handle msg" + bodyData);

        String packageName = com.ubtech.utilcode.utils.Utils.getContext().getPackageName();
        MasterInteractor interactor = Master.get().getOrCreateInteractor("robot:" + packageName);
        SkillsProxy aSkillsProxy = interactor.createSkillsProxy();

        aSkillsProxy.call("/book/recognition",  new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {
                LogUtils.d(TAG, "/book/recognition success ！！！");
            }

            @Override
            public void onFailure(Request request, CallException e) {
                LogUtils.d(TAG, "/book/recognition fail ！！！");
                SkillHelper.fordReasonHandler(e);
            }
        });
    }
}
