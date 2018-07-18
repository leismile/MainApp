package com.ubtechinc.bluetoothrobot.handler


import com.ubtech.utilcode.utils.LogUtils
import com.ubtechinc.bluetoothrobot.CodeMaoService
import com.ubtechinc.bluetoothrobot.Robot2PhoneMsgMgr
import com.ubtechinc.bluetoothrobot.Statics
import com.ubtechinc.codemao.*
import com.ubtechinc.protocollibrary.communite.IMsgHandler
import com.ubtechinc.protocollibrary.protocol.MiniMessage
import com.ubtrobot.master.param.ProtoParam
import com.ubtrobot.transport.message.CallException
import com.ubtrobot.transport.message.Request
import com.ubtrobot.transport.message.Response
import com.ubtrobot.transport.message.ResponseCallback

/**
 * @Deseription 获取机器人表情列表
 * @Author tanghongyu
 * @Time 2018/3/14 20:43
 */

class GetExpressionListHandler : IMsgHandler {

    override fun handleMsg(requestCmdId: Short, responseCmdId: Short, request: MiniMessage, peer: Object) {


        val requestSerial = request.sendSerial


        CodeMaoService.instance.getExpressionList( object : ResponseCallback {
            override fun onFailure(p0: Request?, e: CallException?) {

                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, CodeMaoGetRobotExpression.GetRobotExpressionResponse.newBuilder().build(), peer)
            }

            override fun onResponse(p0: Request?, response: Response?) {
                try {

                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, ProtoParam.from(response!!.param, CodeMaoGetRobotExpression.GetRobotExpressionResponse::class.java).protoMessage, peer)
                } catch (e: ProtoParam.InvalidProtoParamException) {
                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, CodeMaoGetRobotExpression.GetRobotExpressionResponse.newBuilder().build(), peer)
                    e.printStackTrace()
                }
            }

        })
    }
}
