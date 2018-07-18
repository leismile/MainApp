package com.ubtechinc.alpha.task;

import android.content.Context;
import com.google.protobuf.ByteString;
import com.ubtech.utilcode.utils.notification.NotificationCenter;
import com.ubtech.utilcode.utils.notification.Subscriber;
import com.ubtechinc.alpha.CmrAppButtonEventDataOuterClass;
import com.ubtechinc.alpha.CmrAppConfigData;
import com.ubtechinc.alpha.event.ReceiveAppButtonEvent;
import com.ubtechinc.alpha.event.ReceiveAppConfigEvent;
import com.ubtechinc.alpha.event.RequestAppButtonEvent;
import com.ubtechinc.alpha.event.RequestAppConfigEvent;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;

/**
 * @desc :
 * @author: wzt
 * @time : 2017/5/24
 * @modifier:
 * @modify_time:
 */

public class ProxyServerRobotPhoneCommuniteImpl extends AbstractProxyService {
  private final Context mContext;
  // 对于异步的响应，需要暂存通信信息
  private int responseCmdID;
  private long requestSerial;
  private String peer;

  public ProxyServerRobotPhoneCommuniteImpl(Context context) {
    mContext = context;
  }

  private Subscriber<RequestAppConfigEvent> requestAppConfigEventSubscriber =
      new Subscriber<RequestAppConfigEvent>() {
        @Override public void onEvent(RequestAppConfigEvent event) {
          responseCmdID = event.responseCmdID;
          requestSerial = event.requestSerial;
          peer = event.peer;
        }
      };

  private Subscriber<ReceiveAppConfigEvent> receiveAppConfigEventSubscriber =
      new Subscriber<ReceiveAppConfigEvent>() {
        @Override public void onEvent(ReceiveAppConfigEvent event) {

          CmrAppConfigData.CmrAppConfigDataResponse responseBody = null;
          CmrAppConfigData.CmrAppConfigDataResponse.Builder builder =
              CmrAppConfigData.CmrAppConfigDataResponse.newBuilder();

          builder.setCmdID(event.cmd);
          builder.setDatas(ByteString.copyFrom(event.datas));
          builder.setPackageName(event.packageName);
          builder.setTags(new String(event.tags));
          responseBody = builder.build();

          RobotPhoneCommuniteProxy.getInstance()
              .sendResponseMessage(responseCmdID, "1", requestSerial, responseBody, peer, null);
        }
      };

  private Subscriber<RequestAppButtonEvent> requestAppButtonEventSubscriber =
      new Subscriber<RequestAppButtonEvent>() {
        @Override public void onEvent(RequestAppButtonEvent event) {
          responseCmdID = event.responseCmdID;
          requestSerial = event.requestSerial;
          peer = event.peer;
        }
      };

  private Subscriber<ReceiveAppButtonEvent> receiveAppButtonEventSubscriber =
      new Subscriber<ReceiveAppButtonEvent>() {
        @Override public void onEvent(ReceiveAppButtonEvent event) {

          CmrAppButtonEventDataOuterClass.CmrAppButtonEventData responseBody = null;
          CmrAppButtonEventDataOuterClass.CmrAppButtonEventData.Builder builder =
              CmrAppButtonEventDataOuterClass.CmrAppButtonEventData.newBuilder();

          builder.setCmd(event.cmd);
          builder.setDatas(ByteString.copyFrom(event.datas));
          builder.setPackageName(event.packageName);
          responseBody = builder.build();

          RobotPhoneCommuniteProxy.getInstance()
              .sendResponseMessage(responseCmdID, "1", requestSerial, responseBody, peer, null);
        }
      };

  @Override public void registerEvent() {
    NotificationCenter.defaultCenter()
        .subscriber(RequestAppConfigEvent.class, requestAppConfigEventSubscriber);
    NotificationCenter.defaultCenter()
        .subscriber(ReceiveAppConfigEvent.class, receiveAppConfigEventSubscriber);
    NotificationCenter.defaultCenter()
        .subscriber(RequestAppButtonEvent.class, requestAppButtonEventSubscriber);
    NotificationCenter.defaultCenter()
        .subscriber(ReceiveAppButtonEvent.class, receiveAppButtonEventSubscriber);
  }

  @Override public void unregisterEvent() {
    NotificationCenter.defaultCenter()
        .unsubscribe(RequestAppConfigEvent.class, requestAppConfigEventSubscriber);
    NotificationCenter.defaultCenter()
        .unsubscribe(ReceiveAppConfigEvent.class, receiveAppConfigEventSubscriber);
    NotificationCenter.defaultCenter()
        .unsubscribe(RequestAppButtonEvent.class, requestAppButtonEventSubscriber);
    NotificationCenter.defaultCenter()
        .unsubscribe(ReceiveAppButtonEvent.class, receiveAppButtonEventSubscriber);
  }
}
