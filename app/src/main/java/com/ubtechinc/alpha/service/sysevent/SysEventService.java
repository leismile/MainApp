package com.ubtechinc.alpha.service.sysevent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.protobuf.GeneratedMessageV3;
import com.ubtech.utilcode.utils.LogUtils;
import com.ubtech.utilcode.utils.notification.NotificationCenter;
import com.ubtech.utilcode.utils.notification.TopicSubscriber;
import com.ubtechinc.alpha.appmanager.MotorManager;
import com.ubtechinc.alpha.appmanager.UbtBatteryManager;
import com.ubtechinc.alpha.key.BroadcastAction;
import com.ubtechinc.alpha.key.KeyCodeConstants;
import com.ubtechinc.alpha.service.MainService;
import com.ubtechinc.alpha.service.sysevent.policy.ActionReceivedPolicy;
import com.ubtechinc.alpha.service.sysevent.receiver.ReceiverWrapper;
import com.ubtechinc.alpha.utils.AlphaUtils;
import com.ubtechinc.alpha.utils.EyesControlUtils;
import com.ubtechinc.alpha.utils.MouthUtils;
import com.ubtechinc.alpha.utils.PlayBehaviorUtil;
import com.ubtechinc.nets.im.event.BatteryStateChange;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.interactor.MasterInteractor;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.master.skill.SkillInfo;
import com.ubtrobot.master.skill.SkillStopCause;
import com.ubtrobot.master.transport.message.CallGlobalCode;
import com.ubtrobot.masterevent.protos.RobotGestures;
import com.ubtrobot.masterevent.protos.SysMasterEvent;
import com.ubtrobot.motor.MotorApi;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Param;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ubtechinc.com.standupsdk.FallClimbEventReceiver;
import ubtechinc.com.standupsdk.StandUpApi;


/**
 * @author：wululin
 * @date：2017/12/20 13:48
 * @modifier：lulin.wu
 * @modify_date：2017/12/20 13:48
 * [A brief description]
 * 实现一个服务来发布各种事件
 */

public class SysEventService extends MasterSystemService {

    private final MasterInteractor.SkillLifecycleCallbacks skillLifecycleCallbacks = new MasterInteractor.SkillLifecycleCallbacks() {
        @Override
        public void onSkillStarted(SkillInfo skillInfo) {

        }

        @Override
        public void onSkillStopped(SkillInfo skillInfo, SkillStopCause skillStopCause) {
            ReceiverWrapper.get().removeHandlerBySkillName(skillInfo.getName());
        }
    };
    private MainService mainService;
    private ExecutorService mEventThreadPool;

    @Override
    protected void onServiceCreate() {
        Log.i(TAG, "onServiceCreate===========");
        mEventThreadPool = Executors.newFixedThreadPool(5);

        registerPublishTouchEvent();
        mSysActiveParam = ProtoParam.create(SysMasterEvent.ActiveStatusData.newBuilder().setOldStatus(mOldStatus).setNewStatus(mNewStatus).build());
        StandUpApi.getInstance().subscribeFallClimbEvent(fallClimbEventReceiver);
        UbtBatteryManager.getInstance().setBatteryStateListener(mBatteryStateListener);
        listenBinderBind();
        listenBinderUnBind();
        listenSkillLifeCycle();
        mainService = new MainService();
        mainService.onStartOnce(this);
    }


    @Override
    protected void onServiceDestroy() {
        UbtBatteryManager.getInstance().unregisterBatteryChangeReceive();
        unregisterReceiver(mBinderReceiver);
        unregisterReceiver(mUnsubscribeReceiver);
        mInteractor.unregisterSkillLifecycleCallbacks(skillLifecycleCallbacks);
        mainService.onDestroy();
    }


    MasterInteractor mInteractor;

    /**
     * 监听Skill的生命周期
     */
    private void listenSkillLifeCycle() {
        mInteractor = Master.get().getOrCreateInteractor("robot:" + getPackageName());
        mInteractor.registerSkillLifecycleCallbacks(skillLifecycleCallbacks);
    }


