package com.ubtechinc.contact;

import com.ubtechinc.alpha.CmQueryCallRecord;

import java.util.List;

/**
 * @desc : 通话记录查询接口
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/1/31
 */

public interface ICallRecord extends ICallRecordUpdate {
    /**
     *  查询通话记录
     * @param position 获取页码位置
     * @return 返回该页码的通话记录
     */
    List<CmQueryCallRecord.CmCallRecordInfo> queryCallRecord(int position, int versionCode);

    /**
     *  获取满足versionCode的通话记录页数
     * @param versionCode 版本号
     * @return 页数
     */
    int getCallRecordSize(int versionCode);
}
