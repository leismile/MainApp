package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;
import com.google.protobuf.ByteString;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmSquareActionProto;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ProtoBufferDispose;
import com.ubtechinc.services.alphamini.R;

public class DoSquareActionHandler implements IMsgHandler {
  @Override public void handleMsg(int requestCmdId, int responseCmdId,
      AlphaMessageOuterClass.AlphaMessage request, String peer) {

    ByteString requestBody = request.getBodyData();
    byte[] bodyBytes = requestBody.toByteArray();
    long requestSerial = request.getHeader().getSendSerial();

    CmSquareActionProto.DoSquareActionRequest doSquareRequest =
        (CmSquareActionProto.DoSquareActionRequest) ProtoBufferDispose.unPackData(
            CmSquareActionProto.DoSquareActionRequest.class, bodyBytes);

    Log.w("Logic", "requestCmdId = " + requestCmdId + " , doSquareRequest =" + request.toString());

    if (doSquareRequest != null) {
      new SquareActionManager().callSquareActionSkill(doSquareRequest.getResourceName(),
          doSquareRequest.getResourceType(),
          new SquareActionManager.SquareActionListener() {
            @Override public void onActionFailure(int errorCode, String msg) {
              CmSquareActionProto.SquareActionResponse.Builder builder =
                  CmSquareActionProto.SquareActionResponse.newBuilder();
              builder.setSuccess(false);
              builder.setErrMsg(msg);
              Log.i("Logic","DoSquareAction response result = " + msg);
              RobotPhoneCommuniteProxy.getInstance()
                  .sendResponseMessage(responseCmdId, "1", requestSerial, builder.build(), peer,
                      null);
            }

            @Override public void onActionFinished() {
              CmSquareActionProto.SquareActionResponse.Builder builder =
                  CmSquareActionProto.SquareActionResponse.newBuilder();
              builder.setSuccess(true);
              Log.i("Logic","DoSquareAction response result = true");
              RobotPhoneCommuniteProxy.getInstance()
                  .sendResponseMessage(responseCmdId, "1", requestSerial, builder.build(), peer,
                      null);
            }
          });
    } else {
      CmSquareActionProto.SquareActionResponse.Builder builder =
          CmSquareActionProto.SquareActionResponse.newBuilder();
      builder.setSuccess(false);
      builder.setErrMsg(AlphaApplication.getContext().getString(R.string.error_param));
      RobotPhoneCommuniteProxy.getInstance()
          .sendResponseMessage(responseCmdId, "1", requestSerial, builder.build(), peer, null);
    }
  }
}
