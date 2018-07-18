package com.ubtechinc.bluetoothrobot.handler


import com.google.protobuf.BoolValue
import com.google.protobuf.Int32Value
import com.ubtech.utilcode.utils.LogUtils
import com.ubtechinc.bluetoothrobot.CodeMaoService
import com.ubtechinc.bluetoothrobot.Robot2PhoneMsgMgr
import com.ubtechinc.bluetoothrobot.Statics
import com.ubtechinc.codemao.CodeMaoObserveButteryStatus
import com.ubtechinc.codemao.CodeMaoObserveFallClimb
import com.ubtechinc.codemao.CodeMaoObserveHeadRacket
import com.ubtechinc.protocollibrary.communite.IMsgHandler
import com.ubtechinc.protocollibrary.communite.ProtoBufferDispose
import com.ubtechinc.protocollibrary.protocol.MiniMessage
import com.ubtrobot.master.param.ProtoParam
import com.ubtrobot.transport.message.CallException
import com.ubtrobot.transport.message.Request
import com.ubtrobot.transport.message.Response
import com.ubtrobot.transport.message.StickyResponseCallback


/**
 * @Description 注册拍头监听
 * @Author tanghongyu
 * @Time  2018/4/10 17:17
 */
class ObserveHeadRacketHandler : IMsgHandler {
    internal var responseCmdId: Short = 0
    override fun handleMsg(requestCmdId: Short, responseCmdId: Short, request: MiniMessage, peer: Object) {
        this.responseCmdId = responseCmdId

        val observeHeadRacketRequest = ProtoBufferDispose.unPackData(
                CodeMaoObserveHeadRacket.ObserveHeadRacketRequest::class.java, request.dataContent) as CodeMaoObserveHeadRacket.ObserveHeadRacketRequest
        LogUtils.d("getIsSubscribe = " + observeHeadRacketRequest.isSubscribe)
        val  observeHeadRacketResponse = CodeMaoObserveHeadRacket.ObserveHeadRacketResponse.newBuilder()
        CodeMaoService.instance.observeHeadRacket( ProtoParam.create(BoolValue.newBuilder().setValue(observeHeadRacketRequest.isSubscribe).build()),object : StickyResponseCallback {
            override fun onFailure(p0: Request?, p1: CallException?) {
            }

            override fun onResponseCompletely(p0: Request?, response: Response?) {

            }

            override fun onResponseStickily(p0: Request?, response: Response?) {
                try {
                    var type = ProtoParam.from(response!!.param,  Int32Value::class.java).protoMessage
                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, 0, observeHeadRacketResponse.setType(type.value).build() , peer)
                } catch (e: ProtoParam.InvalidProtoParamException) {
                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, 0, observeHeadRacketResponse.build(), peer)
                    e.printStackTrace()
                }
            }

        })

    }


}
