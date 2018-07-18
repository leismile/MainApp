package com.ubtechinc.alpha.appmanager;

import android.support.annotation.NonNull;
import android.util.Log;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtrobot.action.ActionApi;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.masterevent.protos.RobotGestures;
import com.ubtrobot.motion.protos.Motion;
import com.ubtrobot.motor.MotorApi;
import com.ubtrobot.motor.MotorServiceException;
import com.ubtrobot.sys.SysApi;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;

import java.util.ArrayList;
import java.util.List;

import ubtechinc.com.standupsdk.GetRobotGestureCallback;
import ubtechinc.com.standupsdk.StandUpApi;

/**
 * Created by lulin.wu on 2018/4/18.
 */

public class MotorManager {
    private static final String TAG = MotorManager.class.getSimpleName();
    public static final String SPLITS_LEFT_NAME = "006"; //左脚在前弓步站起动作文件
    public static final String SPLITS_RIGHT_NAME = "005";//右脚在前弓步站起动作文件
    public static List<Integer> letMotorId = new ArrayList<>();//腿部舵机id
    public static List<Integer> headerMotorIds = new ArrayList<>();//头部舵机Id
    public static List<Integer> waistMotorIds = new ArrayList<>();//腰部舵机id
    public static List<Integer> armMotorIds = new ArrayList<>();//手臂舵机id

    private static MotorManager instance;

    private MotorManager() {
        letMotorId.add(5);
        letMotorId.add(6);
        letMotorId.add(7);
        letMotorId.add(8);
        letMotorId.add(9);
        letMotorId.add(10);

        headerMotorIds.add(11);
        headerMotorIds.add(12);
        headerMotorIds.add(13);

        waistMotorIds.add(14);

        armMotorIds.add(1);
        armMotorIds.add(2);
        armMotorIds.add(3);
        armMotorIds.add(4);

    }

    public static MotorManager getInstance() {
        if (instance == null) {
            synchronized (MotorManager.class) {
                if (instance == null) {
                    instance = new MotorManager();
                }
            }
        }
        return instance;
    }

