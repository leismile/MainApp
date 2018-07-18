package com.ubtechinc.bluetoothrobot.handler


import com.google.protobuf.Int32Value
import com.ubtech.utilcode.utils.LogUtils
import com.ubtechinc.bluetoothrobot.CodeMaoService
import com.ubtechinc.bluetoothrobot.Robot2PhoneMsgMgr
import com.ubtechinc.bluetoothrobot.Statics
import com.ubtechinc.codemao.CodeMaoTakePicture
import com.ubtechinc.protocollibrary.communite.IMsgHandler
import com.ubtechinc.protocollibrary.communite.ProtoBufferDispose
import com.ubtechinc.protocollibrary.protocol.MiniMessage
import com.ubtrobot.master.param.ProtoParam
import com.ubtrobot.transport.message.CallException
import com.ubtrobot.transport.message.Request
import com.ubtrobot.transport.message.Response
import com.ubtrobot.transport.message.ResponseCallback

/**
 * @Deseription 拍照
 * @Author tanghongyu
 * @Time 2018/3/14 20:43
 */

class TakePictureHandler : IMsgHandler {
    internal var responseCmdId: Short = 0

    override fun handleMsg(requestCmdId: Short, responseCmdId: Short, request: MiniMessage, peer: Object) {
        this.responseCmdId = responseCmdId
        val requestSerial = request.sendSerial

        val cmTakePictureRequest = ProtoBufferDispose.unPackData(
                CodeMaoTakePicture.TakePictureRequest::class.java, request.dataContent) as CodeMaoTakePicture.TakePictureRequest
        val cmTakePictureResponse = CodeMaoTakePicture.TakePictureResponse.newBuilder()
        LogUtils.d("type = " + cmTakePictureRequest.type)
        CodeMaoService.instance.takePicture(ProtoParam.create(Int32Value.newBuilder().setValue(cmTakePictureRequest.type).build()), object : ResponseCallback {
            override fun onFailure(p0: Request?, exception: CallException?) {
                exception?.let {
                    LogUtils.w("onFailure code = " + exception.code)
                    cmTakePictureResponse.code = exception.code
                }
                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial,cmTakePictureResponse.build(), peer)
            }

            override fun onResponse(p0: Request?, response: Response?) {

                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, cmTakePictureResponse.setIsSuccess(true).build(), peer)
            }

        })

    }


}
