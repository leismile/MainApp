package com.ubtechinc.alpha.im.msghandler;

import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.behavior.RobotStandup;
import com.ubtrobot.mini.libs.behaviors.Behavior;
import com.ubtrobot.mini.libs.behaviors.BehaviorInflater;

/**
 * Created by bob.xu on 2018/1/8.
 */

public class BehaviorManager {
    private static BehaviorManager instance;
    private Behavior currBehavior;
    private String currBehaviorPath;
    public static BehaviorManager getInstance() {
        if (instance == null) {
            synchronized (BehaviorManager.class) {
                if (instance == null) {
                    instance = new BehaviorManager();
                }
            }
        }
        return instance;
    }

    public synchronized void playBehavior(final String behaviorPath,final Behavior.BehaviorListener listener) {
        if (currBehavior != null) {
            currBehavior.stop();
        }
        try {
            currBehavior = BehaviorInflater.loadBehaviorFromXml(behaviorPath);
            currBehaviorPath = behaviorPath;
            currBehavior.setBehaviorListener(new Behavior.BehaviorListener() {
                @Override
                public void onCompleted() {
                    currBehavior = null;
                    currBehaviorPath = null;
                    if (listener != null) {

                    }
                }
            });
            currBehavior.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void stopBehavior(String behaviorPath) {
        if (currBehavior != null && currBehaviorPath != null && currBehaviorPath.equals(behaviorPath)) {
            currBehavior.stop();
            currBehavior = null;
            currBehaviorPath = null;
        }
        RobotStandup robotStandup = new RobotStandup(AlphaApplication.getContext());
        robotStandup.start();
    }
}
