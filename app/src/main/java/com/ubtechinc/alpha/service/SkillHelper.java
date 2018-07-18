package com.ubtechinc.alpha.service;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.ubtech.utilcode.utils.LogUtils;
import com.ubtechinc.bluetoothlibrary.UbtBluetoothConnManager;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.call.MasterSubCode;
import com.ubtrobot.master.component.ComponentBaseInfo;
import com.ubtrobot.master.context.ContextRunnable;
import com.ubtrobot.master.interactor.MasterInteractor;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.skill.SkillIntent;
import com.ubtrobot.master.skill.SkillOpponent;
import com.ubtrobot.master.skill.SkillsProxy;
import com.ubtrobot.master.transport.message.CallGlobalCode;
import com.ubtrobot.master.transport.message.parcel.ParcelableParam;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.speech.SpeechApi;
import com.ubtrobot.speech.listener.TTsListener;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Param;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;

import java.util.List;

/**
 * Created by bob.xu on 2018/2/22.
 */

public class SkillHelper {
    public static final String TAG = "SkillHelper";
    public static void startSkillByIntent(String utterance,Any any) {
        String packageName = com.ubtech.utilcode.utils.Utils.getContext().getPackageName();
        MasterInteractor interactor = Master.get().getOrCreateInteractor("robot:" + packageName);
        SkillsProxy aSkillsProxy = interactor.createSkillsProxy();
        SkillIntent skillIntent = new SkillIntent(SkillIntent.CATEGORY_SPEECH);
        skillIntent.setSpeechUtterance(utterance);
        Param param = null;
        if (any != null) {
            param = ProtoParam.create(any);
        }
        ResponseCallback callback = new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {
                Log.i(TAG,"onResponse===path====" + request.getPath() + "response===="+response.getPath());
            }

            @Override
            public void onFailure(Request request, CallException e) {
                Log.i(TAG,"onFailure===path====" + request.getPath() + "errorMsg===="+e.getMessage() + ";;errorCode===" + e.getCode());
            }
        };
        aSkillsProxy.call(skillIntent,param, callback);
    }

    private static void startSkillByPath(String path,Any any){
        ResponseCallback callback = new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {
                Log.i(TAG,"onResponse===path====" + request.getPath() + "response===="+response.getPath());
            }

            @Override
            public void onFailure(Request request, CallException e) {
                Log.i(TAG,"onFailure===path====" + request.getPath() + "errorMsg===="+e.getMessage() + ";;errorCode===" + e.getCode());
            }
        };
        startSkillByPath(path, any, callback);
    }

    private static void startSkillByPath(String path,Any any, ResponseCallback callback){
        String packageName = com.ubtech.utilcode.utils.Utils.getContext().getPackageName();
        MasterInteractor interactor = Master.get().getOrCreateInteractor("robot:" + packageName);
        SkillsProxy aSkillsProxy = interactor.createSkillsProxy();
        Param param = null;
        if (any != null) {
            param = ProtoParam.create(any);
        }
        aSkillsProxy.call(path,param, callback);
    }
    public static void startBleNetwrokSkill(){
        SkillHelper.startSkillByPath("/bluetooth/network", null, new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {

            }

            @Override
            public void onFailure(Request request, CallException e) {
                fordReasonHandler(e);
            }
        });
    }

    public static void stopBleNetworkSkill(){
        Master.get().execute(BleNetworkSkill.class, new ContextRunnable<BleNetworkSkill>() {
            @Override
            public void run(BleNetworkSkill bleNetworkSkill) {
                bleNetworkSkill.stopSkill();
            }
        });
    }

    /**
     * 关闭关机skill
     */
    public static void stopShutDownSkill(){
        Master.get().execute(ShutDownSkill.class, new ContextRunnable<ShutDownSkill>() {

            @Override
            public void run(ShutDownSkill shutdownskill) {
                shutdownskill.stopSkill();
            }
        });
    }

    /**
     * 关闭待机skill
     */
    public static void stopStandbySkill(){
        Master.get().execute(StandbySkill.class, new ContextRunnable<StandbySkill>() {
            @Override
            public void run(StandbySkill standbySkill) {
                standbySkill.stopSkill();
            }
        });
    }

    public static void startShutDownSkill(boolean isLongPress){
        Any any = Any.pack(BoolValue.newBuilder().setValue(isLongPress).build());
        SkillHelper.startSkillByPath("/control/prowoff",any);
    }
    public static void startSleepSkill(){
        SkillHelper.startSkillByPath("/control/sleep",null);
    }
    /**skill禁止调用的原因分析*/
    public static void fordReasonHandler(CallException e) {
        if (e.getCode() == CallGlobalCode.FORBIDDEN && e.getSubCode() == MasterSubCode.FORBIDDEN_TO_START_SKILL) {
            try {
                SkillOpponent skillOpponent = ParcelableParam.from(e.getParam(),SkillOpponent.class).getParcelable();
                List<ComponentBaseInfo> skillList =  skillOpponent.getSkillList();
                List<Pair<ComponentBaseInfo,String>> stateList = skillOpponent.getServiceStateList();
                List<Pair<ComponentBaseInfo,String>> skillStateList = skillOpponent.getSkillStateList();

                //优先判断状态冲突、再判断skill冲突
                boolean hasFoundReason = false;
                if (stateList!= null && stateList.size() > 0) {
                    for(Pair<ComponentBaseInfo,String> pair : stateList) {
                        if (pair != null && pair.second != null) {
                            if (pair.second.equals("lowPower")) {
                                LogUtils.d(TAG,"低电量，不支持随机动作");
                                VoicePool.get().playLocalTTs("low-power_003", Priority.NORMAL, null);
                                hasFoundReason = true;
                            } else if (pair.second.equals("fallclimb")){
                                hasFoundReason = true;
                            }
                        }
                    }
                }

                if (!hasFoundReason) {
                    if (skillList != null && skillList.size() > 0) {
                        LogUtils.d(TAG,"当前正在运行--"+skillList.get(0).getName()+",不支持随机动作");
                        String skillName = null;
                        for (ComponentBaseInfo skillComponent : skillList) {
                            if (skillComponent != null && skillComponent.getName() != null) {
                                skillName = skillComponent.getName();
                                if (!TextUtils.isEmpty(skillName)) {
                                    break;
                                }
                            }
                        }
                        Log.i(TAG,"skillName=====" + skillName);
                        if (skillName.equals("avatar")) {
                            VoicePool.get().playTTs("我现在在视频监控中哦，不能分心，退出视频监控再试吧",Priority.NORMAL,null);
                        }
                        if(skillName.equals("phone_call") || skillName.equals("phone_ring")){
                            VoicePool.get().playTTs("我正在通话中哦,结束通话在试吧",Priority.NORMAL,null);
                        }
                        if(skillName.equals("codemao_running")){
                            VoicePool.get().playTTs("悟空正在编程模式中，可以按电源键退出编程模式",Priority.NORMAL,null);
                        }
                        if(skillName.equals("critical_upgrade_skill")){
                            UbtBluetoothConnManager.getInstance().cancelCurrentDevices();
                            VoicePool.get().playTTs("悟空正在升级中，等我升级完在试吧。",Priority.NORMAL,null);

                        }
                        hasFoundReason = true;
                    }
                }

            } catch (ParcelableParam.InvalidParcelableParamException e1) {
                e1.printStackTrace();
            }
        }
    }

}
