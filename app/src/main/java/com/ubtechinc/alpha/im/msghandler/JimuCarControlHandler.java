package com.ubtechinc.alpha.im.msghandler;

import android.text.TextUtils;
import android.util.Log;

import com.google.protobuf.BytesValue;
import com.google.protobuf.InvalidProtocolBufferException;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.JimuCarControl;
import com.ubtechinc.alpha.JimuErrorCodeOuterClass;
import com.ubtechinc.alpha.im.IMCmdId;
import com.ubtechinc.alpha.utils.SkillUtils;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;

import static com.ubtechinc.alpha.im.RequestParseUtils.getRequestClass;

/**
 * @author : kevin.liu@ubtrobot.com
 * @description :
 * @date : 2018/6/29
 * @modifier :
 * @modify time :
 */
public class JimuCarControlHandler implements IMsgHandler {

    private static volatile long mRequestSerial;
    public volatile static String mPeer;
    public volatile static int mResponseCmdId;

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        Log.d("msgHandler", "JimuCarControlHandler:");
        mRequestSerial = request.getHeader().getSendSerial();
        mPeer = peer;
        mResponseCmdId = responseCmdId;

        final JimuCarControl.JimuCarControlRequest controlRequest = getRequestClass(request, JimuCarControl.JimuCarControlRequest.class);
        final JimuCarControl.Control cmd = controlRequest.getCmd();
        final JimuCarControl.JimuCarControlResponse.Builder errorBuilder = JimuCarControl.JimuCarControlResponse.newBuilder();
        String callPath = "";
        switch (cmd) {
            case CAR_FORWARD:
                callPath = "/jimucar/go_forward";
                break;
            case CAR_BACK:
                callPath = "/jimucar/go_back";
                break;
            case CAR_LEFT:
                callPath = "/jimucar/turn_left";
                break;
            case CAR_RIGHT:
                callPath = "/jimucar/turn_right";
                break;
            case CAR_STOP:
                callPath = "/jimucar/stop_going";
                break;
            case CAR_RESET_DIRECTION:
                callPath = "/jimucar/reset_car_direction";
                break;
        }
        if(TextUtils.isEmpty(callPath)){
            RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, mRequestSerial, errorBuilder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), peer, null);
            return;
        }
        SkillUtils.getSkill().call(callPath, new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {
                Log.d("msgHandler", "control path::" + request.getPath());
                try {
                    final BytesValue bytesValue = ProtoParam.from(response.getParam(), BytesValue.class).getProtoMessage();
                    try {
                        final JimuCarControl.JimuCarControlResponse ret = JimuCarControl.JimuCarControlResponse.parseFrom(bytesValue.getValue());
                        final JimuCarControl.JimuCarControlResponse controlResponse = JimuCarControl.JimuCarControlResponse.newBuilder(ret).setCmd(cmd).setErrorCode(ret.getErrorCode()).build();
                        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, mRequestSerial, controlResponse, peer, null);

                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, mRequestSerial, errorBuilder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), peer, null);
                    }
                } catch (ProtoParam.InvalidProtoParamException e) {
                    e.printStackTrace();
                    RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, mRequestSerial, errorBuilder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), peer, null);
                }

            }

            @Override
            public void onFailure(Request request, CallException e) {
                RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, mRequestSerial, errorBuilder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), peer, null);
            }
        });

    }
}
