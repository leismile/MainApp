package com.ubtechinc.alpha.im.msghandler;

import android.text.TextUtils;

import com.ubtech.utilcode.utils.SystemProperty;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.GetRobotConfiguration;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.im.IMCmdId;
import com.ubtechinc.alpha.robotinfo.RobotState;
import com.ubtechinc.alpha.utils.SharedPreferenceUtil;
import com.ubtechinc.alpha.utils.SystemUtils;
import com.ubtechinc.alpha.utils.WifiUtils;
import com.ubtechinc.contact.Contact;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtrobot.sys.SysApi;
import com.ubtrobot.ulog.ULog;
import java.util.regex.Pattern;

/**
 * @author：wululin
 * @date：2017/11/10 16:24
 * @modifier：ubt
 * @modify_date：2017/11/10 16:24
 * [A brief description]
 * version
 */

public class GetRobotConfigHandler implements IMsgHandler {
    private static final Pattern androidPattern =
        Pattern.compile("^Alpha_Mini-\\d{4}-\\d{4}-\\d{4}-V\\d.\\d.\\d");
    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        final long requestSerial = request.getHeader().getSendSerial();
        String wifiName = WifiUtils.getCurrConnWifi();
        if(TextUtils.isEmpty(wifiName)){
            wifiName = "";
        }
        GetRobotConfiguration.GetRobotConfigurationResponse.Builder builder =  GetRobotConfiguration.GetRobotConfigurationResponse.newBuilder();
        builder.setWifiname(wifiName);
        builder.setTotalspace(SystemUtils.getSDTotalSize());
        builder.setAvailablespace(SystemUtils.getSDAvailableSize());
        builder.setSysversion(SystemUtils.getSysVersion());
        builder.setMainappversion(SystemUtils.getMainService());
        builder.setSerail(RobotState.get().getSid());
        builder.setBatterycapacity("4060mAh");
        builder.setOperator(SystemUtils.getProvidersName(AlphaApplication.getContext()));  //运营商
        String imei = SystemUtils.getIMEI(AlphaApplication.getContext());
        String emid = SystemUtils.getMEID(AlphaApplication.getContext());
        builder.setImei(imei == null? "" : imei);   //IMEI
        builder.setEmid(emid == null? "" : emid);   //EMID
        String sys_version = SystemProperty.getProperty("ro.build.display.id");
        if (!TextUtils.isEmpty(sys_version) && androidPattern.matcher(sys_version).matches()) {
            sys_version = sys_version.substring(sys_version.length() - 6).toLowerCase();
        } else {
            sys_version = "v0.0.0";
        }
        builder.setHardwareversion(sys_version);
        String firstBindTime = SharedPreferenceUtil.readString(AlphaApplication.getContext(), "first_bind_time", null);
        builder.setFirstbindtime(firstBindTime == null ? "" : firstBindTime);
        builder.setIsOpenData(Contact.getContactFunc().isOpenData());
        builder.setIsSimExist(Contact.getContactFunc().simExist());
        builder.setIsAdbEnable(SystemUtils.getUsbDebugEnable(AlphaApplication.getContext()));
        builder.setMac(WifiUtils.getMacAddress());
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, IMCmdId.IM_VERSION,requestSerial,builder.build(),peer,null);
    }
}
