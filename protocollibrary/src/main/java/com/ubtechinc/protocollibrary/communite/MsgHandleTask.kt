package com.ubtechinc.protocollibrary.communite

/**
 * @Description 消息处理线程
 * @Author tanghongyu
 * @Time  2018/7/11 15:35
 */
class MsgHandleTask(private val messageContent: ByteArray, private val from: Any //消息的发送方
) : Runnable {


    override fun run() {

        MessageHandleFactory.instance.getMessageHandle(from)?.handleMessage(messageContent, from)

    }


}
