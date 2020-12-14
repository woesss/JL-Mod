# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\tools\adt-bundle-windows-x86_64-20131030\sdk/tools/proguard/proguard-android.txt
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
-keep public class javax.** { public *; }
-keep public class com.siemens.mp.** { public *; }
-keep public class com.samsung.util.** { public *; }
-keep public class com.sonyericsson.accelerometer.** { public *; }
-keep public class com.sprintpcs.media.** { public *; }
-keep public class com.mascotcapsule.micro3d.v3.* { public *; }
-keep public class com.motorola.** { public *; }
-keep public class com.nokia.mid.** { public *; }
-keep public class com.sun.midp.midlet.** { public *; }
-keep public class com.vodafone.** { public *; }
-keep public class mmpp.media.** { public *; }
-keep public class org.microemu.** { public *; }
# Keep the BuildConfig
-keep class ru.playsoftware.j2meloader.BuildConfig { *; }

-keep class com.arthenica.mobileffmpeg.** { *; }
-keep class ru.playsoftware.j2meloader.crashes.AppCenterAPI** { *; }
