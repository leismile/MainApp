package com.ubtechinc.alpha.im.msghandler;


import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import com.ubtech.utilcode.utils.CollectionUtils;
import com.ubtechinc.alpha.AlGetAgoraRoomInfo;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.im.IMCmdId;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.call.CallConfiguration;
import com.ubtrobot.master.call.MasterSubCode;
import com.ubtrobot.master.component.ComponentBaseInfo;
import com.ubtrobot.master.interactor.MasterInteractor;
import com.ubtrobot.master.skill.SkillOpponent;
import com.ubtrobot.master.skill.SkillsProxy;
import com.ubtrobot.master.transport.message.CallGlobalCode;
import com.ubtrobot.master.transport.message.parcel.ParcelableParam;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Param;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;

import java.util.HashMap;
import java.util.List;


//import com.ubtechinc.alpha.speech.SpeechServiceProxy;


/**
 * Created by hongjie.xiang on 2017/9/4.
 */

public class AlGetAgoraRoomInfoMsgHandler implements IMsgHandler {
    private static final String TAG = "AlGetRoomInfoMsgHandler";
    long requestSerial;
    private int resposeId;
    private String peer;

    public static final int ERROR_DEFAULT = -1;
    public static final int ERROR_BUSY = 1;
    public static final int ERROR_BUSY_CALL = 2;
    public static final int ERROR_LOW_POWER = 3;
    public static final int ERROR_CODING = 4;
    public static final int ERROR_TIMEOUT = 5;

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        requestSerial = request.getHeader().getSendSerial();
        resposeId = responseCmdId;
        this.peer = peer;
        doStart();
    }

    private void doStart() {
        sendStartAppEvent();
    }

    private void sendStartAppEvent() {
        startAvatar();
    }

    private void startAvatar() {
        Log.i("wll", "startAvatar+++++++++");
        String packageName = com.ubtech.utilcode.utils.Utils.getContext().getPackageName();
        MasterInteractor interactor = Master.get().getOrCreateInteractor("robot:" + packageName);
        SkillsProxy aSkillsProxy = interactor.createSkillsProxy();
        CallConfiguration configuration = new CallConfiguration.Builder().setTimeout(20000).build();
        aSkillsProxy.setConfiguration(configuration);
        ResponseCallback callback = new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {
                Log.i(TAG, "response====" + response.getPath());
                try {
                    Bundle bundle = ParcelableParam.from(response.getParam(), Bundle.class).getParcelable();
                    int roomId = bundle.getInt("roomId", -1);
                    AlGetAgoraRoomInfo.AlGetAgoraRoomInfoResponse.Builder builder = AlGetAgoraRoomInfo.AlGetAgoraRoomInfoResponse.newBuilder();
                    if (roomId != -1) {
                        builder.setRoomNumber(String.valueOf(roomId));
                    } else {
                        builder.setResultCode(ERROR_BUSY);
                        builder.setErrorMsg(getMsg(ERROR_BUSY));
                    }
                    RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(resposeId, IMCmdId.IM_VERSION, requestSerial, builder.build(), peer, null);
                } catch (ParcelableParam.InvalidParcelableParamException e) {
                    AlGetAgoraRoomInfo.AlGetAgoraRoomInfoResponse.Builder builder = AlGetAgoraRoomInfo.AlGetAgoraRoomInfoResponse.newBuilder();
                    builder.setResultCode(ERROR_BUSY);
                    builder.setErrorMsg(getMsg(ERROR_BUSY));
                    RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(resposeId, IMCmdId.IM_VERSION, requestSerial, builder.build(), peer, null);
                }
            }

            @Override
            public void onFailure(Request request, CallException e) {
                Log.i(TAG, "errorMsg====" + e.getMessage() + ";;errorCode===" + e.getCode());
                startFail(e);

            }
        };
        Bundle bundle = new Bundle();
        bundle.putString("userId", this.peer);
        Param param = ParcelableParam.create(bundle);
        aSkillsProxy.call("/startavatar", param, callback);
    }

    private void startFail(CallException e) {
        Integer code = ERROR_DEFAULT;
        AlGetAgoraRoomInfo.AlGetAgoraRoomInfoResponse.Builder builder = AlGetAgoraRoomInfo.AlGetAgoraRoomInfoResponse.newBuilder();
        if(e.getCode() == CallGlobalCode.RESPOND_TIMEOUT){
            code = ERROR_TIMEOUT;
        }else{
            if (e.getCode() == CallGlobalCode.FORBIDDEN && e.getSubCode() == MasterSubCode.FORBIDDEN_TO_START_SKILL) {
                try {
                    SkillOpponent skillOpponent = ParcelableParam.from(e.getParam(), SkillOpponent.class).getParcelable();
                    List<ComponentBaseInfo> components = skillOpponent.getSkillList();
                    List<Pair<ComponentBaseInfo, String>> stateList = skillOpponent.getServiceStateList();
                    if (!CollectionUtils.isEmpty(stateList)) {
                        for (Pair<ComponentBaseInfo, String> componentBaseInfoStringPair : stateList) {
                            code = getSkillCodeMap.get(componentBaseInfoStringPair.second);
                            if (code != null) {
                                break;
                            }
                        }
                        if(code == null){
                            code = ERROR_BUSY;
                        }
                    }
                    if (code == null || code == ERROR_BUSY || code == ERROR_DEFAULT) {
                        if (!CollectionUtils.isEmpty(components)) {
                            ComponentBaseInfo info = components.get(0);
                            code = getSkillCodeMap.get(info.getName());
                            if (code == null) {
                                code = ERROR_BUSY;
                            }
                        }
                    }
                } catch (ParcelableParam.InvalidParcelableParamException e1) {
                    e1.printStackTrace();
                    code = ERROR_DEFAULT;
                }
            }
            if (code == null) {
                code = ERROR_DEFAULT;
            }
        }
        builder.setResultCode(code);
        builder.setErrorMsg(getMsg(code));
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(resposeId, "1", requestSerial, builder.build(), peer, null);
    }

    private final static HashMap<String, Integer> getSkillCodeMap = new HashMap<>();
    private final static HashMap<Integer, String> getSkillMsgMap = new HashMap<>();

    static {
        getSkillCodeMap.put("phone_call", ERROR_BUSY_CALL);
        getSkillCodeMap.put("phone_ring", ERROR_BUSY_CALL);
        getSkillCodeMap.put("lowPower", ERROR_LOW_POWER);
        getSkillCodeMap.put("codemaoagent", ERROR_CODING);

        getSkillMsgMap.put(ERROR_BUSY_CALL, "悟空正在通话中，请稍后再试!");
        getSkillMsgMap.put(ERROR_LOW_POWER, "悟空电量低，一会再试吧!");
        getSkillMsgMap.put(ERROR_CODING, "悟空执行编程中，请稍后再试!");
    }

    private static String getMsg(int code) {
        switch (code) {
            case ERROR_BUSY:
                return "悟空正在做一些厉害的技能呢，一会再试吧";
            case ERROR_BUSY_CALL:
            case ERROR_LOW_POWER:
            case ERROR_CODING:
                return getSkillMsgMap.get(code);
            case ERROR_TIMEOUT:
                return "连接超时,一会再试吧";
            default:
                return "发生内部错误，一会再试吧";
        }
    }
}
