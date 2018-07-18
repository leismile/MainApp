package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;

import com.google.protobuf.Any;
import com.google.protobuf.StringValue;
import com.ubt.alpha2.download.util.LogUtils;
import com.ubtechinc.alpha.network.module.CheckBindRobotModule;
import com.ubtechinc.alpha.robotinfo.RobotState;
import com.ubtechinc.alpha.service.SkillHelper;
import com.ubtechinc.nets.ResponseListener;
import com.ubtechinc.nets.http.HttpProxy;
import com.ubtechinc.nets.http.ThrowableWrapper;
import com.ubtechinc.nets.im.modules.IMJsonMsg;


/**
*@data 创建时间：2018/4/25
*@author：bob.xu
*@Description:帮助指引中不需要做查询或业务逻辑的都放在此处实现
*@version
*/
public class GuideWhoAdminHandler implements IMJsonMsgHandler {

    private static final String TAG = "GuideWhoAdminHandler";

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, IMJsonMsg jsonRequest, String peer) {
        final CheckBindRobotModule.Request request = new CheckBindRobotModule.Request(RobotState.get().getSid());
        HttpProxy.get().doGet(request, new ResponseListener<CheckBindRobotModule.Response>() {
            @Override
            public void onError(ThrowableWrapper e) {
                LogUtils.d(TAG, String.format("onError -- e : %s", Log.getStackTraceString(e)));
            }

            @Override
            public void onSuccess(CheckBindRobotModule.Response response) {
                if(response != null) {
                    for(CheckBindRobotModule.User user : response.getData().getResult()) {
                        // 等于0表示主账号
                        if(user.getUpUser() == 0) {
                            StringValue stringValue = StringValue.newBuilder().setValue(user.getNickName()).build();
                            SkillHelper.startSkillByIntent("查询管理员", Any.pack(stringValue));
                        }
                    }
                }
                LogUtils.d(TAG, String.format("onSuccess -- response :  %s", response));
            }
        });
    }
}
