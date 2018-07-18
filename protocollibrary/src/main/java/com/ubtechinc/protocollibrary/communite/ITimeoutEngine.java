package com.ubtechinc.protocollibrary.communite;

import android.util.Log;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IM 超时 管理类
 * Created by ubt on 2017/12/2.
 */

public abstract class ITimeoutEngine {
    private static final String TAG = "ITimeoutEngine";
    Map<Integer, RobotPhoneCommuniteProxy.Callback> callbackMap = new HashMap<>();

    List<Integer> whileList;

    public void setTimeout(short cmId, int requestId, RobotPhoneCommuniteProxy.Callback dataCallback) {
        if (isInWhileList(cmId)) {
            Log.d(TAG,"setTimeout cmdId = %d requestId= %d in while list" + cmId + requestId);
        } else {
            Log.d(TAG,"setTimeout cmdId = %d requestId= %d "+ cmId+ requestId);
            callbackMap.put(requestId, dataCallback);
            onSetTimeout( cmId, requestId);
        }
    }

    public abstract void onSetTimeout( short cmId, int requestId);

    public void cancelTimeout(Long requestId) {
        Log.d(TAG,"cancelTimeout requestId= %d "+ requestId);
        onCancelTimeout(requestId);
        callbackMap.remove(requestId);
    }

    public abstract void onCancelTimeout(Long requestId);

    public void onTimeout(int requestId) {
        Log.d(TAG,"onTimeout requestId= %d "+ requestId);
        RobotPhoneCommuniteProxy.Callback callback = callbackMap.get(requestId);
        if (callback != null) {
            callback.onSendError(requestId, IMErrorUtil.ERROR.TIMEOUT_ERROR);
        }
    }

    public void release() {
        callbackMap.clear();
    }

    private boolean isInWhileList(short cmdId) {
        if (whileList != null) {
            return whileList.contains(cmdId);
        }
        return false;
    }

    public void setWhileList(List<Integer> whileList) {
        this.whileList = whileList;
    }
}
