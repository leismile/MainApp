package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmCheckSimCard;
import com.ubtechinc.contact.Contact;
import com.ubtechinc.nets.http.ThrowableWrapper;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ICallback;

/**
 * @desc : 查询Sim卡
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/2/3
 */

public class CheckSimCardHandler implements IMsgHandler{

    private static final String TAG = "CheckSimCardHandler";

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        long requestSerial = request.getHeader().getSendSerial();
        String phoneNumber = Contact.getContactFunc().getSimNumber();
        CmCheckSimCard.CmCheckSimCardResponse cmCheckSimCardResponse = CmCheckSimCard.CmCheckSimCardResponse.newBuilder().setIsExist(Contact.getContactFunc().simExist()).setPhoneNumber(phoneNumber == null ? "未知号码" : phoneNumber).build();
        Log.d(TAG, " cmCheckSimCardResponse : " + cmCheckSimCardResponse.getIsExist());
        Log.d(TAG, " cmCheckSimCardResponse : " + cmCheckSimCardResponse.getPhoneNumber());
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId,"1",requestSerial, cmCheckSimCardResponse ,peer, new ICallback(){
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
