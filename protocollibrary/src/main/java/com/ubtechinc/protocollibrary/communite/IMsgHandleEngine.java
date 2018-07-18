package com.ubtechinc.protocollibrary.communite;


import android.util.Log;

import com.ubtech.utilcode.utils.ConvertUtils;
import com.ubtech.utilcode.utils.LogUtils;
import com.ubtechinc.codemao.CodeMaoMessage;
import com.ubtechinc.protocollibrary.protocol.MiniBleProto;
import com.ubtechinc.protocollibrary.protocol.MiniMessage;
import com.ubtechinc.protocollibrary.protocol.PacketData;
import com.ubtechinc.protocollibrary.protocol.PacketMessage;

import kotlin.Pair;

/**
 * Created by Administrator on 2017/5/24.
 */

public class IMsgHandleEngine {
    private static final String TAG = "IMsgHandleEngine";
    private static IMsgHandleEngine sInstance;

    private ImMsgDispathcer msgDispathcer;

    public static IMsgHandleEngine getInstance() {
        if (sInstance == null) {
            synchronized (ImMsgDispathcer.class) {
                if (sInstance == null) {
                    sInstance = new IMsgHandleEngine();
                }
            }
        }
        return sInstance;
    }
    public void setIMsgDispatcher(ImMsgDispathcer msgDispathcer) {
        this.msgDispathcer = msgDispathcer;
    }


    public synchronized void handleProtoBuffMsg(byte[] msgBytes, Object peer) {
        if(msgBytes == null || msgBytes.length == 0) return;
        Log.i(TAG, "handleProtoBuffMsg content =  " + ConvertUtils.bytes2HexString(msgBytes));
        PacketMessage packetMessage = MiniBleProto.INSTANCE.parsePacketMsg(msgBytes);
        if(packetMessage == null) return;

        // 先解析一次
        Pair<Integer, MiniMessage[]> unpackData = MiniBleProto.INSTANCE.unpack(packetMessage.getDataContent());
        //更新心跳包
        if(unpackData.getFirst() == -1  || unpackData.getSecond().length == 0  ) {//如果不是一条命令则是数据包



            PacketData.Companion.get().putBytes(packetMessage);
            byte[] content = PacketData.Companion.get().getBuffer();
            Pair<Integer, MiniMessage[]> data = MiniBleProto.INSTANCE.unpack(content);
            if(data.getFirst() != -1   && data.getSecond().length != 0  ){
                parseMessage(data.getSecond(), peer);
            }


        }else {//组包完成
            parseMessage(unpackData.getSecond(), peer);
        }

    }

    private void parseMessage(MiniMessage[] dataPairs, Object peer) {
        Log.i(TAG, "receive dataPairsSize =  " + dataPairs.length);
        for (MiniMessage msgRequest: dataPairs) {
            if(msgRequest==null){
                return;
            }

            CodeMaoMessage.Message message = ProtoBufferDispose.parseMessage(msgRequest.getDataContent());
            MiniMessage miniMessage = new MiniMessage();
            miniMessage.setResponseSerial(message.getHeader().getResponseSerial());

            miniMessage.setDataContent(message.getBodyData().toByteArray());
            miniMessage.setSendSerial(message.getHeader().getSendSerial());
            miniMessage.setCommandId((short) message.getHeader().getCommandId());

            LogUtils.d("handleProtoBuffMsg--cmdId = "+miniMessage.getCommandId()+", requestId = "+miniMessage.getSendSerial()+", responseId = "+miniMessage.getResponseSerial());
            if (miniMessage.getResponseSerial() > 0) { //说明该消息是一个Response Msg,这种不由ImMsgDispathcer处理，把Resonse抛给调用方
                RobotPhoneCommuniteProxy.getInstance().dispatchResponse(message.getHeader().getResponseSerial(),miniMessage);
            } else {
                msgDispathcer.dispatchMsg(miniMessage.getCommandId(), miniMessage,peer);
            }

        }
        PacketData.Companion.get().clear();//
    }

    public void handleJsonMsg(String jasonStr,Object peer) {
//        IMJsonMsg jsonRequest = JsonUtils.getObject(jasonStr,IMJsonMsg.class);
//        int cmdId = jsonRequest.header.commandId;
//        msgDispathcer.dispatchMsg(cmdId,jsonRequest,peer);

    }
}
