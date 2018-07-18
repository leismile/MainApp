package com.ubtechinc.alpha.appmanager;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.utils.AlphaUtils;
import com.ubtechinc.alpha.utils.SharedPreferenceUtil;
import com.ubtechinc.sauron.api.FaceApi;
import com.ubtechinc.sauron.api.FaceInfo;
import com.ubtechinc.sauron.api.FaceTrackListener;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.express.listeners.AnimationListener;
import com.ubtrobot.masterevent.protos.SysMasterEvent;
import com.ubtrobot.mini.libs.scenes.EmotionStore;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.speech.listener.TTsListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import event.master.ubtrobot.com.sysmasterevent.event.SysActiveEvent;


/**
 * Created by lulin.wu on 2018/3/29.
 * 人脸追踪管理类
 */

public class FaceDetectManager {
    private static final String TAG = FaceDetectManager.class.getSimpleName();
    private static final int TEN_MINUTES = 10 * 60 * 1000;
    private static final int FIVE_SECONDS = 5 * 1000;
    private static FaceDetectManager instance;
    private boolean isStartFaceDetect = false;
    HashMap<String, SayHelloFriend> mFaceCoolingTime = new HashMap<>();
    List<FaceInfo> mFaces = new ArrayList<>();
    List<FaceInfo> mCurrentFaces = new ArrayList<>();

    private FaceDetectManager() {
        mFaceCoolingTime = SharedPreferenceUtil.getHashMapData(SharedPreferenceUtil.AFTER_COOLING_TIME_FRIENDS, SayHelloFriend.class);
    }

    public static FaceDetectManager getInstance() {
        if (instance == null) {
            synchronized (FaceDetectManager.class) {
                if (instance == null) {
                    instance = new FaceDetectManager();
                }
            }
        }
        return instance;
    }

    /**
     * 开启人脸追踪
     */
    private long startFaceTrackTime;

