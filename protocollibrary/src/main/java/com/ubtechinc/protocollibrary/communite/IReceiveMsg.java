package com.ubtechinc.protocollibrary.communite;


import com.ubtechinc.protocollibrary.communite.old.IOldMsgDispather;

/**
 * Created by Administrator on 2017/5/25.
 */

public interface IReceiveMsg {
    void init();
    void setIMsgDispatcher(ImMsgDispathcer msgDispathcer);
    void setOldMsgDispatcher(IOldMsgDispather oldMsgDispatcher);
}