    private void listenBinderBind() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SUBSCRIBE_ACTION);
        registerReceiver(mBinderReceiver, intentFilter);
    }

    private void listenBinderUnBind() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UNSUBSCRIBE_ACTION);
        registerReceiver(mUnsubscribeReceiver, intentFilter);
    }


    /**
     * 订阅 触摸发布 事件
     */
    public void registerPublishTouchEvent() {
        publishSingleClickEvent();
        publishDoubleClickEvent();
        publishLongPressEvent();
    }

    /**
     * 订阅 长按发布 事件
     */
    private void publishLongPressEvent() {
        NotificationCenter.defaultCenter().subscriber(BroadcastAction.ACTION_KEYCODE_LONG_PRESS, new TopicSubscriber<SysMasterEvent.TouchEvent>() {
            @Override
            public void onEvent(String s, SysMasterEvent.TouchEvent event) {
                Log.d(TAG, "publishLongPressEvent Keycode:" + event.getKeycode().getKeycode());
                if (event.getKeycode().getKeycode() == KeyCodeConstants.KEYCODE_RACKET_HEAD) {
                    publishSysEvent(BroadcastAction.ACTION_RACKET_HEAD, event);
                } else if (event.getKeycode().getKeycode() == KeyCodeConstants.KEYCODE_POWER_KEY_DOWN) {
                    publishSysEvent(BroadcastAction.ACTION_KEYCODE_SHUTDOWN, event);
                }

            }
        });
    }

    /**
     * 订阅 双击发布 事件
     */
    private void publishDoubleClickEvent() {
        NotificationCenter.defaultCenter().subscriber(BroadcastAction.ACTION_KEYCODE_DOUBLE_CLICK, new TopicSubscriber<SysMasterEvent.TouchEvent>() {
            @Override
            public void onEvent(String s, SysMasterEvent.TouchEvent event) {
                Log.d(TAG, "publishDoubleClickEvent Keycode:" + event.getKeycode().getKeycode());
                if (event.getKeycode().getKeycode() == KeyCodeConstants.KEYCODE_RACKET_HEAD) {
                    publishSysEvent(BroadcastAction.ACTION_RACKET_HEAD, event);
                }


            }
        });
    }

    /**
     * 订阅 单击发布 事件
     */
    private void publishSingleClickEvent() {
        NotificationCenter.defaultCenter().subscriber(BroadcastAction.ACTION_KEYCODE_SINGLE_CLICK, new TopicSubscriber<SysMasterEvent.TouchEvent>() {
            @Override
            public void onEvent(String s, SysMasterEvent.TouchEvent event) {
                Log.d(TAG, "publishSingleClickEvent Keycode:" + event.getKeycode().getKeycode());

                switch (event.getKeycode().getKeycode()) {
                    case KeyCodeConstants.KEYCODE_RACKET_HEAD:
                        publishSysEvent(BroadcastAction.ACTION_RACKET_HEAD, event);
                        break;
                    case KeyCodeConstants.KEYCODE_POWER_KEY_DOWN:
                        publishSysEvent(BroadcastAction.ACTION_KEYCODE_POWER_CLICK, event);
                        break;
                    case KeyCodeConstants.KEYCODE_VOLUMN_DOWN_KEY_DOWN:
                        publishSysEvent(BroadcastAction.ACTION_VOLUME_DOWN_KEY_DOWN, event);
                        break;
                    case KeyCodeConstants.KEYCODE_VOLUMN_DOWN_KEY_UP:
                        publishSysEvent(BroadcastAction.ACTION_VOLUME_DOWN_KEY_UP, event);
                        break;
                    case KeyCodeConstants.KEYCODE_VOLUMN_UP_KEY_DOWN:
                        publishSysEvent(BroadcastAction.ACTION_VOLUME_UP_KEY_DOWN, event);
                        break;
                    case KeyCodeConstants.KEYCODE_VOLUMN_UP_KEY_UP:
                        publishSysEvent(BroadcastAction.ACTION_VOLUME_UP_KEY_UP, event);
                        break;
                }
            }
        });
    }

    /**
     * 发布事件
     *
     * @param action
     * @param event
     */
    public void publishSysEvent(final String action, final GeneratedMessageV3 event) {
        mEventThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                ReceiverWrapper.get().getLock().lock();
                try {
                    List<EventHandler> handlers;
                    if (BroadcastAction.getPolicy(action) == ActionReceivedPolicy.ONE_SHOT) {
                        handlers = ReceiverWrapper.get().findHighestHandlers(action);
                        if (handlers == null || handlers.size() == 0) {
                            Log.d(TAG, " handlers == null or size zero!========action:" + action);
                        } else {
                            int ret = publishList(event, handlers);
                            int pre_priority = handlers.get(0).getPriority();
                            while (ret == 0 && pre_priority > 0) {//未被消费，依次往下一个优先级发布事件
                                pre_priority -= 1;
                                handlers = ReceiverWrapper.get().findPriorityHandlers(pre_priority, action);
                                if (handlers != null && handlers.size() > 0) {
                                    ret = publishList(event, handlers);
                                }
                            }
                        }
                    } else {
                        handlers = ReceiverWrapper.get().findHandlers(action);
                        if (handlers == null || handlers.size() == 0) {
                            Log.d(TAG, " handlers == null or size zero!========action:" + action);
                        } else {
                            publishList(event, handlers);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    ReceiverWrapper.get().getLock().unlock();
                }
            }
        });
    }

    /**
     * 根据消费状态依次发布事件
     *
     * @param event
     * @param handlers
     * @return
     */
    private int publishList(GeneratedMessageV3 event, List<EventHandler> handlers) {
        ReceiverWrapper.get().getLock().lock();
        int consumerMask = 0;
        try {
            final Parcel parcel = Parcel.obtain();
            parcel.writeSerializable(event);
            for (EventHandler handler :
                    handlers) {
                consumerMask |= doPublishEvent(parcel, handler);
                Log.d(TAG, "consumerMask:"+consumerMask + " skillname:" + handler.skillName + " priority:" + handler.getPriority());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReceiverWrapper.get().getLock().unlock();
        }


        return consumerMask;
    }

    /**
     * 发布事件
     *
     * @param parcel
     * @param handler
     * @return
     */
    private int doPublishEvent(Parcel parcel, EventHandler handler) {
        IBinder binder;
        final Parcel reply = Parcel.obtain();
        binder = handler.getBinder();
        if (binder != null && binder.isBinderAlive()) {
            try {
                binder.transact(ON_EVENT_CODE, parcel, reply, 0);
                final int ret = reply.readInt();
                Log.d(TAG, "remote reply:" + ret + "====" + handler.toString());
                return ret;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }


    /**
     * 处理注册事件
     */
    private BroadcastReceiver mBinderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            mEventThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    ReceiverWrapper.get().getLock().lock();
                    try {
                        final Bundle bundle = intent.getBundleExtra(SUBSCRIBE_BUNDLE_KEY);
                        final IBinder binder = bundle.getBinder(EVENT_BINDER_KEY);
                        final String action = bundle.getString(EVENT_ACTION_KEY);
                        final int pid = bundle.getInt(EVENT_PID_KEY);
                        final int priority = bundle.getInt(EVENT_PRIORITY_KEY);
                        final String skillName = bundle.getString(EVENT_SKILL_NAME_KEY);

                        if(binder == null || TextUtils.isEmpty(action)){
                            Log.d(TAG, "subscribe fail:==========" + "binder == null || action == null");
                            return;
                        }

                        final EventHandler eventHandler = new EventHandler.Builder().action(action).pid(pid).priority(priority).uuid(UUID.randomUUID().toString()).binder(binder).skillName(skillName).build();
                        Log.d(TAG, "subscribe:==========" + eventHandler.toString());
                        try {
                            binder.linkToDeath(new IBinder.DeathRecipient() {
                                @Override
                                public void binderDied() {
                                    ReceiverWrapper.get().removeHandler(eventHandler);
                                }
                            }, 0);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        int ret = ReceiverWrapper.get().addHandler(eventHandler);
                        final Parcel parcel = Parcel.obtain();
                        final HashMap<String, String> map = new HashMap<>();
                        if (ret == 0) {
                            map.put(STATUS_OK, "subscribe success!" + eventHandler.toString());
                        } else {
                            map.put(STATUS_ERROR, "unknown error!" + eventHandler.toString());
                            ReceiverWrapper.get().removeHandler(eventHandler);
                        }
                        map.put(UUID_KEY, eventHandler.getUuid());
                        final String json = new Gson().toJson(map);
                        if (!TextUtils.isEmpty(json)) {
                            parcel.writeString(json);
                        }

                        try {
                            binder.transact(SUBSCRIBE_RESULT_CODE, parcel, Parcel.obtain(), 0);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        ReceiverWrapper.get().getLock().unlock();
                    }
                }
            });
        }
    };

    /**
     * 处理反注册事件
     */
    private BroadcastReceiver mUnsubscribeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            mEventThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    ReceiverWrapper.get().getLock().lock();
                    try {

                        final Bundle bundle = intent.getBundleExtra(UNSUBSCRIBE_BUNDLE_KEY);
                        if (bundle.getInt(UNSUBSCRIBE_METHOD) == 1) {
                            ReceiverWrapper.get().removeHandlerBySkillName(bundle.getString(EVENT_SKILL_NAME_KEY));
                        } else {
                            final String uuid = bundle.getString(UUID_KEY);
                            final String action = bundle.getString(EVENT_ACTION_KEY);
                            Log.d(TAG, "unsubscribeReceiver uuid====" + uuid + "===action===" + action);
                            ReceiverWrapper.get().removeHandlerByUuid(uuid);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        ReceiverWrapper.get().getLock().unlock();
                    }
                }
            });

        }
    };

    static final String UNSUBSCRIBE_METHOD = "UNSUBSCRIBE_METHOD";
    static final String SUBSCRIBE_BUNDLE_KEY = "SUBSCRIBE_BUNDLE";
    static final String UNSUBSCRIBE_BUNDLE_KEY = "UNSUBSCRIBE_BUNDLE";
    static final String SUBSCRIBE_ACTION = "com.ubtechinc.alpha.service.sysevent.subscribe";
    static final String UNSUBSCRIBE_ACTION = "com.ubtechinc.alpha.service.sysevent.unsubscribe";
    static final String EVENT_ACTION_KEY = "EVENT_ACTION_KEY";
    static final String EVENT_PID_KEY = "EVENT_PID_KEY";
    static final String EVENT_PRIORITY_KEY = "EVENT_PRIORITY_KEY";
    static final String STATUS_OK = "STATUS_OK";
    static final String STATUS_ERROR = "STATUS_ERROR";
    static final String UUID_KEY = "UUID";
    static final String EVENT_SKILL_NAME_KEY = "EVENT_SKILL_NAME_KEY";
    static final String EVENT_BINDER_KEY = "EVENT_BINDER_KEY";
    static final int SUBSCRIBE_RESULT_CODE = 0x01;
    static final int ON_EVENT_CODE = 0x02;


    /*****************************************以下系统状态事件发布 及 获取当前状态**************************************************/


    @Call(path = "/getCurrentBatteryInfo")
    public void getBatteryInfo(Request request, Responder responder) {
        Log.i(TAG, "获取电量信息=================");
        Param param = UbtBatteryManager.getInstance().getBatteryStatsParam();
        responder.respondSuccess(param);
    }

    @Call(path = "/sendSysActive")
    public void sendSysActive(Request request, Responder responder) {
        try {
            SysMasterEvent.ActiveStatusData data = ProtoParam.from(request.getParam(), SysMasterEvent.ActiveStatusData.class).getProtoMessage();
            mNewStatus = data.getNewStatus();
            final SysMasterEvent.ActiveStatusData activeStatusData = SysMasterEvent.ActiveStatusData.newBuilder().setOldStatus(mOldStatus).setNewStatus(mNewStatus).build();
            mSysActiveParam = ProtoParam.create(activeStatusData);
            publishSysEvent(BroadcastAction.ACTION_SYS_ACTIVE_STATUS, activeStatusData);
            mOldStatus = mNewStatus;
        } catch (ProtoParam.InvalidProtoParamException e) {
            e.printStackTrace();
        }
        responder.respondSuccess();
    }

    @Call(path = "/getSysActive")
    public void getSysActive(Request request, Responder responder) {
        Log.e(TAG, "getSysActive");
        responder.respondSuccess(mSysActiveParam);
    }

    public void sendRing(String action) {
        publish(action);
    }

    public void nomalPowerStatus(Param param) {
        Log.i(TAG, "进入电量正常状态==========");
        EyesControlUtils.openEyes();
        removeState("lowPower");
        publishPowerEvent(param);
    }

    public void lowPowerStatus(Param param) {
        Log.i(TAG, "进入低电状态=========");
        mFallClimbType = StandUpApi.getInstance().getRobotFallStatus();
        LogUtils.i(TAG, "mFallClimbType=======" + mFallClimbType);
        if (mFallClimbType == RobotGestures.FallClimbType.FINISH_FALLCLIMB) {
            initLowPowerStatus(param);
        }
    }

    private void initLowPowerStatus(Param param) {
        publishPowerEvent(param);
        addState("lowPower");
        AlphaUtils.playBehavior("low-power_0001", Priority.HIGH, null);
        StandUpApi.getInstance().squatdown(new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {
                unlockAllMotor();
            }

            @Override
            public void onFailure(Request request, CallException e) {
                LogUtils.i(TAG, "initLowPowerStatus====" + e.getCode() + ";;msg===" + e.getMessage());
                unlockAllMotor();
            }
        });
    }
    private void unlockAllMotor(){
        MotorApi.get().unlockAllMotor(new ResponseListener<Boolean>() {
            @Override
            public void onResponseSuccess(Boolean aBoolean) {
            }

            @Override
            public void onFailure(int i, @NonNull String s) {
            }
        });
    }
    private void publishPowerEvent(Param param) {
        mParam = param;
        ProtoParam<SysMasterEvent.BatteryStatusData> bp = (ProtoParam<SysMasterEvent.BatteryStatusData>) param;
        publishSysEvent(BroadcastAction.ACTION_BATTERY_STATE, bp.getProtoMessage());
    }


    UbtBatteryManager.BatteryStateListener mBatteryStateListener = new UbtBatteryManager.BatteryStateListener() {
        @Override
        public void onNormalPower(Param param) {
            nomalPowerStatus(param);
        }

        @Override
        public void onLowPower(Param param) {
            lowPowerStatus(param);
        }

        @Override
        public void onPublishEvent(Param param) {
            publishPowerEvent(param);
        }
    };

    FallClimbEventReceiver fallClimbEventReceiver = new FallClimbEventReceiver() {
        @Override
        public void onFallclimb(RobotGestures.FallClimbType fallClimbType) {
            if (fallClimbType == RobotGestures.FallClimbType.FINISH_FALLCLIMB && mFallClimbType == RobotGestures.FallClimbType.START_FALLCLIMB) {
                initLowPowerStatus(mParam);
            }
        }
    };


    @Override
    protected void onCall(Request request, Responder responder) {
        responder.respondFailure(CallGlobalCode.INTERNAL_ERROR, "当前服务不支持调用：" + request.getPath());
    }

    private static final String TAG = SysEventService.class.getSimpleName();
    private BatteryStateChange mBatteryStateChange;
    private SysMasterEvent.ActivieStatusType mOldStatus = SysMasterEvent.ActivieStatusType.ACTIVE;
    private SysMasterEvent.ActivieStatusType mNewStatus = SysMasterEvent.ActivieStatusType.ACTIVE;
    private ProtoParam<SysMasterEvent.ActiveStatusData> mSysActiveParam;
    private RobotGestures.FallClimbType mFallClimbType = RobotGestures.FallClimbType.FINISH_FALLCLIMB;
    private Param mParam;

}
