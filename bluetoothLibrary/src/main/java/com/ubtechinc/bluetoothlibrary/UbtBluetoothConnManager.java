package com.ubtechinc.bluetoothlibrary;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.util.Log;

import com.ubtech.utilcode.utils.ListUtils;
import com.ubtech.utilcode.utils.LogUtils;
import com.ubtech.utilcode.utils.notification.NotificationCenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author：wululin
 * @date：2017/10/19 15:48
 * @modifier：ubt
 * @modify_date：2017/10/19 15:48
 * [A brief description]
 * 蓝牙连接管理类
 */
public class UbtBluetoothConnManager {

    private static final String TAG = UbtBluetoothConnManager.class.getSimpleName();
    private static final String BLUETOOTH_PROTOCOL_VERSION = "V2.0";
    //连接成功
    private static final int CONN_SUCCESS_MSG_WATH = 0x001;
    //连接失败
    private static final int CONN_FIALD_MSG_WATH = 0x002;
    //断开
    private static final int DISCONNECT_MSG_WATH = 0x003;
    private Context mContext;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGattServer mBlueGattServer;
    private UBTGattService mGattService;
    private OldUbtGattService mOldGattService;
    private BluetoothLeAdvertiser mAdvertiser;
    private String mBleNamePrefix;
    private String mSerialNumber;
    private AdvertiseSettings mAdvSettings;
    private AdvertiseData mAdvData;
    private AdvertiseData mAdvScanResponse;
    private IBluetoothDataCallback mDataCallback;
    private BleConnectListener mBluetoothConnCallback;
    private volatile BluetoothDevice mCurrentDevice;
    private List<BluetoothDevice> mOtherDevices = new ArrayList<>();
    private LinkedBlockingQueue<DataObject> dataQueque = new LinkedBlockingQueue<>();

    private Thread sendBtDataThread;

