package com.ubtechinc.protocollibrary.protocol

import com.ubtech.utilcode.utils.ConvertUtils
import java.util.*

/**
 * @Deseription
 * @Author tanghongyu
 * @Time 2018/5/10 10:24
 */
class PacketMessage {
    var cmdId : Short = 0
    var serialNo : Short = 0
    var dataContent : ByteArray = byteArrayOf()
    override fun toString(): String {
        return "PacketMessage(cmdId=$cmdId, serialNo=$serialNo, dataContent=${ConvertUtils.bytes2HexString(dataContent)})"
    }


}