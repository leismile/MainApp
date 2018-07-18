package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmAddContact;
import com.ubtechinc.alpha.CmQueryContactList;
import com.ubtechinc.contact.Contact;
import com.ubtechinc.contact.ResultConstans;
import com.ubtechinc.nets.http.ThrowableWrapper;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ICallback;

import static com.ubtechinc.alpha.im.RequestParseUtils.getRequestClass;

/**
 * @desc : 增加联系人Handler
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/2/3
 */

public class AddContactHandler implements IMsgHandler{

    private static final String TAG = "AddContactHandler";

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        long requestSerial = request.getHeader().getSendSerial();
        CmAddContact.CmAddContactRequest cmAddContactRequest = getRequestClass(request, CmAddContact.CmAddContactRequest.class);
        CmQueryContactList.CmContactInfo cmContactInfo = CmQueryContactList.CmContactInfo.newBuilder().setPhone(cmAddContactRequest.getPhone()).setName(cmAddContactRequest.getName()).build();
        long contactId = Contact.getContactFunc().addContact(cmContactInfo, cmAddContactRequest.getUserId());
        CmAddContact.CmAddContactResponse cmAddContactResponse = CmAddContact.CmAddContactResponse.newBuilder().setContactId(contactId).setIsSuccess(contactId > 0).setResultCode(contactId > 0 ? ResultConstans.RESULT_SUCCESS : (int) -contactId).build();
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId,"1",requestSerial, cmAddContactResponse ,peer, new ICallback(){

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
