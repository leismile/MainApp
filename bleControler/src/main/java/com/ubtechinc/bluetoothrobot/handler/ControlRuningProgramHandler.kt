package com.ubtechinc.bluetoothrobot.handler


import android.bluetooth.BluetoothDevice
import android.text.TextUtils
import com.clj.fastble.server.HeartBeatStrategy
import com.ubtech.utilcode.utils.LogUtils
import com.ubtech.utilcode.utils.thread.HandlerUtils
import com.ubtechinc.bluetoothlibrary.BleSender
import com.ubtechinc.bluetoothlibrary.UbtBluetoothConnManager
import com.ubtechinc.bluetoothrobot.*
import com.ubtechinc.codemao.CodeMaoControlMouthLamp
import com.ubtechinc.codemao.CodeMaoControlRunningProgram
import com.ubtechinc.protocollibrary.communite.IMsgHandler
import com.ubtechinc.protocollibrary.communite.ProtoBufferDispose
import com.ubtechinc.protocollibrary.protocol.MiniMessage
import com.ubtrobot.master.call.MasterSubCode
import com.ubtrobot.master.skill.SkillOpponent
import com.ubtrobot.master.transport.message.CallGlobalCode
import com.ubtrobot.master.transport.message.parcel.ParcelableParam
import com.ubtrobot.transport.message.CallException
import com.ubtrobot.transport.message.Request
import com.ubtrobot.transport.message.Response
import com.ubtrobot.transport.message.ResponseCallback

/**
 * @Deseription 播放TTS
 * @Author tanghongyu
 * @Time 2018/3/14 20:43
 */

class ControlRuningProgramHandler : IMsgHandler {
    override fun handleMsg(requestCmdId: Short, responseCmdId: Short, request: MiniMessage, peer: Object) {

        val requestSerial = request.sendSerial
        val controlRuningProgramRequest = ProtoBufferDispose.unPackData(
                CodeMaoControlRunningProgram.ControlRunningProgramRequest::class.java, request.dataContent) as  CodeMaoControlRunningProgram.ControlRunningProgramRequest
        val responseBuilder = CodeMaoControlRunningProgram.ControlRunningProgramResponse.newBuilder()
        if(controlRuningProgramRequest.isStart) {
            SkillManager.instance.startCodeMaoRunningSkill(object : ResponseCallback {
                override fun onFailure(p0: Request?, e: CallException?) {
                    responseBuilder.setIsSuccess(false).build()
                    responseBuilder.resultCode = fordReasonHandler(e!!)
                    LogUtils.w("start CodeMaoRunningSkill fail code = " + responseBuilder.resultCode)
                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial,
                            responseBuilder.build(), peer)

                    HandlerUtils.runUITask({
                        try {
                            val (peer1) = peer as BleSender

                            UbtBluetoothConnManager.getInstance().cancelDevice(peer1 as BluetoothDevice)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, 2000)
                }

                override fun onResponse(p0: Request?, p1: Response?) {
                    LogUtils.i("start CodeMaoRunningSkill success  ")
                    HeartBeatStrategy.instance.startHeartBeat()
                    responseBuilder.setIsSuccess(true).build()
                    Robot2PhoneMsgMgr.get().sendResponseData(responseCmdId, Statics.PROTO_VERSION, requestSerial,
                            responseBuilder.build(), peer)


                }

            })
        }else{
            SkillManager.instance.stopCodeMaoRunningSkill(object : ResponseCallback {
                override fun onFailure(p0: Request?, p1: CallException?) {
                    LogUtils.w("start fail code = " + p1?.code)
                }

                override fun onResponse(p0: Request?, p1: Response?) {
                    LogUtils.i("stopCodeMaoRunningSkill success  ")
                }

            })
        }



    }


    private val ERROR_UNKNOWN_ERROR = 0//
    private val ERROR_CODE_LOW_POWER = 1//电量过低(悟空电量过低，无法执行编程动作)
    private val ERROR_ROBOT_AVATAR = 2//视频监控（ 提示：悟空正在视频监控，请稍后再试）
    private val ERROR_ROBOT_BUSY = 3//机器人忙碌（ 提示：其他人正使用悟空进行编程，请稍后再试）
    private val ERROR_ROBOT_PHONE_CALL = 4//通话中（ 提示：悟空正在通话中，请通话结束后再试）
    private val ERROR_ROBOT_HIGH_RISK_ACTION = 5//高危动作（ 提示：悟空忙碌中，请稍后再试）
    private val ERROR_ROBOT_FALL_CLIMB = 6//跌倒爬起（ 提示：悟空忙碌中，请稍后再试）
    private val ERROR_ROBOT_STAND_BY = 7//休眠状态（ 提示：悟空休息中，请唤醒后再试）
    fun fordReasonHandler(e: CallException): Int {
        var errorCode = ERROR_UNKNOWN_ERROR
        if (e.code == CallGlobalCode.FORBIDDEN && e.subCode == MasterSubCode.FORBIDDEN_TO_START_SKILL) {
            try {

                val skillOpponent = ParcelableParam.from(e.param, SkillOpponent::class.java).parcelable
                val skillList = skillOpponent.skillList
                val stateList = skillOpponent.serviceStateList
                val skillStateList = skillOpponent.skillStateList

                //优先判断状态冲突、再判断skill冲突
                var hasFoundReason = false
                if (stateList != null && stateList.size > 0) {
                    for (pair in stateList) {
                        if (pair != null && pair.second != null) {
                            if (pair.second == "lowPower") {//低电量
                                LogUtils.w("low power can not run codemao skill")
                                hasFoundReason = true
                                errorCode = ERROR_CODE_LOW_POWER
                            }
                        }
                    }
                }

                if (!hasFoundReason) {
                    if (skillList != null && skillList.size > 0) {
                        LogUtils.w("当前正在运行--" + skillList[0].name)
                        var skillName: String? = null
                        for (skillComponent in skillList) {
                            if (skillComponent != null && skillComponent.name != null) {
                                skillName = skillComponent.name
                                if (!TextUtils.isEmpty(skillName)) {
                                    break
                                }
                            }
                        }
                        when (skillName) {
                            "phone_call"//阿凡达
                            -> errorCode = ERROR_ROBOT_PHONE_CALL
                            "codemaoagent"//重复启动
                            -> errorCode = ERROR_ROBOT_BUSY
                            "avatar"//阿凡达
                            -> errorCode = ERROR_ROBOT_AVATAR

                            "speech_highrisk_actor"//阿凡达
                            -> errorCode = ERROR_ROBOT_HIGH_RISK_ACTION
                            "fallclimbagent"//阿凡达
                            -> errorCode = ERROR_ROBOT_FALL_CLIMB
                            "standby"//阿凡达
                            -> errorCode = ERROR_ROBOT_STAND_BY
                        }

                        hasFoundReason = true
                    }
                }

            } catch (e1: ParcelableParam.InvalidParcelableParamException) {
                e1.printStackTrace()
            }

        } else {
            errorCode = e.code
        }

        return errorCode
    }

}
