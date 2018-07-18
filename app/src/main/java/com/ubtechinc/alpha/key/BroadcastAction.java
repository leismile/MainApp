package com.ubtechinc.alpha.key;

import com.google.protobuf.Message;
import com.ubtechinc.alpha.service.sysevent.policy.ActionReceivedPolicy;
import com.ubtrobot.masterevent.protos.SysMasterEvent;

import java.util.HashMap;



/**
 * @author : kevin.liu@ubtrobot.com
 * @description :
 * @date : 2018/4/23
 * @modifier :
 * @modify time :
 */
public class BroadcastAction {
    /********************************* 底层芯片的广播Action *************************************/
    public static final String TOUCH_DOWN_BROADCAST = "touch_down_broadcast";
    public static final String TOUCH_DOUBLE_CLICK_BROADCAST = "touch_double_click_broadcast";
    public static final String TOUCH_LONG_BROADCAST = "touch_long_broadcast";
    public static final String TOUCH_DOWN_VOLUME_DOWN_BROADCAST = "key_down_volume_down_broadcast";
    public static final String TOUCH_DOWN_VOLUME_UP_BROADCAST = "key_up_volume_down_broadcast";
    public static final String TOUCH_UP_VOLUME_DOWN_BROADCAST = "key_down_volume_up_broadcast";
    public static final String TOUCH_UP_VOLUME_UP_BROADCAST = "key_up_volume_up_broadcast";
    public static final String CUSTOM_KEY_DOWN_BROADCAST = "wind.action.CUSTOM_KEY_EVENT";

    /********************************* UBT 对应底层广播Action 封装过后的 统一Action *************************************/
    public static final String ACTION_VOLUME_DOWN_KEY_DOWN = "ubtechinc.intent.action.volume.down.key.down";
    public static final String ACTION_VOLUME_DOWN_KEY_UP = "ubtechinc.intent.action.volume.down.key.up";
    public static final String ACTION_VOLUME_UP_KEY_DOWN = "ubtechinc.intent.action.volume.up.key.down";
    public static final String ACTION_VOLUME_UP_KEY_UP = "ubtechinc.intent.action.volume.up.key.up";

    public static final String ACTION_KEYCODE_SINGLE_CLICK = "com.ubtechinc.services.Action.ROBOT_INTERRUPTED"; // 单击
    public static final String ACTION_KEYCODE_DOUBLE_CLICK = "ubtechinc.intent.action.double";    // 双击
    public static final String ACTION_KEYCODE_LONG_PRESS = "ubtechinc.intent.action.lentouch";   // 长按

    public static final String ACTION_RACKET_HEAD = "ubtechinc.intent.action.racket.head";
    public static final String ACTION_SYS_ACTIVE_STATUS = "ubtechinc.intent.action..sys.active.status";
    public static final String ACTION_BATTERY_STATE = "ubtechinc.intent.action.battery.state";

    public static final String ACTION_KEYCODE_POWER_CLICK = "ubtechinc.intent.action.power.click";
    public static final String ACTION_KEYCODE_SHUTDOWN = "ubtechinc.intent.action.power.shutdown";
    /*********************************  UBT 对应底层广播Action 封装过后的 统一Action *************************************/


    public static HashMap<String, ActionReceivedPolicy> mActionPolicyMap = new HashMap<>();

    static {

        mActionPolicyMap.put(ACTION_RACKET_HEAD, ActionReceivedPolicy.ONE_SHOT);
        mActionPolicyMap.put(ACTION_KEYCODE_SINGLE_CLICK, ActionReceivedPolicy.ONE_SHOT);
        mActionPolicyMap.put(ACTION_KEYCODE_DOUBLE_CLICK, ActionReceivedPolicy.ONE_SHOT);
        mActionPolicyMap.put(ACTION_KEYCODE_LONG_PRESS, ActionReceivedPolicy.ONE_SHOT);
        mActionPolicyMap.put(ACTION_VOLUME_DOWN_KEY_DOWN, ActionReceivedPolicy.ONE_SHOT);
        mActionPolicyMap.put(ACTION_VOLUME_DOWN_KEY_UP, ActionReceivedPolicy.ONE_SHOT);
        mActionPolicyMap.put(ACTION_VOLUME_UP_KEY_DOWN, ActionReceivedPolicy.ONE_SHOT);
        mActionPolicyMap.put(ACTION_VOLUME_UP_KEY_UP, ActionReceivedPolicy.ONE_SHOT);
        mActionPolicyMap.put(ACTION_KEYCODE_POWER_CLICK, ActionReceivedPolicy.ONE_SHOT);

        mActionPolicyMap.put(ACTION_BATTERY_STATE, ActionReceivedPolicy.ALL);
        mActionPolicyMap.put(ACTION_SYS_ACTIVE_STATUS, ActionReceivedPolicy.ALL);
        mActionPolicyMap.put(ACTION_KEYCODE_SHUTDOWN, ActionReceivedPolicy.ALL);
    }
    public static ActionReceivedPolicy getPolicy(String action){
        return mActionPolicyMap.get(action);
    }

}
