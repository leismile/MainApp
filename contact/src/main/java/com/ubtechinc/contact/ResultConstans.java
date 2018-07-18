package com.ubtechinc.contact;

/**
 * @desc : 返回码定义
 * @author: zach.zhang
 * @email : Zach.zhang@ubtrobot.com
 * @time : 2018/2/7
 */

public class ResultConstans
{
    /**
     *  成功
     */
    public static final int RESULT_SUCCESS = 0;

    /**
     * 参数无效，如ContactId不存在
     */
    public static final int RESULT_INVALID_PARAMS = -1;

    /**
     *  操作冲突，当前正在修改中，不允许修改
     */
    public static final int RESULT_OPTION_CONFILCT = -2;

    /**
     * 其他错误
     */
    public static final int RESULT_UNKOWN_ERROR = -3;
}
