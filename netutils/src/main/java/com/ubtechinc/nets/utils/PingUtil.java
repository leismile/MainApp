package com.ubtechinc.nets.utils;

import com.ubtech.utilcode.utils.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PingUtil {

    public static final String PING_BAIDU = "www.baidu.com";

    public static boolean pingBaidu() {
        return ping(PING_BAIDU);
    }

    public static boolean ping(String str) {
        boolean result = false;
        Process p;
        try {
            //ping -c 3 -w 100  中  ，-c 是指ping的次数 3是指ping 1次 ，-w 3  以秒为单位指定超时间隔，是指超时时间为3秒
            p = Runtime.getRuntime().exec("ping -c 1 -w 3 " + str);
            int status = p.waitFor();

            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            LogUtils.d("ping Return ============" + buffer.toString());
            if (status == 0) {
                result = true;
            } else {
                result = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }
}
