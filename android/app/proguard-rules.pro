-keep class com.bridgeai.android.net.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
