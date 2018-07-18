package com.ubtechinc.alpha.appmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Environment;
import android.util.Log;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtechinc.alpha.service.SkillHelper;
import com.ubtechinc.alpha.utils.AlphaUtils;
import com.ubtechinc.alpha.utils.PowerUtils;
import com.ubtechinc.nets.im.event.BatteryStateChange;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.express.listeners.AnimationListener;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.masterevent.protos.SysMasterEvent;
import com.ubtrobot.mini.libs.behaviors.Behavior;
import com.ubtrobot.mini.libs.behaviors.BehaviorInflater;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.speech.SpeechApi;
import com.ubtrobot.speech.listener.TTsListener;
import com.ubtrobot.transport.message.Param;

import java.io.FileNotFoundException;

import event.master.ubtrobot.com.sysmasterevent.event.SysActiveEvent;

/**
 * Created by lulin.wu on 2018/3/10.
 */

public class UbtBatteryManager {
    public static final int LOWPOWER_STATUS = 0;
    public static final int NOMRALPOWER_STATUS = 1;
    private static final String TAG = UbtBatteryManager.class.getSimpleName();
    private Context mContext;
    private int mLevelStatus = 0 ;
    private BatteryInfoBroadcastReceiver mBatteryInfoBroadcastReceiver;
    private BatteryStateChange mBatteryStateChange;
    private BatteryStateChange mCurrBatteryStateChange;
    private BatteryStateListener mBatteryStateListener;

    private UbtBatteryManager(){}
    private static class UbtBatteryManagerHolder {
        public static UbtBatteryManager instance = new UbtBatteryManager();
    }
    public static UbtBatteryManager getInstance(){
        return UbtBatteryManager.UbtBatteryManagerHolder.instance;
    }

    public void init(Context context){
        this.mContext = context;
        registerBatteryChangeReceive();
    }

    public void setBatteryStateListener(BatteryStateListener batteryStateListener){
        this.mBatteryStateListener = batteryStateListener;
    }
    /**
     * 监听电池变化广播
     */
    private void registerBatteryChangeReceive(){
        mBatteryInfoBroadcastReceiver = new BatteryInfoBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        Intent intent = mContext.registerReceiver(mBatteryInfoBroadcastReceiver,intentFilter);
        int status = intent.getIntExtra("status", 0);        //电池状态，充电、放电、充满、未充电
        int health = intent.getIntExtra("health", 0);   //电池的健康状态
        int level = intent.getIntExtra("level", 0);           //获取当前电量
        int plugged = intent.getIntExtra("plugged", 0);
        int voltage = intent.getIntExtra("voltage", 0);        // 电池伏数
        int temperature = intent.getIntExtra("temperature", 0); // 电池温度
        if(plugged == android.os.BatteryManager.BATTERY_PLUGGED_AC) {
            mLevelStatus = 1;
        }else {
            if(level <= 20){
                mLevelStatus = 0;
            }else {
                mLevelStatus = 1;
            }
        }
        mBatteryStateChange = new BatteryStateChange();
        mBatteryStateChange.setHealth(health);
        mBatteryStateChange.setStatu(status);
        mBatteryStateChange.setLevel(level);
        mBatteryStateChange.setPluggedp(plugged);
        mBatteryStateChange.setTemperature(temperature);
        mCurrBatteryStateChange = mBatteryStateChange;
    }

