package com.ubtechinc.alpha.service.jimucar;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author : kevin.liu@ubtrobot.com
 * @description :
 * @date : 2018/7/3
 * @modifier :
 * @modify time :
 * <p>
 * <pre>
 *      （无符号）
 *      字头(2B) +长度(1B) +命令(1B) +参数(nB) + checksum(1B)  + 结束符(1B)
 *
 *      字头:0xFB+0xBF
 *
 *      长度:字头+长度+命令+参数+checksum的长度.==》5+参数长度>=6
 *
 *      参数:n字节,不定长,1-245字节.
 *
 *      Checksum:
 *      计算方式：Checksum(1Byte) =长度 +命令 + 参数的累加和
 *
 *      结束符:0xED
 *
 * </pre>
 */
public class BlePacket {
    private static final String D_TAG = BlePacket.class.getName();
    private static final byte H1 = (byte) 0xFB;
    private static final byte H2 = (byte) 0xBF;
    private static final byte[] HEAD = {H1, H2};
    private static final byte END = (byte) 0xED;
    private final CompositeByteBuf mPacketBuf;
    private static final int MIN_PACKET_LENGTH = 7;
    private static final int PACKET_WITHOUT_PARAMS_AND_END_LENGTH = MIN_PACKET_LENGTH - 2;
    private static final int MIN_BYTES_COUNT_WITHOUT_END = MIN_PACKET_LENGTH - 1;
    private static final int MAX_PACKET_LENGTH = 20; // ble default max data size
    private static AtomicInteger WHOLE_PACKETS_SUM = new AtomicInteger(1);
    private static AtomicInteger ORDER_NUMBER = new AtomicInteger();
    private static ByteBuf[] mSubPackets;
    public volatile static PACKET_MODE MODE = PACKET_MODE.SINGLE;


    private static AtomicInteger mTotal = new AtomicInteger();

    public static BlePacket get() {
        return new BlePacket();
    }

    public BlePacket() {
        mPacketBuf = Unpooled.compositeBuffer(6);
    }

