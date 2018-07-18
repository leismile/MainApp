package com.ubtechinc.alpha.appmanager;

import android.content.res.Resources;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.Log;
import com.google.protobuf.BoolValue;
import com.ubtech.utilcode.utils.CloseUtils;
import com.ubtech.utilcode.utils.FileUtils;
import com.ubtech.utilcode.utils.LogUtils;
import com.ubtech.utilcode.utils.NetworkUtils;
import com.ubtech.utilcode.utils.StringUtils;
import com.ubtech.utilcode.utils.notification.NotificationCenter;
import com.ubtech.utilcode.utils.thread.HandlerUtils;
import com.ubtech.utilcode.utils.thread.ThreadPool;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.event.EnterCriticalUpgradeEvent;
import com.ubtechinc.alpha.network.module.CheckBindRobotModule;
import com.ubtechinc.alpha.robotinfo.RobotState;
import com.ubtechinc.nets.ResponseListener;
import com.ubtechinc.nets.http.HttpProxy;
import com.ubtechinc.nets.http.ThrowableWrapper;
import com.ubtechinc.services.alphamini.R;
import com.ubtrobot.action.ActionApi;
import com.ubtrobot.async.Promise;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.interactor.MasterInteractor;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.skill.SkillsProxy;
import com.ubtrobot.masterevent.protos.RobotGestures;
import com.ubtrobot.masterevent.protos.SysMasterEvent;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;
import com.ubtrobot.upgrade.DetectException;
import com.ubtrobot.upgrade.DownloadException;
import com.ubtrobot.upgrade.DownloadOperationException;
import com.ubtrobot.upgrade.Firmware;
import com.ubtrobot.upgrade.FirmwareDownloader;
import com.ubtrobot.upgrade.FirmwarePackage;
import com.ubtrobot.upgrade.FirmwarePackageGroup;
import com.ubtrobot.upgrade.UpgradeException;
import com.ubtrobot.upgrade.UpgradeManager;
import com.ubtrobot.upgrade.UpgradeProgress;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import ubtechinc.com.standupsdk.StandUpApi;

/**
 * 处理普通OTA下载
 *
 * @author logic
 */

public final class UpgradeClient {
  public static final int UPGRADE_MIN_POWER = 50;
  private final Resources resources = AlphaApplication.getContext().getResources();
  public static final String TAG = "Upgrade";

  private final UpgradeManager upgradeManager;
  private FirmwareDownloader downloader;
  private Timer timer;
  private AtomicInteger timeCount = new AtomicInteger(0);
  private AtomicBoolean speechMonitoring = new AtomicBoolean(false);
  private final byte[] mLock = new byte[0];
  private volatile AtomicBoolean upgrading = new AtomicBoolean(false);
  private volatile DownloadException de;

  public static UpgradeClient get() {
    return Holder._mgr;
  }

  private UpgradeClient() {
    this.upgradeManager = new UpgradeManager(Master.get().getGlobalContext());
  }

  public boolean isUpgrading() {
    return upgrading.get();
  }

  private static class Holder {
    private static UpgradeClient _mgr = new UpgradeClient();
  }

  /**
   * 普通固件下载状态监听
   */
  private FirmwareDownloader.StateListener commonFirmwareDownloadListener =
      new FirmwareDownloader.StateListener() {
        @Override public void onStateChange(FirmwareDownloader firmwareDownloader, int state,
            DownloadException e) {
          de = e;
          if (firmwareDownloader != null && firmwareDownloader.isComplete()) {
            //打开,支持语音指令升级
            checkBatteryAndStartSpeechMonitor(firmwareDownloader.packageGroup());
          } else if (state == FirmwareDownloader.STATE_ERROR) {
            Log.e(TAG, e.getMessage());
            if (NetworkUtils.isWifiConnected()) {
              startDownload(null);
            }
          }
        }
      };

