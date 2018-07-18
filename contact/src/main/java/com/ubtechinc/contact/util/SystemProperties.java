package com.ubtechinc.contact.util;

import java.lang.reflect.Method;

/**
 * Created by bob.xu on 2017/9/15.
 */

public class SystemProperties {

    public static  String getProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String)(get.invoke(c, key, "unknown" ));
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return value;
        }
    }

    public static void setProperty(String key, String value) throws Exception {
        Class<?> c = Class.forName("android.os.SystemProperties");
        Method set = c.getMethod("set", String.class, String.class);
        set.invoke(c, key, value );
    }
}
