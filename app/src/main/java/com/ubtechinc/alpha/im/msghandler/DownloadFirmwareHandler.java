package com.ubtechinc.alpha.im.msghandler;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.DownloadFirmwareProto;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.appmanager.UpgradeClient;
import com.ubtechinc.alpha.im.TecentIMManager;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtrobot.upgrade.DetectException;
import com.ubtrobot.upgrade.FirmwarePackageGroup;

/**
 * Created by logic on 18-4-8.
 *
 * @author logic
 */

public class DownloadFirmwareHandler implements IMsgHandler {

  @Override public void handleMsg(int requestCmdId, final int responseCmdId,
      AlphaMessageOuterClass.AlphaMessage request, final String peer) {
    final long requestSerial = request.getHeader().getSendSerial();
    final DownloadFirmwareProto.DownloadFirmware.Builder downloadResponse =
        DownloadFirmwareProto.DownloadFirmware.newBuilder();
    final UpgradeClient otaClient = UpgradeClient.get();
    otaClient.detectUpgrade(new UpgradeClient.StartDetectListener() {
      @Override public void onDetectSuccess(FirmwarePackageGroup firmwarePackages) {
        if (firmwarePackages.isForced()) {
          UpgradeClient.criticalUpgradeTrigger(new UpgradeClient.UpgradeListener() {
            @Override public void onSuccess() {
              //downloadResponse.setResult(1);
              //RobotPhoneCommuniteProxy.getInstance()
              //    .sendResponseMessage(responseCmdId, "1", requestSerial,
              //        downloadResponse.build(), peer, null);
            }

            @Override public void onFailure(Exception e) {
              downloadResponse.setResult(2);
              downloadResponse.setErrMsg(e.getMessage() + "");
              RobotPhoneCommuniteProxy.getInstance()
                  .sendResponseMessage(responseCmdId, "1", requestSerial,
                      downloadResponse.build(), peer, null);
            }
          });
        } else if (firmwarePackages.getPackageCount() > 0){
          otaClient.tryToDownloadCommonFirmware(firmwarePackages,
              new UpgradeClient.StartDownloadListener() {
                @Override public void onStartSuccess() {
                  downloadResponse.setResult(1);
                  RobotPhoneCommuniteProxy.getInstance()
                      .sendResponseMessage(responseCmdId, "1", requestSerial,
                          downloadResponse.build(), peer, null);
                }

                @Override public void onStartFailure(Exception e) {
                  downloadResponse.setResult(2);
                  downloadResponse.setErrMsg(e.getMessage() + "");
                  RobotPhoneCommuniteProxy.getInstance()
                      .sendResponseMessage(responseCmdId, "1", requestSerial,
                          downloadResponse.build(), peer, null);
                }
              });
        }
      }

      @Override public void onDetectFailure(DetectException e) {
        downloadResponse.setResult(2);
        downloadResponse.setErrMsg(UpgradeClient.getDetectErrorMsg(e));
        RobotPhoneCommuniteProxy.getInstance()
            .sendResponseMessage(responseCmdId, "1", requestSerial, downloadResponse.build(), peer,
                null);
      }
    });
  }
}
