package com.ubtechinc.contact.model;

import com.ubtechinc.contact.IVersionCodeRequest;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @desc : 通讯录数据库实体对象
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/1/31
 */

@Entity
public class ContactInfo {
    @Id(autoincrement = true)
    Long contactId;
    @Property(nameInDb = "NAME")
    String name;
    @Property(nameInDb = "PHONE")
    String phone;
    @Property(nameInDb = "NAMEPINYIN")
    String namePinYin;
    @Property(nameInDb = "VERSIONCODE")
    Long versionCode;
    @Property(nameInDb = "ISDELETE")
    boolean isDelete;
    private static IVersionCodeRequest versionCodeRequest;

    @Generated(hash = 2075324813)
    public ContactInfo(Long contactId, String name, String phone, String namePinYin,
            Long versionCode, boolean isDelete) {
        this.contactId = contactId;
        this.name = name;
        this.phone = phone;
        this.namePinYin = namePinYin;
        this.versionCode = versionCode;
        this.isDelete = isDelete;
    }

    @Generated(hash = 2019856331)
    public ContactInfo() {
    }

    public static ContactInfo newInstance() {
        ContactInfo contactInfo = new ContactInfo();
        contactInfo.versionCode = getNewVersionCode();
        return contactInfo;
    }

    public static ContactInfo newInstance(ContactInfo contactInfo) {
        contactInfo.versionCode = getNewVersionCode();
        return contactInfo;
    }

    public static void setVersionCodeRequest(IVersionCodeRequest versionCodeRequest) {
        ContactInfo.versionCodeRequest = versionCodeRequest;
    }

    private static long getNewVersionCode() {
        if(versionCodeRequest != null) {
            return versionCodeRequest.getVersionCode();
        }
        return 0;
    }

    public Long getContactId() {
        return this.contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNamePinYin() {
        return this.namePinYin;
    }

    public void setNamePinYin(String namePinYin) {
        this.namePinYin = namePinYin;
    }

    public Long getVersionCode() {
        return this.versionCode;
    }

    public void setVersionCode(Long versionCode) {
        this.versionCode = versionCode;
    }

    public boolean getIsDelete() {
        return this.isDelete;
    }

    public void setIsDelete(boolean isDelete) {
        this.isDelete = isDelete;
    }

    @Override
    public String toString() {
        return "ContactInfo{" +
                "contactId=" + contactId +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", namePinYin='" + namePinYin + '\'' +
                ", versionCode=" + versionCode +
                ", isDelete=" + isDelete +
                '}';
    }
}
