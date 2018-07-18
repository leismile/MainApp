package com.ubtechinc.contact.event;

/**
 * @desc : 真正播放铃声事件
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/4/20
 */

public class RealPlayRingEvent {
    private String phoneName;
    private boolean wakeupState = false;

    public RealPlayRingEvent(String phoneName) {
        this.phoneName = phoneName;
    }

    public String getPhoneName() {
        return phoneName;
    }
}
