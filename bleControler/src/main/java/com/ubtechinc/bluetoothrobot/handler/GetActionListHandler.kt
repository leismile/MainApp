package com.ubtechinc.bluetoothrobot.handler


import com.ubtechinc.bluetoothrobot.CodeMaoService
import com.ubtechinc.bluetoothrobot.Robot2PhoneMsgMgr
import com.ubtechinc.bluetoothrobot.Statics
import com.ubtechinc.codemao.CodeMaoGetActionList
import com.ubtechinc.protocollibrary.communite.IMsgHandler
import com.ubtechinc.protocollibrary.protocol.MiniMessage
import com.ubtrobot.master.param.ProtoParam
import com.ubtrobot.transport.message.CallException
import com.ubtrobot.transport.message.Request
import com.ubtrobot.transport.message.Response
import com.ubtrobot.transport.message.ResponseCallback

/**
 * @Deseription 获取机器人列表
 * @Author tanghongyu
 * @Time 2018/6/25 20:43
 */

class GetActionListHandler : IMsgHandler {

    override fun handleMsg(requestCmdId: Short, responseCmdId: Short, request: MiniMessage, peer: Object) {


        val requestSerial = request.sendSerial
        CodeMaoService.instance.getActionList(object : ResponseCallback {
            override fun onResponse(request: Request, response: Response) {
                try {

                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, ProtoParam.from(response.param, CodeMaoGetActionList.GetActionListResponse::class.java).protoMessage, peer)
                } catch (e: ProtoParam.InvalidProtoParamException) {
                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, CodeMaoGetActionList.GetActionListResponse.newBuilder().build(), peer)
                    e.printStackTrace()
                }

            }

            override fun onFailure(request: Request, e: CallException) {

                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, CodeMaoGetActionList.GetActionListResponse.newBuilder().build(), peer)
            }
        })


    }
}
