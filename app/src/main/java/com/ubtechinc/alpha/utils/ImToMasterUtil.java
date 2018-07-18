package com.ubtechinc.alpha.utils;


import android.util.Log;

import com.google.protobuf.Any;
import com.ubtechinc.alpha.AlGetAlubmInfo;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.service.ServiceProxy;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;


/**
 * Created by Ian on 17-9-29.
 */

public class ImToMasterUtil {

    private static final String TAG = "ImToMasterUtil";
    private int responseCmdId ;
    private String peer ;
    private long requestSerial ;
    private String host ;
    private String path ;
    public  ImToMasterUtil(int responseCmdId,String peer,long requestSerial,String host,String path) {
        this.responseCmdId = responseCmdId ;
        this.peer = peer ;
        this.requestSerial=requestSerial;
        this.host = host ;
        this.path = path ;
    }
    public void sendToMaster(final Any any) {
        ServiceProxy serviceProxy = Master.get().getGlobalContext().createSystemServiceProxy(host);
        ProtoParam param = ProtoParam.create(any);
        serviceProxy.call(path, param, new ResponseCallback() {
            @Override
            public void onResponse(Request request, Response response) {
                Log.i(TAG, "get an response from bus. response:" + response);
                sendPacket(response);
            }
            @Override
            public void onFailure(Request req, CallException e) {
                // 失败。处理异常，根据 e.getCode() 获取到的错误码判断
                Log.e(TAG, "Call service fail! path:" + req.getPath());
            }
        });

//        MasterService service = Master.get().getMasterService(host);
//        ProtoParam param = ProtoParam.encode(any);
//        Cancelable cancelable = service.call(path, param, new ResponseCallback() {
//            @Override
//            public void onResponse(Request request, Response response) {
//                Log.i(TAG, "get an response from bus. response:" + response);
//                sendPacket(response);
//            }
//
//            @Override
//            public void onStartFailure(Request req, CallException e) {
//                // 失败。处理异常，根据 e.getCode() 获取到的错误码判断
//                Log.e(TAG, "Call service fail! path:" + req.getPath());
//            }
//        });
    }

    private void sendPacket(Response response) {
        Log.i(TAG, "sendPacket:"+responseCmdId+peer);

        AlGetAlubmInfo.AlGetAlubmInfoResponse.Builder builder=AlGetAlubmInfo.AlGetAlubmInfoResponse.newBuilder();
        try {
//            Log.i(TAG, "sendPacket:"+"size"+ProtoParam.from(response.getParam()).getAny().getSerializedSize());
            ProtoParam<Any> protoParam = ProtoParam.from(response.getParam(),Any.class);

            Any any = protoParam.getAny();
            builder.setBodyData(any);
        } catch (ProtoParam.InvalidProtoParamException e) {
            Log.i(TAG, "sendPacket:"+"InvalidProtoParamException"+e.getMessage());

            e.printStackTrace();
        }

        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId,"1",requestSerial,builder.build(),peer,null);
    }

}
