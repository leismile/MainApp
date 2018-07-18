package com.ubtechinc.protocollibrary.communite;


import com.ubtechinc.protocollibrary.protocol.MiniMessage;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Administrator on 2017/5/25.
 */

public class NullHandler implements IMsgHandler {

    @Override
    public void handleMsg(short requestCmdId, short responseCmdId, @NotNull MiniMessage request, @NotNull Object peer) {

    }
}
