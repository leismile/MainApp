package com.ubtechinc.bluetoothrobot

import com.ubtech.utilcode.utils.LogUtils
import com.ubtech.utilcode.utils.notification.NotificationCenter
import com.ubtech.utilcode.utils.notification.Subscriber
import com.ubtechinc.bluetoothlibrary.BleStatusEvent
import com.ubtechinc.bluetoothlibrary.UbtBluetoothConnManager
import com.ubtrobot.masterevent.protos.SysMasterEvent
import com.ubtrobot.transport.message.CallException
import com.ubtrobot.transport.message.Request
import com.ubtrobot.transport.message.Response
import com.ubtrobot.transport.message.ResponseCallback
import event.master.ubtrobot.com.sysmasterevent.SysEventApi
import event.master.ubtrobot.com.sysmasterevent.event.PowerButtonEvent
import event.master.ubtrobot.com.sysmasterevent.event.base.KeyEvent
import event.master.ubtrobot.com.sysmasterevent.receiver.KeyEventReceiver


/**
 * @Deseription 蓝牙状态管理
 * @Author tanghongyu
 * @Time 2018/6/29 10:12
 */
class BleStatusManager private constructor()   {
     private  var networkSkillStatus : SkillEvent.SKILL= SkillEvent.SKILL.OTHER
    private  var codeMaoSkillStatus : SkillEvent.SKILL = SkillEvent.SKILL.OTHER
    init {
        var bleStatusSubscriber: Subscriber<BleStatusEvent> = Subscriber { bleStatusEvent ->
            when(bleStatusEvent.type) {
                bleStatusEvent.TYPE_CONNECTED -> {

                }
                bleStatusEvent.TYPE_FAILED -> {
                    LogUtils.w("receive bleStatusEvent connect fail")
                    exitSkill()

                }
                else -> {
                    LogUtils.w("receive bleStatus Event disconnect")
                    exitSkill()
                }
            }
        }
        var skillStautsSubscriber: Subscriber<SkillEvent> = Subscriber { skillStatusEvent ->
            LogUtils.i("SkillEvent  = " + skillStatusEvent!!.skillStatus)
            when ( skillStatusEvent!!.skillStatus){
                SkillEvent.SKILL.BLE_NETWORK_START -> {
                    networkSkillStatus = SkillEvent.SKILL.BLE_NETWORK_START
                }
                SkillEvent.SKILL.BLE_NETWORK_STOP -> {
                    networkSkillStatus = SkillEvent.SKILL.BLE_NETWORK_STOP
                }
                SkillEvent.SKILL.CODE_MAO_CONNECTION_STOP -> {
                    codeMaoSkillStatus = SkillEvent.SKILL.CODE_MAO_CONNECTION_STOP
                    UbtBluetoothConnManager.getInstance().cancelCurrentDevices()
                }
                SkillEvent.SKILL.CODE_MAO_CONNECTION_START -> {
                    SysEventApi.get().subscribe(PowerButtonEvent.newInstance().setPriority(SysMasterEvent.Priority.HIGH), powerKeyReceiver)
                    codeMaoSkillStatus = SkillEvent.SKILL.CODE_MAO_CONNECTION_START
                }
            }

        }



        NotificationCenter.defaultCenter().subscriber(BleStatusEvent::class.java, bleStatusSubscriber)
        NotificationCenter.defaultCenter().subscriber(SkillEvent::class.java, skillStautsSubscriber)

    }
    private var powerKeyReceiver: KeyEventReceiver = object : KeyEventReceiver() {
        override fun onSingleClick(keyEvent: KeyEvent): Boolean {
            UbtBluetoothConnManager.getInstance().cancelCurrentDevices()
            SysEventApi.get().unsubscribe(this)
            return false
        }

        override fun onDoubleClick(keyEvent: KeyEvent): Boolean {
            return false
        }

        override fun onLongClick(keyEvent: KeyEvent): Boolean {
            return false
        }
    }

    companion object {
        val instance: BleStatusManager by lazy { BleStatusManager() }
    }


     fun isCodeMaoConnectionStart() : Boolean {

        return codeMaoSkillStatus == SkillEvent.SKILL.CODE_MAO_CONNECTION_START
    }
    private fun exitSkill() {
        LogUtils.i("exitSkill networkSkillStatus = $networkSkillStatus  codeMaoSkillStatus = $codeMaoSkillStatus ")

        if(networkSkillStatus == SkillEvent.SKILL.BLE_NETWORK_START) {
            exitNetworkSkill()
        }
        if(codeMaoSkillStatus == SkillEvent.SKILL.CODE_MAO_CONNECTION_START) {
            exitCodeMaoSkill()
        }

    }

    private fun exitCodeMaoSkill() {
        LogUtils.i("exitCodeMaoSkill")
        SkillManager.instance.stopCodeMaoConnectionSkill(object : ResponseCallback {
            override fun onResponse(p0: Request?, p1: Response?) {
            }

            override fun onFailure(p0: Request?, p1: CallException?) {
            }

        })
        SkillManager.instance.stopCodeMaoRunningSkill(object : ResponseCallback {
            override fun onResponse(p0: Request?, p1: Response?) {
            }

            override fun onFailure(p0: Request?, p1: CallException?) {
            }

        })
    }

    private fun exitNetworkSkill() {
        LogUtils.i("exitNetworkSkill")
        SkillManager.instance.exitNetworkSkill(object : ResponseCallback {
            override fun onFailure(p0: Request?, p1: CallException?) {
            }

            override fun onResponse(p0: Request?, p1: Response?) {
            }

        })
    }



}