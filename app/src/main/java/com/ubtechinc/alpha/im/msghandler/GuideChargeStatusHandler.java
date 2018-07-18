package com.ubtechinc.alpha.im.msghandler;

import com.ubtechinc.nets.im.modules.IMJsonMsg;


/**
*@data 创建时间：2018/4/25
*@author：bob.xu
*@Description:帮助指引中不需要做查询或业务逻辑的都放在此处实现
*@version
*/
public class GuideChargeStatusHandler implements IMJsonMsgHandler {

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, IMJsonMsg jsonRequest, String peer) {

    }
}