  /**
   * 紧急固件下载状态监听
   */
  private FirmwareDownloader.StateListener criticalFirmwareDownloadListener =
      (firmwareDownloader, state, e) -> {
        if (state == FirmwareDownloader.STATE_ERROR) {//出错, 重新下载
          if (NetworkUtils.isWifiConnected()) {
            startDownload(null);
          } else if (NetworkUtils.is4G()) {
            VoicePool.get()
                .playTTs(getString(R.string.detected_start_download), Priority.HIGH,
                    getDefaultTTsListener());
          }
        } else if (state == FirmwareDownloader.STATE_COMPLETE) {
          NotificationCenter.defaultCenter().publish(new EnterCriticalUpgradeEvent());
        }
      };

  private FirmwareDownloader.ProgressListener progressListener =
      (firmwareDownloader, downloadSize, speed) -> Log.e(TAG,
          "downloaded = " + downloadSize + ", speed =" + speed);

  /**
   * 非紧急固件下载: 默认后台下载,且不需要skill, 如果下载失败,则默认重试
   */
  public void tryToDownloadCommonFirmware(final FirmwarePackageGroup firmwarePackages,
      final StartDownloadListener listener) {
    de = null;
    lazyCreateCommonDownloader();
    _downloadInner(firmwarePackages, listener);
  }

  /**
   * 紧急升级,固件下载
   */
  public void downloadCriticalFirmware(final FirmwarePackageGroup firmwarePackages,
      @NonNull StartDownloadListener listener) {
    lazyCreateCriticalDownloader();
    _downloadInner(firmwarePackages, listener);
  }

  private void lazyCreateCommonDownloader() {
    synchronized (mLock) {
      if (downloader == null) {
        downloader = upgradeManager.firmwareDownloader();
        downloader.registerStateListener(commonFirmwareDownloadListener);
        downloader.registerProgressListener(progressListener);
      } else {
        FirmwarePackageGroup curPackage = downloader.packageGroup();
        if (curPackage != null && curPackage.isForced()) {
          LogUtils.e(TAG, "common firmware error....................");
          downloader.unregisterStateListener(criticalFirmwareDownloadListener);
          downloader.stop();
          downloader.registerStateListener(commonFirmwareDownloadListener);
        }
      }
    }
  }

  private void lazyCreateCriticalDownloader() {
    synchronized (mLock) {
      if (downloader == null) {
        downloader = upgradeManager.firmwareDownloader();
        downloader.registerStateListener(criticalFirmwareDownloadListener);
        downloader.registerProgressListener(progressListener);
      } else {
        FirmwarePackageGroup curPackage = downloader.packageGroup();
        if (curPackage != null && !curPackage.isForced()) {
          LogUtils.e(TAG, "critical firmware error....................");
          downloader.unregisterStateListener(commonFirmwareDownloadListener);
          downloader.stop();
          downloader.registerStateListener(criticalFirmwareDownloadListener);
        }
      }
    }
  }

  private void _downloadInner(final FirmwarePackageGroup firmwarePackages,
      @NonNull final StartDownloadListener listener) {
    if (downloader.isIdle()) {
      Log.v(TAG, "downloader idle");
      readyAndStartDownload(firmwarePackages, listener);
    } else if (downloader.isError()) {
      Log.v(TAG, "downloader error");
      startDownload(listener);
    } else if (downloader.isDownloading()) {
      Log.v(TAG, "downloader downloading");
      listener.onStartSuccess();
    } else {//ready or completed
      if (!downloader.downloadFor(firmwarePackages)) {
        Log.v(TAG, "download for different");
        Promise<Void, DownloadOperationException, Void> clearPromise = downloader.clear();
        clearPromise.done(aVoid -> readyAndStartDownload(firmwarePackages, listener));
        clearPromise.fail(e -> listener.onStartFailure(new Exception("机器人内部错误")));
      } else {
        if (downloader.isComplete()) {
          Log.v(TAG, "downloader complete");
          File file = new File(downloader.packageGroup().getPackage(0).getLocalFile());
          LogUtils.w(TAG, "download file = "
              + file.getPath()
              + " , exist ="
              + file.exists()
              + " , size = "
              + file.length()
              + ", md5 ="
              + downloader.packageGroup().getPackage(0).getPackageMd5()
              + ", md5 = "
              + downloader.packageGroup().getPackage(0).getIncrementMd5());
          // 已经下载好了最新的包,不能start
          listener.onStartSuccess();
        } else {//isReady
          Log.v(TAG, "downloader start");
          Promise<Void, DownloadOperationException, Void> promiseStart = downloader.start();
          promiseStart.done(aVoid -> listener.onStartSuccess());
          promiseStart.fail(e -> listener.onStartFailure(
              new Exception(e.causedByIllegalOperation() ? "下载未ready 或者 completed" : "机器人内部错误")));
        }
      }
    }
  }

