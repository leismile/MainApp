package com.ubtechinc.alpha.im;

import android.content.Context;
import android.util.Log;

import com.tencent.imsdk.TIMSdkConfig;
import com.tencent.imsdk.TIMUserConfig;
import com.ubtech.utilcode.utils.LogUtils;
import com.ubtech.utilcode.utils.notification.NotificationCenter;
import com.ubtech.utilcode.utils.notification.Subscriber;
import com.ubtechinc.alpha.event.IMLoginResultEvent;
import com.ubtechinc.nets.im.business.LoginBussiness;


/** @author donaldxu
 */
public class TecentIMManager {

    public static final String TAG ="TecentIMManager";   //TecentIMManager

    private Context context;
    private volatile int i;
    private static TecentIMManager sInstance;
    private String controlClientUserId; // 手机控制端的userId,用于发送心跳包

    public static TecentIMManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (TecentIMManager.class) {
                if (sInstance == null) {
                    sInstance = new TecentIMManager(context);
                }
            }
        }
        return sInstance;
    }

    private TecentIMManager(Context context) {
        this.context = context;
    }

    private boolean isLoginSuccess() {
        return LoginBussiness.getInstance(context).isLoginIM();
    }

    public void init(String sid) {
        LogUtils.d("TecentIMManager oncreate!!!");

        i = 0;
        LoginBussiness.getInstance(context).login(sid);
        NotificationCenter.defaultCenter().subscriber(IMLoginResultEvent.class, loginResultSubscriber);
    }

    public void logout() {
        LoginBussiness.getInstance(context).logout();
    }


    Subscriber<IMLoginResultEvent> loginResultSubscriber = new Subscriber<IMLoginResultEvent>(){

        @Override
        public void onEvent(IMLoginResultEvent imLoginResultEvent) {
            if(imLoginResultEvent.success) {
                Log.d(TAG,"IM Login Success");
            } else {
                Log.d(TAG,"IM Login Error");
            }
        }
    };



    public void setUserId(String userId) {
        LoginBussiness.getInstance(context).setUserId(userId);
    }



//    private void saveRobotControl(String info){
//        SharedPreferences preferences = context.getSharedPreferences("RobotControl",
//                Context.MODE_MULTI_PROCESS| Context.MODE_WORLD_WRITEABLE| Context.MODE_PRIVATE);
//        preferences.edit().putString("ControlName",info).commit();
//    }

}
