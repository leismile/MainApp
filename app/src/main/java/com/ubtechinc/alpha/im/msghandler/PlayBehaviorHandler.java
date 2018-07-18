package com.ubtechinc.alpha.im.msghandler;

import com.google.protobuf.ByteString;
import com.ubtech.utilcode.utils.LogUtils;
import com.ubtechinc.alpha.AIPlayBehevior;
import com.ubtechinc.alpha.AlPlayActionCommand;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ProtoBufferDispose;
import com.ubtrobot.action.ActionApi;
import com.ubtrobot.mini.libs.behaviors.Behavior;
import com.ubtrobot.mini.libs.behaviors.BehaviorInflater;

/**
 * Created by bob.xu on 2018/1/8.
 */

public class PlayBehaviorHandler implements IMsgHandler {

    private static final String BEHAVIOR_ROOT_PATH = "/sdcard/robotshow/";
    public static final String TAG = "PlayBehaviorHandler";

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        ByteString requestBody = request.getBodyData();
        byte[] bodyBytes = requestBody.toByteArray();
        long requestSerial = request.getHeader().getSendSerial();

        AIPlayBehevior.BehaviorRequest playBehaviorCommand  = (AIPlayBehevior.BehaviorRequest) ProtoBufferDispose.unPackData(
                AIPlayBehevior.BehaviorRequest.class,bodyBytes);

        LogUtils.i("request body : behaviorName = "+playBehaviorCommand.getBehaviorName());
        if (playBehaviorCommand.getAction().equals("play")) {
            doBehaviorFromXml(playBehaviorCommand.getBehaviorName());
        } else {
            stopBehavior(playBehaviorCommand.getBehaviorName());
        }

        AIPlayBehevior.BehaviorResponse.Builder builder = AIPlayBehevior.BehaviorResponse.newBuilder();
        builder.setIsSuccess(true);
        LogUtils.i( "PlayAction response result = " + builder.getIsSuccess());
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId,"1",requestSerial, builder.build() ,peer,null);
    }

    private void doBehaviorFromXml(String xmlName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(BEHAVIOR_ROOT_PATH).append(xmlName);
        String filePath = stringBuilder.toString();
        LogUtils.d(TAG, " doBehaviorFromXml -- filePath : " + filePath);
        BehaviorManager.getInstance().playBehavior(filePath,new Behavior.BehaviorListener() {

            @Override
            public void onCompleted() {

            }
        });
    }

    private void stopBehavior(String xmlName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(BEHAVIOR_ROOT_PATH).append(xmlName);
        String filePath = stringBuilder.toString();
        LogUtils.d(TAG, " stopBehavior -- filePath : " + filePath);
        BehaviorManager.getInstance().stopBehavior(filePath);
    }
}