  private void checkBatteryAndStartSpeechMonitor(final FirmwarePackageGroup firmwarePackages) {
    SysMasterEvent.BatteryStatusData battery = UbtBatteryManager.getInstance().getBatteryInfo();
    // 电量 > 20 ??
    if (battery.getLevel() > UPGRADE_MIN_POWER) {
      checkBindingStatus(success -> {
        if (success) {
          LogUtils.w(TAG, "common upgrade check bind status success...");
          doSpeechCallMonitorTTs(firmwarePackages);
        }
      });
    } else {
      LogUtils.w(TAG, "quit upgrade: low power.");
    }
  }

  public void checkBindingStatus(final BindingStatusListener listener) {
    final CheckBindRobotModule.Request request =
        new CheckBindRobotModule.Request(RobotState.get().getSid());
    HttpProxy.get().doGet(request, new ResponseListener<CheckBindRobotModule.Response>() {
      @Override public void onError(ThrowableWrapper e) {
        LogUtils.w(TAG, " onError -- e : " + Log.getStackTraceString(e));
        if (listener != null) {
          listener.onBindStatus(false);
        }
      }

      @Override public void onSuccess(CheckBindRobotModule.Response response) {
        if (listener != null) {
          Log.i(TAG, "" + response.toString());
          listener.onBindStatus(response.isSuccess()
              && response.getData() != null
              && response.getData().getResult() != null
              && response.getData().getResult().size() > 0);
        }
      }
    });
  }

  /**
   * 普通升级--语音触发
   */
  public void tryToUpgradeBySpeechCall(final UpgradeListener listener) {
    if (speechMonitoring.compareAndSet(true, false)) {
      if (upgrading.compareAndSet(false, true)) {
        _tryToUpgrade(listener);
      } else {
        listener.onSuccess();
      }
    } else {
      if (downloader != null && downloader.isDownloading()) {
        VoicePool.get()
            .playTTs(getString(R.string.downloading_packages), Priority.NORMAL,
                getDefaultTTsListener());
      } else if (upgrading.get()) {
        if (UbtBatteryManager.getInstance().isRobotAcOrUbs()) {//判断是否充电
          String tts = getString(R.string.upgrading_and_acquire_user_wait);
          VoicePool.get().playTTs(tts, Priority.NORMAL, getDefaultTTsListener());
        } else {
          String tts = getString(R.string.upgrade_acquire_connect_power);
          VoicePool.get().playTTs(tts, Priority.NORMAL, getDefaultTTsListener());
        }
      } else {
        FirmwarePackageGroup group = downloader != null ? downloader.packageGroup() : null;
        if (group != null && group.getPackageCount() >= 1) {
          FirmwarePackage firmwarePackage = group.getPackage(0);
          if (firmwarePackage.getName().equals("android")) {
            if (firmwarePackage.getVersion()
                .equals(upgradeManager.getFirmware("android").getVersion())) {
              VoicePool.get()
                  .playTTs(getString(R.string.no_upgrade_packages), Priority.NORMAL,
                      getDefaultTTsListener());
            } else {
              if (upgrading.compareAndSet(false, true)) {
                _tryToUpgrade(listener);
                return;
              }
            }
          } else {
            if (!firmwarePackage.getVersion()
                .equals(upgradeManager.getFirmware("firmware").getVersion())) {
              if (upgrading.compareAndSet(false, true)) {
                _tryToUpgrade(listener);
                return;
              }
            } else {
              VoicePool.get()
                  .playTTs(getString(R.string.no_upgrade_packages), Priority.NORMAL,
                      getDefaultTTsListener());
            }
          }
        } else {
          VoicePool.get()
              .playTTs(getString(R.string.no_upgrade_packages), Priority.NORMAL,
                  getDefaultTTsListener());
        }
      }
      listener.onFailure(new IllegalStateException("未启动升级指令"));
    }
  }

