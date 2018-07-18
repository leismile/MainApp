package com.ubtechinc.alpha.service;

import android.support.annotation.NonNull;
import android.util.Log;
import com.ubtech.utilcode.utils.LogUtils;
import com.ubtech.utilcode.utils.NetworkUtils;
import com.ubtech.utilcode.utils.notification.NotificationCenter;
import com.ubtech.utilcode.utils.notification.Subscriber;
import com.ubtech.utilcode.utils.thread.HandlerUtils;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.appmanager.UbtBatteryManager;
import com.ubtechinc.alpha.appmanager.UpgradeClient;
import com.ubtechinc.alpha.event.EnterCriticalUpgradeEvent;
import com.ubtechinc.alpha.event.SidEvent;
import com.ubtechinc.alpha.im.TecentIMManager;
import com.ubtechinc.services.alphamini.R;
import com.ubtrobot.action.ActionApi;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.skill.MasterSkill;
import com.ubtrobot.master.skill.SkillStopCause;
import com.ubtrobot.masterevent.protos.SysMasterEvent;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.motor.MotorApi;
import com.ubtrobot.speech.SpeechApi;
import com.ubtrobot.speech.protos.Speech;
import com.ubtrobot.speech.receivers.WakeupReceiver;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;
import com.ubtrobot.upgrade.DetectException;
import com.ubtrobot.upgrade.FirmwarePackageGroup;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ubtechinc.alpha.appmanager.UpgradeClient.UPGRADE_MIN_POWER;

/**
 * Created by logic on 18-4-16.
 *
 * @author logic 强制升级skill
 */

