package com.ubtechinc.alpha.service.jimucar.handler;

import android.util.Log;

import com.google.protobuf.BytesValue;
import com.ubtechinc.alpha.JimuCarPower;
import com.ubtechinc.alpha.service.jimucar.BlePacket;
import com.ubtechinc.alpha.service.jimucar.JimuCarNotifyCallbackDispatcher;
import com.ubtechinc.alpha.service.jimucar.JimuCarPresenter;
import com.ubtechinc.alpha.service.jimucar.JimuConstants;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.transport.message.Responder;

import java.util.Collections;
import java.util.List;

import io.netty.buffer.ByteBuf;

/**
 * @author : kevin.liu@ubtrobot.com
 * @description :
 * @date : 2018/7/5
 * @modifier :
 * @modify time :
 */
public class ReadCarPowerHandler implements IBleHandler {
    private static final String TAG = ReadCarPowerHandler.class.getName();
    private JimuCarPower.CarPower.Builder mCarPowerBuilder;
    public static volatile JimuCarPower.CarPower mCarPower;

    public static ReadCarPowerHandler get() {
        return new ReadCarPowerHandler();
    }

    @Override
    public synchronized void onBleResponse(BlePacket packet, List<JimuCarResponder> responder) {
        final List<JimuCarResponder> jimuCarResponders = Collections.unmodifiableList(responder);
        if (jimuCarResponders == null || jimuCarResponders.size() == 0) {
            return;
        }
        final ByteBuf byteBuf = packet.getParams();
        mCarPowerBuilder = JimuCarPower.CarPower.newBuilder();
        readWithAdapter(byteBuf);
        readIsCharging(byteBuf);
        readCarPower(byteBuf);
        readCarPowerPercent(byteBuf);
        mCarPower = mCarPowerBuilder.build();
        Log.d(TAG, "carPower:" + mCarPower.toString());
        final BytesValue.Builder builder = BytesValue.newBuilder();
        final ProtoParam<BytesValue> param = ProtoParam.create(builder.setValue(mCarPower.toByteString()).build());
        try {
            for (JimuCarResponder r :
                    jimuCarResponders) {
                r.responder().respondSuccess(param);
                JimuCarNotifyCallbackDispatcher.unregisterResponder(JimuConstants.CMD_READ_POWER_INFO,r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JimuCarPresenter.get().setCarPower(mCarPower);
    }

    private void readCarPowerPercent(ByteBuf byteBuf) {
        final byte[] carPowerPercent = BytesReader.read(byteBuf, 1);
        if (carPowerPercent != null && carPowerPercent.length == 1) {
            mCarPowerBuilder.setPowerPercentage(carPowerPercent[0]);
        }
    }

    private void readCarPower(ByteBuf byteBuf) {
        final byte[] carPower = BytesReader.read(byteBuf, 1);
        if (carPower != null && carPower.length == 1) {
            mCarPowerBuilder.setPowerNum(carPower[0]);
        }
    }

    private void readWithAdapter(ByteBuf byteBuf) {
        final byte[] withAdapter = BytesReader.read(byteBuf, 1);//是否接了适配器（00：无外接适配器  01：有外接适配器）
    }

    private void readIsCharging(ByteBuf byteBuf) {
        final byte[] isCharging = BytesReader.read(byteBuf, 1);
        if (isCharging != null && isCharging.length == 1) {
            mCarPowerBuilder.setIsCharging(isCharging[0] == 0x00);
        }
    }


}
