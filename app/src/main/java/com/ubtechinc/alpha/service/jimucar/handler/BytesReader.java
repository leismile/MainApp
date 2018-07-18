package com.ubtechinc.alpha.service.jimucar.handler;

import android.util.Log;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

/**
 * @author : kevin.liu@ubtrobot.com
 * @description :
 * @date : 2018/7/5
 * @modifier :
 * @modify time :
 */
public class BytesReader {

    private static final String TAG = BytesReader.class.getName();

    public synchronized static byte[] read(ByteBuf byteBuf, int readByteNum) {
        if (readable(byteBuf, readByteNum)) {
            final byte[] bytes = new byte[readByteNum];
            byteBuf.readBytes(bytes);
            Log.d(TAG, "read bytes hex:" + ByteBufUtil.hexDump(bytes));
            return bytes;
        }
        return null;
    }

    public synchronized static boolean readable(ByteBuf byteBuf, int readByteNum) {
        return byteBuf.isReadable() && byteBuf.readableBytes() > readByteNum - 1;
    }

    public synchronized static void skip(ByteBuf byteBuf, int skipBytesNum) {
        if (BytesReader.readable(byteBuf, skipBytesNum)) {
            byteBuf.skipBytes(skipBytesNum);
        }
    }

    /**
     * 把byte转为字符串的bit
     */
    public synchronized static String byteToBit(byte b) {
        return ""
                + (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1)
                + (byte) ((b >> 5) & 0x1) + (byte) ((b >> 4) & 0x1)
                + (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1)
                + (byte) ((b >> 1) & 0x1) + (byte) ((b >> 0) & 0x1);

    }
}