  /**
   * 普通升级--IM触发
   */
  public void tryToUpgradeByIM(final UpgradeListener listener) {
    if (upgrading.compareAndSet(false, true)) {
      _tryToUpgrade(listener);
    } else {
      listener.onSuccess();
    }
  }

  private void _tryToUpgrade(final UpgradeListener listener) {
    if (UbtBatteryManager.getInstance().isRobotAcOrUbs()) {//判断是否充电
      // TODO: 18-4-9 系统级别播报
      String tts = getString(R.string.upgrade_not_disconnect_power);
      VoicePool.get().playTTs(tts, Priority.NORMAL, getDefaultTTsListener());
      LogUtils.v(TAG, tts);
      upgradeInner(listener);
    } else {
      String tts = getString(R.string.upgrade_acquire_connect_power);
      VoicePool.get().playTTs(tts, Priority.NORMAL, getDefaultTTsListener());
      LogUtils.v(TAG, tts);
      start60SecondChargingCheck(charging -> {
        String tts1;
        if (charging) {
          tts1 = getString(R.string.upgrade_not_disconnect_power);
          VoicePool.get().playTTs(tts1, Priority.NORMAL, getDefaultTTsListener());
          LogUtils.v(TAG, tts1);
          upgradeInner(listener);
        } else {
          tts1 = getString(R.string.upgrade_cancel_tips);
          VoicePool.get().playTTs(tts1, Priority.HIGH, getDefaultTTsListener());
          LogUtils.v(TAG, tts1);
          upgrading.compareAndSet(true, false);
          listener.onFailure(new IllegalStateException("因没有连接电源, 升级取消"));
        }
      });
    }
  }

  /**
   * 紧急升级--开机/IM触发
   */
  public void upgradeForCriticalFirmware(UpgradeListener listener) {
    if (upgrading.compareAndSet(false, true)) {
      _tryToUpgrade(listener);
    } else {
      listener.onSuccess();
    }
  }

  @NonNull private VoiceListener getDefaultTTsListener() {
    return new VoiceListener() {

      @Override public void onCompleted() {

      }

      @Override public void onError(int i, String s) {

      }
    };
  }

  private void start60SecondChargingCheck(final ChargingCheckListener listener) {
    synchronized (mLock) {
      if (timer == null) {
        timer = new Timer();
        timeCount.set(60);
      }
    }
    if (timeCount.decrementAndGet() > 0) {
      timer.schedule(new TimerTask() {
        @Override public void run() {
          if (isCharging()) {
            synchronized (mLock) {
              timer.cancel();
              timer = null;
              timeCount.set(0);
            }
            listener.onCompleted(true);
          } else {
            start60SecondChargingCheck(listener);
          }
        }
      }, 1000);
    } else {
      synchronized (mLock) {
        timer.cancel();
        timer = null;
      }
      listener.onCompleted(false);
    }
  }

  public boolean isCharging() {
    return UbtBatteryManager.getInstance().isRobotAcOrUbs();
  }