    public synchronized ByteBuf wrap(byte[] raw) {
        if (WHOLE_PACKETS_SUM.get() < ORDER_NUMBER.incrementAndGet()) {
            WHOLE_PACKETS_SUM.set(1);
            ORDER_NUMBER.set(0);
        }
        final ByteBuf byteBuf = writeBytes(raw);
        final int raw_length = byteBuf.readableBytes();
        if (raw_length == 0) {
            return clear();
        }

        if (raw_length < MIN_PACKET_LENGTH) {
            //raw 为错误数据
        } else {
            boolean HIT_H1 = hitH(byteBuf, H1);
            boolean HIT_H2 = hitH(byteBuf, H2);

            if (HIT_H1 && HIT_H2) {
                addHead();
                final int length = readLength(byteBuf) & 0xff; //除end的长度
                int params_length;
                if (length < MIN_BYTES_COUNT_WITHOUT_END) {
                    MODE = PACKET_MODE.SINGLE;
                    return clear();
                } else {
                    final byte cmd = readCmd(byteBuf);
                    Log.d(D_TAG, "cmd:" + ByteBufUtil.hexDump(new byte[]{cmd}));
                    if (cmd == 0) {
                        return clear();
                    }
                    byte[] params = null;
                    if (length > MAX_PACKET_LENGTH - 1) {
                        MODE = PACKET_MODE.MULTIPLE;
                        //分包了
                        params_length = length - PACKET_WITHOUT_PARAMS_AND_END_LENGTH;
                        final int pd = MAX_PACKET_LENGTH - (2 + 1 + 1);
                        if (params_length > pd) {
                            WHOLE_PACKETS_SUM.set(params_length / MAX_PACKET_LENGTH + 1);
                            ORDER_NUMBER.set(1);
                            mSubPackets = new ByteBuf[WHOLE_PACKETS_SUM.get() - 1];
                            params = readParams(byteBuf, pd);
                            addLength(params_length);
                            addCmd(cmd);
                            addParams(params);
                            Log.d(D_TAG, "sub packet raw: " + ByteBufUtil.hexDump(raw));
                        } else {
                            params = readParams(byteBuf, params_length);
                            addLength(params_length);
                            addCmd(cmd);
                            addParams(params);
                            final byte checksum = readChecksum(byteBuf);
                            if (checksum != computeChecksum()) {
                                return clear();
                            }
                            Log.d(D_TAG, "checksum:" + ByteBufUtil.hexDump(new byte[]{checksum}));
                            addCheckSum(checksum);
                        }
                    } else {
                        MODE = PACKET_MODE.SINGLE;
                        // 整包
                        params_length = length - PACKET_WITHOUT_PARAMS_AND_END_LENGTH;
                        params = readParams(byteBuf, params_length);
                        if (params == null || params.length < 1) {
                            return clear();
                        }
                        Log.d(D_TAG, "params:" + ByteBufUtil.hexDump(params));

                        addLength(params_length);
                        addCmd(cmd);
                        addParams(params);

                        final byte checksum = readChecksum(byteBuf);
                        if (checksum != computeChecksum()) {
                            return clear();
                        }
                        Log.d(D_TAG, "checksum:" + ByteBufUtil.hexDump(new byte[]{checksum}));

                        addCheckSum(checksum);
                        addEnd();
                    }
                    Log.d(D_TAG, "params_length:" + params_length);
                }
            } else {
                final int index = ORDER_NUMBER.get() - 2;
                mSubPackets[index] = Unpooled.buffer(raw.length);
                mSubPackets[index].writeBytes(raw);
                if (WHOLE_PACKETS_SUM.intValue() == ORDER_NUMBER.intValue()) {
                    //组包
                    if (mPacketBuf.numComponents() == 5) {
                        addEnd();
                        Log.d(D_TAG, "==5==组包后:" + getHexString());
                    } else if (mPacketBuf.numComponents() == 4) {
                        //需要从后往前取出checksum 校验
                        Log.d(D_TAG, "===4===组包前:" + index + "===" + ByteBufUtil.hexDump(mSubPackets[index]));
                        final int bytesBefore = mSubPackets[index].bytesBefore((byte) 0xed);
                        Log.d(D_TAG, "==4==组包:bytesBefore:" + bytesBefore);
                        byte checksum = mSubPackets[index].getByte(bytesBefore - 1);
                        Log.d(D_TAG, "==4==组包:checksum:" + ByteBufUtil.hexDump(new byte[]{checksum}));
                        if (bytesBefore > 2) {
                            //剩余的params
                            final ByteBuf b = Unpooled.buffer().writeBytes(mPacketBuf.internalComponent(mPacketBuf.numComponents() - 1)).writeBytes(mSubPackets[index].readBytes(bytesBefore - 2));
                            mPacketBuf.removeComponent(mPacketBuf.numComponents() - 1).addComponent(b);
                        }
                        if (checksum != computeChecksum()) {
                            Log.d(D_TAG, "checksum 不同:" + checksum + " != " + computeChecksum());
                            MODE = PACKET_MODE.SINGLE;
                            return clear();
                        }
                        addCheckSum(checksum);
                        addEnd();
                        Log.d(D_TAG, "==4==组包后：" + getHexString());
                    }
                    MODE = PACKET_MODE.SINGLE;
                } else {
                    Log.d(D_TAG, "===4===组包前:" + index + "===" + ByteBufUtil.hexDump(mSubPackets[index]) + "===writableBytes :" + mPacketBuf.internalComponent(mPacketBuf.numComponents() - 1).writableBytes());
                    //剩余的params
                    final ByteBuf b = Unpooled.buffer().writeBytes(mPacketBuf.internalComponent(mPacketBuf.numComponents() - 1)).writeBytes(mSubPackets[index]);
                    mPacketBuf.removeComponent(mPacketBuf.numComponents() - 1).addComponent(b);
                }
            }
        }
        return mPacketBuf;
    }

