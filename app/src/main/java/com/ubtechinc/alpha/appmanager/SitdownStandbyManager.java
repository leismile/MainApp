package com.ubtechinc.alpha.appmanager;

import android.support.annotation.NonNull;
import android.util.Log;

import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.sauron.api.FaceApi;
import com.ubtechinc.sauron.api.FaceInfo;
import com.ubtechinc.sauron.api.FaceTrackListener;
import com.ubtechinc.sauron.api.SauronApi;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.masterevent.protos.SysMasterEvent;
import com.ubtrobot.transport.message.CallException;

import java.util.List;

import event.master.ubtrobot.com.sysmasterevent.SysEventApi;
import event.master.ubtrobot.com.sysmasterevent.listener.publish.PublishSysActiveStatusListener;


/**
 * Created by lulin.wu on 2018/4/4.
 * 蹲下待命管理类
 */

public class SitdownStandbyManager {
    private static final String TAG = "SitdownStandbyManager";
    private SitdownStandbyManager(){}
    private static class SitdownStandbyManagerHolder {
        public static SitdownStandbyManager instance = new SitdownStandbyManager();
    }
    public static SitdownStandbyManager get(){
        return SitdownStandbyManager.SitdownStandbyManagerHolder.instance;
    }


    public void intoSitdownStandbyManager(){
        SmallActionManager.get().randomExpressIntoSitdownStandby();
        boolean isLowpoer = UbtBatteryManager.getInstance().isLowPower();
        Log.i(TAG,"进入坐下待命检测低电状态==========" + isLowpoer);
        if(!isLowpoer){
            InfraRedManager.get().stopObjectDetection();
            Log.i(TAG,"intoSitdownStandbyManager=======");
            InfraRedManager.get().startObjectDetection(new InfraRedManager.ObjectDetectionListener() {
                @Override
                public void onObjectMove(int distance) {
                    Log.i(TAG,"onObjectMove=========" + distance);
                    InfraRedManager.get().stopObjectDetection();
                    SmallActionManager.get().stopExcuteSitdownExpress();
                    FaceDetectManager.getInstance().startFaceDetect();
                }

                @Override
                public void onObjectStatic(int distance) {
                }
                @Override
                public void onFail(int code, String message) {
                }
            });
        }
    }

    public void stopFaceDeceteAndExpress(){
        InfraRedManager.get().stopObjectDetection();
        SmallActionManager.get().stopExcuteSitdownExpress();
    }


}
