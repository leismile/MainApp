package com.ubtechinc.alpha.im;

/**
 * Created by Administrator on 2017/5/29.
 * 手机与机器人通信的命令字
 * 命名规范： IM_XXX_REQUEST,IM_XXX_RESPONSE
 */

public class IMCmdId {

    public static final String IM_VERSION = "1";

    //request
    public static final int RESPONSE_BASE = 1000;

/***************************************客户端请求************************************************/

    /**
     * 删除动作文件
     */
    static public final int IM_DELETE_ACTIONFILE_REQUEST = 4;
    /**
     * 执行动作表
     */
    static public final int IM_PLAY_ACTION_REQUEST = 5;
    /**
     * 获取舵机角度值
     */
    static public final int IM_GET_MOTORANGLE_REQUEST = 10;
    /**
     * 停止执行动作
     */
    static public final int IM_STOP_PLAY_REQUEST = 11;
    /**
     * 启动第三方app
     **/
    public static final int IM_START_APP_REQUEST = 30;
    /**
     * 退出第三方app
     **/
    public static final int IM_STOP_APP_REQUEST = 31;
    /**
     * 获取全部第三方app
     **/
    public static final int IM_GET_ALLAPPS_REQUEST = 35;
    /**
     * 设置调试模式
     **/
    public static final int IM_SET_DEBUGMODE_REQUEST = 36;
    /**
     * 安装第三方
     **/
    public static final int IM_INSTALL_PACKAGES_REQUEST = 38;
    /**
     * 卸载第三方
     **/
    public static final int IM_UNINSTALL_PACKAGES_REQUEST = 39;
    /**
     * 更新第三方
     **/
    public static final int IM_UPDATE_PACKAGES_REQUEST = 40;
    /**
     * 获取第三方应用配置信息
     **/
    public static final int IM_GET_APPCONFIG_REQUEST = 41;
    /**
     * 保存第三方配置信息
     **/
    public static final int IM_SAVE_APPCONFIG_REQUEST = 42;
    /**
     * 获取当前APP
     **/
    public static final int IM_GET_TOP_APP_REQUEST = 44;
    /**
     * 请求获取app 按钮事件
     **/
    public static final int IM_GET_APP_BUTTONEVENT_REQUEST = 45;
    /**
     * 请求执行app 按钮
     **/
    public static final int IM_CLICK_APP_BUTTON_REQUEST = 46;
    /**
     * 动作下载
     **/
    public static final int IM_DOWNLOAD_ACTIONFILE_REQUEST = 47;
    /**
     * 闹钟功能请求
     **/
    public static final int IM_DESKCLOCK_MANAGE_REQUEST = 49;
    /**
     * 获取有效的闹钟列表请求
     **/
    public static final int IM_DESKCLOCK_ACTIVIE_LIST_REQUEST = 50;
    /**
     * 传送照片
     **/
    public static final int IM_TRANSFER_PHOTO_REQUEST = 53;
    /**
     * 恢复出厂设置
     **/
    public static final int IM_MASTER_CLEAR_REQUEST = 58;
    /**
     * 设置RTC时间
     **/
    public static final int IM_SET_RTC_TIME_REQUEST = 59;
    /**
     * 找手机
     **/
    public static final int IM_FIND_MOBILEPHONE_REQUEST = 62;

