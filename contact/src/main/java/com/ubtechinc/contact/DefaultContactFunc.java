package com.ubtechinc.contact;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ISub;
import com.android.internal.telephony.ITelephony;
import com.ubtechinc.alpha.CmQueryCallRecord;
import com.ubtechinc.alpha.CmQueryContactList;
import com.ubtechinc.contact.model.UserContact;
import com.ubtechinc.contact.util.PhoneInfoUtils;
import com.ubtechinc.contact.util.SystemProperties;
import com.ubtrobot.speech.SpeechApi;
import com.ubtrobot.speech.listener.TTsListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @desc : 默认通讯功能实现
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/1/31
 */

public class DefaultContactFunc implements IContactFunc{

    private static final String TAG = "DefaultContactFunc";
    private static final int DEFAULT_SIM_SUB_ID = 1;
    private Context context = Contact.getInstance().getContext();
    private IContact contact = new DefaultContact();
    private ICallRecord callRecord = new DefaultCallRecord();
    private static DefaultContactFunc instance;

    private DefaultContactFunc() {
    }

    public static DefaultContactFunc getInstance() {
        if(instance == null) {
            synchronized (DefaultContactFunc.class) {
                if(instance == null) {
                    instance = new DefaultContactFunc();
                }
            }
        }
        return instance;
    }

