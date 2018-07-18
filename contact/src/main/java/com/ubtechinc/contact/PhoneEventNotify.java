package com.ubtechinc.contact;

import android.util.Log;

import com.ubtechinc.contact.model.UserContact;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.service.ServiceProxy;
import com.ubtrobot.phone.PhoneCall;
import com.ubtrobot.transport.message.CallException;

import java.util.ArrayList;
import java.util.List;

/**
 * @desc : 电话事件通知
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/3/6
 */

public class PhoneEventNotify {
    private static final String TAG = "PhoneEventNotify";
    private static volatile PhoneEventNotify instance;
    final ServiceProxy speechContact;

    private PhoneEventNotify() {
        this.speechContact = Master.get().getGlobalContext().createSystemServiceProxy("speech_contact");
    }

    public static PhoneEventNotify getInstance() {
        if(instance == null) {
            synchronized (PhoneEventNotify.class) {
                if(instance == null) {
                    instance = new PhoneEventNotify();
                }
            }
        }
        return instance;
    }

    public void notifyWillStart(List<UserContact> userContactList) {
        Log.d(TAG, " notifyWillStart ");
        List<PhoneCall.Contact> contactList = transfer(userContactList);
        try {
            speechContact.call("/onCallWillStart", ProtoParam.create(PhoneCall.CallWillStart.newBuilder().addAllContactList(contactList).build()));
        } catch (CallException e) {
            e.printStackTrace();
        }
    }

    private List<PhoneCall.Contact> transfer(List<UserContact> userContactList) {
        List<PhoneCall.Contact> contactList = new ArrayList<>();
        for(UserContact userContact : userContactList) {
            PhoneCall.Contact contact = PhoneCall.Contact.newBuilder().setPhoneNumber(userContact.getPhoneNumber() == null ? "" : userContact.getPhoneNumber()).setName(userContact.getName() == null ? "" : userContact.getName()).build();
            contactList.add(contact);
        }
        return contactList;
    }

    public void notifyCallFinished(long time) {
        Log.d(TAG, " notifyCallFinished ");
        try {
            speechContact.call("/onCallFinished", ProtoParam.create(PhoneCall.CallFinished.newBuilder().setTimeInMilliSeconds(time).build()));
        } catch (CallException e) {
            e.printStackTrace();
        }

    }

    public void notifyCallComingUp(List<UserContact> userContactList) {
        Log.d(TAG, " notifyCallComingUp ");
        List<PhoneCall.Contact> contactList = transfer(userContactList);
        try {
            speechContact.call("/onCallComingUp", ProtoParam.create(PhoneCall.CallComingUp.newBuilder().addAllContactList(contactList).build()));
        } catch (CallException e) {
            e.printStackTrace();
        }
    }
}
