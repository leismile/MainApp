package com.ubtechinc.bluetoothrobot.communite;


import com.ubtechinc.bluetoothrobot.handler.BindOrSwitchWifiHandler;
import com.ubtechinc.bluetoothrobot.handler.ControlBehaviorHandler;
import com.ubtechinc.bluetoothrobot.handler.ControlFaceRegisterHandler;
import com.ubtechinc.bluetoothrobot.handler.ControlFindFaceHandler;
import com.ubtechinc.bluetoothrobot.handler.ControlMouthLampHandler;
import com.ubtechinc.bluetoothrobot.handler.ControlRuningProgramHandler;
import com.ubtechinc.bluetoothrobot.handler.ControlTTSHandler;
import com.ubtechinc.bluetoothrobot.handler.DoExpressionHandler;
import com.ubtechinc.bluetoothrobot.handler.FaceAnalyzeHandler;
import com.ubtechinc.bluetoothrobot.handler.FaceDetectHandler;
import com.ubtechinc.bluetoothrobot.handler.FaceRecogniseHandler;
import com.ubtechinc.bluetoothrobot.handler.GetActionListHandler;
import com.ubtechinc.bluetoothrobot.handler.GetExpressionListHandler;
import com.ubtechinc.bluetoothrobot.handler.GetRegisterFacesHandler;
import com.ubtechinc.bluetoothrobot.handler.HandShakeHandler;
import com.ubtechinc.bluetoothrobot.handler.HeartBeatHandler;
import com.ubtechinc.bluetoothrobot.handler.ObserveBatteryStatusHandler;
import com.ubtechinc.bluetoothrobot.handler.ObserveHeadRacketHandler;
import com.ubtechinc.bluetoothrobot.handler.ObserveVolumeKeyPressHandler;
import com.ubtechinc.bluetoothrobot.handler.ObserverInfraredDistanceHandler;
import com.ubtechinc.bluetoothrobot.handler.ObserverRobotPostureHandler;
import com.ubtechinc.bluetoothrobot.handler.RecogniseObjectHandler;
import com.ubtechinc.bluetoothrobot.handler.MoveRobotHandler;
import com.ubtechinc.bluetoothrobot.handler.PlayActionHandler;
import com.ubtechinc.bluetoothrobot.handler.RevertOriginHandler;
import com.ubtechinc.bluetoothrobot.handler.SetlMouthLampHandler;
import com.ubtechinc.bluetoothrobot.handler.StopActionHandler;
import com.ubtechinc.bluetoothrobot.handler.TakePictureHandler;
import com.ubtechinc.protocollibrary.annotation.IMJsonMsgRelationVector;
import com.ubtechinc.protocollibrary.annotation.IMMsgRelationVector;
import com.ubtechinc.protocollibrary.annotation.ImMsgRelation;
import com.ubtechinc.protocollibrary.communite.ImMsgDispathcer;
import com.ubtechinc.protocollibrary.protocol.CmdId;

/**
 * Created by Administrator on 2017/5/26.
 */
