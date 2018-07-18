package com.ubtechinc.protocollibrary.communite

/**
 * @Deseription
 * @Author tanghongyu
 * @Time 2018/7/11 14:58
 */
interface IMessageHandle {

    fun handleMessage(data : ByteArray, from : Any)
}