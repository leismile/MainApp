package com.ubtechinc.contact;

import android.telephony.TelephonyManager;
import android.util.Log;

import com.ubtechinc.contact.event.AnswerEvent;
import com.ubtechinc.contact.event.HangupEvent;
import com.ubtechinc.contact.event.PlayRingEvent;
import com.ubtechinc.contact.event.StartCallEvent;
import com.ubtechinc.contact.model.UserContact;
import com.ubtechinc.contact.phone.DefaultPhone;
import com.ubtechinc.contact.util.Constant;
import com.ubtechinc.contact.util.TTSPlayUtil;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.phone.PhoneCall;
import com.ubtrobot.speech.listener.TTsListener;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * @desc : 调用电话服务
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/2/26
 */

public class CallService extends MasterSystemService {

    private static final String TAG = "CallService";

    @Call(path="/call")
    public void call(Request request, Responder responder) {
        Log.d(TAG, " call ");
        int state = PhoneListenerService.state;
        if(state == TelephonyManager.CALL_STATE_IDLE){
            if(!Contact.getContactFunc().simExist()) {
                TTSPlayUtil.playLocalTTs(Constant.LOCAL_TTSNAME_NOSIM);
                return ;
            }
            try {
                PhoneCall.CallRequest callRequest = ProtoParam.from(request.getParam(), PhoneCall.CallRequest.class).getProtoMessage();
                Log.d(TAG, " call callRequest  : " + callRequest + " number : ");
                List<PhoneCall.Contact> contactList = callRequest.getContactListList();
                List<UserContact> userContactList = new ArrayList<>();
                String callName = "";
                for(PhoneCall.Contact contact : contactList) {
                    callName = contact.getName();
                    String number = contact.getPhoneNumber();
                    if(number != null && !number.isEmpty()) {
                        if(!callName.equals("")) {
                            new DefaultPhone().call(callName, number);
                        } else {
                            new DefaultPhone().call(number);
                        }
                        return ;
                    }
                    Log.d(TAG,"contact.getName() : " + contact.getName() + " contact.getPhoneNumber() : " + contact.getPhoneNumber());
                    userContactList.addAll(Contact.getContactFunc().getPhoneNumber(contact.getName()));
                }
                int count = userContactList.size();
                PhoneCall.ResponseType reponseType = PhoneCall.ResponseType.SUCCESS;
                List<PhoneCall.Contact> contactList1 = new ArrayList<>();
                if(count == 0) {
                    reponseType = PhoneCall.ResponseType.CONTACTNOTFOUND;
                    contactList1.addAll(contactList);
                    TTSPlayUtil.playTTS(Contact.getInstance().getContext().getResources().getString(R.string.phone_not_found, callName), Constant.LOCAL_TTSNAME_NOTFOUND_CONTACT);
                } else if(count == 1) {
                    reponseType = PhoneCall.ResponseType.SUCCESS;
                    contactList1.addAll(transfer(userContactList));
                    Log.d(TAG, " contact : " + userContactList.get(0));
                    new DefaultPhone().call(userContactList.get(0).getName(), userContactList.get(0).getPhoneNumber());
                } else {
                    reponseType = PhoneCall.ResponseType.MULTINUMBERFOUND;
                    contactList1.addAll(transfer(userContactList));
                }
                PhoneCall.CallResponse.Builder builder = PhoneCall.CallResponse.newBuilder().setResponseType(reponseType);
                if(contactList1 != null && !contactList1.isEmpty()) {
                    builder.addAllContactList(contactList1);
                }
                EventBus.getDefault().post(new StartCallEvent());
                responder.respondSuccess(ProtoParam.create(builder.build()));
            } catch (ProtoParam.InvalidProtoParamException e) {
                Log.d(TAG, " InvalidProtoParamException -- e: " + Log.getStackTraceString(e));
                responder.respondFailure(0, "");
            }
        }else { //已经在通话或响铃状态
            VoicePool.get().playTTs("我正在通话中哦", Priority.NORMAL,null);
        }

    }

    private List<PhoneCall.Contact> transfer(List<UserContact> contactList) {
        List<PhoneCall.Contact> contactList1 = new ArrayList<>();
        for(UserContact userContact : contactList) {
            contactList1.add(transfer(userContact));
        }
        return contactList1;
    }

    private PhoneCall.Contact transfer(UserContact userContact) {
        String userName = userContact.getName();
        String phoneNumber = userContact.getPhoneNumber();
        return PhoneCall.Contact.newBuilder().setName(userName == null ? "" : userName).setPhoneNumber(phoneNumber == null ? "" : phoneNumber).build();
    }

    @Call(path="/answer")
    public void answer(Request request, Responder responder) {
        Log.d(TAG, " answer");
        EventBus.getDefault().post(new AnswerEvent());
        responder.respondSuccess();
    }

    @Call(path="/hangup")
    public void hangup(Request request, Responder responder) {
        Log.d(TAG, " hangup");
        EventBus.getDefault().post(new HangupEvent());
        responder.respondSuccess();
    }

    @Call(path="/playring")
    public void playring(Request request, Responder responder) {
        Log.d(TAG, " playring");
        EventBus.getDefault().post(new PlayRingEvent());
        responder.respondSuccess();
    }

    private static class TTsListenerEx implements TTsListener{

        @Override
        public void onTtsBegin() {

        }

        @Override
        public void onTtsVolumeChange(int i) {

        }

        @Override
        public void onTtsCompleted() {

        }

        @Override
        public void onError(int i, String s) {

        }
    }
}
