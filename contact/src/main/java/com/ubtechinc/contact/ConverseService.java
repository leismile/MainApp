package com.ubtechinc.contact;

import android.util.Log;

import com.ubtechinc.contact.event.ConverseEndEvent;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.skill.MasterSkill;
import com.ubtrobot.master.skill.SkillStopCause;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * @desc : 电话Skill服务
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/2/9
 */

public class ConverseService extends MasterSkill{

    private static final String TAG = "ConverseService";
    public static volatile boolean skillStart = false;

    @Override
    protected void onSkillStart() {
        Log.d(TAG, " onCalling == ");
        if(!skillStart) {
            Log.d(TAG, " onCalling skillStart ：" + skillStart);
            skillStart = true;
            EventBus.getDefault().register(this);
            ExpressApi.get().doExpress("phone_calling_001", Short.MAX_VALUE, Priority.HIGH);
            Log.d(TAG, " onCalling phone_calling_001 ：");
        }
    }


    @Override
    protected void onSkillStop(SkillStopCause skillStopCause) {
        Log.d(TAG, " onSkillStop ");
        EventBus.getDefault().unregister(this);
        skillStart = false;
    }

    @Subscribe
    public void onEnd(ConverseEndEvent messageEvent) {
        Log.d(TAG, " onEnd ");
        stopSkill();
        ExpressApi.get().doExpress("normal_1", 0, Priority.HIGH);
    }

    @Call(path="/phone/call")
    public void onCalling(Request request, Responder responder){
        Log.d(TAG, " onCalling ");
    }
}
