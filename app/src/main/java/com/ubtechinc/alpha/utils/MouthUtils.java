package com.ubtechinc.alpha.utils;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ubtrobot.commons.Priority;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.lib.mouthledapi.MouthLedApi;

/**
 * Created by lulin.wu on 2018/6/26.
 */

public class MouthUtils {
    /**
     * 常态下嘴巴灯灯效
     */
    public static void normalMouthLed() {

        MouthLedApi.get().startBreathModel(Color.argb(0, 255, 255, 255),
                8000, Integer.MAX_VALUE, Priority.HIGH, new ResponseListener() {
                    @Override
                    public void onResponseSuccess(Object o) {
                    }

                    @Override
                    public void onFailure(int i, @NonNull String s) {
                    }
                });


    }

    /**
     * 播tts时嘴巴灯灯效
     */
    public static void ttsMouthLed() {

        MouthLedApi.get().startBreathModel(Color.argb(0, 255, 255, 255), 2000, Integer.MAX_VALUE, Priority.HIGH, new ResponseListener() {
            @Override
            public void onResponseSuccess(Object o) {
            }

            @Override
            public void onFailure(int i, @NonNull String s) {
            }
        });


    }
}
