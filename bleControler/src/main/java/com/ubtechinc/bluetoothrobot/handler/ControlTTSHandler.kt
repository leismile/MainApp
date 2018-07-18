package com.ubtechinc.bluetoothrobot.handler


import com.ubtech.utilcode.utils.LogUtils
import com.ubtechinc.bluetoothrobot.CodeMaoService
import com.ubtechinc.bluetoothrobot.Robot2PhoneMsgMgr
import com.ubtechinc.bluetoothrobot.Statics
import com.ubtechinc.codemao.CodeMaoControlTTS
import com.ubtechinc.protocollibrary.communite.IMsgHandler
import com.ubtechinc.protocollibrary.communite.ProtoBufferDispose
import com.ubtechinc.protocollibrary.protocol.MiniMessage
import com.ubtrobot.master.param.ProtoParam
import com.ubtrobot.transport.message.*

/**
 * @Deseription 播放TTS
 * @Author tanghongyu
 * @Time 2018/3/14 20:43
 */

class ControlTTSHandler : IMsgHandler {
    override fun handleMsg(requestCmdId: Short, responseCmdId: Short, request: MiniMessage, peer: Object) {

        val requestSerial = request.sendSerial
        val controlTTSRequest = ProtoBufferDispose.unPackData(
                CodeMaoControlTTS.ControlTTSRequest::class.java, request.dataContent) as CodeMaoControlTTS.ControlTTSRequest
        val responseBuilder = CodeMaoControlTTS.ControlTTSResponse.newBuilder()
        CodeMaoService.instance.controlTTS(ProtoParam.create(controlTTSRequest), object: StickyResponseCallback {
            override fun onResponseCompletely(p0: Request?, p1: Response?) {

                LogUtils.w("onResponseCompletely " )
                responseBuilder.isSuccess = true
                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial,responseBuilder.build() , peer)
            }

            override fun onResponseStickily(p0: Request?, p1: Response?) {
            }

            override fun onFailure(request: Request?, response: CallException?) {

                responseBuilder.isSuccess = false
                 response?.let {
                     responseBuilder.resultCode = response.code
                }
                LogUtils.w("onFailure code = " + response?.code)
                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, responseBuilder.build(), peer)

            }


        })

    }
}
