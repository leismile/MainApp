package com.ubtechinc.contact.util;

import android.util.Log;

/**
 * @desc :  条件执行器，主条件满足执行，次要条件1和次要条件2同时满足执行
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/3/15
 */

public class ConditionExecutor {
    private Runnable runnable;

    private static final String TAG = "ConditionExecutor";
    private volatile boolean conditionOne;
    private volatile boolean conditionTwo;
    private volatile boolean conditionThree;
    private volatile boolean isCompletely = false;

    public ConditionExecutor(Runnable runnable) {
        this.runnable = runnable;
    }

    public void reset() {
        isCompletely = false;
        conditionOne = false;
        conditionTwo = false;
        conditionThree = false;
    }

    public void mainConditionReach() {
        Log.d(TAG, "mainConditionReach");
        conditionOne = true;
        tryRun();
    }

    private void completely() {
        isCompletely = true;
    }

    public boolean isCompletely() {
        return isCompletely;
    }

    public void secondConditionOneReach() {
        Log.d(TAG, "secondConditionOneReach");
        conditionTwo = true;
        tryRun();
    }

    public void secondConidtionTwoReach() {
        Log.d(TAG, "secondConidtionTwoReach");
        conditionThree = true;
        tryRun();
    }

    public void tryRun() {
        Log.d(TAG, " tryRun isCompletely : " + isCompletely);
        if(!isCompletely) {
            if(conditionOne ||(conditionTwo && conditionThree)) {
                Log.d(TAG, "runnable.run()");
                runnable.run();
                completely();
            }
        }
    }

    /**
     * 强制执行
     */
    public void executor() {
        Log.d(TAG, " executor - isCompletely : " + isCompletely);
        if(!isCompletely) {
            runnable.run();
        }
    }

    /**
     * 标记完成
     */
    public void cancle() {
        isCompletely = true;
    }
}
