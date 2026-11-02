# Proguard rules for AcademicFlow
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* *;
}
-keep class * implements kotlinx.serialization.KSerializer { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
