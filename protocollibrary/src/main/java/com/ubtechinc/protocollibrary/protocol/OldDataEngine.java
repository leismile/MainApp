package com.ubtechinc.protocollibrary.protocol;

import com.google.common.collect.Lists;
import com.ubtech.utilcode.utils.ListUtils;
import com.ubtechinc.bluetoothlibrary.IDataEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * @Deseription
 * @Author tanghongyu
 * @Time 2018/5/11 17:32
 */

public class OldDataEngine implements IDataEngine {

    /**最大发送20个字节，但是首个字节由标志位占用*/
    private static final int MAX_SIZE = 18;

    /**开始标志*/
    private static final byte START_BYTE = 0x01;
    /**继续标志*/
    private static final byte CONTINUE_BYTE = 0x02;
    /**结束标志*/
    private static final byte END_BYTE = 0x00;

    @Override
    public List<byte[]> spliteData(byte[] originData) {

        /**
         * 字符串转为二维字节数组
         * */
            try{
                int size = (int)Math.ceil(originData.length / (MAX_SIZE*1.0));
                byte[][] data = new byte[size][MAX_SIZE+1];

                int start = 0;
                int end = 0;
                int index  = 0;
                while(index < size) {
                    index ++ ;
                    if(index == size) {
                        data[index-1][0] = END_BYTE;
                    } else if(index == 1) {
                        data[index-1][0] = START_BYTE;
                    } else {
                        data[index-1][0] = CONTINUE_BYTE;
                    }

                    end = Math.min(start + MAX_SIZE, originData.length);
                    System.arraycopy(originData, start, data[index-1], 1, end - start);

                    start = end;
                }
                ArrayList arrayList = Lists.newArrayList();
                for(int i= 0; i<data.length; i++) {
                    arrayList.add(data[i]);
                }
                return arrayList;
            }catch(Exception e){
                e.printStackTrace();
            }

            return null;


    }

    @Override
    public byte[] packetData(byte[] data) {
        return MiniBleProto.INSTANCE.devide(data).get(0);
    }
}
