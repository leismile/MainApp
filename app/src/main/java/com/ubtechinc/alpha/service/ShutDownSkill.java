package com.ubtechinc.alpha.service;

import com.google.protobuf.BoolValue;
import com.ubtech.utilcode.utils.LogUtils;
import com.ubtechinc.alpha.utils.AlphaUtils;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.skill.MasterSkill;
import com.ubtrobot.master.skill.SkillStopCause;
import com.ubtrobot.master.transport.message.CallGlobalCode;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

/**
 * Created by lulin.wu on 2018/5/23.
 */

public class ShutDownSkill extends MasterSkill {
    @Override
    protected void onSkillStart() {

    }

    @Override
    protected void onSkillStop(SkillStopCause skillStopCause) {

    }
    @Override
    protected void onCall(Request request, Responder responder) {
        responder.respondFailure(CallGlobalCode.NOT_IMPLEMENTED,
                request.getPath() + " NOT implemented.");
    }

    @Call(path = "/control/prowoff")
    public void controlProwoff(Request request, final Responder responder){
        LogUtils.i("ShutDownSkill","关机==================");
        try {
            LogUtils.i("ShutDownSkill","type=======" + request.getParam().isEmpty());
            if(request.getParam().isEmpty()){
                AlphaUtils.shutDown(true);
            }else {
                BoolValue isLongPress = ProtoParam.from(request.getParam(), BoolValue.class).getProtoMessage();
                LogUtils.i("ShutDownSkill","isLongPress======" + isLongPress.getValue());
                AlphaUtils.shutDown(isLongPress.getValue());
            }
        } catch (ProtoParam.InvalidProtoParamException e) {
            e.printStackTrace();
        }
        responder.respondSuccess();
    }
}