  // 语音指令监听
  private void doSpeechCallMonitorTTs(final FirmwarePackageGroup firmwarePackages) {
    speechMonitoring.set(true);
    VoicePool.get()
        .playTTs(getString(R.string.detected_acquire_say_upgrade), Priority.NORMAL,
            new VoiceListener() {
              @Override public void onCompleted() {
                _startSpeechCallMonitoring(firmwarePackages);
              }

              @Override public void onError(int i, String s) {
                _startSpeechCallMonitoring(firmwarePackages);
              }
            });
  }

  private void _startSpeechCallMonitoring(FirmwarePackageGroup firmwarePackages) {
    Log.v(TAG, "start SpeechCall Monitor..");
    //7秒内未收到"升级"指令，提示:
    HandlerUtils.runUITask(() -> {
      if (speechMonitoring.compareAndSet(true, false)) {
        if (firmwarePackages.getPackageCount() == 1 && "firmware".equals(
            firmwarePackages.getPackage(0).getName())) {
          //如果只有胸板固件升级包, 不能在手机端触发升级, 所以播报单独处理
          VoicePool.get()
              .playTTs(getString(R.string.upgrade_cancel_tips_), Priority.NORMAL,
                  getDefaultTTsListener());
        } else {
          VoicePool.get()
              .playTTs(getString(R.string.upgrade_cancel_tips), Priority.NORMAL,
                  getDefaultTTsListener());
        }
      }
    }, 7000);
  }

  private void readyAndStartDownload(@Nullable final FirmwarePackageGroup firmwarePackagesGroup,
      final StartDownloadListener listener) {
    final Promise<Void, DownloadOperationException, Void> promiseReady =
        downloader != null ? downloader.ready(firmwarePackagesGroup)
            : upgradeManager.firmwareDownloader().ready(firmwarePackagesGroup);
    promiseReady.done(aVoid -> {
      Log.v(TAG, "ready success.");
      startDownload(listener);
    });
    promiseReady.fail(e -> {
      Log.e(TAG, "ready failure.");
      if (listener != null) {
        listener.onStartFailure(e);
      }
    });
  }

  private void startDownload(final StartDownloadListener listener) {
    Log.v(TAG,
        "do start download...file =" + downloader.packageGroup().getPackage(0).getLocalFile());
    Promise<Void, DownloadOperationException, Void> promiseStart = downloader.start();
    promiseStart.done(aVoid -> {
      LogUtils.v(TAG, "start download success.");
      if (listener != null) {
        listener.onStartSuccess();
      }
    });
    promiseStart.fail(e -> {
      LogUtils.e(TAG, "start download failure.");
      if (listener != null) {
        listener.onStartFailure(e);
      }
    });
  }

  private void upgradeInner(final UpgradeListener listener) {
    FirmwarePackageGroup firmwarePackageGroup = (downloader != null ? downloader.packageGroup()
        : upgradeManager.firmwareDownloader().packageGroup());
    Promise<Void, UpgradeException, UpgradeProgress> promiseUpgrade =
        upgradeManager.upgrade(firmwarePackageGroup);
    ExpressApi.get().doExpress("awake_001", Short.MAX_VALUE, Priority.HIGH);
    if (StandUpApi.getInstance().getRobotGesture() == RobotGestures.GestureType.STAND) {
      if (UbtBatteryManager.getInstance().isRobotAcOrUbs()) {
        ActionApi.get().playAction("028", Priority.HIGH, null);
      } else {
        ActionApi.get().playAction("031", Priority.HIGH, null);
      }
    }
    writeVersionIntoFile(firmwarePackageGroup);
    promiseUpgrade.done(aVoid -> {
      Log.v(TAG, "upgrade success.");
      if (upgrading.compareAndSet(true, false)) {
        //FIXME 系统升级不能清理升级包
        ExpressApi.get().doExpress("awake_001", 1, Priority.HIGH);
      }
      listener.onSuccess();
    });
    promiseUpgrade.fail(e -> {
      if (upgrading.compareAndSet(true, false)) {
        ExpressApi.get().doExpress("awake_001", 1, Priority.HIGH);
        VoicePool.get().playTTs(getUpgradeErrorMsg(e), Priority.NORMAL, getDefaultTTsListener());
        if (e.causedByExecutingUpgradeError() || e.causedByVerifyingPackageError()) {
          //校验失败, 删除升级包
          if (downloader != null) {
            downloader.clear();
          } else {
            clear();
          }
        }
      }
      LogUtils.e(TAG, e.getMessage());
      listener.onFailure(e);
    });

    promiseUpgrade.progress(upgradeProgress -> Log.v(TAG, "upgrade progress.."));
  }

