package com.ubtechinc.bluetoothrobot.handler


import com.google.protobuf.Int32Value
import com.ubtech.utilcode.utils.LogUtils
import com.ubtechinc.bluetoothrobot.CodeMaoService
import com.ubtechinc.bluetoothrobot.Robot2PhoneMsgMgr
import com.ubtechinc.bluetoothrobot.Statics
import com.ubtechinc.codemao.CodeMaoFaceRecognise
import com.ubtechinc.protocollibrary.communite.IMsgHandler
import com.ubtechinc.protocollibrary.communite.ProtoBufferDispose
import com.ubtechinc.protocollibrary.protocol.MiniMessage
import com.ubtrobot.master.param.ProtoParam
import com.ubtrobot.transport.message.CallException
import com.ubtrobot.transport.message.Request
import com.ubtrobot.transport.message.Response
import com.ubtrobot.transport.message.ResponseCallback

/**
 * @Deseription 人脸识别
 * @Author tanghongyu
 * @Time 2018/3/14 20:43
 */
class FaceRecogniseHandler : IMsgHandler {
    override fun handleMsg(requestCmdId: Short, responseCmdId: Short, request: MiniMessage, peer: Object) {

        val requestSerial = request.sendSerial

        val faceRecogniseRequest = ProtoBufferDispose.unPackData(
                CodeMaoFaceRecognise.FaceRecogniseRequest::class.java, request.dataContent) as CodeMaoFaceRecognise.FaceRecogniseRequest
        val responseBuilder = CodeMaoFaceRecognise.FaceRecogniseResponse.newBuilder()
        CodeMaoService.instance.faceRecognise(ProtoParam.create(Int32Value.newBuilder().setValue(faceRecogniseRequest.timeout).build()), object : ResponseCallback {
            override fun onFailure(p0: Request?, exception: CallException?) {
                exception?.let {
                    LogUtils.w("onFailure code = " + exception.code)
                    responseBuilder.resultCode = exception.code
                }
                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, responseBuilder.build(), peer)
            }

            override fun onResponse(p0: Request?, response: Response?) {
                try {

                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, ProtoParam.from(response!!.param,CodeMaoFaceRecognise.FaceRecogniseResponse::class.java).protoMessage, peer)
                } catch (e: ProtoParam.InvalidProtoParamException) {
                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, responseBuilder.build(), peer)
                    e.printStackTrace()
                }

            }

        })

    }
}
