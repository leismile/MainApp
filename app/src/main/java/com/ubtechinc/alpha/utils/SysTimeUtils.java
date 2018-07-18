package com.ubtechinc.alpha.utils;

import android.app.AlarmManager;
import android.content.Context;
import android.provider.Settings;
import android.support.annotation.NonNull;
import com.ubtechinc.alpha.robotinfo.RobotConfiguration;
import com.ubtechinc.alpha.robotinfo.RobotLanguage;
import com.yanzhenjie.permission.PermissionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.content.Context.ALARM_SERVICE;

/**
 * @author wzt
 * @date 2017/5/22
 * @Description 系统时间设置工具类
 * @modifier
 * @modify_time
 */

public class SysTimeUtils {

  public static void autoUpdateTimeAndTimeZoo(Context context) {
    //AndPermission.with(context)
    //    .requestCode(100)
    //    .permission(Manifest.permission.WRITE_SECURE_SETTINGS)
    //    .callback(new WriteSecurePermissionListener(context))
    //    .start();
    if (Settings.Global.getInt(context.getContentResolver(), Settings.Global.AUTO_TIME, 0) == 0) {
      Settings.Global.putInt(context.getContentResolver(), Settings.Global.AUTO_TIME, 1);
    }
    if (Settings.Global.getInt(context.getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0)
        == 0) {
      Settings.Global.putInt(context.getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 1);
    }
    if (RobotLanguage.CN.equals(RobotConfiguration.get().asr_Language)) {
      AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
      am.setTimeZone("GMT+08:00");
    }
  }

  static class WriteSecurePermissionListener implements PermissionListener {
    private Context mContext;

    protected WriteSecurePermissionListener(Context context) {
      this.mContext = context;
    }

    @Override public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
      if (Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.AUTO_TIME, 0)
          == 0) {
        Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.AUTO_TIME, 1);
      }
      if (Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0)
          == 0) {
        Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 1);
      }
      if (RobotLanguage.CN.equals(RobotConfiguration.get().asr_Language)) {
        AlarmManager am = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
        am.setTimeZone("GMT+08:00");
      }
    }

    @Override public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {

    }
  }

  /**
   * 判断当前时间的哪个时间段
   */
  public static int periodOfTheCurrentTime() {
    String[] dates = new String[5];
    dates[0] = "6:00-8:59";
    dates[1] = "9:00-10:59";
    dates[2] = "11:00-12:59";
    dates[3] = "13:00-18:59";
    dates[4] = "19:00-5:59";
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    Date now = new Date();
    int index = 0;
    try {
      now = sdf.parse(sdf.format(now));
      for (int i = 0; i < dates.length - 1; i++) {
        if (is(now, dates[i])) {
          break;
        }
        index++;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return index;
  }

  public static boolean is(Date now, String arg) throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    String[] s = arg.split("-");
    Date start = sdf.parse(s[0]);
    Date end = sdf.parse(s[1]);
    return start.getTime() <= now.getTime() && end.getTime() >= now.getTime();
  }
}
