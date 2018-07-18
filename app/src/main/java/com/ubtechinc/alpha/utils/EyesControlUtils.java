package com.ubtechinc.alpha.utils;

import android.text.TextUtils;
import android.util.Log;

import com.ubtech.utilcode.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;

/**
 * Created by bob.xu on 2017/12/12.
 */

public class EyesControlUtils {
    private static String node_backlight = "/sys/devices/platform/st7789v_backlight/st7789v_backlight_update";  //控制眼睛灯亮度
    private static String node_backlight_open = "/sys/devices/platform/st7789v_backlight/st7789v_backlight_enable"; //控制眼睛灯开关
    private static final int NormalLight = 16; //正常亮度
    public static void openEyes() {
        openOrCloseEyes(true);
    }

    private static void openOrCloseEyes(boolean isOpen){
        Log.i("EyesControlUtils","openOrCloseEyes==========" + isOpen);
        String open = isOpen?"1":"0";
        FileUtils.writeFileFromString(node_backlight_open,open,false);
    }
    public static void closeEyes() {
        openOrCloseEyes(false);
    }

    private static boolean setBrightness(int light) {
        if (light > 31 || light <0) {
            throw new RuntimeException("参数不合法，light 取值范围：1-31");
        }
        return FileUtils.writeFileFromString(node_backlight,String.valueOf(light),false);

    }

    public static boolean isEyesOpen(){

        String str = FileUtils.readFile2String(node_backlight_open, "utf-8");

        Log.i("EyesControlUtils","isEyesOpen==========" + str);

        return TextUtils.isEmpty(str)?false:(Integer.parseInt(str) == 1);
    }
}