package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;
import com.google.protobuf.ByteString;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmSquareActionProto;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ProtoBufferDispose;

public class StopSquareActionHandler implements IMsgHandler {
  @Override public void handleMsg(int requestCmdId, int responseCmdId,
      AlphaMessageOuterClass.AlphaMessage request, String peer) {

    ByteString requestBody = request.getBodyData();
    byte[] bodyBytes = requestBody.toByteArray();
    long requestSerial = request.getHeader().getSendSerial();

    CmSquareActionProto.StopSquareActionRequest stopSquareActionRequest =
        (CmSquareActionProto.StopSquareActionRequest) ProtoBufferDispose.unPackData(
            CmSquareActionProto.StopSquareActionRequest.class, bodyBytes);

    Log.w("Logic",
        "requestCmdId = " + requestCmdId + " , request =" + stopSquareActionRequest.toString());

    new SquareActionManager().stopSquareActionSkill(new SquareActionManager.SquareActionListener() {
      @Override public void onActionFailure(int errorCode, String msg) {
        CmSquareActionProto.SquareActionResponse.Builder builder =
            CmSquareActionProto.SquareActionResponse.newBuilder();
        builder.setSuccess(false);
        builder.setErrMsg(msg);
        RobotPhoneCommuniteProxy.getInstance()
            .sendResponseMessage(responseCmdId, "1", requestSerial, builder.build(), peer, null);
      }

      @Override public void onActionFinished() {
        CmSquareActionProto.SquareActionResponse.Builder builder =
            CmSquareActionProto.SquareActionResponse.newBuilder();
        builder.setSuccess(true);
        RobotPhoneCommuniteProxy.getInstance()
            .sendResponseMessage(responseCmdId, "1", requestSerial, builder.build(), peer, null);
      }
    });
  }
}
