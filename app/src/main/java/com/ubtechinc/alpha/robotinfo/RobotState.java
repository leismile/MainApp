package com.ubtechinc.alpha.robotinfo;

import android.text.TextUtils;

import com.ubtech.utilcode.utils.thread.ThreadPool;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.utils.SharedPreferenceUtil;
import com.ubtrobot.sys.SysApi;

/**
 * @desc : 机器人状态管理
 * @author: wzt
 * @time : 2017/5/18
 * @modifier:
 * @modify_time:
 */

public class RobotState {
    private static volatile RobotState sRobotState;
    /**
     * 机器人序列号
     **/
    private volatile String sid;
    /**
     * 电池电量
     **/
    private volatile int powerValue = -1;

    private RobotState() {
        init();
    }

    public static RobotState get() {
        if (sRobotState == null) {
            synchronized (RobotState.class) {
                if (sRobotState == null) {
                    sRobotState = new RobotState();
                }
            }
        }
        return sRobotState;
    }

    private void init() {
        sid = SysApi.get().readRobotSid();
    }


    public String getSid() {
        if (TextUtils.isEmpty(sid)) {
            sid = SysApi.get().readRobotSid();
            setSid(sid);
        }
        return sid;
    }

    private void setSid(String sid) {
        if (sid != null && !sid.equals(this.sid)) {
            this.sid = sid;
        }
    }

    public int getPowerValue() {
        return powerValue;
    }


}