@IMJsonMsgRelationVector({
})
@IMMsgRelationVector({

        @ImMsgRelation(requestCmdId = CmdId.BL_GET_ACTION_LIST_REQUEST, responseCmdId = CmdId.BL_GET_ACTION_LIST_RESPONSE, msgHandleClass = GetActionListHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_PLAY_ACTION_REQUEST, responseCmdId = CmdId.BL_PLAY_ACTION_RESPONSE, msgHandleClass = PlayActionHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_STOP_ACTION_REQUEST, responseCmdId = CmdId.BL_STOP_ACTION_RESPONSE, msgHandleClass = StopActionHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_MOVE_ROBOT_REQUEST, responseCmdId = CmdId.BL_MOVE_ROBOT_RESPONSE, msgHandleClass = MoveRobotHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_REVERT_ORIGINAL_REQUEST, responseCmdId = CmdId.BL_REVERT_ORIGINAL_RESPONSE, msgHandleClass = RevertOriginHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_CONTROL_TTS_REQUEST, responseCmdId = CmdId.BL_CONTROL_TTS_RESPONSE, msgHandleClass = ControlTTSHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_GET_RECOGNISE_FACE_COUNT_REQUEST, responseCmdId = CmdId.BL_GET_RECOGNISE_FACE_COUNT_RESPONSE, msgHandleClass = FaceDetectHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_GET_FACE_ANALYZE_REQUEST, responseCmdId = CmdId.BL_GET_RECOGNISE_FACE_GENDER_RESPONSE, msgHandleClass = FaceAnalyzeHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_GET_RECOGNISE_OBJECT_REQUEST, responseCmdId = CmdId.BL_GET_RECOGNISE_OBJECT_RESPONSE, msgHandleClass = RecogniseObjectHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_CONTROL_REGISTER_FACE_REQUEST, responseCmdId = CmdId.BL_CONTROL_REGISTER_FACE_RESPONSE, msgHandleClass = ControlFaceRegisterHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_GET_REGISTER_FACES_REQUEST, responseCmdId = CmdId.BL_GET_REGISTER_FACES_RESPONSE, msgHandleClass = GetRegisterFacesHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_FACE_RECOGNISE_REQUEST, responseCmdId = CmdId.BL_FACE_RECOGNISE_RESPONSE, msgHandleClass = FaceRecogniseHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_CONTROL_FIND_FACE_REQUEST, responseCmdId = CmdId.BL_CONTROL_FIND_FACE_RESPONSE, msgHandleClass = ControlFindFaceHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_TAKE_PICTURE_REQUEST, responseCmdId = CmdId.BL_TAKE_PICTURE_RESPONSE, msgHandleClass = TakePictureHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_GET_EXPRESS_LIST_REQUEST, responseCmdId = CmdId.BL_GET_EXPRESS_LIST_RESPONSE, msgHandleClass = GetExpressionListHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_PLAY_EXPRESS_REQUEST, responseCmdId = CmdId.BL_PLAY_EXPRESS_RESPONSE, msgHandleClass = DoExpressionHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_CONTROL_MOUTH_LAMP_REQUEST, responseCmdId = CmdId.BL_CONTROL_MOUTH_LAMP_RESPONSE, msgHandleClass = ControlMouthLampHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_SET_MOUTH_LAMP_REQUEST, responseCmdId = CmdId.BL_SET_MOUTH_LAMP_RESPONSE, msgHandleClass = SetlMouthLampHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_CONTROL_BEHAVIOR_REQUEST, responseCmdId = CmdId.BL_CONTROL_BEHAVIOR_RESPONSE, msgHandleClass = ControlBehaviorHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_OBSERVE_VOLUME_KEY_PRESS_REQUEST, responseCmdId = CmdId.BL_OBSERVE_VOLUME_KEY_PRESS_RESPONSE, msgHandleClass = ObserveVolumeKeyPressHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_OBSERVE_BATTERY_STATUS_REQUEST, responseCmdId = CmdId.BL_OBSERVE_BATTERY_STATUS_RESPONSE, msgHandleClass = ObserveBatteryStatusHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_OBSERVE_RACKET_HEAD_REQUEST, responseCmdId = CmdId.BL_OBSERVE_RACKET_HEAD_RESPONSE, msgHandleClass = ObserveHeadRacketHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_OBSERVE_ROBOT_POSTURE_REQUEST, responseCmdId = CmdId.BL_OBSERVE_ROBOT_POSTURE_RESPONSE, msgHandleClass = ObserverRobotPostureHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_OBSERVE_DISTANCE_REQUEST, responseCmdId = CmdId.BL_OBSERVE_DISTANCE_RESPONSE, msgHandleClass = ObserverInfraredDistanceHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_HEART_BEAT_REQUEST,responseCmdId = CmdId.BL_HEART_BEAT_RESPONSE,msgHandleClass = HeartBeatHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_BIND_OR_SWITCH_WIFI_REQUEST, responseCmdId = CmdId.BL_BIND_OR_SWITCH_WIFI_RESPONSE, msgHandleClass = BindOrSwitchWifiHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_HAND_SHAKE_REQUEST, responseCmdId = CmdId.BL_HAND_SHAKE_RESPONSE, msgHandleClass = HandShakeHandler.class),
        @ImMsgRelation(requestCmdId = CmdId.BL_CONTROL_RUNNING_PROGRAM_REQUEST, responseCmdId = CmdId.BL_CONTROL_RUNNING_PROGRAM_RESPONSE, msgHandleClass = ControlRuningProgramHandler.class),
})
public class
ImRobotMsgDispatcher extends ImMsgDispathcer {
}

