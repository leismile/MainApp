/*
 *
 *  *
 *  *  *
 *  *  * Copyright (c) 2008-2017 UBT Corporation.  All rights reserved.  Redistribution,
 *  *  *  modification, and use in source and binary forms are not permitted unless otherwise authorized by UBT.
 *  *  *
 *  *
 *
 */

package com.ubtechinc.alpha.service;

import android.content.Context;

import com.ubtechinc.alpha.task.AbstractProxyService;
import com.ubtechinc.alpha.task.ProxyAppManageImpl;
import com.ubtechinc.alpha.task.ProxyGetWifiListImpl;
import com.ubtechinc.alpha.task.ProxyPowerOffTaskImpl;
import com.ubtechinc.alpha.task.ProxyServerRobotPhoneCommuniteImpl;
import com.ubtechinc.alpha.task.ProxyService;

import java.util.HashMap;
import java.util.Iterator;

/**
 * @desc : ProxyServic的管理器，在主服务中启动
 * @author: Logic
 * @email : logic.peng@ubtech.com
 * @time : 2017/4/21
 * @modifier:
 * @modify_time:
 */

public class ProxyServiceManager extends AbstractProxyService {

    private static ProxyServiceManager serviceManager;
    private final Context mContext;

    public static final String SERVICE_POWER_OFF = "alpha_power_off";
    public static final String SERVICE_SERVER_ROBOT_PHONE = "alpha_server_robot_phone";
    public static final String SERVICE_APP_MANAGE = "alpha_app_manage";
    public static final String SERVICE_GET_WIF_LIST = "get_wifi_list";
    private final HashMap<String, ProxyService> mServices = new HashMap<>();

    private ProxyServiceManager(Context cxt) {
        this.mContext = cxt;
    }

    public static ProxyServiceManager get(Context cxt) {
        if (serviceManager == null) {
            synchronized (ProxyServiceManager.class) {
                if (serviceManager == null)
                    serviceManager = new ProxyServiceManager(cxt);
            }
        }
        return serviceManager;
    }

    @Override
    public void onCreate() {
        initProxyService();
    }

    public void initProxyService() {
        initService(SERVICE_POWER_OFF);
        initService(SERVICE_SERVER_ROBOT_PHONE);
        initService(SERVICE_APP_MANAGE);
        initService(SERVICE_GET_WIF_LIST);
    }

    @Override
    public void onDestroy() {
        destroyService();
    }

    private void destroyService() {
        Iterator<String> iterator = mServices.keySet().iterator();
        while (iterator.hasNext()) {
            mServices.get(iterator.next()).onDestroy();
            mServices.remove(iterator.next());
        }
    }

    private ProxyService initService(String id) {
        if (mServices.containsKey(id)) {
            return mServices.get(id);
        }
        ProxyService p = null;
        switch (id) {
            case SERVICE_POWER_OFF:
                p = new ProxyPowerOffTaskImpl(mContext);
                break;
            case SERVICE_SERVER_ROBOT_PHONE:
                p = new ProxyServerRobotPhoneCommuniteImpl(mContext);
                break;
            case SERVICE_APP_MANAGE:
                p = new ProxyAppManageImpl(mContext);
                break;
            case SERVICE_GET_WIF_LIST:
                p = new ProxyGetWifiListImpl(mContext);
                break;
            default:
                break;
        }
        if (p == null) return null;
        mServices.put(id, p);
        p.onCreate();
        return p;
    }
}
