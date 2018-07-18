//package com.ubtechinc.bluetoothrobot;
//
//import android.annotation.SuppressLint;
//import android.annotation.TargetApi;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothGatt;
//import android.bluetooth.BluetoothGattCharacteristic;
//import android.bluetooth.BluetoothGattDescriptor;
//import android.bluetooth.BluetoothGattServer;
//import android.bluetooth.BluetoothGattServerCallback;
//import android.bluetooth.BluetoothManager;
//import android.bluetooth.le.AdvertiseCallback;
//import android.bluetooth.le.AdvertiseData;
//import android.bluetooth.le.AdvertiseSettings;
//import android.bluetooth.le.BluetoothLeAdvertiser;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Build;
//import android.os.Handler;
//import android.os.Looper;
//import android.os.Message;
//import android.os.ParcelUuid;
//import android.util.Log;
//
//import com.ubtech.utilcode.utils.LogUtils;
//import com.ubtechinc.bluetoothrobot.utils.ProtoUtil;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.UnsupportedEncodingException;
//import java.util.Arrays;
//import java.util.UUID;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import static com.ubtechinc.bluetoothrobot.utils.BleConstants.ALEARDY_CONNECT_ERROR_CODE;
//import static com.ubtechinc.bluetoothrobot.utils.BleConstants.BLE_NETWORK_ERROR;
//import static com.ubtechinc.bluetoothrobot.utils.BleConstants.JSON_COMMAND;
//import static com.ubtechinc.bluetoothrobot.utils.BleConstants.RESPONSE_CODE;
//import static com.ubtechinc.bluetoothrobot.utils.ProtoUtil.getUUID;
//
///**
// * @author：wululin
// * @date：2017/10/19 15:48
// * @modifier：ubt
// * @modify_date：2017/10/19 15:48
// * [A brief description]
// * 蓝牙连接管理类
// */
//@TargetApi(Build.VERSION_CODES.LOLLIPOP)
//public class UbtBluetoothConnManager {
//
//    private static final String TAG = UbtBluetoothConnManager.class.getSimpleName();
//    private static final String TAG1 = "0403";
//    private static final String BLUETOOTH_PROTOCOL_VERSION="V2.0";
//    private static final int CONN_SUCCESS_MSG_WATH = 0x001;
//    private static final int CONN_FIALD_MSG_WATH = 0x002;
//    private static final int DISCONNECT_MSG_WATH = 0x003;
//    private Context mContext;
//    private BluetoothManager mBluetoothManager;
//    private BluetoothAdapter mBluetoothAdapter;
//    private BluetoothGattServer mBlueGattServer;
//    //private byte[] mResultByte = null;
//    private UBTGattService mGattService;
//    private BluetoothLeAdvertiser mAdvertiser;
//    private String mBleNamePrefix;mBleNamePrefix
//    private String mSerialNumber;
//    private BluetoothBLEncryption mBluetoothBLEncrption;
//    private IBluetoothDataCallback mDataCallback;
//    private IBluetoothConnCallback mBluetoothConnCallback;
//    private BluetoothDevice mCurrentDevice;
//    private BluetoothDevice mOtherDevice;
//    private ExecutorService mThreadExecutor;
//    private ICommandEncode commandEncode;
//
//    private Handler mHandler  = new Handler(Looper.getMainLooper()){
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what){
//                case CONN_SUCCESS_MSG_WATH:
//                    if(mBluetoothConnCallback!= null){
//                        BluetoothDevice device = (BluetoothDevice) msg.obj;
//                        mBluetoothConnCallback.connSuccess(device);
//                    }
//                    break;
//
//                case CONN_FIALD_MSG_WATH:
//                    if(mBluetoothConnCallback != null){
//                        mBluetoothConnCallback.connFiald();
//                    }
//                    break;
//
//                case DISCONNECT_MSG_WATH:
//                    if(mBluetoothConnCallback != null){
//                        mBluetoothConnCallback.disConnect();
//                    }
//                    break;
//            }
//        }
//    };
//    private static class UbtBluetoothManagerHolder {
//        @SuppressLint("StaticFieldLeak") public static UbtBluetoothConnManager instance = new UbtBluetoothConnManager();
//    }
//    private UbtBluetoothConnManager(){
//    }
//
//    public static UbtBluetoothConnManager getInstance(){
//        return UbtBluetoothManagerHolder.instance;
//    }
//
//    void setCommandEncode(ICommandEncode commandEncode) {
//        this.commandEncode = commandEncode;
//    }
//
//    public void init(Context context, String bleNamePrefix, String serialNumber){
//        this.mContext = context;
//        this.mBleNamePrefix = bleNamePrefix;
//        this.mSerialNumber = serialNumber;
//        mBluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
//        if (mBluetoothManager != null) {
//            mBluetoothAdapter = mBluetoothManager.getAdapter();
//            mThreadExecutor =  Executors.newCachedThreadPool();
//            mBluetoothBLEncrption = new BluetoothBLEncryption(mSerialNumber);
//            initBluetoothServer();
//        }else {
//            LogUtils.e(TAG, "不能获取系统蓝牙服务, 初始化蓝牙失败!!!!!!!");
//        }
//    }
//
//    /**
//     * 初始化蓝牙
//     */
//    private void initBluetoothServer() {
//        mBlueGattServer = mBluetoothManager.openGattServer(mContext, mGattServerCallback);
//        if(mBlueGattServer != null) {
//            mGattService = new UBTGattService();
//            mBlueGattServer.addService(mGattService.getGattService());
//            initAdvertiser();
//        }else{
//            //Log.e(TAG, "初始化蓝牙设备失败!!!");
//            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    //initBluetoothServer();
//                }
//            },2000);
//        }
//
//    }
//
//    /**
//     * 初始化蓝牙广播信息
//     */
//    private void initAdvertiser() {
//        mAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
//        boolean isSettingOK = mBluetoothAdapter.setName(mBleNamePrefix + mSerialNumber);
//        Log.d(TAG, "bluetooth name change is :" + isSettingOK);
//        AdvertiseSettings mAdvSettings = new AdvertiseSettings.Builder().setAdvertiseMode(
//            AdvertiseSettings.ADVERTISE_MODE_BALANCED)//设置关闭的强弱和延时
//            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)//设置广播发送的频率等级
//            .setConnectable(true) //设置广播是连接还是不连接
//            .build();
//        ParcelUuid pUuid = new ParcelUuid(UUID.fromString(getUUID()));
//        AdvertiseData mAdvData = new AdvertiseData.Builder().addServiceUuid(pUuid)
//            .addServiceData(pUuid, BLUETOOTH_PROTOCOL_VERSION.getBytes())
//            .build();
//
//        AdvertiseData mAdvScanResponse =
//            new AdvertiseData.Builder().setIncludeDeviceName(true) //设置设备的名字是否要在广播的packet
//                .setIncludeTxPowerLevel(true).build();
//        stopAdvertising();
//        if (mAdvertiser != null){
//            mAdvertiser.startAdvertising(mAdvSettings, mAdvData, mAdvScanResponse, mAdvCallback);
//        }else{
//            Log.e(TAG, "初始化蓝牙广播失败!!!!!!!!!!");
//        }
//        registerBleReceiver();
//    }
//
//    private void stopAdvertising(){
//        if(mAdvertiser != null){
//            mAdvertiser.stopAdvertising(mAdvCallback);
//        }
//    }
//
//    private void registerBleReceiver(){
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
//        mContext.registerReceiver(mBleReceiver,intentFilter);
//    }
//
//
//
//    void setDataCallback(IBluetoothDataCallback dataCallback){
//        this.mDataCallback = dataCallback;
//    }
//
//    void setBluetoothConnCallback(IBluetoothConnCallback bluetoothConnCallback){
//        this.mBluetoothConnCallback = bluetoothConnCallback;
//    }
//
//    /**
//     * 判断是否是第二台手机来连接机器人
//     */
//    boolean isOtherDevicesConnect(){
//        return mOtherDevice == null;
//    }
//
//    /**
//     * 收到信息手机发送过来的数据
//     * @param value 每一个数据包的数据
//     */
//    private void handleDataFromMobile(byte[] value) {
//        if (commandEncode.addData(value)) {
//            if (mDataCallback != null) {
//                mDataCallback.receverData(commandEncode.getCommand());
//            }
//        }
//    }
//    void robotConnectToOtherDeviecs(){
//        try {
//            JSONObject reply=new JSONObject();
//            reply.put(JSON_COMMAND,BLE_NETWORK_ERROR);
//            reply.put(RESPONSE_CODE, ALEARDY_CONNECT_ERROR_CODE);
//            sendBleDataToOtherDevices(reply.toString(),mOtherDevice);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    /**
//     * 如果有设备连接了，告诉这台机器人已经被连接了
//     */
//    private void sendBleDataToOtherDevices(String data,BluetoothDevice bluetoothDevice){
//        Log.d(TAG,"加密前的数据==sendBleDataToOtherDevices===: "+data);
//        String encryptionData = mBluetoothBLEncrption.encryptionMessage(data);
//        Log.d(TAG1,"加密后的数据==sendBleDataToOtherDevices===: "+encryptionData.length() + "   " + encryptionData);
//        SendBluetoothData sendBluetoothData = new SendBluetoothData(encryptionData,bluetoothDevice);
//        mThreadExecutor.execute(sendBluetoothData);
//    }
//
//    /**
//     * 通过蓝牙发送数据
//     * @param data 蓝牙数据
//     */
//    void sendBleData(String data){
//        //Log.d(TAG1,"加密前的数据==sendBleDataToOtherDevices===: "+data.length() + "   " + data);
//        //try {
//        //    byte[] originData = data.getBytes("UTF-8");
//        //    Log.d(TAG1,"加密前的数据 originData.size "+originData.length);
//        //} catch (UnsupportedEncodingException e) {
//        //    e.printStackTrace();
//        //}
//        //Log.d(TAG,"加密前的数据=====: "+data);
//        String encryptionData = commandEncode.encryption(data);
//        //Log.d(TAG1,"加密后的数据==sendBleDataToOtherDevices===: "+encryptionData.length() + "   " + encryptionData);
//        try {
//            byte[] originData = encryptionData.getBytes("UTF-8");
//            Log.d(TAG1,"加密后的数据 originData.size "+originData.length);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        SendBluetoothData sendBluetoothData = new SendBluetoothData(encryptionData,mCurrentDevice);
//        mThreadExecutor.execute(sendBluetoothData);
//    }
//
//
//
//    /**
//     * 发送蓝牙数据的任务
//     */
//    class SendBluetoothData implements Runnable{
//        private String data;
//        private BluetoothDevice mBluetoothDevice;
//        SendBluetoothData(String data, BluetoothDevice bluetoothDevice){
//            this.data = data;
//            this.mBluetoothDevice = bluetoothDevice;
//        }
//
//        @Override
//        public void run() {
//            BluetoothGattCharacteristic characteristic = mGattService.getHeartWriteCharacteristic();
//            Log.d(TAG1, " data : " + data + " data.size : " + data.length() );
//            byte[][] packets = commandEncode.encode(data);
//            Log.d(TAG1, " packdets.size : " + packets.length);
//            synchronized (UbtBluetoothConnManager.class) {
//                long beforeTime = System.currentTimeMillis();
//                for (byte[] bytes : packets) {
//                    if (bytes != null) {
//                        if (mBlueGattServer != null) {
//                            characteristic.setValue(bytes);
//                            try {  //fix NullpointException
//                                mBlueGattServer.notifyCharacteristicChanged(mBluetoothDevice,
//                                    characteristic, false);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }
//                Log.d(TAG1, " costTime : " + (System.currentTimeMillis() - beforeTime));
//            }
//        }
//    }
//
//
//    private AdvertiseCallback mAdvCallback = new AdvertiseCallback() {
//        @Override
//        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
//            super.onStartSuccess(settingsInEffect);
//            if(settingsInEffect != null){
//                Log.i(TAG,"Advertising onStartSuccess: " + settingsInEffect.toString());
//            }else {
//                Log.i(TAG,"Advertising settingsInEffect: ====" + null);
//            }
//        }
//
//        @Override
//        public void onStartFailure(int errorCode) {
//            super.onStartFailure(errorCode);
//            Log.i(TAG,"Advertising onStartFailure: " + errorCode);
////            initAdvertiser();
//            ProtoUtil.closeBle();
//        }
//
//    };
//
//    private BroadcastReceiver mBleReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
//                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
//                Log.i(TAG,"blueState=====" + blueState);
//                switch (blueState){
//                    case BluetoothAdapter.STATE_TURNING_ON:
//                        initAdvertiser();
//                        break;
//                    case BluetoothAdapter.STATE_TURNING_OFF:
//                        ProtoUtil.openBle();
//                        break;
//                }
//                mContext.unregisterReceiver(mBleReceiver);
//            }
//        }
//    };
//
//
//
//    public BluetoothDevice getCurrentDevice(){
//        return mCurrentDevice;
//    }
//    private final BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
//        @Override
//        public void onConnectionStateChange(BluetoothDevice device, final int status, int newState) {
//            super.onConnectionStateChange(device, status, newState);
//            Log.i(TAG,"status====" + status + ";;newState=====" + newState);
//            if (status == BluetoothGatt.GATT_SUCCESS){
//                if (newState == BluetoothGatt.STATE_CONNECTED){
//                    if(mCurrentDevice == null){
//                        mCurrentDevice = device;
//                        mOtherDevice = null;
//                        Message msg = new Message();
//                        msg.what = CONN_SUCCESS_MSG_WATH;
//                        msg.obj = device;
//                        mHandler.sendMessage(msg);
//                    }else {
//                        mOtherDevice = device;
////                        mBlueGattServer.cancelConnection(device);
//                    }
//                } else if (newState == BluetoothGatt.STATE_DISCONNECTED){
//                    if(mCurrentDevice != null){
//                        if(mCurrentDevice.getAddress().equals(device.getAddress())){
////                        mBlueGattServer.cancelConnection(mCurrentDevice);
//                            mCurrentDevice = null;
//                            //mResultByte = null;
//                        }
//                    }
//                    if(mOtherDevice != null){
//                        if(mOtherDevice.getAddress().equals(device.getAddress())){
//                            mOtherDevice = null;
//                        }
//                    }
//                    mHandler.sendEmptyMessage(DISCONNECT_MSG_WATH);
//                }
//            } else{
//                //mResultByte = null;
//                mHandler.sendEmptyMessage(CONN_FIALD_MSG_WATH);
//                Log.e(TAG, "设备连接失败==" + status);
//            }
//        }
//
//        @Override
//        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
//                                                BluetoothGattCharacteristic characteristic) {
//            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
//            Log.i(TAG, "Device tried to read characteristic: " + characteristic.getUuid());
//            Log.i(TAG, "read Value: " + Arrays.toString(characteristic.getValue())+"  offset="+offset);
//            String wiwiListString = "";
//            byte b[] = wiwiListString.getBytes();//String转换为byte[]
//            /* 这里是读请求，可以把WIFI列表返回给手机 */
//            if (offset != 0) {
//                mBlueGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset,
//                        b);
//                return;
//            }
//            mBlueGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
//                    offset, b);
//        }
//
//        @Override
//        public void onNotificationSent(BluetoothDevice device, int status) {
//            super.onNotificationSent(device, status);
//        }
//
//        @Override
//        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
//                                                 BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
//                                                 int offset, byte[] value) {
//            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite,
//                    responseNeeded, offset, value);
//            Log.i(TAG, "Characteristic Write request: " + Arrays.toString(value) +
//                    "  offset=" + offset + ";;responseNeeded===" + responseNeeded + ";;requestId====" + requestId + ";;device====" + device);
//            handleDataFromMobile(value);
//            if(responseNeeded){
//                if(!isDisconnect){
//                    mBlueGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0,null);
//                }
//                isDisconnect = false;
//            }
//        }
//
//        @Override
//        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
//                                             BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded,
//                                             int offset,
//                                             byte[] value) {
//            Log.i(TAG, "Descriptor Write Request " + descriptor.getUuid() + " " + Arrays.toString(value));
//            Log.i(TAG, "responseNeeded====" + responseNeeded);
//            if(responseNeeded) {
//                //String s = "hellow world";
//                //byte b[] = s.getBytes();//String转换为byte[]
//                mBlueGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0,null);
//            }
//
//        }
//
//
//        //.特征被读取。当回复响应成功后，客户端会读取然后触发本方法
//        @Override
//        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
//            Log.e(TAG, String.format("onDescriptorReadRequest：device name = %s, address = %s , requestId = %s", device.getName(), device.getAddress(), requestId));
//            mBlueGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
//        }
//
//    };
//
//    private boolean isDisconnect = false;
//    void disconnectCurrentDevices(){
//        if(mCurrentDevice != null){
//            isDisconnect = true;
//            mBlueGattServer.cancelConnection(mCurrentDevice);
//            mCurrentDevice = null;
//
//        }
//    }
//
//    public interface IBluetoothConnCallback {
//        void connSuccess(BluetoothDevice device);
//        void connFiald();
//        void disConnect();
//    }
//
//
//    public interface IBluetoothDataCallback {
//
//         void receverData(String result);
//
//    }
//
//}
