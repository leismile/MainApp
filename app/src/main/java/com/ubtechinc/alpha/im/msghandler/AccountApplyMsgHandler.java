package com.ubtechinc.alpha.im.msghandler;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.im.IMCmdId;
import com.ubtechinc.alpha.im.msghandler.IMJsonMsgHandler;

import com.ubtechinc.nets.im.modules.IMJsonMsg;
import com.ubtechinc.nets.utils.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/6/6.
 */

public class AccountApplyMsgHandler implements IMJsonMsgHandler {

    private String TAG = "AccountApplyMsgHandler";

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, IMJsonMsg jsonRequest, String peer) {
        String bodyData = jsonRequest.bodyData;
        Log.d(TAG, "handle msg" + bodyData);

        if(requestCmdId == IMCmdId.IM_ACCOUNT_MASTER_UNBINDED_RESPONSE) {
            try {
                JSONObject jsonObject = new JSONObject(bodyData);
                String noticeTitle = jsonObject.getString("noticeTitle");
                if (!TextUtils.isEmpty(noticeTitle) && noticeTitle.contains("将所有从账号解绑")) {
                    unbindTVSToken();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
    public void unbindTVSToken() {
        //TODO:临时改成广播方案， 后续等接入了终端通信模块再改过来
        Intent intent = new Intent("tvs.unbind.accesstoken");
        AlphaApplication.getContext().sendBroadcast(intent);
    }
}
