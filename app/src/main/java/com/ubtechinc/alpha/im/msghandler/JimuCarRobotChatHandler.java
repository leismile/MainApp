package com.ubtechinc.alpha.im.msghandler;

import android.text.TextUtils;
import android.util.Log;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.JimuCarRobotChat;
import com.ubtechinc.alpha.JimuErrorCodeOuterClass;
import com.ubtechinc.alpha.im.IMCmdId;
import com.ubtechinc.alpha.im.RequestParseUtils;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;

/**
 * @author : kevin.liu@ubtrobot.com
 * @description :
 * @date : 2018/6/29
 * @modifier :
 * @modify time :
 */
public class JimuCarRobotChatHandler implements IMsgHandler {

    private static volatile long mRequestSerial;
    public volatile static String mPeer;
    public volatile static int mResponseCmdId;

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        Log.d("msgHandler", "JimuCarRobotChatHandler:");
        mRequestSerial = request.getHeader().getSendSerial();
        mPeer = peer;
        mResponseCmdId = responseCmdId;

        final JimuCarRobotChat.JimuCarRobotChatRequest jimuCarRobotChatRequest = RequestParseUtils.getRequestClass(request, JimuCarRobotChat.JimuCarRobotChatRequest.class);
        final JimuCarRobotChat.JimuCarRobotChatResponse.Builder builder = JimuCarRobotChat.JimuCarRobotChatResponse.newBuilder();
        final String content = jimuCarRobotChatRequest.getContent();
        final JimuCarRobotChat.chatMode chatMode = jimuCarRobotChatRequest.getChatMode();
        if (TextUtils.isEmpty(content)) {
            RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, mRequestSerial, builder.setChatMode(chatMode).setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.REQUEST_PARAMS_ERROR).build(), peer, null);
            return;
        }

        if (chatMode == JimuCarRobotChat.chatMode.CUSTOM) {
            playTTs(content, new VoiceListener() {
                @Override
                public void onCompleted() {
                    responsePlayTTsComplete(responseCmdId, builder, chatMode);
                }

                @Override
                public void onError(int i, String s) {
                    responsePlayTTsError(responseCmdId, builder, chatMode);
                }
            });

        } else {
            //TODO 播放预置

            VoicePool.get().playLocalTTs(content,Priority.NORMAL,null);

            RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, request.getHeader().getSendSerial(), builder.setChatMode(JimuCarRobotChat.chatMode.PRESET).setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.REQUEST_SUCCESS).build(), mPeer, null);
        }
    }


    public void playTTs(String content, VoiceListener listener) {
        VoicePool.get().playTTs(content, Priority.NORMAL, listener);
    }


    private void responsePlayTTsError(int responseCmdId, JimuCarRobotChat.JimuCarRobotChatResponse.Builder builder, JimuCarRobotChat.chatMode chatMode) {
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, mRequestSerial, builder.setChatMode(chatMode).setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.INTERNAL_ERROR).build(), mPeer, null);
    }

    private void responsePlayTTsComplete(int responseCmdId, JimuCarRobotChat.JimuCarRobotChatResponse.Builder builder, JimuCarRobotChat.chatMode chatMode) {
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION, mRequestSerial, builder.setChatMode(chatMode).setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.REQUEST_SUCCESS).build(), mPeer, null);
    }

    public static JimuCarRobotChatHandler get() {
        return new JimuCarRobotChatHandler();
    }
}
