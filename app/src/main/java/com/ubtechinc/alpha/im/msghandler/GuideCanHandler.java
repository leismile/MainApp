package com.ubtechinc.alpha.im.msghandler;

import android.text.TextUtils;
import android.util.Log;

import com.ubtechinc.alpha.service.SkillHelper;
import com.ubtechinc.nets.im.modules.IMJsonMsg;

import java.util.HashMap;


/**
*@data 创建时间：2018/4/25
*@author：bob.xu
*@Description:帮助指引中不需要做查询或业务逻辑的都放在此处实现
*@version
*/
public class GuideCanHandler implements IMJsonMsgHandler {

    private static final String TAG = "GuideCanHandler";

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, IMJsonMsg jsonRequest, String peer) {
        Log.d(TAG, " GuideCanHandler handleMsg requestCmdId : " + requestCmdId);
        String utteranceName = idUtteranceMap.get(requestCmdId);
        if (!TextUtils.isEmpty(utteranceName)) {
            SkillHelper.startSkillByIntent(utteranceName, null);
        }
    }

    private static HashMap<Integer,String> idUtteranceMap = new HashMap<Integer,String>() {
        {
            put(2020,"你能做什么");
            put(2021,"怎么绑定");
            put(2022,"怎么联网");
            put(2023,"怎么停止");
            put(2024,"怎么调音量");
            put(2025,"怎么关机");
            put(2026,"怎么休眠");
            put(2027,"怎么开启4G");
            put(2028,"怎么拍照");
            put(2029,"怎么打电话");
            put(2030,"怎么视频监控");
            put(2031,"怎么读绘本");
            put(2032,"怎么翻译");
            put(2033,"怎么查天气");
            put(2034,"怎么查股票");
            put(2035,"怎么听音乐");
            put(2036,"怎么听故事");
            put(2037,"怎么定闹钟");
            put(2038,"怎么查汇率");
            put(2039,"怎么切网");
            put(2040,"4G开关状态");
            put(2041,"充电状态");
            put(2042,"联哪个网");
//            put(2043,"管理员是谁");
//            put(2044,"绑定者是谁");
            put(2045,"有多少电量");
            put(2046,"有多少容量");

        }

    };


}
