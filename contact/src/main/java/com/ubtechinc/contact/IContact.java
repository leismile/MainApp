package com.ubtechinc.contact;

import com.ubtechinc.alpha.CmQueryContactList;
import com.ubtechinc.contact.model.UserContact;

import java.util.List;

/**
 * @desc : 通讯录变更查询接口
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/1/31
 */

public interface IContact extends IContactQuery {

    /**
     *  批量导入联系人数据
     * @param cmContactInfos 当前联系人
     * @return
     */
    int importContact(List<CmQueryContactList.CmContactInfo> cmContactInfos, String userId);

    /**
     * 获取指定页联系人数据
     * @param position 获取页码
     * @param versionNumber 当前客户端版本号，获取大于此版本号的数据
     * @return 返回指定页电话数据
     */
    List<CmQueryContactList.CmContactInfo> queryContactList(int position, long versionNumber);

    /**
     * 添加联系人
     * @param cmContactInfo 添加联系人信息
     * @return 添加是否成功
     */
    long addContact(CmQueryContactList.CmContactInfo cmContactInfo, String userId);

    /**
     *  修改联系人
     * @param contactId 联系人ContactId
     * @param name 修改后的名称
     * @param phone 修改后的电话号码
     * @return 返回码
     */
    int modifyContact(long contactId, String name, String phone, String userId);

    /**
     * 删除联系人
     * @param contactId 删除联系人ID
     * @return 返回码
     */
    int deleteContact(long contactId, String userId);

    /**
     *  获取版本号
     * @return 版本号
     */
    long getVersionCode();

    /**
     *  获取满足版本号的总页数
     * @return 版本号
     */
    int getTotalPage(long versionNumber);

    /**
     * 查询名字对应号码
     * @param name 用户名称
     * @return 返回号码列表
     */
    List<UserContact> getPhoneNumber(String name);
}