    public synchronized boolean isCompletedPacket() {
        return (mPacketBuf.numComponents() == 6);
    }


    public synchronized ByteBuf wrap(byte cmd, @NonNull byte[] params) {

        addHead();
        addLength(params.length);
        addCmd(cmd);
        addParams(params);
        addCheckSum(computeChecksum());
        addEnd();
        return mPacketBuf;
    }

    private void addHead() {
        addComponent(writeBytes(HEAD));
    }

    private void addLength(int params_length) {
        addComponent(writeByte((byte) computeTotalLength(params_length)));
    }

    private void addCmd(byte cmd) {
        addComponent(writeByte(cmd));
    }

    private void addParams(byte[] params) {
        if (params == null) {
            params = new byte[1];
        }
        addComponent(writeBytes(params));
    }

    private void addCheckSum(byte checksum) {
        addComponent(writeByte(checksum));
    }

    private byte computeChecksum() {
        byte checksum = 0;
        for (int i = HEAD.length; i < mPacketBuf.capacity(); i++) {
            checksum += mPacketBuf.getByte(i);
        }
        return checksum;
    }

    private void addEnd() {
        addComponent(writeByte(END));
    }


    public ByteBuf getHead() {
        return mPacketBuf.component(0);
    }

    public ByteBuf getLength() {
        return mPacketBuf.component(1);
    }

    public ByteBuf getCmd() {
        return mPacketBuf.component(2);
    }

    public ByteBuf getParams() {
        return mPacketBuf.component(3);
    }

    public ByteBuf getCheckSum() {
        return mPacketBuf.component(4);
    }

    public ByteBuf getEnd() {
        return mPacketBuf.component(5);
    }

    /**
     * 计算总长度
     *
     * @param paramsLength
     * @return
     */
    private int computeTotalLength(int paramsLength) {
        mTotal.set(PACKET_WITHOUT_PARAMS_AND_END_LENGTH + paramsLength);
        return mTotal.get() & 0xff;
    }

    private ByteBuf writeBytes(byte[] bytes) {
        return Unpooled.buffer(bytes.length).writeBytes(bytes);
    }

    private ByteBuf writeByte(byte b) {
        return Unpooled.buffer(1).writeByte(b);
    }

    private void addComponent(ByteBuf component) {
        mPacketBuf.addComponent(component);
    }

    public synchronized String getHexString() {
        StringBuffer sb = new StringBuffer();
        for (ByteBuf buf : mPacketBuf) {
            sb.append(ByteBufUtil.hexDump(buf));
        }
        return sb.toString();
    }

    public synchronized byte[] getBytes() {
        // total 是没有计算最后的ED字节，所以取完整的需要 +1个字节
        final byte[] bytes = new byte[mTotal.get() + 1];
        try {
            mPacketBuf.getBytes(0, bytes);
        }catch (Exception e){
            e.printStackTrace();
        }
        return bytes;
    }

    private boolean hitH(ByteBuf src, byte H) {
        if (src.isReadable()) {
            return src.readByte() == H;
        }
        return false;
    }

    private byte readLength(ByteBuf src) {
        return readByte(src);
    }

    private byte readByte(ByteBuf src) {
        if (src.isReadable()) {
            return src.readByte();
        }
        return 0;
    }


    private byte readChecksum(ByteBuf src) {
        return readByte(src);
    }

    private byte[] readParams(ByteBuf src, int params_length) {

        final byte[] param = new byte[params_length];
        if (src.readableBytes() > 1) {
            src.readBytes(param);
            return param;
        }

        return null;
    }

    private byte readCmd(ByteBuf src) {
        return readByte(src);
    }

    private synchronized ByteBuf clear() {
        return mPacketBuf.clear();
    }

    enum PACKET_MODE {
        SINGLE, MULTIPLE
    }
}

