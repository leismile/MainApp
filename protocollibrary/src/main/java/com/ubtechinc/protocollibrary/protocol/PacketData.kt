package com.ubtechinc.protocollibrary.protocol

import com.ubtech.utilcode.utils.ConvertUtils
import com.ubtech.utilcode.utils.LogUtils
import java.nio.ByteBuffer

/**
 * @Deseription
 * @Author tanghongyu
 * @Time 2018/3/21 14:15
 */

class PacketData {

    //容量
    private var nCapacity: Int = 0
    //读取的位置
    private var nPosition: Int = 0
    private var buffer: ByteBuffer = ByteBuffer.allocate(nCapacity)
    private var cmdId : Short = 0
    private var isFirst : Boolean = true
    private var TAG : String = PacketData.javaClass.simpleName
    private fun allocate(len: Int): Boolean {
        if (nPosition + len > nCapacity) {
            val temp = buffer.array()

            nCapacity += len
            LogUtils.d(TAG, "allocate temp = " + ConvertUtils.bytes2HexString(temp) + "  tempSize = " + temp.size + " nCapacity = " + nCapacity + " dataLen = " + len + " position = " + nPosition)
            buffer = ByteBuffer.allocate(nCapacity)
            buffer.put(temp)
            buffer.position(nPosition)
        }
        return true
    }
    fun clear() {
        var index = 0
        while (index < buffer.limit()) {
            buffer.put(index, 0x00.toByte())
            index ++
        }
        nPosition = 0
        buffer.clear()
        isFirst = true
     }
    fun putBytes(  packetMessage : PacketMessage) {
        if (isFirst) {//第一次初始化
            this.cmdId = packetMessage.cmdId
            isFirst = false
        }else{
            if(cmdId != packetMessage.cmdId) {//中间有丢包，则把上次内容全部清空
                clear()
            }
        }
        allocate(packetMessage.dataContent.size)
        buffer.put(packetMessage.dataContent)
        nPosition += packetMessage.dataContent.size
    }

    fun getBuffer(): ByteArray {
        val temp = buffer.array()
        val buf = ByteArray(nPosition)
        System.arraycopy(temp, 0, buf, 0, nPosition)
        return buf
    }


    companion object {

        private var packetData: PacketData? = null
        fun get(): PacketData? {
            if (packetData == null) {
                synchronized(PacketData::class.java) {
                    packetData = PacketData()
                }
            }

            return packetData
        }
    }

}
