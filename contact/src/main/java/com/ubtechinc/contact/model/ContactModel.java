package com.ubtechinc.contact.model;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @desc : 通讯录model，提供通讯录的增删改查
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/1/31
 */

public class ContactModel {

    private static final String TAG = "ContactModel";
    private Context context;
    private ContactInfoDao contactInfoDao;

    public ContactModel(Context context) {
        Log.d(TAG, " ContactModel context : " + context);
        this.context = context.getApplicationContext();
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(context, "contact.db", null);
        DaoMaster daoMaster = new DaoMaster(devOpenHelper.getWritableDb());
        DaoSession daoSession = daoMaster.newSession();
        contactInfoDao = daoSession.getContactInfoDao();
    }

    public long add(ContactInfo contactInfo) {
        // 同名过滤
        Log.d(TAG, " add -- 1");
        List<ContactInfo> contactInfoList = queryPhoneNumber(contactInfo.getName());
        if (contactInfoList != null && contactInfoList.size() > 0) {
            for (ContactInfo contactInfo1 : contactInfoList) {
                if (contactInfo1.getPhone().equals(contactInfo.getPhone()) && !contactInfo1.getIsDelete()) {
                    return -1;
                }
            }
        }
        return contactInfoDao.insert(contactInfo);
    }

    public boolean update(ContactInfo contactInfo) {
        // TODO 获取插入数据的ContactId
        contactInfoDao.update(contactInfo);
        return true;
    }

    public ContactInfo getContactInfo(long contactId) {
        List<ContactInfo> list = contactInfoDao.queryBuilder().where(ContactInfoDao.Properties.ContactId.eq(contactId)).list();
//        list = contactInfoDao.queryBuilder().where(ContactInfoDao.Properties.ContactId.eq(contactId)).list();
        Log.d(TAG, " getContactInfo -- list : " + list);
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    public void delete(Long contactId) {
        contactInfoDao.deleteByKey(contactId);
    }

    public void modify(ContactInfo contactInfo) {
        contactInfoDao.update(contactInfo);
    }

    public List<ContactInfo> query(long versionCode) {
        Log.d(TAG, " query -- versionCode : " + versionCode);
        List<ContactInfo> list = contactInfoDao.queryBuilder().where(ContactInfoDao.Properties.VersionCode.ge(versionCode + 1)).build().list();
        // TODO test
        if (list == null || list.isEmpty()) {
            List<ContactInfo> list1 = contactInfoDao.loadAll();
            Log.d(TAG, " query -- size " + list.size() + " all : " + list1);
        }
        return list;
    }

    public List<UserContact> query(String name) {
        List<ContactInfo> list = contactInfoDao.queryBuilder().where(ContactInfoDao.Properties.Name.eq(name)).build().list();
        // TODO test
        if (list == null || list.isEmpty()) {
        }
        List<UserContact> list1 = new ArrayList<>();
        for (ContactInfo contactInfo : list) {
            list1.add(new UserContact(contactInfo.getName(), contactInfo.getPhone()));
        }
        return list1;
    }

    public String containsPhoneNumber(String phoneNumber) {
        List<ContactInfo> list = contactInfoDao.queryBuilder().where(ContactInfoDao.Properties.Phone.eq(phoneNumber)).build().list();
        return list != null && !list.isEmpty() ? list.get(0).getName() : null;
    }

    public List<ContactInfo> queryPhoneNumber(String name) {
        List<ContactInfo> list = contactInfoDao.queryBuilder().where(ContactInfoDao.Properties.Name.eq(name)).build().list();
        return list;
    }
}
