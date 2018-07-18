package com.ubtechinc.contact;

/**
 * @desc : 通话记录改变接口
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/2/23
 */

public interface ICallRecordUpdate {
    void update();
    long getCallRecordVersionCode();
}
