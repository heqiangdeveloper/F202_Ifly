# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

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

# 代码混淆压缩比，在0~7之间，默认为5，一般不做修改
-optimizationpasses 5

# 混合时不使用大小写混合，混合后的类名为小写
-dontusemixedcaseclassnames

# 指定不去忽略非公共库的类
-dontskipnonpubliclibraryclasses

# 这句话能够使我们的项目混淆后产生映射文件
# 包含有类名->混淆后类名的映射关系
-verbose

# 指定不去忽略非公共库的类成员
-dontskipnonpubliclibraryclassmembers

# 不做预校验，preverify是proguard的四个步骤之一，Android不需要preverify，去掉这一步能够加快混淆速度。
-dontpreverify

# 保留Annotation不混淆
-keepattributes *Annotation*,InnerClasses

# 避免混淆泛型
-keepattributes Signature

# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable

# 指定混淆是采用的算法，后面的参数是一个过滤器
# 这个过滤器是谷歌推荐的算法，一般不做更改
-optimizations !code/simplification/cast,!field/*,!class/merging/*


# 保留本地native方法不被混淆
-keepclasseswithmembernames class * {
     native <methods>;
}

# 保留Parcelable序列化类不被混淆
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# 保持异常不被混淆
-keepattributes Exceptions

# 保留Serializable序列化的类不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keep interface xx.xx.xx..**{*;}



-keep class com.iflytek.seopt.SeoptConstant {  public <methods>;public static <fields>; public  <fields>;}
-keep class com.iflytek.seopt.SeoptManager {  public <methods>;public static <fields>;  public  <fields>;}

-keep class  com.iflytek.speech.* {*;}

-keep class  com.iflytek.speech.mvw.IMVWListener{*;}

-keep class  com.iflytek.speech.sr.ISRListener{*;}

-keep class com.iflytek.tts.ESpeaker{*;}
-keep class com.iflytek.tts.IPlayerListener{*;}
-keep class com.iflytek.tts.ITtsInitListener{*;}
-keep class com.iflytek.tts.ITtsListener{*;}

-keep class com.iflytek.adapter.** {public <methods>;public static <fields>; public  <fields>;}
#-keep class com.iflytek.adapter.* {public <methods>;public static <fields>; public  <fields>;}
#-keep class com.iflytek.adapter.common.** {public <methods>;public static <fields>; public  <fields>;}
#-keep class com.iflytek.adapter.controllerInterface.** {public <methods>;public static <fields>; public  <fields>;}
#-keep class com.iflytek.adapter.mvw.** {public <methods>;public static <fields>; public  <fields>;}
#-keep class com.iflytek.adapter.sr.** {public <methods>;public static <fields>; public  <fields>;}
#-keep class com.iflytek.adapter.oneshot.OneShotConstant {*;}
#-keep class com.iflytek.adapter.oneshot.OneShotManager {public <methods>;public static <fields>; public  <fields>;}



-keep class  com.iflytek.speech.util.*{public static boolean isNetworkAvailable(android.content.Context);}


 -keepclassmembers class com.android.proguard.example.Test {
        public <init>();
    }

-keep class com.iflytek.adapter.ttsservice.aidl.** { *; }


