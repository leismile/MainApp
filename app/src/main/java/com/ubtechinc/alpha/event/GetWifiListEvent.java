package com.ubtechinc.alpha.event;

/**
 * @author：wululin
 * @date：2017/11/13 11:16
 * @modifier：ubt
 * @modify_date：2017/11/13 11:16
 * [A brief description]
 * version
 */

public class GetWifiListEvent extends BaseEvent {

    public long requestSerial;
    public String peer;

    public int responseCmdID;

    public int status;

}