    /**
     * 批量删除闹钟
     */
    public static final int IM_DELETE_FORMER_CLOCK_REQUEST = 63;
    /**
     * 获取所有闹钟
     */
    public static final int IM_GET_FORMER_CLOCK_REQUEST = 64;
    /**
     * 请求所有的缩略图
     */
    public static final int IM_GET_ALL_THUMBNAIL_REQUEST = 66;
    /**
     * 查询机器人电量
     */
    public static final int IM_QUERY_ROBOT_POWER_REQUEST = 68;
    /**
     * 获取机器人软硬件版本号
     */
    public static final int IM_QUERY_HARD_SOFT_WARE_VERSION_REQUEST = 69;
    /**
     * 获取动作列表
     */
    public static final int IM_GET_NEW_ACTION_LIST_REQUEST = 70;
    /**
     * 获取机器人初始化参数 状态信息  与72合并
     */
    public static final int IM_GET_ROBOT_INIT_STATUS_REQUEST = 72;
    /**
     * 管理机器人蓝牙 待确认
     */
    public static final int IM_MANAGER_ROBOT_BLUETOOTH_REQUEST = 74;
    /**
     * 闲聊tts动作 &呼吸动作开关
     */
    public static final int IM_TTS_BREATH_ACTION_ON_OFF_REQUEST = 90;
    /**
     * 语音合成指令
     */
    public static final int IM_SYN_SPEECH_REQUEST = 91;
    /**
     * 机器人错误日志开关
     */
    public static final int IM_CLOSE_ROBOT_ERROR_LOG_REQUEST = 92;
    /**
     * 查询机器人空间 app列表
     */
    public static final int IM_QUERY_ROBOT_STORAGE_APP_LIST_REQUEST = 93;
    /**
     * 批量卸载apps
     */
    public static final int IM_UNINSTALL_BATCH_APPS_REQUEST = 94;
    /**
     * 重新播放答案内容
     */
    public static final int IM_RETRY_PLAY_ANSWER_REQUEST = 95;
    /**
     * 连接机器人 control
     */
    public static final int IM_CONNECT_ROBOT_REQUEST = 97;
    /**
     * 断开机器人 control
     */
    public static final int IM_DISCONNECT_ROBOT_REQUEST = 98;
    /**
     * 设置主人名称
     */
    public static final int IM_SET_MASTER_NAME_REQUEST = 99;
    /**
     * 获取闲聊tts动作 &呼吸动作开关
     */
    public static final int IM_GET_TTS_BREATH_ACTION_ON_OFF_REQUEST = 100;

    public static final int IM_GET_AGORA_ROOM_INFO_REQUEST = 109;
    /**
     * 通讯功能
     */
    public static final int IM_CHECK_SIM_CARD_REQUEST = 119;
    public static final int IM_IMPORT_PHONE_CONTACT_REQUEST = 120;
    public static final int IM_QUERY_CONTACT_LIST_REQUEST = 121;
    public static final int IM_ADD_CONTACT_REQUEST = 122;
    public static final int IM_MODIFY_CONTACT_REQUEST = 123;
    public static final int IM_DELETECONTACT_REQUEST = 124;
    public static final int IM_QUERY_CALL_RECORD_REQUEST = 125;
    public static final int IM_QUERY_MOBILE_NETWORK_REQUEST = 126;
    public static final int IM_MODIFY_MOBILE_DATA_REQUEST = 127;
    public static final int IM_MODIFY_MOBILE_ROAM_REQUEST = 128;


    public static final int IM_CONFIRM_ONLINE_REQUEST = 106; //手机端发起的Ping包协议，用来判断机器人是否在线

    /**
     * 接入腾讯TVS专用，用来传输 accessToken,freshToken等账号相关的信息
     **/
    public static final int IM_SEND_TVS_ACCESSTOKEN_REQUEST = 107;
    public static final int IM_GET_TVS_PRODUCTID_REQUEST = 108;


    public static final int IM_GET_MOBILE_ALBUM_PUSH_REQUEST = 110;
    public static final int IM_GET_MOBILE_ALBUM_DOWNLOAD_REQUEST = 111;
    public static final int IM_GET_ALBUM_LIST_REQUEST = 112;

    /**
     * 人脸录入相关接口
     **/
    public static final int IM_START_FACE_DETECT_REQUEST = 113;//启动人脸识别
    public static final int IM_FACE_UPDATE_REQUEST = 114; //修改人脸信息
    public static final int IM_FACE_DELETE_REQUEST = 115; //删除人脸信息
    public static final int IM_FACE_LIST_REQUEST = 116; //人脸列表信息
    public static final int IM_FACE_EXIT_REQUEST = 117; //退出人脸录入
    public static final int IM_FACE_CHECK_STATE_REQUEST = 118; //检查人脸录入状态

    public static final int IM_GET_SERIAL_NUMBER_REQUEST = 303;


