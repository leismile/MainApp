package com.ubtechinc.bluetoothlibrary;

import java.util.LinkedList;
import java.util.List;

/**
 * @Deseription
 * @Author tanghongyu
 * @Time 2018/5/11 15:39
 */

public class DefaultDataEngine implements IDataEngine {
    int count = 20;
    @Override
    public List<byte[]> spliteData(byte[] data) {
        List<byte[]> byteQueue = new LinkedList<>();
        int index = 0;
        do {
            byte[] rawData = new byte[data.length - index];
            byte[] newData;
            System.arraycopy(data, index, rawData, 0, data.length - index);
            if (rawData.length <= count) {
                newData = new byte[rawData.length];
                System.arraycopy(rawData, 0, newData, 0, rawData.length);
                index += rawData.length;
            } else {
                newData = new byte[count];
                System.arraycopy(data, index, newData, 0, count);
                index += count;
            }
            byteQueue.add(newData);
        } while (index < data.length);

        return byteQueue;
    }

    @Override
    public byte[] packetData(byte[] data)
    {
        return data;
    }
}