    /**
     * 开机时根据机器人状态设置舵机状态
     */
    public void powerOnMotorsStatus() {
        StandUpApi.getInstance().getRobotGestureSync(new GetRobotGestureCallback() {
            @Override
            protected void getRobotGesture(RobotGestures.GestureType gestureType) {
                Log.i(TAG, "开机时机器人姿态=========" + gestureType);
                if (gestureType == RobotGestures.GestureType.STAND) {
                    try {
                        MotorApi.get().reset(new ResponseListener<Boolean>() {
                            @Override
                            public void onResponseSuccess(Boolean aBoolean) {
                                Log.i(TAG, "机器人复位成功=======");
                                unlockMotorForBydy(armMotorIds);
                            }

                            @Override
                            public void onFailure(int i, @NonNull String s) {
                                Log.i(TAG, "机器人复位失败=======");
                            }
                        });
                    } catch (MotorServiceException e) {
                        e.printStackTrace();
                    }
                } else if (gestureType == RobotGestures.GestureType.SITDOWN
                        || gestureType == RobotGestures.GestureType.KNEELING
                        || gestureType == RobotGestures.GestureType.SPLITS_LEFT_1
                        || gestureType == RobotGestures.GestureType.SPLITS_RIGHT_2) {
                    resetHeaderMotors(new ResponseListener() {
                        @Override
                        public void onResponseSuccess(Object o) {
                        }

                        @Override
                        public void onFailure(int i, @NonNull String s) {
                        }
                    });
                } else if (gestureType == RobotGestures.GestureType.SPLITS_RIGHT) {
                    playAction(SPLITS_RIGHT_NAME);
                } else if (gestureType == RobotGestures.GestureType.SPLITS_LEFT) {
                    playAction(SPLITS_LEFT_NAME);
                } else {
                    try {
                        MotorApi.get().reset(new ResponseListener<Boolean>() {
                            @Override
                            public void onResponseSuccess(Boolean aBoolean) {
                                unlockMotorForBydy(armMotorIds);
                            }

                            @Override
                            public void onFailure(int i, @NonNull String s) {

                            }
                        });
                    } catch (MotorServiceException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Request request, CallException e) {
                Log.i(TAG, "开机获取机器人姿态失败======" + e.getCode() + ";;;===" + e.getMessage());
            }
        });
    }


    /**
     * 复位腰部舵机
     *
     * @param responseListener
     */
    public void resetWaistMotors(ResponseListener responseListener) {
        resetMotorForBody(waistMotorIds, responseListener);
    }

    /**
     * 复位手部舵机
     *
     * @param responseListener
     */
    public void resetArmMotors(ResponseListener responseListener) {
        resetMotorForBody(armMotorIds, responseListener);
    }

    /**
     * 复位头部舵机
     *
     * @param responseListener
     */
    public void resetHeaderMotors(ResponseListener responseListener) {
        resetMotorForBody(headerMotorIds, responseListener);
    }

    /**
     * 复位腿部舵机
     */
    public void resetLetMotors(ResponseListener responseListener) {
        resetMotorForBody(letMotorId, responseListener);
    }

    /**
     * 复位机器人身体某个部位的舵机
     */
    public void resetMotorForBody(List<Integer> motorIds, ResponseListener responseListener) {
        List<Motion.MotorArg> angleList = new ArrayList<Motion.MotorArg>();
        List<Motion.Motor> motors = MotorApi.get().getMotorList();
        Motion.Motor motor;
        if (motors != null) {
            for (int i = 0; i < motors.size(); i++) {
                motor = motors.get(i);
                int motorId = motor.getId();
                if (isIntoMotos(motorId, motorIds)) {
                    Motion.MotorArg.Builder builder = Motion.MotorArg.newBuilder();
                    Motion.MotorArg angle = builder.setId(motorId).setAngle(motor.getResetAngle()).setRunTime(1000).build();
                    angleList.add(angle);
                }
            }
            MotorApi.get().moveToAbsoluteAngle(angleList,Priority.MAXHIGH, responseListener);
        }
    }

    /**
     * 锁住头部舵机
     */
    public void lockHeaderMotors() {
        lockMotorsForBody(headerMotorIds);
    }

    /**
     * 锁住头部舵机
     */
    public void lockLetMotors() {
        lockMotorsForBody(letMotorId);
    }

    /**
     * 锁住手部舵机
     */
    public void lockArmMotors() {
        lockMotorsForBody(armMotorIds);
    }

    /**
     * 锁住腰部舵机
     */
    public void lockWaistMotors() {
        lockMotorsForBody(waistMotorIds);
    }

    /**
     * 锁住身体某个部位舵机
     */
    public void lockMotorsForBody(List<Integer> motorIds) {
        MotorApi.get().lockMotor(motorIds, Priority.NORMAL, new ResponseListener<Boolean>() {
            @Override
            public void onResponseSuccess(Boolean aBoolean) {
            }

            @Override
            public void onFailure(int i, @NonNull String s) {
            }
        });
    }

    public void unlockMotorForBydy(List<Integer> motorIds) {
        MotorApi.get().unlockMotor(motorIds, Priority.NORMAL, new ResponseListener<Boolean>() {
            @Override
            public void onResponseSuccess(Boolean aBoolean) {
            }

            @Override
            public void onFailure(int i, @NonNull String s) {
            }
        });
    }

    private boolean isIntoMotos(int motorId, List<Integer> motorIds) {
        boolean isInto = false;
        for (int i = 0; i < motorIds.size(); i++) {
            int id = motorIds.get(i);
            if (motorId == id) {
                return true;
            }
        }
        return isInto;
    }

    private void playAction(String actionName) {
        ActionApi.get().playAction(actionName, new ResponseListener<Void>() {
            @Override
            public void onResponseSuccess(Void aVoid) {
                unlockMotorForBydy(armMotorIds);
                unlockMotorForBydy(waistMotorIds);
            }

            @Override
            public void onFailure(int i, @NonNull String s) {
            }
        });
    }

    /**
     * 除头部舵机外所有舵机掉电
     */
    public void unlockMotorIsNotHeader() {
        unlockMotorForBydy(letMotorId);
        unlockMotorForBydy(waistMotorIds);
        unlockMotorForBydy(armMotorIds);
    }

    /**
     * 胸口板开关
     *
     * @param isOn
     */
    public void switchBoard(boolean isOn, ResponseListener responseListener) {
        if (isOn) {
            if (SysApi.get().isStarted()) {
                LogUtils.i(TAG, "胸口板已经开着了");
                responseListener.onResponseSuccess(null);
            } else {
                SysApi.get().startup(responseListener);
            }
        } else {
            LogUtils.i(TAG,"SysApi.get().isStarted()-====" + SysApi.get().isStarted());
            if (SysApi.get().isStarted()) {
                SysApi.get().shutdown();
            }
            if(responseListener != null){
                responseListener.onResponseSuccess(null);
            }
        }


    }

    /**
     * 根据姿态判断机器人是否能掉电
     *
     * @return
     */
    public boolean isUnlockAllMotorWithGesture() {
        RobotGestures.GestureType robotGestures = StandUpApi.getInstance().getRobotGesture();
        com.ubtech.utilcode.utils.LogUtils.i(TAG, "舵机掉电机器人姿态======" + robotGestures);
        if (robotGestures == RobotGestures.GestureType.SPLITS_RIGHT
                || robotGestures == RobotGestures.GestureType.SPLITS_LEFT
                || robotGestures == RobotGestures.GestureType.SQUATDOWN
                || robotGestures == RobotGestures.GestureType.SITDOWN
                || robotGestures == RobotGestures.GestureType.KNEELING
                || robotGestures == RobotGestures.GestureType.LYING
                || robotGestures == RobotGestures.GestureType.LYINGDOWN
                || robotGestures == RobotGestures.GestureType.SPLITS_LEFT_1
                || robotGestures == RobotGestures.GestureType.SPLITS_RIGHT_2) {
            return true;
        } else {
            return false;
        }
    }


}
