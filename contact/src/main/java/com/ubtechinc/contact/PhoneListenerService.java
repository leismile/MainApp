package com.ubtechinc.contact;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtechinc.contact.event.AnswerEvent;
import com.ubtechinc.contact.event.ConverseEndEvent;
import com.ubtechinc.contact.event.HangupEvent;
import com.ubtechinc.contact.event.PlayRingEvent;
import com.ubtechinc.contact.event.RealPlayRingEvent;
import com.ubtechinc.contact.event.RingEndEvent;
import com.ubtechinc.contact.event.StartCallEvent;
import com.ubtechinc.contact.event.StopRingEvent;
import com.ubtechinc.contact.model.UserContact;
import com.ubtechinc.contact.notice.DefaultNotice;
import com.ubtechinc.contact.notice.INotice;
import com.ubtechinc.contact.phone.DefaultRing;
import com.ubtechinc.contact.phone.IRing;
import com.ubtechinc.contact.phone.IRingListener;
import com.ubtechinc.contact.util.Constant;
import com.ubtechinc.contact.util.TTSPlayUtil;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.express.listeners.AnimationListener;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.interactor.MasterInteractor;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.skill.SkillsProxy;
import com.ubtrobot.masterevent.protos.SysMasterEvent;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.phone.StartRingOuterClass;
import com.ubtrobot.speech.listener.TTsListener;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import event.master.ubtrobot.com.sysmasterevent.SysEventApi;
import event.master.ubtrobot.com.sysmasterevent.data.KeyCode;
import event.master.ubtrobot.com.sysmasterevent.event.HeadEvent;
import event.master.ubtrobot.com.sysmasterevent.event.PowerButtonEvent;
import event.master.ubtrobot.com.sysmasterevent.event.base.KeyEvent;
import event.master.ubtrobot.com.sysmasterevent.receiver.SingleClickReceiver;


/**
 * @desc : 电话监听服务
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/2/5
 */

public class PhoneListenerService extends Service {
    private static final String PHONE_RING_EXPRESS = "ring_001";
    private static final String PHONE_CALL_EXPRESS = "ring_002";
    private String phoneExpress = PHONE_RING_EXPRESS;
    public static final String TOUCH_DOWN_VOLUME_UP_BROADCAST = "key_up_volume_down_broadcast";
    public static final String TOUCH_UP_VOLUME_UP_BROADCAST = "key_up_volume_up_broadcast";
    private static final String TAG = "PhoneListenerService";
    private static final long MSG_DELAY = 3 * 1000;
    private boolean onComming;
    private String phoneName;
    private List<String> missContact = new ArrayList<>(2);
    private MyPhoneStateListener myPhoneStateListener;
    private INotice notice = new DefaultNotice();
    private boolean contactCalling = true;
    private boolean offHook = false;
    private boolean isHungup = false;
    private PhoneEventNotify phoneEventNotify;
    public static int state;
    // TODO delete 测试切换
    private String ACTION_RING = "com.ubtrobot.service.ring";
    private String ACTION_RING_KEY_WAKEUP = "com.ubtrobot.service.ring.wakeup";
    private String ACTION_RING_KEY_ENDING = "com.ubtrobot.service.ring.ending";
    private static final String ACTION_NAME = "wind.action.CUSTOM_KEY_EVENT";
    private static final String HEAD_CLICK_ACTION_NAME = "touch_down_broadcast";
    private IPhoneAnswer phoneAnswer;
    private static RingListenerEx ringListenerEx;
    private IRing ring;
    // 电话是否拦截，默认进行拦截
    private boolean isIntercept = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void setServiceForground() {
        LogUtils.d("TVSSpeechUtils", "setServiceForground");
        Notification.Builder builder = new Notification.Builder(this);
        Intent intent = new Intent(Intent.ACTION_CALL);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle("电话监听");
        builder.setContentText("电话监听服务永远活着");
        builder.setTicker("电话监听永存");
        builder.setOngoing(true);
        Notification notification = builder.build();
        this.startForeground(1, notification);
    }


