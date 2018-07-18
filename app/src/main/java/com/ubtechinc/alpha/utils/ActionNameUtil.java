package com.ubtechinc.alpha.utils;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import com.ubtechinc.alpha.AlDeleteActionFile;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.network.business.ActionDetail;
import com.ubtechinc.alpha.network.module.ActionDetailModule;

import com.ubtechinc.nets.ResponseListener;
import com.ubtechinc.nets.http.ThrowableWrapper;
import com.ubtrobot.provider.ActionStore;
import com.ubtrobot.ubx.UbxFlow;
import com.ubtrobot.ubx.utils.UbxFlowHelper;
import com.ubtrobot.ubx.utils.UbxParser;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ubtrobot.provider.ActionStore.Action.Columns.ACTION_HEADER;
import static com.ubtrobot.provider.ActionStore.Action.Columns.ACTION_ID;
import static com.ubtrobot.provider.ActionStore.Action.Columns.ACTION_TIME;
import static com.ubtrobot.provider.ActionStore.Action.Columns.ACTION_VER;
import static com.ubtrobot.provider.ActionStore.Action.Columns.CN_NAME;

/**
 * @author wzt
 * @date 2017/6/9
 * @Description 动作名称的后台查询、数据库操作类
 * @modifier
 * @modify_time
 */

public class ActionNameUtil {

    public static void insertActionInfo(String ids) {
        findActionNameInServer(ids, "EN");
        findActionNameInServer(ids, "CN");
    }

    private static void findActionNameInServer(final String ids, final String language) {
        ActionDetail.getInstance().requestAcionDetail(ids, new ResponseListener<ActionDetailModule.Response>() {
            @Override
            public void onError(ThrowableWrapper e) {
                e.printStackTrace();
            }

            @Override
            public void onSuccess(ActionDetailModule.Response response) {
                String id = response.data.result.actionOriginalId;
                String cn_name = response.data.result.actionName;
                String type = response.data.result.actionType;

                ContentResolver resolver = AlphaApplication.getContext().getContentResolver();
                Cursor cursor = resolver.query(ActionStore.Action.getContentUri(),
                        null, ACTION_ID + " = ? OR ",
                        new String[]{id}, null);
                boolean update = cursor != null && cursor.getCount() == 1;
                cursor.close();
                UbxFlow flow = UbxParser.parseUbxFile(Constants.ACTION_PATH + File.separator + id);
                if (flow != null) {
                    ContentValues values = new ContentValues();
                    values.put(ACTION_ID, id);
                    values.put(CN_NAME, cn_name);
                    values.put(ACTION_TIME, UbxFlowHelper.calActionTime(flow));
                    values.put(ACTION_VER, flow.getVersion());
                    values.put(ACTION_HEADER, flow.getHeader());
                    if (update) {
                        resolver.update(ActionStore.Action.getContentUri(), values, ACTION_ID + " = ?", new String[]{id});
                    } else {
                        resolver.insert(ActionStore.Action.getContentUri(), values);
                    }
                }
            }
        });
    }
}
