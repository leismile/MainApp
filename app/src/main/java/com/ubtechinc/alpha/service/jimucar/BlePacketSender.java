package com.ubtechinc.alpha.service.jimucar;

import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;

import io.netty.buffer.ByteBufUtil;

import static com.ubtechinc.alpha.service.jimucar.JimuCarSkill.TAG;

/**
 * @author : kevin.liu@ubtrobot.com
 * @description :
 * @date : 2018/7/4
 * @modifier :
 * @modify time :
 */
public class BlePacketSender {

    public synchronized static void sendPacket(byte cmd, byte[] params, BleDevice bleDevice) {
        if (BleManager.getInstance().isConnected(bleDevice)) {
            // send shake hand cmd 2 ble car.
            final BlePacket blePacket = BlePacket.get();
            blePacket.wrap(cmd, params);
            BleManager.getInstance().write(bleDevice, JimuConstants.UUID_SERVICE, JimuConstants.UUID_WRITE_CHARACTERISTIC, blePacket.getBytes(), false, new BleWriteCallback() {
                @Override
                public void onWriteSuccess(int current, int total, byte[] justWrite) {
                    Log.d(TAG, "cmd:" + cmd +"====="+"onWriteSuccess current = " + current + " total = " + total + " write = " + ByteBufUtil.hexDump(justWrite));

                }

                @Override
                public void onWriteFailure(BleException e) {
                    Log.d(TAG, "cmd:" + cmd +"====="+ "onWriteFailure exception = " + e.getDescription());
                }
            });
        }
    }


}
