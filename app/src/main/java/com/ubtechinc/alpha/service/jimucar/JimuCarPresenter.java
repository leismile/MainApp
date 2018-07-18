package com.ubtechinc.alpha.service.jimucar;

import android.util.Log;

import com.clj.fastble.data.BleDevice;
import com.google.protobuf.BytesValue;
import com.google.protobuf.InvalidProtocolBufferException;
import com.ubtechinc.alpha.JimuCarCheck;
import com.ubtechinc.alpha.JimuCarConnectBleCar;
import com.ubtechinc.alpha.JimuCarDriveMode;
import com.ubtechinc.alpha.JimuCarGetBleList;
import com.ubtechinc.alpha.JimuCarPower;
import com.ubtechinc.alpha.JimuCarPreparedOuterClass;
import com.ubtechinc.alpha.JimuCarQueryConnectState;
import com.ubtechinc.alpha.JimuErrorCodeOuterClass;
import com.ubtechinc.alpha.appmanager.UbtBatteryManager;
import com.ubtechinc.alpha.im.IMCmdId;
import com.ubtechinc.alpha.im.msghandler.JimuCarChangeDriveModeHandler;
import com.ubtechinc.alpha.im.msghandler.JimuCarCheckHandler;
import com.ubtechinc.alpha.im.msghandler.JimuCarIRDistanceHandler;
import com.ubtechinc.alpha.im.msghandler.JimuCarQueryPowerHandler;
import com.ubtechinc.alpha.im.msghandler.JimuCarRobotChatHandler;
import com.ubtechinc.alpha.im.msghandler.JimuCarScanBleListHandler;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.masterevent.protos.SysMasterEvent;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : kevin.liu@ubtrobot.com
 * @description :
 * @date : 2018/7/9
 * @modifier :
 * @modify time :
 */
public class JimuCarPresenter {

    private static final String TAG = "JimuCarPresenter";

    volatile JimuCarDriveMode.DriveMode mDriveMode;

    volatile JimuCarConnectBleCar.BleCarConnectState mConnectState;

    volatile BleDevice mConnectedDevice;

    volatile int mIrDistance = 10000;

    volatile String mPeer;

    volatile JimuCarPower.CarPower mCarPower;

    volatile JimuCarCheck.checkCarResponse mCheckCarResponse;

    volatile AtomicBoolean mPrepared = new AtomicBoolean(false);

    private static ScheduledExecutorService mScheduledExecutorService;

    JimuCarIRDistanceHandler mJimuCarIRDistanceHandler = JimuCarIRDistanceHandler.get();
    JimuCarCheckHandler mJimuCarCheckHandler = JimuCarCheckHandler.get();
    JimuCarQueryPowerHandler mJimuCarQueryPowerHandler = JimuCarQueryPowerHandler.get();
    JimuCarScanBleListHandler mJimuCarScanBleListHandler = JimuCarScanBleListHandler.get();

    public void clear() {
        shutdownExecutorService();
        mDriveMode = null;
        mConnectState = null;
        mConnectedDevice = null;
        mIrDistance = 10000;
        mCarPower = null;
        mCheckCarResponse = null;
        mPrepared.set(false);
        mScheduledExecutorService = null;
    }

    private JimuCarPresenter() {

    }

