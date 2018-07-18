package com.ubtechinc.contact;

import android.content.Context;
import android.util.Log;

/**
 * @desc : 通信模块全局类
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/1/31
 */

public class Contact {
    private static final String TAG = "Contact";
    private static Contact instance;
    private static Object lock = new Object();
    private Context context;
    private String robotId;
    private IGetRobotId getRobotId;

    private Contact() {
    }

    public static IContactFunc getContactFunc() {
        return instance.getContactFuncInner();
    }

    public static Contact getInstance() {
        if(instance == null) {
            synchronized (lock) {
                if(instance == null) {
                    instance = new Contact();
                }
            }
        }
        return instance;
    }

    public Context getContext() {
        Log.d(TAG, "getContext - context : " + context);
        return context;
    }

    private void setContext(Context context) {
        Log.d(TAG, "setContext - context : " + context);
        this.context = context;
    }

    public void init(Context context, IGetRobotId getRobotId) {
        setContext(context);
        setGetRobotId(getRobotId);
    }

    public String getRobotId() {
        if(getRobotId != null) {
            return getRobotId.getRobotId();
        }
        return robotId;
    }

    private IContactFunc getContactFuncInner() {
        return DefaultContactFunc.getInstance();
    }

    public void setGetRobotId(IGetRobotId getRobotId) {
        this.getRobotId = getRobotId;
    }

    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }
}
