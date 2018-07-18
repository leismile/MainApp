package com.ubtechinc.alpha.im.msghandler;

import android.text.TextUtils;

import com.ubtechinc.alpha.service.SkillHelper;
import com.ubtechinc.nets.im.modules.IMJsonMsg;

import java.util.HashMap;


/**
*@data 创建时间：2018/4/25
*@author：bob.xu
*@Description:帮助指引中不需要做查询或业务逻辑的都放在此处实现
*@version
*/
public class GuideWhichWifiHandler implements IMJsonMsgHandler {

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, IMJsonMsg jsonRequest, String peer) {

    }
}
