package com.ubtechinc.alpha.utils;

import com.ubtech.utilcode.utils.JsonUtils;
import com.ubtechinc.nets.ResponseListener;
import com.ubtechinc.nets.http.ThrowableWrapper;
import com.ubtechinc.nets.http.Utils;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by lulin.wu on 2018/4/3.
 */

public final class HttpUtils {

    private static HttpUtils instance;

    private OkHttpClient client;
    private static final String TAG = "HttpUtils";

    private HttpUtils() {
        initialize();
    }

    private void initialize() {
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public static HttpUtils get() {
        if (instance != null) return instance;
        synchronized (HttpUtils.class) {
            if (instance == null) instance = new HttpUtils();
        }
        return instance;
    }

    public <T> void doGet(String url, final ResponseListener<T> listener) {
        Type[] types = listener.getClass().getGenericInterfaces();
        if (Utils.hasUnresolvableType(types[0])) {
            return;
        }
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        doRequest(request, listener);
    }

    private <T> void doRequest(Request request, final ResponseListener<T> listener) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener != null) {
                    listener.onError(new ThrowableWrapper(e, 500));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (listener != null) {
                    Type[] types = listener.getClass().getGenericInterfaces();
                    Type type = Utils.getParameterUpperBound(0, (ParameterizedType) types[0]);
                    T result = JsonUtils.getObject(response.body().string(), type);
                    listener.onSuccess(result);
                }
            }
        });
    }
}

