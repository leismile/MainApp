package com.ubtechinc.alpha.behavior;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ubtechinc.services.alphamini.BuildConfig;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.motion.protos.Motion;
import com.ubtrobot.motor.MotorApi;

import java.util.ArrayList;
import java.util.List;


/**
 * @desc : 机器人站起来
 * @author: wzt
 * @time : 2017/5/25
 * @modifier:
 * @modify_time:
 */

public class RobotStandup {
    private Context mContext;

    public RobotStandup(Context context) {
        mContext = context;
    }

    public void start() {
        List<Motion.MotorArg> angleList = new ArrayList<Motion.MotorArg>();
        if (BuildConfig.APPLICATION_ID.contains("alphamini")) {
            List<Motion.Motor> motors = MotorApi.get().getMotorList();
            Motion.Motor motor;
            if (motors != null) {
                for (int i = 0; i < motors.size(); i++) {
                    motor = motors.get(i);
                    Motion.MotorArg.Builder builder = Motion.MotorArg.newBuilder();
                    Motion.MotorArg angle = builder.setId(motor.getId()).setAngle(motor.getResetAngle()).setRunTime(1200).build();
                    angleList.add(angle);
                }
            }
        } else {
            int[] dataMsg = {120, 205, 120, 120, 35, 120, 120, 63, 145, 135, 120, 120, 177, 95, 105, 120, 120, 120, 250, 120};
            for (int i = 0; i < 20; i++) {
                Motion.MotorArg.Builder builder = Motion.MotorArg.newBuilder();
                Motion.MotorArg angle = builder.setId(i + 1).setAngle(dataMsg[i]).setRunTime(1200).build();
                angleList.add(angle);
            }
        }
        MotorApi.get().moveToAbsoluteAngle(angleList, new ResponseListener<Void>() {
            @Override
            public void onResponseSuccess(Void aVoid) {
            }

            @Override
            public void onFailure(int i, @NonNull String s) {
            }
        });
    }
}
