package com.ubtechinc.bluetoothrobot

/**
 * @Deseription
 * @Author tanghongyu
 * @Time 2018/6/6 18:10
 */
class SkillEvent constructor(skillStatus : SKILL){

    var skillStatus : SKILL = skillStatus

    enum class SKILL{
        CODE_MAO_CONNECTION_START,CODE_MAO_CONNECTION_STOP, CODE_MAO_RUNNING_START, CODE_MAO_RUNNING_STOP,BLE_NETWORK_START,BLE_NETWORK_STOP, OTHER
    }


}