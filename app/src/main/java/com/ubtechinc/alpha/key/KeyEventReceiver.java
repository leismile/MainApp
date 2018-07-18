package com.ubtechinc.alpha.key;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtrobot.masterevent.protos.SysMasterEvent;

/**
 * @modify: kevin.liu@ubtech.com
 * @modify time:2018/04/21
 */
public class KeyEventReceiver extends BroadcastReceiver {
    private static final String TAG = "KeyEventReceiver";
    private IKeyEventHandler handler = SysEventHandler.get();

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        LogUtils.d(TAG, "KeyEventReceiver -- action : " + action);
        if(action.equals(BroadcastAction.TOUCH_DOWN_BROADCAST)){
            handler.onSingleClick(SysMasterEvent.KeyCode.newBuilder().setKeycode(KeyCodeConstants.KEYCODE_RACKET_HEAD).build());
        }else if(action.equals(BroadcastAction.TOUCH_DOUBLE_CLICK_BROADCAST)){
            handler.onDoubleClick(SysMasterEvent.KeyCode.newBuilder().setKeycode(KeyCodeConstants.KEYCODE_RACKET_HEAD).build());
        }else if(action.equals(BroadcastAction.TOUCH_LONG_BROADCAST)){
            handler.onLongPress(SysMasterEvent.KeyCode.newBuilder().setKeycode(KeyCodeConstants.KEYCODE_RACKET_HEAD).build());
        }else if(action.equals(BroadcastAction.CUSTOM_KEY_DOWN_BROADCAST)){
            int keyValue = intent.getIntExtra("key_value",-1);
            Log.e("PowerEvent","CUSTOM_KEY_DOWN_BROADCAST keyValue" + keyValue);
            if(keyValue == 26){
                boolean isPowerBtnSingleClick = intent.getBooleanExtra("key_event_is_down",false);
                if(isPowerBtnSingleClick){
                    handler.onSingleClick(SysMasterEvent.KeyCode.newBuilder().setKeycode(KeyCodeConstants.KEYCODE_POWER_KEY_DOWN).build());
                }else{
                    handler.onLongPress(SysMasterEvent.KeyCode.newBuilder().setKeycode(KeyCodeConstants.KEYCODE_POWER_KEY_DOWN).build());
                }
            }

        }else if(action.equals(BroadcastAction.TOUCH_DOWN_VOLUME_DOWN_BROADCAST)){
            handler.onSingleClick(SysMasterEvent.KeyCode.newBuilder().setKeycode(KeyCodeConstants.KEYCODE_VOLUMN_DOWN_KEY_DOWN).build());
        }else if(action.equals(BroadcastAction.TOUCH_DOWN_VOLUME_UP_BROADCAST)){
            handler.onSingleClick(SysMasterEvent.KeyCode.newBuilder().setKeycode(KeyCodeConstants.KEYCODE_VOLUMN_DOWN_KEY_UP).build());
        }else if(action.equals(BroadcastAction.TOUCH_UP_VOLUME_DOWN_BROADCAST)){
            handler.onSingleClick(SysMasterEvent.KeyCode.newBuilder().setKeycode(KeyCodeConstants.KEYCODE_VOLUMN_UP_KEY_DOWN).build());
        }else if(action.equals(BroadcastAction.TOUCH_UP_VOLUME_UP_BROADCAST)){
            handler.onSingleClick(SysMasterEvent.KeyCode.newBuilder().setKeycode(KeyCodeConstants.KEYCODE_VOLUMN_UP_KEY_UP).build());
        }
    }

}
