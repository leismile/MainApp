/*
 *
 *  *
 *  *  *
 *  *  * Copyright (c) 2008-2017 UBT Corporation.  All rights reserved.  Redistribution,
 *  *  *  modification, and use in source and binary forms are not permitted unless otherwise authorized by UBT.
 *  *  *
 *  *
 *
 */

package com.ubtechinc.alpha.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.util.SparseIntArray;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtechinc.services.alphamini.R;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.express.ExpressApi;



/**
 * [音量控制类]
 * 
 * @author zengdengyi
 * @version 1.0
 * @date 2014-12-31 上午11:04:54
 * @mofifier logic.peng 2017/4/26
 **/

public class SoundVolumesUtils {
	private static int[] rawids = {R.raw.media_volume, R.raw.app_switch_hint};
	private static SoundVolumesUtils _instance;
	private Context mContext;
	private AudioManager mAudioManager;
	private SparseIntArray mSoundPoolMap;
	private SoundPool mSoundPool;
	private float mMaxVolume;  //当前设备能设置的最大音量
	private static final int MAX_VOLUME_LEVEL = 5; //当前设备音量划分的档位 不能大于15
	private float mVolumeStep;
	private String TAG = "SoundVolumesUtils";
	/**
	 * Requests the get of the Sound Manager and creates it if it does not
	 * exist.
	 * 
	 * @return Returns the single get of the SoundManager
	 */
	static public SoundVolumesUtils get(Context mContext) {
		if (_instance != null) return _instance;
		synchronized (SoundVolumesUtils.class) {
			if (_instance == null)
				_instance = new SoundVolumesUtils(mContext);
		}
		return _instance;
	}

	public SoundVolumesUtils(Context mContext) {
		this.mContext = mContext;
		// 音量控制,初始化定义
		mAudioManager = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);
		mSoundPool = new SoundPool(rawids.length, AudioManager.STREAM_MUSIC, 0);
		mSoundPoolMap = new SparseIntArray(rawids.length);
		loadSounds();
		mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		mVolumeStep = mMaxVolume/MAX_VOLUME_LEVEL;

	}

	public void loadSounds() {
		for (int i = 0; i < rawids.length ; i ++){
			mSoundPoolMap.put(i+1,mSoundPool.load(mContext, rawids[i], 1));
		}
	}

	public float getMaxVolume(){
		return mMaxVolume;
	}

	public boolean  isPlayMusic(){
		return mAudioManager.isMusicActive();
	}

	/**
	 * 增加音量
	 * 
	 * @author zengdengyi
	 * @date 2014-12-31 上午11:08:32
	 */
	public void addVolume(int value) {
		// 当前音量
		int currentVolume = mAudioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		int maxVolume = mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		currentVolume += value;
		currentVolume = currentVolume < maxVolume ? currentVolume : maxVolume;
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
		LogUtils.i("currentVolume:" + currentVolume);
		isMulVolume = false;
		showVolumeExpress(currentVolume);
		playSound();

	}

	/**
	 * 音量减
	 *
	 * 
	 * @author zengdengyi
	 * @date 2014-12-31 上午11:27:50
	 */
	private boolean isMulVolume = false;
	public void mulVolume(int value) {
		// 当前音量
		int currentVolume = mAudioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		currentVolume -= value;
		currentVolume = 0 < currentVolume ? currentVolume : 0;
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0); // tempVolume:音量绝对值
		LogUtils.i("currentVolume:" + currentVolume);
		isMulVolume = true;
		showVolumeExpress(currentVolume);
		playSound();
	}

	private void showVolumeExpress(int volumeStep){
		Log.i(TAG,"volumeStep========" + volumeStep);
		if(volumeStep == 0){
			ExpressApi.get().doExpress("volume_001",1,false, Priority.HIGH);
		}else if (volumeStep==1 || volumeStep == 2 ){
			ExpressApi.get().doExpress("volume_002",1,false, Priority.HIGH);
		}else if (volumeStep ==3 || volumeStep == 4 ){
			if(isMulVolume){
				AlphaUtils.ttsMessage("volume_011");
			}
			ExpressApi.get().doExpress("volume_003",1,false, Priority.HIGH);
		}else if(volumeStep == 5 || volumeStep == 6){
			ExpressApi.get().doExpress("volume_004",1, false,Priority.HIGH);
		}else if(volumeStep == 7 || volumeStep == 8){
			ExpressApi.get().doExpress("volume_005",1, false,Priority.HIGH);
		}else if(volumeStep == 9 || volumeStep == 10 ){
			ExpressApi.get().doExpress("volume_006",1,false, Priority.HIGH);
		}else if(volumeStep == 11 || volumeStep == 12){
			ExpressApi.get().doExpress("volume_007",1, false,Priority.HIGH);
		}else if(volumeStep == 13 || volumeStep == 14){
			ExpressApi.get().doExpress("volume_008",1, false,Priority.HIGH);
		}else  {
			ExpressApi.get().doExpress("volume_009",1, false,Priority.HIGH);
		}

	}



	private void setVolume(){
		mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,500,0);
	}

	/**
	 * 获取当前音量档位
	 * @return
     */
	public int getVolumeLevel(){
		int currentVolume = mAudioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		return  currentVolume;
	}
	public void playSound() {
		mSoundPool.play(mSoundPoolMap.get(1), 1, 1, 0, 0, 1.5f);
	}
	
	public void playSound(int i){
		mSoundPool.play(mSoundPoolMap.get(i), 1, 1, 0, 0, 1.5f);
	}
}