    /***获取机器人的配置信息***/
    public static final int IM_GET_ROBOT_CONFIG_REQUEST = 305;

    /****发送WiFi名称给机器人***/
    public static final int IM_SEND_WIFI_TO_ROBOT_REQUEST = 306;

    /***发送获取机器人WiFi列表**/
    public static final int IM_GET_WIFI_LIST_TO_ROBOT_REQUEST = 307;


    static public final int IM_APP_LEAVE_AGORA_ROOM = 1903;

    /**PC端 机器人一键设置舵机基准角度**/


    /***执行行为配置文件****/
    public static final int IM_PLAY_BEHAVIOR_REQUEST = 309;
    public static final int IM_GET_BEHAVIOR_LIST_REQUEST = 310;
    /**
     * 获取机器人ip,Wifi名称
     */
    public static final int IM_GET_ROBOT_IP_REQUEST = 311;
    /**
     * ota
     */
    static final int IM_DETECT_UPGRADE_REQUEST = 312;
    static final int IM_FIRMWARE_DOWNLOAD_REQUEST = 313;
    static final int IM_FIRMWARE_UPGRADE_REQUEST = 314;
    static final int IM_QUERY_FIRMWARE_DOWNLOAD_PROGRESS_REQUEST = 315;
    /**
     * adb
     */
    public static final int IM_ADB_SWITCH_REQUEST = 316;

    /**
     * 切换编程模式
     */
    public static final int IM_SWITCH_CODE_MAO_REQUEST = 340;

    public static final int IM_AVATAR_CONTROL_REQUEST = 317; // avatar 控制指令 该指令直接由video_live 应用处理，主服务无需处理
    public static final int IM_UPLOAD_LOG_REQUEST = 318;//上传log到七牛云
    public static final int IM_PLAY_SQUARE_ACTION_REQUEST = 319;//播放广场动作
    public static final int IM_STOP_SQUARE_ACTION_REQUEST = 320;//播放广场动作

    public static final int IM_GET_CAMREA_PRIVACY_REQUEST = 320;//获取摄像头隐私
    public static final int IM_SET_CAMREA_PRIVACY_REQUEST = 321;//设置摄像头隐私



    /***********************小车相关***********************/
    public static final int IM_JIMU_CAR_QUERY_POWER_REQUEST = 400;
    public static final int IM_JIMU_CAR_CHANGE_DRIVE_MODE_REQUEST = 401;
    public static final int IM_JIMU_CAR_GET_IR_DISTANCE_REQUEST = 402;
    public static final int IM_JIMU_CAR_CONTROL_REQUEST = 403;
    public static final int IM_JIMU_CAR_ROBOT_CHAT_REQUEST = 404;
    public static final int IM_JIMU_CAR_GET_BLE_CAR_LIST_REQUEST = 405;
    public static final int IM_JIMU_CAR_CAR_CHECK_REQUEST = 406;
    public static final int IM_JIMU_CAR_CONNECT_CAR_REQUEST = 407;
    public static final int IM_JIMU_CAR_QUERY_CONNECT_STATE_REQUEST = 408;
    public static final int IM_JIMU_CAR_CHANGE_LEVEL_REQUEST = 409;
    //411 已用
    /***********************小车相关***********************/


    public static final int IM_GET_MULTI_CONVERSATION_STATE_REQUEST = 360;//获取多轮交互的开关状态
    public static final int IM_SET_MULTI_CONVERSATION_STATE_REQUEST = 361;//设置多轮交互的开关状态


    /**************************************后台推送********************************************/
    static public final int IM_OFFLINE_FROM_SERVER_RESPONSE = 2001;//后台推送IM状态变更
    //升级skill,后台推送到终端，检测是否有新版本更新。完整流程：语音询问“有新版本可更新吗？”—>TVS后台—> UBT 升级skill —> Push该命令到机器人
    static public final int IM_CHECK_UPDATE_FROM_SERVICE_RESPONSE = 2002;
    /**************************************主服务推送，预留100个********************************************/
    static public final int IM_APP_INSTALL_STATE_RESPONSE = 1901;
    static public final int IM_ACTIONFILE_DOWNLOAD_STATE_RESPONSE = 1902;

