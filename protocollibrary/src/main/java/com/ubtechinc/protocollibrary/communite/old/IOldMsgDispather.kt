package com.ubtechinc.protocollibrary.communite.old

/**
 * @Deseription
 * @Author tanghongyu
 * @Time 2018/7/12 15:29
 */
interface IOldMsgDispather {

    fun handleMsg(data: String, from: Any)
}