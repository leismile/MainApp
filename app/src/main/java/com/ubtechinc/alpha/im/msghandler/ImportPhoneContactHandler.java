package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmImportPhoneContact;
import com.ubtechinc.contact.Contact;
import com.ubtechinc.nets.http.ThrowableWrapper;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ICallback;

import static com.ubtechinc.alpha.im.RequestParseUtils.getRequestClass;

/**
 * @desc : 导入通讯录Handler
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/2/3
 */

public class ImportPhoneContactHandler implements IMsgHandler{

    private static final String TAG = "ImportPhoneHandler";

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        long requestSerial = request.getHeader().getSendSerial();
        CmImportPhoneContact.CmImportPhoneContactRequest importPhoneContactRequest = getRequestClass(request, CmImportPhoneContact.CmImportPhoneContactRequest.class);
        int result = Contact.getContactFunc().importContact(importPhoneContactRequest.getContactListList(), importPhoneContactRequest.getUserId());
        CmImportPhoneContact.CmImportPhoneContactResponse response = CmImportPhoneContact.CmImportPhoneContactResponse.newBuilder().setIsSuccess(result == 0).setResultCode(-result).build();
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId,"1",requestSerial, response ,peer, new ICallback(){
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
