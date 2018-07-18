package com.ubtechinc.contact;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;

import com.ubtechinc.alpha.CmQueryCallRecord;
import com.ubtechinc.contact.model.CallRecordInfo;
import com.ubtechinc.contact.model.CallRecordModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @desc : 默认通话记录接口实现
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/1/31
 */

public class DefaultCallRecord implements ICallRecord {

    private static final String TAG = "DefaultCallRecord";
    private final static int PAGE_CAPACITY = 150;
    private Context context = Contact.getInstance().getContext();
    private List<CmQueryCallRecord.CmCallRecordInfo> cmCallRecordInfoList = new ArrayList<>();
    private Map<String, CmQueryCallRecord.CmCallRecordInfo> callRecordInfoMap = new HashMap<>();
    private CallRecordModel callRecordModel = new CallRecordModel(context);
    private long versionCode;

    public DefaultCallRecord() {
        initCallRecordList();
    }

    @Override
    public List<CmQueryCallRecord.CmCallRecordInfo> queryCallRecord(int position, int versionCode) {
        return subList(position, versionCode);
    }

    @Override
    public int getCallRecordSize(int versionCode) {
        return getCallRecoreList(versionCode).size() == 0 ? 0 : getCallRecoreList(versionCode).size() / PAGE_CAPACITY + 1;
    }

    private List<CmQueryCallRecord.CmCallRecordInfo> subList(int position, int versionCode) {
        versionCode = versionCode < 0 ? 0 : versionCode;
        List<CmQueryCallRecord.CmCallRecordInfo> list = new ArrayList<>(PAGE_CAPACITY);
        List<CmQueryCallRecord.CmCallRecordInfo> list1 = getCallRecoreList(versionCode);
        int count = list1.size();
        Log.d(TAG, " subList - count : " + count + " versionCode : " + versionCode);
        for(int i = position * PAGE_CAPACITY ; i < position * PAGE_CAPACITY + PAGE_CAPACITY && i < count; i ++) {
            list.add(list1.get(i));
        }
        return list;
    }

