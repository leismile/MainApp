package com.ubtechinc.contact;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.ubtechinc.alpha.CmQueryContactList;
import com.ubtechinc.contact.model.ContactInfo;
import com.ubtechinc.contact.model.ContactModel;
import com.ubtechinc.contact.model.UserContact;
import com.ubtechinc.contact.util.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @desc : 默认通讯录变更查询接口实现
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/1/31
 */

// TODO 同步到通讯录;优化分页查询时重读读取问题
public class DefaultContact implements IContact, IVersionCodeRequest{

    // 记录正在操作的userId，userId为空时才能操作
    private volatile String userId;
    private Object object = new Object();
    private static final String TAG = "DefaultContact";
    private final String KEY_VERSION_CODE = "VERSION_CODE";
    private final static int PAGE_CAPACITY = 200;
    private Context context = Contact.getInstance().getContext();
    private volatile int versionCode = -1;
    private ContactModel contactModel = new ContactModel(Contact.getInstance().getContext());
    private List<ContactInfo> cmContactInfoListCache;
    private List<ContactInfo> contactInfoList;
    private Uri contentProvider = Uri.parse("content://com.provider.ubtechinc.contact/key_contact/0");
    private static final String KEY_CONTACT_ADD = "key_contact_add";
    private static final String KEY_CONTACT = "key_contact";
    private static final String KEY_CONTACT_DELTE = "key_contact_delete";


    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void clearUserId() {
        this.userId = null;
    }

    public DefaultContact() {
        ContactInfo.setVersionCodeRequest(this);
        initVersionCode();
        contactInfoList = contactModel.query(0);
    }

    @Override
    public int importContact(List<CmQueryContactList.CmContactInfo> cmContactInfos, String userId) {
        synchronized (object) {
            if (this.userId != null && !this.userId.equals(userId)) {
                return ResultConstans.RESULT_OPTION_CONFILCT;
            } else {
                setUserId(userId);
            }
        }
        updateVersionCode();
        for(CmQueryContactList.CmContactInfo cmContactInfo : cmContactInfos) {
            ContactInfo contactInfo = transfer(cmContactInfo);
            long contactId = contactModel.add(contactInfo);
            if(contactId != -1) {
                contactInfo.setContactId(contactId);
                contactInfoList.add(contactInfo);
                notifyAdd(contactInfo.getName());
            }
        }
        clearUserId();
        return ResultConstans.RESULT_SUCCESS;
    }

    @Override
    public List<CmQueryContactList.CmContactInfo> queryContactList(int position, long versionNumber) {
        cmContactInfoListCache = getContactList(versionNumber);
        return subList(cmContactInfoListCache, position);
    }

    private List<ContactInfo> getContactList(long versionNumber) {
        List<ContactInfo> list = new ArrayList<>();
        for(ContactInfo contactInfo : contactInfoList) {
            if(contactInfo.getVersionCode() > versionNumber) {
                list.add(contactInfo);
            }
        }
        return list;
    }

    private List<CmQueryContactList.CmContactInfo> subList(List<ContactInfo> contactInfoList, int position) {
        List<CmQueryContactList.CmContactInfo> list = new ArrayList<>(PAGE_CAPACITY);
        int count = contactInfoList.size();
        if(count > position * PAGE_CAPACITY) {
            int index = 0;
            for(int i = position * PAGE_CAPACITY ;i < count; i ++) {
                if(++index > PAGE_CAPACITY) {
                    break;
                }
                list.add(transfer(contactInfoList.get(i)));
            }
        }
        Log.d(TAG, "query -- list : " + list.size());
        for(CmQueryContactList.CmContactInfo cmContactInfo : list) {
            Log.d(TAG, "query -- count1 : getIsDelete" + cmContactInfo.getIsDelete() + " name : " + cmContactInfo.getName() + " phone : " + cmContactInfo.getPhone() + " id : " + cmContactInfo.getContactId());
        }
        return list;
    }

    @Override
    public long addContact(CmQueryContactList.CmContactInfo cmContactInfo, String userId) {
        contactModel.getContactInfo(2);
        synchronized (object) {
            if (this.userId != null && !this.userId.equals(userId)) {
                return ResultConstans.RESULT_OPTION_CONFILCT;
            } else {
                setUserId(userId);
            }
        }
        updateVersionCode();
        ContactInfo contactInfo = transfer(cmContactInfo);
        long contactId = contactModel.add(transfer(cmContactInfo));
        if(contactId != -1) {
            contactInfo.setContactId(contactId);
            contactInfoList.add(contactInfo);
            notifyAdd(contactInfo.getName());
            Log.d(TAG, "addContact -- transfer(cmContactInfo) : " + transfer(cmContactInfo));
        }
        clearUserId();
        return contactId;
    }

