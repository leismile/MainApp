package com.ubtechinc.protocollibrary.communite.old

/**
 * @Deseription 兼容旧的绑定配网
 * @Author tanghongyu
 * @Time 2018/7/12 15:26
 */
class OldMsgHandleEngine private constructor(){
    private var iOldMsgDispather: IOldMsgDispather ?= null
    companion object {
        val instance: OldMsgHandleEngine by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            OldMsgHandleEngine()
        }
    }

    fun setOldMsgDispatcher(iOldMsgDispather: IOldMsgDispather) {
        this.iOldMsgDispather = iOldMsgDispather
    }

    fun handleOldJsonMsg(data: ByteArray, from : Any) {

        if(JsonCommandEncode.get().addData(data)) {
            iOldMsgDispather?.handleMsg(JsonCommandEncode.get().command, from)
        }
    }

}