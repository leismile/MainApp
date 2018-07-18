package com.ubtechinc.bluetoothrobot.handler


import com.google.protobuf.Int32Value
import com.ubtech.utilcode.utils.LogUtils
import com.ubtechinc.bluetoothrobot.CodeMaoService
import com.ubtechinc.bluetoothrobot.Robot2PhoneMsgMgr
import com.ubtechinc.bluetoothrobot.Statics
import com.ubtechinc.codemao.CodeMaoFaceDetect
import com.ubtechinc.protocollibrary.communite.IMsgHandler
import com.ubtechinc.protocollibrary.communite.ProtoBufferDispose
import com.ubtechinc.protocollibrary.protocol.MiniMessage
import com.ubtrobot.master.param.ProtoParam
import com.ubtrobot.transport.message.CallException
import com.ubtrobot.transport.message.Request
import com.ubtrobot.transport.message.Response
import com.ubtrobot.transport.message.ResponseCallback

/**
 * @Description 识别人脸数量
 * @Author tanghongyu
 * @Time  2018/6/28 9:30
 */
class FaceDetectHandler : IMsgHandler {
    override fun handleMsg(requestCmdId: Short, responseCmdId: Short, request: MiniMessage, peer: Object) {

        val requestSerial = request.sendSerial

        val recogniseFaceCountRequest = ProtoBufferDispose.unPackData(
                CodeMaoFaceDetect.FaceDetectRequest::class.java, request.dataContent) as CodeMaoFaceDetect.FaceDetectRequest
        var builder = CodeMaoFaceDetect.FaceDetectResponse.newBuilder()
        CodeMaoService.instance.faceDetect(ProtoParam.create(Int32Value.newBuilder().setValue(recogniseFaceCountRequest.timeout).build()), object : ResponseCallback {
            override fun onFailure(p0: Request?, exception: CallException?) {
                exception?.let {
                    LogUtils.w("onFailure code = " + exception.code)
                    builder.resultCode = exception.code
                }
                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, builder.build(), peer)
            }

            override fun onResponse(p0: Request?, response: Response?) {
                try {

                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, ProtoParam.from(response!!.param, CodeMaoFaceDetect.FaceDetectResponse::class.java).protoMessage, peer)
                    LogUtils.i("onResponse FaceDetect ")
                } catch (e: ProtoParam.InvalidProtoParamException) {
                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, builder.build(), peer)
                    e.printStackTrace()
                }
            }

        })


    }
}
