package com.ubtechinc.alpha.im.msghandler;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.GetRobotIp;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.call.CallConfiguration;
import com.ubtrobot.master.interactor.MasterInteractor;
import com.ubtrobot.master.service.ServiceProxy;
import com.ubtrobot.master.skill.SkillsProxy;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;

import static com.ubtech.utilcode.utils.bugly.CrashReporter.TAG;

/**
 * Created by bob.xu on 2018/1/16.
 */

public class GetRobotIpHandler implements IMsgHandler{

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        long requestSerial = request.getHeader().getSendSerial();
        WifiManager wifiManager = (WifiManager) AlphaApplication.getContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        startDownloadService();
        Log.d("wifiInfo", wifiInfo.toString());
        String ssid = "";
        String ip = "";
        if (wifiInfo != null) {
            ssid = wifiInfo.getSSID();
            ip = intToIp(wifiInfo.getIpAddress());
        }
        GetRobotIp.GetIpResponse response = GetRobotIp.GetIpResponse.newBuilder().setIp(ip).setWifissid(ssid).build();
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId,"1",requestSerial, response ,peer,null);
        Log.d("GetRobotIpHandler","handleMsg---ip:"+ip+", ssid = "+ssid);
    }

    private String intToIp(int i) {

        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }

    private void startDownloadService(){
        String packageName = com.ubtech.utilcode.utils.Utils.getContext().getPackageName();
        MasterInteractor interactor = Master.get().getOrCreateInteractor("robot:" + packageName);
        ServiceProxy serviceProxy = interactor.createSystemServiceProxy("sync");
        serviceProxy.call("/start_socket_server", new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {
                Log.d(TAG,"start_socket_server success");
            }

            @Override
            public void onFailure(Request request, CallException e) {
                Log.d(TAG,"start_socket_server failure " + e.getCode() + e.getMessage());
            }
        });
    }
}
