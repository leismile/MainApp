package com.ubtechinc.alpha.im.msghandler;

import android.text.TextUtils;
import android.util.Pair;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmSwitchCodeMao;
import com.ubtechinc.bluetoothrobot.SkillManager;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtrobot.master.call.MasterSubCode;
import com.ubtrobot.master.component.ComponentBaseInfo;
import com.ubtrobot.master.skill.SkillOpponent;
import com.ubtrobot.master.transport.message.CallGlobalCode;
import com.ubtrobot.master.transport.message.parcel.ParcelableParam;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;

import java.util.List;

import static com.ubtechinc.alpha.im.RequestParseUtils.getRequestClass;

/**
 * @Description 切换编程模式
 * @Author tanghongyu
 * @Time 2018/5/9 15:55
 */
public class SwitchCodeMaoHandler implements IMsgHandler {

    @Override
    public void handleMsg(int requestCmdId, final int responseCmdId, final AlphaMessageOuterClass.AlphaMessage alphaMessageRequest, final String peer) {
        CmSwitchCodeMao.SwitchModelRequest adbSwitchRequest = getRequestClass(alphaMessageRequest, CmSwitchCodeMao.SwitchModelRequest.class);
        boolean isOpen = adbSwitchRequest.getIsOpen();
        LogUtils.d("isOpen CodeMao = " + isOpen);
        if (isOpen) {

            CmSwitchCodeMao.SwitchModelResponse response = CmSwitchCodeMao.SwitchModelResponse.newBuilder().setIsSuccess(false).setResultCode(404).build();
            RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, "1", alphaMessageRequest.getHeader().getSendSerial(), response, peer, null);

//            SkillManager.Companion.getInstance().startCodeMaoConnectionSkill(new ResponseCallback() {
//                @Override
//                public void onResponse(Request request, Response response) {
//                    LogUtils.d("start success " );
//                    CmSwitchCodeMao.SwitchModelResponse callbackResponse = CmSwitchCodeMao.SwitchModelResponse.newBuilder().setIsSuccess(true).build();
//                    RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, "1", alphaMessageRequest.getHeader().getSendSerial(), callbackResponse, peer, null);
//                }
//
//                @Override
//                public void onFailure(Request request, CallException e) {
//                    SkillHelper.fordReasonHandler(e);
//                    int code = fordReasonHandler(e);
//                    LogUtils.w("start fail code = " + code);
//                    CmSwitchCodeMao.SwitchModelResponse response = CmSwitchCodeMao.SwitchModelResponse.newBuilder().setIsSuccess(false).setResultCode(code).build();
//                    RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, "1", alphaMessageRequest.getHeader().getSendSerial(), response, peer, null);
//                }
//            });
        } else {

            SkillManager.Companion.getInstance().stopCodeMaoConnectionSkill(new ResponseCallback() {
                @Override
                public void onResponse(Request request, Response response) {
                    CmSwitchCodeMao.SwitchModelResponse response1 = CmSwitchCodeMao.SwitchModelResponse.newBuilder().setIsSuccess(true).build();
                    RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, "1", alphaMessageRequest.getHeader().getSendSerial(), response1, peer, null);
                }

                @Override
                public void onFailure(Request request, CallException e) {
                    CmSwitchCodeMao.SwitchModelResponse response1 = CmSwitchCodeMao.SwitchModelResponse.newBuilder().setIsSuccess(false).build();
                    RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, "1", alphaMessageRequest.getHeader().getSendSerial(), response1, peer, null);
                }
            });

        }


    }

    private int ERROR_UNKNOWN_ERROR = 0;//
    private int ERROR_CODE_LOW_POWER = 1;//电量过低(悟空电量过低，无法执行编程动作)
    private int ERROR_ROBOT_AVATAR = 2;//视频监控（ 提示：悟空正在视频监控，请稍后再试）
    private int ERROR_ROBOT_BUSY = 3;//机器人忙碌（ 提示：其他人正使用悟空进行编程，请稍后再试）
    private int ERROR_ROBOT_PHONE_CALL = 4;//通话中（ 提示：悟空正在通话中，请通话结束后再试）
    private int ERROR_ROBOT_HIGH_RISK_ACTION = 5;//高危动作（ 提示：悟空忙碌中，请稍后再试）
    private int ERROR_ROBOT_FALL_CLIMB = 6;//跌倒爬起（ 提示：悟空忙碌中，请稍后再试）
    private int ERROR_ROBOT_STAND_BY = 7;//休眠状态（ 提示：悟空休息中，请唤醒后再试）
    public int fordReasonHandler(CallException e) {
        int errorCode = ERROR_UNKNOWN_ERROR;
        if (e.getCode() == CallGlobalCode.FORBIDDEN && e.getSubCode() == MasterSubCode.FORBIDDEN_TO_START_SKILL) {
            try {

                SkillOpponent skillOpponent = ParcelableParam.from(e.getParam(), SkillOpponent.class).getParcelable();
                List<ComponentBaseInfo> skillList = skillOpponent.getSkillList();
                List<Pair<ComponentBaseInfo, String>> stateList = skillOpponent.getServiceStateList();
                List<Pair<ComponentBaseInfo, String>> skillStateList = skillOpponent.getSkillStateList();

                //优先判断状态冲突、再判断skill冲突
                boolean hasFoundReason = false;
                if (stateList != null && stateList.size() > 0) {
                    for (Pair<ComponentBaseInfo, String> pair : stateList) {
                        if (pair != null && pair.second != null) {
                            if (pair.second.equals("lowPower")) {//低电量
                                LogUtils.w("low power can not run codemao skill");
                                hasFoundReason = true;
                                errorCode = ERROR_CODE_LOW_POWER;
                            }
                        }
                    }
                }

                if (!hasFoundReason) {
                    if (skillList != null && skillList.size() > 0) {
                        LogUtils.w("当前正在运行--" + skillList.get(0).getName() );
                        String skillName = null;
                        for (ComponentBaseInfo skillComponent : skillList) {
                            if (skillComponent != null && skillComponent.getName() != null) {
                                skillName = skillComponent.getName();
                                if (!TextUtils.isEmpty(skillName)) {
                                    break;
                                }
                            }
                        }
                        switch (skillName) {
                            case "phone_call"://阿凡达
                                errorCode = ERROR_ROBOT_PHONE_CALL;
                                break;
                            case "codemaoagent"://重复启动
                                errorCode = ERROR_ROBOT_BUSY;
                                break;
                            case "avatar"://阿凡达
                                errorCode = ERROR_ROBOT_AVATAR;
                                break;

                            case "speech_highrisk_actor"://阿凡达
                                errorCode = ERROR_ROBOT_HIGH_RISK_ACTION;
                                break;
                            case "fallclimbagent"://阿凡达
                                errorCode = ERROR_ROBOT_FALL_CLIMB;
                                break;
                            case "standby"://阿凡达
                                errorCode = ERROR_ROBOT_STAND_BY;
                                break;
                        }

                        hasFoundReason = true;
                    }
                }

            } catch (ParcelableParam.InvalidParcelableParamException e1) {
                e1.printStackTrace();
            }
        }else {
            errorCode = e.getCode();
        }

        return errorCode;
    }

}
