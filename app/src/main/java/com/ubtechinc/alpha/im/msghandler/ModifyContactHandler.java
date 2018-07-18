package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmModifyContact;
import com.ubtechinc.contact.Contact;
import com.ubtechinc.contact.ResultConstans;
import com.ubtechinc.nets.http.ThrowableWrapper;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ICallback;

import static com.ubtechinc.alpha.im.RequestParseUtils.getRequestClass;

/**
 * @desc : 修改联系人Handler
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/2/3
 */

public class ModifyContactHandler implements IMsgHandler{

    private static final String TAG = "ModifyContactHandler";

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        long requestSerial = request.getHeader().getSendSerial();
        CmModifyContact.CmModifyContactRequest cmModifyContactRequest = getRequestClass(request, CmModifyContact.CmModifyContactRequest.class);
        Log.d(TAG, " cmModifyContactRequest.getContactId() : " + cmModifyContactRequest.getContactId());
        int result = Contact.getContactFunc().modifyContact(cmModifyContactRequest.getContactId(), cmModifyContactRequest.getName(), cmModifyContactRequest.getPhone(), cmModifyContactRequest.getUserId());
        CmModifyContact.CmModifyContactResponse cmModifyContactResponse = CmModifyContact.CmModifyContactResponse.newBuilder().setIsSuccess(result == ResultConstans.RESULT_SUCCESS).build();
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId,"1",requestSerial, cmModifyContactResponse ,peer, new ICallback(){

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
