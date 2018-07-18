package com.ubtechinc.protocollibrary.communite;

import android.util.Log;


import com.ubtech.utilcode.utils.LogUtils;
import com.ubtechinc.protocollibrary.annotation.IMJsonMsgRelationVector;
import com.ubtechinc.protocollibrary.annotation.IMMsgRelationVector;
import com.ubtechinc.protocollibrary.annotation.ImJsonMsgRelation;
import com.ubtechinc.protocollibrary.annotation.ImMsgRelation;
import com.ubtechinc.protocollibrary.bean.IMJsonMsg;
import com.ubtechinc.protocollibrary.protocol.MiniMessage;

import java.util.HashMap;


/**
 * Created by Administrator on 2017/5/25.
 * author: bob.xu
 * IM消息的处理分发类， 每个Msg的处理逻辑，尽量提取到单独的类中,主服务与客户端需要各实现一个子类,
 * 在子类中通过注解注册要处理哪些Msg
 */

abstract public class ImMsgDispathcer {
    private static final String TAG = "ImMsgDispathcer" ;
    //protobuffer定义的消息
    protected HashMap<Short,ImMsgRelation> cmdIdMap = new HashMap<>();
    //json定义的消息
    protected HashMap<Short,ImJsonMsgRelation> cmdIdJsonMap = new HashMap<>();
    protected ImMsgDispathcer() {
        parseMsgRelation();
    }

    private void parseMsgRelation() {
        IMMsgRelationVector relationSum = this.getClass().getAnnotation(IMMsgRelationVector.class);
        if (relationSum != null) {
            ImMsgRelation[] msgRelationList = relationSum.value();
            if (msgRelationList != null && msgRelationList.length > 0) {
                for (ImMsgRelation msgRelation : msgRelationList) {
                    cmdIdMap.put(msgRelation.requestCmdId(), msgRelation);
                }
            }
        }

        IMJsonMsgRelationVector relationJsonSum = this.getClass().getAnnotation(IMJsonMsgRelationVector.class);
        if (relationJsonSum != null) {
            ImJsonMsgRelation[] msgjsonRelationList = relationJsonSum.value();
            if (msgjsonRelationList != null && msgjsonRelationList.length > 0) {
                for (ImJsonMsgRelation msgJsonRelation : msgjsonRelationList) {
                    cmdIdJsonMap.put(msgJsonRelation.requestCmdId(), msgJsonRelation);
                }
            }
        }
    }



    public void dispatchMsg(short cmdId, MiniMessage requestMsg, Object peer) {
        Log.w(TAG,"dispatchMsg--- ");
        ImMsgRelation msgRelation = cmdIdMap.get(cmdId);
        if(msgRelation == null) {
            Log.w(TAG,"dispatchMsg--- no ImMsgRelation  or not support this cmdId");
            return;
        }
        Class handleClass = msgRelation.msgHandleClass();
        if (handleClass == null || handleClass.isInstance(NullHandler.class)) {
            Log.w(TAG,"dispatchMsg--- no msg handle class or not support this cmdId");
            return;
        }
        LogUtils.d("dispatchMsg---cmdId = "+cmdId+", peer = "+peer+", handleClass = "+handleClass.getSimpleName());
        try {
            IMsgHandler msgHandlerInstance = (IMsgHandler)handleClass.newInstance();
            msgHandlerInstance.handleMsg(cmdId,msgRelation.responseCmdId(),requestMsg,peer);

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void dispatchMsg(int cmdId, IMJsonMsg jsonRequest, String peer) {
        ImJsonMsgRelation msgRelation = cmdIdJsonMap.get(cmdId);
        Class handleClass = msgRelation.msgHandleClass();
        if (handleClass == null || handleClass.isInstance(NullJsonHandler.class)) {
            Log.d(TAG,"dispatchMsg--- no msg handle class or not support this cmdId");
            return;
        }
        LogUtils.d("dispatchMsg---cmdId = "+cmdId+", peer = "+peer+", handleClass = "+handleClass.getSimpleName());
        try {
            IMJsonMsgHandler msgHandlerInstance = (IMJsonMsgHandler)handleClass.newInstance();
            msgHandlerInstance.handleMsg(cmdId,msgRelation.responseCmdId(),jsonRequest,peer);

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}


