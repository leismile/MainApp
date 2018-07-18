package com.ubtechinc.alpha.appmanager;

import android.util.Log;

import com.baidu.location.BDLocation;
import com.ubtechinc.alpha.network.module.GetWeatherModule;
import com.ubtechinc.alpha.utils.HttpUtils;
import com.ubtechinc.nets.ResponseListener;
import com.ubtechinc.nets.http.ThrowableWrapper;

import java.util.List;

/**
 * Created by lulin.wu on 2018/4/3.
 */

public class GetWeatherManager {
    private static final String WEATHER_URL = "https://www.sojson.com/open/api/weather/json.shtml?city=";
    private static final String TAG = GetWeatherManager.class.getSimpleName();
    private String mCurrentCity;
    private GetWeatherModule.Forecast todayWeather;
    private GetWeatherModule.Forecast tomorrowWeather;
    private GetWeatherManager(){
    }
    private static class GetWeatherManagerHolder {
        public static GetWeatherManager instance = new GetWeatherManager();
    }

    public String getTodayWeather(){
        StringBuffer sb = new StringBuffer();
        if(todayWeather!= null){
            sb.append(mCurrentCity);
            sb.append("今天天气");
            sb.append(todayWeather.type);
            sb.append(",");
            sb.append("最");
            sb.append(todayWeather.high);
            sb.append(",");
            sb.append("最");
            sb.append(todayWeather.low);
            sb.append(",");
            sb.append(todayWeather.notice);
            return sb.toString();
        }else {
            return "";
        }
    }


    public String getTomorrowWeather(){
        StringBuffer sb = new StringBuffer();
        if(tomorrowWeather != null){
            sb.append(mCurrentCity);
            sb.append("明天天气");
            sb.append(tomorrowWeather.type);
            sb.append(",");
            sb.append("最");
            sb.append(tomorrowWeather.high);
            sb.append(",");
            sb.append("最");
            sb.append(tomorrowWeather.low);
            sb.append(",");
            sb.append(tomorrowWeather.notice);
            return sb.toString();
        }else {
            return "";
        }
    }



    public static GetWeatherManager getInstance(){
        return GetWeatherManager.GetWeatherManagerHolder.instance;
    }
    public void init(){
        UbtLocationManager.getInstance().init();
    }



    public void getCityWeather(){
        UbtLocationManager.getInstance().setGetLocationListener(new UbtLocationManager.GetLocationListener() {
            @Override
            public void getLocation(BDLocation location) {
                mCurrentCity = location.getCity();
                if(mCurrentCity == null){
                    return;
                }
                Log.i(TAG,"当前定位城市=========" + mCurrentCity);
                String weather_url = WEATHER_URL + mCurrentCity;
                Log.i(TAG,"weather_url==========" + weather_url);
                HttpUtils.get().doGet(weather_url, new ResponseListener<GetWeatherModule.Response>() {
                    @Override
                    public void onError(ThrowableWrapper e) {
                    }

                    @Override
                    public void onSuccess(GetWeatherModule.Response response) {
                        if(response != null){
                            if(response.data != null){
                                List<GetWeatherModule.Forecast> weathers = response.data.forecast;
                                todayWeather = weathers.get(0);
                                tomorrowWeather = weathers.get(1);
                                Log.i(TAG,"todayWeather=======" + todayWeather.toString());
                                Log.i(TAG,"tomorrowWeather=======" + tomorrowWeather.toString());
                            }
                        }
                    }
                });
            }
        });
        UbtLocationManager.getInstance().startLocation();
    }


}
