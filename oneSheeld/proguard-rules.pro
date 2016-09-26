# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /usr/local/opt/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:
-dontobfuscate
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature
-keep class com.integreight.** { *; }


# Required for Parse
-dontwarn com.squareup.**
-dontwarn okio.**
-dontwarn android.net.SSLCertificateSocketFactory
-dontwarn android.app.Notification

# Required for Twitter4j
-dontwarn twitter4j.**
-keep class twitter4j.** { *; }

# Required for Kryo and SnappyDB
-dontwarn sun.reflect.**
-dontwarn java.beans.**
-keep,allowshrinking class com.esotericsoftware.** {
   <fields>;
   <methods>;
}
-keep,allowshrinking class java.beans.** { *; }
-keep,allowshrinking class sun.reflect.** { *; }
-keep class com.esotericsoftware.kryo.** { *; }
-keep,allowshrinking class com.esotericsoftware.kryo.io.** { *; }
-keep,allowshrinking class sun.nio.ch.** { *; }
-dontwarn sun.nio.ch.**
-dontwarn sun.misc.**

-keep,allowshrinking class com.snappydb.** { *; }
-dontwarn com.snappydb.**

# Required for Crashlytics
-keep class com.crashlytics.** { *; }
-keep class com.crashlytics.android.**
-keep public class * extends java.lang.Exception

# Required for GmailSender
-dontwarn java.awt.**
-dontwarn java.beans.Beans
-dontwarn javax.security.**
-keep class javamail.** {*;}
-keep class javax.mail.** {*;}
-keep class javax.activation.** {*;}
-keep class com.sun.mail.dsn.** {*;}
-keep class com.sun.mail.handlers.** {*;}
-keep class com.sun.mail.smtp.** {*;}
-keep class com.sun.mail.util.** {*;}
-keep class mailcap.** {*;}
-keep class mimetypes.** {*;}
-keep class myjava.awt.datatransfer.** {*;}
-keep class org.apache.harmony.awt.** {*;}
-keep class org.apache.harmony.misc.** {*;}

# Required for Android Async Http
-keep class cz.msebera.android.httpclient.** { *; }
-keep class com.loopj.android.http.** { *; }

# Required for Google Apis
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-dontwarn com.google.api.client.googleapis.extensions.android.**
-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}
-dontnote java.nio.file.Files, java.nio.file.Path
-dontnote sun.misc.Unsafe
-dontwarn sun.misc.Unsafe