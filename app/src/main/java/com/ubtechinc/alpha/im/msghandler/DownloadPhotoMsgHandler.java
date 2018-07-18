package com.ubtechinc.alpha.im.msghandler;


import android.os.Environment;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.ubt.alpha2.download.util.FileUtils;
import com.ubtechinc.alpha.AlDwonloadfile;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.nets.im.business.SendMessageBusiness;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ProtoBufferDispose;

import java.io.File;

/**
 * Created by Administrator on 2017/11/8.
 */

public class DownloadPhotoMsgHandler implements IMsgHandler {
    private static final String TAG = "DownloadPhotoMsgHandler";

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        ByteString requestBody = request.getBodyData();
        byte[] bodyBytes = requestBody.toByteArray();
        long requestSerial = request.getHeader().getSendSerial();
        AlDwonloadfile.AlDwonloadfileRequest alDwonloadfileRequest = (AlDwonloadfile.AlDwonloadfileRequest) ProtoBufferDispose.unPackData(AlDwonloadfile.AlDwonloadfileRequest.class, bodyBytes);

        String fileName = alDwonloadfileRequest.getName();
        String filePath ="" ;
        int level =alDwonloadfileRequest.getLevel();
        if(level == 1){
            filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ubtrobot" + File.separator + "camera"+File.separator + ".thumbnail" + File.separator+fileName ;
        }else if(level==0){
            filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ubtrobot" + File.separator + "camera" + File.separator+fileName ;

        }else if(level == 2){
            filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ubtrobot" + File.separator + "camera" +File.separator + ".thumbnail1080" + File.separator+fileName ;
            File file = new File(filePath);
            if(!file.exists()){
                filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ubtrobot" + File.separator + "camera" + File.separator+fileName ;
            }
        }
        Log.d(TAG, filePath+"levle ="+level);

        SendMessageBusiness.getInstance().sendFileMsg(requestSerial,
                peer,
                filePath, level,
                new RobotPhoneCommuniteProxy.Callback() {
                    @Override
                    public void onSendSuccess() {

                        Log.d(TAG, "onSendSuccess = ");
                    }

                    @Override
                    public void onSendError(long requestId, int errorCode) {

                        Log.d(TAG, "onSendError = ");
                    }

                    @Override
                    public void onReturnMessage(long requestId, AlphaMessageOuterClass.AlphaMessage response) {

                    }
                });


    }
}
