package com.ubtechinc.alpha.im.msghandler;


import android.util.Log;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.ubtechinc.alpha.AlGetAlubmInfo;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.utils.ImToMasterUtil;
import com.ubtechinc.nets.phonerobotcommunite.ProtoBufferDispose;

/**
 * Created by Administrator on 2017/5/29.
 */

public class GetAlbumlistMsgHandler implements IMsgHandler {
    static final String TAG = "GetAlbumlistMsgHandler";
    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        Log.d(TAG, "GetAlbumlistMsgHandler"+peer);

        long requestSerial = request.getHeader().getSendSerial();

        ByteString requestBody = request.getBodyData();
        byte[] bodyBytes = requestBody.toByteArray();
        AlGetAlubmInfo.AlGetAlubmInfoRequest alGetAlubmInfoRequest = (AlGetAlubmInfo.AlGetAlubmInfoRequest)ProtoBufferDispose.unPackData(AlGetAlubmInfo.AlGetAlubmInfoRequest.class,bodyBytes);
        Any any = alGetAlubmInfoRequest.getBodyData();
        String host = alGetAlubmInfoRequest.getHost() ;
        String path = alGetAlubmInfoRequest.getPath();
        ImToMasterUtil imToMasterUtil =new ImToMasterUtil(responseCmdId,peer,requestSerial,host,path);
        imToMasterUtil.sendToMaster(any);

    }


}
