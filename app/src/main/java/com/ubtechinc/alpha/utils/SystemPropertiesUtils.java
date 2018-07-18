package com.ubtechinc.alpha.utils;

import com.ubtechinc.alpha.CmCameraPrivacy;

import java.lang.reflect.Method;

/**
 * Created by lulin.wu on 2018/7/9.
 */

public  class SystemPropertiesUtils {
    private static final String PERSIST_FIRSTSTART = "persist.mini.firststart";
    private static final String PERSIST_FIRSTSTART_STEP = "persist.mini.firststart.step";
    private static final String PERSIST_CAMERAPRIVACY_TYPE = "persist.mini.cameraprivacy.sype";
    public static boolean getFirststart() {
        String value =  "1";
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String .class);
            value = (String) (get.invoke(c, PERSIST_FIRSTSTART, value));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value.equals("1");
    }

    public static void setFirststart(boolean isFirststart) {
        try {
            String value = isFirststart == true?"1":"0";
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, PERSIST_FIRSTSTART, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getFirststartStep(){
        String value = "0";
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, PERSIST_FIRSTSTART_STEP, value));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void setFirststartStep(String value){
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, PERSIST_FIRSTSTART_STEP, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static CmCameraPrivacy.CameraPrivacyType getCameraPrivacyType(){
        String value = "0";
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, PERSIST_CAMERAPRIVACY_TYPE, value));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value.equals("0")? CmCameraPrivacy.CameraPrivacyType.OFF: CmCameraPrivacy.CameraPrivacyType.ON ;
    }

    public static void setCameraPrivacyType(CmCameraPrivacy.CameraPrivacyType type){
        String value = type==CmCameraPrivacy.CameraPrivacyType.OFF?"0":"1";
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, PERSIST_CAMERAPRIVACY_TYPE, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
