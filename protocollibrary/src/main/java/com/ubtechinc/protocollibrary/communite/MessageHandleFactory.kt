package com.ubtechinc.protocollibrary.communite

import com.ubtechinc.bluetoothlibrary.BleSender
import com.ubtechinc.protocollibrary.communite.old.OldBleMessageHandle

/**
 * @Deseription 消息处理工厂
 * @Author tanghongyu
 * @Time 2018/7/11 15:05
 */
class MessageHandleFactory private constructor(){

    companion object {
        val instance: MessageHandleFactory by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            MessageHandleFactory()
        }
    }

    fun getMessageHandle(from : Any): IMessageHandle? {
        return when(from) {
            is BleSender -> {
                if(from.bleVersion == 1) {
                    ProtoMessageHandle()
                }else{
                    OldBleMessageHandle()//兼容老的绑定配网
                }
            }
            else -> {
                null
            }
        }

    }
}