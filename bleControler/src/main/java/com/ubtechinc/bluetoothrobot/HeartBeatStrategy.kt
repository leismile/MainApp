package com.clj.fastble.server

import android.os.Handler
import android.os.Message
import com.clj.fastble.IConnectionControl
import com.ubtech.utilcode.utils.LogUtils
import java.util.*

/**
 * @Deseription 心跳策略
 * @Author tanghongyu
 * @Time 2018/6/8 16:05
 */
class HeartBeatStrategy {

    lateinit var iBleControl: IConnectionControl
    private var isNeedHeartBeat: Boolean = true

    @Volatile
    private var failCount = 0

    companion object {
        val instance: HeartBeatStrategy by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            HeartBeatStrategy()
        }
    }

    fun init(iBleControl: IConnectionControl) {
        LogUtils.d("init")
        this.iBleControl = iBleControl
    }

    @Synchronized
    fun updateTimeCount() {
//        LogUtils.d("updateTimeCount")
//        failCount = 0
//        isNeedHeartBeat = false
    }

    lateinit var timer: Timer
    fun startHeartBeat() {
//        LogUtils.d("startHeartBeat")
//        timer = Timer()
//        timer.schedule(object : TimerTask() {
//            override fun run() {
//                handler.sendEmptyMessage(1)
//            }
//
//        }, 10000, 2000)
    }

    fun stopHeartBeat() {
        LogUtils.d("stopHeartBeat")
//        if (timer != null) {
//            timer.cancel()
//        }
//        failCount = 0
    }

//    var handler = object : Handler() {
//        override fun handleMessage(msg: Message?) {
//            super.handleMessage(msg)
//            if (msg!!.what == 1) {
//                if (failCount > 4) {
//                    LogUtils.e("failCount > 4")
//                    iBleControl.disconnect()
//                    failCount = 0
//                    stopHeartBeat()
//                }else{
//                    failCount++
//                }
//
//
//                LogUtils.i("failCount = $failCount")
//            }
//        }
//    }
}