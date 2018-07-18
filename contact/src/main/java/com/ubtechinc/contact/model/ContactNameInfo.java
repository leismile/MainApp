package com.ubtechinc.contact.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @desc : 联系人实体对象
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/2/27
 */

@Entity
public class ContactNameInfo {
    @Id(autoincrement = true)
    private Long contactId;
    @Property(nameInDb = "ContactName")
    private String contactName;
    @Generated(hash = 565493102)
    public ContactNameInfo(Long contactId, String contactName) {
        this.contactId = contactId;
        this.contactName = contactName;
    }
    @Generated(hash = 967569407)
    public ContactNameInfo() {
    }
    public Long getContactId() {
        return this.contactId;
    }
    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }
    public String getContactName() {
        return this.contactName;
    }
    public void setContactName(String contactName) {
        this.contactName = contactName;
    }
}