  public boolean isCompleted() {
    return upgradeManager != null && upgradeManager.firmwareDownloader().isComplete();
  }

  public boolean isDownloading() {
    return upgradeManager != null && upgradeManager.firmwareDownloader().isDownloading();
  }

  public boolean sameWith(FirmwarePackageGroup firmwarePackages) {
    return upgradeManager.firmwareDownloader().downloadFor(firmwarePackages);
  }

  public long totalBytes() {
    return upgradeManager.firmwareDownloader().totalBytes();
  }

  public int speed() {
    return upgradeManager.firmwareDownloader().speed();
  }

  public long downloadBytes() {
    return upgradeManager.firmwareDownloader().downloadedBytes();
  }

  public void detectUpgrade(final StartDetectListener listener) {
    Promise<FirmwarePackageGroup, DetectException, Void> promiseDetect =
        upgradeManager.detectUpgrade();
    promiseDetect.fail(listener::onDetectFailure);

    promiseDetect.done(listener::onDetectSuccess);
  }

  public Firmware getAndroidFirmware() {
    return upgradeManager.getFirmware("android");
  }

  public boolean isUpgradeSuccess() {
    List<Firmware> firmwareList = upgradeManager.getFirmwareList();
    boolean success = false;
    String v = readVersionFromFile();
    LogUtils.w(TAG, "v = " + v + ", firmwareList = " + firmwareList);
    if (!TextUtils.isEmpty(v) && firmwareList != null && firmwareList.size() == 2) {
      LogUtils.w(TAG, v + " <---> " + firmwareList.toString());
      String[] vv = v.split("_");
      success = vv[0].equals(firmwareList.get(0).getVersion());
      if (success && vv.length == 2) {
        success = vv[1].equals(firmwareList.get(1).getVersion());
      }
    }
    return success;
  }

  /***
   * 只有在没有下载的情况下, 清除
   */
  public void clear() {
    synchronized (mLock) {
      if (downloader == null) {
        //FIXME 只有在没有下载的情况下, 清除
        upgradeManager.firmwareDownloader().clear();
      }
    }
  }

  public interface StartDownloadListener {
    void onStartSuccess();

    void onStartFailure(Exception e);
  }

  public interface UpgradeListener {
    void onSuccess();

    void onFailure(Exception e);
  }

  public interface ChargingCheckListener {
    void onCompleted(boolean charging);
  }

  public interface StartDetectListener {
    void onDetectSuccess(FirmwarePackageGroup firmwarePackages);

    void onDetectFailure(DetectException e);
  }

  public interface BindingStatusListener {
    void onBindStatus(boolean success);
  }

  public static void fakeSpeechUpgradeTrigger(final @Nullable UpgradeListener listener) {
    //FIXME 同一进程内call skill
    ThreadPool.runOnNonUIThread(() -> {
      String packageName = AlphaApplication.getContext().getPackageName();
      MasterInteractor interactor = Master.get().getOrCreateInteractor("robot:" + packageName);
      SkillsProxy aSkillsProxy = interactor.createSkillsProxy();
      ProtoParam param = ProtoParam.create(BoolValue.newBuilder().setValue(true).build());
      aSkillsProxy.call("/ota/commonUpgrade", param, new ResponseCallback() {
        @Override public void onResponse(Request request, Response response) {
          if (listener != null) listener.onSuccess();
        }

        @Override public void onFailure(Request request, CallException e) {
          LogUtils.e(TAG, "trigger common upgrade failure: " + e.getMessage());
          if (listener != null) listener.onFailure(e);
        }
      });
    });
  }

