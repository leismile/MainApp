package com.ubtechinc.protocollibrary.communite.old;

/**
 * @desc : 蓝牙命令的抽象工程
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/4/3
 */

public interface IAbstractBleCommandFactory {
    ICommandEncode getCommandEncode();
    ICommandProduce getCommandProduce();
}
