package com.ubtechinc.contact;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ubtechinc.contact.util.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @desc : 联系人数据库
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/2/27
 */

public class ContactProvider extends ContentProvider {

    private static final String TAG = "ContactProvider";
    private ArrayList<String> contactList = new ArrayList<>();
    private static final String AUTHORITIES = "com.provider.ubtechinc.contact";
    private Uri contactUri = Uri.parse("content://com.provider.ubtechinc.contact");
    private static final String KEY_CONTACT = "key_contact";
    private static final String KEY_CONTACT_ADD = "key_contact_add";
    private static final String KEY_CONTACT_DELTE = "key_contact_delete";
    private static final int KEY_ID_CONTACT = 0;
    private static final String SPLIT_SYMBOL = ":";
    private static final String[] COLUMN_NAME = {"value"};
    private static UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITIES, KEY_CONTACT, KEY_ID_CONTACT);
    }

    @Override
    public boolean onCreate() {
        Log.d(TAG, " onCreate ");
        initContactList();
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    private void initContactList() {
        String str = SharedPreferenceUtil.readString(getContext(), KEY_CONTACT, null);
        if(str != null) {
            String[] strings = str.split(SPLIT_SYMBOL);
            if(strings.length != 0) {
                contactList.addAll(Arrays.asList(strings));
            }
        } else {
        }
    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        Log.d(TAG, " call -- method : " + method + " arg : " + arg);
        Bundle bundle = new Bundle();
        if(method.equals(KEY_CONTACT)) {
            bundle.putStringArrayList(KEY_CONTACT, contactList);
        } else if(method.equals(KEY_CONTACT_ADD)) {
            if(arg != null) {
                if(!contactList.contains(arg)) {
                    contactList.add(arg);
                    updateContact();
                }
            }
        } else if(method.equals(KEY_CONTACT_DELTE)) {
            if(arg != null) {
                boolean result = contactList.remove(arg);
                if(result) {
                    updateContact();
                }
            }
        }
        return bundle;
    }

    private void updateContact() {
        StringBuilder stringBuilder = new StringBuilder();
        boolean first = true;
        for(String str : contactList) {
            if(!first) {
                stringBuilder.append(SPLIT_SYMBOL);
            } else {
                first = false;
            }
            stringBuilder.append(str);
        }
        String value = stringBuilder.toString();
        SharedPreferenceUtil.saveString(getContext(), KEY_CONTACT, value);
        Log.d(TAG, " value : " + value);
        getContext().getContentResolver().notifyChange(contactUri, null);
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
