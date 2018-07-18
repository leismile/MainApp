package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmDeleteContact;
import com.ubtechinc.contact.Contact;
import com.ubtechinc.contact.ResultConstans;
import com.ubtechinc.nets.http.ThrowableWrapper;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ICallback;

import static com.ubtechinc.alpha.im.RequestParseUtils.getRequestClass;

/**
 * @desc : 删除联系人Handler
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/2/3
 */

public class DeleteContactHandler implements IMsgHandler{

    private static final String TAG = "DeleteContactHandler";

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        long requestSerial = request.getHeader().getSendSerial();
        try {
            CmDeleteContact.CmDeleteContactRequest cmDeleteContactRequest = getRequestClass(request, CmDeleteContact.CmDeleteContactRequest.class);
            int result = Contact.getContactFunc().deleteContact(cmDeleteContactRequest.getContactId(), cmDeleteContactRequest.getUserId());
            CmDeleteContact.CmDeleteContactResponse cmDeleteContactResponse = CmDeleteContact.CmDeleteContactResponse.newBuilder().setIsSuccess(result == ResultConstans.RESULT_SUCCESS).setResultCode(-result).build();
            RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, "1", requestSerial, cmDeleteContactResponse, peer, new ICallback() {

                @Override
                public void onSuccess(Object data) {
                    Log.d(TAG, "onStartSuccess");
                }

                @Override
                public void onError(ThrowableWrapper e) {
                    Log.d(TAG, "onError -- e: " + Log.getStackTraceString(e));
                }
            });
        }catch (Exception e) {
            Log.d(TAG, " error : " + Log.getStackTraceString(e));
        }
    }
}
