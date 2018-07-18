package com.ubtechinc.protocollibrary.communite;

import android.util.Log;

import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.GeneratedMessageV3;
import com.ubtech.utilcode.utils.ConvertUtils;
import com.ubtech.utilcode.utils.LogUtils;
import com.ubtechinc.protocollibrary.communite.old.IOldMsgDispather;
import com.ubtechinc.protocollibrary.protocol.MiniMessage;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by Administrator on 2017/5/24.
 */

public class RobotPhoneCommuniteProxy<T> {

    HashMap<Integer, RequestBean> requestCache = new HashMap<>();
    AtomicInteger requestSerial = new AtomicInteger();
    private ISendMsg sendMsgEngine;
    private IReceiveMsg receiveMsgEngine;
    private ITimeoutEngine timeoutEngine;
    private static final String TAG = "RobotPhoneComProxy";

    private static RobotPhoneCommuniteProxy sInstance;

    public static RobotPhoneCommuniteProxy getInstance() {
        if (sInstance == null) {
            synchronized (RobotPhoneCommuniteProxy.class) {
                sInstance = new RobotPhoneCommuniteProxy();
            }
        }
        return sInstance;
    }



    private synchronized void sendData(short cmdId,byte version, int responseSerail, GeneratedMessageV3 requestBody, Object peer, Callback callback) {


        int requestId = requestSerial.incrementAndGet();
        requestCache.put(requestId, new RequestBean(cmdId, callback));
        byte[] data = ProtoBufferDispose.buildAlphaMessage(cmdId, version,requestId , responseSerail, requestBody );
        LogUtils.d("send data cmdId = " + cmdId + " responseSerail = " + responseSerail +  " requestId = " + " peer = " + peer + " data: " + ConvertUtils.bytes2HexString(data));
        sendMsgEngine.sendMsg(requestId, peer, data, callback);
    }


    public void sendResponseDataOld(byte[] data, Object peer) {
        sendMsgEngine.sendMsg(0, peer, data, null);
    }
    /**
     * 发送回复消息
     */
    public void sendResponseMessage(short cmdId, byte version, int responseSerial, GeneratedMessageV3 requestBody, Object peer, final ICallback dataCallback) {
        sendData(cmdId, version, responseSerial, requestBody,peer, new Callback() {

            @Override
            public void onSendSuccess() {
                //消息发送成功，等待回调
                LogUtils.d(TAG, "onSendSuccess---");
            }

            @Override
            public void onSendError(int requestId, int errorCode) {
                if (dataCallback != null) {
                    dataCallback.onError(IMErrorUtil.handleException(errorCode));
                    requestCache.remove(requestId);
                }

            }

            @Override
            public void onReturnMessage(int requestId, MiniMessage response) {
                if (dataCallback != null) {
                    dataCallback.onSuccess(response);
                    requestCache.remove(requestId);
                }
            }



        });
    }

    public void sendMessage2Robot(short cmdId, byte version, final GeneratedMessageV3 requestBody, String peer, final ICallback<T> dataCallback) {
        sendData(cmdId,version,0,requestBody,peer, new Callback() {



            @Override
            public void onSendSuccess() {
                //消息发送成功，等待回调
                Log.d(TAG,"onSendSuccess---");
            }

            @Override
            public void onSendError(int requestId,int errorCode) {
                if (dataCallback != null) {
                    dataCallback.onError(IMErrorUtil.handleException(errorCode));
                    requestCache.remove(requestId);
                }
            }

            @Override
            public void onReturnMessage(int requestId,MiniMessage response) {
                if (dataCallback != null) {
                    if (response != null && response.getDataContent() != null) {



                        Type[] types =  ((ParameterizedType)dataCallback.getClass().getGenericInterfaces()[0]).getActualTypeArguments();
                        if (Utils.hasUnresolvableType(types[0])){
                            return;
                        }
                        Class < T >  entityClass  =  (Class < T > ) ((ParameterizedType) dataCallback.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[ 0 ];
                        GeneratedMessageV3 generatedMessageLite = ProtoBufferDispose.unPackData(entityClass, response.getDataContent());
                        if(generatedMessageLite != null) {
                            dataCallback.onSuccess((T)generatedMessageLite);

                        }else {
                            dataCallback.onError(IMErrorUtil.handleException(IMErrorUtil.ERROR.NULL_DATA));
                        }
                    }

                    requestCache.remove(requestId);
                }
            }

        });
    }
    public void init(ISendMsg sendMsgEngine, IReceiveMsg receiveMsgEngine) {
        this.sendMsgEngine = sendMsgEngine;
        this.receiveMsgEngine = receiveMsgEngine;
        sendMsgEngine.init();
        receiveMsgEngine.init();
    }

    public void setIMsgDispatcher(ImMsgDispathcer msgDispathcer) {
        receiveMsgEngine.setIMsgDispatcher(msgDispathcer);
    }

    public void setOldMsgDispatcher(IOldMsgDispather oldMsgDispatcher) {
        receiveMsgEngine.setOldMsgDispatcher(oldMsgDispatcher);
    }

    public void setTimeoutEngine(ITimeoutEngine timeoutEngine) {
        this.timeoutEngine = timeoutEngine;
    }



    private void setTimeout(short cmId, int id, Callback callback) {
        if (timeoutEngine != null) {
            timeoutEngine.setTimeout(cmId, id, callback);
        }
    }

    private void cancelTimeout(long id) {
        if (timeoutEngine != null) {
            timeoutEngine.cancelTimeout(id);
        }
    }

    public void dispatchResponse(int responseId, MiniMessage cmdBody) {
        RequestBean bean = requestCache.get(responseId);
        if (bean != null && bean.callback != null) {
            bean.callback.onReturnMessage(responseId, cmdBody);
        }
        requestCache.remove(responseId);
    }


    static class RequestBean {
        int cmdId;
        Callback callback;

        RequestBean(int cmdId, Callback callback) {
            this.cmdId = cmdId;
            this.callback = callback;
        }
    }


    public interface Callback {
        void onSendSuccess(); //消息发送成功

        void onSendError(int requestId, int errorCode);

        void onReturnMessage(int requestId, MiniMessage response); //收到返回的数据
    }

    private   boolean hasUnresolvableType(Type type) {
        if (type instanceof Class<?>) {
            return false;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            for (Type typeArgument : parameterizedType.getActualTypeArguments()) {
                if (hasUnresolvableType(typeArgument)) {
                    return true;
                }
            }
            return false;
        }
        if (type instanceof GenericArrayType) {
            return hasUnresolvableType(((GenericArrayType) type).getGenericComponentType());
        }
        if (type instanceof TypeVariable) {
            return true;
        }
        if (type instanceof WildcardType) {
            return true;
        }
        String className = type == null ? "null" : type.getClass().getName();
        throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                + "GenericArrayType, but <" + type + "> is of type " + className);
    }
}