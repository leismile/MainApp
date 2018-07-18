package com.ubtechinc.bluetoothrobot.old;

import com.ubtechinc.protocollibrary.communite.old.IAbstractBleCommandFactory;
import com.ubtechinc.protocollibrary.communite.old.ICommandEncode;
import com.ubtechinc.protocollibrary.communite.old.ICommandProduce;
import com.ubtechinc.protocollibrary.communite.old.JsonCommandEncode;

/**
 * @desc : JSON类型数据工厂类
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/4/3
 */

public class JsonAbstractBleCommandFactory implements IAbstractBleCommandFactory {

    private String mSerialNumber;

    public JsonAbstractBleCommandFactory(String mSerialNumber) {
        this.mSerialNumber = mSerialNumber;
    }

    @Override
    public ICommandProduce getCommandProduce() {
        return new JsonCommandProduce();
    }

    @Override
    public ICommandEncode getCommandEncode() {
        return  JsonCommandEncode.get().init(mSerialNumber);
    }
}
