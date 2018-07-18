package com.ubtechinc.contact;

import java.util.List;

/**
 * @desc : 来电查询
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/2/5
 */

public interface IContactQuery {
    String containsPhoneNumber(String phoneNumber);
    List<String> queryPhoneNumber(String name);
}
