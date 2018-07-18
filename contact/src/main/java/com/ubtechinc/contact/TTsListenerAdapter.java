package com.ubtechinc.contact;

import com.ubtrobot.speech.listener.TTsListener;

/**
 * @desc : TTS监听空适配器
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/3/17
 */

public class TTsListenerAdapter implements TTsListener{
    @Override
    public void onTtsBegin() {

    }

    @Override
    public void onTtsVolumeChange(int i) {

    }

    @Override
    public void onTtsCompleted() {

    }

    @Override
    public void onError(int i, String s) {

    }
}
