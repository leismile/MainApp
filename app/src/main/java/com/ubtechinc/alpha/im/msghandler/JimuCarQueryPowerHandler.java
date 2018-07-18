package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;

import com.google.protobuf.BytesValue;
import com.google.protobuf.InvalidProtocolBufferException;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.JimuCarConnectBleCar;
import com.ubtechinc.alpha.JimuCarPower;
import com.ubtechinc.alpha.JimuErrorCodeOuterClass;
import com.ubtechinc.alpha.appmanager.UbtBatteryManager;
import com.ubtechinc.alpha.im.IMCmdId;
import com.ubtechinc.alpha.service.jimucar.JimuCarPresenter;
import com.ubtechinc.alpha.utils.SkillUtils;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.masterevent.protos.SysMasterEvent;
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
public class JimuCarQueryPowerHandler implements IMsgHandler {
    private static volatile long mRequestSerial;
    public volatile static String mPeer;
    public volatile static int mResponseCmdId;


    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        Log.d("msgHandler", "JimuCarQueryPowerHandler:");
        mRequestSerial = request.getHeader().getSendSerial();
        mPeer = peer;
        mResponseCmdId = responseCmdId;

        doGetPower(mRequestSerial);
    }

    private void doGetPower(long requestSerial) {
        final SysMasterEvent.BatteryStatusData batteryInfo = UbtBatteryManager.getInstance().getBatteryInfo();
        final int robotPowerPercentage = batteryInfo.getLevel();

        final JimuCarQueryConnectStateHandler jimuCarQueryConnectStateHandler = JimuCarQueryConnectStateHandler.get();
        jimuCarQueryConnectStateHandler.getConnectState(new ResponseCallback() {

            @Override
            public void onResponse(Request request, Response response) {
                if (JimuCarPresenter.get().getConnectState() != JimuCarConnectBleCar.BleCarConnectState.CONNECTED) {
                    Log.d("msgHandler", "only query robot power:" + robotPowerPercentage);
                    RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(mResponseCmdId, IMCmdId.IM_VERSION, requestSerial, JimuCarPower.GetJimuCarPowerResponse.newBuilder().setRobotPower(JimuCarPower.RobotPower.newBuilder().setPowerPercentage(robotPowerPercentage).build()).setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.REQUEST_SUCCESS).build(), mPeer, null);
                } else {
                    final JimuCarPower.GetJimuCarPowerResponse.Builder errorBuilder = JimuCarPower.GetJimuCarPowerResponse.newBuilder();
                    SkillUtils.getSkill().call("/jimucar/get_car_power", new ResponseCallback() {
                        @Override
                        public void onResponse(Request request, Response response) {
                            try {
                                final BytesValue bytesValue = ProtoParam.from(response.getParam(), BytesValue.class).getProtoMessage();
                                try {
                                    final JimuCarPower.GetJimuCarPowerResponse carPowerResponse = JimuCarPower.GetJimuCarPowerResponse.parseFrom(bytesValue.getValue());
                                    RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(mResponseCmdId, IMCmdId.IM_VERSION, requestSerial, JimuCarPower.GetJimuCarPowerResponse.newBuilder(carPowerResponse).setCarPower(carPowerResponse.getCarPower()).setRobotPower(JimuCarPower.RobotPower.newBuilder().setPowerPercentage(robotPowerPercentage).build()).setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.REQUEST_SUCCESS).build(), mPeer, null);
                                } catch (InvalidProtocolBufferException e) {
                                    e.printStackTrace();
                                    RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(mResponseCmdId, IMCmdId.IM_VERSION, requestSerial, errorBuilder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), mPeer, null);
                                }

                            } catch (ProtoParam.InvalidProtoParamException e) {
                                e.printStackTrace();
                                RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(mResponseCmdId, IMCmdId.IM_VERSION, requestSerial, errorBuilder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), mPeer, null);
                            }

                        }

                        @Override
                        public void onFailure(Request request, CallException e) {
                            RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(mResponseCmdId, IMCmdId.IM_VERSION, requestSerial, errorBuilder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), mPeer, null);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Request request, CallException e) {

            }
        });
    }

    public void doGetCarPower(ResponseCallback callback) {
        SkillUtils.getSkill().call("/jimucar/get_car_power", callback);
    }

    public static JimuCarQueryPowerHandler get() {
        return new JimuCarQueryPowerHandler();
    }
}
