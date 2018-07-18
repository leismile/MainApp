package com.ubtechinc.contact.phone;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import com.ubtechinc.contact.Contact;
import com.ubtechinc.contact.PhoneEventNotify;
import com.ubtechinc.contact.R;
import com.ubtechinc.contact.model.UserContact;
import com.ubtechinc.contact.util.Constant;
import com.ubtechinc.contact.util.TTSPlayUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * @desc : 默认电话实现
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/2/1
 */

public class DefaultPhone implements IPhone{
    @SuppressLint("MissingPermission")
    @Override
    public void call(final String phoneNumber) {
        TTSPlayUtil.playTTS(Contact.getInstance().getContext().getResources().getString(R.string.call_tip, phoneNumber), Constant.LOCAL_TTSNAME_CALL, new TTSPlayUtil.IPlayListener() {
            @Override
            public void onError() {
                call();
            }

            @Override
            public void onFinish() {
                call();
            }

            private void call() {
                startDialingProcess(phoneNumber);
            }
        });
        //HandlerUtils.runUITask(new Runnable() {
        //    @Override public void run() {
        //        startDialingProcess(phoneNumber);
        //    }
        //}, 500);
    }

    @Override
    public void call(final String name, final String phoneNumber) {
        TTSPlayUtil.playTTS(Contact.getInstance().getContext().getResources().getString(R.string.call_tip, name), Constant.LOCAL_TTSNAME_CALL, new TTSPlayUtil.IPlayListener() {
            @Override
            public void onError() {
                call();
            }

            @Override
            public void onFinish() {
                call();
            }

            private void call() {
                startDialingProcess(phoneNumber);
            }
        });
        //HandlerUtils.runUITask(new Runnable() {
        //    @Override public void run() {
        //       startDialingProcess(phoneNumber);
        //    }
        //}, 500);
    }

    @SuppressLint("MissingPermission")
    private void startDialingProcess(String phoneNumber) {
        Intent intent=new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+phoneNumber));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        UserContact userContact = new UserContact(Contact.getContactFunc().containsPhoneNumber(phoneNumber), phoneNumber);
        List<UserContact> userContactList = new ArrayList<>();
        userContactList.add(userContact);
        PhoneEventNotify.getInstance().notifyWillStart(userContactList);
        Contact.getInstance().getContext().startActivity(intent);
    }
}
