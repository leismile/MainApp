package com.ubtechinc.alpha.im.msghandler;

import android.content.Intent;

import com.ubtechinc.alpha.AdbSwitch;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.utils.SystemUtils;
import com.ubtechinc.nets.http.ThrowableWrapper;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ICallback;

import static com.ubtechinc.alpha.im.RequestParseUtils.getRequestClass;

/**
 * @author : kevin.liu@ubtrobot.com
 * @description :
 * @date : 2018/5/2
 * @modifier :
 * @modify time :
 */
public class AdbCmdMsgHandler implements IMsgHandler {

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        AdbSwitch.AdbSwitchRequest adbSwitchRequest = getRequestClass(request, AdbSwitch.AdbSwitchRequest.class);
        boolean isOpen = adbSwitchRequest.getOpen();
        SystemUtils.switchAdb(AlphaApplication.getContext(),isOpen);
        sleep(200l);
        //TODO 查一下adb 状态
        boolean usbDebugOpened = SystemUtils.getUsbDebugEnable(AlphaApplication.getContext());

        AdbSwitch.AdbSwitchResponse adbSwitchResponse = AdbSwitch.AdbSwitchResponse.newBuilder().setIsSuccess((usbDebugOpened == isOpen)).build();
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, "1", request.getHeader().getSendSerial(), adbSwitchResponse, peer, new ICallback() {
            @Override
            public void onSuccess(Object data) {

            }

            @Override
            public void onError(ThrowableWrapper e) {
                //TODO
            }
        });
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
