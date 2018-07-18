package com.ubtechinc.bluetoothrobot.old;

/**
 * @author：wululin
 * @date：2017/10/24 15:10
 * @modifier：ubt
 * @modify_date：2017/10/24 15:10
 * [A brief description]
 * version
 */

public class BleConstants {

    public static String JSON_COMMAND="co";

    public static String JSON_SSID="s";
    public static String JSON_PWD="p";
    public static String JSON_SECURE="c";

    public static String LEVEL= "l";
    public static String SSID =  "s";
    public static String CAPABILITIES ="c";

    /**
     * 手机端发送获取WiFi列表指令
     */
    public static int WIFI_LIST_TRANS = 1;

    /**
     * 机器人端向手机端发送wifi列表
     */
    public static int WIFI_LIST_TO_MOBILE_TRANS = 101;

    /**
     * 收到手机端发送过来的名称
     */
    public static int WIFI_NAME_TRANS=2;

    /**
     * 收到手机端发送过来的名称
     */
    public static int WIFI_INFO_TRANS_FOR_BINDING =21;

    /**
     * 机器人联网成功
     */
    public static int CONNECT_WIFI_SUCCESS_TRANS = 102;

    /**
     * 收到手机端发送过来clientid指令
     */
    public static int CLIENTID_TRANS = 3;

    /**
     * 绑定成功指令
     */
    public static int BINGDANG_SUCCESS_TRANS = 103;


    public static final int CONNECT_SUCCESS = 4;//蓝牙连接成功指令

    public static final int ROBOT_CONNECT_SUCCESS = 104; //机器人回复手机端连接成功指令

    public static final int ROBOT_IS_WIFI_TRANS = 5;

    public static final int REPLY_ROBOT_IS_WIFI_TRANS = 105;

    public static final int BLE_DISCONNECT_TRANS = 6;//蓝牙连接断开指令

    public static final int ROBOT_NETWORK_NOT_AVAILABLE = 7;//手机网络无效

    /**
     * 机器人蓝牙配网失败
     */
    public static int BLE_NETWORK_ERROR = -1;

    public static final String WIFILIST = "p";

    public static final String PRODUCTID = "pid";

    public static final String SERISAL_NUMBER = "sid";

    public static final String CLIENTID = "cid";

    public static final String QM = "qm";  //该机器人是否已领取音乐vip权限


    /**
     * WiFi连接失败错误码
     */
    public static int CONNENT_WIFI_FIALED_ERROR_CODE = 0;

    /**
     * WiFi密码不合法错误码
     */
    public static int PASSWORD_VALIDATA_ERROR_CODE = 1;

    /**
     * ping 网址失败的错误码
     */
    public static int PING_ERROR_CODE = 2;

    /**
     * 登录TVS错误码
     */
    public static int TVS_LOGIN_ERROR_CODE = 3;

    /**
     * 连接WiFi超时错误码
     */
    public static int CONNECT_TIME_OUT_ERROR_CODE = 4;

    /**
     * 已经有设备连接机器人错误码
     */
    public static int ALEARDY_CONNECT_ERROR_CODE = 5;


    /**
     * 注册tvs失败错误码
     */
    public static int TVS_ERROR_CODE = 6;
    public static String RESPONSE_CODE = "ec";

    public static String CODE = "cd";

    public static int WIFI_OK = 1;
    public static int WIFI_NOT_OK = 0;
}