    private boolean mIsOldServiceAddSuccess = false;
    private boolean mIsNewServiceAddSuccess = false;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            BleStatusEvent bleStatusEvent = new BleStatusEvent();
            switch (msg.what) {
                case CONN_SUCCESS_MSG_WATH:
                    if (mBluetoothConnCallback != null) {
                        BluetoothDevice device = (BluetoothDevice) msg.obj;
                        mBluetoothConnCallback.onSuccess(device);

                    }

                    bleStatusEvent.setType(bleStatusEvent.getTYPE_CONNECTED());
                    notificationEvent(bleStatusEvent);
                    break;

                case CONN_FIALD_MSG_WATH:
                    if (mBluetoothConnCallback != null) {

                        mBluetoothConnCallback.onFailed();
                    }
                    bleStatusEvent.setType(bleStatusEvent.getTYPE_FAILED());
                    notificationEvent(bleStatusEvent);
                    break;

                case DISCONNECT_MSG_WATH:
                    if (mBluetoothConnCallback != null) {

                        mBluetoothConnCallback.onLost();
                    }
                    notificationEvent(bleStatusEvent);
                    break;
            }
        }
    };


    private void notificationEvent(BleStatusEvent bleStatusEvent) {

        NotificationCenter.defaultCenter().publish(bleStatusEvent);
    }


    private static class UbtBluetoothManagerHolder {
        public static UbtBluetoothConnManager instance = new UbtBluetoothConnManager();
    }

    private UbtBluetoothConnManager() {
    }

    public static UbtBluetoothConnManager getInstance() {
        return UbtBluetoothManagerHolder.instance;
    }


    private AtomicBoolean mIsInit = new AtomicBoolean(false);

    public synchronized void init(Context context, String bleNamePrefix, String serialNumber) {
        if (mIsInit.get()) {
            LogUtils.i("already init");

        } else {
            this.mContext = context;
            this.mBleNamePrefix = bleNamePrefix;
            this.mSerialNumber = serialNumber;
            initBluetoothServer(context);
            registerBleReceiver();
            sendBtDataThread = new Thread(new SendBluetoothData());
            sendBtDataThread.start();
            mIsInit.set(true);
        }

    }


    /**
     * 初始化蓝牙,每次蓝牙重启都会重新初始化
     */
    private synchronized void initBluetoothServer(final Context context) {
        mIsNewServiceAddSuccess = false;
        mIsOldServiceAddSuccess = false;
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBlueGattServer = mBluetoothManager.openGattServer(mContext, mGattServerCallback);
        if (mBlueGattServer != null) {
            mGattService = new UBTGattService();
            mOldGattService = new OldUbtGattService();
            mBlueGattServer.addService(mGattService.getGattService());
            mBlueGattServer.addService(mOldGattService.getGattService());
            initAdvertiser();
        } else {
            Log.e(TAG, "初始化蓝牙设备失败!!!");
            //mHandler.postDelayed(mCloseBleRunnable, 2000);
            mCloseBleFuture = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(mCloseBleRunnable, 2, 2, TimeUnit.SECONDS);
        }

    }

    // fix send always!!!
    private ScheduledFuture<?> mCloseBleFuture;
    private final Runnable mCloseBleRunnable = new Runnable() {
        @Override
        public void run() {
            if (mBlueGattServer == null) {
                BleUtil.closeBle();
            } else {
                mCloseBleFuture.cancel(true);
            }
        }
    };

    /**
     * 初始化蓝牙广播信息
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initAdvertiser() {
        mAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        StringBuffer buffer = new StringBuffer();
        buffer.append(mBleNamePrefix).append(mSerialNumber);
        boolean isSettingOK = mBluetoothAdapter.setName(buffer.toString());
        Log.d(TAG, "bluetooth name change is :" + isSettingOK);
        mAdvSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)//设置关闭的强弱和延时
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)//设置广播发送的频率等级
                .setConnectable(true) //设置广播是连接还是不连接
                .build();
        ParcelUuid pUuid = new ParcelUuid(UUID.fromString(BleUtil.getUUID()));
        mAdvData = new AdvertiseData.Builder()
                .addServiceUuid(pUuid)
                .addServiceData(pUuid, BLUETOOTH_PROTOCOL_VERSION.getBytes())
                .build();

        mAdvScanResponse = new AdvertiseData.Builder()
                .setIncludeDeviceName(true) //设置设备的名字是否要在广播的packet
                .build();

        if (mAdvertiser != null) {
            if (mAdvCallback == null) {
                mAdvCallback = new AdvertiseCallback() {
                    @Override
                    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                        super.onStartSuccess(settingsInEffect);
                        if (!mIsNewServiceAddSuccess || !mIsOldServiceAddSuccess) {//只要有一个服务没有加入成功，则重启蓝牙
                            if (mCloseBleFuture == null || mCloseBleFuture.isCancelled()) {
                                mCloseBleFuture = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(mCloseBleRunnable, 2, 2, TimeUnit.SECONDS);
                            }
                        }
                        Log.i(TAG, "Advertising onStartSuccess: ");
                    }

                    @Override
                    public void onStartFailure(int errorCode) {
                        super.onStartFailure(errorCode);
                        Log.i(TAG, "Advertising onStartFailure: " + errorCode);
                        if (mCloseBleFuture == null || mCloseBleFuture.isCancelled()) {
                            mCloseBleFuture = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(mCloseBleRunnable, 2, 2, TimeUnit.SECONDS);
                        }

                    }

                };
            }

            mAdvertiser.startAdvertising(mAdvSettings, mAdvData, mAdvScanResponse, mAdvCallback);
            LogUtils.i("startAdvertising ");
        } else {
            LogUtils.e(TAG, "初始化蓝牙广播失败!!!!!!!!!!");
        }

    }

    private void registerBleReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(mBleReceiver, intentFilter);
    }

    private void unRegisterBleReceiver() {
        mContext.unregisterReceiver(mBleReceiver);
    }


    public synchronized void setDataCallback(IBluetoothDataCallback dataCallback) {
        this.mDataCallback = dataCallback;
    }

    public synchronized void setBluetoothConnCallback(BleConnectListener bluetoothConnCallback) {
        this.mBluetoothConnCallback = bluetoothConnCallback;
    }

    /**
     * 判断是否是第二台手机来连接机器人
     */
    public synchronized boolean isOtherDeviceConnect() {
        return !ListUtils.isEmpty(mOtherDevices);
    }

    private class DataObject {
        private List<byte[]> data;
        private BluetoothDevice peer;
        private int characteristicVersion;

        public List<byte[]> getData() {
            return data;
        }

        public void setData(List<byte[]> data) {
            this.data = data;
        }

        public BluetoothDevice getPeer() {
            return peer;
        }

        public void setPeer(BluetoothDevice peer) {
            this.peer = peer;
        }

        public int getCharacteristicVersion() {
            return characteristicVersion;
        }

        public void setCharacteristicVersion(int characteristicVersion) {
            this.characteristicVersion = characteristicVersion;
        }
    }

    /**
     * 通过蓝牙发送数据
     *
     * @param data 蓝牙数据
     */
    public synchronized void sendBleData(List<byte[]> data, Object peer) {
        try {
            BleSender bleSender = (BleSender) peer;

            if (bleSender.getPeer() instanceof BluetoothDevice) {//指定了设备，则发到指定设备
                DataObject dataObject = new DataObject();
                dataObject.setData(data);
                dataObject.setPeer((BluetoothDevice) bleSender.getPeer());
                dataObject.setCharacteristicVersion(bleSender.getBleVersion());
                dataQueque.offer(dataObject);
            } else if (bleSender.getPeer() instanceof String) {//没有指定设备，则默认发送到当前设备
                DataObject dataObject = new DataObject();
                dataObject.setData(data);
                dataObject.setPeer(mCurrentDevice);
                dataObject.setCharacteristicVersion(bleSender.getBleVersion());
                dataQueque.offer(dataObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 发送蓝牙数据的任务
     */
    class SendBluetoothData implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {

                    DataObject dataObject = dataQueque.take();
                    synchronized (UbtBluetoothConnManager.class) {
                        for (int i = 0; i < dataObject.getData().size(); i++) {
                            byte[] bytes = dataObject.getData().get(i);
                            if (bytes != null) {
                                BluetoothGattCharacteristic characteristic;
                                if (dataObject.getCharacteristicVersion() == 0) {
                                    characteristic = mOldGattService.getNotifyCharacteristic();
                                } else {
                                    characteristic = mGattService.getNotifyCharacteristic();
                                }
                                if (characteristic != null && mBlueGattServer != null && dataObject.getPeer() != null) {
                                    try {
                                        characteristic.setValue(bytes);
                                        mBlueGattServer.notifyCharacteristicChanged(dataObject.getPeer(), characteristic, false);
                                        SystemClock.sleep(20);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        }

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    AdvertiseCallback mAdvCallback;

    private BroadcastReceiver mBleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                Log.i(TAG, "blueState=====" + blueState);
                switch (blueState) {
                    case BluetoothAdapter.STATE_ON:
                        initBluetoothServer(mContext);
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        BleUtil.openBle();
                        break;
                }
//                mContext.unregisterReceiver(mBleReceiver);
            }
        }
    };


    public synchronized boolean isConnected() {
        return mCurrentDevice != null;
    }

    private final BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, final int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            Log.i(TAG, "status====" + status + ";;newState=====" + newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    if (mCurrentDevice == null) {
                        mCurrentDevice = device;
                        mOtherDevices.clear();
                        Message msg = new Message();
                        msg.what = CONN_SUCCESS_MSG_WATH;
                        msg.obj = device;
                        mHandler.sendMessage(msg);
                    } else {
                        mOtherDevices.add(device);
                    }
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {

                    if (mCurrentDevice != null) {
                        if (mCurrentDevice.getAddress().equals(device.getAddress())) {
                            mCurrentDevice = null;
                            mHandler.sendEmptyMessage(DISCONNECT_MSG_WATH);

                        }
                    }
                    for (int i = 0; i < mOtherDevices.size(); i++) {
                        if (mOtherDevices.get(i).equals(device.getAddress())) {
                            mOtherDevices.remove(i);
                        }
                    }

                }
            } else {
                mHandler.sendEmptyMessage(CONN_FIALD_MSG_WATH);
                Log.e(TAG, "设备连接失败==" + status);
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.i(TAG, "Device tried to read characteristic: " + characteristic.getUuid());
            Log.i(TAG, "read Value: " + Arrays.toString(characteristic.getValue()) + "  offset=" + offset);
            String wiwiListString = new String();
            byte b[] = wiwiListString.getBytes();//String转换为byte[]
            //这里是读请求，可以把WIFI列表返回给手机
            if (offset != 0) {
                mBlueGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset,
                        b);
                return;
            }
            mBlueGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
                    offset, b);
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            Log.i(TAG, "onNotificationSent status " + status);
            super.onNotificationSent(device, status);
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                 BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
                                                 int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite,
                    responseNeeded, offset, value);
            LogUtils.d(TAG, "Characteristic Write request: " + Arrays.toString(value) +
                    "  offset=" + offset + ";responseNeeded===" + responseNeeded + ";requestId====" + requestId + ";device====" + device + " characteristic = " + characteristic.getUuid());
            if (responseNeeded) {
                if (!isDisconnect) {
                    mBlueGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
                    LogUtils.i(TAG, " sendResponse BluetoothGatt.GATT_SUCCESS ");
                }
                isDisconnect = false;
            }
            if (UBTGattService.READ_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                mDataCallback.onReceiveData(value, new BleSender(device, 1));
            } else {//老的协议
                mDataCallback.onReceiveData(value, new BleSender(device, 0));
            }


        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded,
                                             int offset,
                                             byte[] value) {
            Log.i(TAG, "Descriptor Write Request " + descriptor.getUuid() + " " + Arrays.toString(value));
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded,
                    offset, value);
            Log.i(TAG, "responseNeeded====" + responseNeeded);
            if (responseNeeded) {
                mBlueGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
            }

        }


        //.特征被读取。当回复响应成功后，客户端会读取然后触发本方法
        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            Log.e(TAG, String.format("onDescriptorReadRequest：device name = %s, address = %s", device.getName(), device.getAddress()));
            Log.e(TAG, String.format("onDescriptorReadRequest：requestId = %s", requestId));
            mBlueGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
            if (service.getUuid().equals(mOldGattService.getGattService().getUuid()) && status == 0) {
                mIsOldServiceAddSuccess = true;
            } else if (service.getUuid().equals(mGattService.getGattService().getUuid()) && status == 0) {
                mIsNewServiceAddSuccess = true;
            }

            LogUtils.d(TAG, String.format("onServiceAdded：status = %s , service = %s", status, service.getUuid()));
        }
    };

    private boolean isDisconnect = false;

    public void cancelCurrentDevices() {
        LogUtils.d("cancelCurrentDevices");
        if (mCurrentDevice != null && mBlueGattServer != null) {
            isDisconnect = true;
            mBlueGattServer.cancelConnection(mCurrentDevice);
            mHandler.sendEmptyMessage(DISCONNECT_MSG_WATH);
            mCurrentDevice = null;
        }
    }

    public void cancelDevice(BluetoothDevice bluetoothDevice) {
        LogUtils.d("cancelDevice");
        if (bluetoothDevice != null && mBlueGattServer != null) {
            mBlueGattServer.cancelConnection(bluetoothDevice);
            mOtherDevices.remove(bluetoothDevice);
        }
    }

    public interface BleConnectListener {
        void onSuccess(BluetoothDevice device);

        void onFailed();

        void onLost();
    }

    public interface IBluetoothDataCallback {
        void onReceiveData(byte[] data, Object from);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public synchronized void destroy() {
        if (mIsInit.get()) {
            try {

                if (mBlueGattServer != null) {
                    // cancelCurrentDevices();
                    if (sendBtDataThread != null) sendBtDataThread.interrupt();

                    mBlueGattServer.close();
                }

                if (mAdvertiser != null) {
                    mAdvertiser.stopAdvertising(mAdvCallback);
                    mAdvertiser = null;
                    mAdvCallback = null;
                }
                mCloseBleFuture.cancel(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mIsInit.set(false);
        } else {
            LogUtils.i("destroy fail not init");
        }


    }

}
