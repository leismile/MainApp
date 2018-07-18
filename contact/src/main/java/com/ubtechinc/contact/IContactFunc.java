package com.ubtechinc.contact;

/**
 * @desc : 通讯功能接口
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/1/31
 */

public interface IContactFunc extends IContact, ICallRecord {
    /**
     * 判断SIM卡是否存在
     * @return SIM是否存在
     */
    boolean simExist();

    /**
     * 获取电话号码
     * @return 电话号码
     */
    String getSimNumber();

    /**
     *  查询移动网络是否打开
     * @return 是否打开
     */
    boolean isOpenData();

    /**
     *  查询数据漫游是否打开
     * @return 是否打开
     */
    boolean isOpenRoam();

    /**
     * 修改移动网络状态
     * @param isOpen 是否打开
     * @return 操作是否成功
     */
    boolean modifyDataStatus(boolean isOpen);

    /**
     * 修改数据漫游状态
     * @param isOpen 是否打开
     * @return 操作是否成功
     */
    boolean modifyRoam(boolean isOpen);

    /**
     * 接听电话
     */
    void phoneAnswer();

    /**
     * 拒接电话
     */
    void endCall();
}