    private void startCarPowerListen() {
        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (enterAndConnected()) {
                    mJimuCarQueryPowerHandler.doGetCarPower(new ResponseCallback() {
                        @Override
                        public void onResponse(Request request, Response response) {
                            Log.d(TAG, "mScheduledExecutorService doGetCarPower onResponse");
                        }

                        @Override
                        public void onFailure(Request request, CallException e) {
                            Log.d(TAG, "mScheduledExecutorService doGetCarPower onFailure");
                        }
                    });
                }
            }
        }, 2, SCAN_PERIOD + 30, TimeUnit.SECONDS);
    }

    private void startCarCheckListen() {
        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (enterAndConnected()) {
                    mJimuCarCheckHandler.doCheckCar(new ResponseCallback() {
                        @Override
                        public void onResponse(Request request, Response response) {
                            Log.d(TAG, "mScheduledExecutorService doCheckCar onResponse");
                        }

                        @Override
                        public void onFailure(Request request, CallException e) {
                            Log.d(TAG, "mScheduledExecutorService doCheckCar onFailure");
                        }
                    });
                }
            }
        }, 2, SCAN_PERIOD + 11, TimeUnit.SECONDS);
    }

    private void startIrDistanceListen() {
        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (enterAndConnected()) {
                    mJimuCarIRDistanceHandler.getIrDistance(new ResponseCallback() {
                        @Override
                        public void onResponse(Request request, Response response) {
                            Log.d(TAG, "mScheduledExecutorService getIrDistance onResponse");
                        }

                        @Override
                        public void onFailure(Request request, CallException e) {
                            Log.d(TAG, "mScheduledExecutorService getIrDistance onFailure");
                        }
                    });
                }
            }
        }, 1, SCAN_PERIOD, TimeUnit.SECONDS);
    }

    private void startScanBleListListen() {
        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (entered() && getConnectState() != JimuCarConnectBleCar.BleCarConnectState.CONNECTED) {
                    mJimuCarScanBleListHandler.doScan(new ResponseCallback() {
                        @Override
                        public void onResponse(Request request, Response response) {
                            Log.d(TAG, "mScheduledExecutorService doScan onResponse");
                            final BytesValue bytesValue;
                            try {
                                bytesValue = ProtoParam.from(response.getParam(), BytesValue.class).getProtoMessage();
                                final JimuCarGetBleList.GetJimuCarBleListResponse carBleListResponse = JimuCarGetBleList.GetJimuCarBleListResponse.parseFrom(bytesValue.getValue());
                                RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(IMCmdId.IM_JIMU_CAR_GET_BLE_CAR_LIST_RESPONSE, IMCmdId.IM_VERSION, -1, carBleListResponse, mPeer, null);
                            } catch (ProtoParam.InvalidProtoParamException e) {
                                e.printStackTrace();
                            } catch (InvalidProtocolBufferException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Request request, CallException e) {
                            Log.d(TAG, "mScheduledExecutorService doScan onFailure");
                        }
                    });
                }
            }
        }, 6, SCAN_PERIOD + 30, TimeUnit.SECONDS);
    }

    public static final JimuCarPresenter mPresenter = new JimuCarPresenter();

    public static final JimuCarPresenter get() {
        return mPresenter;
    }

    public synchronized final JimuCarDriveMode.DriveMode getDriveMode() {
        return mDriveMode;
    }

    public synchronized void setDriveMode(JimuCarDriveMode.DriveMode mode) {
        if (mode != mDriveMode) {
            mDriveMode = mode;
            onDriveModeChanged();
        }

    }

    public synchronized final JimuCarConnectBleCar.BleCarConnectState getConnectState() {
        return mConnectState;
    }

    public synchronized void setConnectState(String mac,String name,JimuCarConnectBleCar.BleCarConnectState connectState) {
        if (connectState != mConnectState) {
            mConnectState = connectState;
            onConnectStateChanged(mac,name);
        }
    }

    public synchronized BleDevice getConnectedDevice() {
        return mConnectedDevice;
    }

    public synchronized void setConnectedDevice(BleDevice mConnectedDevice) {
        this.mConnectedDevice = mConnectedDevice;
    }

    public final synchronized String getPeer() {
        return mPeer;
    }

    public void setPeer(String peer) {
        this.mPeer = peer;
    }

    public final synchronized int getIrDistance() {
        return mIrDistance;
    }

    public final synchronized void setIrDistance(int irDistance) {
        if (mIrDistance != irDistance) {
            this.mIrDistance = irDistance;
            onIrDistanceChanged();
        }
    }

    public final synchronized JimuCarPower.CarPower getCarPower() {
        return mCarPower;
    }

    public final synchronized void setCarPower(JimuCarPower.CarPower carPower) {
        if (mCarPower == null || carPower.getPowerPercentage() != mCarPower.getPowerPercentage()) {
            this.mCarPower = carPower;
            onCarPowerChanged();
        }
    }

    public final synchronized AtomicBoolean getPrepared() {
        return mPrepared;
    }

    public final synchronized void setPrepared(boolean prepared) {
        if (mPrepared.get() != prepared) {
            this.mPrepared.set(prepared);
            onPreparedChanged();
        }

    }

    public final synchronized JimuCarCheck.checkCarResponse getCheckCarResponse() {
        return mCheckCarResponse;
    }

    public final synchronized void setCheckCarResponse(JimuCarCheck.checkCarResponse checkCarResponse) {
        this.mCheckCarResponse = checkCarResponse;
        onCarCheck(checkCarResponse);
    }

    public synchronized boolean enterAndConnected() {
        return getDriveMode() == JimuCarDriveMode.DriveMode.ENTER && getConnectState() == JimuCarConnectBleCar.BleCarConnectState.CONNECTED;
    }

    private synchronized void onPreparedChanged() {
        Log.d(TAG, "====onPreparedChanged===" + getPrepared().get());
        if (getPrepared().get()) {
            JimuCarRobotChatHandler.get().playTTs("Yahoo!进入开车模式!", null);
        }
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(IMCmdId.IM_JIMU_CAR_CHECK_PREPARED, IMCmdId.IM_VERSION, -1, JimuCarPreparedOuterClass.JimuCarPrepared.newBuilder().setPrepared(mPrepared.get()).build(), mPeer, null);
    }

    private synchronized void onCarCheck(JimuCarCheck.checkCarResponse checkCarResponse) {
        Log.d(TAG, "=======enterAndConnected" + enterAndConnected() + "====onCarCheck====" + checkCarResponse);
        if (enterAndConnected()) {
            RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(IMCmdId.IM_JIMU_CAR_CAR_CHECK_RESPONSE, IMCmdId.IM_VERSION, -1, checkCarResponse, mPeer, null);
        }
    }

    private synchronized void onDriveModeChanged() {
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(IMCmdId.IM_JIMU_CAR_CHANGE_DRIVE_MODE_RESPONSE, IMCmdId.IM_VERSION, -1, JimuCarDriveMode.ChangeJimuDriveModeResponse.newBuilder().setDriveMode(mDriveMode).setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.REQUEST_SUCCESS).build(), mPeer, null);
        if (entered()) {
            shutdownExecutorService();
            mScheduledExecutorService = Executors.newScheduledThreadPool(4);
            startScanBleListListen();
            startIrDistanceListen();
            startCarCheckListen();
            startCarPowerListen();
        } else {
            shutdownExecutorService();
        }
    }

    private void shutdownExecutorService() {
        if (mScheduledExecutorService != null && !mScheduledExecutorService.isShutdown()) {
            mScheduledExecutorService.shutdown();
        }
    }

    private synchronized boolean entered() {
        return getDriveMode() == JimuCarDriveMode.DriveMode.ENTER;
    }

    private synchronized void onConnectStateChanged(String mac,String name) {
        Log.d(TAG, "=====onConnectStateChanged====" + getConnectState());
        try {
            RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(IMCmdId.IM_JIMU_CAR_QUERY_CONNECT_STATE_RESPONSE, IMCmdId.IM_VERSION, -1, JimuCarQueryConnectState.JimuCarQueryConnectStateResponse.newBuilder().setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.REQUEST_SUCCESS).setState(getConnectState()).setCar(JimuCarGetBleList.JimuCarBle.newBuilder().setMac(mac).setName(name).build()).build(), mPeer, null);
        }catch (Exception e){
            Log.d(TAG,"crash on onConnectStateChanged : mac===="+mac+"====name===="+name);
        }
        if (enterAndConnected()) {
            JimuCarRobotChatHandler.get().playTTs("连接上小车!", null);
        } else {
            JimuCarRobotChatHandler.get().playTTs("小车连接断开!", null);
        }
    }

    private volatile static AtomicInteger LEAVE_SECONDS_DISTANCE_1600 = new AtomicInteger();
    private final static int SCAN_PERIOD = 5;

    private volatile static AtomicInteger LEAVE_SECONDS_DISTANCE_2400 = new AtomicInteger(30);
