/*
 *
 *  *
 *  * Copyright (c) 2008-2016 UBT Corporation.  All rights reserved.  Redistribution,
 *  *  modification, and use in source and binary forms are not permitted unless otherwise authorized by UBT.
 *  *
 *
 */

package com.ubtechinc.alpha.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ubtechinc.alpha.app.AlphaApplication;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author paul.zhang@ubtrobot.com
 * @date 2016/8/3
 * @Description 简单存储工具类
 * @modifier
 * @modify_time
 */

public class SharedPreferenceUtil {

	private static final String USERCARDSPFILE = "ALPHA_MAIN_SHARED";
	public static final String AFTER_COOLING_TIME_FRIENDS = "after_cooling_time_friends";
	/**
	 * @param context
	 * @param key
	 * @param value
	 * @return
	 * @throws
	 * @Description 自定义存储数据
	 */
	public static boolean save(Context context, String key, String value) {
		Editor editor = context.getSharedPreferences(USERCARDSPFILE, Context.MODE_PRIVATE).edit();
		editor.putString(key, value);
		return editor.commit();
	}

	/**
	 * @param context
	 * @param key
	 * @param value
	 * @return
	 * @throws
	 * @Description 存储String类型数据
	 */
	public static boolean saveString(Context context, String key, String value) {
		Editor editor = context.getSharedPreferences(USERCARDSPFILE, Context.MODE_PRIVATE).edit();
		editor.putString(key, value);
		return editor.commit();
	}

	/**
	 * @param context
	 * @param key
	 * @return
	 * @throws
	 * @Description 根据key值读取String类型数据
	 */
	public static String readString(Context context, String key,String defaultVoicer) {
		SharedPreferences sp = context.getSharedPreferences(USERCARDSPFILE, Context.MODE_PRIVATE);
		return sp.getString(key, defaultVoicer);
	}


	/**
	 * @param context
	 * @param key
	 * @param value
	 * @return
	 * @throws
	 * @Description 根据key存储int类型数据
	 */
	public static boolean saveInt(Context context, String key, int value) {
		Editor editor = context.getSharedPreferences(USERCARDSPFILE, Context.MODE_PRIVATE).edit();
		editor.putInt(key, value);
		return editor.commit();
	}

	/**
	 * @param context
	 * @param key
	 * @param value
	 * @return
	 * @throws
	 * @Description 存储布尔类型数据
	 */
	public static boolean saveBoolean(Context context, String key, boolean value) {
		Editor editor = context.getSharedPreferences(USERCARDSPFILE, Context.MODE_PRIVATE).edit();
		editor.putBoolean(key, value);
		return editor.commit();

	}

	/**
	 * @param context
	 * @param key
	 * @return
	 * @throws
	 * @Description 根据key值读取布尔类型数据,默认返回false
	 */
	public static boolean readBoolean(Context context, String key) {
		SharedPreferences sp = context.getSharedPreferences(USERCARDSPFILE, Context.MODE_PRIVATE);
		return sp.getBoolean(key, false);
	}

	/**
	 * @param context
	 * @param key
	 * @return
	 * @throws
	 * @Description 根据key值读取int类型数据,默认返回0
	 */
	public static int readInt(Context context, String key) {
		SharedPreferences sp = context.getSharedPreferences(USERCARDSPFILE, Context.MODE_PRIVATE);
		return sp.getInt(key, 0);

	}

	/**
	 * @param context
	 * @param key
	 * @param defaultValue
	 * @return
	 * @throws
	 * @Description 根据key值读取int类型数据
	 */

	public static int readInt(Context context, String key, int defaultValue) {
		SharedPreferences sp = context.getSharedPreferences(USERCARDSPFILE, Context.MODE_PRIVATE);
		return sp.getInt(key, defaultValue);
	}

	/**
	 * @param context
	 * @return
	 * @throws
	 * @Description 清除全部存储
	 */

	public static void clear(Context context) {
		Editor editor = context.getSharedPreferences(USERCARDSPFILE, Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
	}

	/**
	 * 用于保存集合
	 *
	 * @param key key
	 * @param map map数据
	 * @return 保存结果
	 */
	public static <K, V> boolean putHashMapData(String key, Map<K, V> map) {
		boolean result;
		SharedPreferences sp = AlphaApplication.getContext().getSharedPreferences(USERCARDSPFILE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		try {
			Gson gson = new Gson();
			String json = gson.toJson(map);
			editor.putString(key, json);
			result = true;
		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		editor.apply();
		return result;
	}

	/**
	 * 用于保存集合
	 *
	 * @param key key
	 * @return HashMap
	 */
	public static <V> HashMap<String, V> getHashMapData(String key, Class<V> clsV) {
		SharedPreferences sp = AlphaApplication.getContext().getSharedPreferences(USERCARDSPFILE, Context.MODE_PRIVATE);
		String json = sp.getString(key, "");
		HashMap<String, V> map = new HashMap<>();
		if(json != null && !TextUtils.isEmpty(json)){
			Gson gson = new Gson();
			JsonObject obj = new JsonParser().parse(json).getAsJsonObject();
			Set<Map.Entry<String, JsonElement>> entrySet = obj.entrySet();
			for (Map.Entry<String, JsonElement> entry : entrySet) {
				String entryKey = entry.getKey();
				JsonElement  value =  entry.getValue();
				map.put(entryKey, gson.fromJson(value, clsV));
			}
			Log.e("SharedPreferencesUtil", obj.toString());
		}
		return map;
	}

}
