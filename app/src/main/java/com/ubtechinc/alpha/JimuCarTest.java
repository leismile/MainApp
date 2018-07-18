package com.ubtechinc.alpha;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.protobuf.BytesValue;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.ubtechinc.alpha.service.jimucar.BlePacket;
import com.ubtechinc.alpha.service.jimucar.JimuCarSkill;
import com.ubtechinc.alpha.service.jimucar.JimuConstants;
import com.ubtechinc.services.alphamini.R;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.context.ContextRunnable;
import com.ubtrobot.master.interactor.MasterInteractor;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.skill.SkillsProxy;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JimuCarTest extends Activity {

    ListView mListView;
    private BaseAdapter adapter;
    private List<JimuCarGetBleList.JimuCarBle> datas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jimu_car_test);
        mListView = findViewById(R.id.list_view);
        datas = new ArrayList<>();
        adapter = new BleCarAdapter();
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                getSkill();
                final ProtoParam<StringValue> param = ProtoParam.create(StringValue.newBuilder().setValue(datas.get(position).getMac()).build());
                aSkillsProxy.call("/jimucar/connect_car", param, new ResponseCallback() {
                    @Override
                    public void onResponse(Request request, Response response) {
                        try {
                            final BytesValue bytesValue = ProtoParam.from(response.getParam(), BytesValue.class).getProtoMessage();
                            final JimuCarConnectBleCar.JimuCarConnectBleCarResponse jimuCarConnectBleCarResponse = JimuCarConnectBleCar.JimuCarConnectBleCarResponse.parseFrom(bytesValue.getValue());
                            ((TextView) view).append("状态：" + jimuCarConnectBleCarResponse.getState() + "\n");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Request request, CallException e) {
                        Log.d(JimuCarSkill.TAG, "onFailure:" + e.getMessage());

                    }
                });
            }
        });
    }

    public void BlePacketTest(View view) {
        final BlePacket blePacket = BlePacket.get();
        blePacket.wrap(JimuConstants.CMD_HAND_SHAKE, new byte[1]);
        Log.d("BlePacketTest", "bytes:" + Arrays.toString(blePacket.getBytes()) + "===" + blePacket.getHexString() + "===hex:" + bytes2HexString(blePacket.getBytes()));
    }

    public static String bytes2HexString(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }

    public void readMainBoard(View view) {
        getSkill();
        aSkillsProxy.call("/jimucar/get_main_board_info", new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {
                Log.d(JimuCarSkill.TAG, "get_main_board_info onResponse");
            }

            @Override
            public void onFailure(Request request, CallException e) {

            }
        });
    }

    public void checkCar(View view) {
        getSkill();
        aSkillsProxy.call("/jimucar/check_car", new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {

            }

            @Override
            public void onFailure(Request request, CallException e) {

            }
        });
    }

    private void getSkill() {
        MasterInteractor interactor = Master.get().getOrCreateInteractor("robot:" + getPackageName());
        aSkillsProxy = interactor.createSkillsProxy();
    }

    public void readCarPower(View view) {
        getSkill();
        aSkillsProxy.call("/jimucar/get_car_power", new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {

            }

            @Override
            public void onFailure(Request request, CallException e) {

            }
        });

    }

    public void getDevicesIDs(View view) {
        getSkill();
        aSkillsProxy.call("/jimucar/get_devices_id", new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {

            }

            @Override
            public void onFailure(Request request, CallException e) {

            }
        });
    }

    public void getIRDistance(View view) {
        getSkill();
        aSkillsProxy.call("/jimucar/get_ir_distance", new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {

            }

            @Override
            public void onFailure(Request request, CallException e) {

            }
        });
    }

    public void goForward(View view) {
        getSkill();
        aSkillsProxy.call("/jimucar/go_forward", new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {

            }

            @Override
            public void onFailure(Request request, CallException e) {

            }
        });
    }

    public void goBack(View view) {
        getSkill();
        aSkillsProxy.call("/jimucar/go_back", new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {

            }

            @Override
            public void onFailure(Request request, CallException e) {

            }
        });
    }

    public void turnLeft(View view){
        getSkill();
        aSkillsProxy.call("/jimucar/turn_left", new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {

            }

            @Override
            public void onFailure(Request request, CallException e) {

            }
        });
    }

    public void turnRight(View view) {
        getSkill();
        aSkillsProxy.call("/jimucar/turn_right", new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {

            }

            @Override
            public void onFailure(Request request, CallException e) {

            }
        });
    }

    public void stopGo(View view) {
        getSkill();
        aSkillsProxy.call("/jimucar/stop_going", new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {

            }

            @Override
            public void onFailure(Request request, CallException e) {

            }
        });
    }


    class BleCarAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int position) {
            return datas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final TextView textView = new TextView(JimuCarTest.this);
            textView.append(datas.get(position).getName() + "\n");
            textView.append(datas.get(position).getMac() + "\n");
            return textView;
        }
    }

    private SkillsProxy aSkillsProxy;

    public void startJimuCar(View view) {
        getSkill();
        aSkillsProxy.call("/jimucar/enter_drive", new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {
                Log.d(JimuCarSkill.TAG, "enter_drive");
                try {
                    final BytesValue bytesValue = ProtoParam.from(response.getParam(), BytesValue.class).getProtoMessage();
                    final JimuCarDriveMode.ChangeJimuDriveModeResponse changeJimuDriveModeResponse = JimuCarDriveMode.ChangeJimuDriveModeResponse.parseFrom(bytesValue.getValue());

                    Log.d(JimuCarSkill.TAG, "enter_drive:auto_connect:" + changeJimuDriveModeResponse.getState());
                    datas.clear();
                    datas.addAll(changeJimuDriveModeResponse.getBleList());
                    adapter.notifyDataSetChanged();
                } catch (ProtoParam.InvalidProtoParamException e) {
                    e.printStackTrace();
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Request request, CallException e) {
                Log.d(JimuCarSkill.TAG, "onFailure:" + e.getMessage());
            }
        });
    }


    public void stopJimuCar(View v) {
        getSkill();
        aSkillsProxy.call("/jimucar/quit_drive", new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {
                Log.d(JimuCarSkill.TAG, "quit_drive");
            }

            @Override
            public void onFailure(Request request, CallException e) {

            }
        });

//        Master.get().execute(JimuCarSkill.class, new ContextRunnable<JimuCarSkill>() {
//            @Override
//            public void run(JimuCarSkill skillA) {
//                skillA.stopSkill();
//                Log.d(JimuCarSkill.TAG, "quit_drive");
//            }
//        });
    }

    public void scanBleList(View view) {
        getSkill();
        aSkillsProxy.call("/jimucar/scan_ble_list", new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {
                Log.d(JimuCarSkill.TAG, "scan_ble_list");
                datas.clear();
                try {
                    final BytesValue bytesValue = ProtoParam.from(response.getParam(), BytesValue.class).getProtoMessage();
                    final JimuCarGetBleList.GetJimuCarBleListResponse carBleListResponse = JimuCarGetBleList.GetJimuCarBleListResponse.parseFrom(bytesValue.getValue());
//                    Log.d(JimuCarSkill.TAG,"scanBleList:count:"+carBleListResponse.getBleCount());
                    datas.clear();
                    datas.addAll(carBleListResponse.getBleList());
                    adapter.notifyDataSetChanged();
                } catch (ProtoParam.InvalidProtoParamException e) {
                    e.printStackTrace();
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Request request, CallException e) {
                Log.d(JimuCarSkill.TAG, "onFailure:" + e.getMessage());
            }
        });
    }
}
