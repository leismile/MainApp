package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;

import com.google.protobuf.Int32Value;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.JimuCarGetIRDistance;
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
public class JimuCarIRDistanceHandler implements IMsgHandler {

    private static volatile long mRequestSerial;
    public volatile static String mPeer;
    public volatile static int mResponseCmdId;


    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        Log.d("msgHandler", "JimuCarIRDistanceHandler:");
        mRequestSerial = request.getHeader().getSendSerial();
        mPeer = peer;
        mResponseCmdId = responseCmdId;

        doGetIrDistance(mRequestSerial);
    }

    public void getIrDistance(ResponseCallback callback) {
        SkillUtils.getSkill().call("/jimucar/get_ir_distance", callback);
    }

    public void doGetIrDistance(final long requestSerial) {
        final JimuCarGetIRDistance.JimuCarGetIRDistanceResponse.Builder builder = JimuCarGetIRDistance.JimuCarGetIRDistanceResponse.newBuilder();
        getIrDistance(new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {
                try {
                    final int distance = ProtoParam.from(response.getParam(), Int32Value.class).getProtoMessage().getValue();
                    Log.d("msgHandler", "JimuCarIRDistanceHandler:  " + distance);
                    builder.setDistance(distance);
                    builder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.REQUEST_SUCCESS);
                    RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(mResponseCmdId, IMCmdId.IM_VERSION, requestSerial, builder.build(), mPeer, null);
                } catch (ProtoParam.InvalidProtoParamException e) {
                    e.printStackTrace();
                    RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(mResponseCmdId, IMCmdId.IM_VERSION, requestSerial, builder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), mPeer, null);
                }
            }

            @Override
            public void onFailure(Request request, CallException e) {
                Log.d("msgHandler", e.getLocalizedMessage());
                RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(mResponseCmdId, IMCmdId.IM_VERSION, requestSerial, builder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), mPeer, null);
            }
        });
    }

    public static JimuCarIRDistanceHandler get() {
        return new JimuCarIRDistanceHandler();
    }
}
