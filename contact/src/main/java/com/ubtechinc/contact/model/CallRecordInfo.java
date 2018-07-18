package com.ubtechinc.contact.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @desc : 通话记录数据库实体对象
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/2/23
 */

@Entity
public class CallRecordInfo {
    @Property(nameInDb = "TYPE")
    private int type;
    @Property(nameInDb = "DURATION")
    private long duration;
    @Property(nameInDb = "DATELONG")
    private long dateLong;
    @Property(nameInDb = "NAME")
    private String name;
    @Id(autoincrement = true)
    private Long versionCode;
    @Generated(hash = 222724061)
    public CallRecordInfo(int type, long duration, long dateLong, String name,
            Long versionCode) {
        this.type = type;
        this.duration = duration;
        this.dateLong = dateLong;
        this.name = name;
        this.versionCode = versionCode;
    }
    @Generated(hash = 1288953305)
    public CallRecordInfo() {
    }
    public int getType() {
        return this.type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public long getDuration() {
        return this.duration;
    }
    public void setDuration(long duration) {
        this.duration = duration;
    }
    public long getDateLong() {
        return this.dateLong;
    }
    public void setDateLong(long dateLong) {
        this.dateLong = dateLong;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Long getVersionCode() {
        return this.versionCode;
    }
    public void setVersionCode(Long versionCode) {
        this.versionCode = versionCode;
    }
}
