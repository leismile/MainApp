package com.ubtechinc.contact.phone;

/**
 * @desc : 铃声接口
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/3/17
 */

public interface IRing {
    /**
     *  播放来电铃声
     * @param name 来电人姓名
     */
    void playCommingRing(String name, boolean wakeupState);

    /**
     * 停止来电铃声
     */
    void stopCommingRing();

    /**
     * 播放挂断铃声
     */
    void playEndRing();

    /**
     * 停止挂断铃声
     */
    void stopEndRing();

    /**
     * 设置完成监听
     */
    void setRingListener(IRingListener ringListener);

    /**
     * 是否正在响铃中
     */
    boolean isOnRing();
}
