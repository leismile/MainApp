package com.ubtechinc.alpha.network.module;

import android.support.annotation.Keep;

import java.util.List;

/**
 * Created by lulin.wu on 2018/4/3.
 */
@Keep
public class GetWeatherModule {

    @Keep
    public static class Response{
        public String date;
        public int status;
        public boolean success;
        public GetWeatherModule.Data data ;
    }
    @Keep
    public static class Data {
        public List<Forecast> forecast;
    }
    @Keep
    public static class Forecast {
        public String date;
        public String sunrise;
        public String high;
        public String low;
        public String sunset;
        public String aqi;
        public String fx;
        public String fl;
        public String type;
        public String notice;

        @Override
        public String toString() {
            return "Forecast{" +
                    "date='" + date + '\'' +
                    ", sunrise='" + sunrise + '\'' +
                    ", high='" + high + '\'' +
                    ", low='" + low + '\'' +
                    ", aqi='" + aqi + '\'' +
                    ", fx='" + fx + '\'' +
                    ", fl='" + fl + '\'' +
                    ", type='" + type + '\'' +
                    ", notice='" + notice + '\'' +
                    '}';
        }
    }
}
