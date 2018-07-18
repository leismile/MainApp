package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.JimuCarLevelOuterClass;
import com.ubtechinc.alpha.JimuErrorCodeOuterClass;
import com.ubtechinc.alpha.im.IMCmdId;
import com.ubtechinc.alpha.im.RequestParseUtils;
import com.ubtechinc.alpha.utils.AlphaUtils;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtrobot.commons.Priority;

/**
 * @author : kevin.liu@ubtrobot.com
 * @description :
 * @date : 2018/7/16
 * @modifier :
 * @modify time :
 */
public class JimuCarChangeLevelHandler implements IMsgHandler {

    private static volatile long mRequestSerial;
    public volatile static String mPeer;
    public volatile static int mResponseCmdId;

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        Log.d("msgHandler", "JimuCarChangeLevelHandler:");
        mRequestSerial = request.getHeader().getSendSerial();
        mPeer = peer;
        mResponseCmdId = responseCmdId;

        final JimuCarLevelOuterClass.ChangeJimuCarLevelRequest carLevelRequest = RequestParseUtils.getRequestClass(request, JimuCarLevelOuterClass.ChangeJimuCarLevelRequest.class);
        final JimuCarLevelOuterClass.JimuCarLevel level = carLevelRequest.getLevel();

        if (level == JimuCarLevelOuterClass.JimuCarLevel.NORMAL) {
            AlphaUtils.playBehavior("Drive_0001", Priority.NORMAL, null);

        } else if (level == JimuCarLevelOuterClass.JimuCarLevel.POLICE) {
            AlphaUtils.playBehavior("Drive_0007", Priority.NORMAL, null);
        }
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(mResponseCmdId, IMCmdId.IM_VERSION, mRequestSerial, JimuCarLevelOuterClass.ChangeJimuCarLevelResponse.newBuilder().setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.REQUEST_SUCCESS).build(), mPeer, null);
    }
}
