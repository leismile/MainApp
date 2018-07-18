package com.ubtechinc.bluetoothrobot

import com.ubtrobot.master.Master
import com.ubtrobot.master.interactor.MasterInteractor
import com.ubtrobot.master.service.ServiceProxy
import com.ubtrobot.master.skill.SkillsProxy
import com.ubtrobot.transport.message.Param
import com.ubtrobot.transport.message.ResponseCallback
import com.ubtrobot.transport.message.StickyResponseCallback

/**
 * Created by lulin.wu on 2018/4/17.
 */

class CodeMaoService private constructor() {
    internal var serviceProxy: ServiceProxy ?= null

    companion object {
        val instance: CodeMaoService by lazy { CodeMaoService() }
    }
    init {

         serviceProxy = Master.get().globalContext.createSystemServiceProxy("codemaoservice")
    }


    fun getActionList(callback: ResponseCallback) {
        serviceProxy!!.call("/codemao/getActionListCall", callback)
    }

    fun playAction(param: Param, callback: StickyResponseCallback) {
        serviceProxy!!.callStickily("/codemao/playActionCall", param, callback)
    }

    fun stopAction(callback: ResponseCallback) {
        serviceProxy!!.call("/codemao/stopActionCall", callback)
    }

    fun moveRobot(param: Param, callback: StickyResponseCallback) {
        serviceProxy!!.callStickily("/codemao/moveRobotCall", param, callback)
    }
    fun revertOrigin( callback: ResponseCallback) {
        serviceProxy!!.call("/codemao/revertOriginCall", callback)
    }
    fun controlTTS(param: Param, callback: StickyResponseCallback) {
        serviceProxy!!.callStickily("/codemao/controlTTSCall", param, callback)
    }

    fun faceRecognise(param: Param, callback: ResponseCallback) {
        serviceProxy!!.call("/codemao/faceRecogniseCall", param, callback)
    }

    fun faceDetect(param: Param, callback: ResponseCallback) {
        serviceProxy!!.call("/codemao/faceDetectCall", param, callback)
    }
    fun faceAnalyze(param: Param, callback: ResponseCallback) {
        serviceProxy!!.call("/codemao/faceAnalyzeCall", param, callback)
    }
    fun controlFindFace(param: Param,  stickyResponseCallback: StickyResponseCallback) {
        serviceProxy!!.callStickily("/codemao/controlFindFaceCall", param, stickyResponseCallback)
    }

    fun controlFaceRegister(param: Param, callback: ResponseCallback) {
        serviceProxy!!.call("/codemao/controlFaceRegisterCall", param, callback)
    }

    fun getRegisterFaces( callback: ResponseCallback) {
        serviceProxy!!.call("/codemao/getRegisterFacesCall", callback)
    }

    fun recogniseObject(param: Param, callback: ResponseCallback) {
        serviceProxy!!.call("/codemao/recogniseObjectCall", param, callback)
    }
    fun takePicture(param: Param, callback: ResponseCallback) {
        serviceProxy!!.call("/codemao/takePictureCall", param, callback)
    }
    fun setMouthLamp(param: Param, callback: ResponseCallback) {
        serviceProxy!!.call("/codemao/setMouthLampCall", param, callback)
    }
    fun controlMouthLamp(param: Param, callback: ResponseCallback) {
        serviceProxy!!.call("/codemao/controlMouthLampCall", param, callback)
    }
    fun getExpressionList( callback: ResponseCallback) {
        serviceProxy!!.call("/codemao/getExpressionListCall", callback)
    }
    fun doExpression(param: Param, callback: StickyResponseCallback) {
        serviceProxy!!.callStickily("/codemao/doExpressionCall", param, callback)
    }
    fun controlBehavior(param: Param, callback: StickyResponseCallback) {
        serviceProxy!!.callStickily("/codemao/controlBehaviorCall", param, callback)
    }

    fun observeVolumeKeyPress(param: Param,  stickyResponseCallback: StickyResponseCallback) {
        serviceProxy!!.callStickily("/codemao/observeVolumeKeyPressCall", param, stickyResponseCallback)
    }
    fun observerRobotPosture(param: Param,  stickyResponseCallback: StickyResponseCallback) {
        serviceProxy!!.callStickily("/codemao/observerRobotPostureCall", param, stickyResponseCallback)
    }
    fun observerInfraredDistance(param: Param,  stickyResponseCallback: StickyResponseCallback) {
        serviceProxy!!.callStickily("/codemao/observerInfraredDistanceCall", param, stickyResponseCallback)
    }
    fun observeHeadRacket(param: Param,  stickyResponseCallback: StickyResponseCallback) {
        serviceProxy!!.callStickily("/codemao/observeHeadRacketCall", param, stickyResponseCallback)
    }
    fun observeBatteryStatus(param: Param,  stickyResponseCallback: StickyResponseCallback) {
        serviceProxy!!.callStickily("/codemao/observeBatteryStatusCall", param, stickyResponseCallback)
    }
    fun unobserveAllSensor(param: Param,  stickyResponseCallback: StickyResponseCallback) {
        serviceProxy!!.callStickily("/codemao/unobserveAllSensorCall", param, stickyResponseCallback)
    }


}