    @Override
    public boolean simExist() {
        TelephonyManager telMgr = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telMgr.getSimState();
        boolean result = true;
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                result = false; // 没有SIM卡
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                result = false;
                break;
        }
        return result;
    }

    @Override
    public String getSimNumber() {
        String phoneNumber = new PhoneInfoUtils(context).getNativePhoneNumber();
        Log.d(TAG, " getSimNumber -- phoneNumber: " + phoneNumber);
        return phoneNumber;
    }


    private ISub getISub() {
        Method method = null;
        try {
            method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{"isub"});
            ISub iSub = ISub.Stub.asInterface(binder);
            return iSub;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isOpenData() {
        TelephonyManager telephonyService = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method getMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("getDataEnabled");
            if (null != getMobileDataEnabledMethod)
            {
                boolean mobileDataEnabled = (Boolean) getMobileDataEnabledMethod.invoke(telephonyService);
                return mobileDataEnabled;
            }
        }
        catch (Exception e) {
            Log.v(TAG, "Error getting" + ((InvocationTargetException)e).getTargetException() + telephonyService);
        }

        return false;
    }

    @Override
    public boolean isOpenRoam() {

        boolean isDataRoamingEnabled = "true".equalsIgnoreCase(SystemProperties.getProperty(
                "ro.com.android.dataroaming", "false"));

        isDataRoamingEnabled = Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.DATA_ROAMING + DEFAULT_SIM_SUB_ID, isDataRoamingEnabled ? 1 : 0) != 0;
        return isDataRoamingEnabled;
    }

    @Override
    public boolean modifyDataStatus(boolean isOpen) {
        // 先指定默认数据卡
        ISub iSub = getISub();
        if(iSub != null) {
            try {
                int result = iSub.getDefaultDataSubId();
                if(result != DEFAULT_SIM_SUB_ID) {
                    iSub.setDefaultDataSubId(DEFAULT_SIM_SUB_ID);
                    iSub.setDefaultSmsSubId(DEFAULT_SIM_SUB_ID);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        boolean result = setMobileData(context, isOpen);
        Log.d(TAG, " result : " + result);
        return result;
    }

    @Override
    public boolean modifyRoam(boolean isOpen) {
        // 先指定默认数据卡
        ISub iSub = getISub();
        if(iSub != null) {
            try {
                int result = iSub.getDefaultDataSubId();
                if(result != DEFAULT_SIM_SUB_ID) {
                    iSub.setDefaultSmsSubId(DEFAULT_SIM_SUB_ID);
                }
                iSub.setDataRoaming(isOpen ? 1 : 0, DEFAULT_SIM_SUB_ID);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        try {
            SystemProperties.setProperty("ro.com.android.dataroaming", isOpen ? "true" : "false");
            int roaming = isOpen ? 1 : 0;
            Settings.Global.putInt(context.getContentResolver(), Settings.Global.DATA_ROAMING + DEFAULT_SIM_SUB_ID, roaming);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void phoneAnswer() {
        new Thread(new Runnable() {

            private void answer() {
                Log.d(TAG, " answer ");
                TelephonyManager telephonyService = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                Method declaredMethod = null;
                try {
                    declaredMethod = telephonyService.getClass()
                            .getDeclaredMethod("getITelephony");
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
                declaredMethod.setAccessible(true);
                ITelephony itelephony = null;
                try {
                    itelephony = (ITelephony) declaredMethod.invoke(telephonyService);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                if (itelephony != null) {
                    try {
                        itelephony.answerRingingCall();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    Intent intent =new Intent();
                    intent.setAction("android.intent.action.CALL_BUTTON");
                    context.startActivity(intent);
                }
            }

            @Override
            public void run() {
                    SpeechApi.get().playLocalTTs("answer", new TTsListener() {
                        @Override
                        public void onTtsBegin() {
                            Log.d(TAG, " onTtsBegin ");
                        }

                        @Override
                        public void onTtsVolumeChange(int i) {
                            Log.d(TAG, " onTtsBegin ");
                        }

                        @Override
                        public void onTtsCompleted() {
                            Log.d(TAG, " onTtsCompleted ");
                            answer();
                        }

                        @Override
                        public void onError(int i, String s) {
                            Log.d(TAG, " onError ");
                            answer();
                        }
                    });
            }
        }).start();
    }

    @Override
    public void endCall()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                TelephonyManager telephonyService = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                Class<TelephonyManager> telephonyManagerClass = TelephonyManager.class;
                try {
                    //得到TelephonyManager.getITelephony方法的Method对象
                    Method method = telephonyManagerClass.getDeclaredMethod("getITelephony");
                    //允许访问私有方法
                    method.setAccessible(true);
                    //调用getITelephony方法发挥ITelephony对象
                    Object object = method.invoke(telephonyService);
                    //挂断电话
                    Method endCall = Class.forName("com.android.internal.telephony.ITelephony").getMethod("endCall");
                    boolean result = (boolean)endCall.invoke(object);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 设置手机的移动数据
     */
    public static boolean setMobileData(Context pContext, boolean pBoolean) {
        Log.d(TAG, " setMobileData -- pBoolean : " + pBoolean);
        TelephonyManager telephonyService = (TelephonyManager) pContext.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
            if (null != setMobileDataEnabledMethod)
            {
                Log.d(TAG, " setMobileData -- pBoolean 1 : " + pBoolean);
                setMobileDataEnabledMethod.invoke(telephonyService, pBoolean);
                Log.d(TAG, " setMobileData -- pBoolean 2 : " + pBoolean);
                return true;
            }
        }
        catch (Exception e) {
            Log.d(TAG, " setMobileData -- pBoolean 3 : " + pBoolean);
            Log.v(TAG, "Error setting" + ((InvocationTargetException)e).getTargetException() + telephonyService);
            Log.d(TAG, " setMobileData -- pBoolean 4 : " + pBoolean);
        } catch (Throwable throwable) {
            Log.d(TAG, " setMobileData -- pBoolean 5 : " + pBoolean);
        }
        Log.d(TAG, " setMobileData -- pBoolean 6 : " + pBoolean);
        return false;
    }

    @Override
    public int importContact(List<CmQueryContactList.CmContactInfo> cmContactInfos, String userId) {
        int result = contact.importContact(cmContactInfos, userId);
        return result;
    }

    @Override
    public List<CmQueryContactList.CmContactInfo> queryContactList(int position, long versionNumber) {
        return contact.queryContactList(position, versionNumber);
    }

    @Override
    public long addContact(CmQueryContactList.CmContactInfo cmContactInfo, String userId) {
        return contact.addContact(cmContactInfo, userId);
    }

    @Override
    public int modifyContact(long contactId, String name, String phone, String userId) {
        return contact.modifyContact(contactId, name, phone, userId);
    }

    @Override
    public int deleteContact(long contactId, String userId) {
        return contact.deleteContact(contactId, userId);
    }

    @Override
    public List<CmQueryCallRecord.CmCallRecordInfo> queryCallRecord(int position, int versionCode) {
        return callRecord.queryCallRecord(position, versionCode);
    }

    @Override
    public int getCallRecordSize(int versionCode) {
        return callRecord.getCallRecordSize(versionCode);
    }

    @Override
    public long getVersionCode() {
        return contact.getVersionCode();
    }

    @Override
    public int getTotalPage(long versionNumber) {
        return contact.getTotalPage(versionNumber);
    }

    @Override
    public List<UserContact> getPhoneNumber(String name) {
        return contact.getPhoneNumber(name);
    }

    @Override
    public String containsPhoneNumber(String phoneNumber) {
        return contact.containsPhoneNumber(phoneNumber);
    }

    @Override
    public List<String> queryPhoneNumber(String name) {
        return contact.queryPhoneNumber(name);
    }

    @Override
    public void update() {
        callRecord.update();
    }

    @Override
    public long getCallRecordVersionCode() {
        return callRecord.getCallRecordVersionCode();
    }
}
