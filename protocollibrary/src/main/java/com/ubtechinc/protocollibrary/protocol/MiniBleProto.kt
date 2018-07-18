package com.ubtechinc.protocollibrary.protocol
import android.util.Log
import com.ubtech.utilcode.utils.ConvertUtils
import com.ubtechinc.protocollibrary.protocol.BleMessage
import com.ubtechinc.protocollibrary.protocol.MiniMessage
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * @Deseription
 * @Author tanghongyu
 * @Time 2018/3/29 16:38
 */
object MiniBleProto {


    /**
     * 字头(2B) + 协议版本号（1B） +长度(2B)  +参数(nB) + checksum(1B) + 结束符(1B)

    字头:0xFB+0xBF

    长度:字头+协议号+长度+ 参数 +checksum的长度.

    参数:n字节,不定长.

    Checksum:
    计算方式：Checksum(1Byte) =长度 + 参数的累加和

    结束符:0xED
     */
    private val pack_limit = 16
    private val pack_header = byteArrayOf(0xfb.toByte(), 0xbf.toByte())
    private val versionIndex = 2
    private val lenIndex = 3
    private val minLen = 7
    //编程猫
    private val codingNameFilter = "codemao"
    private val miniNameFilter = "mini"
    private val packEnd = 0xed.toByte()
    private val versionSize = 1
    private val lenSize = 2
    private val checkNumSize = 1
    private val defaultVersion = 0x01.toByte()
    fun pack(bleMessage: BleMessage): ByteArray {
        var len : Short = (pack_header.size + versionSize + lenSize + bleMessage.content.size  + checkNumSize).toShort()
        println("dataLen = " + len)
        var checksum = byteArrayOf(0x01.toByte())
//        var checksum = (data.reduce { acc, byte -> (acc.toInt() + byte.toInt()).toByte() }.toInt() + cmd.reduce { acc, byte -> (acc.toInt() + byte.toInt()).toByte() }.toInt()+ len).toByte()
        return pack_header + (bleMessage.version ?: defaultVersion) + ConvertUtils.h_short2Byte(len)  + bleMessage.content  + checksum + byteArrayOf(packEnd)
    }


    fun parsePacketMsg(data: ByteArray) : PacketMessage? {
        var cmdIdIndex = data.lastIndex - 3
        var serialNoIndex = data.lastIndex - 1
        var packetMsg : PacketMessage ?= null
        try {
            packetMsg = PacketMessage()
            packetMsg.cmdId = ConvertUtils.h_byte2Short(data.sliceArray(cmdIdIndex until  cmdIdIndex + 2))
            packetMsg.serialNo = ConvertUtils.h_byte2Short(data.sliceArray(serialNoIndex until  serialNoIndex + 2))
            packetMsg.dataContent = data.sliceArray(0 until cmdIdIndex)
            println(packetMsg)

        }catch (e : Exception) {
            e.printStackTrace()
        }
        return packetMsg

    }


    val TAG = "MiniBleProto"
    fun unpack(data: ByteArray): Pair<Int, Array<MiniMessage>>  {
        Log.i(TAG, "unpack content =  " + ConvertUtils.bytes2HexString(data))
        var idx = 0
        var res = emptyArray<MiniMessage>()
        while (idx + lenIndex < data.size) {

            if (data[idx] != pack_header[0] || data[idx + 1] != pack_header[1]) return Pair(-1, res)
            var len = ConvertUtils.h_byte2Short(byteArrayOf(data[idx + lenIndex], data[idx + lenIndex + 1]))
            println("data len = $len")
            if (data.size - idx < len + 1) {
                break
            } else {
                if (data[idx + len] != packEnd) return Pair(-1, res)//有头没有尾巴
                var msg = MiniMessage()
                msg.versionCode = data[idx + versionIndex]
                msg.dataContent = data.sliceArray((idx + lenIndex + 2) until (idx + len - 1))
                res += msg
                idx += 1 + len
            }
        }
        return Pair(idx, res)
    }

    /**
     * 分包
     * 内容（16B）+ 随机数(2B)+ 序列号（2B）
     */
    fun devide(data: ByteArray): List<ByteArray> {
        var randomValue = ConvertUtils.h_short2Byte(Random().nextInt(Short.MAX_VALUE.toInt()).toShort())

        var serialNo : Short = 1
        return if (data.size <= pack_limit) {
            listOf(data + randomValue + ConvertUtils.h_short2Byte(serialNo))
        } else {
            var res = emptyList<ByteArray>()
            val n = data.size / pack_limit
            (0 until n).forEach {

                res += data.sliceArray(pack_limit * it until (pack_limit * (it + 1))) + randomValue + (ConvertUtils.h_short2Byte(serialNo++))

            }
            if (data.size % pack_limit != 0) {
                res += data.sliceArray(n * pack_limit until data.size) + randomValue + (ConvertUtils.h_short2Byte(serialNo++))
            }
            res
        }
    }

    fun assemble(data: ByteArray, totalData: ByteArray): ByteArray {
        var finalData = totalData + data
        return finalData
    }

    fun checkDataFormat(data: ByteArray): Boolean {
        return data.size > minLen && data[0] == 0xfb.toByte() && data[1] == 0xbf.toByte() && data.last() == packEnd
    }

    fun isEnd(data: ByteArray): Boolean {
        return unpack(data).first != -1
    }

    fun getCodingNameFilter(): String {
        return codingNameFilter
    }

    fun getMiniNameFilter() : String {
        return miniNameFilter
    }

    fun getSplitLength() : Int {
        return pack_limit
    }


}