package com.ubtechinc.bluetoothrobot.handler


import com.google.protobuf.BoolValue
import com.google.protobuf.Int32Value
import com.ubtech.utilcode.utils.LogUtils
import com.ubtechinc.bluetoothrobot.CodeMaoService
import com.ubtechinc.bluetoothrobot.Robot2PhoneMsgMgr
import com.ubtechinc.bluetoothrobot.Statics
import com.ubtechinc.codemao.CodeMaoControlFindFace
import com.ubtechinc.codemao.CodeMaoObserveVolumeKeyPress
import com.ubtechinc.protocollibrary.communite.IMsgHandler
import com.ubtechinc.protocollibrary.communite.ProtoBufferDispose
import com.ubtechinc.protocollibrary.protocol.MiniMessage
import com.ubtrobot.master.param.ProtoParam
import com.ubtrobot.transport.message.CallException
import com.ubtrobot.transport.message.Request
import com.ubtrobot.transport.message.Response
import com.ubtrobot.transport.message.StickyResponseCallback

/**
 * @Deseription 注册音量键点击回调
 * @Author tanghongyu
 * @Time 2018/3/14 20:43
 */

class ObserveVolumeKeyPressHandler : IMsgHandler {

    override fun handleMsg(requestCmdId: Short, responseCmdId: Short, request: MiniMessage, peer: Object) {


        val observeVolumeKeyPressRequest = ProtoBufferDispose.unPackData(
                CodeMaoObserveVolumeKeyPress.ObserveVolumeKeyPressRequest::class.java, request.dataContent) as CodeMaoObserveVolumeKeyPress.ObserveVolumeKeyPressRequest
                LogUtils.d("getIsSubscribe = " + observeVolumeKeyPressRequest.isSubscribe)
                val keyPressBuilder = CodeMaoObserveVolumeKeyPress.ObserveVolumeKeyPressResponse.newBuilder()
        CodeMaoService.instance.observeVolumeKeyPress(ProtoParam.create(BoolValue.newBuilder().setValue(observeVolumeKeyPressRequest.isSubscribe).build()), object : StickyResponseCallback {
            override fun onFailure(p0: Request?, p1: CallException?) {
            }

            override fun onResponseCompletely(p0: Request?, response: Response?) {



            }

            override fun onResponseStickily(p0: Request?, response: Response?) {
                try {
                    var type = ProtoParam.from(response!!.param, Int32Value::class.java).protoMessage.value
                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, 0, keyPressBuilder.setType(type).build() , peer)
                } catch (e: ProtoParam.InvalidProtoParamException) {
                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, 0, keyPressBuilder.build(), peer)
                    e.printStackTrace()
                }
            }

        })

    }

    companion object {
        private val TAG = "ObserveVolumeKeyPressHa"
    }


}