    static public final int IM_AVATAR_STOPPED_RESPONSE = 1903; //退出视频监控推送 由视频监控应用推送
    static public final int IM_AVATAR_FAIL_RESPONSE = 1904;//视频监控跌倒推送 由视频监控应用推送
    static public final int IM_AVATAR_USER_CHANGE_RESPONSE = 1905;//视频监控用户上下线通知 由视频监控应用推送
    static public final int IM_AVATAR_LOWPOWER_TIPS_RESPONSE = 1906; //低电量提示推送(电量30% 推送) 由视频监控应用推送


    //主账号解绑自己
    static public final int IM_ACCOUNT_MASTER_UNBINDED_RESPONSE = 2006;
    //主账号解绑从账号
    static public final int IM_ACCOUNT_BEEN_UNBIND_RESPONSE = 2007;
    //从账号解绑自己
    static public final int IM_ACCOUNT_SLAVER_UNBIND_RESPONSE = 2008;
    //人脸录入、识别相关的语音指令通过后台push下发
    static public final int IM_FACE_COLLECTION_RESPONSE = 2011;
    static public final int IM_FACE_RECOGNITION_RESPONSE = 2012;
    static public final int IM_FACE_BEAUTY_RESPONSE = 2013;

    //绘图识别相关语音指令通过后台push下发
    static public final int IM_BOOK_READING_START_RESPONSE = 2014;
    static public final int IM_BOOK_READING_FINISH_RESPONSE = 2015;
    static public final int IM_START_FRUIT_RECOGNITION_RESPONSE = 2016;
    static public final int IM_START_OBJECT_RECOGNITION_RESOPNSE = 2017;

    //花草识别：
    static public final int IM_FLOWER_RECOGNITION_RESOPNSE = 2018;
    static public final int IM_FLOWER_RECOGNIZER_RESOPNSE = 2019;

    //帮助指引相关指令通过后台Push下发
    static public final int IM_GUIDE_WHAT_CAN_RESOPNSE = 2020;
    static public final int IM_GUIDE_HOW_BIND_RESOPNSE = 2021;
    static public final int IM_GUIDE_HOW_CONNECT_RESOPNSE = 2022;
    static public final int IM_GUIDE_HOW_STOP_RESOPNSE = 2023;
    static public final int IM_GUIDE_HOW_VOLUME_RESOPNSE = 2024;
    static public final int IM_GUIDE_HOW_SHUTOFF_RESOPNSE = 2025;
    static public final int IM_GUIDE_HOW_RESET_RESOPNSE = 2026;
    static public final int IM_GUIDE_HOW_LTE_RESOPNSE = 2027;
    static public final int IM_GUIDE_HOW_PHOTO_RESOPNSE = 2028;
    static public final int IM_GUIDE_HOW_TELEPHONE_RESOPNSE = 2029;
    static public final int IM_GUIDE_HOW_SURVEILLANCE_RESOPNSE = 2030;
    static public final int IM_GUIDE_HOW_PICTURE_RESOPNSE = 2031;
    static public final int IM_GUIDE_HOW_TRANSLATE_RESOPNSE = 2032;
    static public final int IM_GUIDE_HOW_WEATHER_RESOPNSE = 2033;
    static public final int IM_GUIDE_HOW_STOCK_RESOPNSE = 2034;
    static public final int IM_GUIDE_HOW_MUSIC_RESOPNSE = 2035;
    static public final int IM_GUIDE_HOW_STORY_RESOPNSE = 2036;
    static public final int IM_GUIDE_HOW_ALARM_RESOPNSE = 2037;
    static public final int IM_GUIDE_HOW_CHANGE_RESOPNSE = 2038;
    static public final int IM_GUIDE_WIFI_SWITCH_RESOPNSE = 2039;
    static public final int IM_GUIDE_4G_STATU_RESOPNSE = 2040;
    static public final int IM_GUIDE_CHARGE_STATU_RESOPNSE = 2041;
    static public final int IM_GUIDE_WHICH_WIFI_RESOPNSE = 2042;
    static public final int IM_GUIDE_WHO_ADMIN_RESOPNSE = 2043;
    static public final int IM_GUIDE_WHO_BINDER_RESOPNSE = 2044;
    static public final int IM_GUIDE_HOW_BATTERY_RESOPNSE = 2045;
    static public final int IM_GUIDE_HOW_CONTENT_RESOPNSE = 2046;

