package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;

import com.google.protobuf.BytesValue;
import com.google.protobuf.InvalidProtocolBufferException;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.JimuCarCheck;
import com.ubtechinc.alpha.JimuCarListenType;
import com.ubtechinc.alpha.JimuErrorCodeOuterClass;
import com.ubtechinc.alpha.im.IMCmdId;
import com.ubtechinc.alpha.im.RequestParseUtils;
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
public class JimuCarCheckHandler implements IMsgHandler {

    private static volatile long mRequestSerial;
    public volatile static String mPeer;
    public volatile static int mResponseCmdId;

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        Log.d("msgHandler", "JimuCarCheckHandler:");
        mRequestSerial = request.getHeader().getSendSerial();
        mPeer = peer;
        mResponseCmdId = responseCmdId;

        final JimuCarCheck.checkCarRequest carRequest = RequestParseUtils.getRequestClass(request, JimuCarCheck.checkCarRequest.class);
        final JimuCarListenType.listenType listenType = carRequest.getListenType();

        doCheck(mRequestSerial);
    }

    public void doCheck(long requestSerial) {
        final JimuCarCheck.checkCarResponse.Builder errorbuilder = JimuCarCheck.checkCarResponse.newBuilder();
        SkillUtils.getSkill().call("/jimucar/check_car", new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {
                Log.d(JimuCarSkill.TAG, "JimuCarCheckHandler onResponse");
                try {
                    final BytesValue bytesValue = ProtoParam.from(response.getParam(), BytesValue.class).getProtoMessage();
                    try {
                        final JimuCarCheck.checkCarResponse checkCarResponse = JimuCarCheck.checkCarResponse.parseFrom(bytesValue.getValue());
                        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(mResponseCmdId, IMCmdId.IM_VERSION, requestSerial, checkCarResponse, mPeer, null);
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(mResponseCmdId, IMCmdId.IM_VERSION, requestSerial, errorbuilder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), mPeer, null);
                    }
                } catch (ProtoParam.InvalidProtoParamException e) {
                    e.printStackTrace();
                    RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(mResponseCmdId, IMCmdId.IM_VERSION, requestSerial, errorbuilder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), mPeer, null);
                }

            }

            @Override
            public void onFailure(Request request, CallException e) {
                RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(mResponseCmdId, IMCmdId.IM_VERSION, requestSerial, errorbuilder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), mPeer, null);
            }
        });
    }

    public void doCheckCar(ResponseCallback callback) {
        SkillUtils.getSkill().call("/jimucar/check_car", callback);
    }

    public static JimuCarCheckHandler get() {
        return new JimuCarCheckHandler();
    }
}
