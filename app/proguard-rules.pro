# ============================================================================
# Pose Match Camera — R8 / ProGuard rules
# Goal: shrink + obfuscate the release build while keeping everything that is
# accessed reflectively (Moshi model parsing, Room, enums) working.
# ============================================================================

# ---- Attributes needed for reflection, generics and annotations ----
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepattributes *Annotation*
# Keep readable crash stack traces (line numbers) — remove if you want max shrink.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---- Kotlin ----
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }
-keep class kotlin.jvm.internal.** { *; }
-dontwarn kotlin.**

# ---- App data models parsed by Moshi via reflection (KotlinJsonAdapterFactory) ----
# Field names must survive, or default_poses.json parsing (PoseItem) breaks at runtime.
-keep class com.aipose.camera.posematch.data.** { *; }
-keepclassmembers class com.aipose.camera.posematch.data.** { *; }

# ---- Moshi ----
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepnames @com.squareup.moshi.JsonClass class *
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}
-keepclassmembers @com.squareup.moshi.JsonClass class * {
    <init>(...);
    <fields>;
}
-dontwarn com.squareup.moshi.**

# ---- Room (entities/DAOs referenced by generated code) ----
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# ---- Retrofit / OkHttp / Okio ----
-keepattributes Exceptions
-keepclasseswithmembers class * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface retrofit2.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ---- Enums accessed via valueOf()/values() ----
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ---- Parcelable ----
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ---- ML Kit / MediaPipe / Firebase / Google Play services ----
# These libraries ship their own consumer R8 rules (applied automatically);
# we only silence warnings so the build does not fail on optional references.
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**
-dontwarn com.google.firebase.**
-dontwarn com.google.mediapipe.**

# ---- Coil / Compose (R8-friendly; suppress optional-reference warnings) ----
-dontwarn coil.**
-dontwarn androidx.compose.**
