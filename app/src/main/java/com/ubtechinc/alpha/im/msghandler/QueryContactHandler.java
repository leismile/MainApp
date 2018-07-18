package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmQueryContactList;
import com.ubtechinc.contact.Contact;
import com.ubtechinc.nets.http.ThrowableWrapper;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ICallback;

import java.util.List;

import static com.ubtechinc.alpha.im.RequestParseUtils.getRequestClass;

/**
 * @desc : 查询通讯录Handler
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/1/30
 */

public class QueryContactHandler implements IMsgHandler{

    private static final String TAG = "QueryContactHandler";

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        Log.d(TAG, "handleMsg : requestCmdId : " + requestCmdId);
        long requestSerial = request.getHeader().getSendSerial();
        CmQueryContactList.CmQueryContactListRequest cmQueryContactListRequest = getRequestClass(request, CmQueryContactList.CmQueryContactListRequest.class);
        int position = cmQueryContactListRequest.getCurrentPage() - 1;
        long versionCode = cmQueryContactListRequest.getVersionCode();
        Log.d(TAG, " versionCode : " + versionCode);
        List<CmQueryContactList.CmContactInfo> cmContactInfoList = Contact.getContactFunc().queryContactList(position, versionCode);
        long newVersionCode = Contact.getContactFunc().getVersionCode();
        Log.d(TAG, " newVersionCode : " + newVersionCode);
        CmQueryContactList.CmQueryContactListResponse response = CmQueryContactList.CmQueryContactListResponse.newBuilder().addAllContactList(cmContactInfoList).setTotalPage(Contact.getContactFunc().getTotalPage(cmQueryContactListRequest.getVersionCode())).setVersionCode(newVersionCode).build();
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
