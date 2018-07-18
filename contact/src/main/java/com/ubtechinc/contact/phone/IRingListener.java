package com.ubtechinc.contact.phone;

/**
 * @desc : 铃声回调
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/3/17
 */

public interface IRingListener {
    void onRingCompletely();
    void onEndRingCompletely();
}
