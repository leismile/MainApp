package com.ubtechinc.protocollibrary.communite.old;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

//import com.scottyab.aescrypt.AESCrypt;

/**
 * 处理蓝牙加密业务
 * Created by Liudongyang on 2017/7/3.
 */

public class BluetoothBLEncryption {

    private String TAG = getClass().getSimpleName();
    private Boolean encryptionEnable=true;

    private String mSerialNumber;

    private String mPassWordKey;

    public BluetoothBLEncryption(String searlNumber){
        mSerialNumber = searlNumber;
        mPassWordKey = getPasswordKey();
    }


    public byte[] encryptionMessage(String data){
        String messageAfterEncrypt="";
        if(encryptionEnable) {
            try {
                messageAfterEncrypt = AESCrypt.encrypt(mPassWordKey, data);
            } catch (GeneralSecurityException e) {
                //handle error - could be due to incorrect password or tampered encryptedMsg
            }
        }else {
            messageAfterEncrypt=data;
        }
        try {
            return messageAfterEncrypt.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }


    public String decryptionMessage(String data){
        String messageAfterDecrypt = "";
        if(encryptionEnable) {
            try {
                messageAfterDecrypt = AESCrypt.decrypt(mPassWordKey, data);
                Log.d(TAG, "messageAfterDecrypt    " + messageAfterDecrypt);
            } catch (GeneralSecurityException e) {
                //handle error - could be due to incorrect password or tampered encryptedMsg
            }
        }else {
            messageAfterDecrypt=data;
        }
        return  messageAfterDecrypt;
    }

    private String getPasswordKey(){
        return DesUtil.getMD5(mSerialNumber.substring(mSerialNumber.length()-4), 32);
    }

}
