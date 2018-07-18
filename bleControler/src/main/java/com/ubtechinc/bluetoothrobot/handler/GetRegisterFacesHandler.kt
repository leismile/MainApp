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

import java.util.ArrayList

/**
 * @Deseription 获取已注册的人脸列表
 * @Author tanghongyu
 * @Time 2018/3/14 20:43
 */
class GetRegisterFacesHandler : IMsgHandler {
    override fun handleMsg(requestCmdId: Short, responseCmdId: Short, request: MiniMessage, peer: Object) {
        val requestSerial = request.sendSerial

        val responseBuilder = CodeMaoGetRegisterFaces.GetRegisterFacesResponse.newBuilder()
        CodeMaoService.instance.getRegisterFaces( object : ResponseCallback {
            override fun onFailure(p0: Request?, exception: CallException?) {
                exception?.let {
                    LogUtils.w("onFailure code = " + exception.code)
                    responseBuilder.resultCode = exception.code
                }
                Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, responseBuilder.build(), peer)
            }

            override fun onResponse(p0: Request?, response: Response?) {
                try {

                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, ProtoParam.from(response!!.param, CodeMaoGetRegisterFaces.GetRegisterFacesResponse::class.java).protoMessage, peer)
                } catch (e: ProtoParam.InvalidProtoParamException) {
                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial, responseBuilder.build(), peer)
                    e.printStackTrace()
                }
            }

        })


    }
}