    private void init() {
//        setServiceForground();
        phoneEventNotify = PhoneEventNotify.getInstance();
        EventBus.getDefault().register(this);
        myPhoneStateListener = new MyPhoneStateListener();
        TelephonyManager manager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        manager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        getContentResolver().registerContentObserver(
                CallLog.Calls.CONTENT_URI, true,
                new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        super.onChange(selfChange);
                        Log.d(TAG, " onChange -- selfChange : " + selfChange);
                        Contact.getContactFunc().update();
                    }

                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        super.onChange(selfChange, uri);
                        Log.d(TAG, " onChange -- selfChange : " + selfChange + " uri : " + uri);
                        Contact.getContactFunc().update();
                    }
                });

        phoneAnswer = new DefaultPhoneAnswer(this.getApplicationContext());
        ringListenerEx = new RingListenerEx();
        ring = new DefaultRing(this);
        ring.setRingListener(ringListenerEx);
    }

    public static RingListenerEx getRingListener() {
        return ringListenerEx;
    }

    private SingleClickReceiver singleClickReceiver;
    private SingleClickReceiver powerSingleClickReceiver;
    private void subcribeSingleClickReceiver(){
        if(singleClickReceiver == null){
            singleClickReceiver = new SingleClickReceiver() {
                @Override
                public boolean onSingleClick(KeyEvent keyEvent) {
                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                        phoneAnswer.onAnswer();
                    }
                    return true;
                }
            };
        }
        if(powerSingleClickReceiver == null){
            powerSingleClickReceiver = new SingleClickReceiver() {
                @Override
                public boolean onSingleClick(KeyEvent keyEvent) {
                    isHungup = true;
                    phoneAnswer.onDecline();
                    return true;
                }
            };
            SysEventApi.get().subscribe(PowerButtonEvent.newInstance().setPriority(SysMasterEvent.Priority.HIGH),powerSingleClickReceiver);
            SysEventApi.get().subscribe(HeadEvent.newInstance().setPriority(SysMasterEvent.Priority.HIGH),singleClickReceiver);
        }
    }
    private void unsubcribeSingleClickReceiver(){
        if(singleClickReceiver != null){
            SysEventApi.get().unsubscribe(singleClickReceiver);
            singleClickReceiver = null;
        }
        if(powerSingleClickReceiver != null){
            SysEventApi.get().unsubscribe(powerSingleClickReceiver);
            powerSingleClickReceiver = null;
        }
    }
    void stopRing() {
        Log.d(TAG, " stopRing ");
        EventBus.getDefault().post(new StopRingEvent());
    }

    private class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, final String incomingNumber) {
            Log.d(TAG, " onCallStateChanged ------- incomingNumber: " + incomingNumber + " state : " + state);
            super.onCallStateChanged(state, incomingNumber);
            int oldState = PhoneListenerService.this.state;
            PhoneListenerService.this.state = state;
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                subcribeSingleClickReceiver();
                if (oldState == TelephonyManager.CALL_STATE_OFFHOOK) {
                    onSecondCall(incomingNumber);
                } else {
                    onCall(incomingNumber);
                    phoneExpress = PHONE_RING_EXPRESS;
                    startPhoneExpress();
                }
            } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                phoneExpress = PHONE_CALL_EXPRESS;
                onOffHook();
            } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                onIdle(incomingNumber);
                stopPhoneExpress();
            }
        }
    }
    private String secondContactName;
    private void onSecondCall(String incomingNumber) {
        Log.d(TAG, " onSecondCall ");
        String contactName = Contact.getContactFunc().containsPhoneNumber(incomingNumber);
        phoneAnswer.onDeclineComming();
        if (contactName != null) {
            secondContactName = contactName;
            missContact.add(contactName);
            notice.notifyMiss(Contact.getContactFunc().containsPhoneNumber(incomingNumber));
        } else {
            notice.notifyIntercept(incomingNumber);
        }
    }

    private void onIdle(String incomingNumber) {
        // 停止来电状态
        endComming();
        endCall();
        notifyEndingCall(incomingNumber);
        offHook = false;
        isHungup = false;
        unsubcribeSingleClickReceiver();
        stopConverseSkill();
    }

    private void contactComingNotify() {
        Log.d(TAG, " contactComingNotify ");
        if (!missContact.isEmpty()) {
            if (missContact.size() == 1) {
                TTSPlayUtil.playLocalTTs(Constant.LOCAL_TTSNAME_MISSCALL1);
            } else if (missContact.size() == 2) {
                TTSPlayUtil.playLocalTTs(Constant.LOCAL_TTSNAME_MISSCALL2);
            } else {
                TTSPlayUtil.playLocalTTs(Constant.LOCAL_TTSNAME_MISSCALL3);
            }
        }
        missContact.clear();
    }

    private void onOffHook() {
        endComming();
        Log.d(TAG, " onOffHook -- contactCalling : " + contactCalling);
        contactCalling = false;
        offHook = true;
        stopRingSkill();
        startCallState();
        stopRing();
    }

    private void notifyEndingCall(String incomingNumber) {
        if (contactCalling) {
            contactCalling = false;
            if(!isHungup){
                notice.notifyMiss(Contact.getContactFunc().containsPhoneNumber(incomingNumber));
            }
        }
    }

    private void onCall(String incomingNumber) {
        String contactName = Contact.getContactFunc().containsPhoneNumber(incomingNumber);
        Log.d(TAG, " onCall incomingNumber : " + incomingNumber + " contactName : " + contactName);
        if (!isIntercept && contactName == null) {
            contactName = "未知用户";
        }
        if (contactName != null) {
            contactCalling = true;
            phoneName = contactName;
            startRingState();
            UserContact userContact = new UserContact(contactName, incomingNumber);
            List<UserContact> userContactList = new ArrayList<>();
            userContactList.add(userContact);
            phoneEventNotify.notifyCallComingUp(userContactList);
        } else {
            Contact.getContactFunc().endCall();
            notice.notifyIntercept(incomingNumber);
        }
    }

    /**
     * 退出通话Skill
     */
    private void stopConverseSkill() {
        EventBus.getDefault().post(new ConverseEndEvent());
    }

    /**
     * 退出铃声Skill
     */
    private void stopRingSkill() {
        Log.d(TAG, " stopRingSkill -- RingEndEvent");
        EventBus.getDefault().post(new RingEndEvent());
        notifyRingEnding();
    }

    /**
     * 进入来电铃声状态
     */
    private void startRingState() {
        Log.d(TAG, " startRingState ");
        String packageName = Contact.getInstance().getContext().getPackageName();
        MasterInteractor interactor = Master.get().getOrCreateInteractor("robot:" + packageName);
        SkillsProxy aSkillsProxy = interactor.createSkillsProxy();
        ResponseCallback callback = new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {
                Log.i(TAG, "response====" + response.getPath());
            }

            @Override
            public void onFailure(Request request, CallException e) {
                Log.i(TAG, "errorMsg====" + e.getMessage() + ";;errorCode===" + e.getCode());
            }
        };
        StartRingOuterClass.StartRing startRing = StartRingOuterClass.StartRing.newBuilder().setPhoneNumber(phoneName).build();
        aSkillsProxy.call("/phone/ring", ProtoParam.create(startRing), callback);
        subcribeSingleClickReceiver();
    }

    /**
     * 进入通话状态
     */
    private void startCallState() {
        Log.d(TAG, " startCallState ");
        String packageName = Contact.getInstance().getContext().getPackageName();
        MasterInteractor interactor = Master.get().getOrCreateInteractor("robot:" + packageName);
        SkillsProxy aSkillsProxy = interactor.createSkillsProxy();
        ResponseCallback callback = new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {
                Log.i(TAG, "response====" + response.getPath());
            }

            @Override
            public void onFailure(Request request, CallException e) {
                Log.i(TAG, "errorMsg====" + e.getMessage() + ";;errorCode===" + e.getCode());
            }
        };
        aSkillsProxy.call("/phone/call", callback);
        subcribeSingleClickReceiver();
    }

    private void endCall() {
        Log.i(TAG, "endCall===========");
        stopConverseSkill();
        stopRingSkill();
        if (offHook) {
            Log.i(TAG,"secondContactName======" + secondContactName);
            if(!TextUtils.isEmpty(secondContactName)){
                secondContactName = "";
                String tts = "刚刚有来自"+secondContactName+"的未接来电，如果需要回拨，请说悟空悟空打电话给" + secondContactName;
                VoicePool.get().playTTs(tts, Priority.NORMAL, null);
            }else {
                ring.playEndRing();
            }
        }
    }

    // 停止来电状态
    private void endComming() {
        onComming = false;
        stopRing();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onAnswer(AnswerEvent answerEvent) {
        Log.d(TAG, " onAnswer ");
        if (state == TelephonyManager.CALL_STATE_RINGING) {
            phoneAnswer.onAnswer();
        }
    }

    @Subscribe
    public void onStartCall(StartCallEvent startCallEvent) {
        Log.i(TAG, "onStartCall=========");
        subcribeSingleClickReceiver();
        startPhoneExpress();
    }

    @Subscribe
    public void onHangup(HangupEvent answerEvent) {
        Log.d(TAG, " onHangup ");
        isHungup = true;
        phoneAnswer.onDecline();
    }

    @Subscribe
    public void onPlayRing(PlayRingEvent playRingEvent) {

        if (state == TelephonyManager.CALL_STATE_RINGING) {
            EventBus.getDefault().post(new RealPlayRingEvent(phoneName));
        }
    }
    private boolean isStartPhoneExpress = false;
    private void startPhoneExpress(){
        if(!isStartPhoneExpress){
            isStartPhoneExpress = true;
            loopExpress(phoneExpress);
        }
    }

    private void loopExpress(final String expressName) {
        ExpressApi.get().doExpress(expressName, 1, Priority.LOW, new AnimationListener() {
            @Override
            public void onAnimationStart() {

            }

            @Override
            public void onAnimationEnd() {
                if(isStartPhoneExpress){
                    loopExpress(phoneExpress);
                }
            }

            @Override
            public void onAnimationRepeat(int loopNumber) {

            }
        });
    }

    private void stopPhoneExpress(){
        isStartPhoneExpress = false;
    }
    public class TTsListenerAdapter implements TTsListener {

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

    private class RingListenerEx implements IRingListener {
        @Override
        public void onRingCompletely() {
            Log.d(TAG, " onRingCompletely ");
            notifyRingCompletely();
        }

        @Override
        public void onEndRingCompletely() {
            Log.d(TAG, " onEndRingCompletely ");
            contactComingNotify();
        }
    }

    private void notifyRingCompletely() {
        Intent intent = new Intent(ACTION_RING);
        intent.putExtra(ACTION_RING_KEY_WAKEUP, RingService.wakeupState);
        sendBroadcast(intent);
    }

    private void notifyRingEnding() {
        Intent intent = new Intent(ACTION_RING);
        intent.putExtra(ACTION_RING_KEY_ENDING, true);
        sendBroadcast(intent);
    }
}
