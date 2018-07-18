package com.ubtechinc.bluetoothrobot.handler


import com.ubtechinc.bluetoothrobot.CodeMaoService
import com.ubtechinc.bluetoothrobot.Robot2PhoneMsgMgr
import com.ubtechinc.bluetoothrobot.Statics
import com.ubtechinc.codemao.CodeMaoControlFindFace
import com.ubtechinc.codemao.CodeMaoGetActionList
import com.ubtechinc.protocollibrary.communite.IMsgHandler
import com.ubtechinc.protocollibrary.communite.ProtoBufferDispose
import com.ubtechinc.protocollibrary.protocol.MiniMessage
import com.ubtrobot.master.param.ProtoParam
import com.ubtrobot.transport.message.*

/**
 * @Deseription 控制寻找人脸
 * @Author tanghongyu
 * @Time 2018/3/14 20:43
 */
class ControlFindFaceHandler : IMsgHandler {
    override fun handleMsg(requestCmdId: Short, responseCmdId: Short, request: MiniMessage, peer: Object) {

        val requestSerial = request.sendSerial
        val builder = CodeMaoControlFindFace.ControlFindFaceResponse.newBuilder()
        val controlFindFaceRequest = ProtoBufferDispose.unPackData(
                CodeMaoControlFindFace.ControlFindFaceRequest::class.java, request.dataContent) as CodeMaoControlFindFace.ControlFindFaceRequest

        CodeMaoService.instance.controlFindFace(ProtoParam.create(controlFindFaceRequest),object: StickyResponseCallback {
            override fun onResponseCompletely(p0: Request?, response: Response?) {
                try {

                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial,  ProtoParam.from(response?.param, CodeMaoControlFindFace.ControlFindFaceResponse::class.java).protoMessage, peer)
                } catch (e: ProtoParam.InvalidProtoParamException) {
                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, builder.build(), peer)
                    e.printStackTrace()
                }

            }

            override fun onResponseStickily(p0: Request?, response: Response?) {

                try {

                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, 0,  ProtoParam.from(response?.param, CodeMaoControlFindFace.ControlFindFaceResponse::class.java).protoMessage, peer)
                } catch (e: ProtoParam.InvalidProtoParamException) {
                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, 0, builder.build(), peer)
                    e.printStackTrace()
                }


            }

            override fun onFailure(request: Request?, response: CallException?) {

                builder.isSuccess = false
                response?.let {
                    builder.code = response.code
                }

                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial,builder.build(), peer)

            }


        })
    }
}