    /***************************************返回************************************************/
    static public final int IM_CONNECT_ROBOT_RESPONSE = RESPONSE_BASE + IM_CONNECT_ROBOT_REQUEST;

    static public final int IM_DISCONNECT_ROBOT_RESPONSE = RESPONSE_BASE + IM_DISCONNECT_ROBOT_REQUEST;


    static public final int IM_DELETE_ACTIONFILE_RESPONSE = RESPONSE_BASE + IM_DELETE_ACTIONFILE_REQUEST;
    static public final int IM_PLAY_ACTION_RESPONSE = RESPONSE_BASE + IM_PLAY_ACTION_REQUEST;
    static public final int IM_GET_MOTORANGLE_RESPONSE = RESPONSE_BASE + IM_GET_MOTORANGLE_REQUEST;
    static public final int IM_STOP_PLAY_RESPONSE = RESPONSE_BASE + IM_STOP_PLAY_REQUEST;
    static public final int IM_START_APP_RESPONSE = RESPONSE_BASE + IM_START_APP_REQUEST;
    static public final int IM_STOP_APP_RESPONSE = RESPONSE_BASE + IM_STOP_APP_REQUEST;

    static public final int IM_GET_ALLAPPS_RESPONSE = RESPONSE_BASE + IM_GET_ALLAPPS_REQUEST;
    static public final int IM_INSTALL_PACKAGES_RESPONSE = RESPONSE_BASE + IM_INSTALL_PACKAGES_REQUEST;
    static public final int IM_UNINSTALL_PACKAGES_RESPONSE = RESPONSE_BASE + IM_UNINSTALL_PACKAGES_REQUEST;
    static public final int IM_UPDATE_PACKAGES_RESPONSE = RESPONSE_BASE + IM_UPDATE_PACKAGES_REQUEST;

    public static final int IM_GET_FORMER_CLOCK_RESPONSE = RESPONSE_BASE + IM_GET_FORMER_CLOCK_REQUEST;
    public static final int IM_DELETE_FORMER_CLOCK_RESPONSE = RESPONSE_BASE + IM_DELETE_FORMER_CLOCK_REQUEST;
    public static final int IM_QUERY_ROBOT_POWER_RESPONSE = RESPONSE_BASE + IM_QUERY_ROBOT_POWER_REQUEST;
    public static final int IM_QUERY_HARD_SOFT_WARE_VERSION_RESPONSE = RESPONSE_BASE + IM_QUERY_HARD_SOFT_WARE_VERSION_REQUEST;
    public static final int IM_TTS_BREATH_ACTION_ON_OFF_RESPONSE = RESPONSE_BASE + IM_TTS_BREATH_ACTION_ON_OFF_REQUEST;
    public static final int IM_GET_TTS_BREATH_ACTION_ON_OFF_RESPONSE = RESPONSE_BASE + IM_GET_TTS_BREATH_ACTION_ON_OFF_REQUEST;

    public static final int IM_SYN_SPEECH_RESPONSE = RESPONSE_BASE + IM_SYN_SPEECH_REQUEST;
    public static final int IM_CLOSE_ROBOT_ERROR_LOG_RESPONSE = RESPONSE_BASE + IM_CLOSE_ROBOT_ERROR_LOG_REQUEST;
    public static final int IM_UNINSTALL_BATCH_APPS_RESPONSE = RESPONSE_BASE + IM_UNINSTALL_BATCH_APPS_REQUEST;
    public static final int IM_RETRY_PLAY_ANSWER_RESPONSE = RESPONSE_BASE + IM_RETRY_PLAY_ANSWER_REQUEST;
    public static final int IM_QUERY_ROBOT_STORAGE_APP_LIST_RESPONSE = RESPONSE_BASE + IM_QUERY_ROBOT_STORAGE_APP_LIST_REQUEST;

