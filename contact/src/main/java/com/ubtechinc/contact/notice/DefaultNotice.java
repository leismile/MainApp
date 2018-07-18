package com.ubtechinc.contact.notice;

import android.util.Log;
import com.google.gson.Gson;
import com.ubtechinc.contact.Contact;
import com.ubtechinc.nets.BuildConfig;
import com.ubtechinc.nets.HttpManager;
import com.ubtechinc.nets.ResponseListener;
import com.ubtechinc.nets.http.ThrowableWrapper;

/**
 * @desc : 默认通知实现
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/2/1
 */

public class DefaultNotice implements INotice{

    private static final String URL = BuildConfig.HOST + "notice/missOrInterceptCall";
    private static final String TAG = "DefaultNotice";
    private static final String KEY_ROBOTID = "robotUserId";
    private static final String KEY_CALLTYPE = "callType";
    private static final String KEY_CALLER = "caller";
    private static final String VALUE_CALLTYPE_MISS = "MISS";
    private static final String VALUE_CALLTYPE_INTERCEPT = "INTERCEPT";

    @Override
    public void notifyMiss(String caller) {
        Request request = Request.newInstanceMiss(caller);
        String json = new Gson().toJson(request);
        HttpManager.get(Contact.getInstance().getContext()).doPostWithJson(URL, json, new ResponseListener<Response>() {
            @Override
            public void onError(ThrowableWrapper e) {
                Log.d(TAG, " notifyMiss -- onError : " + Log.getStackTraceString(e));
            }

            @Override
            public void onSuccess(Response response) {
                Log.d(TAG, " notifyMiss -- onSuccess : " + response);
            }
        });
    }

    @Override
    public void notifyIntercept(String caller) {
        Request request = Request.newInstanceIntercept(caller);
        String json = new Gson().toJson(request);
        Log.d(TAG, " json : " + json);
        HttpManager.get(Contact.getInstance().getContext()).doPostWithJson(URL, json, new ResponseListener<Response>() {
            @Override
            public void onError(ThrowableWrapper e) {
                Log.d(TAG, " notifyIntercept -- onError : " + Log.getStackTraceString(e));
            }

            @Override
            public void onSuccess(Response response) {
                Log.d(TAG, " notifyIntercept -- onSuccess : " + response);
            }
        });
    }

    private static String getRobotId() {
        Log.d(TAG, " getRobotId : " + Contact.getInstance().getRobotId());
        String result = Contact.getInstance().getRobotId();
        if(result == null) {
            Log.e(TAG, " UserContact Not set robotId, please Set robotId such as UserContact.getInstance().setRobotId(\"\")");
        }
        return result;
    }

    public static class Request{
        private String robotUserId;
        private String callType;
        private String caller;

        public Request(String robotUserId, String callType, String caller) {
            this.robotUserId = robotUserId;
            this.callType = callType;
            this.caller = caller;
        }

        public static Request newInstanceMiss(String caller) {
            return new Request(getRobotId(), VALUE_CALLTYPE_MISS, caller);
        }

        public static Request newInstanceIntercept(String caller) {
            return new Request(getRobotId(), VALUE_CALLTYPE_INTERCEPT, caller);
        }
    }

    public static class Response{

        @Override
        public String toString() {
            return "Response{" +
                    "msg='" + msg + '\'' +
                    ", data=" + data +
                    ", success=" + success +
                    '}';
        }

        /**
         * msg : 无效的请求头
         * data : {}
         * success : false
         * resultCode : -100
         */



        private String msg;
        private DataBean data;
        private boolean success;

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public DataBean getData() {
            return data;
        }

        public void setData(DataBean data) {
            this.data = data;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public static class DataBean {
        }
    }
}
