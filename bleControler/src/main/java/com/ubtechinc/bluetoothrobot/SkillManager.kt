package com.ubtechinc.bluetoothrobot

import com.ubtech.utilcode.utils.LogUtils
import com.ubtech.utilcode.utils.notification.NotificationCenter
import com.ubtrobot.master.Master
import com.ubtrobot.master.interactor.MasterInteractor
import com.ubtrobot.master.skill.SkillInfo
import com.ubtrobot.master.skill.SkillStopCause
import com.ubtrobot.master.skill.SkillsProxy
import com.ubtrobot.transport.message.ResponseCallback

/**
 * @Deseription Skill管理类
 * @Author tanghongyu
 * @Time 2018/6/28 18:06
 */

class SkillManager private constructor() {
    internal val skillsProxy: SkillsProxy
    private val mCodemaoConnectionSkillName = "codemao_connection"
    private val mCodemaoRunningSkillName = "codemao_running"
    private val skillLifecycleCallbacks = object : MasterInteractor.SkillLifecycleCallbacks {
        override fun onSkillStarted(skillInfo: SkillInfo) {
            LogUtils.i("onSkillStarted name = " + skillInfo.name)
            when (skillInfo.name) {
                mCodemaoConnectionSkillName ->

                    NotificationCenter.defaultCenter().publish(SkillEvent(SkillEvent.SKILL.CODE_MAO_CONNECTION_START))

                mCodemaoRunningSkillName ->
                    NotificationCenter.defaultCenter().publish(SkillEvent(SkillEvent.SKILL.CODE_MAO_RUNNING_START))

                "blenetwork" -> {
                    NotificationCenter.defaultCenter().publish(SkillEvent(SkillEvent.SKILL.BLE_NETWORK_START))
                }
            }

        }

        override fun onSkillStopped(skillInfo: SkillInfo, skillStopCause: SkillStopCause) {
            LogUtils.i("onSkillStopped name = " + skillInfo.name)
            when (skillInfo.name) {
                mCodemaoConnectionSkillName -> {

                    NotificationCenter.defaultCenter().publish(SkillEvent(SkillEvent.SKILL.CODE_MAO_CONNECTION_STOP))
                }
                mCodemaoRunningSkillName ->
                    NotificationCenter.defaultCenter().publish(SkillEvent(SkillEvent.SKILL.CODE_MAO_RUNNING_STOP))

                "blenetwork" -> {
                    NotificationCenter.defaultCenter().publish(SkillEvent(SkillEvent.SKILL.BLE_NETWORK_STOP))
                }
            }
        }
    }

    init {

        val packageName = com.ubtech.utilcode.utils.Utils.getContext().packageName
        val interactor = Master.get().getOrCreateInteractor("robot:$packageName")
        this.skillsProxy = interactor.createSkillsProxy()
        interactor.registerSkillLifecycleCallbacks(skillLifecycleCallbacks)
    }

    fun startCodeMaoConnectionSkill(callback: ResponseCallback) {
        skillsProxy.call("/codemao/connect", null, callback)

    }

    fun stopCodeMaoConnectionSkill(callback: ResponseCallback) {
        skillsProxy.call("/codemao/disconnect", null, callback)
    }

    fun startCodeMaoRunningSkill(callback: ResponseCallback) {
        skillsProxy.call("/codemao/startRuning", null, callback)

    }

    fun stopCodeMaoRunningSkill(callback: ResponseCallback) {
        skillsProxy.call("/codemao/stopRuning", null, callback)
    }

    fun startNetworkSkill(callback: ResponseCallback) {
        skillsProxy.call("/bluetooth/network", null, callback)
    }

    fun exitNetworkSkill(callback: ResponseCallback) {
        skillsProxy.call("/bluetooth/exit", null, callback)
    }

    companion object {
        val instance: SkillManager by lazy { SkillManager() }
    }
}
