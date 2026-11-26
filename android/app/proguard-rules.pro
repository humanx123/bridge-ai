-keep class com.bridgeai.android.net.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-dontwarn org.slf4j.**
-dontwarn org.codehaus.mojo.animal_sniffer.*
