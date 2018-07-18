package com.ubtechinc.protocollibrary.communite.old;

import android.net.wifi.ScanResult;

import java.util.List;

/**
 * @desc : 命令生成接口
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/4/3
 */

public interface ICommandProduce {
    /**
     *  获取wifi连接成功指令
     * @return 指令
     */
    String getWifiSuc(String serialNumber, String productId);

    /**
     * 获取连接成功指令
     * @return 指令
     */
    String getConnectSuc(int code);

    /**
     * 获取wifi有效指令
     * @param serialNumber 序列号
     * @param productId 产品ID
     * @return 指令
     */
    String getWifiAvailable(String serialNumber, String productId);

    /**
     * 获取wifi无效指令
     * @return 指令
     */
    String getWifiInvalid();

    /**
     *  获取Wifi列表
     * @param list wifi列表
     * @return 指令
     */
    String getWifiList(List<ScanResult> list);

    /**
     * 获取绑定成功指令
     * @return 指令
     */
    String getBindingSuc();

    /**
     * 获取错误码指令
     * @param errorCode 错误码
     * @return 指令
     */
    String getErrorCode(int errorCode);
}
