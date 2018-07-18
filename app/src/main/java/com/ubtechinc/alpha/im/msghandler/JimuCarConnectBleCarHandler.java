package com.ubtechinc.alpha.im.msghandler;

import android.text.TextUtils;
import android.util.Log;

import com.google.protobuf.BytesValue;
import com.google.protobuf.StringValue;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.JimuCarConnectBleCar;
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
public class JimuCarConnectBleCarHandler implements IMsgHandler {
    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        Log.d("msgHandler", "JImuCarConnectBleCarHandler:");
        long requestSerial = request.getHeader().getSendSerial();
        final JimuCarConnectBleCar.JimuCarConnectBleCarRequest bleCarRequest = RequestParseUtils.getRequestClass(request, JimuCarConnectBleCar.JimuCarConnectBleCarRequest.class);
        final JimuCarConnectBleCar.JimuCarConnectBleCarResponse.Builder errorBuilder = JimuCarConnectBleCar.JimuCarConnectBleCarResponse.newBuilder();
        if (bleCarRequest == null || TextUtils.isEmpty(bleCarRequest.getMac())) {
            RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, requestSerial, errorBuilder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.REQUEST_PARAMS_ERROR).build(), peer, null);
            return;
        }
        final ProtoParam<StringValue> param = ProtoParam.create(StringValue.newBuilder().setValue(bleCarRequest.getMac()).build());
        SkillUtils.getSkill().call("/jimucar/connect_car", param, new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {
                try {
                    final BytesValue bytesValue = ProtoParam.from(response.getParam(), BytesValue.class).getProtoMessage();
                    final JimuCarConnectBleCar.JimuCarConnectBleCarResponse jimuCarConnectBleCarResponse = JimuCarConnectBleCar.JimuCarConnectBleCarResponse.parseFrom(bytesValue.getValue());
                    Log.d("msgHandler", "JImuCarConnectBleCarHandler:" + jimuCarConnectBleCarResponse.getState().name());
                    RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, requestSerial, jimuCarConnectBleCarResponse, peer, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, requestSerial, errorBuilder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), peer, null);
                    return;
                }
            }

            @Override
            public void onFailure(Request request, CallException e) {
                Log.d(JimuCarSkill.TAG, "onFailure:" + e.getMessage());
                RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, requestSerial, errorBuilder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), peer, null);
            }
        });
    }
}
