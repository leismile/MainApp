package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmQueryCallRecord;
import com.ubtechinc.contact.Contact;
import com.ubtechinc.nets.http.ThrowableWrapper;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ICallback;

import java.util.List;

import static com.ubtechinc.alpha.im.RequestParseUtils.getRequestClass;

/**
 * @desc : 通话记录查询
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/1/30
 */

public class CallRecordHandler implements IMsgHandler{

    private static final String TAG = "CallRecordHandler";

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        long requestSerial = request.getHeader().getSendSerial();
        try {
            CmQueryCallRecord.CmQueryCallRecordRequest cmQueryCallRecordRequest = getRequestClass(request, CmQueryCallRecord.CmQueryCallRecordRequest.class);
            int newPostion = cmQueryCallRecordRequest.getCurrentPage() - 1;
            int position = newPostion < 0 ? 0 : newPostion;
            int versionCode = (int)cmQueryCallRecordRequest.getVersionCode();
            List<CmQueryCallRecord.CmCallRecordInfo> cmCallRecordInfoList = Contact.getContactFunc().queryCallRecord(position, versionCode);
            CmQueryCallRecord.CmQueryCallRecordResponse recordResponse = null;
            if (cmCallRecordInfoList != null) {
                Log.d(TAG, " cmCallRecordInfoList : " + cmCallRecordInfoList);
                recordResponse = CmQueryCallRecord.CmQueryCallRecordResponse.newBuilder().setTotalPage(Contact.getContactFunc().getCallRecordSize(versionCode)).setVersionCode(Contact.getContactFunc().getCallRecordVersionCode()).addAllContactList(cmCallRecordInfoList).build();
            } else {
                recordResponse = CmQueryCallRecord.CmQueryCallRecordResponse.newBuilder().setTotalPage(Contact.getContactFunc().getCallRecordSize(versionCode)).setVersionCode(Contact.getContactFunc().getCallRecordVersionCode()).build();

            }
            RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId,"1",requestSerial, recordResponse ,peer, new ICallback(){

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
            Log.d(TAG, Log.getStackTraceString(e));
        }
    }
}
