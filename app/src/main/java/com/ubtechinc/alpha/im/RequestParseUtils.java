package com.ubtechinc.alpha.im;

import com.google.protobuf.ByteString;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.nets.phonerobotcommunite.ProtoBufferDispose;

/**
 * @desc : 解析IMRequest工具类
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/2/3
 */

public class RequestParseUtils {
    public static <T> T getRequestClass(AlphaMessageOuterClass.AlphaMessage request, Class<T> tClass){
        ByteString requestBody = request.getBodyData();
        byte[] bodyBytes = requestBody.toByteArray();
        long requestSerial = request.getHeader().getSendSerial();
        T value = (T) ProtoBufferDispose.unPackData(tClass, bodyBytes);
        return value;
    }
}
