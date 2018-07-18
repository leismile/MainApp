package com.ubtechinc.protocollibrary.communite;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.GeneratedMessageV3;
import com.ubtechinc.codemao.CodeMaoMessage;
import com.ubtechinc.codemao.CodeMaoMessageHeader;
import com.ubtechinc.codemao.CodeMaoObserveInfraredDistance;
import com.ubtechinc.protocollibrary.protocol.BleMessage;
import com.ubtechinc.protocollibrary.protocol.MiniBleProto;
import com.ubtechinc.protocollibrary.protocol.MiniMessage;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author：tanghongyu
 * @date：5/16/2017 4:15 PM
 * @modifier：tanghongyu
 * @modify_date：5/16/2017 4:15 PM
 * [A brief description]
 * version
 */

public class ProtoBufferDispose {
    public static final String TAG = "ProtoBufferDispose";

    public static byte[] getPackMData(GeneratedMessageV3 data) {
        if (data != null) {
            return data.toByteArray();
        } else {
            return null;
        }
    }

    public static GeneratedMessageV3 unPackData(Class targetClass, byte[] data) {
        try {
            Method parseFromMethod = targetClass.getDeclaredMethod("parseFrom", byte[].class);
            parseFromMethod.setAccessible(true);
            Object object = parseFromMethod.invoke(null, data);
//            printProtoBuffBody((GeneratedMessageLite)object);
            return (GeneratedMessageV3) object;


        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static byte[] buildAlphaMessage(short cmdId, byte version, int sendSerial, int responseSerial, GeneratedMessageV3 requestBody) {
        CodeMaoMessageHeader.MessageHeader.Builder builder = CodeMaoMessageHeader.MessageHeader.newBuilder();
        builder.setCommandId(cmdId);
        builder.setResponseSerial(responseSerial);
        builder.setSendSerial(sendSerial);
        CodeMaoMessage.Message.Builder messageBuilder = CodeMaoMessage.Message.newBuilder();
        messageBuilder.setHeader(builder.build());
        if (requestBody != null) {
            ByteString body = ByteString.copyFrom(ProtoBufferDispose.getPackMData(requestBody));
            messageBuilder.setBodyData(body);
        }

        MiniMessage miniMessage = new MiniMessage();
        miniMessage.setCommandId(cmdId);
        miniMessage.setDataContent(messageBuilder.build().toByteArray());
        miniMessage.setVersionCode(version);
        return MiniBleProto.INSTANCE.pack(new BleMessage(miniMessage));
    }

    public static CodeMaoMessage.Message parseMessage(byte[] data) {

        CodeMaoMessage.Message message = (CodeMaoMessage.Message) unPackData(
                CodeMaoMessage.Message.class, data);
        return message;

    }
}
