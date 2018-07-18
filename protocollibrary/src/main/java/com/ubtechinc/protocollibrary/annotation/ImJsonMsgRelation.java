package com.ubtechinc.protocollibrary.annotation;



import com.ubtechinc.protocollibrary.communite.IMJsonMsgHandler;
import com.ubtechinc.protocollibrary.communite.NullJsonHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Administrator on 2017/5/25.
 */

@Target({ElementType.TYPE,ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ImJsonMsgRelation {
    short requestCmdId() default 0;
    short responseCmdId() default 0;
    Class<? extends IMJsonMsgHandler> msgHandleClass() default NullJsonHandler.class;
}
