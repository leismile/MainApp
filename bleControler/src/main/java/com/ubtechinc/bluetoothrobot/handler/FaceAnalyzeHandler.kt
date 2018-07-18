package com.ubtechinc.bluetoothrobot.handler


import android.util.Log
import com.google.protobuf.Int32Value
import com.ubtech.utilcode.utils.LogUtils
import com.ubtechinc.bluetoothrobot.CodeMaoService
import com.ubtechinc.bluetoothrobot.Robot2PhoneMsgMgr
import com.ubtechinc.bluetoothrobot.Statics
import com.ubtechinc.codemao.CodeMaoFaceAnalyze
import com.ubtechinc.codemao.CodeMaoFaceDetect
import com.ubtechinc.codemao.CodeMaoFaceInfo
import com.ubtechinc.codemao.CodeMaoGetActionList
import com.ubtechinc.protocollibrary.communite.IMsgHandler
import com.ubtechinc.protocollibrary.communite.ProtoBufferDispose
import com.ubtechinc.protocollibrary.protocol.MiniMessage
import com.ubtrobot.master.param.ProtoParam
import com.ubtrobot.transport.message.CallException
import com.ubtrobot.transport.message.Request
import com.ubtrobot.transport.message.Response
import com.ubtrobot.transport.message.ResponseCallback

/**
 * @Deseription 识别人性别
 * @Author tanghongyu
 * @Time 2018/3/14 20:43
 */
class FaceAnalyzeHandler : IMsgHandler {
    override fun handleMsg(requestCmdId: Short, responseCmdId: Short, request: MiniMessage, peer: Object) {

        val requestSerial = request.sendSerial

        val faceAnalyzeRequest = ProtoBufferDispose.unPackData(
                CodeMaoFaceAnalyze.FaceAnalyzeRequest::class.java, request.dataContent) as CodeMaoFaceAnalyze.FaceAnalyzeRequest
        var builder = CodeMaoFaceAnalyze.FaceAnalyzeResponse.newBuilder()
        CodeMaoService.instance.faceAnalyze(ProtoParam.create(Int32Value.newBuilder().setValue(faceAnalyzeRequest.timeout).build()), object : ResponseCallback {
            override fun onFailure(p0: Request?, response: CallException?) {
                response?.let {
                    LogUtils.w("onFailure code = " + response.code)
                    builder.resultCode = response.code
                }
                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial,builder.build(), peer)
            }

            override fun onResponse(p0: Request?, response: Response?) {
                try {

                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, ProtoParam.from(response?.param, CodeMaoFaceAnalyze.FaceAnalyzeResponse::class.java).protoMessage, peer)
                    LogUtils.i("onResponse FaceAnalyze ")
                } catch (e: ProtoParam.InvalidProtoParamException) {
                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, builder.build(), peer)
                    e.printStackTrace()
                }
            }

        })
    }
}
