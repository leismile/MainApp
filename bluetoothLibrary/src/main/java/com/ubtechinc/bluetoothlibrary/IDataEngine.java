package com.ubtechinc.bluetoothlibrary;

import java.util.List;

/**
 * @Deseription
 * @Author tanghongyu
 * @Time 2018/5/11 14:44
 */

public interface IDataEngine {

     List<byte[]> spliteData(byte[] data);
     byte[]  packetData(byte[] data);
}
