package com.ubtechinc.bluetoothrobot.handler


import com.google.protobuf.StringValue
import com.ubtech.utilcode.utils.LogUtils
import com.ubtechinc.bluetoothrobot.CodeMaoService
import com.ubtechinc.bluetoothrobot.Robot2PhoneMsgMgr
import com.ubtechinc.bluetoothrobot.Statics
import com.ubtechinc.codemao.CodeMaoGetRobotExpression
import com.ubtechinc.codemao.CodeMaoPlayExpression
import com.ubtechinc.protocollibrary.communite.IMsgHandler
import com.ubtechinc.protocollibrary.communite.ProtoBufferDispose
import com.ubtechinc.protocollibrary.protocol.MiniMessage
import com.ubtrobot.master.param.ProtoParam
import com.ubtrobot.transport.message.*

/**
 * @Deseription 播放表情
 * @Author tanghongyu
 * @Time 2018/3/14 20:43
 */

class DoExpressionHandler : IMsgHandler {
    override fun handleMsg(requestCmdId: Short, responseCmdId: Short, request: MiniMessage, peer: Object) {

        val requestSerial = request.sendSerial

        val playExpressCommandRequest = ProtoBufferDispose.unPackData(
                CodeMaoPlayExpression.PlayExpressionRequest::class.java, request.dataContent) as CodeMaoPlayExpression.PlayExpressionRequest
        val builder = CodeMaoPlayExpression.PlayExpressionResponse.newBuilder()
        LogUtils.d("DoExpression name = " + playExpressCommandRequest.expressName)
        CodeMaoService.instance.doExpression(ProtoParam.create(StringValue.newBuilder().setValue(playExpressCommandRequest.expressName).build()), object : StickyResponseCallback {
            override fun onResponseCompletely(p0: Request?, p1: Response?) {
                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, builder.setIsSuccess(true).build(), peer)
            }

            override fun onResponseStickily(p0: Request?, p1: Response?) {
            }

            override fun onFailure(p0: Request?, response: CallException?) {

                response?.let {
                    LogUtils.w("onFailure code = " + response.code)
                    builder.resultCode = response.code
                }
                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, builder.build(), peer)
            }


        })


    }
}