    static public final int IM_GET_APPCONFIG_RESPONSE = RESPONSE_BASE + IM_GET_APPCONFIG_REQUEST;
    static public final int IM_SAVE_APPCONFIG_RESPONSE = RESPONSE_BASE + IM_SAVE_APPCONFIG_REQUEST;
    static public final int IM_GET_TOP_APP_RESPONSE = RESPONSE_BASE + IM_GET_TOP_APP_REQUEST;

    static public final int IM_GET_APP_BUTTONEVENT_RESPONSE = RESPONSE_BASE + IM_GET_APP_BUTTONEVENT_REQUEST;
    static public final int IM_CLICK_APP_BUTTON_RESPONSE = RESPONSE_BASE + IM_CLICK_APP_BUTTON_REQUEST;
    static public final int IM_DOWNLOAD_ACTIONFILE_RESPONSE = RESPONSE_BASE + IM_DOWNLOAD_ACTIONFILE_REQUEST;
    static public final int IM_DESKCLOCK_RESPONSE = RESPONSE_BASE + IM_DESKCLOCK_MANAGE_REQUEST;


    static public final int IM_DESKCLOCKLIST_RESPONSE = RESPONSE_BASE + IM_DESKCLOCK_ACTIVIE_LIST_REQUEST;

    static public final int IM_TRANSFER_PHOTO_RESPONSE = RESPONSE_BASE + IM_TRANSFER_PHOTO_REQUEST;
    static public final int IM_MASTER_CLEAR_RESPONSE = RESPONSE_BASE + IM_MASTER_CLEAR_REQUEST;
    static public final int IM_SET_RTC_TIME_RESPONSE = RESPONSE_BASE + IM_SET_RTC_TIME_REQUEST;
    static public final int IM_FIND_MOBILEPHONE_RESPONSE = RESPONSE_BASE + IM_FIND_MOBILEPHONE_REQUEST;
    static public final int IM_GET_ROBOT_INIT_STATUS_RESPONSE = RESPONSE_BASE + IM_GET_ROBOT_INIT_STATUS_REQUEST;
    static public final int IM_GET_ALL_THUMBNAIL_RESPONSE = RESPONSE_BASE + IM_GET_ALL_THUMBNAIL_REQUEST;
    static public final int IM_GET_NEW_ACTION_LIST_RESPONSE = RESPONSE_BASE + IM_GET_NEW_ACTION_LIST_REQUEST;
    static public final int IM_SET_MASTER_NAME_RESPONSE = RESPONSE_BASE + IM_SET_MASTER_NAME_REQUEST;


    static public final int IM_CONFIRM_ONLINE_RESPONSE = RESPONSE_BASE + IM_CONFIRM_ONLINE_REQUEST;

    public static final int IM_SEND_TVS_ACCESSTOKEN_RESPONSE = RESPONSE_BASE + IM_SEND_TVS_ACCESSTOKEN_REQUEST;
    public static final int IM_GET_TVS_PRODUCTID_RESPONSE = RESPONSE_BASE + IM_GET_TVS_PRODUCTID_REQUEST;

    static public final int IM_GET_SERIAL_NUMBER_RESPONSE = RESPONSE_BASE + IM_GET_SERIAL_NUMBER_REQUEST;
    public static final int IM_GET_ROBOT_CONFIG_RESPONSE = RESPONSE_BASE + IM_GET_ROBOT_CONFIG_REQUEST;
    public static final int IM_SEND_WIFI_TO_ROBOT_RESPONSE = RESPONSE_BASE + IM_SEND_WIFI_TO_ROBOT_REQUEST;
    public static final int IM_GET_WIFI_LIST_TO_ROBOT_RESPONSE = RESPONSE_BASE + IM_GET_WIFI_LIST_TO_ROBOT_REQUEST;
    static public final int IM_GET_ALBUM_LIST_RESPONSE = RESPONSE_BASE + IM_GET_ALBUM_LIST_REQUEST;
    static public final int IM_GET_MOBILE_ALBUM_DOWNLOAD_RESPONSE = RESPONSE_BASE + IM_GET_MOBILE_ALBUM_DOWNLOAD_REQUEST;


