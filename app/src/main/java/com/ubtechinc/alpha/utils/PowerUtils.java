package com.ubtechinc.alpha.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.appmanager.UbtBatteryManager;
import com.ubtechinc.contact.ConverseService;
import com.ubtechinc.contact.RingService;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.express.listeners.AnimationListener;
import com.ubtrobot.masterevent.protos.SysMasterEvent;

/**
 * @author : kevin.liu@ubtrobot.com
 * @description :
 * @date : 2018/4/16
 * @modifier :
 * @modify time :
 */
public class PowerUtils {

    private static PowerUtils DEFAULT = new PowerUtils();

    private static final int EXPRESS_ON_ANIMATION = 0x01;
    private static final int EXPRESS_ANIMATION_FINISH = 0x02;
    private static final int EXPRESS_IDLE = 0x03;

    private static int EXPRESS_STATE = EXPRESS_IDLE;
    private final Handler handler;

    private PowerUtils() {
        handler = new Handler(Looper.getMainLooper());
    }

    public static PowerUtils get() {
        return DEFAULT;
    }

    /**
     * 根据是否充电来做充电动画表情或者电量表情
     */
    public synchronized void show() {
        if (EyesControlUtils.isEyesOpen()) {
            if (!isOnAnimating()) {
                int progress = getProgress();
                if (SystemUtils.isCharging(AlphaApplication.getContext())) {
                    //显示充电动画
                    showChargingExpress(progress);

                } else {
                    if (isAnimationFinish()) {
                        backToNormalDelayed(2000l);
                    } else {
                        //显示当前电量
                        showCurrentBatteryExpress(progress);
                    }

                }
            }
        }else {
            backToNormal();
        }
    }

    public synchronized boolean isOnAnimating() {
        return EXPRESS_STATE == EXPRESS_ON_ANIMATION;
    }

    public synchronized boolean isAnimationFinish() {
        return EXPRESS_STATE == EXPRESS_ANIMATION_FINISH;
    }

    public synchronized boolean isIdle() {
        return EXPRESS_STATE == EXPRESS_IDLE || isAnimationFinish();
    }

    /**
     * 获取当前的电量值
     *
     * @return
     */
    public synchronized int getProgress() {
        SysMasterEvent.BatteryStatusData batteryStatusData = UbtBatteryManager.getInstance().getBatteryInfo();
        return batteryStatusData.getLevel();
    }

    /**
     * 显示当前电量表情
     *
     * @param progress
     */
    public synchronized void showCurrentBatteryExpress(int progress) {
        if (isIdle()) {
            Log.i("Power", "showCurrentBatteryExpress");

            removeCallbacks();

            ExpressApi.get().doProgressExpress(0, progress, false, Priority.HIGH, null);

            EXPRESS_STATE = EXPRESS_ANIMATION_FINISH;
            backToNormalDelayed(2000l);
        } else {
            backToNormalDelayed(200l);
        }
    }



    /**
     * 显示充电动画表情，动画结束后刷回正常表情
     *
     * @param progress
     */
    public synchronized void showChargingExpress(int progress) {
        if (isIdle()) {
            removeCallbacks();
            Log.i("Power", "showChargingExpress");
            ExpressApi.get().doProgressExpress(1, progress, true, Priority.MAXHIGH, new AnimationListener() {
                @Override
                public void onAnimationStart() {
                    Log.i("Power", "showChargingExpress onAnimationStart");
                    EXPRESS_STATE = EXPRESS_ON_ANIMATION;
                }

                @Override
                public void onAnimationEnd() {
                    Log.i("Power", "showChargingExpress onAnimationEnd");
                    EXPRESS_STATE = EXPRESS_ANIMATION_FINISH;
                    backToNormalDelayed(1000l);
                }

                @Override
                public void onAnimationRepeat(int i) {

                }
            });
        } else {
            backToNormalDelayed(200l);
        }
    }

    /**
     * 刷回正常表情
     */
    public synchronized void backToNormal() {
        Log.i("Power", "backToNormal");
        // TODO 在铃声和通话skill时不响应
        if(ConverseService.skillStart || RingService.skillStart) {
            Log.i("Power", "ConverseService.skillStart: " + ConverseService.skillStart + " RingService.skillStart :" + RingService.skillStart);
            return ;
        }

        ExpressApi.get().doExpress("normal_1", 1, Priority.HIGH, new AnimationListener() {
            @Override
            public void onAnimationStart() {
                // EXPRESS_STATE = EXPRESS_ON_ANIMATION;

            }

            @Override
            public void onAnimationEnd() {
                EXPRESS_STATE = EXPRESS_ANIMATION_FINISH;
                EXPRESS_STATE = EXPRESS_IDLE;
            }

            @Override
            public void onAnimationRepeat(int loopNumber) {

            }
        });

    }

    Runnable delayToNormalRunnable;

    private void backToNormalDelayed(long delayMillis) {
        removeCallbacks();

        delayToNormalRunnable = new Runnable() {
            @Override
            public void run() {
                backToNormal();
            }
        };
        handler.postDelayed(delayToNormalRunnable, delayMillis);
    }

    private void removeCallbacks() {
        if (delayToNormalRunnable != null)
            handler.removeCallbacks(delayToNormalRunnable);
    }

}
