package com.ubtechinc.bluetoothrobot.handler


import com.google.protobuf.StringValue
import com.ubtech.utilcode.utils.LogUtils
import com.ubtechinc.bluetoothrobot.CodeMaoService
import com.ubtechinc.bluetoothrobot.Robot2PhoneMsgMgr
import com.ubtechinc.bluetoothrobot.Statics
import com.ubtechinc.codemao.CodeMaoPlayAction
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

class PlayActionHandler : IMsgHandler {
    override fun handleMsg(requestCmdId: Short, responseCmdId: Short, request: MiniMessage, peer: Object) {

        val requestSerial = request.sendSerial
        val playActionRequest = ProtoBufferDispose.unPackData(
                CodeMaoPlayAction.PlayActionRequest::class.java, request.dataContent) as CodeMaoPlayAction.PlayActionRequest

        var playActionResponse = CodeMaoPlayAction.PlayActionResponse.newBuilder()
        CodeMaoService.instance.playAction(ProtoParam.create(StringValue.newBuilder().setValue(playActionRequest.actionName).build()), object: StickyResponseCallback {
            override fun onResponseCompletely(p0: Request?, p1: Response?) {
                playActionResponse.isSuccess = true
                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial,playActionResponse.build() , peer)
            }

            override fun onResponseStickily(p0: Request?, p1: Response?) {
            }

            override fun onFailure(request: Request?, exception: CallException?) {

                exception?.let {
                    LogUtils.w("onFailure code = " + exception.code)
                    playActionResponse.resultCode = exception.code
                }
                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, playActionResponse.build(), peer)

            }


        })
    }
}
