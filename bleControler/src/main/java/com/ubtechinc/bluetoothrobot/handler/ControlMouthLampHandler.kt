package com.ubtechinc.bluetoothrobot.handler


import com.google.protobuf.BoolValue
import com.google.protobuf.StringValue
import com.ubtechinc.bluetoothrobot.CodeMaoService
import com.ubtechinc.bluetoothrobot.Robot2PhoneMsgMgr
import com.ubtechinc.bluetoothrobot.Statics
import com.ubtechinc.codemao.CodeMaoControlMouthLamp
import com.ubtechinc.protocollibrary.communite.IMsgHandler
import com.ubtechinc.protocollibrary.communite.ProtoBufferDispose
import com.ubtechinc.protocollibrary.protocol.MiniMessage
import com.ubtrobot.commons.Priority
import com.ubtrobot.master.param.ProtoParam
import com.ubtrobot.transport.message.CallException
import com.ubtrobot.transport.message.Request
import com.ubtrobot.transport.message.Response
import com.ubtrobot.transport.message.ResponseCallback

/**
 * @Deseription 控制嘴巴灯
 * @Author tanghongyu
 * @Time 2018/3/14 20:43
 */

class ControlMouthLampHandler : IMsgHandler {
    override fun handleMsg(requestCmdId: Short, responseCmdId: Short, request: MiniMessage, peer: Object) {
        val requestSerial = request.sendSerial

        val alControlMouthLampCommandRequest = ProtoBufferDispose.unPackData(
                CodeMaoControlMouthLamp.ControlMouthRequest::class.java, request.dataContent) as CodeMaoControlMouthLamp.ControlMouthRequest
        val builder = CodeMaoControlMouthLamp.ControlMouthResponse.newBuilder()
        CodeMaoService.instance.controlMouthLamp(ProtoParam.create(BoolValue.newBuilder().setValue(alControlMouthLampCommandRequest.isOpen).build()), object : ResponseCallback {
            override fun onFailure(p0: Request?, response: CallException?) {
                response?.let {
                    builder.resultCode = response.code
                }
                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, builder.build(), peer)
            }

            override fun onResponse(p0: Request?, response: Response?) {

                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, builder.setIsSuccess(true).build(), peer)
            }

        })

    }
}
