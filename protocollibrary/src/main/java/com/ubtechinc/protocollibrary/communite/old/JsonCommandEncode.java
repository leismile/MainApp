package com.ubtechinc.protocollibrary.communite.old;

import android.util.Log;

/**
 * @desc : Json形式加密和拆分编码
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/4/3
 */

public class JsonCommandEncode implements ICommandEncode {

    private static final String TAG = "JsonCommandEncode";
    protected BluetoothBLEncryption bluetoothBLEncryption;
    private byte[] mResultByte = null;
    private static JsonCommandEncode instance;
    public static JsonCommandEncode get() {

        if(instance == null) {
            synchronized (JsonCommandEncode.class) {
                if(instance == null) {
                    instance = new JsonCommandEncode();
                }
            }

        }
        return instance;
    }

    public JsonCommandEncode init(String mSerialNumber) {
        bluetoothBLEncryption = new BluetoothBLEncryption(mSerialNumber);
        return instance;
    }

    @Override
    public byte[] encryption(String content) {
        return bluetoothBLEncryption.encryptionMessage(content);
    }

    @Override
    public byte[][] encode(byte[] content) {
        return BLEDataUtil.encode(content);
    }

    @Override
    public boolean addData(byte[] data) {
        mResultByte = BLEDataUtil.decode(data, mResultByte);//作为全局变量..
        boolean result = BLEDataUtil.isEnd(data);
        Log.d(TAG, " addData -- result : " + result);
        return result;
    }

    @Override
    public String getCommand() {
        String result = new String(mResultByte);
        String decryptionResult = bluetoothBLEncryption.decryptionMessage(result);
        mResultByte = null;
        Log.d(TAG, " getCommand -- decryptionResult : " + decryptionResult);
        return decryptionResult;
    }
}
