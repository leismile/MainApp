package com.ubtechinc.alpha.im.msghandler;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmPcSerialNumOuterClass;
import com.ubtrobot.sys.SysApi;

/**
 * @author tanghongyu
 * @ClassName GetChargePlayMsgHandler
 * @date 6/19/2017
 * @Description 获取边冲边玩状态
 * @modifier
 * @modify_time
 */
public class GetSerialNumMsgHandler implements IMsgHandler {
  static final String TAG = "GetSerialNumMsgHandler";

  @Override public void handleMsg(int requestCmdId, int responseCmdId,
      final AlphaMessageOuterClass.AlphaMessage request, String peer) {
    final int responseId = responseCmdId;
    final long requestSerial = request.getHeader().getSendSerial();
    final CmPcSerialNumOuterClass.CmPcSerialNum.Builder builder =
        CmPcSerialNumOuterClass.CmPcSerialNum.newBuilder();
    builder.setStrSerialNum(SysApi.get().readRobotSid());
  }
}

