package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;

import com.google.protobuf.BytesValue;
import com.google.protobuf.InvalidProtocolBufferException;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.JimuCarGetBleList;
import com.ubtechinc.alpha.JimuErrorCodeOuterClass;
import com.ubtechinc.alpha.im.IMCmdId;
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
public class JimuCarScanBleListHandler implements IMsgHandler {

    public volatile static long mRequestSerial;
    public volatile static String mPeer;
    public volatile static int mResponseCmdId;

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        Log.d("msgHandler", "JimuCarScanBleListHandler:");
        mRequestSerial = request.getHeader().getSendSerial();
        mPeer = peer;
        mResponseCmdId = responseCmdId;

        doScan(mRequestSerial);
    }

    private void doScan(long requestSerial) {
        final JimuCarGetBleList.GetJimuCarBleListResponse.Builder errorBuilder = JimuCarGetBleList.GetJimuCarBleListResponse.newBuilder();
        SkillUtils.getSkill().call("/jimucar/scan_ble_list", new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {
                Log.d(JimuCarSkill.TAG, "scan_ble_list");
                try {
                    final BytesValue bytesValue = ProtoParam.from(response.getParam(), BytesValue.class).getProtoMessage();
                    final JimuCarGetBleList.GetJimuCarBleListResponse carBleListResponse = JimuCarGetBleList.GetJimuCarBleListResponse.parseFrom(bytesValue.getValue());
                    RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(mResponseCmdId, IMCmdId.IM_VERSION, requestSerial, carBleListResponse, mPeer, null);
                } catch (ProtoParam.InvalidProtoParamException e) {
                    e.printStackTrace();
                    RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(mResponseCmdId, IMCmdId.IM_VERSION, requestSerial, errorBuilder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), mPeer, null);
                    return;
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                    RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(mResponseCmdId, IMCmdId.IM_VERSION, requestSerial, errorBuilder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), mPeer, null);
                    return;
                }

            }

            @Override
            public void onFailure(Request request, CallException e) {
                Log.d(JimuCarSkill.TAG, "onFailure:" + e.getMessage());
                RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(mResponseCmdId, IMCmdId.IM_VERSION, requestSerial, errorBuilder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), mPeer, null);
            }
        });
    }

    public void doScan(ResponseCallback callback) {
        SkillUtils.getSkill().call("/jimucar/scan_ble_list", callback);
    }

    public static JimuCarScanBleListHandler get() {
        return new JimuCarScanBleListHandler();
    }
}