    public void startFaceDetect() {
        Log.i(TAG, "startFaceDetect==========");
        if (!isStartFaceDetect) {
            isPlaySomebodyGone = false;
            isPlayFriendLeave = false;
            mFaces.clear();
            FaceApi.get().faceTrack(20, true, new FaceTrackListener() {
                @Override
                public void onStart() {
                    isStartFaceDetect = true;
                }

                @Override
                public void onFaceChange(final List<FaceInfo> list) {
                    if (SysStatusManager.getInstance().getmCurrentStatusData().getNewStatus() == SysMasterEvent.ActivieStatusType.SITDOWN_STANDBY) {
                        SysStatusHelpManager.get().setIntoSitdownStandbyTime(System.currentTimeMillis());
                    }

                    if (null != list) {
                        mCurrentFaces = list;
                        int size = list.size();
                        int lastTimeSize = mFaces.size();
                        LogUtils.i(TAG, "镜头里的人====" + list.toString());
                        LogUtils.i(TAG, "上次镜头里的人=====" + mFaces.toString());
                        long currentTime = System.currentTimeMillis();
                        LogUtils.i(TAG,"currentTime====" + currentTime  + ";;startFaceTrackTime===" + startFaceTrackTime);
                        if (currentTime - startFaceTrackTime < FIVE_SECONDS) {
                            return;
                        }
                        startFaceTrackTime = currentTime;
                        if (size == 0) {
                            excuteBodyExpress();
                        } else {
                            stopBodyBehavior();
                            int friendNum = friendNum(list);
                            Log.i(TAG, "friendNum=======" + friendNum);
                            if (friendNum == 1) { //判断当前镜头中是否只有一个熟人
                                Log.i(TAG, "镜头只有一个熟人==========");
                                FaceInfo faceInfo = getFriend(list).get(0);
                                boolean isAfterCoolingTimer = isAfterCoolingTimer(faceInfo);
                                Log.i(TAG, "isAfterCoolingTimer=======" + isAfterCoolingTimer);
                                if (isAfterCoolingTimer) {//判断是否过了冷却时间
                                    String faceId = faceInfo.getId();
                                    SayHelloFriend sayHelloFriend = getFriendForFaceId(faceId);
                                    if (sayHelloFriend == null) {
                                        broadcastWelcome(faceInfo, new VoiceListener() {
                                            @Override
                                            public void onCompleted() {
                                                Log.i(TAG, "onCompleted=======");
                                                if (isHaveStranger(list)) {
                                                    playStrangerEnterTts(list);
                                                } else {
                                                    excuteBodyExpress();
                                                }
                                            }

                                            @Override
                                            public void onError(int i, String s) {
                                            }
                                        });
                                    } else {
                                        broadcastWelcome(sayHelloFriend, faceInfo, new VoiceListener() {
                                            @Override
                                            public void onCompleted() {
                                                if (isHaveStranger(list)) {
                                                    playStrangerEnterTts(list);
                                                } else {
                                                    excuteBodyExpress();
                                                }
                                            }

                                            @Override
                                            public void onError(int i, String s) {
                                            }
                                        });
                                    }
                                } else {
                                    Log.i(TAG, "没过冷却期========");
                                    boolean isHaveStranger = isHaveStranger(list);
                                    Log.i(TAG, "isHaveStranger=========" + isHaveStranger);
                                    if (isHaveStranger) {
                                        playStrangerEnterTts(list);
                                    } else {
                                        boolean isFriendLeave = isFriendLeave(list);
                                        boolean isStrangerLeave = isStrangerLeave(list);
                                        Log.i(TAG, "isFriendLeave========" + isFriendLeave);
                                        Log.i(TAG, "isStrangerLeave========" + isStrangerLeave);
                                        if (isFriendLeave && isStrangerLeave) {
                                            playSomebodyGone();
                                        } else {
                                            if (isFriendLeave) {
                                                playFriendLeaveTts(list);
                                            }else {
                                                if (isStrangerLeave) {
                                                    playSomebodyGone();
                                                }else {
                                                    excuteBodyExpress();
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (friendNum == 0) { //没有熟人
                                    Log.i(TAG, "镜头没有熟人==========");
                                    boolean isStrangerLeave = isStrangerLeave(list);
                                    boolean isFriendLeave = isFriendLeave(list);
                                    Log.i(TAG, "isStrangerLeave=======" + isStrangerLeave + ";;isFriendLeave=====" + isFriendLeave);
                                    if (isStrangerLeave && isStrangerLeave) {
                                        playSomebodyGone();
                                    } else {
                                        if (isFriendLeave) {
                                            playFriendLeaveTts(list);
                                        } else {
                                            if (isStrangerLeave) {
                                                playSomebodyGone();
                                            } else {
                                                excuteBodyExpress();
                                            }
                                        }
                                    }
                                } else { //有多个熟人
                                    Log.i(TAG, "镜头有多个熟人==========");
                                    boolean isFriendEnter = isFriendEnter(list);
                                    boolean isStrangerEnter = isStrangerEnter(list);
                                    boolean isFriendLeave = isFriendLeave(list);
                                    boolean isStrangerLeave = isStrangerLeave(list);
                                    Log.i(TAG, "isFriendEnter========" + isFriendEnter);
                                    Log.i(TAG, "isStrangerEnter========" + isStrangerEnter);
                                    Log.i(TAG, "isFriendLeave========" + isFriendLeave);
                                    Log.i(TAG, "isStrangerLeave========" + isStrangerLeave);
                                    if (isFriendEnter) {
                                        playFriendsEnterTts(list);
                                    } else {
                                        if (isStrangerEnter) {
                                            playStrangerEnterTts(list);
                                        } else {
                                            if (isStrangerLeave && isFriendLeave) {
                                                playSomebodyGone();
                                            } else {
                                                if (isFriendLeave) {
                                                    playFriendLeaveTts(list);
                                                } else {
                                                    if (isStrangerLeave) {
                                                        playSomebodyGone();
                                                    } else {
                                                        excuteBodyExpress();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            addFriendFormClooingTime(list);
                        }
                        mFaces = list;
                    }
                }

                @Override
                public void onStop() {
                    LogUtils.i(TAG, "人脸追踪关闭========");
                    stopBodyBehavior();
                    isStartFaceDetect = false;
                    if (AlphaUtils.isShutDowning) {
                        return;
                    }
                    SysActiveEvent activeStatusData = SysStatusManager.getInstance().getmCurrentStatusData();
                    if (activeStatusData.getNewStatus() == SysMasterEvent.ActivieStatusType.STANDUP_STANDBY) {
                        long currentTime = System.currentTimeMillis();
                        long dTime = currentTime - SysStatusHelpManager.get().getIntoStandbuStandbyTime();
                        if (dTime < AppConstants.FIVE_MIN) {
                            SmallActionManager.get().startInfraRed();
                        } else {
                            InfraRedManager.get().stopObjectDetection();
                            SysStatusManager.getInstance().publishSysActiveStatus(SysMasterEvent.ActivieStatusType.SITDOWN_STANDBY);
                        }
                    } else if (activeStatusData.getNewStatus() == SysMasterEvent.ActivieStatusType.SITDOWN_STANDBY) {
                        long currentTime = System.currentTimeMillis();
                        long intoSitdownStandbyTime = SysStatusHelpManager.get().getIntoSitdownStandbyTime();
                        if (currentTime - intoSitdownStandbyTime < AppConstants.FIVE_MIN) {
                            SitdownStandbyManager.get().intoSitdownStandbyManager();
                        } else {
                            InfraRedManager.get().stopObjectDetection();
                            SysStatusManager.getInstance().publishSysActiveStatus(SysMasterEvent.ActivieStatusType.STANDBY);
                        }
                    }
                }

                @Override
                public void onFail(int i, String s) {
                    LogUtils.i(TAG, "人脸追踪开启失败=====" + s);
                }
            });
        }
    }

    /**
     * 执行有无人的表情
     */
    private Timer excuteBodyTimer;
    private TimerTask excuteBodyTask;

    private boolean isBodyExpressStop = false;
    private synchronized void excuteBodyExpress() {
        stopBodyBehavior();
        isBodyExpressStop = false;
        excuteBodyTimer = new Timer();
        excuteBodyTask = new TimerTask() {
            @Override
            public void run() {
                if(!isBodyExpressStop){
                    String expressName;
                    if (mCurrentFaces.size() == 0) { //执行无人的表情
                        expressName = "w_track_101";//
                    } else { //执行有人的表情
                        expressName = "w_track_201b";//
                    }
                    Log.i(TAG, "expressName=====" + expressName);
                    doBodyExpress(expressName);
                }
            }
        };
        excuteBodyTimer.schedule(excuteBodyTask,0,8 * 1000);
    }

    public synchronized void stopBodyBehavior() {
        isBodyExpressStop = true;
        if(excuteBodyTimer != null){
            excuteBodyTimer.cancel();
            excuteBodyTimer = null;
        }
        if(excuteBodyTask != null){
            excuteBodyTask.cancel();
            excuteBodyTask = null;
        }
        doBodyExpress("normal_1");
    }

    private void doBodyExpress(final String expressName) {
        ExpressApi.get().doExpress(expressName, new AnimationListener() {
            @Override
            public void onAnimationStart() {

            }

            @Override
            public void onAnimationEnd() {

            }

            @Override
            public void onAnimationRepeat(int loopNumber) {
            }
        });
    }

    private void addFriendFormClooingTime(List<FaceInfo> list) {
        List<FaceInfo> leaveFriends = getEnterFriends(list);
        for (FaceInfo faceInfo : leaveFriends) {
            if (mSayHelloFriend == null) {
                addSayHelloFriend(faceInfo);
            } else {
                if (faceInfo.getId().equals(mSayHelloFriend.getFaceId())) {
                    mSayHelloFriend.setTime(System.currentTimeMillis());
                    mFaceCoolingTime.put(faceInfo.getId(), mSayHelloFriend);
                    SharedPreferenceUtil.putHashMapData(SharedPreferenceUtil.AFTER_COOLING_TIME_FRIENDS, mFaceCoolingTime);
                } else {
                    addSayHelloFriend(faceInfo);
                }
            }
        }
    }

    private void addSayHelloFriend(FaceInfo faceInfo) {
        SayHelloFriend sayHelloFriend = new SayHelloFriend();
        sayHelloFriend.setFaceId(faceInfo.getId());
        sayHelloFriend.setWithcTime(TimeSlot.DEFAULT);
        sayHelloFriend.setTime(System.currentTimeMillis());
        mFaceCoolingTime.put(faceInfo.getId(), sayHelloFriend);
        SharedPreferenceUtil.putHashMapData(SharedPreferenceUtil.AFTER_COOLING_TIME_FRIENDS, mFaceCoolingTime);
    }

    private SayHelloFriend getFriendForFaceId(String faceId) {
        SayHelloFriend sayHelloFriend = mFaceCoolingTime.get(faceId);
        return sayHelloFriend;
    }

    public void stopFaceDetect() {
        if (isStartFaceDetect) {
//            stopBodyBehavior();
            FaceApi.get().stopFaceTrack(new ResponseListener() {

                @Override
                public void onResponseSuccess(Object o) {
                    Log.i(TAG, "人脸识别关闭成功========");

                }

                @Override
                public void onFailure(int i, @NonNull String s) {
                    Log.i(TAG, "人脸识别关闭成功========" + s);
                }
            });
        }
    }

    /**
     * 播放熟人进来tts
     *
     * @param list
     */
    private void playFriendsEnterTts(final List<FaceInfo> list) {
        Log.i(TAG, "playFriendsEnterTts======");
        List<FaceInfo> friends = getNotSayHelloFriends(list);
        if (friends.size() != 0) {
            StringBuffer sb = new StringBuffer();
            for (FaceInfo faceInfo : friends) {
                sb.append(faceInfo.getName());
                sb.append(",");
            }
            commonWelcomeBehaivor(new VoiceListener() {
                @Override
                public void onCompleted() {
                }

                @Override
                public void onError(int errCode, String errMsg) {
                }
            }, sb.toString());
        }

    }

    /**
     * 获取没有打过招呼的熟人
     *
     * @param list
     * @return
     */
    private List<FaceInfo> getNotSayHelloFriends(List<FaceInfo> list) {
        List<FaceInfo> enterFriends = getEnterFriends(list);
        List<FaceInfo> notSayHelloFriends = new ArrayList<>();
        for (FaceInfo faceInfo : enterFriends) {
            if (isAfterCoolingTimer(faceInfo)) {
                notSayHelloFriends.add(faceInfo);
            }
        }
        return notSayHelloFriends;
    }

    /**
     * 获取进来的熟人
     *
     * @param list
     * @return
     */
    private List<FaceInfo> getEnterFriends(List<FaceInfo> list) {
        List<FaceInfo> lastTimeFriends = getFriend(mFaces);
        List<FaceInfo> friends = getFriend(list);
        List<FaceInfo> enterFriends = getDiffrent(friends, lastTimeFriends);
        return enterFriends;
    }

    /**
     * 播报有陌生人进来tts
     *
     * @param list
     */
    private void playStrangerEnterTts(final List<FaceInfo> list) {
        List<FaceInfo> friends = getFriend(list);
        if (friends.size() > 0) {
            FaceInfo friend = friends.get(0);
            String friendName = friend.getName();
            Mood mood = getMood();
            String text1 = "";
            String text2 = "";
            String behaviorName1 = "";
            String behaviorName2 = "";
            if (mood == Mood.NICE) {
                text1 = "哎呦喂，还有我不认识的人" + friendName;
                text2 = "快给我介绍一下呀";
                behaviorName1 = "w_guide_0001a";
                behaviorName2 = "w_guide_0001b";
            } else if (mood == Mood.GOOD) {
                text1 = "哎呦喂，还有我不认识的人" + friendName;
                text2 = "快给我介绍一下呀";
                behaviorName1 = "w_guide_0001a";
                behaviorName2 = "w_guide_0001b";
            } else if (mood == Mood.BAD) {
                text1 = "哎呦喂，还有我不认识的人" + friendName;
                text2 = "快给我介绍一下呀";
                behaviorName1 = "w_guide_0001a";
                behaviorName2 = "w_guide_0001b";
            }
            weclomeBehaivor(text1, text2, behaviorName1, behaviorName2, new VoiceListener() {
                @Override
                public void onCompleted() {
                    excuteBodyExpress();
                }

                @Override
                public void onError(int errCode, String errMsg) {

                }
            });

        }
    }


    /**
     * 播报熟人走了tts
     *
     * @param list
     */
    private boolean isPlayFriendLeave = false;

    private void playFriendLeaveTts(final List<FaceInfo> list) {
        if (!isPlayFriendLeave) {
            isPlayFriendLeave = true;
            List<FaceInfo> friends = getLeaveFriends(list);
            StringBuffer sb = new StringBuffer();
            sb.append("呀");
            for (FaceInfo faceInfo : friends) {
                sb.append(",");
                sb.append(faceInfo.getName());
            }
            String text1 = sb.toString();
            String text2 = ",怎么不见了.";
            Log.i(TAG, "playFriendLeaveTts==text1===" + text1 + ";;;text2====" + text2);
            String behaviorName1 = "w_gone_3001a";
            String behaviorName2 = "w_gone_3001b";
            weclomeBehaivor(text1, text2, behaviorName1, behaviorName2, new VoiceListener() {
                @Override
                public void onCompleted() {
                    excuteBodyExpress();
                }

                @Override
                public void onError(int errCode, String errMsg) {
                }
            });
        }
    }

    /**
     * 判断是否有陌生人进来
     *
     * @param list
     * @return
     */
    private boolean isStrangerEnter(List<FaceInfo> list) {
        List<FaceInfo> lastTimeStrangers = getStranger(mFaces);
        List<FaceInfo> strangers = getStranger(list);
        return strangers.size() > lastTimeStrangers.size();
    }

    /**
     * 判断是否有熟人进来
     *
     * @param list
     */
    private boolean isFriendEnter(List<FaceInfo> list) {
        List<FaceInfo> lastTimeFriends = getFriend(mFaces);
        List<FaceInfo> friends = getFriend(list);
        return friends.size() > lastTimeFriends.size();
    }

    /**
     * 获取离开的熟人
     *
     * @return
     */
    private List<FaceInfo> getLeaveFriends(List<FaceInfo> list) {
        List<FaceInfo> lastTimeFriends = getFriend(mFaces);
        List<FaceInfo> frinds = getFriend(list);
        List<FaceInfo> leaveFriends = new ArrayList<>();
        if (mFaces.size() > frinds.size()) {
            leaveFriends = getDiffrent(lastTimeFriends, frinds);
        }
        return leaveFriends;
    }

    private List<FaceInfo> getDiffrent(List<FaceInfo> list1, List<FaceInfo> list2) {
        List<FaceInfo> same = new ArrayList<>();
        for (FaceInfo faceInfo1 : list1) {
            String faceInfo1Id = faceInfo1.getId();
            for (FaceInfo faceInfo2 : list2) {
                if (faceInfo1Id.equals(faceInfo2.getId())) {
                    same.add(faceInfo1);
                }
            }
        }
        list1.removeAll(same);
        return list1;

    }

    private boolean isFaceLeave(List<FaceInfo> list) {
        return mFaces.size() > list.size();
    }


    /**
     * 判断是否有熟人离开
     *
     * @param list
     * @return
     */
    private boolean isFriendLeave(List<FaceInfo> list) {
        List<FaceInfo> lastTimeFriends = getFriend(mFaces);
        List<FaceInfo> friends = getFriend(list);
        return lastTimeFriends.size() > friends.size();
    }


    /**
     * 判断是否有陌生人离开
     *
     * @param list
     * @return
     */
    private boolean isStrangerLeave(List<FaceInfo> list) {
        List<FaceInfo> lastTimeStrangers = getStranger(mFaces);
        List<FaceInfo> stranger = getStranger(list);
        return lastTimeStrangers.size() > stranger.size();
    }


    /**
     * 判断镜头是否有熟人
     *
     * @param list
     * @return
     */
    private boolean isHaveFriend(List<FaceInfo> list) {
        for (FaceInfo faceInfo : list) {
            if (!TextUtils.isEmpty(faceInfo.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断镜头里是否有陌生人
     *
     * @param list
     * @return
     */
    private boolean isHaveStranger(List<FaceInfo> list) {
        for (FaceInfo faceInfo : list) {
            if (TextUtils.isEmpty(faceInfo.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断镜头里面有几个熟人
     *
     * @param list
     * @return
     */
    public int friendNum(List<FaceInfo> list) {
        int index = 0;
        for (FaceInfo faceInfo : list) {
            String id = faceInfo.getId();
            if (!TextUtils.isEmpty(id)) {
                index++;
            }
        }
        return index;
    }

    /**
     * 获取镜头里的熟人
     *
     * @param list
     * @return
     */
    public List<FaceInfo> getFriend(List<FaceInfo> list) {
        List<FaceInfo> friends = new ArrayList<>();
        for (FaceInfo faceInfo : list) {
            String id = faceInfo.getId();
            if (!TextUtils.isEmpty(id)) {
                friends.add(faceInfo);
            }
        }
        return friends;
    }

    /**
     * 获取镜头里的陌生人
     *
     * @param list
     * @return
     */
    private List<FaceInfo> getStranger(List<FaceInfo> list) {
        List<FaceInfo> strangers = new ArrayList<>();
        for (FaceInfo faceInfo : list) {
            String id = faceInfo.getId();
            if (TextUtils.isEmpty(id)) {
                strangers.add(faceInfo);
            }
        }
        return strangers;
    }

    private SayHelloFriend mSayHelloFriend;

    private void broadcastWelcome(FaceInfo faceInfo, final VoiceListener voiceListener) {
        TimeSlot timeSlot = whichTimeSlot();
        String name = faceInfo.getName();
        if (timeSlot == TimeSlot.MORNING) {
            morningWeclomeBehaivor(voiceListener, name);
        } else if (timeSlot == TimeSlot.NOON) {
            noonWeclomeBehaivor(voiceListener, name);
        } else if (timeSlot == TimeSlot.AFTERNOON) {
            afternoorWelomeBehaivor(voiceListener, name);
        } else if (timeSlot == TimeSlot.NIGTH) {
            nightWelcomeBehaivor(voiceListener, name);
        } else {
            commonWelcomeBehaivor(voiceListener, name);
        }
    }

    private void commonWelcomeBehaivor(VoiceListener voiceListener, String name) {
        String text = "";
        String behaviorName = "";
        Mood mood = getMood();
        if (mood == Mood.NICE) {
            String[] names = name.split(",");
            if (names.length > 1) {
                text = "哎哟威，大家好呀," + name;
            } else {
                text = "哎哟威，你好呀," + name;
            }
            behaviorName = "w_hello_5001";
        } else if (mood == Mood.GOOD) {
            text = "哈low，" + name;
            behaviorName = "w_hello_5002";
        } else if (mood == Mood.BAD) {
            text = "哟" + name;
            behaviorName = "w_hello_5003";
        }
        AlphaUtils.playBehavior(behaviorName, Priority.LOW, null);
        VoicePool.get().playTTs(text, Priority.NORMAL, voiceListener);
    }

    /**
     * 播报晚上的欢迎语
     *
     * @param voiceListener
     * @param name
     */
    private void nightWelcomeBehaivor(VoiceListener voiceListener, String name) {
        Mood mood = getMood();
        String text1 = "";
        String text2 = "";
        String expressName1 = "";
        String expressName2 = "";
        String tomorrowWeather = GetWeatherManager.getInstance().getTomorrowWeather();
        if (null == tomorrowWeather || tomorrowWeather.equals("")) {
            //李昕，晚上好，今晚可以好好放松休息一下！
            text1 = "嗨," + name;
            text2 = "晚上好，今晚可以好好放松休息一下!";
            expressName1 = "w_hello_4001a";
            expressName2 = "w_hello_004c";
        } else {
            if (mood == Mood.NICE) {
                text1 = "wow " + name;
                StringBuffer sb = new StringBuffer();
                sb.append("一天过的好快哇～悄悄告诉你哦～");
                sb.append(tomorrowWeather);
                text2 = sb.toString();
                expressName1 = "w_hello_4001a";
                expressName2 = "w_hello_4001b";
            } else if (mood == Mood.GOOD) {
                text1 = "good evening " + name;
                StringBuffer sb = new StringBuffer();
                sb.append("我给你播报下明天的天气吧");
                sb.append(tomorrowWeather);
                text2 = sb.toString();
                expressName1 = "w_hello_4002a";
                expressName2 = "w_hello_4002b";
            } else if (mood == Mood.BAD) {
                text1 = name;
                StringBuffer sb = new StringBuffer();
                sb.append("晚上好");
                sb.append(tomorrowWeather);
                text2 = sb.toString();
                expressName1 = "w_hello_4003a";
                expressName2 = "w_hello_4003b";
            }
        }
        weclomeBehaivor(text1, text2, expressName1,
                expressName2, voiceListener);
    }

    /**
     * 播报下午的欢迎语
     *
     * @param voiceListener
     * @param name
     */
    private void afternoorWelomeBehaivor(VoiceListener voiceListener, String name) {
        Mood mood = getMood();
        String text1 = "";
        String text2 = "";
        String expressName1 = "";
        String expressName2 = "";
        if (mood == Mood.NICE) {
            text1 = "嘿" + name;
            text2 = "我们一起拍日落吧";
            expressName1 = "w_hello_3001a";
            expressName2 = "w_hello_3001b";
        } else if (mood == Mood.GOOD) {
            text1 = "今天喝下午茶了吗";
            text2 = name;
            expressName1 = "w_hello_3002a";
            expressName2 = "w_hello_3002b";
        } else if (mood == Mood.BAD) {
            text1 = "Good afternoon";
            text2 = name;
            expressName1 = "w_hello_3003a";
            expressName2 = "w_hello_3003b";
        }
        weclomeBehaivor(text1, text2, expressName1,
                expressName2, voiceListener);
    }

    /**
     * 播报中午的欢迎语
     *
     * @param voiceListener
     * @param name
     */
    private void noonWeclomeBehaivor(VoiceListener voiceListener, String name) {
        Mood mood = getMood();
        String text1 = "";
        String text2 = "";
        String expressName1 = "";
        String expressName2 = "";
        if (mood == Mood.NICE) {
            text1 = "hi," + name;
            text2 = "中午不睡下午崩溃~有没有享受一个美美哒午休啊~";
            expressName1 = "w_hello_2001a";
            expressName2 = "w_hello_2001b";
        } else if (mood == Mood.GOOD) {
            text1 = "hi，" + name;
            text2 = "看见你吼开心呢";
            expressName1 = "w_hello_2002a";
            expressName2 = "w_hello_2002b";
        } else if (mood == Mood.BAD) {
            text1 = "Hi，" + name;
            text2 = "下午好啊！";
            expressName1 = "w_hello_2003a";
            expressName2 = "w_hello_2003b";
        }
        weclomeBehaivor(text1, text2, expressName1,
                expressName2, voiceListener);
    }

    /**
     * 播报早上的欢迎语
     *
     * @param voiceListener
     * @param name
     */
    private void morningWeclomeBehaivor(final VoiceListener voiceListener, String name) {
        Mood mood = getMood();
        String text1 = "";
        String text2 = "";
        String expressName1 = "";
        String expressName2 = "";
        String weather = GetWeatherManager.getInstance().getTodayWeather();
        if (null == weather || weather.equals("")) {
            text1 = "嗨," + name;
            text2 = "早上好啊，又是元气满满的一天";
            expressName1 = "w_hello_1001a";
            expressName2 = "w_hello_001c";
        } else {
            if (mood == Mood.NICE) {
                text1 = "嗨," + name;
                StringBuffer sb = new StringBuffer();
                sb.append("早上好啊！很高兴又能见到你，我给你播报一下今天的天气吧");
                sb.append(weather);
                text2 = sb.toString();
                expressName1 = "w_hello_1001a";
                expressName2 = "w_hello_1001b";
            } else if (mood == Mood.GOOD) {
                text1 = "Good morning " + name;
                StringBuffer sb = new StringBuffer();
                sb.append("我给你播报一下今天的天气吧");
                sb.append(weather);
                text2 = sb.toString();
                expressName1 = "w_hello_1002a";
                expressName2 = "w_hello_1002b";
            } else if (mood == Mood.BAD) {
                text1 = name;
                StringBuffer sb = new StringBuffer();
                sb.append("早上好！我给你播报一下今天的天气吧");
                sb.append(weather);
                sb.append("我要去睡个回笼觉了");
                text2 = sb.toString();
                expressName1 = "w_hello_1003a";
                expressName2 = "w_hello_1003b";
            }
        }
        weclomeBehaivor(text1, text2, expressName1,
                expressName2, voiceListener);
    }

    private void weclomeBehaivor(String text1, final String text2, String expressName1,
                                 final String expressName2, final VoiceListener voiceListener) {
        AlphaUtils.playBehavior(expressName1, Priority.LOW, null);
        VoicePool.get().playTTs(text1, Priority.NORMAL, new VoiceListener() {
            @Override
            public void onCompleted() {
                VoicePool.get().playTTs(text2, Priority.HIGH, voiceListener);
                AlphaUtils.playBehavior(expressName2, Priority.LOW, null);
            }

            @Override
            public void onError(int i, String s) {
            }
        });
    }

    /**
     * 播报欢迎语
     */
    private void broadcastWelcome(SayHelloFriend sayHelloFriend, FaceInfo faceInfo, VoiceListener voiceListener) {
        TimeSlot timeSlot = whichTimeSlot();
        String text = "";
        TimeSlot friendTimeSlot = sayHelloFriend.getWithcTime();
        String name = faceInfo.getName();
        Log.i(TAG, "timeSlot=====" + timeSlot + ";;friendTimeSlot======" + friendTimeSlot);
        mSayHelloFriend = sayHelloFriend;
        mSayHelloFriend.setWithcTime(timeSlot);
        if (timeSlot == TimeSlot.MORNING) {
            if (friendTimeSlot == TimeSlot.MORNING) {
                commonWelcomeBehaivor(voiceListener, name);
            } else {
                morningWeclomeBehaivor(voiceListener, name);
            }
        } else if (timeSlot == TimeSlot.NOON) {
            if (friendTimeSlot == TimeSlot.NOON) {
                commonWelcomeBehaivor(voiceListener, name);
            } else {
                noonWeclomeBehaivor(voiceListener, name);
            }
        } else if (timeSlot == TimeSlot.AFTERNOON) {
            if (friendTimeSlot == TimeSlot.AFTERNOON) {
                commonWelcomeBehaivor(voiceListener, name);
            } else {
                afternoorWelomeBehaivor(voiceListener, name);
            }
        } else if (timeSlot == TimeSlot.NIGTH) {
            if (friendTimeSlot == TimeSlot.NIGTH) {
                commonWelcomeBehaivor(voiceListener, name);
            } else {
                nightWelcomeBehaivor(voiceListener, name);
            }
        } else {
            commonWelcomeBehaivor(voiceListener, name);
        }
    }


    /**
     * 判断是否过了冷却     * @param faceInfo
     *
     * @return false 没过冷却时间  true 过了冷却时间
     */
    private boolean isAfterCoolingTimer(FaceInfo faceInfo) {
        Iterator<Map.Entry<String, SayHelloFriend>> entries = mFaceCoolingTime.entrySet().iterator();
        long currentTime = System.currentTimeMillis();
        SayHelloFriend sayHelloFriend  = mFaceCoolingTime.get(faceInfo.getId());
        if(sayHelloFriend != null && currentTime - sayHelloFriend.getTime() < TEN_MINUTES){
            return false;
        }
//        while (entries.hasNext()) {
//            Map.Entry<String, SayHelloFriend> entry = entries.next();
//            String id = entry.getKey();
//            SayHelloFriend sayHelloFriend = entry.getValue();
//            long time = sayHelloFriend.getTime();
//            if (id.equals(faceInfo.getId()) ) {
//                if (currentTime - time < TEN_MINUTES) {
//                    return false;
//                } else {
//                    return true;
//                }
//            }
//        }
        return true;
    }

    /**
     * @param faceInfo
     * @return
     */
    private boolean isSayHelloForFriend(FaceInfo faceInfo) {
        Iterator<Map.Entry<String, SayHelloFriend>> entries = mFaceCoolingTime.entrySet().iterator();
        String faceId = faceInfo.getId();
        long currentTime = System.currentTimeMillis();
        while (entries.hasNext()) {
            Map.Entry<String, SayHelloFriend> entry = entries.next();
            String id = entry.getKey();
            if (id.equals(faceInfo.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean isPlaySomebodyGone = false;

    private void playSomebodyGone() {
        if (!isPlaySomebodyGone) {
            isPlaySomebodyGone = true;
            AlphaUtils.playBehavior("w_gone_1001", Priority.LOW, null);
            VoicePool.get().playTTs("哎呀呀！那个人我还没认识呢，怎么就不见了。", Priority.LOW, new VoiceListener() {
                @Override
                public void onCompleted() {
                    excuteBodyExpress();
                }

                @Override
                public void onError(int i, String s) {
                }
            });
        }
    }

    public enum Mood {
        NICE, GOOD, BAD
    }

    enum TimeSlot {
        MORNING, NOON, AFTERNOON, NIGTH, DEFAULT
    }

    private Mood getMood() {
        int fave = EmotionStore.queryFavorByUserId(AlphaApplication.getContext().getContentResolver(), 0);
        if (0 < fave && fave < 30) {
            return Mood.BAD;
        } else if (30 <= fave && fave < 60) {
            return Mood.GOOD;
        } else {
            return Mood.NICE;
        }

    }

    private TimeSlot whichTimeSlot() {
        int index = periodOfTheCurrentTime();
        if (index == 0) {
            return TimeSlot.MORNING;
        } else if (index == 1) {
            return TimeSlot.NOON;
        } else if (index == 2) {
            return TimeSlot.AFTERNOON;
        } else if (index == 3) {
            return TimeSlot.NIGTH;
        } else {
            return TimeSlot.DEFAULT;
        }
    }


    /**
     * 判断当前时间的哪个时间段
     */
    public int periodOfTheCurrentTime() {
        String[] dates = new String[5];
        dates[0] = "6:00-10:00";
        dates[1] = "12:00-14:00";
        dates[2] = "14:30-17:00";
        dates[3] = "19:30-23:30";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date now = new Date();
        int index = 0;
        try {
            now = sdf.parse(sdf.format(now));
            for (int i = 0; i < dates.length - 1; i++) {
                if (is(now, dates[i])) {
                    break;
                }
                index++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return index;
    }

    private boolean is(Date now, String arg) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String[] s = arg.split("-");
        Date start = sdf.parse(s[0]);
        Date end = sdf.parse(s[1]);
        return start.getTime() <= now.getTime() && end.getTime() >= now.getTime();
    }

    private class TTsListenerAbs implements TTsListener {

        @Override
        public void onTtsBegin() {

        }

        @Override
        public void onTtsVolumeChange(int volume) {

        }

        @Override
        public void onTtsCompleted() {

        }

        @Override
        public void onError(int errCode, String errMsg) {

        }
    }

    private class SayHelloFriend {
        public long time;
        public TimeSlot withcTime;
        public String faceId;

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public TimeSlot getWithcTime() {
            return withcTime;
        }

        public void setWithcTime(TimeSlot withcTime) {
            this.withcTime = withcTime;
        }

        public String getFaceId() {
            return faceId;
        }

        public void setFaceId(String faceId) {
            this.faceId = faceId;
        }
    }
}