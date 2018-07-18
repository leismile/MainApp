package com.ubtechinc.bluetoothrobot.old

import com.clj.fastble.server.HeartBeatStrategy
import com.ubtechinc.bluetoothlibrary.IDataEngine
import com.ubtechinc.bluetoothlibrary.UbtBluetoothConnManager
import com.ubtechinc.protocollibrary.protocol.OldDataEngine

/**
 * @Deseription 兼容老的配网绑定接口
 * @Author tanghongyu
 * @Time 2018/7/12 21:11
 */
class Robot2PhoneOldMsg private constructor(){
    private var iDataEngine : IDataEngine = OldDataEngine()

    companion object {
        val instance: Robot2PhoneOldMsg by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            Robot2PhoneOldMsg()
        }
    }

    fun sendData(data : ByteArray, from: Any) {
        UbtBluetoothConnManager.getInstance().sendBleData(iDataEngine.spliteData(data), from)
    }
}