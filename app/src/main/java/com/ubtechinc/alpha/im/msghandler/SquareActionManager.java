package com.ubtechinc.alpha.im.msghandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.Log;
import com.google.protobuf.StringValue;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.appmanager.UbtBatteryManager;
import com.ubtechinc.services.alphamini.R;
import com.ubtrobot.action.ActionApi;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.interactor.MasterInteractor;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.skill.SkillInfo;
import com.ubtrobot.mini.properties.sdk.PropertiesApi;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;
import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

public class SquareActionManager {

  private static Pattern pattern = Pattern.compile(".*(ubx|xml)$");
  private final Context mContext;

  SquareActionManager() {
    this.mContext = AlphaApplication.getContext();
  }

  void callSquareActionSkill(String resourceName, int resourceType,
      @NonNull SquareActionListener listener) {
    MasterInteractor interactor =
        Master.get().getOrCreateInteractor("robot:" + mContext.getPackageName());
    try {
      Log.w("Logic", "resourceName =" + resourceName);
      if (ActionApi.get().unsafeAction()) {
        listener.onActionFailure(1, toMessage(1));
        VoicePool.get().playTTs("等我做完这个动作先", Priority.NORMAL, null);
      } else {
        if (UbtBatteryManager.getInstance().isLowPower()) {
          listener.onActionFailure(2, toMessage(2));
        } else {
          if (isSquareActionSkillRunning(interactor.getStartedSkills())) {
            listener.onActionFailure(3, toMessage(3));
            VoicePool.get().playTTs("等我做完这个动作先", Priority.NORMAL, null);
          } else {
            File file = getResourceFile(resourceName, resourceType);
            if (file == null || !file.exists()) {
              listener.onActionFailure(4, toMessage(4));
            } else {
              interactor.createSkillsProxy()
                  .call("/speechactor/dosquareaction", ProtoParam.create(
                      StringValue.newBuilder().setValue(file.getAbsolutePath()).build()),
                      new ResponseCallback() {
                        @Override public void onResponse(Request request, Response response) {
                          AlphaApplication.getContext().registerReceiver(new BroadcastReceiver() {
                            @Override public void onReceive(Context context, Intent intent) {
                              AlphaApplication.getContext().unregisterReceiver(this);
                              int code = intent.getIntExtra("result", 0);
                              if (code == 0) {
                                listener.onActionFinished();
                              } else {
                                listener.onActionFailure(code, toMessage(code));
                              }
                            }
                          }, new IntentFilter("speechactor.dosquareaction.event"));
                        }

                        @Override public void onFailure(Request request, CallException e) {
                          Log.e("Logic", "call square action skill: code ="
                              + e.getCode()
                              + ", message = "
                              + e.getMessage());
                          if (e.getSubCode() == 0) {
                            listener.onActionFailure(5, toMessage(5));
                          } else {
                            listener.onActionFailure(e.getSubCode(), toMessage(e.getSubCode()));
                          }
                        }
                      });
            }
          }
        }
      }
    } catch (CallException e) {
      //ignore
      Log.w("Logic", "!!!!!!error!!!!!");
      listener.onActionFailure(e.getCode(), toMessage(e.getCode()));
    }
  }

  void stopSquareActionSkill(@NonNull SquareActionListener listener) {
    MasterInteractor interactor =
        Master.get().getOrCreateInteractor("robot:" + mContext.getPackageName());
    if (isSquareActionSkillRunning(interactor.getStartedSkills())) {
      interactor.createSkillsProxy().call("/speechactor/stopsquareaction", new ResponseCallback() {
        @Override public void onResponse(Request request, Response response) {
          listener.onActionFinished();
        }

        @Override public void onFailure(Request request, CallException e) {
          if (e.getSubCode() == 0) {
            listener.onActionFinished();
          } else {
             listener.onActionFailure(e.getSubCode(), toMessage(e.getSubCode()));
          }
        }
      });
    } else {
      listener.onActionFinished();
    }
  }

  private File getResourceFile(String resourceName, int resourceType) {
    if (!pattern.matcher(resourceName).matches()) return null;
    if (resourceType == 1) {
      if (resourceName.endsWith(".ubx")) {//系统
        return new File(PropertiesApi.get().getActionDir(), resourceName);
      } else {
        return new File(PropertiesApi.get().getBehaviorDir(), resourceName);
      }
    }/* else if (resourceType == 2) {//扩展
      if (resourceName.endsWith(".ubx")) {
        return new File(PropertiesApi.get().getActionDir(), resourceName).exists();
      } else {
        return new File(PropertiesApi.get().getBehaviorDir(), resourceName).exists();
      }
    } else if (resourceType == 3) {//自定义
      if (resourceName.endsWith(".ubx")) {
        return new File(PropertiesApi.get().getActionDir(), resourceName).exists();
      } else {
        return new File(PropertiesApi.get().getBehaviorDir(), resourceName).exists();
      }
    }*/
    return null;
  }

  private boolean isSquareActionSkillRunning(List<SkillInfo> startedSkills) {
    for (SkillInfo info : startedSkills) {
      if (info.getName().equals("action_square_actor")) {
        return true;
      }
    }
    return false;
  }

  private String toMessage(int code) {
    switch (code) {
      case 1:
        return getString(R.string.error_unsafe_action);
      case 2:
        return getString(R.string.error_low_power);
      case 3:
        return getString(R.string.error_last_action_no_finished);
      case 4:
        return getString(R.string.error_resource_no_exist);
      case 5:
        return getString(R.string.error_skill_enter_conflict);
      case 6:
        return getString(R.string.error_resource_broken);
      case 7:
        return getString(R.string.error_skill_stopped_by_other);
      case 8:
        return getString(R.string.error_param);
      case 9:
        return getString(R.string.error_unsafe_action);
      default:
        return getString(R.string.error_unknown);
    }
  }

  private String getString(@StringRes int resId) {
    return mContext.getString(resId);
  }

  public interface SquareActionListener {

    void onActionFailure(int errorCode, String msg);

    void onActionFinished();
  }
}
