package com.ubtechinc.bluetoothrobot.handler


import com.google.protobuf.BoolValue
import com.google.protobuf.Int32Value
import com.ubtech.utilcode.utils.LogUtils
import com.ubtechinc.bluetoothrobot.CodeMaoService
import com.ubtechinc.bluetoothrobot.Robot2PhoneMsgMgr
import com.ubtechinc.bluetoothrobot.Statics
import com.ubtechinc.codemao.CodeMaoControlFindFace
import com.ubtechinc.codemao.CodeMaoObserveButteryStatus
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
 * @Description 注册电池监听
 * @Author tanghongyu
 * @Time  2018/4/10 17:17
 */
class ObserveBatteryStatusHandler : IMsgHandler {
    internal var responseCmdId: Short = 0
    override fun handleMsg(requestCmdId: Short, responseCmdId: Short, request: MiniMessage, peer: Object) {
        this.responseCmdId = responseCmdId


        val observeButteryStatusRequest = ProtoBufferDispose.unPackData(
                CodeMaoObserveButteryStatus.ObserveButteryStatusRequest::class.java, request.dataContent) as CodeMaoObserveButteryStatus.ObserveButteryStatusRequest
        LogUtils.d("getIsSubscribe = " + observeButteryStatusRequest.isSubscribe)

        CodeMaoService.instance.observeBatteryStatus( ProtoParam.create(BoolValue.newBuilder().setValue(observeButteryStatusRequest.isSubscribe).build()),object : StickyResponseCallback {
            override fun onFailure(p0: Request?, p1: CallException?) {
            }

            override fun onResponseCompletely(p0: Request?, response: Response?) {

            }

            override fun onResponseStickily(p0: Request?, response: Response?) {
                try {
                    var response = ProtoParam.from(response!!.param, CodeMaoObserveButteryStatus.ObserveButteryStatusResponse::class.java).protoMessage
                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, 0, response , peer)
                } catch (e: ProtoParam.InvalidProtoParamException) {
                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, 0, CodeMaoObserveButteryStatus.ObserveButteryStatusResponse.newBuilder().build(), peer)
                    e.printStackTrace()
                }
            }

        })
    }


}
