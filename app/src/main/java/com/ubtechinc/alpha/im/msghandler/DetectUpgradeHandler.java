package com.ubtechinc.alpha.im.msghandler;

import android.text.TextUtils;
import com.ubtech.utilcode.utils.SystemProperty;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.DetectUpgradeProto;
import com.ubtechinc.alpha.appmanager.UpgradeClient;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtrobot.upgrade.DetectException;
import com.ubtrobot.upgrade.Firmware;
import com.ubtrobot.upgrade.FirmwarePackage;
import com.ubtrobot.upgrade.FirmwarePackageGroup;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Created by logic on 18-4-8.
 *
 * @author logic
 */

public class DetectUpgradeHandler implements IMsgHandler {

  @Override public void handleMsg(int requestCmdId, final int responseCmdId,
      AlphaMessageOuterClass.AlphaMessage request, final String peer) {
    final long requestSerial = request.getHeader().getSendSerial();
    final DetectUpgradeProto.DetectUpgrade.Builder detectResponse =
        DetectUpgradeProto.DetectUpgrade.newBuilder();
    final UpgradeClient client = UpgradeClient.get();
    client.detectUpgrade(new UpgradeClient.StartDetectListener() {
      @Override public void onDetectSuccess(FirmwarePackageGroup firmwarePackages) {
        if (firmwarePackages.getPackageCount() == 0) {
          fillNoFirmwareUpgradeInfo(detectResponse);
        } else {
          Iterator<FirmwarePackage> iterator = firmwarePackages.iterator();
          do {
            FirmwarePackage firmwarePackage = iterator.next();
            if (firmwarePackage.getName().equals("android")) {
              detectResponse.addFirmwareInfo(DetectUpgradeProto.FirmwareInfo.newBuilder()
                  .setGroup(firmwarePackage.getGroup())
                  .setName(firmwarePackage.getName())
                  .setReleaseNote(firmwarePackage.getReleaseNote())
                  .setVersion(firmwarePackage.getVersion())
                  .setReleaseTime(firmwarePackage.getReleaseTime()));
            }
          } while (iterator.hasNext());

          if (detectResponse.getFirmwareInfoCount() == 1) {
            if (client.isDownloading()) {
              detectResponse.setState(DetectUpgradeProto.DetectState.IS_DOWNLOADING);
            } else if (client.isCompleted() && client.sameWith(firmwarePackages)) {
              detectResponse.setState(DetectUpgradeProto.DetectState.HAS_DOWNLOADED);
            } else {
              detectResponse.setState(DetectUpgradeProto.DetectState.HAS_UPDATE);
            }
            if (firmwarePackages.isForced()) {
              UpgradeClient.criticalUpgradeTrigger(null);
            }
          } else {
            fillNoFirmwareUpgradeInfo(detectResponse);
          }
        }
        RobotPhoneCommuniteProxy.getInstance()
            .sendResponseMessage(responseCmdId, "1", requestSerial, detectResponse.build(), peer,
                null);
      }

      @Override public void onDetectFailure(DetectException e) {
        detectResponse.setErrMsg(UpgradeClient.getDetectErrorMsg(e));
        RobotPhoneCommuniteProxy.getInstance()
            .sendResponseMessage(responseCmdId, "1", requestSerial, detectResponse.build(), peer,
                null);
      }
    });
  }

  private void fillNoFirmwareUpgradeInfo(DetectUpgradeProto.DetectUpgrade.Builder detectResponse) {
    Firmware androidFirmware = UpgradeClient.get().getAndroidFirmware();
    detectResponse.addFirmwareInfo(DetectUpgradeProto.FirmwareInfo.newBuilder()
        .setGroup("AlphaMini")
        .setName("android")
        .setVersion(androidFirmware == null ? getAndroidFirmwareVersion()
            : androidFirmware.getVersion() + "")
        .setReleaseNote(getReleaseNote(androidFirmware))
        .setReleaseTime(androidFirmware == null ? System.currentTimeMillis()
            : androidFirmware.getUpgradeTime()));
    detectResponse.setState(DetectUpgradeProto.DetectState.NO_UPDATE);
  }

  private String getAndroidFirmwareVersion() {
    Pattern androidPattern = Pattern.compile("^Alpha_Mini-\\d{4}-\\d{4}-\\d{4}-V\\d.\\d.\\d");
    String sys_version = SystemProperty.getProperty("ro.build.display.id");
    if (!TextUtils.isEmpty(sys_version) && androidPattern.matcher(sys_version).matches()) {
      sys_version = sys_version.substring(sys_version.length() - 6).toLowerCase();
    } else {
      sys_version = "v0.0.0";
    }
    return sys_version;
  }

  private String getReleaseNote(Firmware firmware) {
    if (firmware == null) return "";
    FirmwarePackage firmwarePackage = firmware.getCurrentPackage();
    if (firmwarePackage == null) return "";
    return firmwarePackage.getReleaseNote() + "";
  }
}
