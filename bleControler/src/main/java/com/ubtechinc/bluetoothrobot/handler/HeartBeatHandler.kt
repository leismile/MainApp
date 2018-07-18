package com.ubtechinc.bluetoothrobot.handler


import com.ubtech.utilcode.utils.LogUtils
import com.ubtechinc.bluetoothrobot.Robot2PhoneMsgMgr
import com.ubtechinc.bluetoothrobot.Statics
import com.ubtechinc.protocollibrary.communite.IMsgHandler
import com.ubtechinc.protocollibrary.protocol.MiniMessage

/**
 * @Deseription 恢复原始状态
 * @Author tanghongyu
 * @Time 2018/3/14 20:43
 */
class HeartBeatHandler : IMsgHandler {
    override fun handleMsg(requestCmdId: Short, responseCmdId: Short, request: MiniMessage, peer: Object) {
        val requestSerial = request.sendSerial
        LogUtils.i("receive heartBeat")
        Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial,null , peer)
    }

}
