package com.ubtechinc.alpha.service.jimucar;

import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.ubtechinc.alpha.service.jimucar.handler.CheckCarHandler;
import com.ubtechinc.alpha.service.jimucar.handler.ControlCarHandler;
import com.ubtechinc.alpha.service.jimucar.handler.JimuCarResponder;
import com.ubtechinc.alpha.service.jimucar.handler.ReadCarPowerHandler;
import com.ubtechinc.alpha.service.jimucar.handler.ReadIrDistanceHandler;
import com.ubtechinc.alpha.service.jimucar.handler.ReadMainBoardHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import io.netty.buffer.ByteBufUtil;

import static com.ubtechinc.alpha.service.jimucar.JimuCarSkill.TAG;

/**
 * @author : kevin.liu@ubtrobot.com
 * @description :
 * @date : 2018/7/4
 * @modifier :
 * @modify time :
 */
public class JimuCarNotifyCallbackDispatcher {

    private volatile static BlePacket mMultiPacket;

    private volatile static ConcurrentHashMap<String, List<JimuCarResponder>> responders = new ConcurrentHashMap();

    public static synchronized void registerResponder(byte cmd, JimuCarResponder responder) {
        final String key = ByteBufUtil.hexDump(new byte[]{cmd});
        List<JimuCarResponder> responderList = JimuCarNotifyCallbackDispatcher.responders.get(key);
        if (responderList == null) {
            responderList = new ArrayList<>();
            JimuCarNotifyCallbackDispatcher.responders.put(key, responderList);
        }
        responderList.add(responder);
    }

    public static synchronized void unregisterResponder(byte cmd, JimuCarResponder responder) {
        final String key = ByteBufUtil.hexDump(new byte[]{cmd});
        List<JimuCarResponder> responderList = JimuCarNotifyCallbackDispatcher.responders.get(key);
        if (responderList == null) {
            return;
        }
        responderList.remove(responder);
    }

    public static synchronized void unregisterAllResponders() {
        try {
            responders.clear();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void registerNotifyCallback(BleDevice bleDevice) {
        BleManager.getInstance().notify(bleDevice, JimuConstants.UUID_SERVICE,
                JimuConstants.UUID_READ_CHARACTERISTIC, new JimuCarNotifyCallback());
    }

    private static synchronized BlePacket getMultiPacket() {
        return mMultiPacket;
    }

    private static synchronized void setMultiPacket(BlePacket packet) {
        mMultiPacket = packet;
    }

    private static class JimuCarNotifyCallback extends BleNotifyCallback {

        @Override
        public void onNotifySuccess() {
            Log.d(TAG, "onNotifySuccess");
        }

        @Override
        public void onNotifyFailure(BleException e) {
            Log.d(TAG, "onNotifyFailure");
        }

        @Override
        public void onCharacteristicChanged(byte[] raw) {
            decodeRaw(raw);
//            mWorker.execute(new Runnable() {
//                @Override
//                public void run() {
//                    decodeRaw(raw);
//                }
//            });
        }

        private void decodeRaw(byte[] raw) {
            if (raw != null && raw.length > 0) {
                Log.d(TAG, "onCharacteristicChanged:raw==== " + ByteBufUtil.hexDump(raw));
                if (BlePacket.MODE == BlePacket.PACKET_MODE.SINGLE) {
                    final BlePacket blePacket = BlePacket.get();
                    blePacket.wrap(raw); //这里有可能改变MODE
                    if (BlePacket.MODE == BlePacket.PACKET_MODE.MULTIPLE) {
                        setMultiPacket(blePacket);
                    } else {
                        final byte cmd = blePacket.getCmd().readByte();
                        final List<JimuCarResponder> jimuCarResponders = responders.get(ByteBufUtil.hexDump(new byte[]{cmd}));
                        if (jimuCarResponders != null && jimuCarResponders.size() != 0) {
                            switch (cmd) {
                                case JimuConstants.CMD_READ_SENSOR_DATA:
                                    ReadIrDistanceHandler.get().onBleResponse(blePacket, jimuCarResponders);
                                    break;
                                case JimuConstants.CMD_READ_POWER_INFO:
                                    ReadCarPowerHandler.get().onBleResponse(blePacket, jimuCarResponders);
                                    break;
                                case JimuConstants.CMD_SELF_INSPECTION:
                                    CheckCarHandler.get().onBleResponse(blePacket, jimuCarResponders);
                                    break;
                                case JimuConstants.CMD_CHECK_SENSOR:
                                    CheckCarHandler.get().onBleResponse(blePacket, jimuCarResponders);
                                    break;
                                case JimuConstants.CMD_CONTROL_MOTOR:
                                    ControlCarHandler.get().onBleResponse(blePacket, jimuCarResponders);
                                    break;
                                case JimuConstants.CMD_CONTROL_SEVRO_ANGLE:
                                    ControlCarHandler.get().onBleResponse(blePacket, jimuCarResponders);
                                    break;
                            }
                        }
                        setMultiPacket(null);
                    }
                    Log.d(TAG, "onCharacteristicChanged:blePacket==== " + blePacket.getHexString());
                } else {
                    if (getMultiPacket() != null) {
                        getMultiPacket().wrap(raw);
                        //组包完成
                        if (getMultiPacket().isCompletedPacket()) {
                            final byte cmd = getMultiPacket().getCmd().readByte();
                            final List<JimuCarResponder> jimuCarResponders = responders.get(ByteBufUtil.hexDump(new byte[]{cmd}));
                            switch (cmd) {
                                case JimuConstants.CMD_READ_MAINBOARD_INFO:
                                    // jimuCarResponders 可能为空，但是主板信息需要先注入。
                                    ReadMainBoardHandler.get().onBleResponse(getMultiPacket(), jimuCarResponders);
                                    break;
                                case JimuConstants.CMD_SELF_INSPECTION:
                                    if (jimuCarResponders != null && jimuCarResponders.size() != 0) {
                                        CheckCarHandler.get().onBleResponse(getMultiPacket(), jimuCarResponders);
                                    }
                                    break;
                            }
                        }
                        Log.d(TAG, "onCharacteristicChanged:blePacket==== mMultiPacket===" + getMultiPacket().getHexString());
                    }

                }

            }
        }

    }

//    static final ExecutorService mWorker = Executors.newSingleThreadExecutor();
}
