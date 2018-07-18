package com.ubtechinc.alpha.service.jimucar;

import android.bluetooth.BluetoothGatt;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.data.BleScanState;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.clj.fastble.scan.BleScanner;
import com.google.protobuf.BytesValue;
import com.google.protobuf.StringValue;
import com.ubtech.utilcode.utils.StringUtils;
import com.ubtechinc.alpha.JimuCarConnectBleCar;
import com.ubtechinc.alpha.JimuCarDriveMode;
import com.ubtechinc.alpha.JimuCarGetBleList;
import com.ubtechinc.alpha.JimuCarListenType;
import com.ubtechinc.alpha.JimuCarQueryConnectState;
import com.ubtechinc.alpha.JimuErrorCodeOuterClass;
import com.ubtechinc.alpha.robotinfo.RobotState;
import com.ubtechinc.alpha.service.jimucar.handler.JimuCarResponder;
import com.ubtechinc.alpha.service.jimucar.handler.ReadMainBoardHandler;
import com.ubtechinc.bluetoothlibrary.UbtBluetoothConnManager;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.skill.MasterSkill;
import com.ubtrobot.master.skill.SkillStopCause;
import com.ubtrobot.master.transport.message.CallGlobalCode;
import com.ubtrobot.transport.message.Param;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author : kevin.liu@ubtrobot.com
 * @description :
 * @date : 2018/7/2
 * @modifier :
 * @modify time :
 */
public class JimuCarSkill extends MasterSkill {
    public static final String TAG = JimuCarSkill.class.getName();

    private static final String JIMU_NAME_FILTER = "Jimu";
    private static final int CONNECT_RETRY_TIME = 2;

    @Override
    protected void onSkillStart() {
        Log.d(TAG, "===============onSkillStart");

    }

    @Override
    protected void onSkillDestroy() {
        super.onSkillDestroy();
    }

    @Override
    protected void onSkillStop(SkillStopCause skillStopCause) {
        Log.d(TAG, "============onSkillStop");
        if (JimuCarPresenter.get().getDriveMode() != JimuCarDriveMode.DriveMode.QUIT) {
            quitDriveMode();
        }
    }

    @Override
    protected void onCall(Request request, Responder responder) {
        super.onCall(request, responder);
    }

    private AtomicBoolean mHasConnected = new AtomicBoolean(false);
    private BleDevice mConnectedDevice;

    @Call(path = "/jimucar/enter_drive")
    public void enterDriveMode(Request request, final Responder responder) {
        Log.d(TAG, "enterDriveMode");

        JimuCarPresenter.get().clear();
        initBleManager();

        //先关闭gatt service
        closeGattService();

        JimuCarPresenter.get().setDriveMode(JimuCarDriveMode.DriveMode.ENTER);
        firstScan(responder);
    }

    private void initBleManager() {
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setMaxConnectCount(7)
                .setSplitWriteNum(20)
                .setOperateTimeout(5000);
        setScanBleListRule(3 * 1000);
    }

    private void firstScan(Responder responder) {
        final JimuCarDriveMode.ChangeJimuDriveModeResponse.Builder builder = JimuCarDriveMode.ChangeJimuDriveModeResponse.newBuilder();
        builder.setDriveMode(JimuCarDriveMode.DriveMode.ENTER);
        firstScan(responder, builder, getFistScanGattCallback(responder, builder));
    }

    private void closeGattService() {
        UbtBluetoothConnManager.getInstance().destroy();
    }

