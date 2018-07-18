package com.ubtechinc.alpha.im.msghandler;

import com.google.protobuf.ByteString;
import com.ubtech.utilcode.utils.LogUtils;
import com.ubtech.utilcode.utils.StringUtils;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.CmGetActionList;
import com.ubtechinc.alpha.CmNewActionInfo;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ProtoBufferDispose;
import com.ubtrobot.action.ActionApi;
import com.ubtrobot.motion.protos.Motion;

import java.util.ArrayList;
import java.util.List;

/**
 * @desc : 获取动作列表消息处理器
 * @author: wzt
 * @time : 2017/6/6
 * @modifier:
 * @modify_time:
 */

public class GetActionlistMsgHandler implements IMsgHandler {
    static final String TAG = "GetActionlistMsgHandler";

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        LogUtils.d(TAG, "GetActionlistMsgHandler");

        ByteString requestBody = request.getBodyData();
        byte[] bodyBytes = requestBody.toByteArray();
        long requestSerial = request.getHeader().getSendSerial();
        CmGetActionList.CmGetActionListRequest actionListRequest = (CmGetActionList.CmGetActionListRequest) ProtoBufferDispose.unPackData(
                CmGetActionList.CmGetActionListRequest.class, bodyBytes);
        LogUtils.d("request body : get action list  languageType = " + actionListRequest.getLanguageType());
        CmGetActionList.CmGetActionListResponse.Builder builder = CmGetActionList.CmGetActionListResponse.newBuilder();
        List<Motion.Action> actions = ActionApi.get().getActionList();

        if (actions == null) {
            actions = new ArrayList<>(0);
        }

        for (Motion.Action item : actions) {
            CmNewActionInfo.CmNewActionInfoResponse.Builder actionBuilder = CmNewActionInfo.CmNewActionInfoResponse.newBuilder();
            if (item.getId() != null) {
                actionBuilder.setActionId(item.getId());
            }
            actionBuilder.setActionType(item.getType());
            if (StringUtils.isEquals("EN", actionListRequest.getLanguageType())) {
                actionBuilder.setActionName(item.getEnName() == null ? "" : item.getEnName());
            } else if (StringUtils.isEquals("CN", actionListRequest.getLanguageType())
                    || StringUtils.isEquals("HK", actionListRequest.getLanguageType())) {
                actionBuilder.setActionName(item.getCnName() == null ? "" : item.getCnName());
            } else {
                actionBuilder.setActionName(item.getCnName() == null ? item.getCnName() : item.getCnName());
            }
            CmNewActionInfo.CmNewActionInfoResponse actionItem = actionBuilder.build();
            LogUtils.d("addActionList actionItem = " + actionItem.getActionId());
            builder.addActionList(actionItem);
        }
        LogUtils.d("response GetActionlist size = " + builder.getActionListCount());
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId,
                "1",
                requestSerial,
                builder.build(),
                peer, null);
    }

}
