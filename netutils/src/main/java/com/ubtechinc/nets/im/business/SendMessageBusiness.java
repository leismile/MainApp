package com.ubtechinc.nets.im.business;

import android.util.Log;

import com.tencent.imsdk.TIMConversation;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.imsdk.TIMCustomElem;
import com.tencent.imsdk.TIMImageElem;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMMessage;
import com.tencent.imsdk.TIMTextElem;
import com.tencent.imsdk.TIMValueCallBack;
import com.ubtech.utilcode.utils.FileUtils;
import com.ubtechinc.nets.im.IMErrorUtil;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ISendMsg;

/**
 * Created by Administrator on 2016/12/17.
 */
public class SendMessageBusiness implements ISendMsg {

    public  final String TAG="SendRevMsgBusiness";

    private static SendMessageBusiness sInstance;

    public static SendMessageBusiness getInstance() {
        if (sInstance == null) {
            synchronized (SendMessageBusiness.class) {
                sInstance = new SendMessageBusiness();
            }
        }
        return sInstance;
    }

    private SendMessageBusiness() {
    }


    public void init() {

    }

    @Override
    public void sendMsg(long requestSerialId, String peer, byte[] bodyBytes, RobotPhoneCommuniteProxy.Callback callback) {
        sendTextMessageByIM(requestSerialId,peer,bodyBytes,callback);
    }

    private void sendTextMessageByIM(final long requestSerialId,String peer, byte[] bodyBytes, final RobotPhoneCommuniteProxy.Callback callback){
        if (bodyBytes == null) {
            return;
        }
        TIMConversation conversation= TIMManager.getInstance().getConversation(TIMConversationType.C2C,peer);
        TIMMessage message = new TIMMessage();
        TIMCustomElem elem = new TIMCustomElem();
        elem.setData(bodyBytes);
        //将elem添加到消息
        if(message.addElement(elem) != 0) {
            callback.onSendError(requestSerialId, IMErrorUtil.ERROR.ADD_ELEMENT_FAIL);
            Log.i(TAG, "addElement failed");
            return;
        }
        conversation.sendOnlineMessage(message,new TIMValueCallBack<TIMMessage>() {

            @Override
            public void onError(int code, String s) {
                Log.e(TAG,"onError---msg = "+s + " code = " + code);
                if (callback != null) {
                    callback.onSendError(requestSerialId,code);
                }
            }

            @Override
            public void onSuccess(TIMMessage timMessage) {
                Log.d(TAG,"onSuccess--msg : "+timMessage);
                if (callback != null) {
                    callback.onSendSuccess();
                }
            }

        });

    }

    public void sendFileMsg(final long requestSerialId, String peer,String filePath,int level, final RobotPhoneCommuniteProxy.Callback callback) {

        if(!FileUtils.isFileExists(filePath)){
            return;
        }
        TIMConversation conversation= TIMManager.getInstance().getConversation(TIMConversationType.C2C,peer);
        TIMMessage message = new TIMMessage();
        TIMImageElem elem = new TIMImageElem();
        elem.setPath(filePath);
        elem.setLevel(level);
        Log.d(TAG,"level ="+level);
        Log.d(TAG,"filePath ="+filePath);
        //将elem添加到消息
        if(message.addElement(elem) != 0) {
            callback.onSendError(requestSerialId, IMErrorUtil.ERROR.ADD_ELEMENT_FAIL);
            Log.i(TAG, "addElement failed");
            return;
        }
        conversation.sendOnlineMessage(message,new TIMValueCallBack<TIMMessage>() {

            @Override
            public void onError(int code, String s) {
                Log.e(TAG,"onError---msg = "+s + " code = " + code);
                if (callback != null) {
                    callback.onSendError(requestSerialId,code);
                }
            }

            @Override
            public void onSuccess(TIMMessage timMessage) {
                Log.d(TAG,"onSuccess--msg : "+timMessage);
                if (callback != null) {
                    callback.onSendSuccess();
                }
            }

        });
    }

    public  void sendHeartMessageByIM(String peer, String text,TIMValueCallBack callback){
        String textMessage = text;
        TIMConversation conversation = TIMManager.getInstance().getConversation(TIMConversationType.C2C,peer);
        TIMMessage message = new TIMMessage();
        TIMTextElem elem = new TIMTextElem();
        elem.setText(textMessage);

        //将elem添加到消息
        if(message.addElement(elem) != 0) {
            Log.i(TAG, "addElement failed");
            return;
        }
        conversation.sendOnlineMessage(message,callback);

    }

}
