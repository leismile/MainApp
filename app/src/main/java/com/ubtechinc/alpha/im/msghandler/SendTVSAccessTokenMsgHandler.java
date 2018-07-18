package com.ubtechinc.alpha.im.msghandler;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.protobuf.ByteString;
import com.ubtech.utilcode.utils.LogUtils;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.TVSSendAccessToken;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.utils.SharedPreferenceUtil;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ProtoBufferDispose;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by bob.xu on 2017/8/8.
 */

public class SendTVSAccessTokenMsgHandler implements IMsgHandler {
    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {

        ByteString requestBody = request.getBodyData();
        byte[] bodyBytes = requestBody.toByteArray();
        long requestSerial = request.getHeader().getSendSerial();
        TVSSendAccessToken.TVSSendAccessTokenRequest accessTokenRequest = (TVSSendAccessToken.TVSSendAccessTokenRequest) ProtoBufferDispose.unPackData(
                TVSSendAccessToken.TVSSendAccessTokenRequest.class, bodyBytes);

        Bundle bundle = new Bundle();
        bundle.putString("access_token", accessTokenRequest.getAccessToken());
        bundle.putString("fresh_token", accessTokenRequest.getFreshToken());
        bundle.putLong("expire_time", accessTokenRequest.getExpireTime());
        bundle.putString("client_id", accessTokenRequest.getClientId());
        bundle.putString("scene","refresh");
        setTVSAccessToken(bundle);
        //send Response
        TVSSendAccessToken.TVSSendAccessTokenResponse response = TVSSendAccessToken.TVSSendAccessTokenResponse.newBuilder().setIsSuccess(true).build();
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, "1", requestSerial, response, peer, null);
    }

    public void setTVSAccessToken(Bundle bundle) {
        //TODO:临时改成广播方案， 后续等接入了终端通信模块再改过来
        Intent intent = new Intent("tvs.set.accesstoken");
        intent.putExtra("access_token", bundle);
        AlphaApplication.getContext().sendBroadcast(intent);
        LogUtils.d("TVSAccessToken", "setTVSAccessToken---Has Send Broadcast");
        markFirstBindTime();
    }

    private void markFirstBindTime() {
        String firstBind = SharedPreferenceUtil
                .readString(AlphaApplication.getContext(), "first_bind_time", null);
        if (TextUtils.isEmpty(firstBind)) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
            Date curDate = new Date(System.currentTimeMillis());
            String str = formatter.format(curDate);
            SharedPreferenceUtil.saveString(AlphaApplication.getContext(), "first_bind_time", str);
        }
    }
}
