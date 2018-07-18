package com.ubtechinc.alpha.appmanager;

import android.util.Log;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.express.listeners.AnimationListener;
import com.ubtrobot.masterevent.protos.SysMasterEvent;
import com.ubtrobot.mini.libs.scenes.Scene;
import com.ubtrobot.mini.libs.scenes.SceneListener;
import com.ubtrobot.mini.libs.scenes.SceneUtils;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import ubtechinc.com.standupsdk.StandUpApi;

/**
 * Created by lulin.wu on 2018/3/28.
 */

public class SmallActionManager {
    private static final String TAG = "SmallActionManager";
    private static String[] expresss = {"w_basic_0001_1","w_basic_0001_2","w_basic_0001_3","w_basic_0001_4"
    ,"w_basic_0001_5","w_basic_0001_6","w_basic_0001_7","w_basic_0001_8","w_basic_0001_9","w_basic_0001_10"
    ,"w_basic_0002_1","w_basic_0002_2","w_basic_0002_3","w_basic_0002_4"
    ,"w_basic_0003_1","w_basic_0004_1","w_basic_0005_1","w_basic_0006_1"
    ,"w_basic_0007_1","w_basic_0007_2"};//基本表情库
    private  List<String> mRandomExpresss;
    private SmallActionManager(){}
    private static class SmallActionManagerHolder {
        public static SmallActionManager instance = new SmallActionManager();
    }
    public static SmallActionManager get(){
        return SmallActionManager.SmallActionManagerHolder.instance;
    }

