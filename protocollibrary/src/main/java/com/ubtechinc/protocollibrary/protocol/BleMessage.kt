package com.ubtechinc.protocollibrary.protocol

import com.ubtech.utilcode.utils.ConvertUtils


/**
 * @Deseription
 * @Author tanghongyu
 * @Time 2018/3/31 15:24
 */
class BleMessage constructor(miniMessage: MiniMessage){


    var cmdId: ByteArray = byteArrayOf()
    var version: Byte? = null
    var reqSer: ByteArray = byteArrayOf()
    var resSer: ByteArray = byteArrayOf()
    var content : ByteArray = byteArrayOf()
    init {
        cmdId = ConvertUtils.h_short2Byte(miniMessage.commandId)
        version = miniMessage.versionCode
        reqSer = ConvertUtils.h_int2Byte(miniMessage.sendSerial)
        resSer = ConvertUtils.h_int2Byte(miniMessage.responseSerial)
        content = miniMessage.dataContent
    }

}