package com.ubtechinc.protocollibrary.communite

import com.ubtechinc.bluetoothlibrary.BleSender

/**
 * @Deseription
 * @Author tanghongyu
 * @Time 2018/7/11 15:01
 */
class ProtoMessageHandle : IMessageHandle {
    override fun handleMessage(data: ByteArray, from: Any) {
        var bleSender = from as BleSender
        IMsgHandleEngine.getInstance().handleProtoBuffMsg(data, bleSender)
    }
}