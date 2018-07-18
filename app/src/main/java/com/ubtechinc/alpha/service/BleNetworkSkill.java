package com.ubtechinc.alpha.service;

import com.ubt.alpha2.download.util.LogUtils;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.skill.MasterSkill;
import com.ubtrobot.master.skill.SkillStopCause;
import com.ubtrobot.master.transport.message.CallGlobalCode;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

/**
 * Created by lulin.wu on 2018/4/17.
 */

public class BleNetworkSkill extends MasterSkill {
    @Override
    protected void onSkillStart() {
        LogUtils.i("onSkillStart");
    }

    @Override
    protected void onSkillStop(SkillStopCause skillStopCause) {
        LogUtils.i("onSkillStop");
    }


    @Override
    protected void onCall(Request request, Responder responder) {
        responder.respondFailure(CallGlobalCode.NOT_IMPLEMENTED,
                request.getPath() + " NOT implemented.");
    }
    @Call(path = "/bluetooth/network")
    public void changeBleNetwork(Request request, final Responder responder){
        responder.respondSuccess();
    }
    @Call(path = "/bluetooth/exit")
    public void exitSkill(Request request, final Responder responder){
        responder.respondSuccess();
        stopSkill();
    }
}
