package com.ubtechinc.contact.model;

import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * @desc : 通话记录model，提供通讯录的增查
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/1/31
 */

public class CallRecordModel {

    private static final String TAG = "CallRecordModel";
    private Context context;
    private CallRecordInfoDao callRecordInfoDao;

    public CallRecordModel(Context context) {
        this.context = context.getApplicationContext();
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(context, "callrecord.db", null);
        DaoMaster daoMaster = new DaoMaster(devOpenHelper.getWritableDb());
        DaoSession daoSession = daoMaster.newSession();
        callRecordInfoDao = daoSession.getCallRecordInfoDao();
    }

    public long add(CallRecordInfo callRecordInfo) {
        callRecordInfoDao.insert(callRecordInfo);
        List<CallRecordInfo> list = callRecordInfoDao.queryBuilder().where(CallRecordInfoDao.Properties.DateLong.eq(callRecordInfo.getDateLong())).where(CallRecordInfoDao.Properties.Duration.eq(callRecordInfo.getDuration())).build().list();
        if(list != null && list.size() != 0) {
            return list.get(0).getVersionCode();
        }
        Log.e(TAG, " add error");
        return 0;
    }

    public List<CallRecordInfo> query(long versionCode) {
        Log.d(TAG, " query -- versionCode : " + versionCode);
        List<CallRecordInfo> list = callRecordInfoDao.queryBuilder().where(CallRecordInfoDao.Properties.VersionCode.ge(versionCode + 1)).build().list();
        if(list == null || list.isEmpty()) {
            List<CallRecordInfo> list1 = callRecordInfoDao.loadAll();
            Log.d(TAG, " query -- size " + list.size() + " all : " + list1);
        }
        return list;
    }
}