  public static void criticalUpgradeTrigger(final @Nullable UpgradeListener listener) {
    //FIXME 同一进程内call skill
    ThreadPool.runOnNonUIThread(() -> {
      String packageName = AlphaApplication.getContext().getPackageName();
      MasterInteractor interactor = Master.get().getOrCreateInteractor("robot:" + packageName);
      SkillsProxy aSkillsProxy = interactor.createSkillsProxy();
      aSkillsProxy.call("/ota/criticalUpgrade", new ResponseCallback() {
        @Override public void onResponse(Request request, Response response) {
          if (listener != null) listener.onSuccess();
        }

        @Override public void onFailure(Request request, CallException e) {
          LogUtils.e(TAG, "trigger critical upgrade failure: " + e.getMessage());
          if (listener != null) listener.onFailure(e);
        }
      });
    });
  }

  public static String getDetectErrorMsg(DetectException e) {
    if (e.causedByTimeout()) {
      return "网络超时";
    } else if (e.causedByServerError()) {
      return "服务器错误";
    } else if (e.causedByFailedToEstablishConnection()) {
      return "无法建立网络链接";
    } else {
      return "机器人内部错误, 未联网?";
    }
  }

  public String getDownloadErrorMsg() {
    if (de == null) return null;
    if (de.causedByFileServerError()) {
      return "文件服务器错误";
    } else if (de.causedByNetworkDisconnected()) {
      return "网络断开链接";
    } else if (de.causedByNetworkTimeout()) {
      return "网络超时";
    } else if (de.causedByInsufficientSpace()) {
      return "机器人存储空间不足";
    } else {
      return "机器人内部错误, 未联网?";
    }
  }

  private static String getUpgradeErrorMsg(UpgradeException ue) {
    if (ue.causedByExecutingUpgradeError()) {
      return AlphaApplication.getContext().getString(R.string.err_firmware_check_failure);
    } else if (ue.causedByProhibitReentry()) {
      return AlphaApplication.getContext().getString(R.string.err_has_upgraded);
    } else if (ue.causedByVerifyingPackageError()) {
      return AlphaApplication.getContext().getString(R.string.err_android_check_failure);
    } else {
      return "机器人内部错误";
    }
  }

  private String getString(@StringRes int strId) {
    return resources.getString(strId);
  }

  private static String readVersionFromFile() {
    File file = new File(Environment.getExternalStorageDirectory().getPath(), "t_version");
    if (file.exists()) {
      BufferedReader reader = null;
      String line;
      try {
        if (StringUtils.isSpace("utf-8")) {
          reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        } else {
          reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
        }
        line = reader.readLine();
      } catch (IOException e) {
        LogUtils.e(TAG, "" + e.getMessage());
        line = null;
      } finally {
        CloseUtils.closeIO(reader);
      }
      if (file.delete()) Log.v(TAG, "delete: " + file.getPath());
      return line;
    } else {
      return null;
    }
  }

  private void writeVersionIntoFile(FirmwarePackageGroup firmwarePackageGroup) {
    File file = new File(Environment.getExternalStorageDirectory().getPath(), "t_version");
    if (FileUtils.createFileByDeleteOldFile(file)) {
      String v = firmwarePackageGroup.getPackage(0).getVersion();
      if (firmwarePackageGroup.getPackageCount() == 2) {
        v += "_" + firmwarePackageGroup.getPackage(1).getVersion();
      }
      FileUtils.writeFileFromString(file, v, false);
    }
  }
}
