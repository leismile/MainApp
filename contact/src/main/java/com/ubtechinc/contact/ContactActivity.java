package com.ubtechinc.contact;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ubtechinc.alpha.CmQueryContactList;
import com.ubtechinc.contact.phone.DefaultPhone;

import java.util.List;

public class ContactActivity extends Activity {

    private ListView listView;
    private List<CmQueryContactList.CmContactInfo> contactInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        listView = findViewById(R.id.ls_contact);
        contactInfoList = Contact.getContactFunc().queryContactList(0, 0);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new DefaultPhone().call(contactInfoList.get(position).getPhone());
            }
        });
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return contactInfoList.size();
            }

            @Override
            public Object getItem(int position) {
                return contactInfoList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null) {
                    TextView textView = new TextView(ContactActivity.this);
                    textView.setTag(position);
                    convertView = textView;
                }
                ((TextView)convertView).setText(contactInfoList.get(position).getName());
                return convertView;
            }
        });
    }
}