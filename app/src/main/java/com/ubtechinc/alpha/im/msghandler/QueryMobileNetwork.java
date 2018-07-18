package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmQueryMobileNetwork;
import com.ubtechinc.contact.Contact;
import com.ubtechinc.nets.http.ThrowableWrapper;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ICallback;

/**
 * @desc : 查询移动网络状态Handler
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/2/3
 */

public class QueryMobileNetwork implements IMsgHandler{

    private static final String TAG = "QueryMobileNetwork";

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        long requestSerial = request.getHeader().getSendSerial();
        CmQueryMobileNetwork.CmQueryMobileNetworkResponse cmQueryMobileNetworkResponse = CmQueryMobileNetwork.CmQueryMobileNetworkResponse.newBuilder().setIsOpenData(Contact.getContactFunc().isOpenData()).setIsOpenRoam(Contact.getContactFunc().isOpenRoam()).build();
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId,"1",requestSerial, cmQueryMobileNetworkResponse ,peer, new ICallback(){

            @Override
            public void onSuccess(Object data) {
                Log.d(TAG, "onStartSuccess");
            }

            @Override
            public void onError(ThrowableWrapper e) {
                Log.d(TAG, "onError -- e: " + Log.getStackTraceString(e));
            }
        });
    }
}