    public class BatteryInfoBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){
                int status = intent.getIntExtra("status", 0);        //电池状态，充电、放电、充满、未充电
                int health = intent.getIntExtra("health", 0);   //电池的健康状态
                boolean present = intent.getBooleanExtra("present", false);
                int level = intent.getIntExtra("level", 0);           //获取当前电量
                int scale = intent.getIntExtra("scale", 0);           //获取总电量　
                int icon_small = intent.getIntExtra("icon-small", 0);
                int plugged = intent.getIntExtra("plugged", 0);
                int voltage = intent.getIntExtra("voltage", 0);        // 电池伏数
                int temperature = intent.getIntExtra("temperature", 0); // 电池温度
                String technology = intent.getStringExtra("technology");
//                Log.i(TAG,"level=====" + level + " mLevel=====" + mBatteryStateChange.getLevel() +
//                        ";;pluggec=====" + plugged + ";;mPluggec=====" + mBatteryStateChange.getPluggedp() +
//                        ";;mLevelStatus=========" + mLevelStatus);
                setBatteryStateChange(status, health, level, plugged, temperature);
                if(plugged == android.os.BatteryManager.BATTERY_PLUGGED_AC) {
                    if(mBatteryStateChange.getPluggedp() != android.os.BatteryManager.BATTERY_PLUGGED_AC) {//插入适配器
                        AlphaUtils.ttsMessage("charge_001");
                        LowPowerShutdowmManager.getInstance().stopShutdownTimer();
                        //add 20180419 by kevin.liu
                        PowerUtils.get().show();
                        SysActiveEvent sysActiveEvent= SysStatusManager.getInstance().getmCurrentStatusData();
                        if(sysActiveEvent != null){
                            if(sysActiveEvent.getNewStatus() == SysMasterEvent.ActivieStatusType.STANDBY){
                                SysStatusManager.getInstance().startIntoStandupStandbyTimer();
                                SysStatusManager.getInstance().publishSysActiveStatus(SysMasterEvent.ActivieStatusType.ACTIVE);
                            }
                        }
                        if(mLevelStatus == LOWPOWER_STATUS){
                            mLevelStatus = NOMRALPOWER_STATUS;
                            mBatteryStateListener.onNormalPower(getBatteryStatsParam());
                        }
                    }
                }else {
                    if(mBatteryStateChange.getPluggedp() == android.os.BatteryManager.BATTERY_PLUGGED_AC) {//拔掉适配器
                        //add 20180419 by kevin.liu
                        PowerUtils.get().backToNormal();
                        SysActiveEvent sysActiveEvent= SysStatusManager.getInstance().getmCurrentStatusData();
                        if(sysActiveEvent != null){
                            if(sysActiveEvent.getNewStatus() == SysMasterEvent.ActivieStatusType.STANDBY){
                                SysStatusManager.getInstance().startIntoStandupStandbyTimer();
                                SysStatusManager.getInstance().publishSysActiveStatus(SysMasterEvent.ActivieStatusType.ACTIVE);
                            }
                        }
                        if(level <= 20){
                            if(mLevelStatus == NOMRALPOWER_STATUS){
                                mLevelStatus = LOWPOWER_STATUS;
                                mBatteryStateListener.onLowPower(getBatteryStatsParam());
                            }
                        }else {
                            if(level<90){
                                AlphaUtils.playBehavior("charge_0002",Priority.HIGH,null);
                            }
                        }
                    }else { //没插适配器的时候
                        if(level<=20){
                            if(mBatteryStateChange.getLevel() > 20){ //进入低电状态
                                if(mLevelStatus == NOMRALPOWER_STATUS){
                                    mLevelStatus = LOWPOWER_STATUS;
                                    mBatteryStateListener.onLowPower(getBatteryStatsParam());
                                }
                            }
                        }else {
                            if(mBatteryStateChange.getLevel() <= 20){ //有低电进入正常状态
                                if(mLevelStatus == LOWPOWER_STATUS){
                                    mLevelStatus = NOMRALPOWER_STATUS;
                                    mBatteryStateListener.onNormalPower(getBatteryStatsParam());
                                }
                            }
                        }
                    }
                }

                //电量低于10% 并且没有插适配器
                if(level <= 10 && plugged != android.os.BatteryManager.BATTERY_PLUGGED_AC){
                    LowPowerShutdowmManager.getInstance().startShutdownTimer();
                }
                mBatteryStateChange = mCurrBatteryStateChange;
                LogUtils.i(TAG,"status===" + status + ";health===" + health + ";present==" + present + ";level==" + level
                        + ";scale===" + scale + ";icon_small==" + icon_small + ";plugged===" + plugged + ";;voltage==" + voltage
                        + ";;temperature===" + temperature + ";;technology==" + technology);
            }
        }
    }



    private void setBatteryStateChange(int status, int health, int level, int plugged, int temperature) {
        BatteryStateChange event = new BatteryStateChange();
        event.setHealth(health);
        event.setStatu(status);
        event.setLevel(level);
        event.setPluggedp(plugged);
        event.setTemperature(temperature);
        mCurrBatteryStateChange = event;
    }

    /**
     * 取消电池广播监听
     */
    public void unregisterBatteryChangeReceive(){
        if(mBatteryInfoBroadcastReceiver != null){
            mContext.unregisterReceiver(mBatteryInfoBroadcastReceiver);
            mBatteryInfoBroadcastReceiver = null;
        }
    }

    /**
     * 获取电量信息
     * @return
     */
    public SysMasterEvent.BatteryStatusData getBatteryInfo(){
        SysMasterEvent.BatteryStatusData batteryStatusData = SysMasterEvent.BatteryStatusData.newBuilder()
                .setLevel(mCurrBatteryStateChange.getLevel())
                .setLevelStatus(mLevelStatus)
                .setStatus(mCurrBatteryStateChange.getPluggedp())
                .build();
        return  batteryStatusData;
    }

    public boolean isLowPower(){
        return mLevelStatus == 0;
    }

    /**
     * 判断机器人是否有插线
     * @return
     */
    public boolean isRobotAcOrUbs(){
        int pluggedp = mCurrBatteryStateChange.getPluggedp();
        LogUtils.i(TAG,"Battery======pluggedp====" + pluggedp);
        if(pluggedp == BatteryManager.BATTERY_PLUGGED_USB || pluggedp ==  BatteryManager.BATTERY_PLUGGED_AC){
            return true;
        }
        return false;

    }
    public Param getBatteryStatsParam(){
        Param param = ProtoParam.create(SysMasterEvent.BatteryStatusData.newBuilder()
                .setLevel(mCurrBatteryStateChange.getLevel())
                .setLevelStatus(mLevelStatus)
                .setStatus(mCurrBatteryStateChange.getPluggedp())
                .build());
        return param;
    }

    public interface BatteryStateListener {
        void onNormalPower(Param param);
        void onLowPower(Param param);
        void onPublishEvent(Param param);
    }




    //            switch (status) {