    public static final int IM_START_FACE_DETECT_RESPONSE = RESPONSE_BASE + IM_START_FACE_DETECT_REQUEST;//启动人脸识别
    public static final int IM_FACE_UPDATE_RESPONSE = RESPONSE_BASE + IM_FACE_UPDATE_REQUEST; //修改人脸信息
    public static final int IM_FACE_DELETE_RESPONSE = RESPONSE_BASE + IM_FACE_DELETE_REQUEST; //删除人脸信息
    public static final int IM_FACE_LIST_RESPONSE = RESPONSE_BASE + IM_FACE_LIST_REQUEST; //人脸列表信息
    public static final int IM_FACE_EXIT_RESPONSE = RESPONSE_BASE + IM_FACE_EXIT_REQUEST; //退出人脸录入流程
    public static final int IM_FACE_CHECK_STATE_RESPONSE = RESPONSE_BASE + IM_FACE_CHECK_STATE_REQUEST; //检查

    static public final int IM_GET_AGORA_ROOM_INFO_RESPONSE = RESPONSE_BASE + IM_GET_AGORA_ROOM_INFO_REQUEST;

    /***执行行为配置文件****/
    public static final int IM_PLAY_BEHAVIOR_RESPONSE = RESPONSE_BASE + IM_PLAY_BEHAVIOR_REQUEST;
    public static final int IM_GET_BEHAVIOR_LIST_RESPONSE = RESPONSE_BASE + IM_GET_BEHAVIOR_LIST_REQUEST;
    public static final int IM_GET_ROBOT_IP_RESPONSE = RESPONSE_BASE + IM_GET_ROBOT_IP_REQUEST;

    /**
     * ota
     */
    static final int IM_DETECT_UPGRADE_RESPONSE = RESPONSE_BASE + IM_DETECT_UPGRADE_REQUEST;
    static final int IM_FIRMWARE_DOWNLOAD_RESPONSE = RESPONSE_BASE + IM_FIRMWARE_DOWNLOAD_REQUEST;
    static final int IM_FIRMWARE_UPGRADE_RESPONSE = RESPONSE_BASE + IM_FIRMWARE_UPGRADE_REQUEST;
    static final int IM_QUERY_FIRMWARE_DOWNLOAD_PROGRESS_RESPONSE = RESPONSE_BASE + IM_QUERY_FIRMWARE_DOWNLOAD_PROGRESS_REQUEST;

    /**
     * 通讯功能
     */
    public static final int IM_CHECK_SIM_CARD_RESPONSE = RESPONSE_BASE + IM_CHECK_SIM_CARD_REQUEST;
    public static final int IM_IMPORT_PHONE_CONTACT_RESPONSE = RESPONSE_BASE + IM_IMPORT_PHONE_CONTACT_REQUEST;
    public static final int IM_QUERY_CONTACT_LIST_RESPONSE = RESPONSE_BASE + IM_QUERY_CONTACT_LIST_REQUEST;
    public static final int IM_ADD_CONTACT_RESPONSE = RESPONSE_BASE + IM_ADD_CONTACT_REQUEST;
    public static final int IM_MODIFY_CONTACT_RESPONSE = RESPONSE_BASE + IM_MODIFY_CONTACT_REQUEST;
    public static final int IM_DELETECONTACT_RESPONSE = RESPONSE_BASE + IM_DELETECONTACT_REQUEST;
    public static final int IM_QUERY_CALL_RECORD_RESPONSE = RESPONSE_BASE + IM_QUERY_CALL_RECORD_REQUEST;
    public static final int IM_QUERY_MOBILE_NETWORK_RESPONSE = RESPONSE_BASE + IM_QUERY_MOBILE_NETWORK_REQUEST;
    public static final int IM_MODIFY_MOBILE_DATA_RESPONSE = RESPONSE_BASE + IM_MODIFY_MOBILE_DATA_REQUEST;
    public static final int IM_MODIFY_MOBILE_ROAM_RESPONSE = RESPONSE_BASE + IM_MODIFY_MOBILE_ROAM_REQUEST;


