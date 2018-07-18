package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmModifyMobileDataStatus;
import com.ubtechinc.contact.Contact;
import com.ubtechinc.nets.http.ThrowableWrapper;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ICallback;

import static com.ubtechinc.alpha.im.RequestParseUtils.getRequestClass;

/**
 * @desc : 改变移动网络Handler
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/2/3
 */

public class ModifyMobileDataHandler implements IMsgHandler{

    private static final String TAG = "ModifyMobileDataHandler";
    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        long requestSerial = request.getHeader().getSendSerial();
        CmModifyMobileDataStatus.CmModifyMobileDataStatusRequest modifyMobileDataStatusRequest = getRequestClass(request, CmModifyMobileDataStatus.CmModifyMobileDataStatusRequest.class);
        boolean result = Contact.getContactFunc().modifyDataStatus(modifyMobileDataStatusRequest.getIsOpen());
        CmModifyMobileDataStatus.CmModifyMobileDataStatusResponse cmQueryMobileNetworkResponse = CmModifyMobileDataStatus.CmModifyMobileDataStatusResponse.newBuilder().setIsSuccess(result).setResultCode(result ? 0 : 1).build();
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