//                case UbtBatteryManager.BATTERY_STATUS_UNKNOWN:
//                    Log.i(TAG,"未知状态");
//                    break;
//                case UbtBatteryManager.BATTERY_STATUS_CHARGING:
//                    Log.i(TAG,"正在充电");
//                    break;
//                case UbtBatteryManager.BATTERY_STATUS_DISCHARGING:
//                    Log.i(TAG,"正在放电");
//                    break;
//                case UbtBatteryManager.BATTERY_STATUS_NOT_CHARGING:
//                    Log.i(TAG,"正在充电");
//                    break;
//                case UbtBatteryManager.BATTERY_STATUS_FULL:
//                    Log.i(TAG,"已经充满");
//                    break;
//            }
//
//            switch (intent.getIntExtra("status",UbtBatteryManager.BATTERY_STATUS_UNKNOWN)) {
//                {
//                    case UbtBatteryManager.BATTERY_STATUS_CHARGING:
//                        BatteryStatus = "充电状态";
//                        break;
//                    case UbtBatteryManager.BATTERY_STATUS_DISCHARGING:
//                        BatteryStatus = "放电状态";
//                        break;
//                    case UbtBatteryManager.BATTERY_STATUS_NOT_CHARGING:
//                        BatteryStatus = "未充电";
//                        break;
//                    case UbtBatteryManager.BATTERY_STATUS_FULL:
//                        BatteryStatus = "充满电";
//                        break;
//                    case UbtBatteryManager.BATTERY_STATUS_UNKNOWN:
//                        BatteryStatus = "未知道状态";
//                        break;
//                }
//
//                switch (intent.getIntExtra("plugged", UbtBatteryManager.BATTERY_PLUGGED_AC))
//                {
//                    case UbtBatteryManager.BATTERY_PLUGGED_AC:
//                        BatteryStatus2 = "AC充电";
//                        break;
//                    case UbtBatteryManager.BATTERY_PLUGGED_USB:
//                        BatteryStatus2 = "USB充电";
//                        break;
//                }
//

//
//                switch (intent.getIntExtra("health",
//                        UbtBatteryManager.BATTERY_HEALTH_UNKNOWN))
//                {
//                    case UbtBatteryManager.BATTERY_HEALTH_UNKNOWN:
//                        BatteryTemp = "未知错误";
//                        break;
//                    case UbtBatteryManager.BATTERY_HEALTH_GOOD:
//                        BatteryTemp = "状态良好";
//                        break;
//                    case UbtBatteryManager.BATTERY_HEALTH_DEAD:
//                        BatteryTemp = "电池没有电";
//                        break;
//                    case UbtBatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
//                        BatteryTemp = "电池电压过高";
//                        break;
//                    case UbtBatteryManager.BATTERY_HEALTH_OVERHEAT:
//                        BatteryTemp =  "电池过热";
//                        break;
//                }

}
