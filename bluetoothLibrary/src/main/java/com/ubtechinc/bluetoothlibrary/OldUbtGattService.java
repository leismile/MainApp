package com.ubtechinc.bluetoothlibrary;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

/**
 * 老的Gatt Service为了兼容老版本的配网绑定
 * Created by ubt on 2017/2/10.
 */

public class OldUbtGattService {

    private BluetoothGattService mBluetoothGattService;

    //三个特征
    private BluetoothGattCharacteristic mHeartRateMeasurementCharacteristic;
    private BluetoothGattCharacteristic mBodySensorLocationCharacteristic;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private static final UUID HEART_RATE_SERVICE_UUID = UUID
            .fromString("0000180D-0000-1000-8000-00805f9b34fb");


    private static final UUID HEART_RATE_MEASUREMENT_UUID = UUID
            .fromString("00002A37-0000-1000-8000-00805f9b34fb");

    /**
     * See <a href="https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.body_sensor_location.xml">
     * Body Sensor Location</a>
     */
    private static final UUID BODY_SENSOR_LOCATION_UUID = UUID
            .fromString("00002A38-0000-1000-8000-00805f9b34fb");

    /**
     * See <a href="https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_control_point.xml">
     * Heart Rate Control Point</a>
     */
    private static final UUID HEART_RATE_CONTROL_POINT_UUID = UUID
            .fromString("00002A39-0000-1000-8000-00805f9b34fb");


    public OldUbtGattService() {

        mHeartRateMeasurementCharacteristic =
                new BluetoothGattCharacteristic(HEART_RATE_MEASUREMENT_UUID,
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            /* No permissions */ 0);


        mBodySensorLocationCharacteristic =
                new BluetoothGattCharacteristic(BODY_SENSOR_LOCATION_UUID,
                        BluetoothGattCharacteristic.PROPERTY_READ,
                        BluetoothGattCharacteristic.PERMISSION_READ);

        mNotifyCharacteristic =
                new BluetoothGattCharacteristic(HEART_RATE_CONTROL_POINT_UUID,
                        BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                        BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY);

        mBluetoothGattService = new BluetoothGattService(HEART_RATE_SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

//        mBluetoothGattService.addCharacteristic(mHeartRateMeasurementCharacteristic);
//        mBluetoothGattService.addCharacteristic(mBodySensorLocationCharacteristic);
        mBluetoothGattService.addCharacteristic(mNotifyCharacteristic);

    }

    public BluetoothGattService getGattService() {
        return mBluetoothGattService;
    }


    public BluetoothGattCharacteristic getNotifyCharacteristic() {
        return mNotifyCharacteristic;
    }

}
