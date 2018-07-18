package com.ubtechinc.alpha.service;

import com.google.protobuf.BoolValue;
import com.ubtechinc.alpha.appmanager.UpgradeClient;
import com.ubtrobot.im.robot.exception.NetException;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.skill.MasterSkill;
import com.ubtrobot.master.skill.SkillStopCause;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

/**
 * 处理OTA升级和紧急包下载
 *
 * @author logic
 */

public final class UpgradeSkill extends MasterSkill {

  public static final String TAG = "UpgradeSkill";

  @Call(path = "/ota/commonUpgrade") public void onUpgrade(Request request, Responder responder) {
    synchronized (this) {
      if (UpgradeClient.get().isUpgrading()) {
        responder.respondSuccess();
        return;
      }
      if (request.getParam().isEmpty()) {
        UpgradeClient.get().tryToUpgradeBySpeechCall(new UpgradeClient.UpgradeListener() {
          @Override public void onSuccess() {
            //FIXME Don't stopSkill in Here
          }

          @Override public void onFailure(Exception e) {
            stopSkill();
          }
        });
        responder.respondSuccess();//只要启动了调用,则回一个Success
      } else {
        try {
          if (ProtoParam.from(request.getParam(), BoolValue.class).getProtoMessage().getValue()) {
            UpgradeClient.get().tryToUpgradeByIM(new UpgradeClient.UpgradeListener() {
              @Override public void onSuccess() {
                //FIXME Don't stopSkill in Here
              }

              @Override public void onFailure(Exception e) {
                stopSkill();
              }
            });
          } else {
            UpgradeClient.get().tryToUpgradeBySpeechCall(new UpgradeClient.UpgradeListener() {
              @Override public void onSuccess() {
                //FIXME Don't stopSkill in Here
              }

              @Override public void onFailure(Exception e) {
                stopSkill();
              }
            });
          }
          responder.respondSuccess();//只要启动了调用,则回一个Success
        } catch (ProtoParam.InvalidProtoParamException e) {
          e.printStackTrace();
          responder.respondFailure(
              new CallException(NetException.GlobalCode.ERROR_INVALID_PARAM, "invalid param", e));
        }
      }
    }
  }

  @Override protected void onSkillStart() {
  }

  @Override protected void onSkillStop(SkillStopCause skillStopCause) {
  }
}
