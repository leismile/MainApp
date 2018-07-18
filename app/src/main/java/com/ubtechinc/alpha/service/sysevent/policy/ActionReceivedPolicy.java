package com.ubtechinc.alpha.service.sysevent.policy;

/**
 * @author : kevin.liu@ubtrobot.com
 * @description :
 * @date : 2018/4/23
 * @modifier :
 * @modify time :
 */
public enum ActionReceivedPolicy {
    ONE_SHOT(0), ALL(2);

    private int mValue;

    ActionReceivedPolicy(int value) {
        this.mValue = value;
    }
}
