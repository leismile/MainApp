package com.ubtechinc.bluetoothrobot.old

import com.ubtechinc.bluetoothrobot.UbtBleNetworkManager
import com.ubtechinc.protocollibrary.communite.old.IOldMsgDispather

/**
 * @Deseription
 * @Author tanghongyu
 * @Time 2018/7/12 15:50
 */
class OldMsgDispather : IOldMsgDispather {

    override fun handleMsg(data: String, from: Any) {
        OldUbtBleNetworkManager.getInstance().parseResult(data, from)
    }

}