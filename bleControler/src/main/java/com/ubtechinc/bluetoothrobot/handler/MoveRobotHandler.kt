package com.ubtechinc.bluetoothrobot.handler


import com.ubtech.utilcode.utils.LogUtils
import com.ubtechinc.bluetoothrobot.CodeMaoService
import com.ubtechinc.bluetoothrobot.Robot2PhoneMsgMgr
import com.ubtechinc.bluetoothrobot.Statics
import com.ubtechinc.codemao.CodeMaoMoveRobot
import com.ubtechinc.codemao.CodeMaoStopAction
import com.ubtechinc.protocollibrary.communite.IMsgHandler
import com.ubtechinc.protocollibrary.communite.ProtoBufferDispose
import com.ubtechinc.protocollibrary.protocol.MiniMessage
import com.ubtrobot.master.param.ProtoParam
import com.ubtrobot.transport.message.*

/**
 * @Deseription 控制移动机器人
 * @Author tanghongyu
 * @Time 2018/3/14 20:43
 */
class MoveRobotHandler : IMsgHandler {
    internal var responseCmdId: Short = 0
    internal var requestSerial: Int = 0
    override fun handleMsg(requestCmdId: Short, responseCmdId: Short, request: MiniMessage, peer: Object) {
        this.responseCmdId = responseCmdId
        requestSerial = request.sendSerial
        val alMoveRobotRequest = ProtoBufferDispose.unPackData(
                CodeMaoMoveRobot.MoveRobotRequest::class.java, request.dataContent) as CodeMaoMoveRobot.MoveRobotRequest
        val responseBuilder = CodeMaoMoveRobot.MoveRobotResponse.newBuilder()
        CodeMaoService.instance.moveRobot(ProtoParam.create(alMoveRobotRequest), object: StickyResponseCallback {
            override fun onResponseStickily(p0: Request?, p1: Response?) {
            }

            override fun onResponseCompletely(p0: Request?, p1: Response?) {
                responseBuilder.isSuccess = true
                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION,  requestSerial, responseBuilder.build() , peer)
            }

            override fun onFailure(request: Request?, exception: CallException?) {
                exception?.let {
                    LogUtils.w("onFailure code = " + exception.code)
                    responseBuilder.code = exception.code
                }
                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, responseBuilder.build(), peer)

            }


        })

    }


}
