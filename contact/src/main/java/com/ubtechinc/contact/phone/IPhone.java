package com.ubtechinc.contact.phone;

/**
 * @desc : 电话接口
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/2/1
 */

public interface IPhone {
    void call(String phoneNumber);
    void call(String name, String phoneNumber);
}
