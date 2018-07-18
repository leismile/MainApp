package com.ubtechinc.alpha.im.msghandler;

import com.ubtechinc.alpha.AlStopPlayAction;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtrobot.action.ActionApi;

/**
 * @desc : 停止动作执行的消息处理器
 * @author: wzt
 * @time : 2017/6/1
 * @modifier:
 * @modify_time:
 */


public class StopActionMsgHandler implements IMsgHandler {
    static final String TAG = "StopActionMsgHandler";
    private int responseCmdId;
    private long requestSerial;
    private String peer;

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        long requestSerial = request.getHeader().getSendSerial();
        this.responseCmdId = responseCmdId;
        this.requestSerial = requestSerial;
        this.peer = peer;
        boolean success = ActionApi.get().stopAction();
        if (success) {
            AlStopPlayAction.AlStopPlayActionResponse.Builder builder = AlStopPlayAction.AlStopPlayActionResponse.newBuilder();
            builder.setIsSuccess(false);
            builder.setResultCode(0);
            RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, "1", requestSerial, builder.build(), peer, null);
        }
    }
}
