/*
 *
 *  *
 *  * Copyright (c) 2008-2016 UBT Corporation.  All rights reserved.  Redistribution,
 *  *  modification, and use in source and binary forms are not permitted unless otherwise authorized by UBT.
 *  *
 *
 */

package com.ubtechinc.alpha.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.ubtech.utilcode.utils.LogUtils;
import com.ubtech.utilcode.utils.NetworkUtils;
import com.ubtech.utilcode.utils.thread.HandlerUtils;
import com.ubtechinc.alpha.appmanager.UpgradeClient;
import com.ubtechinc.alpha.utils.EmotionUtils;
import com.ubtechinc.alpha.utils.ServiceUtils;
import com.ubtrobot.mini.libs.scenes.SceneScaner;
import com.ubtrobot.upgrade.DetectException;
import com.ubtrobot.upgrade.FirmwarePackageGroup;

/**
 * @author paul.zhang@ubtrobot.com
 * @date 2016/12/27
 * @Description 开、关机广播
 * @modifier
 * @modify_time
 */

public class BootReceiver extends BroadcastReceiver {
  public static final String TAG = "BootReceiver";

  @Override public void onReceive(final Context context, Intent intent) {
    if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
      Log.i(TAG, "收到开机启动广播...");
      ServiceUtils.startService(context);
      EmotionUtils.calculateRobotRunningTime(context);
      EmotionUtils.tryUpdateFitness(context);
      HandlerUtils.runUITask(new Runnable() {
        @Override public void run() {
          detectUpgrade(UpgradeClient.get());
        }
      }, 25000);
    } else if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
      //关机时，记录本次开机时长
      EmotionUtils.calculateRobotRunningTime(context);
    } else if ("com.ubtrobot.mini.action.SCAN_SCENE".equals(intent.getAction())) {
      //扫描场景文件
      SceneScaner.scan();
    }
  }

  private void detectUpgrade(final UpgradeClient otaClient) {
    otaClient.detectUpgrade(new UpgradeClient.StartDetectListener() {
      @Override public void onDetectSuccess(FirmwarePackageGroup firmwarePackages) {
        if (firmwarePackages.isForced()) {
          //FIXME 绑定成功才能升级
          LogUtils.i(TAG, "detect force upgrade packages...");
          UpgradeClient.get().checkBindingStatus(new UpgradeClient.BindingStatusListener() {
            @Override public void onBindStatus(boolean success) {
              if (success) {
                LogUtils.i(TAG, "trigger force upgrade skill...");
                UpgradeClient.criticalUpgradeTrigger(null);
              }
            }
          });
        } else if (firmwarePackages.getPackageCount() > 0) {
          otaClient.tryToDownloadCommonFirmware(firmwarePackages,
              new UpgradeClient.StartDownloadListener() {
                @Override public void onStartSuccess() {
                  Log.i(TAG, "start download success..");
                }

                @Override public void onStartFailure(Exception e) {
                  LogUtils.e(TAG, e.getMessage());
                }
              });
        }
      }

      @Override public void onDetectFailure(DetectException e) {
        LogUtils.e(TAG, UpgradeClient.getDetectErrorMsg(e));
        if (e.causedByInternalError() && NetworkUtils.isWifiConnected()) {
          detectUpgrade(otaClient);
        }
      }
    });
  }
}
