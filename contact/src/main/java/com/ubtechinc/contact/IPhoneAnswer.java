package com.ubtechinc.contact;

/**
 * @desc : 电话接听和挂断接口
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/3/23
 */

public interface IPhoneAnswer {
    void onAnswer();
    void onDecline();
    void onDeclineComming();
}
