package com.ubtechinc.alpha.service.jimucar.handler;

import android.util.Log;

import com.ubtechinc.alpha.service.jimucar.BlePacket;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import static com.ubtechinc.alpha.service.jimucar.handler.BytesReader.skip;

/**
 * @author : kevin.liu@ubtrobot.com
 * @description :
 * @date : 2018/7/5
 * @modifier :
 * @modify time :
 * <p>
 * 传感器ID 都是 2 位变量 表示
 */
public class ReadMainBoardHandler implements IBleHandler {
    private static final String TAG = ReadMainBoardHandler.class.getName();

    public static ReadMainBoardHandler get() {
        return new ReadMainBoardHandler();
    }

    @Override
    public synchronized void onBleResponse(BlePacket packet, List<JimuCarResponder> responder) {
        final ByteBuf byteBuf = packet.getParams();
        mMainBoard = new MainBoard();
        readMainVersion(byteBuf);
        readPower(byteBuf);
        //舵机
        readServoIds(byteBuf);
        readErrorServoIds(byteBuf);
        readServoVersion(byteBuf);
        skip(byteBuf, 4);//版本号不一致的ID 4B
        skip(byteBuf, 1);//外部Flash的容量
        //红外
        readIrIds(byteBuf);//红外模块的ID 1B
        readErrorIrId(byteBuf);//异常红外模块的ID 1B
        readIrVersion(byteBuf);//红外模块版本信息 4B
        skip(byteBuf, 1);//红外模块版本不一致的ID 1B
        skip(byteBuf, 7 * 12);
        //马达
        readMotorIds(byteBuf);

    }

    private void readMotorIds(ByteBuf byteBuf) {
        final byte[] ids = BytesReader.read(byteBuf, 1);
        if (checkBytes(ids, 1)) {
            final String motorBit = ByteBufUtil.hexDump(ids);
            Log.d(TAG, "motor ids raw:" + motorBit);
            final int motorCount = motorBit.length();
            mMainBoard.motorIds = new byte[motorCount];
            for (int i = 0; i < motorCount; i++) {
                mMainBoard.motorIds[i] = Byte.parseByte(motorBit.substring(i, i + 1));
                Log.d(TAG, "motor id" + i + ":" + mMainBoard.motorIds[i]);
            }
        }
    }

    private void readIrVersion(ByteBuf byteBuf) {
        final byte[] version = BytesReader.read(byteBuf, 4);
        if (checkBytes(version, 4)) {
            Log.d(TAG, "ir version:" + ByteBufUtil.hexDump(version));
        }
    }

    private void readIrIds(ByteBuf byteBuf) {
        final byte[] ids = BytesReader.read(byteBuf, 1);
        if (checkBytes(ids, 1)) {
            final String irBit = ByteBufUtil.hexDump(ids);
            Log.d(TAG, "ir ids raw:" + irBit);
            final int irCount = irBit.length();
            mMainBoard.IrIds = new byte[irCount];
            for (int i = 0; i < irCount; i++) {
                mMainBoard.IrIds[i] = Byte.parseByte(irBit.substring(i, i + 1));
                Log.d(TAG, "ir id" + i + ":" + mMainBoard.IrIds[i]);
            }
        }
    }

    private void readErrorIrId(ByteBuf byteBuf) {
        final byte[] ids = BytesReader.read(byteBuf, 1);
        final String irBit = ByteBufUtil.hexDump(ids);
        Log.d(TAG, "ir error ids raw:" + irBit);
        final int irCount = irBit.length();
        mMainBoard.errorIrIds = new byte[irCount];
        for (int i = 0; i < irCount; i++) {
            mMainBoard.errorIrIds[i] = Byte.parseByte(irBit.substring(i, i + 1));
            Log.d(TAG, "ir error id" + i + ":" + mMainBoard.errorIrIds[i]);
        }
    }

    private void readServoIds(ByteBuf byteBuf) {
        final byte[] ids = BytesReader.read(byteBuf, 4);
        if (checkBytes(ids, 4)) {
            final String idsStr = ByteBufUtil.hexDump(ids);
            Log.d(TAG, "Servo raw:" + idsStr);
            final int length = idsStr.length();
            mMainBoard.servoIds = new byte[length];
            for (int i = 0; i < length; i++) {
                mMainBoard.servoIds[i] = Byte.parseByte(idsStr.substring(i, i + 1));
                Log.d(TAG, "servo id" + i + ":" + mMainBoard.servoIds[i]);
            }
        }

    }

    private void readErrorServoIds(ByteBuf byteBuf) {
        final byte[] ids = BytesReader.read(byteBuf, 4);
        if (checkBytes(ids, 4)) {
            Log.d(TAG, "error Servo raw:" + ByteBufUtil.hexDump(ids));
        }
    }

    private void readServoVersion(ByteBuf byteBuf) {
        final byte[] version = BytesReader.read(byteBuf, 4);
        if (checkBytes(version, 4)) {
            Log.d(TAG, "Servo version raw:" + ByteBufUtil.hexDump(version));
        }
    }

    private void readPower(ByteBuf byteBuf) {
        final byte[] power = BytesReader.read(byteBuf, 1);
        if (checkBytes(power, 1)) {
            float real_power = (power[0] & 0xff) / 10;//电池电量：需要除以10
            mMainBoard.power = real_power;
            Log.d(TAG, "power:" + real_power);
        }
    }

    private void readMainVersion(ByteBuf byteBuf) {
        final byte[] mainVersion = BytesReader.read(byteBuf, 10);
        if (checkBytes(mainVersion, 10)) {
            mMainBoard.mainVersion = ByteBufUtil.hexDump(mainVersion);
            Log.d(TAG, "mainVersion:" + mMainBoard.mainVersion);
        }
    }

    private boolean checkBytes(byte[] bytes, int length) {
        return bytes != null && bytes.length == length;
    }


    public final static MainBoard getMainBoard() {
        return mMainBoard;
    }

    private volatile static MainBoard mMainBoard = new MainBoard();

    public static class MainBoard {
        String mainVersion;
        float power = -1;
        byte[] IrIds;
        byte[] errorIrIds;

        byte[] motorIds;
        byte[] errorMotorIds;

        byte[] servoIds;

        public byte getFirstNonullIrId() {
            if (IrIds == null || IrIds.length == 0) {
                return 0x01;
            }
            for (byte id :
                    IrIds) {
                if (id != 0x00)
                    return id;
            }
            return 0x01;
        }

        public byte getFirstNonullServoId() {
            if (servoIds == null || servoIds.length == 0) {
                return 0x00;
            }
            for (byte id :
                    servoIds) {
                if (id != 0x00) {
                    return id;
                }
            }
            return 0x00;
        }
    }


}
