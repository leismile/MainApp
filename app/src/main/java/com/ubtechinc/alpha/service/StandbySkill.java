package com.ubtechinc.alpha.service;

import android.util.Log;

import com.ubtechinc.alpha.appmanager.SysStatusManager;
import com.ubtechinc.alpha.utils.AlphaUtils;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.skill.MasterSkill;
import com.ubtrobot.master.skill.SkillStopCause;
import com.ubtrobot.master.transport.message.CallGlobalCode;
import com.ubtrobot.masterevent.protos.SysMasterEvent;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

/**
 * Created by lulin.wu on 2018/5/23.
 */

public class StandbySkill  extends MasterSkill {
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
    @Call(path = "/control/sleep")
    public void controlSleep(Request request, final Responder responder){
        Log.i("VolumeService","待机==================");
        SysStatusManager.getInstance().publishSysActiveStatus(SysMasterEvent.ActivieStatusType.STANDBY);
        responder.respondSuccess();
    }

}
