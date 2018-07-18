package com.ubtechinc.alpha.service.jimucar.handler;

import android.util.Log;

import com.google.protobuf.Int32Value;
import com.ubtechinc.alpha.service.jimucar.BlePacket;
import com.ubtechinc.alpha.service.jimucar.JimuCarNotifyCallbackDispatcher;
import com.ubtechinc.alpha.service.jimucar.JimuCarPresenter;
import com.ubtechinc.alpha.service.jimucar.JimuConstants;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.transport.message.Responder;

import java.util.Collections;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

/**
 * @author : kevin.liu@ubtrobot.com
 * @description :
 * @date : 2018/7/5
 * @modifier :
 * @modify time :
 * <p>
 * 参数(nB)：总帧数（1B） + 当前帧（1B）+ 传感器种类数量（1B）+传感器类型（1B）+ErrID（1B）+传感器有效ID（1B）
 * +传感器数据（NB）+（传感器类型（1B）+ErrID（1B）+传感器有效ID（1B）+传感器数据（NB）。。。）
 */
public class ReadIrDistanceHandler implements IBleHandler {
    private static final String TAG = ReadIrDistanceHandler.class.getName();

    public static ReadIrDistanceHandler get() {
        return new ReadIrDistanceHandler();
    }

    @Override
    public synchronized void onBleResponse(BlePacket packet, List<JimuCarResponder> responder) {
        final List<JimuCarResponder> jimuCarResponders = Collections.unmodifiableList(responder);
        final ByteBuf byteBuf = packet.getParams();
        BytesReader.skip(byteBuf, 5);
        final byte[] irIds = BytesReader.read(byteBuf, 1);
        Log.d(TAG, "有效ID：" + irIds[0]);
        final byte[] bytes = BytesReader.read(byteBuf, 2);
        if (bytes != null && bytes.length == 2) {
            Log.d(TAG, "hex:" + ByteBufUtil.hexDump(bytes));
            mDistance = ((bytes[0] << 8) & 0xff00) | (bytes[1] & 0xff);
            Log.d(TAG, "distance:" + mDistance);
            final ProtoParam<Int32Value> param = ProtoParam.create(Int32Value.newBuilder().setValue(mDistance).build());
            try {
                for (JimuCarResponder r :
                        jimuCarResponders) {
                    r.responder().respondSuccess(param);
                    JimuCarNotifyCallbackDispatcher.unregisterResponder(JimuConstants.CMD_READ_SENSOR_DATA,r);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            JimuCarPresenter.get().setIrDistance(mDistance);
        }
    }

    public volatile static int mDistance = 10000;

}