    private void respondDriveMode(JimuCarDriveMode.ChangeJimuDriveModeResponse.Builder builder, Responder responder) {
        final JimuCarDriveMode.ChangeJimuDriveModeResponse driveModeResponse = builder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.REQUEST_SUCCESS).build();
        responder.respondSuccess(ProtoParam.create(BytesValue.newBuilder().setValue(driveModeResponse.toByteString()).build()));
    }

    @Call(path = "/jimucar/scan_ble_list")
    public void scanBleList(Request request, final Responder responder) {
        retryConnectCount.set(0);
        //确保没有线程在扫描
        cancelBleScan();
        setScanBleListRule(3 * 1000);
        scanBleList(responder, getScanListGattCallback());
    }


    @Call(path = "/jimucar/quit_drive")
    public void quitDriveMode(Request request, final Responder responder) {
        Log.d(TAG, "quitDriveMode");
        quitDriveMode();

        final JimuCarDriveMode.ChangeJimuDriveModeResponse.Builder builder = JimuCarDriveMode.ChangeJimuDriveModeResponse.newBuilder();
        builder.setDriveMode(JimuCarDriveMode.DriveMode.QUIT);
        builder.setState(JimuCarConnectBleCar.BleCarConnectState.DISCONNECT);
        builder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.REQUEST_SUCCESS);
        respondDriveMode(builder, responder);

        JimuCarNotifyCallbackDispatcher.unregisterAllResponders();
    }

    private void quitDriveMode() {
        JimuCarPresenter.get().setDriveMode(JimuCarDriveMode.DriveMode.QUIT);
        destroyAllConnections();
    }

    private void destroyAllConnections() {
        cancelBleScan();
        BleManager.getInstance().disconnectAllDevice();
        resetConnectState();
    }

    private void resetConnectState() {
        mHasConnected.set(false);
        mConnectedDevice = null;
        retryConnectCount.set(0);
    }

    @Call(path = "/jimucar/connect_car")
    public void connectCar(Request request, final Responder responder) {
        retryConnectCount.set(0);
        final Param param = request.getParam();
        if (param.isEmpty()) {
            responder.respondFailure(CallGlobalCode.BAD_REQUEST, "param is empty!!!");
        } else {
            doConnectCar(responder, param);
        }
    }

    private synchronized void doConnectCar(Responder responder, Param param) {
        try {
            final String mac = ProtoParam.from(param, StringValue.class).getProtoMessage().getValue();
            final List<BleDevice> connectedDevice = BleManager.getInstance().getAllConnectedDevice();
            if (!TextUtils.isEmpty(mac) && BleManager.getInstance().isConnected(mac) && connectedDevice != null && connectedDevice.size() > 0 && StringUtils.isEquals(connectedDevice.get(0).getMac(), mac)) {
                Log.d(TAG, "mac:" + mac + "connected!!!!");
                onConnectedByUser(responder, connectedDevice.get(0));
            } else {
                BleManager.getInstance().connect(mac, getConnectByUserBleGattCallback(mac, responder));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "doConnectCar Exception:" + e.getLocalizedMessage());
            final JimuCarConnectBleCar.JimuCarConnectBleCarResponse.Builder builder = JimuCarConnectBleCar.JimuCarConnectBleCarResponse.newBuilder();
            final JimuCarConnectBleCar.JimuCarConnectBleCarResponse response = builder.setState(JimuCarConnectBleCar.BleCarConnectState.CONNECT_FAIL).setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.REQUEST_SUCCESS).build();
            respondConnectCar(responder, response);
        }
    }


    @Call(path = "/jimucar/get_main_board_info")
    public void readMainBoardInfo(Request request, final Responder responder) {
        Log.d(TAG, "readMainBoardInfo");
        JimuCarNotifyCallbackDispatcher.registerResponder(JimuConstants.CMD_READ_MAINBOARD_INFO, new JimuCarResponder(JimuCarListenType.listenType.ONCE, responder));
        readMainBoardAndOpenIr();
    }

    @Call(path = "/jimucar/check_car")
    public void checkCar(Request request, final Responder responder) {
        Log.d(TAG, "checkCar");
        if (!checkConnectCar(responder)) return;
        JimuCarNotifyCallbackDispatcher.registerResponder(JimuConstants.CMD_SELF_INSPECTION, new JimuCarResponder(JimuCarListenType.listenType.ALWAYS, responder));
        sendCmd(JimuConstants.CMD_SELF_INSPECTION, new byte[1]); //设置自检开启
        //检测红外传感器
        byte irId = ReadMainBoardHandler.getMainBoard().getFirstNonullIrId();
        //openIr
        sendCmd(JimuConstants.CMD_OPEN_OR_CLOSE_SENSOR, new byte[]{JimuConstants.SENSOR_TYPE_IR, irId, 0x00});

        JimuCarNotifyCallbackDispatcher.registerResponder(JimuConstants.CMD_CHECK_SENSOR, new JimuCarResponder(JimuCarListenType.listenType.ALWAYS, responder));
        sendCmd(JimuConstants.CMD_CHECK_SENSOR, new byte[]{JimuConstants.SENSOR_TYPE_IR, irId, 0x00});
    }

    @Call(path = "/jimucar/get_car_power")
    public void getCarPower(Request request, final Responder responder) {
        Log.d(TAG, "getCarPower");
        if (!checkConnectCar(responder)) return;
        JimuCarNotifyCallbackDispatcher.registerResponder(JimuConstants.CMD_READ_POWER_INFO, new JimuCarResponder(JimuCarListenType.listenType.ONCE, responder));
        sendCmd(JimuConstants.CMD_READ_POWER_INFO, new byte[1]);
    }

    @Call(path = "/jimucar/get_ir_distance")
    public void getCarIrDistance(Request request, final Responder responder) {
        Log.d(TAG, "getCarIrDistance");
        if (!checkConnectCar(responder)) return;
        byte irId = ReadMainBoardHandler.getMainBoard().getFirstNonullIrId();
        JimuCarNotifyCallbackDispatcher.registerResponder(JimuConstants.CMD_READ_SENSOR_DATA, new JimuCarResponder(JimuCarListenType.listenType.ONCE, responder));
        //open ir
        sendCmd(JimuConstants.CMD_OPEN_OR_CLOSE_SENSOR, new byte[]{JimuConstants.SENSOR_TYPE_IR, irId, 0x00});
        SystemClock.sleep(20);
        sendCmd(JimuConstants.CMD_READ_SENSOR_DATA, new byte[]{JimuConstants.SENSOR_TYPE_IR, irId, 0x00});
    }

    //此接口嵌入式无回复
    @Call(path = "/jimucar/get_devices_id")
    public void getDevicesId(Request request, final Responder responder) {
        Log.d(TAG, "getDevicesId");
        sendCmd(JimuConstants.CMD_READ_DEVICES_ID, new byte[1]);
    }

    @Call(path = "/jimucar/go_forward")
    public void goForward(Request request, final Responder responder) {
        Log.d(TAG, "goForward");
        if (!checkConnectCar(responder)) return;
        final byte[] left = getControlLowMotorPwmParams(0x01, -140);
        final byte[] right = getControlLowMotorPwmParams(0x02, 140);
        JimuCarNotifyCallbackDispatcher.registerResponder(JimuConstants.CMD_CONTROL_MOTOR, new JimuCarResponder(JimuCarListenType.listenType.ONCE, responder));
        sendCmd(JimuConstants.CMD_CONTROL_MOTOR, left);
        sendCmd(JimuConstants.CMD_CONTROL_MOTOR, right);
    }

    @Call(path = "/jimucar/go_back")
    public void goBack(Request request, final Responder responder) {
        Log.d(TAG, "go_back");
        if (!checkConnectCar(responder)) return;
        final byte[] left = getControlLowMotorPwmParams(0x01, 140);
        final byte[] right = getControlLowMotorPwmParams(0x02, -140);
        JimuCarNotifyCallbackDispatcher.registerResponder(JimuConstants.CMD_CONTROL_MOTOR, new JimuCarResponder(JimuCarListenType.listenType.ONCE, responder));
        sendCmd(JimuConstants.CMD_CONTROL_MOTOR, left);
        sendCmd(JimuConstants.CMD_CONTROL_MOTOR, right);
    }

    @Call(path = "/jimucar/stop_going")
    public void stopGoing(Request request, final Responder responder) {
        Log.d(TAG, "stop_going");
        if (!checkConnectCar(responder)) return;
        final byte[] left = getStopLowMotorParams(0x01);
        final byte[] right = getStopLowMotorParams(0x02);
        JimuCarNotifyCallbackDispatcher.registerResponder(JimuConstants.CMD_CONTROL_MOTOR, new JimuCarResponder(JimuCarListenType.listenType.ONCE, responder));
        sendCmd(JimuConstants.CMD_CONTROL_STOP_MOTOR, left);
        sendCmd(JimuConstants.CMD_CONTROL_STOP_MOTOR, right);
    }

    /**
     * 中心三角线 - 120
     *
     * @param request
     * @param responder
     */
    @Call(path = "/jimucar/turn_left")
    public void turnLeft(Request request, final Responder responder) {
        final ReadMainBoardHandler.MainBoard mainBoard = ReadMainBoardHandler.getMainBoard();
        if (!checkConnectCarStateAndMainBoard(responder, mainBoard)) return;
        final ByteBuf buffer = Unpooled.buffer(8);

        buffer.writeByte(0x00);
        buffer.writeByte(0x00);
        buffer.writeByte(0x00);
        buffer.writeByte(mainBoard.getFirstNonullServoId());

        buffer.writeByte(0x64);// 100
        buffer.writeByte(0x14); //20ms
        buffer.writeByte(0x00);
        buffer.writeByte(0x00);  //   20的倍数

        JimuCarNotifyCallbackDispatcher.registerResponder(JimuConstants.CMD_CONTROL_SEVRO_ANGLE, new JimuCarResponder(JimuCarListenType.listenType.ONCE, responder));
        sendCmd(JimuConstants.CMD_CONTROL_SEVRO_ANGLE, buffer.array());
    }


    @Call(path = "/jimucar/turn_right")
    public void turnRight(Request request, final Responder responder) {
        final ReadMainBoardHandler.MainBoard mainBoard = ReadMainBoardHandler.getMainBoard();
        if (!checkConnectCarStateAndMainBoard(responder, mainBoard)) return;
        final ByteBuf buffer = Unpooled.buffer(8);

        buffer.writeByte(0x00);
        buffer.writeByte(0x00);
        buffer.writeByte(0x00);

        buffer.writeByte(mainBoard.getFirstNonullServoId());

        buffer.writeByte(0x8c);//140
        buffer.writeByte(0x14); //20ms
        buffer.writeByte(0x00);
        buffer.writeByte(0x00);  //   20的倍数

        JimuCarNotifyCallbackDispatcher.registerResponder(JimuConstants.CMD_CONTROL_SEVRO_ANGLE, new JimuCarResponder(JimuCarListenType.listenType.ONCE, responder));
        sendCmd(JimuConstants.CMD_CONTROL_SEVRO_ANGLE, buffer.array());
    }


    @Call(path = "/jimucar/reset_car_direction")
    public void resetCarDirection(Request request, final Responder responder) {
        final ReadMainBoardHandler.MainBoard mainBoard = ReadMainBoardHandler.getMainBoard();
        if (!checkConnectCarStateAndMainBoard(responder, mainBoard)) return;
        final ByteBuf buffer = Unpooled.buffer(8);

        buffer.writeByte(0x00);
        buffer.writeByte(0x00);
        buffer.writeByte(0x00);
        buffer.writeByte(mainBoard.getFirstNonullServoId());

        buffer.writeByte(0x78);//TODO change 120
        buffer.writeByte(0x14); //20ms
        buffer.writeByte(0x00);
        buffer.writeByte(0x00);  //   20的倍数

        JimuCarNotifyCallbackDispatcher.registerResponder(JimuConstants.CMD_CONTROL_SEVRO_ANGLE, new JimuCarResponder(JimuCarListenType.listenType.ONCE, responder));
        sendCmd(JimuConstants.CMD_CONTROL_SEVRO_ANGLE, buffer.array());
    }

    @Call(path = "/jimucar/get_connect_state")
    public void getCarConnectState(Request request, final Responder responder) {
        final JimuCarQueryConnectState.JimuCarQueryConnectStateResponse response;
        try {
            if (mHasConnected.get() && mConnectedDevice != null) {
                response = JimuCarQueryConnectState.JimuCarQueryConnectStateResponse.newBuilder().setCar(JimuCarGetBleList.JimuCarBle.newBuilder().setMac(TextUtils.isEmpty(mConnectedDevice.getMac()) ? "unKnown" : mConnectedDevice.getMac()).setName(TextUtils.isEmpty(mConnectedDevice.getName()) ? "unKnown" : mConnectedDevice.getName()).build()).setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.REQUEST_SUCCESS).setState(JimuCarConnectBleCar.BleCarConnectState.CONNECTED).build();
            } else {
                response = JimuCarQueryConnectState.JimuCarQueryConnectStateResponse.newBuilder().setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.REQUEST_SUCCESS).setState(JimuCarConnectBleCar.BleCarConnectState.DISCONNECT).build();
            }
            responder.respondSuccess(ProtoParam.create(BytesValue.newBuilder().setValue(response.toByteString()).build()));
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private boolean checkConnectCarStateAndMainBoard(Responder responder, ReadMainBoardHandler.MainBoard mainBoard) {
        if (mainBoard == null) {
            if (checkConnectCar(responder)) {
                readMainBoardAndOpenIr();
                SystemClock.sleep(1000l);
                if (mainBoard == null) {
                    responder.respondFailure(CallGlobalCode.INTERNAL_ERROR, "主板信息初始化出错!");
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkConnectCar(Responder responder) {
        if (!mHasConnected.get()) {
            responder.respondFailure(CallGlobalCode.INTERNAL_ERROR, "未连接小车!");
            return false;
        }
        return true;
    }


    private void readMainBoardAndOpenIr() {
        sendCmd(JimuConstants.CMD_READ_MAINBOARD_INFO, new byte[1]);
        //open ir
        byte irId = 0x01;

        sendCmd(JimuConstants.CMD_OPEN_OR_CLOSE_SENSOR, new byte[]{JimuConstants.SENSOR_TYPE_IR, irId, 0x00});
    }

    private void sendCmd(byte cmd, byte[] params) {
        if (BleManager.getInstance().isConnected(mConnectedDevice)) {
            BlePacketSender.sendPacket(cmd, params, mConnectedDevice);
        }
    }


    public synchronized void onCarConnectSuccess(BleDevice bleDevice) {
        Log.d(TAG, "onCarConnectSuccess:" + bleDevice.getKey() + "====" + bleDevice.getMac());
        retryConnectCount.set(0);
        mHasConnected.set(true);
        mConnectedDevice = bleDevice;
        JimuCarPresenter.get().setConnectedDevice(mConnectedDevice);
        JimuCarPresenter.get().setConnectState(bleDevice.getMac(), bleDevice.getName(), JimuCarConnectBleCar.BleCarConnectState.CONNECTED);
        cancelBleScan();
        JimuCarNotifyCallbackDispatcher.registerNotifyCallback(bleDevice);
        SystemClock.sleep(1000);
        BlePacketSender.sendPacket(JimuConstants.CMD_HAND_SHAKE, new byte[1], bleDevice);
        readMainBoardAndOpenIr();

    }


    public synchronized void onCarDisConnected(BleDevice bleDevice) {
        Log.d(TAG, "onCarDisConnected:" + bleDevice.getKey() + "====" + bleDevice.getMac());
        JimuCarPresenter.get().setConnectState(bleDevice.getMac(), bleDevice.getName(), JimuCarConnectBleCar.BleCarConnectState.DISCONNECT);
        if (JimuCarPresenter.get().getDriveMode() == JimuCarDriveMode.DriveMode.QUIT) {
            UbtBluetoothConnManager.getInstance().init(getApplicationContext(), "Mini_", RobotState.get().getSid());
        }
        clear();
    }


    private synchronized void clear() {
        retryConnectCount.set(0);
        mHasConnected.set(false);
        mConnectedDevice = null;
    }

    private static byte[] getControlLowMotorPwmParams(int lowMotorId, int pwm) {
        byte index = 0x01;
        byte id = lowMotorId == 0 ? (byte) 0xff : (byte) (1 << (lowMotorId - 1));
        byte pwmLow;
        byte pwmHigh;
        if (pwm < 0) {
            pwm = (~(-pwm) + 1);
        }
        pwmHigh = (byte) ((pwm >> 8) & 0xff);
        pwmLow = (byte) (pwm & 0xff);
        return lowMotorId == 0 ?
                new byte[]{index, id
                        , pwmHigh, pwmLow
                        , pwmHigh, pwmLow
                        , pwmHigh, pwmLow
                        , pwmHigh, pwmLow
                        , pwmHigh, pwmLow
                        , pwmHigh, pwmLow
                        , pwmHigh, pwmLow
                        , pwmHigh, pwmLow
                }
                : new byte[]{index, id, pwmHigh, pwmLow};
    }

    private static byte[] getStopLowMotorParams(int lowMotorId) {
        byte index = 0x01;
        byte id = lowMotorId == 0 ? (byte) 0xff : (byte) (1 << (lowMotorId - 1));
        return new byte[]{index, id};
    }

    private static byte[] getControlLowMotorParams(int lowMotorId, int runtimeMilli, int speed) {
        byte index = 0x01;
        byte id = lowMotorId == 0 ? (byte) 0xff : (byte) (1 << (lowMotorId - 1));
        byte speedLow;
        byte speedHigh;
        if (speed < 0) {
            speed = (~(-speed) + 1);
        }
        speedHigh = (byte) ((speed >> 8) & 0xff);
        speedLow = (byte) (speed & 0xff);

        final byte runtimeLow = (byte) (runtimeMilli & 0xff);
        final byte runtimeHigh = (byte) ((runtimeMilli >> 8) & 0xff);

        return lowMotorId == 0 ?
                new byte[]{index, id
                        , speedHigh, speedLow, runtimeHigh, runtimeLow
                        , speedHigh, speedLow, runtimeHigh, runtimeLow
                        , speedHigh, speedLow, runtimeHigh, runtimeLow
                        , speedHigh, speedLow, runtimeHigh, runtimeLow
                        , speedHigh, speedLow, runtimeHigh, runtimeLow
                        , speedHigh, speedLow, runtimeHigh, runtimeLow
                        , speedHigh, speedLow, runtimeHigh, runtimeLow
                        , speedHigh, speedLow, runtimeHigh, runtimeLow}
                : new byte[]{index, id, speedHigh, speedLow, runtimeHigh, runtimeLow};
    }


    @NonNull
    private BleGattCallback getFistScanGattCallback(Responder responder, JimuCarDriveMode.ChangeJimuDriveModeResponse.Builder builder) {
        return new BleGattCallback() {
            @Override
            public void onStartConnect() {
                Log.d(TAG, "onStartConnect");
//                builder.setState(JimuCarDriveMode.RobotCarConnectState.CONNECTING);
//                respondDriveMode(builder, responder);
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException e) {
                onFirstScanCarConnectFail(bleDevice, e);
            }

            private void onFirstScanCarConnectFail(BleDevice bleDevice, BleException e) {
                Log.d(TAG, "onConnectFail:" + e.getDescription());
                if (retryConnectCount.incrementAndGet() > CONNECT_RETRY_TIME) {
                    builder.setState(JimuCarConnectBleCar.BleCarConnectState.CONNECT_FAIL);
                    respondDriveMode(builder, responder);
                } else {
                    //retry...
                    BleManager.getInstance().disableBluetooth();
                    SystemClock.sleep(100);
                    BleManager.getInstance().enableBluetooth();
                    SystemClock.sleep(2000);
                    BleManager.getInstance().connect(bleDevice, this);
                }
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt bluetoothGatt, int i) {
                Log.d(TAG, "onConnectSuccess");
                onCarConnectSuccess(bleDevice);
                builder.setState(JimuCarConnectBleCar.BleCarConnectState.CONNECTED);
                respondDriveMode(builder, responder);
            }

            @Override
            public void onDisConnected(boolean b, BleDevice bleDevice, BluetoothGatt bluetoothGatt, int i) {
                Log.d(TAG, "onDisConnected");
                onCarDisConnected(bleDevice);
                //TODO 走通知通道
//                builder.setState(JimuCarConnectBleCar.BleCarConnectState.DISCONNECT);
//                respondDriveMode(builder, responder);
            }
        };
    }


    private void firstScan(Responder responder, JimuCarDriveMode.ChangeJimuDriveModeResponse.Builder builder, BleGattCallback gattCallback) {
        scanJimuBle(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean b) {
                //tts -> 正在扫描小车（主线程）
                Log.d(TAG, "onScanStarted " + b);
                if (!b) {

                }
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                // 扫描到一个符合扫描规则的BLE设备（主线程）
//                Log.d(TAG,""+bleDevice.getName());
            }

            @Override
            public void onScanFinished(List<BleDevice> list) {
                Log.d(TAG, "onScanFinished");
                synchronized (JimuCarSkill.class) {
                    //先取消scan
                    cancelBleScan();
                    // 扫描结束，列出所有扫描到的符合扫描规则的BLE设备（主线程）
                    if (!mHasConnected.get()) {
                        //之前异常未释放
                        clear();
                    }
                    final List<BleDevice> deviceList = Collections.unmodifiableList(list);
                    JimuCarSkill.this.firstScan(deviceList, builder, gattCallback, responder);
                }

            }
        });
    }

    private synchronized void firstScan(List<BleDevice> list, JimuCarDriveMode.ChangeJimuDriveModeResponse.Builder builder, BleGattCallback gattCallback, Responder responder) {
        final List<BleDevice> bleDeviceList = Collections.unmodifiableList(list);
        if (bleDeviceList != null) {
            BleDevice ble;
            for (int i = 0; i < bleDeviceList.size(); i++) {
                ble = bleDeviceList.get(i);
                Log.d("BleMAC", i + ":" + ble.getName() + "===mac:" + ble.getMac() + "====rssi===" + ble.getRssi());
                if (!TextUtils.isEmpty(ble.getName()) && !TextUtils.isEmpty(ble.getMac()) && ble.getName().toLowerCase().contains("jimu")) {
                    builder.addBle(JimuCarGetBleList.JimuCarBle.newBuilder().setName(ble.getName()).setMac(ble.getMac()).build());
                }
            }
            //auto connect gattCallback 有对responder 回复
            if (bleDeviceList.size() == 1) {
                BleManager.getInstance().connect(bleDeviceList.get(0), gattCallback);
            } else {
                builder.setState(JimuCarConnectBleCar.BleCarConnectState.IDLE);
                respondDriveMode(builder, responder);
            }

        }
    }

    volatile static AtomicInteger retryConnectCount = new AtomicInteger();

    @NonNull
    private BleGattCallback getConnectByUserBleGattCallback(final String mac, Responder responder) {
        return new BleGattCallback() {
            @Override
            public void onStartConnect() {

            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException e) {
                onCarConnectFail(bleDevice);
            }

            private void onCarConnectFail(BleDevice bleDevice) {
                if (retryConnectCount.incrementAndGet() > CONNECT_RETRY_TIME) {
                    final JimuCarConnectBleCar.JimuCarConnectBleCarResponse.Builder builder = JimuCarConnectBleCar.JimuCarConnectBleCarResponse.newBuilder();
                    final JimuCarConnectBleCar.JimuCarConnectBleCarResponse response = builder.setState(JimuCarConnectBleCar.BleCarConnectState.CONNECT_FAIL).setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.REQUEST_SUCCESS).build();
                    respondConnectCar(responder, response);
                } else {
                    //retry...
                    BleManager.getInstance().disableBluetooth();
                    SystemClock.sleep(100);
                    BleManager.getInstance().enableBluetooth();
                    SystemClock.sleep(2000);
                    BleManager.getInstance().connect(bleDevice, this);
                }
            }


            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt bluetoothGatt, int i) {
                onConnectedByUser(responder, bleDevice);
            }


            @Override
            public void onDisConnected(boolean b, BleDevice bleDevice, BluetoothGatt bluetoothGatt, int i) {
                onCarDisConnected(bleDevice);
            }


        };
    }

    private void onConnectedByUser(Responder responder, BleDevice bleDevice) {
        final JimuCarConnectBleCar.JimuCarConnectBleCarResponse.Builder builder = JimuCarConnectBleCar.JimuCarConnectBleCarResponse.newBuilder();
        final JimuCarConnectBleCar.JimuCarConnectBleCarResponse response = builder.setState(JimuCarConnectBleCar.BleCarConnectState.CONNECTED).setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.REQUEST_SUCCESS).build();
        respondConnectCar(responder, response);
        onCarConnectSuccess(bleDevice);
    }

    private void respondConnectCar(Responder responder, JimuCarConnectBleCar.JimuCarConnectBleCarResponse response) {
        responder.respondSuccess(ProtoParam.create(BytesValue.newBuilder().setValue(response.toByteString()).build()));
    }

    private void scanBleList(Responder responder, BleGattCallback gattCallback) {
        scanJimuBle(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean b) {
                //tts -> 正在扫描小车（主线程）
                Log.d(TAG, "onScanStarted " + b);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                // 扫描到一个符合扫描规则的BLE设备（主线程）
//                Log.d(TAG,""+bleDevice.getName());
            }

            @Override
            public void onScanFinished(List<BleDevice> list) {
                Log.d(TAG, "onScanFinished");
                cancelBleScan();
                synchronized (JimuCarSkill.class) {
                    // 扫描结束，列出所有扫描到的符合扫描规则的BLE设备（主线程）
                    final List<BleDevice> deviceList = Collections.unmodifiableList(list);
                    responseScanBleList(deviceList, gattCallback, responder);
                }
            }
        });
    }

    private synchronized void responseScanBleList(List<BleDevice> list, BleGattCallback gattCallback, Responder responder) {
        final List<BleDevice> bleDeviceList = Collections.unmodifiableList(list);
        if (bleDeviceList != null) {
            final JimuCarGetBleList.GetJimuCarBleListResponse.Builder responseBuilder = JimuCarGetBleList.GetJimuCarBleListResponse.newBuilder();
            BleDevice ble;
            for (int i = 0; i < bleDeviceList.size(); i++) {
                ble = bleDeviceList.get(i);
                Log.d("BleMAC", i + ":" + ble.getName() + "===mac:" + ble.getMac() + "====rssi===" + ble.getRssi());
                if (!TextUtils.isEmpty(ble.getName()) && !TextUtils.isEmpty(ble.getMac()) && ble.getName().toLowerCase().contains("jimu")) {
                    responseBuilder.addBle(JimuCarGetBleList.JimuCarBle.newBuilder().setName(ble.getName()).setMac(ble.getMac()).build());
                }
            }
            if (bleDeviceList.size() == 1) {
                if (!mHasConnected.get()) {
                    //auto connect
                    BleManager.getInstance().connect(bleDeviceList.get(0), gattCallback);
                }
            }
            final JimuCarGetBleList.GetJimuCarBleListResponse response = responseBuilder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.REQUEST_SUCCESS).build();
            responder.respondSuccess(ProtoParam.create(BytesValue.newBuilder().setValue(response.toByteString()).build()));

        }
    }


    @NonNull
    private BleGattCallback getScanListGattCallback() {
        return new BleGattCallback() {
            @Override
            public void onStartConnect() {

            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException e) {
                if (retryConnectCount.incrementAndGet() > CONNECT_RETRY_TIME) {
                    //TODO
                } else {
                    //retry...
                    BleManager.getInstance().disableBluetooth();
                    SystemClock.sleep(100);
                    BleManager.getInstance().enableBluetooth();
                    SystemClock.sleep(2000);
                    BleManager.getInstance().connect(bleDevice, this);
                }

            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt bluetoothGatt, int i) {
                onCarConnectSuccess(bleDevice);
            }

            @Override
            public void onDisConnected(boolean b, BleDevice bleDevice, BluetoothGatt bluetoothGatt, int i) {
                onCarDisConnected(bleDevice);
                //TODO 走通知通道
            }
        };
    }

    private void cancelBleScan() {
        if (BleScanner.getInstance().getScanState() == BleScanState.STATE_SCANNING)
            BleManager.getInstance().cancelScan();
    }


    private void setScanBleListRule(int time) {
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setScanTimeOut(time)              // 扫描超时时间，可选，默认10秒；小于等于0表示不限制扫描时间
                .setDeviceName(true, JIMU_NAME_FILTER)
//                .setDistance(1f) //@
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    private void scanJimuBle(BleScanCallback scanCallback) {
        //从 从状态切换到主状态 扫描 Jimu 小车
        if (BleScanner.getInstance().getScanState() == BleScanState.STATE_IDLE) {
            try {
                BleManager.getInstance().scan(scanCallback);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

        }
    }


}
