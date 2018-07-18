package com.ubtechinc.alpha.im.msghandler;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.FirmwareUpgradeProto;
import com.ubtechinc.alpha.appmanager.UbtBatteryManager;
import com.ubtechinc.alpha.appmanager.UpgradeClient;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtrobot.masterevent.protos.SysMasterEvent;

/**
 * Created by logic on 18-4-8.
 *
 * @author logic
 */

public class UpgradeFirmwareHandler implements IMsgHandler {
  private static final String TAG = "Logic";

  @Override public void handleMsg(int requestCmdId, final int responseCmdId,
      AlphaMessageOuterClass.AlphaMessage request, final String peer) {

    final long requestSerial = request.getHeader().getSendSerial();

    if (!UpgradeClient.get().isCharging()) {
      RobotPhoneCommuniteProxy.getInstance()
          .sendResponseMessage(responseCmdId, "1", requestSerial,
              FirmwareUpgradeProto.UpgradeResponse.newBuilder()
                  .setState(FirmwareUpgradeProto.UpgradeState.NO_CHARGING)
                  .build(), peer, null);

      return;
    }

    SysMasterEvent.BatteryStatusData data = UbtBatteryManager.getInstance().getBatteryInfo();
    if (data.getLevel() < 50) {
      RobotPhoneCommuniteProxy.getInstance()
          .sendResponseMessage(responseCmdId, "1", requestSerial,
              FirmwareUpgradeProto.UpgradeResponse.newBuilder()
                  .setState(FirmwareUpgradeProto.UpgradeState.LOW_POWER)
                  .build(), peer, null);

      return;
    }

    UpgradeClient.fakeSpeechUpgradeTrigger(new UpgradeClient.UpgradeListener() {
      @Override public void onSuccess() {
        RobotPhoneCommuniteProxy.getInstance()
            .sendResponseMessage(responseCmdId, "1", requestSerial,
                FirmwareUpgradeProto.UpgradeResponse.newBuilder()
                    .setState(FirmwareUpgradeProto.UpgradeState.OK)
                    .build(), peer, null);
      }

      @Override public void onFailure(Exception e) {
        RobotPhoneCommuniteProxy.getInstance()
            .sendResponseMessage(responseCmdId, "1", requestSerial,
                FirmwareUpgradeProto.UpgradeResponse.newBuilder()
                    .setState(FirmwareUpgradeProto.UpgradeState.SKILL_FORBIDDEN)
                    .build(), peer, null);
      }
    });
  }
}