    @Override
    public int modifyContact(long contactId, String name, String phone, String userId) {
        synchronized (object) {
            if (this.userId != null && !this.userId.equals(userId)) {
                return ResultConstans.RESULT_OPTION_CONFILCT;
            } else {
                setUserId(userId);
            }
        }
        updateVersionCode();
        int result = ResultConstans.RESULT_SUCCESS;
        ContactInfo contactInfoOld = contactModel.getContactInfo(contactId);
        if(contactInfoOld != null){
            String oldName = contactInfoOld.getName();
            ContactInfo contactInfo = ContactInfo.newInstance();
            contactInfo.setContactId(contactId);
            contactInfo.setName(name);
            contactInfo.setPhone(phone);
            contactModel.update(contactInfo);
            deleteById(contactId);
            contactInfoList.add(contactInfo);
            if(!oldName.equals(name)) {
                notifyDelete(oldName);
                notifyAdd(name);
            }
        } else {
            result = ResultConstans.RESULT_INVALID_PARAMS;
        }
        clearUserId();
        return result;
    }

    private void deleteById(long contactId) {
        Iterator<ContactInfo> infoIterable = contactInfoList.iterator();
        while (infoIterable.hasNext()) {
            ContactInfo contactInfo = infoIterable.next();
            if(contactInfo.getContactId().equals(contactId)) {
                infoIterable.remove();
                return ;
            }
        }
    }

    @Override
    public int deleteContact(long contactId, String userId) {
        synchronized (object) {
            if (this.userId != null && !this.userId.equals(userId)) {
                return ResultConstans.RESULT_OPTION_CONFILCT;
            } else {
                setUserId(userId);
            }
        }
        ContactInfo contactInfo = contactModel.getContactInfo(contactId);
        if(contactInfo != null) {
            updateVersionCode();
            contactInfo = ContactInfo.newInstance(contactModel.getContactInfo(contactId));
            contactInfo.setIsDelete(true);
            contactModel.update(contactInfo);
            deleteById(contactId);
            contactInfoList.add(contactInfo);
            notifyDelete(contactInfo.getName());
            clearUserId();
            return ResultConstans.RESULT_SUCCESS;
        }
        clearUserId();
        return ResultConstans.RESULT_INVALID_PARAMS;
    }

    private ContactInfo transfer(CmQueryContactList.CmContactInfo cmContactInfo) {
        ContactInfo contactInfo = ContactInfo.newInstance();
        contactInfo.setName(cmContactInfo.getName());
        contactInfo.setPhone(cmContactInfo.getPhone());
        contactInfo.setVersionCode(getVersionCode());
        return contactInfo;
    }

    private CmQueryContactList.CmContactInfo transfer(ContactInfo contactInfo) {
        Log.d(TAG, " transfer : contactInfo : " + contactInfo);
        CmQueryContactList.CmContactInfo cmContactInfo= CmQueryContactList.CmContactInfo.newBuilder().
                setContactId(contactInfo.getContactId()).setIsDelete(contactInfo.getIsDelete() ? 1 : 0).setName(contactInfo.getName()).setPhone(contactInfo.getPhone()).build();
        return cmContactInfo;
    }

    /**
     * 更新版本号
     */
    private void updateVersionCode() {
        SharedPreferenceUtil.saveInt(context, KEY_VERSION_CODE, ++versionCode);
        Log.d(TAG, " updateVersionCode -- versionCode : " + versionCode);
    }

    public void initVersionCode() {
        versionCode = SharedPreferenceUtil.readInt(context, KEY_VERSION_CODE);
        Log.d(TAG, " getVersionCode -- versionCode : " + versionCode);
    }

    public long getVersionCode() {
        Log.d(TAG, " getVersionCode -- versionCode : " + versionCode);
        if(versionCode == -1) {
            versionCode = SharedPreferenceUtil.readInt(context, KEY_VERSION_CODE);
        }
        return (long)versionCode;
    }

    @Override
    public int getTotalPage(long versionNumber) {
        int size = getContactList(versionNumber).size();
        return size == 0 ? size : size / PAGE_CAPACITY + 1;
    }

    @Override
    public List<UserContact> getPhoneNumber(String name) {
        List<UserContact> userContactList = new ArrayList<>();
        for(ContactInfo contactInfo : contactInfoList) {
            if(contactInfo.getName().equals(name) && !contactInfo.getIsDelete()) {
                userContactList.add(new UserContact(contactInfo.getName(), contactInfo.getPhone()));
            }
        }
        return userContactList;
    }

    @Override
    public String containsPhoneNumber(String phoneNumber) {
        for(ContactInfo contactInfo : contactInfoList) {
            if(contactInfo.getPhone().equals(phoneNumber) && !contactInfo.getIsDelete()) {
                return contactInfo.getName();
            }
        }
        return null;
    }

    private void notifyAdd(String name){
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.call(contentProvider, KEY_CONTACT_ADD, name, null);
    }

    private void notifyDelete(String name){
        ContentResolver contentResolver = context.getContentResolver();
        Bundle bundle = contentResolver.call(contentProvider, KEY_CONTACT_DELTE, name, null);
    }


    @Override
    public List<String> queryPhoneNumber(String name) {
        List<String> phoneList = new ArrayList<>();
        for(ContactInfo contactInfo : contactInfoList) {
            if(contactInfo.getName().equals(name)) {
                phoneList.add(contactInfo.getPhone());
            }
        }
        return phoneList;
    }
}