    /**
     * 开启红外检测
     */
    public void startInfraRed(){
        Log.i(TAG,"startInfraRed===========");
        boolean isLowpower = UbtBatteryManager.getInstance().isLowPower();
        Log.i(TAG,"开红外时检测电量状态=======" + isLowpower);
        SmallActionManager.get().excuteRandomExpress();
        if(!isLowpower){
            InfraRedManager.get().startObjectDetection(new InfraRedManager.ObjectDetectionListener() {
                @Override
                public void onObjectMove(int distance) {
                    Log.i(TAG,"distance=========" + distance);
                    SmallActionManager.get().stopSmallAction();
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
    /**
     * 开始执行随机表情
     */
    public void excuteRandomExpress(){
        isStop = false;
        if(mRandomExpresss != null){
            mRandomExpresss.clear();
        }
        mRandomExpresss = getRandomExpress();
        excuteExpress();
    }

    private List<String> sitdownRandomExpresss;
    public void randomExpressIntoSitdownStandby(){
        isStopExcuteSitdownExpress = false;
        if(sitdownRandomExpresss != null){
            sitdownRandomExpresss.clear();
        }
        sitdownRandomExpresss = getRandomExpress();
        excuteExpressIntoSitdownStandby();
    }
    private void excuteExpressIntoSitdownStandby(){
        if(sitdownRandomExpresss.size() > 0){
            String express = sitdownRandomExpresss.remove(0);
            Log.i(TAG,"express=====" + express);
            ExpressApi.get().doExpress(express,1,Priority.NORMAL, new AnimationListener() {
                @Override
                public void onAnimationStart() {
                }

                @Override
                public void onAnimationEnd() {
                    if(sitdownRandomExpresss.size()>0){
                        excuteExpressIntoSitdownStandby();
                    }else {
                        if(!isStopExcuteSitdownExpress){
                            long currentTime = System.currentTimeMillis();
                            long dTime = currentTime - SysStatusHelpManager.get().getIntoSitdownStandbyTime();
                            LogUtils.i(TAG,"表情结束的时间=====" + dTime);
                            if(dTime < AppConstants.FIVE_MIN){
                                randomExpressIntoSitdownStandby();
                            }else {
                                SysStatusManager.getInstance().publishSysActiveStatus(SysMasterEvent.ActivieStatusType.STANDBY);
                            }
                        }
                    }
                }

                @Override
                public void onAnimationRepeat(int loopNumber) {

                }
            });
        }
    }

    private boolean isStopExcuteSitdownExpress;
    public void stopExcuteSitdownExpress(){
        isStopExcuteSitdownExpress = true;
        if(sitdownRandomExpresss != null){
            ExpressApi.get().doExpress("normal_1",1,Priority.HIGH);
            sitdownRandomExpresss.clear();
        }
    }
    /**
     * 执行表情
     */
    private void excuteExpress(){
        if(mRandomExpresss.size() > 0){
            String express = mRandomExpresss.remove(0);
            LogUtils.i(TAG,"执行表情======" + express);
            ExpressApi.get().doExpress(express,1, Priority.NORMAL, new AnimationListener() {
                @Override
                public void onAnimationStart() {
                }

                @Override
                public void onAnimationEnd() {
                    if(mRandomExpresss.size()>0){
                        excuteExpress();
                    }else {
                        if(!isStop){
                            long currentTime = System.currentTimeMillis();
                            long dTime = currentTime - SysStatusHelpManager.get().getIntoStandbuStandbyTime();
                            LogUtils.i(TAG,"表情结束的时间=====" + dTime);
                            if(dTime < AppConstants.FIVE_MIN){
                                boolean isLowpower = UbtBatteryManager.getInstance().isLowPower();
                                if(isLowpower){ //低电量只执行表情
                                    excuteRandomExpress();
                                }else {
                                    excuteSmallAction();
                                }
                            }else {
                                InfraRedManager.get().stopObjectDetection();
                                SysStatusManager.getInstance().publishSysActiveStatus(SysMasterEvent.ActivieStatusType.SITDOWN_STANDBY);
                            }
                        }
                    }
                }

                @Override
                public void onAnimationRepeat(int loopNumber) {
                }
            });
        }
    }


    /**
     * 获取随机表情
     */
    private List<String> getRandomExpress(){
        List<String> randomExpresss = Collections.synchronizedList(new ArrayList<String>());
        int random = random3To10();
        for (int i=0; i< random; i++){
            int index = (int) (Math.random() * expresss.length);
            randomExpresss.add(expresss[index]);
        }
        return randomExpresss;
    }

    /**
     * 执行小动作
     */
    private Scene mScene = null;
    public void excuteSmallAction(){
        try {
            mScene  = SceneUtils.loadScene("w_stand_001");
            mScene.display(Priority.NORMAL,new SceneListener() {
                @Override
                public void onCompleted() {
                    LogUtils.i(TAG,"随机动作结束=====");
                    if(!UbtBatteryManager.getInstance().isLowPower()){//判断一下低电，怕低电的表现昨晚之后，有重新站起来。
                        resetNotIsHead();
                    }
                    mScene = null;
                    if(!isStop){
                        long currentTime = System.currentTimeMillis();
                        long dTime = currentTime - SysStatusHelpManager.get().getIntoStandbuStandbyTime();
                        LogUtils.i(TAG,"动作结束的时间========" + dTime);
                        if(dTime < AppConstants.FIVE_MIN){
                            excuteRandomExpress();
                        }else {
                            InfraRedManager.get().stopObjectDetection();
                            SysStatusManager.getInstance().publishSysActiveStatus(SysMasterEvent.ActivieStatusType.SITDOWN_STANDBY);
                        }
                    }
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止随机小动作和随机表情
     */
    private boolean isStop = false;
    public void stopSmallAction(){
        LogUtils.i(TAG,"停止随机小动作和表情====");
        isStop = true;
        InfraRedManager.get().stopObjectDetection();
        if(mRandomExpresss != null){
            mRandomExpresss.clear();
            ExpressApi.get().doExpress("normal_1",1,Priority.HIGH);
        }

        if(mScene != null){
            mScene.close();
            mScene = null;
            LogUtils.i(TAG,"停止小动作复位机器人======");
        }
    }

    private void resetNotIsHead(){
        StandUpApi.getInstance().resetIsNotHead(new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {
            }

            @Override
            public void onFailure(Request request, CallException e) {
            }
        });
    }

    /**
     * 取3到10之间的随机整数。
     */
    private int random3To10(){
        Random random = new Random();
        int randNumber = random.nextInt(16 - 8 + 1) + 8;
        return randNumber;
    }
}
