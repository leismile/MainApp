package com.ubtechinc.bluetoothrobot.handler


import com.ubtech.utilcode.utils.LogUtils
import com.ubtechinc.alpha.BleBindOrSwitchWifi
import com.ubtechinc.bluetoothrobot.UbtBleNetworkManager
import com.ubtechinc.protocollibrary.communite.IMsgHandler
import com.ubtechinc.protocollibrary.communite.ProtoBufferDispose
import com.ubtechinc.protocollibrary.protocol.MiniMessage

/**
 * @Deseription 绑定或配网
 * @Author tanghongyu
 * @Time 2018/3/14 20:43
 */

class BindOrSwitchWifiHandler : IMsgHandler {
    override fun handleMsg(requestCmdId: Short, responseCmdId: Short, request: MiniMessage, peer: Object) {

        val requestSerial = request.sendSerial
        val bleBindOrSwitchWifiRequest = ProtoBufferDispose.unPackData(
                BleBindOrSwitchWifi.BindOrSwitchWifiRequest::class.java, request.dataContent) as BleBindOrSwitchWifi.BindOrSwitchWifiRequest
        LogUtils.d("receive====== " + bleBindOrSwitchWifiRequest.data);
        UbtBleNetworkManager.getInstance().parseResult(bleBindOrSwitchWifiRequest.data, peer)


    }
}
