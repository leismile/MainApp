package com.ubtechinc.alpha.service.sysevent;

import android.os.IBinder;

import com.ubtrobot.masterevent.protos.SysMasterEvent;

/**
 * @author : kevin.liu@ubtrobot.com
 * @description :
 * @date : 2018/6/11
 * @modifier :
 * @modify time :
 */
public class EventHandler {

    int priority;
    String action;
    String uuid;
    int pid;
    String skillName;
    IBinder binder;

    private EventHandler(int priority, String action, String uuid, int pid, IBinder binder,String skillName) {
        this.priority = priority;
        this.action = action;
        this.uuid = uuid;
        this.pid = pid;
        this.binder = binder;
        this.skillName = skillName;
    }

    public int getPriority() {
        return priority;
    }

    public String getAction() {
        return action;
    }

    public String getUuid() {
        return uuid;
    }

    public int getPid() {
        return pid;
    }

    public IBinder getBinder() {
        return binder;
    }

    public String getSkillName() {
        return skillName;
    }

    @Override
    public String toString() {
        return "EventHandler{" +
                "priority=" + priority +
                ", action='" + action + '\'' +
                ", uuid='" + uuid + '\'' +
                ", pid=" + pid +
                ", skillName='" + skillName + '\'' +
                ", binder=" + binder +
                '}';
    }

    public static class Builder {

        private int priority;
        private String action;
        private String uuid;
        private int pid;
        private IBinder binder;
        private String skillName;

        public Builder skillName(String skill){
            this.skillName = skill;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public Builder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder pid(int pid) {
            this.pid = pid;
            return this;
        }

        public Builder binder(IBinder binder) {
            this.binder = binder;
            return this;
        }


        public EventHandler build() {
            return new EventHandler(priority, action, uuid, pid, binder,skillName);
        }
    }
}
