package com.ubtechinc.protocollibrary.communite.old

import com.ubtechinc.protocollibrary.communite.IMessageHandle


/**
 * @Deseription 兼容老的绑定配网协议,无其他命令
 * @Author tanghongyu
 * @Time 2018/7/11 15:01
 */
class OldBleMessageHandle : IMessageHandle {

    override fun handleMessage(data: ByteArray, from: Any) {
        OldMsgHandleEngine.instance.handleOldJsonMsg(data, from)
    }
}