package com.ubtechinc.alpha.key;

import com.ubtech.utilcode.utils.notification.NotificationCenter;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.masterevent.protos.SysMasterEvent;

/**
 * @desc :
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2017/11/14
 */

class SysEventHandler implements IKeyEventHandler, ISysEventHandler {

    private static final SysEventHandler mSysEventHandler = new SysEventHandler();

    private SysEventHandler() {

    }

    public static final SysEventHandler get() {
        return mSysEventHandler;
    }

    @Override
    public void onSingleClick(SysMasterEvent.KeyCode keyCode) {
        NotificationCenter.defaultCenter().publish(BroadcastAction.ACTION_KEYCODE_SINGLE_CLICK, SysMasterEvent.TouchEvent
                .newBuilder()
                .setType(SysMasterEvent.TouchEventType.SINGLE_CLICK)
                .setKeycode(keyCode)
                .build());
    }

    @Override
    public void onDoubleClick(SysMasterEvent.KeyCode keyCode) {
        NotificationCenter.defaultCenter().publish(BroadcastAction.ACTION_KEYCODE_DOUBLE_CLICK, SysMasterEvent.TouchEvent
                .newBuilder()
                .setType(SysMasterEvent.TouchEventType.DOUBLE_CLICK)
                .setKeycode(keyCode)
                .build());

    }

    @Override
    public void onLongPress(SysMasterEvent.KeyCode keyCode) {
        NotificationCenter.defaultCenter().publish(BroadcastAction.ACTION_KEYCODE_LONG_PRESS, SysMasterEvent.TouchEvent
                .newBuilder()
                .setType(SysMasterEvent.TouchEventType.LONG_PRESS)
                .setKeycode(keyCode)
                .build());
    }
}