//    private final static int SCAN_PERIOD_DISTANCE_2400 = 30;

    private synchronized void onIrDistanceChanged() {
        if (enterAndConnected()) {
            Log.d(TAG, "====onIrDistanceChanged====" + getIrDistance());
            // RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(IMCmdId.IM_JIMU_CAR_GET_IR_DISTANCE_RESPONSE, IMCmdId.IM_VERSION, 0, JimuCarGetIRDistance.JimuCarGetIRDistanceResponse.newBuilder().setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.REQUEST_SUCCESS).setDistance(mIrDistance).build(), mPeer, null);
            if (getIrDistance() > 800 && getIrDistance() < 1600) {
                setPrepared(true);
            } else {
                setPrepared(false);
            }

            if (getIrDistance() > 1600) {
                //需要计时
                final int seconds = LEAVE_SECONDS_DISTANCE_1600.getAndAdd(SCAN_PERIOD);
                if (seconds > 120) {
                    if (seconds < 120 + SCAN_PERIOD + 1) {
                        JimuCarRobotChatHandler.get().playTTs("你一定是没好好把我放到车上吧!", null);
                    } else if (seconds > 600) {
                        //quit
                        JimuCarChangeDriveModeHandler.get().quitDriveMode(new ResponseCallback() {
                            @Override
                            public void onResponse(Request request, Response response) {
                                JimuCarRobotChatHandler.get().playTTs("我感觉我一直没坐上车，我还是先不开车了!", null);
                                resetTimeCount(LEAVE_SECONDS_DISTANCE_1600,0);
                                resetTimeCount(LEAVE_SECONDS_DISTANCE_2400,30);
                            }

                            @Override
                            public void onFailure(Request request, CallException e) {
                                Log.d(JimuCarSkill.TAG, "quit drive mode failure !!!");
                            }
                        });
                    }
                } else {
                    if (getIrDistance() > 2400) {
                        final int seconds2 = LEAVE_SECONDS_DISTANCE_2400.getAndAdd(SCAN_PERIOD);
                        if (seconds2 % 30 == 0) {
                            JimuCarRobotChatHandler.get().playTTs("我离开了车座，快把我放回去吧!", null);
                        }

                    } else {
                        resetTimeCount(LEAVE_SECONDS_DISTANCE_2400,30);
                    }
                }


            } else if (getIrDistance() > 800 && getIrDistance() < 1000) {
                //重新计时，清零
                resetTimeCount(LEAVE_SECONDS_DISTANCE_1600,0);
            }
        }
    }

    private void resetTimeCount(AtomicInteger timer,int count) {
        timer.set(0);
    }

    private synchronized void onCarPowerChanged() {
        Log.d(TAG, "=====onCarPowerChanged=====" + getCarPower());
        if (enterAndConnected()) {
            final SysMasterEvent.BatteryStatusData batteryInfo = UbtBatteryManager.getInstance().getBatteryInfo();
            final int robotPowerPercentage = batteryInfo.getLevel();
            RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(IMCmdId.IM_JIMU_CAR_QUERY_POWER_RESPONSE, IMCmdId.IM_VERSION, -1, JimuCarPower.GetJimuCarPowerResponse.newBuilder().setCarPower(mCarPower).setRobotPower(JimuCarPower.RobotPower.newBuilder().setPowerPercentage(robotPowerPercentage).build()).setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.REQUEST_SUCCESS).build(), mPeer, null);
        }
    }

}
