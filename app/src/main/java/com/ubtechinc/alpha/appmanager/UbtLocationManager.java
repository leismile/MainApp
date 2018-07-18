package com.ubtechinc.alpha.appmanager;

import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.ubtechinc.alpha.app.AlphaApplication;

/**
 * Created by lulin.wu on 2018/4/3.
 */

public class UbtLocationManager {
    private static final String TAG = UbtLocationManager.class.getSimpleName();
    public LocationClient mLocationClient = null;
    private UbtLocationListener ubtLocationListener = new UbtLocationListener();
    private GetLocationListener mGetLocationListener;

    private UbtLocationManager() {
    }

    private static class UbtLocationManagerHolder {
        public static UbtLocationManager instance = new UbtLocationManager();
    }

    public static UbtLocationManager getInstance() {
        return UbtLocationManager.UbtLocationManagerHolder.instance;
    }

    public void init() {
        Log.i(TAG, "init============");
        mLocationClient = new LocationClient(AlphaApplication.getContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener(ubtLocationListener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(0);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    public void setGetLocationListener(GetLocationListener getCityListener) {
        this.mGetLocationListener = getCityListener;
    }

    public void startLocation() {
        Log.i(TAG, "startLocation==============");
        mLocationClient.start();
    }


    public void stopLocation() {
        mLocationClient.stop();
    }

    public class UbtLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (mGetLocationListener != null) {
                mGetLocationListener.getLocation(location);
            }
        }
    }

    public interface GetLocationListener {
        void getLocation(BDLocation location);
    }

    public void openGPSSettings(boolean isOpen) {
        //获取GPS现在的状态（打开或是关闭状态）
        boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(AlphaApplication.getContext().getContentResolver(), LocationManager.GPS_PROVIDER);
        Log.i(TAG,"gpsEnabled======" + gpsEnabled);
        if(isOpen){
            if(!gpsEnabled){
                Settings.Secure.setLocationProviderEnabled(AlphaApplication.getContext().getContentResolver(), LocationManager.GPS_PROVIDER, true);
            }
        }else {
            if(gpsEnabled){
                Settings.Secure.setLocationProviderEnabled(AlphaApplication.getContext().getContentResolver(), LocationManager.GPS_PROVIDER, false);
            }
        }


    }
}