    @Override
    public void update() {
        Log.d(TAG, "update");
        ContentResolver resolver = Contact.getInstance().getContext().getContentResolver();
        @SuppressLint("MissingPermission") Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI, // 查询通话记录的URI
                new String[]{CallLog.Calls.CACHED_NAME// 通话记录的联系人
                        , CallLog.Calls.NUMBER// 通话记录的电话号码
                        , CallLog.Calls.DATE// 通话记录的日期
                        , CallLog.Calls.DURATION// 通话时长
                        , CallLog.Calls.TYPE}// 通话类型
                , null, null, CallLog.Calls.DEFAULT_SORT_ORDER// 按照时间逆序排列，最近打的最先显示
        );
        // 3.通过Cursor获得数据
        List<CallRecordInfo> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
            String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            long dateLong = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)) / 1000;
            int duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));
            int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
            Map<String, String> map = new HashMap<String, String>();
            //TODO 查询名称
            CmQueryCallRecord.CmCallRecordInfo cmCallRecordInfo = null;
            name = Contact.getContactFunc().containsPhoneNumber(number);
            if(name == null) {
                name = number;
            }
            CallRecordInfo callRecordInfo = new CallRecordInfo();
            callRecordInfo.setDateLong(dateLong);
            callRecordInfo.setName(name);
            callRecordInfo.setType(type);
            callRecordInfo.setDuration(duration);
            if((type == CallLog.Calls.VOICEMAIL_TYPE || type ==CallLog. Calls.ANSWERED_EXTERNALLY_TYPE || type ==CallLog.Calls.BLOCKED_TYPE) && innerHalfYear(dateLong)) {
                continue;
            } else {
                list.add(callRecordInfo);
            }
        }
        int size = list.size();
        boolean isAdd = false;
        for(int i = size - 1; i >= 0; i --) {
            CallRecordInfo callRecordInfo = list.get(i);
            String key = getKey(callRecordInfo);
            if(!callRecordInfoMap.containsKey(key)) {
                versionCode = callRecordModel.add(callRecordInfo);
                callRecordInfo.setVersionCode(versionCode);
                Log.d(TAG, " update - versionCode : " + versionCode);
                CmQueryCallRecord.CmCallRecordInfo cmCallRecordInfo = transfer(callRecordInfo);
                cmCallRecordInfoList.add(cmCallRecordInfo);
                if(callRecordInfo.getType() == CallLog.Calls.INCOMING_TYPE || callRecordInfo.getType() == CallLog.Calls.OUTGOING_TYPE) {
                    PhoneEventNotify.getInstance().notifyCallFinished(callRecordInfo.getDuration()*1000);
                } else if(callRecordInfo.getType() == CallLog.Calls.MISSED_TYPE){
                    PhoneEventNotify.getInstance().notifyCallFinished(callRecordInfo.getDuration()*1000);
                }
                putMap(key, cmCallRecordInfo);
                isAdd = true;
            }
        }
        if(isAdd) {
            sortCallRecord();
        }
    }

    private String getKey(CallRecordInfo callRecordInfo) {
        String key = String.valueOf(callRecordInfo.getDuration()+callRecordInfo.getDateLong()+callRecordInfo.getType());
        return key;
    }

    @Override
    public long getCallRecordVersionCode() {
        Log.d(TAG, " getCallRecordVersionCode -- versionCode : " + versionCode);
        return versionCode;
    }

    private List<CmQueryCallRecord.CmCallRecordInfo> getCallRecoreList(int versionCode) {
        List<CmQueryCallRecord.CmCallRecordInfo> list = new ArrayList<>();
        for(CmQueryCallRecord.CmCallRecordInfo cmCallRecordInfo : cmCallRecordInfoList) {
            if(cmCallRecordInfo.getCallId() > versionCode) {
                list.add(cmCallRecordInfo);
            }
        }
        return list;
    }

    public CallRecordInfo transfer(CmQueryCallRecord.CmCallRecordInfo cmCallRecordInfo) {
        CallRecordInfo callRecordInfo = new CallRecordInfo();
        callRecordInfo.setDateLong(cmCallRecordInfo.getCallTime());
        callRecordInfo.setDuration(cmCallRecordInfo.getDuration());
        callRecordInfo.setName(cmCallRecordInfo.getCallerName() != null ? cmCallRecordInfo.getCallerName() : "");
        callRecordInfo.setType(cmCallRecordInfo.getType());
        return callRecordInfo;
    }

    public CmQueryCallRecord.CmCallRecordInfo transfer(CallRecordInfo callRecordInfo) {
        CmQueryCallRecord.CmCallRecordInfo cmCallRecordInfo = CmQueryCallRecord.CmCallRecordInfo.newBuilder().setCallerName(callRecordInfo.getName()).setDuration(callRecordInfo.getDuration()).setType(callRecordInfo.getType()).setCallTime(callRecordInfo.getDateLong()).setCallId(callRecordInfo.getVersionCode()).build();
        return cmCallRecordInfo;
    }


    // 判断时间不超过半年
    private boolean innerHalfYear(long recordTime) {
        return System.currentTimeMillis() - recordTime < 6 * 30 * 24 * 60 * 60 * 1000;
    }

    public void initCallRecordList() {
        List<CallRecordInfo> callRecordInfoList = callRecordModel.query(0);
        for(CallRecordInfo callRecordInfo : callRecordInfoList) {
            CmQueryCallRecord.CmCallRecordInfo cmCallRecordInfo =  transfer(callRecordInfo);
            putMap(getKey(callRecordInfo), cmCallRecordInfo);
            cmCallRecordInfoList.add(cmCallRecordInfo);
            versionCode = versionCode > callRecordInfo.getVersionCode() ? versionCode : callRecordInfo.getVersionCode();
        }
        sortCallRecord();
    }

    private void sortCallRecord() {
        Collections.sort(cmCallRecordInfoList, new Comparator<CmQueryCallRecord.CmCallRecordInfo>() {
            @Override
            public int compare(CmQueryCallRecord.CmCallRecordInfo o1, CmQueryCallRecord.CmCallRecordInfo o2) {
                return (int)(o2.getCallId() - o1.getCallId());
            }
        });
    }

    private void putMap(String key, CmQueryCallRecord.CmCallRecordInfo cmCallRecordInfo) {
        callRecordInfoMap.put(key, cmCallRecordInfo);
    }
}
