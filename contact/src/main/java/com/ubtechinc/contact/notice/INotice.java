package com.ubtechinc.contact.notice;

/**
 * @desc : 通知接口
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/2/1
 */

public interface INotice {
    void notifyMiss(String caller);
    void notifyIntercept(String caller);
}
