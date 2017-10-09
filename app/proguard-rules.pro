# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/fun/Android/Sdk/tools/proguard/proguard-android.txt
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

# Disable logs
-assumenosideeffects class android.util.Log{
    public static *** v(...);
    public static *** i(...);
    public static *** d(...);
    public static *** w(...);
    public static *** e(...);
}

# For greenDao
-keep class de.greenrobot.dao.** {*;}
-keep class com.neusoft.oddc.db.gen.** {*;}
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
public static java.lang.String TABLENAME;
}
-keep class **$Properties
# If you do not use SQLCipher:
-dontwarn org.greenrobot.greendao.database.**
# If you do not use Rx:
-dontwarn rx.**


# For jackson
-keep class org.apache.commons.** {*;}
-dontwarn org.apache.commons.httpclient.**
-keep class org.codehaus.jackson.** {*;}
-dontwarn org.codehaus.jackson.**
-keep class com.fasterxml.jackson.** {*;}
-dontwarn com.fasterxml.jackson.**
# For springframework
-keep class org.simpleframework.** {*;}
-dontwarn org.simpleframework.**
-keep class org.springframework.android.** {*;}
-keep class org.w3c.dom.** {*;}
-keep class com.google.code.** {*;}
-dontwarn com.google.code.**


##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }
-keep class com.google.gson.** { *;}
-dontwarn com.google.gson.**

# Application classes that will be serialized/deserialized over Gson
-keep class com.antew.redditinpictures.library.imgur.** { *; }
-keep class com.antew.redditinpictures.library.reddit.** { *; }

##---------------End: proguard configuration for Gson  ----------


# For glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# For multi-dex
-keep class android.support.multidex.** {*; }
-keepattributes EnclosingMethod

# For apache common io
-dontwarn org.apache.commons.**

# For ADAS
-keep class com.neusoft.adas.** {*; }
# -keep class com.neusoft.oddc.adas.** {*; }

# For EventBus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
