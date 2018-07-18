# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Volumes/Logic/android/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keepattributes EnclosingMethod

-dontskipnonpubliclibraryclasses # 不忽略非公共的库类
-optimizationpasses 5            # 指定代码的压缩级别
-dontusemixedcaseclassnames      # 是否使用大小写混合
-dontpreverify                   # 混淆时是否做预校验
-verbose                         # 混淆时是否记录日志
-keepattributes *Annotation*     # 保持注解
-ignorewarning                   # 忽略警告
-dontoptimize                    # 优化不优化输入的类文件

-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*  # 混淆时所采用的算法

#保持哪些类不被混淆
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

#生成日志数据，gradle build时在本项目根目录输出
-dump class_files.txt            #apk包内所有class的内部结构
-printseeds seeds.txt            #未混淆的类和成员
-printusage unused.txt           #打印未被使用的代码
-printmapping mapping.txt        #混淆前后的映射

-keep public class * extends android.support.** #如果有引用v4或者v7包，需添加
#-libraryjars libs/xxx.jar        #混淆第三方jar包，其中xxx为jar包名
#-keep class com.xxx.**{*;}       #不混淆某个包内的所有文件
#-dontwarn com.xxx**              #忽略某个包的警告
-keepattributes Signature        #不混淆泛型
-keepnames class * implements java.io.Serializable #不混淆Serializable

-keepclasseswithmembers class * {      # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {      # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.app.Activity { # 保持自定义控件类不被混淆
    public void *(android.view.View);
}
-keepclassmembers enum * {             # 保持枚举 enum 类不被混淆
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {         # 保持 Parcelable 不被混淆
    public static final android.os.Parcelable$Creator *;
}

#不混淆AIDL
-keep class * implements android.os.IInterface {*;}

# IM 相关----start
-keep class com.tencent.**{*;}
-dontwarn com.tencent.**

-keep class tencent.**{*;}
-dontwarn tencent.**

-keep class qalsdk.**{*;}
-dontwarn qalsdk.**
# IM 相关 ---end

# bugly相关--start
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}
#bugly相关---end

# jni 相关 --start
-keepclasseswithmembernames class * {
    native <methods>;
}
# jni 相关 --end

# R文件中所有记录资源的静态字段 --start
-keepclasseswithmembers class **.R$* {
    public static <fields>;
}
# R文件中所有记录资源的静态字段 --end

# okhttp相关 --start
-dontwarn okhttp3.internal.**
-keep class okhttp3.internal.** { *; }
# okhttp相关 --start

#Retrofit相关 --start
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
#Retrofit相关 --end

#iflytek
-dontwarn com.iflytek.**
-keep class com.iflytek.**{ *; }

#utilcode
-keep class com.ubtech.utilcode.utils.** { *; }
-keep class com.ubtechinc.alpha.utils.** { *; }

#headkey
-keep class com.ubtechinc.alpha.jni.headkey.** {*;}

#ubt framework
-keep class com.ubtrobot.framework.db.**{*;}

#保留对象数据库的注解类和字段
-keep @com.ubtrobot.framework.db.annotation.Table class * {*;}

#android keep 注解
-keep @android.support.annotation.Keep class * {*;}

#rxjava
-keep class rx.internal.** {*;}

#netutils
-keep class com.ubtechinc.nets.**{*;}

#保留网络请求中json转化的类
-keep @android.support.annotation.Keep class * {*;}

#保留IM中的注解和类
-keep @com.ubtechinc.nets.im.annotation.ImMsgRelation class *{ *;}
-keep @com.ubtechinc.nets.im.annotation.IMMsgRelationVector class * {*;}
-keep class * implements com.ubtechinc.alpha.im.msghandler.IMsgHandler {*;}

#总线不混淆
-keep class com.ubtrobot.concurrent.** {*;}
-keep class com.ubtrobot.master.** {*;}
-keep class com.ubtrobot.transport.** {*;}

#总线注解修饰的类不混淆
-keep @com.ubtrobot.master.annotation.Call class * {*;}
-keep @com.ubtrobot.master.annotation.Subscirbe class * {*;}

-keep class * extends com.ubtrobot.master.service.MasterService {*;}
-keep class * extends com.ubtrobot.master.service.MasterSystemService {*;}
-keep class * extends com.ubtrobot.master.skill.MasterSkill {*;}

#保留protobuf
-keep class com.google.protobuf.** { *; }
-keep class * extends com.google.protobuf.** { *; }


#与tvs后台通信的request类
-keepattributes InnerClasses
-keep class com.willblaschko.android.alexa.TokenManager{*;}
-keep class com.willblaschko.android.alexa.TokenManager$GetAccessTokenRequest {*;}


#保留EventBus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

#xStream
-keep class com.thoughtworks.xstream.** {*;}

#OTA upgrade
-keep class com.ubtrobot.upgrade.** {*;}


-keep class org.greenrobot.greendao.**{*;}
-keep public interface org.greenrobot.greendao.**
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
public static java.lang.String TABLENAME;
}
-keep class **$Properties
-keep class net.sqlcipher.database.**{*;}
-keep public interface net.sqlcipher.database.**
-dontwarn net.sqlcipher.database.**
-dontwarn org.greenrobot.greendao.**