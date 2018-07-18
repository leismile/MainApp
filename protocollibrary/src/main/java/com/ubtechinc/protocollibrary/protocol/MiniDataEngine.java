package com.ubtechinc.protocollibrary.protocol;

import com.ubtechinc.bluetoothlibrary.IDataEngine;

import java.util.List;

/**
 * @Deseription
 * @Author tanghongyu
 * @Time 2018/5/11 17:32
 */

public class MiniDataEngine implements IDataEngine {
    @Override
    public List<byte[]> spliteData(byte[] data) {
        return MiniBleProto.INSTANCE.devide(data);
    }

    @Override
    public byte[] packetData(byte[] data) {
        return MiniBleProto.INSTANCE.devide(data).get(0);
    }
}
