package com.ubtechinc.alpha.im.msghandler;

import android.os.Environment;

import com.google.protobuf.ByteString;
import com.ubtech.utilcode.utils.FileUtils;
import com.ubtech.utilcode.utils.LogUtils;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.GetBehaviorList;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ProtoBufferDispose;

import com.ubtrobot.mini.properties.sdk.PropertiesApi;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bob.xu on 2018/1/8.
 */

public class GetBehaviorListHandler implements IMsgHandler {
    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        ByteString requestBody = request.getBodyData();
        byte[] bodyBytes = requestBody.toByteArray();
        long requestSerial = request.getHeader().getSendSerial();

        GetBehaviorList.GetListRequest playBehaviorCommand  = (GetBehaviorList.GetListRequest) ProtoBufferDispose.unPackData(
                GetBehaviorList.GetListRequest.class,bodyBytes);
        String rootPath = playBehaviorCommand.getPath();
        List<String> behaviorList = getBehaviorList(rootPath);

        GetBehaviorList.GetListResponse.Builder builder = GetBehaviorList.GetListResponse.newBuilder();
        builder.addAllBehaviorList(behaviorList);

        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId,"1",requestSerial, builder.build() ,peer,null);
    }

    List<String> getBehaviorList(String rootPath) {
        List<File> files = FileUtils.listFilesInDirWithFilter(
                new File(PropertiesApi.getRootPath(), rootPath), ".xml", false);
        List<String> fileNames = new ArrayList<>();
        if(files != null && files.size() > 0) {
            for (int i = 0; i < files.size(); ++i) {
                fileNames.add(files.get(i).getName());
                LogUtils.w("logic", files.get(i).getName());
            }
        }
        return fileNames;
    }
}
