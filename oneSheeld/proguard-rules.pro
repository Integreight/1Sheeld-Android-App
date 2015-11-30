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