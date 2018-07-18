package com.ubtechinc.contact;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.ubtechinc.contact.util.Constant;
import com.ubtechinc.contact.util.TTSPlayUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @desc :
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/3/23
 */

public class DefaultPhoneAnswer implements IPhoneAnswer{

    private static final String TAG = "DefaultPhoneAnswer";
    private static final String ACTION_DIALER = "com.ubtrobot.dialer.ACTION_CALL";
    private static final String ACTION_DIALER_FUNC_KEY = "funckey";
    private static final int FUNC_ANSWER = 1;
    private static final int FUNC_DECLINE = 2;

    private Context context;

    public DefaultPhoneAnswer(Context context) {
        this.context = context;
    }

    @Override
    public void onAnswer() {
        Log.d(TAG, " onAnswer ");
        TTSPlayUtil.playLocalTTs(Constant.LOCAL_TTSNAME_ANSWER, new TTSPlayUtil.IPlayListener() {
            @Override
            public void onError() {
                onResult();
            }

            @Override
            public void onFinish() {
                onResult();
            }

            private void onResult() {
//                Intent intent = new Intent(ACTION_DIALER);
//                intent.putExtra(ACTION_DIALER_FUNC_KEY, FUNC_ANSWER);
//                sendBroadcast(intent);
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
        });

    }

    @Override
    public void onDecline() {
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

    @Override
    public void onDeclineComming() {
        Log.d(TAG, " onDeclineComming ");
        // 由于时序问题，延迟发送广播
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, " onDeclineComming sendBroadcast ACTION_DIALER");
                Intent intent = new Intent(ACTION_DIALER);
                intent.putExtra(ACTION_DIALER_FUNC_KEY, FUNC_DECLINE);
                sendBroadcast(intent);
            }
        }, 100);
    }

    private void sendBroadcast(Intent intent) {
        context.sendBroadcast(intent);
    }
}
