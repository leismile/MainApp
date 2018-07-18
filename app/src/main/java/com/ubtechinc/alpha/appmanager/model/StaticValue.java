package com.ubtechinc.alpha.appmanager.model;

import android.os.Environment;

import com.ubtrobot.mini.properties.sdk.PropertiesApi;

/**
 * #####主服务和ubt应用之间使用的常量####
 *
 * @date 2017/07/26
 * @author wzx@ubtrobot.com
 * @Description 用于常量等基本定义
 * @modifier logic.peng@ubtrobot.com
 * @modify_time
 */
public class StaticValue {
	/**
	 * Socket服务端口
	 */
	public final static int SERVER_PORT = 6100;
	/**
	 * 舵机处理Socket端口
	 */
	public final static int MOTOR_SOCKET_PORT = 6102;

	/**
	 * ACTION FILE 存储路径
	 */
	// public static final String ACTION_PATH = "/mnt/external1/actions/";
	/**
	 * PHOTO FILE 存储路径
	 */
	public static String PHOTO_PATH = "/mnt/shell/emulated/0/photos/";
	/**
	 * SOCKET通讯标志
	 */
	public static final short SOCKET_FLAG = 0x1234;
	/**
	 * SOCKET协议版本号
	 */
	public static final short SOCKET_VERSION = 0x0001;

	/** 接收讯飞语音 语音初始化完成 **/
	public static final String SPEECHINIT_ACTION = "com.ubtechinc.services.speechclientinit";
	/** 接收讯飞语音 语音命令广播 **/
	public static final String SPEECHCMD_ACTION = "com.ubtechinc.services.speechcmd";

	/** 发送蓝牙音频切换广播 **/
	public static final String AUDIODEV_ACTION = "com.ubtechinc.services.audiodev";
	/** 发送蓝牙联系人更新广播 **/
	public static final String UPLOADCONTACTS_ACTION = "com.ubtechinc.services.uploadcontactslexicon";
	/** 发送给讯飞语音 语音命令更新广播 **/
	public static final String UPLOADALPHACMD_ACTION = "com.ubtechinc.services.uploadalphacmdlexicon";
	/** 服务基本的广播消息处理 */
	public static final String SERVICE_BASE_ACTION = "com.ubtechinc.services.baseaction";
	/** 命令词更新完成广播 **/
	public static final String UPLOADALPHACMDOVER_ACTION = "com.ubtechinc.services.uploadoveralphacmdlexicon";
	/** 停止外部TTS **/
	public static final String STOPTTS_ACTION = "com.ubtechinc.services.stoptts";
	/** 事件标志 */
	public static final String BASE_ACTION_EVENT = "base_event";
	/** wifi 连接AP改变事件 */
	public static final String BASE_ACTION_KEY_WIFICONNECTCHANGE = "wifi_connect_change";
	/** 关闭舵机电源 */
	public static final String BASE_ACTION_KEY_CLOSEMOTORPOWER = "close_motor_power";
	/** 蓝牙spp连接广播 **/
	public static final String BLUETOOTH_CONNECED_ACTION = "com.ubtechinc.services.bluetoothclientconnected";
	/** 蓝牙SPP断开 **/
	public static final String BLUETOOTH_DISCONNECTED_ACTION = "com.ubtechinc.services.bluetoothclientdisconnected";
	/** 蓝牙数据广播 **/
	public static final String BLUETOOTH_SENDDATA_ACTION = "com.ubtechinc.services.bluetoothclientdata";
	/** 蓝牙打电话广播 **/
	public static final String ALPHA_PHONE_ACTION = "com.ubtechinc.services.phone";
	/** 传递号码 **/
	public static final String ALPHA_PHONE_NUM = "com.ubtechinc.services.phone.num";
	/** 更新胸部软件 **/
	public static final String ALPHA_UPDATE_CHESSOFTWARE = "com.ubtechinc.update.chess";
	/** 更新头部软件 **/
	public static final String ALPHA_UPDATE_HEADERSOFTWARE = "com.ubtechinc.udate.header";
	/** 蓝牙连接广播 **/
	public static final String ALPHA_BT_CONNECTION = "com.ubtechinc.services.bluetooth";
	/** 手势aciton **/
	public static final String ALPHA_GESTURE_ACTION = "come.ubt.alpha2.gesture";
	/** 手势方向 **/
	public static final String ALPHA_GESTURE_DIRECTION = "getstureDirection";
	/** 手势开启录音，相当于按钮触发 **/
	public static final String ALPHA_GESTURE_ACTION_REC = "come.ubt.alpha2.gesture.rec";
	/** 蓝牙闹钟事件 **/
	public static final String ALPHA_BT_ALARM = "com.ubt.alpha2.bt.alarm";
	/** 二维码扫描结果反馈 **/
	public static final String ALPHA_QR_CODE = "com.ubt.alpha2.qr_code";
	/** 停止二维码扫面 **/
	public static final String ALPHA_QR_CODE_CANCLE = "com.ubt.alpha2.qr_code.cancle";
	/** 拍完照后请求将照片发送到客户端 **/
	public final static String UPLOAD_PHOTO_BY_SERVICE = "com.ubtrobot.action.transfer_photo";
	/** 照片的存储路径 **/
	public final static String PHOTO_PATH_KEY = "photo_path";

	public static final String SCHEME = "ubtechinc";
	public static final String HOST = "com.ubtechinc";

	/**==============begin:ubtech 内置应用的包名====================**/
	public static final String CHAT_PACKAGE_NAME = "com.ubtech.iflytekmix";
	public static final String TRANSLATION_PACKAGE_NAME = "com.ubtechinc.alphatranslation";
	public static final String SMARTCAMERA_PACKAGE_NAME = "om.ubtech.smartcamera";
	/**==============end:ubtech 内置应用的包名====================**/


	/**==============begin:内置应用间通过广播字段====================*/
	public static final String ACTION_UBT_APP_EXIT = "com.ubtechinc.action.closeapp";
	public static final String ACTION_REPLAY_BUSINESS = "com.ubtechinc.services.REPLAY_BUSINESS";
	/** app ---> server---->third party read it's app config **/
	public static final String APP_CONFIG = "appconfig";
	/** third party --->server --->app for read app config **/
	public static final String APP_CONFIG_BACK = "appconfigback";
	/** app--->server--->third party save it's app config **/
	public static final String APP_CONFIG_SAVE = "appconfigsave";
	/** server--->read third party app button event config **/
	public static final String APP_BUTTON_EVENT = "buttonevent";
	/** third party app button event--->server---> app **/
	public static final String APP_BUTOON_EVENT_BACK = "buttonback";
	/** app--->server--->third party app button click **/
	public static final String APP_BUTOON_EVENT_CLICK = "buttonclick";
	/**==============end:内置应用间通过广播字段====================*/
	public static final String APP_SEND_AGORA_ROOMINFO ="com.ubtechinc.action.agora.roominfo";
	public static final String APP_LEVAE_AGORA_ROOMINFO ="com.ubtechinc.action.leave.agora.roominfo";
}
