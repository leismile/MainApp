package com.ubtechinc.bluetoothrobot.handler


import com.ubtech.utilcode.utils.LogUtils
import com.ubtechinc.bluetoothrobot.CodeMaoService
import com.ubtechinc.bluetoothrobot.Robot2PhoneMsgMgr
import com.ubtechinc.bluetoothrobot.Statics
import com.ubtechinc.codemao.CodeMaoControlBehavior
import com.ubtechinc.protocollibrary.communite.IMsgHandler
import com.ubtechinc.protocollibrary.communite.ProtoBufferDispose
import com.ubtechinc.protocollibrary.protocol.MiniMessage
import com.ubtrobot.master.param.ProtoParam
import com.ubtrobot.transport.message.*

/**
 * @Deseription 播放动作
 * @Author tanghongyu
 * @Time 2018/3/14 20:43
 */

class ControlBehaviorHandler : IMsgHandler {
    override fun handleMsg(requestCmdId: Short, responseCmdId: Short, request: MiniMessage, peer: Object) {
        val requestSerial = request.sendSerial

        val cmControlBehaviorRequest = ProtoBufferDispose.unPackData(
                CodeMaoControlBehavior.ControlBehaviorRequest::class.java, request.dataContent) as CodeMaoControlBehavior.ControlBehaviorRequest
        val builder = CodeMaoControlBehavior.ControlBehaviorResponse.newBuilder()
        CodeMaoService.instance.controlBehavior(ProtoParam.create(cmControlBehaviorRequest), object : StickyResponseCallback {
            override fun onResponseCompletely(p0: Request?, p1: Response?) {

                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, builder.setIsSuccess(true).build(), peer)
            }

            override fun onResponseStickily(p0: Request?, p1: Response?) {
            }

            override fun onFailure(p0: Request?, response: CallException?) {
                LogUtils.w("onFailure code = " + response?.code)
                response?.let {
                    builder.resultCode = response.code
                }
                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, builder.build(), peer)
            }


        })
    }
}
