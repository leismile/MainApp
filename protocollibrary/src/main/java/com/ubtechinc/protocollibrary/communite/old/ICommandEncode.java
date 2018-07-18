package com.ubtechinc.protocollibrary.communite.old;

/**
 * @desc : 加密和拆分编码接口
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/4/3
 */

public interface ICommandEncode {

    /**
     * 加密数据
     * @param content 待加密数据
     * @return 加密后的数据
     */
    byte[] encryption(String content);

    /**
     * 拆分成byte数组
     * @param content 待传输数据
     * @return 拆分后数据
     */
    byte[][] encode(byte[] content);

    /**
     * 添加待解析数据
     * @param data 数据
     * @return 是否添加完成
     */
    boolean addData(byte[] data);

    /**
     * 获取最新的指令
     * @return 指令
     */
    String getCommand();
}
