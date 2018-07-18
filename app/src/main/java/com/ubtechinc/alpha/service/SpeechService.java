package com.ubtechinc.alpha.service;

import android.util.Log;

import com.ubtechinc.alpha.CmCameraPrivacy;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.appmanager.InfraRedManager;
import com.ubtechinc.alpha.appmanager.SysStatusManager;
import com.ubtechinc.alpha.utils.AlphaUtils;
import com.ubtechinc.alpha.utils.SoundVolumesUtils;
import com.ubtechinc.alpha.utils.SystemPropertiesUtils;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.skill.MasterSkill;
import com.ubtrobot.master.skill.SkillStopCause;
import com.ubtrobot.master.transport.message.CallGlobalCode;
import com.ubtrobot.masterevent.protos.SysMasterEvent;
import com.ubtrobot.speech.protos.Speech;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

/**
 * @author：wululin
 * @date：2017/11/22 10:49
 * @modifier：ubt
 * @modify_date：2017/11/22 10:49
 * [A brief description]
 * [接收语音服务的skill指令]
 */

public class SpeechService extends MasterSkill {

    @Call(path = "/volume/adjust")
    public void adjustVolume(Request request, final Responder responder){
        Log.i("VolumeService","adjustVolume==========");
        try {
            Speech.VolumeParam param = ProtoParam.from(request.getParam(), Speech.VolumeParam.class).getProtoMessage();
            int volume = SoundVolumesUtils.get(AlphaApplication.getContext()).getVolumeLevel();
            if(volume == SoundVolumesUtils.get(AlphaApplication.getContext()).getMaxVolume()){
                AlphaUtils.playBehavior("volume_0010", Priority.HIGH,null);
            }
            switch (param.getOptType()){
                case AddVolume: //增加音量
                    SoundVolumesUtils.get(AlphaApplication.getContext()).addVolume(2);
                    break;
                case ReduceVolume://减小音量
                    SoundVolumesUtils.get(AlphaApplication.getContext()).mulVolume(2);
                    break;
                case SetMute://设置静音

                    break;
                case SetVolume: //设置音量

                    break;
            }
        } catch (ProtoParam.InvalidProtoParamException e) {
            e.printStackTrace();
        }
        responder.respondSuccess();
        stopSkill();
    }
    @Call(path = "/cameraprivacy/open")
    public void openCamerPrivacy(){
        SystemPropertiesUtils.setCameraPrivacyType(CmCameraPrivacy.CameraPrivacyType.ON);
        InfraRedManager.get().setAllowStartInfraRad();
    }
    @Call(path = "/cameraprivacy/close")
    public void closeCamerPrivacy(){
        SystemPropertiesUtils.setCameraPrivacyType(CmCameraPrivacy.CameraPrivacyType.OFF);
        InfraRedManager.get().setAllowStartInfraRad();
    }
    @Override
    protected void onCall(Request request, Responder responder) {
        responder.respondFailure(CallGlobalCode.NOT_IMPLEMENTED,
                request.getPath() + " NOT implemented.");
    }

    @Override
    public void onSkillStart() {
        Log.i("VolumeService","onSkillStart==========");
    }

    @Override
    public void onSkillStop(SkillStopCause skillStopCause) {
        Log.i("VolumeService","onSkillStop==========");
    }

}
