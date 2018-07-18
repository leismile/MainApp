package com.ubtechinc.alpha.utils;

import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.libs.behaviors.Behavior;
import com.ubtrobot.mini.libs.behaviors.BehaviorInflater;
import com.ubtrobot.mini.properties.sdk.PropertiesApi;

import java.io.FileNotFoundException;

/**
 * Created by lulin.wu on 2018/7/14.
 */

public class PlayBehaviorUtil {

    private static Behavior mBehavior;

    public static void playBehavior(String name, Priority priority, Behavior.BehaviorListener behaviorListener){
        try {
            if(mBehavior == null){
                mBehavior = BehaviorInflater.loadBehaviorFromXml(
                        PropertiesApi.getRootPath() + "/behaviors/" + name + ".xml");
                mBehavior.setBehaviorListener(new Behavior.BehaviorListener() {
                    @Override
                    public void onCompleted() {
                        if(behaviorListener != null){
                            behaviorListener.onCompleted();
                        }
                        mBehavior = null;
                    }
                });
                mBehavior.setPriority(priority);
                mBehavior.start();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
