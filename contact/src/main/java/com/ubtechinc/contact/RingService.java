package com.ubtechinc.contact;

import android.content.Intent;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.contact.event.PlayEndRingEvent;
import com.ubtechinc.contact.event.RealPlayRingEvent;
import com.ubtechinc.contact.event.RingEndEvent;
import com.ubtechinc.contact.event.StopRingEvent;
import com.ubtechinc.contact.phone.DefaultRing;
import com.ubtechinc.contact.phone.IRing;
import com.ubtechinc.nets.phonerobotcommunite.ProtoBufferDispose;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.skill.MasterSkill;
import com.ubtrobot.master.skill.SkillStopCause;
import com.ubtrobot.phone.StartRingOuterClass;
import com.ubtrobot.speech.SpeechApi;
import com.ubtrobot.speech.protos.Speech;
import com.ubtrobot.speech.receivers.WakeupReceiver;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * @desc : 铃声Skill
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/3/14
 */

public class RingService extends MasterSkill {

    private String ACTION_RING = "com.ubtrobot.service.ring";
    private String ACTION_RING_KEY_WAKEUP = "com.ubtrobot.service.ring.wakeup";
    private static final String TAG = "RingService";
    public volatile static boolean skillStart = false;
    public static boolean wakeupState = false;
    private IRing ring;
    private WakeupReceiver wakeupReceiver;

    @Override
    protected void onSkillStart() {
        Log.d(TAG, " onSkillStart : onSkillStart ");
        if(!skillStart) {
            skillStart = true;
            EventBus.getDefault().register(this);
            ring = new DefaultRing(this);
            ring.setRingListener(PhoneListenerService.getRingListener());
            wakeupReceiver = new WakeupReceiver() {
                @Override
                public void onWakeup(Speech.WakeupParam wakeupParam) {
                    Log.d(TAG, " onWakeup ");
                    if(ring.isOnRing()) {
                        ring.stopCommingRing();
                        notifyRingCompletely();
                    }
                }
            };
            SpeechApi.get().subscribeEvent(wakeupReceiver);
            //TODO 没有表情，先用normal_1替换一下
            ExpressApi.get().doExpress("normal_1", 1, Priority.HIGH);
//            ExpressApi.get().doExpress("phone_ringing_001", Short.MAX_VALUE, Priority.HIGH);
            Log.d(TAG, " phone_ringing_001 : ");

        }
    }

    @Subscribe
    public void onPlayEndRing(PlayEndRingEvent ringEndEvent) {
        ring.playEndRing();
    }

    @Subscribe
    public void onStopRing(StopRingEvent stopRingEvent) {
        Log.d(TAG, " onStopRing -- stopRing ");
        ring.stopCommingRing();

    }

    @Subscribe
    public void onRealPlayRing(RealPlayRingEvent realPlayRingEvent) {
        ring.playCommingRing(realPlayRingEvent.getPhoneName(), wakeupState);
    }

    @Subscribe
    public void onEnd(RingEndEvent ringEndEvent) {
        Log.d(TAG, " onEnd ");
        ExpressApi.get().doExpress("normal_1", 0, Priority.HIGH);
        stopSkill();
    }

    @Override
    protected void onSkillStop(SkillStopCause skillStopCause) {
        Log.d(TAG, "onSkillStop  11 ");
        EventBus.getDefault().unregister(this);
        SpeechApi.get().unsubscribeEvent(wakeupReceiver);
        skillStart = false;
    }

    @Call(path="/phone/ring")
    public void onRing(Request request, Responder responder){
        Log.d(TAG, " onRing ");
        try {
            StartRingOuterClass.StartRing callRequest = ProtoParam.from(request.getParam(), StartRingOuterClass.StartRing.class).getProtoMessage();
            ring.playCommingRing(callRequest.getPhoneNumber(), wakeupState);
        } catch (ProtoParam.InvalidProtoParamException e) {
            e.printStackTrace();
        }
        responder.respondSuccess();
    }

    private void notifyRingCompletely() {
        Intent intent = new Intent(ACTION_RING);
        intent.putExtra(ACTION_RING_KEY_WAKEUP, RingService.wakeupState);
        sendBroadcast(intent);
    }

    public static <T> T getRequestClass(AlphaMessageOuterClass.AlphaMessage request, Class<T> tClass){
        ByteString requestBody = request.getBodyData();
        byte[] bodyBytes = requestBody.toByteArray();
        long requestSerial = request.getHeader().getSendSerial();
        T value = (T) ProtoBufferDispose.unPackData(tClass, bodyBytes);
        return value;
    }
}
