package com.ubtechinc.alpha.service.jimucar.handler;

import com.ubtechinc.alpha.JimuCarListenType;
import com.ubtrobot.transport.message.Responder;

/**
 * @author : kevin.liu@ubtrobot.com
 * @description :
 * @date : 2018/7/9
 * @modifier :
 * @modify time :
 */
public class JimuCarResponder {

    JimuCarListenType.listenType mListenType;

    Responder mResponder;

    public JimuCarResponder(JimuCarListenType.listenType type,Responder responder){
        this.mListenType = type;
        this.mResponder=responder;
    }

    public final JimuCarListenType.listenType listenType() {
        return mListenType;
    }

    public final Responder responder(){
        return this.mResponder;
    }

}
