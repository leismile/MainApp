package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageLite;
import com.ubtech.utilcode.utils.LogUtils;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmReadMotorAngle;
import com.ubtechinc.nets.http.ThrowableWrapper;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ICallback;
import com.ubtechinc.nets.phonerobotcommunite.ProtoBufferDispose;
import com.ubtrobot.motor.MotorApi;

/**
 * @desc : 读取舵机角度的消息处理器
 * @author: wzt
 * @time : 2017/6/1
 * @modifier:
 * @modify_time:
 */

public class ReadMotorAngleMsgHandler implements IMsgHandler {
    static final String TAG = "ReadMotorAngleMsgHandler";

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {

        ByteString requestBody = request.getBodyData();
        byte[] bodyBytes = requestBody.toByteArray();
        long requestSerial = request.getHeader().getSendSerial();
        CmReadMotorAngle.CmReadMotorAngleRequest readMotorAngle = (CmReadMotorAngle.CmReadMotorAngleRequest) ProtoBufferDispose.unPackData(
                CmReadMotorAngle.CmReadMotorAngleRequest.class, bodyBytes);
        LogUtils.i("request body : read motorID = " + readMotorAngle.getMotorID());


        int angle = MotorApi.get().readAbsoluteAngle(readMotorAngle.getMotorID(), readMotorAngle.getAdcump());

        Log.w("Logic", "responseId === " + responseCmdId + ", id == " + readMotorAngle.getMotorID() + ", angle =========" + angle);

        CmReadMotorAngle.CmReadMotorAngleResponse responseBody;
        CmReadMotorAngle.CmReadMotorAngleResponse.Builder builder = CmReadMotorAngle.CmReadMotorAngleResponse.newBuilder();

        builder.setMotorID(readMotorAngle.getMotorID());
        builder.setAngle(angle);
        builder.setTime(0);
        responseBody = builder.build();

        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, "1", requestSerial, responseBody, peer, new ICallback<GeneratedMessageLite>() {

            @Override
            public void onSuccess(GeneratedMessageLite data) {
                LogUtils.i("onStartSuccess---");
            }

            @Override
            public void onError(ThrowableWrapper e) {
                LogUtils.i("onError---code");
            }

        });

    }
}
