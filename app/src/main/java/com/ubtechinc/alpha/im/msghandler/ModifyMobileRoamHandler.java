package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmModifyMobileRoamStatus;
import com.ubtechinc.contact.Contact;
import com.ubtechinc.nets.http.ThrowableWrapper;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ICallback;

import static com.ubtechinc.alpha.im.RequestParseUtils.getRequestClass;

/**
 * @desc : 改变漫游状态Handler
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/2/3
 */

public class ModifyMobileRoamHandler implements IMsgHandler{
    private static final String TAG = "ModifyMobileRoamHandler";
    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        long requestSerial = request.getHeader().getSendSerial();
        CmModifyMobileRoamStatus.CmModifyMobileRoamRequest cmModifyMobileRoamRequest = getRequestClass(request, CmModifyMobileRoamStatus.CmModifyMobileRoamRequest.class);
        boolean result = Contact.getContactFunc().modifyRoam(cmModifyMobileRoamRequest.getIsOpen());
        CmModifyMobileRoamStatus.CmModifyMobileRoamResponse cmModifyMobileRoamResponse = CmModifyMobileRoamStatus.CmModifyMobileRoamResponse.newBuilder().setIsSuccess(result).setResultCode(result ? 0 : 1).build();
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId,"1",requestSerial, cmModifyMobileRoamResponse ,peer, new ICallback(){

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
