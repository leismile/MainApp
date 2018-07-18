package com.ubtechinc.protocollibrary.communite


import com.ubtechinc.protocollibrary.protocol.MiniMessage

/**
 * Created by Administrator on 2017/5/25.
 */

interface IMsgHandler {
    fun handleMsg(requestCmdId: Short, responseCmdId: Short, request: MiniMessage, peer: Object)
}
