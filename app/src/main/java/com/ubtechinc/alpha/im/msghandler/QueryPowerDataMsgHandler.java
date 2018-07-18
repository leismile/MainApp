package com.ubtechinc.alpha.im.msghandler;

import android.util.Log;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmQueryPowerData;
import com.ubtechinc.alpha.appmanager.UbtBatteryManager;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtrobot.masterevent.protos.SysMasterEvent;

/**
 * Created by Administrator on 2017/6/6 0006.
 */

public class QueryPowerDataMsgHandler implements IMsgHandler {
    private static final String TAG = "QueryPowerDataMsgHandler";

    @Override
    public void handleMsg(int requestCmdId, final int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, final String peer) {
        final long requestSerial = request.getHeader().getSendSerial();
        SysMasterEvent.BatteryStatusData batteryStatusData = UbtBatteryManager.getInstance().getBatteryInfo();
        int power = batteryStatusData.getLevel();
        int statu = batteryStatusData.getStatus();
        int levelStatus = batteryStatusData.getLevelStatus();
        Log.i("QueryPowerDataHandler","power=======" + power);
        CmQueryPowerData.CmQueryPowerDataResponse.Builder dataResponse = CmQueryPowerData.CmQueryPowerDataResponse.newBuilder();
        dataResponse.setPowerValue(power);
        dataResponse.setStatu(statu);
        dataResponse.setLevelStatus(levelStatus);
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, "1", requestSerial, dataResponse.build(), peer, null);
    }
}

