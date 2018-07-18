package com.ubtechinc.contact;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.ubtechinc.alpha.CmQueryContactList;
import com.ubtechinc.contact.model.CallRecordInfo;
import com.ubtechinc.contact.model.CallRecordModel;
import com.ubtechinc.contact.notice.DefaultNotice;
import com.ubtechinc.contact.phone.DefaultPhone;
import com.ubtechinc.contact.phone.DefaultRing;
import com.ubtechinc.contact.phone.IRing;
import com.ubtechinc.contact.phone.IRingListener;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.interactor.MasterInteractor;
import com.ubtrobot.master.skill.SkillInfo;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;

import java.util.ArrayList;
import java.util.List;

/**
 * @desc : 测试接口
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/1/31
 */

public class TestContactActivity extends Activity{

    private String ACTION_RING = "com.ubtrobot.service.ring";
    private String ACTION_RING_KEY_WAKEUP = "com.ubtrobot.service.ring.wakeup";
    private static final String TAG = "TestContactActivity";
    private IContactFunc contactFunc;
    private IRing iRing;

    private static final String ACTION_DIALER = "com.ubtrobot.dialer.ACTION_CALL";
    private static final String ACTION_DIALER_FUNC_KEY = "funckey";
    private static final int FUNC_ANSWER = 1;
    private static final int FUNC_DECLINE_SECONDE = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contactFunc = DefaultContactFunc.getInstance();
        setContentView(R.layout.activity_test_contact);
        iRing = new DefaultRing(this);
        iRing.setRingListener(new IRingListener() {
            @Override
            public void onRingCompletely() {
                Log.d(TAG, " onRingCompletely ");
            }

            @Override
            public void onEndRingCompletely() {

            }
        });
    }

    public void onImport(View view) {
        List<CmQueryContactList.CmContactInfo> list = new ArrayList<>(2);
        for(int i = 0; i < 200; i ++) {
            CmQueryContactList.CmContactInfo cmContactInfo = CmQueryContactList.CmContactInfo.newBuilder().setContactId(i).setName("名字中" + i).setPhone("13888888888").build();
            list.add(cmContactInfo);
        }
        contactFunc.importContact(list, "");
    }

    public void onAdd(View view) {
        int i = 204;
        CmQueryContactList.CmContactInfo cmContactInfo = CmQueryContactList.CmContactInfo.newBuilder().setContactId(i).setName("李白").setPhone("15875567086").build();
        contactFunc.addContact(cmContactInfo, "");
    }

    public void onModifyContact(View view) {
        contactFunc.modifyContact(1, "名字" + 1000, "13553322113", "");
    }

    public void onDeleteContact(View view) {
        contactFunc.deleteContact(2, "");
    }

    public void onSimExist(View view) {
        boolean result = contactFunc.simExist();
        Log.d(TAG, " onSimExist result : " + result);
        VoicePool.get().playTTs("你好", Priority.NORMAL, new VoiceListener() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(int i, String s) {

            }
        });

    }

    public void onIsOpenData(View view) {
        openData = contactFunc.isOpenData();
        Log.d(TAG, " onIsOpenData result : " + openData);
    }

    public void onIsOpenRoam(View view) {
        openRoam = contactFunc.isOpenRoam();
        Log.d(TAG, " onIsOpenRoam result : " + openRoam);
    }

    private boolean openData;
    public void onModifyDataStatus(View view) {
        contactFunc.modifyDataStatus(!openData);
        Log.d(TAG, " onModifyDataStatus result : " + contactFunc.isOpenData());
    }

    private boolean openRoam;
    public void onModifyRoam(View view) {
        contactFunc.modifyRoam(openRoam = !openRoam);
        Log.d(TAG, " onModifyRoam result : " + contactFunc.isOpenRoam());

    }

    public void onQueryCallRecord(View view) {
        Log.d(TAG, " onQueryCallRecord result : " + contactFunc.queryCallRecord(0, 0));
    }

    public void onMissRequest(View view) {
        Log.d(TAG, " onMissRequest : ");
        new DefaultNotice().notifyMiss("13596865478");
    }

    public void onInterceptRequest(View view) {
        Log.d(TAG, " onInterceptRequest : ");
        new DefaultNotice().notifyIntercept("13596865478");
    }

    public void onQueryContact(View view) {
        int size = contactFunc.getTotalPage(0);
        for(int i = 0; i < size; i ++) {
            contactFunc.queryContactList(i, 0);
        }
    }

    public void onCallPhone(View view) {
        new DefaultPhone().call("10086");
    }

    public void onListenerPhone(View view) {
        Log.d(TAG , "onListenerPhone");
        contactFunc.phoneAnswer();
    }

    public void onEndCall(View view) {
        contactFunc.endCall();
    }

    public void onContactList(View view) {
        startActivity(new Intent(this, ContactActivity.class));
    }

    public void onQueryPhoneNumber(View view) {
        contactFunc.getSimNumber();
    }

    public void onAddCallRecord(View view) {
        CallRecordInfo callRecordInfo = new CallRecordInfo();
        callRecordInfo.setType(1);
        callRecordInfo.setName("hello");
        callRecordInfo.setDuration(23324);
        callRecordInfo.setDateLong(133434);
        new CallRecordModel(this).add(callRecordInfo);
    }

    public void onQuerySkill(View view) {
        MasterInteractor masterInteractor = Master.get().getOrCreateInteractor("robot:" + getPackageName());
        List<SkillInfo> skillInfoList = masterInteractor.getStartedSkills();
        if (skillInfoList != null) {
            for (SkillInfo skillInfo : skillInfoList) {
                if (skillInfo != null) {
                    Log.d(TAG,"skillInfo ===="+skillInfo.toString());
                    String skillName = skillInfo.getName();
                }
            }
        }
    }

    public void onPhoneSwitch(View view) {
        RingService.wakeupState = !RingService.wakeupState;
        ((Button)view).setText(RingService.wakeupState ? "自动唤醒" : "用户唤醒");
    }

    public void onAnswer(View view) {
        new DefaultPhoneAnswer(this).onAnswer();
    }

    public void onStopRing(View view) {
        iRing.stopCommingRing();
    }

    public void onRing(View view) {
        iRing.playCommingRing("路飞", true);
    }

    public void onEndRing(View view) {
        iRing.playEndRing();
    }

    public void onStopEndRing(View view) {
        iRing.stopEndRing();
    }

    public void onWakeup(View view) {
        Intent intent = new Intent(ACTION_RING);
        intent.putExtra(ACTION_RING_KEY_WAKEUP, true);
        sendBroadcast(intent);
    }

    public void onToDecline(View view) {
//        new DefaultPhoneAnswer(this).onDeclineComming();
        try {
            Class dcTracker = Class.forName("com.android.internal.telephony.dataconnection.DcTracker");
            Log.d(TAG, " onToDecline -- ClassNotFoundException dcTracker : " + dcTracker);
        } catch (ClassNotFoundException e) {
            Log.d(TAG, " onToDecline -- ClassNotFoundException : ");
            e.printStackTrace();
        }
    }
}
