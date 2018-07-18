package com.ubtechinc.nets.im.event;

/**
 * @author：wululin
 * @date：2017/11/21 17:26
 * @modifier：ubt
 * @modify_date：2017/11/21 17:26
 * [A brief description]
 * version
 */

public class BatteryStateChange {

    private int statu;//电池状态，充电、放电、充满、未充电
    private int health;// "未知错误""状态良好";"电池没有电";"电池电压过高";"电池过热";
    private int level;//电池电量百分比
    private int pluggedp;// 电池的充电方式 AC或者USB
    private int temperature;//电池的温度

    public int getStatu() {
        return statu;
    }

    public void setStatu(int statu) {
        this.statu = statu;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getPluggedp() {
        return pluggedp;
    }

    public void setPluggedp(int pluggedp) {
        this.pluggedp = pluggedp;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }
}
