package com.ubtechinc.bluetoothrobot.handler


import com.ubtechinc.bluetoothrobot.CodeMaoService
import com.ubtechinc.bluetoothrobot.Robot2PhoneMsgMgr
import com.ubtechinc.bluetoothrobot.Statics
import com.ubtechinc.codemao.CodeMaoStopAction
import com.ubtechinc.protocollibrary.communite.IMsgHandler
import com.ubtechinc.protocollibrary.protocol.MiniMessage
import com.ubtrobot.master.param.ProtoParam
import com.ubtrobot.transport.message.CallException
import com.ubtrobot.transport.message.Request
import com.ubtrobot.transport.message.Response
import com.ubtrobot.transport.message.ResponseCallback

/**
 * @Deseription 停止动作
 * @Author tanghongyu
 * @Time 2018/3/14 20:43
 */
class StopActionHandler : IMsgHandler {
    override fun handleMsg(requestCmdId: Short, responseCmdId: Short, request: MiniMessage, peer: Object) {

        val requestSerial = request.sendSerial
        val builder = CodeMaoStopAction.StopActionResponse.newBuilder()

        CodeMaoService.instance.stopAction(object: ResponseCallback {
            override fun onFailure(request: Request?, response: CallException?) {

                builder.isSuccess = false
                builder.resultCode = response!!.code
                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial,builder.build(), peer)

            }

            override fun onResponse(request: Request?, response: Response?) {
                builder.isSuccess = true
                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial,builder.build() , peer)
            }

        })
    }
}
