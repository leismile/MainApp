package com.ubtechinc.alpha.im.msghandler;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtechinc.alpha.im.IMCmdId;
import com.ubtechinc.alpha.service.SkillHelper;
import com.ubtechinc.nets.im.modules.IMJsonMsg;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.interactor.MasterInteractor;
import com.ubtrobot.master.skill.SkillsProxy;
import com.ubtrobot.master.transport.message.parcel.ParcelableParam;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Param;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by bob.xu on 2018/1/17.
 */

public class PushFaceCollectionHandler  implements IMJsonMsgHandler{
    public static final String TAG = "PushFaceCollectionHandler";
    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, IMJsonMsg jsonRequest, String peer) {
        String bodyData = jsonRequest.bodyData;
        LogUtils.d(TAG, "handle msg" + bodyData);

        try {
            JSONObject jsonObject = new JSONObject(bodyData);
            String name = jsonObject.getString("name");
            if (TextUtils.isEmpty(name)) {
                LogUtils.e(TAG,"name is empty");
                return;
            }
            String packageName = com.ubtech.utilcode.utils.Utils.getContext().getPackageName();
            MasterInteractor interactor = Master.get().getOrCreateInteractor("robot:" + packageName);
            SkillsProxy aSkillsProxy = interactor.createSkillsProxy();
            Bundle bundle = new Bundle();
            bundle.putString("name", name);
            Param param = ParcelableParam.create(bundle);
            aSkillsProxy.call("/face/register", param, new ResponseCallback() {
                @Override
                public void onResponse(Request request, Response response) {
                    LogUtils.d(TAG, "/face/register success ！！！");
                }

                @Override
                public void onFailure(Request request, CallException e) {
                    LogUtils.d(TAG, "/face/register fail ！！！");
                    SkillHelper.fordReasonHandler(e);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
