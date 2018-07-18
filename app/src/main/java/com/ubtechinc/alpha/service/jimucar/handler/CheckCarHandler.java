package com.ubtechinc.alpha.service.jimucar.handler;

import android.util.Log;

import com.google.protobuf.BytesValue;
import com.ubtechinc.alpha.JimuCarCheck;
import com.ubtechinc.alpha.JimuErrorCodeOuterClass;
import com.ubtechinc.alpha.service.jimucar.BlePacket;
import com.ubtechinc.alpha.service.jimucar.JimuCarNotifyCallbackDispatcher;
import com.ubtechinc.alpha.service.jimucar.JimuCarPresenter;
import com.ubtechinc.alpha.service.jimucar.JimuConstants;
import com.ubtrobot.master.param.ProtoParam;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;

/**
 * @author : kevin.liu@ubtrobot.com
 * @description :
 * @date : 2018/7/5
 * @modifier :
 * @modify time :
 */
public class CheckCarHandler implements IBleHandler {
    public volatile static AtomicInteger mCounter = new AtomicInteger();
    private static final String TAG = CheckCarHandler.class.getName();

    public static CheckCarHandler get() {
        return new CheckCarHandler();
    }

    @Override
    public synchronized void onBleResponse(BlePacket packet, List<JimuCarResponder> responder) {
        final List<JimuCarResponder> jimuCarResponders = Collections.unmodifiableList(responder);
        if (mCounter.incrementAndGet() > 2) {
            mCounter.set(1);
        }
        if (mCounter.get() == 1) {
            carCheckBuilder = JimuCarCheck.checkCarResponse.newBuilder();
        }
        final ByteBuf byteBuf = packet.getParams();
        if (packet.getCmd().readByte() == JimuConstants.CMD_SELF_INSPECTION) {

            final byte[] type = BytesReader.read(byteBuf, 1);
            if (type != null && type.length == 1) {
                switch (type[0]) {
                    case 0x00:
                        Log.d(TAG, "设置成功");
                        isCarProblem.set(false);
                        break;
                    case 0x01:
                        Log.d(TAG, "电量异常");
                        isCarProblem.set(true);
                        carCheckBuilder.addErrorModelIds(type[0]);
                        break;
                    case 0x02:
                        Log.d(TAG, "舵机有问题");
                        isCarProblem.set(true);
                        carCheckBuilder.addErrorModelIds(type[0]);
                        break;
                    case 0x03:
                        Log.d(TAG, "舵机版本不一致");
                        isCarProblem.set(true);
                        carCheckBuilder.addErrorModelIds(type[0]);
                        break;
                    case 0x04:
                        Log.d(TAG, "马达有问题");
                        isCarProblem.set(true);
                        carCheckBuilder.addErrorModelIds(type[0]);
                        break;
                    case 0x05:
                        Log.d(TAG, "马达版本不一致");
                        isCarProblem.set(true);
                        carCheckBuilder.addErrorModelIds(type[0]);
                        break;
                }
                carCheckBuilder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.REQUEST_SUCCESS);
            }

        } else {
            final byte[] sensorType = BytesReader.read(byteBuf, 1);
            if (sensorType != null && sensorType[0] == JimuConstants.SENSOR_TYPE_IR) {
                BytesReader.skip(byteBuf, 1);
                final byte[] ret = BytesReader.read(byteBuf, 1);
                if (ret != null && ret[0] == 0xEE) {
                    carCheckBuilder.addErrorModelIds(0x06);
                }
            }
        }

        if (mCounter.get() == 2) {
            // 组包完成
            final JimuCarCheck.checkCarResponse checkCarResponse = carCheckBuilder.build();
            final ProtoParam<BytesValue> param = ProtoParam.create(BytesValue.newBuilder().setValue(checkCarResponse.toByteString()).build());
            try {
                for (JimuCarResponder r :
                        jimuCarResponders) {
                    r.responder().respondSuccess(param);
                    JimuCarNotifyCallbackDispatcher.unregisterResponder(JimuConstants.CMD_SELF_INSPECTION,r);
                    JimuCarNotifyCallbackDispatcher.unregisterResponder(JimuConstants.CMD_CHECK_SENSOR,r);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            JimuCarPresenter.get().setCheckCarResponse(checkCarResponse);
        }
    }

    public volatile static JimuCarCheck.checkCarResponse.Builder carCheckBuilder;
    public volatile static AtomicBoolean isCarProblem = new AtomicBoolean(false);
}
