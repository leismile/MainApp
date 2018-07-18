package com.ubtechinc.alpha.key;

import com.ubtrobot.masterevent.protos.SysMasterEvent;

/**
 * @desc : KeyEvent通知
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2017/11/14
 */

public interface IKeyEventHandler {
    /**
     * 单击
     */
    void onSingleClick(SysMasterEvent.KeyCode keyCode);

    /**
     * 双击
     */
    void onDoubleClick(SysMasterEvent.KeyCode keyCode);

    /**
     * 长按
     */
    void onLongPress(SysMasterEvent.KeyCode keyCode);
}
