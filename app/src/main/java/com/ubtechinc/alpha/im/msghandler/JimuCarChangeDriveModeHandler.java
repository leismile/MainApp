package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;

import com.google.protobuf.BytesValue;
import com.google.protobuf.InvalidProtocolBufferException;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.JimuCarDriveMode;
import com.ubtechinc.alpha.JimuErrorCodeOuterClass;
import com.ubtechinc.alpha.im.IMCmdId;
import com.ubtechinc.alpha.im.RequestParseUtils;
import com.ubtechinc.alpha.service.jimucar.JimuCarPresenter;
import com.ubtechinc.alpha.service.jimucar.JimuCarSkill;
import com.ubtechinc.alpha.utils.SkillUtils;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;

/**
 * @author : kevin.liu@ubtrobot.com
 * @description :
 * @date : 2018/6/29
 * @modifier :
 * @modify time :
 */
public final class JimuCarChangeDriveModeHandler implements IMsgHandler {

    public static volatile long mRequestSerial;
    public volatile static String mPeer;
    public volatile static int mResponseCmdId;


    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        Log.d("msgHandler", "JimuCarChangeDriveModeHandler:");
        JimuCarPresenter.get().setPeer(peer);
        mRequestSerial = request.getHeader().getSendSerial();
        mPeer = peer;
        mResponseCmdId = responseCmdId;

        final JimuCarDriveMode.ChangeJimuDriveModeRequest jimuDriveModeRequest = RequestParseUtils.getRequestClass(request, JimuCarDriveMode.ChangeJimuDriveModeRequest.class);
        final JimuCarDriveMode.ChangeJimuDriveModeResponse.Builder errorBuilder = JimuCarDriveMode.ChangeJimuDriveModeResponse.newBuilder();
        if (jimuDriveModeRequest == null) {
            RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, mRequestSerial, errorBuilder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.REQUEST_PARAMS_ERROR).build(), peer, null);
            return;
        }

        changeDriveMode(jimuDriveModeRequest, errorBuilder);
    }

    private void changeDriveMode(JimuCarDriveMode.ChangeJimuDriveModeRequest jimuDriveModeRequest, JimuCarDriveMode.ChangeJimuDriveModeResponse.Builder builder) {
        final JimuCarDriveMode.DriveMode driveMode = jimuDriveModeRequest.getDriveMode();

        if (driveMode == JimuCarDriveMode.DriveMode.ENTER) {
            enterDriveMode(new ResponseCallback() {
                @Override
                public void onResponse(Request request, Response response) {
                    Log.d(JimuCarSkill.TAG, "enter_drive");
                    responderSuccess(response, mResponseCmdId, mRequestSerial, mPeer, builder);
                }

                @Override
                public void onFailure(Request request, CallException e) {
                    Log.d(JimuCarSkill.TAG, "onFailure:" + e.getMessage());
                    responderFailure(mResponseCmdId, mRequestSerial, builder, mPeer);
                }
            });

        } else {
            quitDriveMode(new ResponseCallback() {
                @Override
                public void onResponse(Request request, Response response) {
                    Log.d(JimuCarSkill.TAG, "quit_drive");
                    responderSuccess(response, mResponseCmdId, mRequestSerial, mPeer, builder);
                    JimuCarRobotChatHandler.get().playTTs("退出开车模式!", null);
                }

                @Override
                public void onFailure(Request request, CallException e) {
                    Log.d(JimuCarSkill.TAG, "onFailure:" + e.getMessage());
                    responderFailure(mResponseCmdId, mRequestSerial, builder, mPeer);
                }
            });
        }
    }

    public void enterDriveMode(ResponseCallback callback) {
        SkillUtils.getSkill().call("/jimucar/enter_drive", callback);
    }


    public void quitDriveMode(ResponseCallback callback) {
        SkillUtils.getSkill().call("/jimucar/quit_drive", callback);
    }

    private void responderFailure(int responseCmdId, long requestSerial, JimuCarDriveMode.ChangeJimuDriveModeResponse.Builder errorBuilder, String peer) {
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, requestSerial, errorBuilder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), peer, null);
    }

    private void responderSuccess(Response response, int responseCmdId, long requestSerial, String peer, JimuCarDriveMode.ChangeJimuDriveModeResponse.Builder errorBuilder) {
        try {
            final BytesValue bytesValue = ProtoParam.from(response.getParam(), BytesValue.class).getProtoMessage();
            final JimuCarDriveMode.ChangeJimuDriveModeResponse changeJimuDriveModeResponse = JimuCarDriveMode.ChangeJimuDriveModeResponse.parseFrom(bytesValue.getValue());
            RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, requestSerial, changeJimuDriveModeResponse, peer, null);
        } catch (ProtoParam.InvalidProtoParamException e) {
            e.printStackTrace();
            RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, requestSerial, errorBuilder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), peer, null);
            return;
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, requestSerial, errorBuilder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), peer, null);
            return;
        }
    }

    public static JimuCarChangeDriveModeHandler get() {
        return new JimuCarChangeDriveModeHandler();
    }

}
