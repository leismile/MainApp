package com.ubtechinc.bluetoothrobot.handler


import com.ubtech.utilcode.utils.LogUtils
import com.ubtechinc.bluetoothrobot.CodeMaoService
import com.ubtechinc.bluetoothrobot.Robot2PhoneMsgMgr
import com.ubtechinc.bluetoothrobot.Statics

import com.ubtechinc.codemao.CodeMaoSetMouthLamp
import com.ubtechinc.protocollibrary.communite.IMsgHandler
import com.ubtechinc.protocollibrary.communite.ProtoBufferDispose
import com.ubtechinc.protocollibrary.protocol.MiniMessage
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

class SetlMouthLampHandler : IMsgHandler {
    override fun handleMsg(requestCmdId: Short, responseCmdId: Short, request: MiniMessage, peer: Object) {
        val requestSerial = request.sendSerial

        val alSetMouthLampCommandRequest = ProtoBufferDispose.unPackData(
                CodeMaoSetMouthLamp.SetMouthLampRequest::class.java, request.dataContent) as CodeMaoSetMouthLamp.SetMouthLampRequest
        val reponseBuilder = CodeMaoSetMouthLamp.SetMouthLampResponse.newBuilder()
        CodeMaoService.instance.setMouthLamp(ProtoParam.create(alSetMouthLampCommandRequest), object : ResponseCallback {
            override fun onFailure(p0: Request?, exception: CallException?) {
                exception?.let {
                    LogUtils.w("onFailure code = " + exception.code)
                    reponseBuilder.resultCode = exception.code
                }
                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, reponseBuilder.build(), peer)
            }

            override fun onResponse(p0: Request?, response: Response?) {

                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, reponseBuilder.setIsSuccess(true).build(), peer)
            }

        })
    }
}
