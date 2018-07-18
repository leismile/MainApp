package com.ubtechinc.alpha.service.jimucar.handler;

import com.ubtechinc.alpha.service.jimucar.BlePacket;
import com.ubtrobot.transport.message.Responder;

import java.util.List;

/**
 * @author : kevin.liu@ubtrobot.com
 * @description :
 * @date : 2018/7/5
 * @modifier :
 * @modify time :
 */
public interface IBleHandler {
    void onBleResponse(BlePacket packet,List<JimuCarResponder> responder);

}
