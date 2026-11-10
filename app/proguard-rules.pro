# NewPipe Extractor / Rhino / Jsoup
-keep class org.schabi.newpipe.extractor.** { *; }
-keep class org.mozilla.** { *; }
-keep class org.jsoup.** { *; }
-keep class com.google.protobuf.** { *; }
-dontwarn org.mozilla.**
-dontwarn org.jsoup.**
-dontwarn com.google.protobuf.**
-dontwarn javax.annotation.**

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keep,includedescriptorclasses class com.cleve.music.**$$serializer { *; }
-keepclassmembers class com.cleve.music.** {
    *** Companion;
}
-keepclasseswithmembers class com.cleve.music.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