    /**
     * adb
     */
    public static final int IM_ADB_CMD_RESPONSE = RESPONSE_BASE + IM_ADB_SWITCH_REQUEST;
    /**
     * 编程猫切换
     */
    public static final int IM_SWITCH_CODE_MAO_RESPONSE = RESPONSE_BASE + IM_SWITCH_CODE_MAO_REQUEST;

    public static final int IM_UPLOAD_LOG_RESPONSE = RESPONSE_BASE + IM_UPLOAD_LOG_REQUEST;
    public static final int IM_PLAY_SQUARE_ACTON_RESPONSE = RESPONSE_BASE + IM_PLAY_SQUARE_ACTION_REQUEST;
    public static final int IM_STOP_SQUARE_ACTON_RESPONSE = RESPONSE_BASE + IM_STOP_SQUARE_ACTION_REQUEST;

    public static final int IM_GET_CAMREA_PRIVACY_RESPONSE = RESPONSE_BASE + IM_GET_CAMREA_PRIVACY_REQUEST;
    public static final int IM_SET_CAMREA_PRIVACY_RESPONSE = RESPONSE_BASE + IM_SET_CAMREA_PRIVACY_REQUEST;


    /*********************************jimu小车*********************************/
    public static final int IM_JIMU_CAR_QUERY_POWER_RESPONSE = RESPONSE_BASE + IM_JIMU_CAR_QUERY_POWER_REQUEST;
    public static final int IM_JIMU_CAR_CHANGE_DRIVE_MODE_RESPONSE = RESPONSE_BASE + IM_JIMU_CAR_CHANGE_DRIVE_MODE_REQUEST;
    public static final int IM_JIMU_CAR_GET_IR_DISTANCE_RESPONSE = RESPONSE_BASE + IM_JIMU_CAR_GET_IR_DISTANCE_REQUEST;
    public static final int IM_JIMU_CAR_CONTROL_RESPONSE = RESPONSE_BASE + IM_JIMU_CAR_CONTROL_REQUEST;
    public static final int IM_JIMU_CAR_ROBOT_CHAT_RESPONSE = RESPONSE_BASE + IM_JIMU_CAR_ROBOT_CHAT_REQUEST;
    public static final int IM_JIMU_CAR_GET_BLE_CAR_LIST_RESPONSE = RESPONSE_BASE + IM_JIMU_CAR_GET_BLE_CAR_LIST_REQUEST;
    public static final int IM_JIMU_CAR_CONNECT_CAR_RESPONSE = RESPONSE_BASE + IM_JIMU_CAR_CONNECT_CAR_REQUEST;
    public static final int IM_JIMU_CAR_CAR_CHECK_RESPONSE = RESPONSE_BASE + IM_JIMU_CAR_CAR_CHECK_REQUEST;
    public static final int IM_JIMU_CAR_QUERY_CONNECT_STATE_RESPONSE = RESPONSE_BASE + IM_JIMU_CAR_QUERY_CONNECT_STATE_REQUEST;
    public static final int IM_JIMU_CAR_CHANGE_LEVEL_RESPONSE = RESPONSE_BASE + IM_JIMU_CAR_CHANGE_LEVEL_REQUEST;
    public static final int IM_JIMU_CAR_CHECK_PREPARED = RESPONSE_BASE + 411;
    /*********************************jimu小车*********************************/


    public static final int IM_GET_MULTI_CONVERSATION_STATE_RESPONSE = RESPONSE_BASE +IM_GET_MULTI_CONVERSATION_STATE_REQUEST;//获取多轮交互的开关状态
    public static final int IM_SET_MULTI_CONVERSATION_STATE_RESPONSE = RESPONSE_BASE+IM_SET_MULTI_CONVERSATION_STATE_REQUEST;//设置多轮交互的开关状态

}
