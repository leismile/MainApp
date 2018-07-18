package com.ubtechinc.protocollibrary.protocol

import com.ubtech.utilcode.utils.ConvertUtils


class MiniMessage {
    var commandId: Short = 0
    var versionCode: Byte? = null
    var sendSerial: Int = 0
    var responseSerial: Int = 0
    var dataContent : ByteArray = byteArrayOf()
    override fun toString(): String {
        return "MiniMessage(commandId=$commandId, versionCode=$versionCode, sendSerial=$sendSerial, responseSerial=$responseSerial, dataContent=${ConvertUtils.bytes2HexString(dataContent)})"
    }

}
