package com.ubtechinc.alpha.appmanager;

import android.util.Log;

import com.ubt.alpha2.download.util.LogUtils;
import com.ubtechinc.alpha.CmCameraPrivacy;
import com.ubtechinc.alpha.utils.SystemPropertiesUtils;
import com.ubtrobot.ir.IrApi;
import com.ubtrobot.masterevent.protos.SysMasterEvent;

import java.util.Timer;
import java.util.TimerTask;

import event.master.ubtrobot.com.sysmasterevent.event.SysActiveEvent;

/**
 * Created by lulin.wu on 2018/3/28.
 */

public class InfraRedManager {
    private static final String TAG = "InfraRedManager";
    private static final int TEN_CENTIMETER = 100;

    private InfraRedManager() {
    }

    private static class InfraRedManagerHolder {
        public static InfraRedManager instance = new InfraRedManager();
    }

    public static InfraRedManager get() {
        return InfraRedManager.InfraRedManagerHolder.instance;
    }

    /**
     * 开始红外检测
     */
    private Timer mObjDetectionTimer;
    private TimerTask mObjDetectionTask;
    private int mDistance;
    private final byte[] mLock = new byte[0];
    private static boolean isAllowStartInfraRed = false;
    public void setAllowStartInfraRad(){
        CmCameraPrivacy.CameraPrivacyType type = SystemPropertiesUtils.getCameraPrivacyType();
        LogUtils.i(TAG,"人脸追踪开关=======" + type);
        if (type == CmCameraPrivacy.CameraPrivacyType.ON){
            isAllowStartInfraRed = true;
        }else {
            isAllowStartInfraRed = false;
            FaceDetectManager.getInstance().stopFaceDetect();
        }

    }
    public void startObjectDetection(final ObjectDetectionListener listener) {
        LogUtils.i(TAG,"isAllowStartInfraRed=======" + isAllowStartInfraRed);
        if(isAllowStartInfraRed){
            Log.i(TAG, "开启红外检测===========");
            mDistance = IrApi.get().distance();
            synchronized (mLock) {
                if (mObjDetectionTask == null) {
                    mObjDetectionTask = new TimerTask() {
                        @Override
                        public void run() {
                            SysActiveEvent sysActiveEvent = SysStatusManager.getInstance().getmCurrentStatusData();
                            if(sysActiveEvent.getNewStatus() == SysMasterEvent.ActivieStatusType.STANDUP_STANDBY
                                    || sysActiveEvent.getNewStatus() == SysMasterEvent.ActivieStatusType.SITDOWN_STANDBY){
                                int distance = IrApi.get().distance();
                                Log.i(TAG, "红外检测的距离==========" + distance);
                                if (distance == -1) {
                                    if (listener != null) {
                                        listener.onFail(distance, "红外获取数据失败");
                                    }
                                    return;
                                }
                                int dtDistance = Math.abs(distance - mDistance);
                                if (dtDistance > TEN_CENTIMETER) {
                                    if (listener != null) {
                                        listener.onObjectMove(dtDistance);
                                    }
                                } else {
                                    if (listener != null) {
                                        listener.onObjectStatic(dtDistance);
                                    }
                                }
                                mDistance = distance;
                            }else {
                                stopObjectDetection();
                            }
                        }
                    };
                }
                if (mObjDetectionTimer == null) {
                    mObjDetectionTimer = new Timer();
                }
                mObjDetectionTimer.schedule(mObjDetectionTask, 1500, 1500);
            }
        }
    }

    public void stopObjectDetection() {
        Log.i(TAG, "停止红外检测==============");
        synchronized (mLock) {
            if (mObjDetectionTimer != null) {
                mObjDetectionTimer.cancel();
                mObjDetectionTimer = null;
            }
            if (mObjDetectionTask != null) {
                mObjDetectionTask.cancel();
                mObjDetectionTask = null;
            }
        }
    }

    interface ObjectDetectionListener {
        void onObjectMove(int distance);

        void onObjectStatic(int distance);

        void onFail(int code, String message);
    }
}
