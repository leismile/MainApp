package com.ubtechinc.alpha.utils;import android.content.Context;import android.content.pm.PackageInfo;import android.content.pm.PackageManager;import com.ubtech.utilcode.utils.LogUtils;import com.ubtechinc.alpha.robotinfo.SoftwareVersionInfo;import com.ubtrobot.sys.SysApi;/** * @desc : 软件版本收集器 * @author: wzt * @time : 2017/5/23 * @modifier: * @modify_time: */public class VersionCollector {	private static VersionCollector sVersionCollector;	private Context mContext;	private VersionCollector(Context context) {		mContext = context.getApplicationContext();	}	public static VersionCollector get(Context context) {		if(sVersionCollector == null) {			synchronized (VersionCollector.class) {				if(sVersionCollector == null)					sVersionCollector = new VersionCollector(context);			}		}		return sVersionCollector;	}	public void requestVersion() {		SoftwareVersionInfo.get().chestVersion = SysApi.get().readCtrlVersion();		SoftwareVersionInfo.get().batteryVersion = "1231";		requestOtherVersion();	}	private void requestOtherVersion() {		String packageName = mContext.getPackageName();		PackageInfo info;		try {			info = mContext.getPackageManager().getPackageInfo(packageName, 0);			SoftwareVersionInfo.get().serviceVersionName = info.versionName;			SoftwareVersionInfo.get().serviceVersionCode = info.versionCode;			LogUtils.d("service version=" + info.versionName);		} catch (PackageManager.NameNotFoundException e) {			e.printStackTrace();		}		SoftwareVersionInfo.get().deviceVersion = android.os.Build.VERSION.RELEASE;		LogUtils.d("device version=" + android.os.Build.VERSION.RELEASE);	}}