public class CriticalUpgradeSkill extends MasterSkill
    implements UpgradeClient.StartDownloadListener {
  private static final String TAG = "CriticalUpgrade";
  final UpgradeClient upgradeClient = UpgradeClient.get();
  private FirmwarePackageGroup firmwarePackageGroup;
  private volatile AtomicBoolean firstEnter = new AtomicBoolean(true);
  private AtomicBoolean checking = new AtomicBoolean(false);
  private AtomicInteger checkCount = new AtomicInteger(15);
  private Subscriber<EnterCriticalUpgradeEvent> subscriber = enterCriticalUpgradeEvent -> {
    if (upgradeClient.isCompleted()) {
      checkChargingUntilStartUpgrade();
    }
  };
  private WakeupReceiver receiver = new WakeupReceiver() {
    @Override public void onWakeup(Speech.WakeupParam data) {
      if (!NetworkUtils.isWifiConnected()) {//wifi没链接
        if (!upgradeClient.isCompleted()) {//没有下载完成
          VoicePool.get()
              .playTTs(getString(R.string.detected_acquire_wifi), Priority.HIGH, getTTsListener());
        } else {
          if (!upgradeClient.isCharging()) {//没有充电
            VoicePool.get()
                .playTTs(getString(R.string.upgrade_connect_power), Priority.HIGH,
                    getTTsListener());
          } else {
            SysMasterEvent.BatteryStatusData battery =
                UbtBatteryManager.getInstance().getBatteryInfo();
            // 电量 > 50 ??
            if (battery.getLevel() <= UPGRADE_MIN_POWER) {
              VoicePool.get()
                  .playTTs(getString(R.string.upgrade_acquire_power_50), Priority.HIGH,
                      getTTsListener());
            } else {
              Log.w(TAG, "ignore...");
            }
          }
        }
      } else {
        if (!upgradeClient.isCompleted()) {//下载完成
          VoicePool.get()
              .playTTs(getString(R.string.downloading_packages2), Priority.HIGH, getTTsListener());
        } else {
          if (!upgradeClient.isCharging()) {//没有充电
            VoicePool.get()
                .playTTs(getString(R.string.upgrade_acquire_connect_power), Priority.HIGH,
                    getTTsListener());
          } else {
            SysMasterEvent.BatteryStatusData battery =
                UbtBatteryManager.getInstance().getBatteryInfo();
            // 电量 > 50 ??
            if (battery.getLevel() <= UPGRADE_MIN_POWER) {
              VoicePool.get()
                  .playTTs(getString(R.string.upgrade_acquire_power_50), Priority.HIGH,
                      getTTsListener());
            } else {
              Log.w(TAG, "ignore...");
            }
          }
        }
      }
    }
  };

  // 强制下载更新，进入skill之后，
  @Call(path = "/ota/criticalUpgrade") public void onFirmwareDownload(Request request,
      final Responder responder) {
    if (!firstEnter.get() || checking.get() || upgradeClient.isUpgrading()) {
      responder.respondSuccess();
      return;
    }
    //蹲下, 不锁位, 停止录音
    ActionApi.get().playAction("028", Priority.MAXHIGH, new ResponseListener<Void>() {
      @Override public void onResponseSuccess(Void aVoid) {
        MotorApi.get().unlockAllMotor(null);
        //FIXME: 18-4-16 紧急升级 ----断开IM
        TecentIMManager.getInstance(AlphaApplication.getContext()).logout();
        responder.respondSuccess();
        checkCount.set(10);
        NotificationCenter.defaultCenter().subscriber(EnterCriticalUpgradeEvent.class, subscriber);
        checkWifiConnectForDownload();
      }

      @Override public void onFailure(int i, @NonNull String s) {
        LogUtils.e(TAG, "critical upgrade failure: " + s);
        responder.respondFailure(i, "upgrade failed: " + s);
        stopSkill();
      }
    });
  }

  private void checkWifiConnectForDownload() {
    checking.set(true);
    String tts = getResources().getString(R.string.detected_acquire_wifi);
    if (NetworkUtils.is4G() || !NetworkUtils.isConnected()) {
      VoicePool.get().playTTs(tts, Priority.HIGH, getTTsListener());
      HandlerUtils.runUITask(() -> {
        if (checkCount.decrementAndGet() > 0) {
          checkWifiConnectForDownload();
        } else {
          checking.set(false);
          VoicePool.get()
              .playTTs(getString(R.string.upgrade_cancel_tips_), Priority.HIGH, getTTsListener());
          stopSkill();
        }
      }, 1000 * 60);
    } else if (NetworkUtils.isWifiConnected()) {
      //下载
      tryDetectOrDownload();
      checking.set(false);
    }
  }

  private void tryDetectOrDownload() {
    if (firmwarePackageGroup == null) {
      _tryDetectAndDownload();
      if (firstEnter.compareAndSet(true, false)) {// 只有第一次创建skill 播报:现在开始下载升级包,请为我连接电源准备升级
        String tts = getResources().getString(R.string.detected_start_download);
        VoicePool.get().playTTs(tts, Priority.HIGH, getTTsListener());
      }
    } else {
      if (firmwarePackageGroup.getPackageCount() > 0) {
        upgradeClient.downloadCriticalFirmware(firmwarePackageGroup, this);
      } else {
        LogUtils.e(TAG, "!!!!升级数据异常!!!!!");
        stopSkill();
      }
    }
  }

  private void _tryDetectAndDownload() {
    upgradeClient.detectUpgrade(new UpgradeClient.StartDetectListener() {
      @Override public void onDetectSuccess(final FirmwarePackageGroup firmwarePackages) {
        firmwarePackageGroup = firmwarePackages;
        if (firmwarePackageGroup.getPackageCount() > 0) {
          upgradeClient.downloadCriticalFirmware(firmwarePackageGroup, CriticalUpgradeSkill.this);
          SpeechApi.get().subscribeEvent(receiver);
        } else {
          LogUtils.e(TAG, "!!!!升级数据异常!!!!!");
          stopSkill();
        }
      }

      @Override public void onDetectFailure(DetectException e) {
        _tryDetectAndDownload();
      }
    });
  }

  @NonNull private VoiceListener getTTsListener() {
    return new VoiceListener() {

      @Override public void onCompleted() {

      }

      @Override public void onError(int i, String s) {

      }
    };
  }

  @Override protected void onSkillStart() {
  }

  @Override protected void onSkillStop(SkillStopCause skillStopCause) {
    firstEnter.set(true);
    //FIXME 重新链接IM
    NotificationCenter.defaultCenter().publish(new SidEvent());
    NotificationCenter.defaultCenter().unsubscribe(EnterCriticalUpgradeEvent.class, subscriber);
    SpeechApi.get().unsubscribeEvent(receiver);
  }

  /**
   * 启动下载成功
   */
  @Override public void onStartSuccess() {
  }

  private void checkChargingUntilStartUpgrade() {
    HandlerUtils.runUITask(() -> {
      if (upgradeClient.isCharging()) {
        SysMasterEvent.BatteryStatusData battery = UbtBatteryManager.getInstance().getBatteryInfo();
        if (battery.getLevel() > UPGRADE_MIN_POWER) {
          VoicePool.get()
              .playTTs(getString(R.string.upgrade_not_disconnect_power), Priority.HIGH,
                  getTTsListener());
          upgradeClient.upgradeForCriticalFirmware(new UpgradeClient.UpgradeListener() {
            @Override public void onSuccess() {
              //FIXME Don't stopSkill in Here
            }

            @Override public void onFailure(Exception e) {
              VoicePool.get().playTTs(e.getMessage(), Priority.HIGH, getTTsListener());
              stopSkill();//升级失败, 紧急升级skill 退出
            }
          });
        } else {
          VoicePool.get()
              .playTTs(getString(R.string.prepare_acquire_user_wait), Priority.HIGH,
                  getTTsListener());
        }
      } else {
        VoicePool.get()
            .playTTs(getString(R.string.upgrade_connect_power), Priority.HIGH, getTTsListener());
      }
    }, 1000 * 25);
  }

  @Override public void onStartFailure(Exception e) {
    LogUtils.e(TAG, e.getMessage());
    //紧急升级, 不断的重试下载
    upgradeClient.downloadCriticalFirmware(firmwarePackageGroup, this);
  }
}
