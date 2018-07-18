package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;

import com.google.protobuf.BytesValue;
import com.google.protobuf.InvalidProtocolBufferException;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.JimuCarConnectBleCar;
import com.ubtechinc.alpha.JimuCarQueryConnectState;
import com.ubtechinc.alpha.JimuErrorCodeOuterClass;
import com.ubtechinc.alpha.im.IMCmdId;
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
public class JimuCarQueryConnectStateHandler implements IMsgHandler {

    private static volatile long mRequestSerial;
    public volatile static String mPeer;
    public volatile static int mResponseCmdId;

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        Log.d("msgHandler", "JimuCarQueryConnectStateHandler:");
        mRequestSerial = request.getHeader().getSendSerial();
        mPeer = peer;
        mResponseCmdId = responseCmdId;

        doGetConnectState(responseCmdId, peer);

    }

    private void doGetConnectState(int responseCmdId, String peer) {
        getConnectState(new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {
                try {
                    final BytesValue bytesValue = ProtoParam.from(response.getParam(), BytesValue.class).getProtoMessage();
                    try {
                        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(mResponseCmdId, IMCmdId.IM_VERSION, 0, JimuCarQueryConnectState.JimuCarQueryConnectStateResponse.parseFrom(bytesValue.getValue()), mPeer, null);
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, mRequestSerial, JimuCarQueryConnectState.JimuCarQueryConnectStateResponse.newBuilder().setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), peer, null);
                    }
                } catch (ProtoParam.InvalidProtoParamException e) {
                    e.printStackTrace();
                    RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, mRequestSerial, JimuCarQueryConnectState.JimuCarQueryConnectStateResponse.newBuilder().setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), peer, null);
                }

            }

            @Override
            public void onFailure(Request request, CallException e) {
                RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, mRequestSerial, JimuCarQueryConnectState.JimuCarQueryConnectStateResponse.newBuilder().setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), peer, null);
            }
        });
    }

    public JimuCarConnectBleCar.BleCarConnectState getCarConnectState(Response response) {
        try {
            return JimuCarQueryConnectState.JimuCarQueryConnectStateResponse.parseFrom(ProtoParam.from(response.getParam(), BytesValue.class).getProtoMessage().getValue()).getState();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void getConnectState(ResponseCallback callback) {
        SkillUtils.getSkill().call("/jimucar/get_connect_state", callback);
    }

    public static JimuCarQueryConnectStateHandler get() {
        return new JimuCarQueryConnectStateHandler();
    }

}
