package com.ubtechinc.alpha.service.jimucar.handler;

import com.google.protobuf.BytesValue;
import com.ubtechinc.alpha.JimuCarControl;
import com.ubtechinc.alpha.JimuErrorCodeOuterClass;
import com.ubtechinc.alpha.service.jimucar.BlePacket;
import com.ubtechinc.alpha.service.jimucar.JimuCarNotifyCallbackDispatcher;
import com.ubtrobot.master.param.ProtoParam;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author : kevin.liu@ubtrobot.com
 * @description :
 * @date : 2018/7/9
 * @modifier :
 * @modify time :
 */
public class ControlCarHandler implements IBleHandler {

    public static ControlCarHandler get() {
        return new ControlCarHandler();
    }

    @Override
    public synchronized void onBleResponse(BlePacket packet, List<JimuCarResponder> responder) {
        //
        final List<JimuCarResponder> jimuCarResponders = Collections.unmodifiableList(responder);
        final byte[] cmd = BytesReader.read(packet.getCmd(), 1);
        if (cmd != null && cmd.length == 1) {
            byte[] ret = BytesReader.read(packet.getParams(), 1);
            final JimuCarControl.JimuCarControlResponse.Builder builder = JimuCarControl.JimuCarControlResponse.newBuilder();
            if (ret != null && ret[0] == 0x00) {
                builder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.CMD_EXEC_SUCCESS);
            } else {
                builder.setErrorCode(JimuErrorCodeOuterClass.JimuErrorCode.CMD_EXEC_FAILURE);
            }
            builder.setCmd(JimuCarControl.Control.UNRECOGNIZED); //
            try {
            final ProtoParam<BytesValue> param = ProtoParam.create(BytesValue.newBuilder().setValue(builder.build().toByteString()).build());
                JimuCarResponder jimuCarResponder;
                for (Iterator<JimuCarResponder> iter = jimuCarResponders.listIterator(); iter.hasNext(); ) {
                    jimuCarResponder = iter.next();
                    jimuCarResponder.responder().respondSuccess(param);
                    JimuCarNotifyCallbackDispatcher.unregisterResponder(cmd[0], jimuCarResponder);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


}
