package com.ubtechinc.alpha.im;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.im.msghandler.*;
import com.ubtechinc.nets.im.annotation.IMJsonMsgRelationVector;
import com.ubtechinc.nets.im.annotation.IMMsgRelationVector;
import com.ubtechinc.nets.im.annotation.ImJsonMsgRelation;
import com.ubtechinc.nets.im.annotation.ImMsgRelation;

/**
 * Created by Administrator on 2017/5/25.
 */

@IMMsgRelationVector({
        @ImMsgRelation(requestCmdId = IMCmdId.IM_CONNECT_ROBOT_REQUEST, responseCmdId = IMCmdId.IM_CONNECT_ROBOT_RESPONSE, msgHandleClass = ConnectRobotMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_DISCONNECT_ROBOT_REQUEST, responseCmdId = IMCmdId.IM_DISCONNECT_ROBOT_RESPONSE, msgHandleClass = DisConnectRobotMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_DELETE_ACTIONFILE_REQUEST, responseCmdId = IMCmdId.IM_DELETE_ACTIONFILE_RESPONSE, msgHandleClass = DeleteActionFileMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_PLAY_ACTION_REQUEST, responseCmdId = IMCmdId.IM_PLAY_ACTION_RESPONSE, msgHandleClass = PlayActionMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_GET_MOTORANGLE_REQUEST, responseCmdId = IMCmdId.IM_GET_MOTORANGLE_RESPONSE, msgHandleClass = ReadMotorAngleMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_STOP_PLAY_REQUEST, responseCmdId = IMCmdId.IM_STOP_PLAY_RESPONSE, msgHandleClass = StopActionMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_START_APP_REQUEST, responseCmdId = IMCmdId.IM_START_APP_RESPONSE, msgHandleClass = StartAppMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_STOP_APP_REQUEST, responseCmdId = IMCmdId.IM_STOP_APP_RESPONSE, msgHandleClass = StopAppMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_GET_ALLAPPS_REQUEST, responseCmdId = IMCmdId.IM_GET_ALLAPPS_RESPONSE, msgHandleClass = GetAllAppsMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_INSTALL_PACKAGES_REQUEST, responseCmdId = IMCmdId.IM_INSTALL_PACKAGES_RESPONSE, msgHandleClass = InstallAppMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_UNINSTALL_PACKAGES_REQUEST, responseCmdId = IMCmdId.IM_UNINSTALL_PACKAGES_RESPONSE, msgHandleClass = UninstallAppMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_UPDATE_PACKAGES_REQUEST, responseCmdId = IMCmdId.IM_UPDATE_PACKAGES_RESPONSE, msgHandleClass = InstallAppMsgHandler.class),

        @ImMsgRelation(requestCmdId = IMCmdId.IM_GET_APPCONFIG_REQUEST, responseCmdId = IMCmdId.IM_GET_APPCONFIG_RESPONSE, msgHandleClass = GetAppConfigMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_SAVE_APPCONFIG_REQUEST, responseCmdId = IMCmdId.IM_SAVE_APPCONFIG_RESPONSE, msgHandleClass = SaveAppConfigMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_GET_TOP_APP_REQUEST, responseCmdId = IMCmdId.IM_GET_TOP_APP_RESPONSE, msgHandleClass = GetTopAppMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_GET_APP_BUTTONEVENT_REQUEST, responseCmdId = IMCmdId.IM_GET_APP_BUTTONEVENT_RESPONSE, msgHandleClass = GetAppButtonMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_CLICK_APP_BUTTON_REQUEST, responseCmdId = IMCmdId.IM_CLICK_APP_BUTTON_RESPONSE, msgHandleClass = ClickAppButtonMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_DOWNLOAD_ACTIONFILE_REQUEST, responseCmdId = IMCmdId.IM_DOWNLOAD_ACTIONFILE_RESPONSE, msgHandleClass = DownloadActionMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_DESKCLOCK_MANAGE_REQUEST, responseCmdId = IMCmdId.IM_DESKCLOCK_RESPONSE, msgHandleClass = DeskClockManageHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_DESKCLOCK_ACTIVIE_LIST_REQUEST, responseCmdId = IMCmdId.IM_DESKCLOCKLIST_RESPONSE, msgHandleClass = GetActiveDeskClockListHandler.class),

        @ImMsgRelation(requestCmdId = IMCmdId.IM_TRANSFER_PHOTO_REQUEST, responseCmdId = IMCmdId.IM_TRANSFER_PHOTO_RESPONSE, msgHandleClass = TransferPhotoMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_SET_RTC_TIME_REQUEST, responseCmdId = IMCmdId.IM_SET_RTC_TIME_RESPONSE, msgHandleClass = SetRTCTimeMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_GET_ALL_THUMBNAIL_REQUEST, responseCmdId = IMCmdId.IM_GET_ALL_THUMBNAIL_RESPONSE, msgHandleClass = TransferThumbPhotoMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_GET_NEW_ACTION_LIST_REQUEST, responseCmdId = IMCmdId.IM_GET_NEW_ACTION_LIST_RESPONSE, msgHandleClass = GetActionlistMsgHandler.class),

        @ImMsgRelation(requestCmdId = IMCmdId.IM_TTS_BREATH_ACTION_ON_OFF_REQUEST, responseCmdId = IMCmdId.IM_TTS_BREATH_ACTION_ON_OFF_RESPONSE, msgHandleClass = SetTTSActionActiveDataMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_GET_TTS_BREATH_ACTION_ON_OFF_REQUEST, responseCmdId = IMCmdId.IM_GET_TTS_BREATH_ACTION_ON_OFF_RESPONSE, msgHandleClass = GetTTSActionActiveDataMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_SYN_SPEECH_REQUEST, responseCmdId = IMCmdId.IM_SYN_SPEECH_RESPONSE, msgHandleClass = SynSpeechMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_CLOSE_ROBOT_ERROR_LOG_REQUEST, responseCmdId = IMCmdId.IM_CLOSE_ROBOT_ERROR_LOG_RESPONSE, msgHandleClass = SetErrorLogDataMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_QUERY_ROBOT_STORAGE_APP_LIST_REQUEST, responseCmdId = IMCmdId.IM_QUERY_ROBOT_STORAGE_APP_LIST_RESPONSE, msgHandleClass = GetStorageAppListMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_UNINSTALL_BATCH_APPS_REQUEST, responseCmdId = IMCmdId.IM_UNINSTALL_BATCH_APPS_RESPONSE, msgHandleClass = BatchUninstallAppsMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_RETRY_PLAY_ANSWER_REQUEST, responseCmdId = IMCmdId.IM_RETRY_PLAY_ANSWER_RESPONSE, msgHandleClass = ReplaySpeechRecordMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_DELETE_FORMER_CLOCK_REQUEST, responseCmdId = IMCmdId.IM_DELETE_FORMER_CLOCK_RESPONSE, msgHandleClass = BatchDeletDeskClockMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_GET_FORMER_CLOCK_REQUEST, responseCmdId = IMCmdId.IM_GET_FORMER_CLOCK_RESPONSE, msgHandleClass = GetDeskClockHistoryMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_GET_ROBOT_INIT_STATUS_REQUEST, responseCmdId = IMCmdId.IM_GET_ROBOT_INIT_STATUS_RESPONSE, msgHandleClass = GetRobotInitStatusMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_QUERY_ROBOT_POWER_REQUEST, responseCmdId = IMCmdId.IM_QUERY_ROBOT_POWER_RESPONSE, msgHandleClass = QueryPowerDataMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_QUERY_HARD_SOFT_WARE_VERSION_REQUEST, responseCmdId = IMCmdId.IM_QUERY_HARD_SOFT_WARE_VERSION_RESPONSE, msgHandleClass = QuerySoftwareVersionMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_SET_MASTER_NAME_REQUEST, responseCmdId = IMCmdId.IM_SET_MASTER_NAME_RESPONSE, msgHandleClass = SetMasterNameMsgHandler.class),

        @ImMsgRelation(requestCmdId = IMCmdId.IM_SEND_TVS_ACCESSTOKEN_REQUEST, responseCmdId = IMCmdId.IM_SEND_TVS_ACCESSTOKEN_RESPONSE, msgHandleClass = SendTVSAccessTokenMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_GET_TVS_PRODUCTID_REQUEST, responseCmdId = IMCmdId.IM_GET_TVS_PRODUCTID_RESPONSE, msgHandleClass = GetTVSProductIdMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_CONFIRM_ONLINE_REQUEST, responseCmdId = IMCmdId.IM_CONFIRM_ONLINE_RESPONSE, msgHandleClass = ConfirmOnlineMsgHandler.class),

        @ImMsgRelation(requestCmdId = IMCmdId.IM_CONFIRM_ONLINE_REQUEST, responseCmdId = IMCmdId.IM_CONFIRM_ONLINE_RESPONSE, msgHandleClass = ConfirmOnlineMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_GET_SERIAL_NUMBER_REQUEST, responseCmdId = IMCmdId.IM_GET_SERIAL_NUMBER_RESPONSE, msgHandleClass = GetSerialNumMsgHandler.class),

        @ImMsgRelation(requestCmdId = IMCmdId.IM_GET_ROBOT_CONFIG_REQUEST, responseCmdId = IMCmdId.IM_GET_ROBOT_CONFIG_RESPONSE, msgHandleClass = GetRobotConfigHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_SEND_WIFI_TO_ROBOT_REQUEST, responseCmdId = IMCmdId.IM_SEND_WIFI_TO_ROBOT_RESPONSE, msgHandleClass = ConnectWifiHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_GET_WIFI_LIST_TO_ROBOT_REQUEST, responseCmdId = IMCmdId.IM_GET_WIFI_LIST_TO_ROBOT_RESPONSE, msgHandleClass = GetWifiListHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_GET_ALBUM_LIST_REQUEST, responseCmdId = IMCmdId.IM_GET_ALBUM_LIST_RESPONSE, msgHandleClass = GetAlbumlistMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_GET_MOBILE_ALBUM_DOWNLOAD_REQUEST, responseCmdId = IMCmdId.IM_GET_MOBILE_ALBUM_DOWNLOAD_RESPONSE, msgHandleClass = DownloadPhotoMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_START_FACE_DETECT_REQUEST, responseCmdId = IMCmdId.IM_START_FACE_DETECT_RESPONSE, msgHandleClass = FaceRegisterHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_FACE_UPDATE_REQUEST, responseCmdId = IMCmdId.IM_FACE_UPDATE_RESPONSE, msgHandleClass = FaceUpdateHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_FACE_DELETE_REQUEST, responseCmdId = IMCmdId.IM_FACE_DELETE_RESPONSE, msgHandleClass = FaceDeleteHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_FACE_LIST_REQUEST, responseCmdId = IMCmdId.IM_FACE_LIST_RESPONSE, msgHandleClass = FaceListHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_FACE_EXIT_REQUEST, responseCmdId = IMCmdId.IM_FACE_EXIT_RESPONSE, msgHandleClass = FaceRegisterExitHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_FACE_CHECK_STATE_REQUEST, responseCmdId = IMCmdId.IM_FACE_CHECK_STATE_RESPONSE, msgHandleClass = FaceCheckStateHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_GET_AGORA_ROOM_INFO_REQUEST, responseCmdId = IMCmdId.IM_GET_AGORA_ROOM_INFO_RESPONSE, msgHandleClass = AlGetAgoraRoomInfoMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_CHECK_SIM_CARD_REQUEST, responseCmdId = IMCmdId.IM_CHECK_SIM_CARD_RESPONSE, msgHandleClass = CheckSimCardHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_IMPORT_PHONE_CONTACT_REQUEST, responseCmdId = IMCmdId.IM_IMPORT_PHONE_CONTACT_RESPONSE, msgHandleClass = ImportPhoneContactHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_QUERY_CONTACT_LIST_REQUEST, responseCmdId = IMCmdId.IM_QUERY_CONTACT_LIST_RESPONSE, msgHandleClass = QueryContactHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_ADD_CONTACT_REQUEST, responseCmdId = IMCmdId.IM_ADD_CONTACT_RESPONSE, msgHandleClass = AddContactHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_MODIFY_CONTACT_REQUEST, responseCmdId = IMCmdId.IM_MODIFY_CONTACT_RESPONSE, msgHandleClass = ModifyContactHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_DELETECONTACT_REQUEST, responseCmdId = IMCmdId.IM_DELETECONTACT_RESPONSE, msgHandleClass = DeleteContactHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_QUERY_CALL_RECORD_REQUEST, responseCmdId = IMCmdId.IM_QUERY_CALL_RECORD_RESPONSE, msgHandleClass = CallRecordHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_QUERY_MOBILE_NETWORK_REQUEST, responseCmdId = IMCmdId.IM_QUERY_MOBILE_NETWORK_RESPONSE, msgHandleClass = QueryMobileNetwork.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_MODIFY_MOBILE_DATA_REQUEST, responseCmdId = IMCmdId.IM_MODIFY_MOBILE_DATA_RESPONSE, msgHandleClass = ModifyMobileDataHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_MODIFY_MOBILE_ROAM_REQUEST, responseCmdId = IMCmdId.IM_MODIFY_MOBILE_ROAM_RESPONSE, msgHandleClass = ModifyMobileRoamHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_PLAY_BEHAVIOR_REQUEST, responseCmdId = IMCmdId.IM_PLAY_BEHAVIOR_RESPONSE, msgHandleClass = PlayBehaviorHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_GET_BEHAVIOR_LIST_REQUEST, responseCmdId = IMCmdId.IM_GET_BEHAVIOR_LIST_RESPONSE, msgHandleClass = GetBehaviorListHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_GET_ROBOT_IP_REQUEST, responseCmdId = IMCmdId.IM_GET_ROBOT_IP_RESPONSE, msgHandleClass = GetRobotIpHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_DETECT_UPGRADE_REQUEST, responseCmdId = IMCmdId.IM_DETECT_UPGRADE_RESPONSE, msgHandleClass = DetectUpgradeHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_FIRMWARE_DOWNLOAD_REQUEST, responseCmdId = IMCmdId.IM_FIRMWARE_DOWNLOAD_RESPONSE, msgHandleClass = DownloadFirmwareHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_FIRMWARE_UPGRADE_REQUEST, responseCmdId = IMCmdId.IM_FIRMWARE_UPGRADE_RESPONSE, msgHandleClass = UpgradeFirmwareHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_QUERY_FIRMWARE_DOWNLOAD_PROGRESS_REQUEST, responseCmdId = IMCmdId.IM_QUERY_FIRMWARE_DOWNLOAD_PROGRESS_RESPONSE, msgHandleClass = QueryFirmwareDownloadProgressHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_ADB_SWITCH_REQUEST, responseCmdId = IMCmdId.IM_ADB_CMD_RESPONSE, msgHandleClass = AdbCmdMsgHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_SWITCH_CODE_MAO_REQUEST, responseCmdId = IMCmdId.IM_SWITCH_CODE_MAO_RESPONSE, msgHandleClass = SwitchCodeMaoHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_UPLOAD_LOG_REQUEST, responseCmdId = IMCmdId.IM_UPLOAD_LOG_RESPONSE, msgHandleClass = UploadFileHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_PLAY_SQUARE_ACTION_REQUEST, responseCmdId = IMCmdId.IM_PLAY_SQUARE_ACTON_RESPONSE, msgHandleClass = DoSquareActionHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_STOP_SQUARE_ACTION_REQUEST, responseCmdId = IMCmdId.IM_STOP_SQUARE_ACTON_RESPONSE, msgHandleClass = StopSquareActionHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_GET_MULTI_CONVERSATION_STATE_REQUEST, responseCmdId = IMCmdId.IM_GET_MULTI_CONVERSATION_STATE_RESPONSE, msgHandleClass = GetMultiConversationHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_SET_MULTI_CONVERSATION_STATE_REQUEST, responseCmdId = IMCmdId.IM_SET_MULTI_CONVERSATION_STATE_RESPONSE, msgHandleClass = SetMultiConversationHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_GET_CAMREA_PRIVACY_REQUEST, responseCmdId = IMCmdId.IM_GET_CAMREA_PRIVACY_RESPONSE, msgHandleClass = GetCameraPrivacyHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_SET_CAMREA_PRIVACY_REQUEST, responseCmdId = IMCmdId.IM_SET_CAMREA_PRIVACY_RESPONSE, msgHandleClass = SetCameraPrivacyHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_JIMU_CAR_CHANGE_DRIVE_MODE_REQUEST, responseCmdId = IMCmdId.IM_JIMU_CAR_CHANGE_DRIVE_MODE_RESPONSE, msgHandleClass = JimuCarChangeDriveModeHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_JIMU_CAR_QUERY_POWER_REQUEST, responseCmdId = IMCmdId.IM_JIMU_CAR_QUERY_POWER_RESPONSE, msgHandleClass = JimuCarQueryPowerHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_JIMU_CAR_CONTROL_REQUEST, responseCmdId = IMCmdId.IM_JIMU_CAR_CONTROL_RESPONSE, msgHandleClass = JimuCarControlHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_JIMU_CAR_GET_IR_DISTANCE_REQUEST, responseCmdId = IMCmdId.IM_JIMU_CAR_GET_IR_DISTANCE_RESPONSE, msgHandleClass = JimuCarIRDistanceHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_JIMU_CAR_ROBOT_CHAT_REQUEST, responseCmdId = IMCmdId.IM_JIMU_CAR_ROBOT_CHAT_RESPONSE, msgHandleClass = JimuCarRobotChatHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_JIMU_CAR_GET_BLE_CAR_LIST_REQUEST, responseCmdId = IMCmdId.IM_JIMU_CAR_GET_BLE_CAR_LIST_RESPONSE, msgHandleClass = JImuCarGetBleCarListHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_JIMU_CAR_CONNECT_CAR_REQUEST, responseCmdId = IMCmdId.IM_JIMU_CAR_CONNECT_CAR_RESPONSE, msgHandleClass = JimuCarConnectBleCarHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_JIMU_CAR_CAR_CHECK_REQUEST, responseCmdId = IMCmdId.IM_JIMU_CAR_CAR_CHECK_RESPONSE, msgHandleClass = JimuCarCheckHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_JIMU_CAR_QUERY_CONNECT_STATE_REQUEST, responseCmdId = IMCmdId.IM_JIMU_CAR_QUERY_CONNECT_STATE_RESPONSE, msgHandleClass = JimuCarQueryConnectStateHandler.class),
        @ImMsgRelation(requestCmdId = IMCmdId.IM_JIMU_CAR_CHANGE_LEVEL_REQUEST, responseCmdId = IMCmdId.IM_JIMU_CAR_CHANGE_LEVEL_RESPONSE, msgHandleClass = JimuCarChangeLevelHandler.class),

})
@IMJsonMsgRelationVector({
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_ACCOUNT_BEEN_UNBIND_RESPONSE, msgHandleClass = AccountApplyMsgHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_ACCOUNT_MASTER_UNBINDED_RESPONSE, msgHandleClass = AccountApplyMsgHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_ACCOUNT_SLAVER_UNBIND_RESPONSE, msgHandleClass = AccountApplyMsgHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_FACE_COLLECTION_RESPONSE, msgHandleClass = PushFaceCollectionHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_FACE_RECOGNITION_RESPONSE, msgHandleClass = PushFaceRecognitionHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_FACE_BEAUTY_RESPONSE, msgHandleClass = PushFaceBeautyHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_BOOK_READING_START_RESPONSE, msgHandleClass = PushBookReadStartHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_BOOK_READING_FINISH_RESPONSE, msgHandleClass = PushBookReadFinishHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_START_FRUIT_RECOGNITION_RESPONSE, msgHandleClass = PushStartFruitRecognitionHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_START_OBJECT_RECOGNITION_RESOPNSE, msgHandleClass = PushStartObjectRecognitionHandler.class),

        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_WHAT_CAN_RESOPNSE, msgHandleClass = GuideCanHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_HOW_BIND_RESOPNSE, msgHandleClass = GuideCanHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_HOW_CONNECT_RESOPNSE, msgHandleClass = GuideCanHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_HOW_STOP_RESOPNSE, msgHandleClass = GuideCanHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_HOW_VOLUME_RESOPNSE, msgHandleClass = GuideCanHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_HOW_SHUTOFF_RESOPNSE, msgHandleClass = GuideCanHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_HOW_RESET_RESOPNSE, msgHandleClass = GuideCanHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_HOW_LTE_RESOPNSE, msgHandleClass = GuideCanHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_HOW_PHOTO_RESOPNSE, msgHandleClass = GuideCanHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_HOW_TELEPHONE_RESOPNSE, msgHandleClass = GuideCanHandler.class),

        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_HOW_SURVEILLANCE_RESOPNSE, msgHandleClass = GuideCanHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_HOW_PICTURE_RESOPNSE, msgHandleClass = GuideCanHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_HOW_TRANSLATE_RESOPNSE, msgHandleClass = GuideCanHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_HOW_WEATHER_RESOPNSE, msgHandleClass = GuideCanHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_HOW_STOCK_RESOPNSE, msgHandleClass = GuideCanHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_HOW_MUSIC_RESOPNSE, msgHandleClass = GuideCanHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_HOW_STORY_RESOPNSE, msgHandleClass = GuideCanHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_HOW_ALARM_RESOPNSE, msgHandleClass = GuideCanHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_HOW_CHANGE_RESOPNSE, msgHandleClass = GuideCanHandler.class),

        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_WIFI_SWITCH_RESOPNSE, msgHandleClass = GuideCanHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_4G_STATU_RESOPNSE, msgHandleClass = GuideCanHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_CHARGE_STATU_RESOPNSE, msgHandleClass = GuideCanHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_WHICH_WIFI_RESOPNSE, msgHandleClass = GuideCanHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_WHO_ADMIN_RESOPNSE, msgHandleClass = GuideWhoAdminHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_WHO_BINDER_RESOPNSE, msgHandleClass = GuideWhoBindHandler.class),

        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_HOW_BATTERY_RESOPNSE, msgHandleClass = GuideCanHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_GUIDE_HOW_CONTENT_RESOPNSE, msgHandleClass = GuideCanHandler.class),
        //识花君
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_FLOWER_RECOGNITION_RESOPNSE, msgHandleClass = FlowerRecognitionHandler.class),
        @ImJsonMsgRelation(requestCmdId = IMCmdId.IM_FLOWER_RECOGNIZER_RESOPNSE, msgHandleClass = FlowerRecognizerHandler.class),
})


public class ImMainServiceMsgDispatcher extends ImMsgDispathcer {
    public ImMainServiceMsgDispatcher() {
        super();
    }

    @Override
    public void dispatchMsg(int cmdId, AlphaMessageOuterClass.AlphaMessage requestMsg, String peer) {
        if (ControlClientManager.getInstance().isLegalPeer(cmdId, peer)) {
            super.dispatchMsg(cmdId, requestMsg, peer);

        }
    }
}

