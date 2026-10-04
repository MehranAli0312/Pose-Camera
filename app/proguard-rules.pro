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

# The accelerated pose detector spins up a MediaPipe graph in a separate
# "mlkit_acceleration_mini_benchmark" process. Native code reads protobuf-lite
# message fields (e.g. the byte[] "value" of ...mlkit_vision_mediapipe.zzib) by
# reflection through JNI (PacketCreator.nativeCreateProto). R8 in the release
# build strips/renames those fields, which aborts that process with
# java.lang.NoSuchFieldError. Keep MediaPipe/MLKit internals and all
# protobuf-lite message fields so the reflective JNI lookups still resolve.
-keep class com.google.mediapipe.** { *; }
-keepclassmembers class com.google.mediapipe.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_mediapipe.** { *; }
-keepclassmembers class com.google.android.gms.internal.mlkit_vision_mediapipe.** { *; }
-keep class com.google.mlkit.** { *; }
-keepclassmembers class com.google.mlkit.** { *; }

# protobuf-lite: generated message classes expose their fields reflectively.
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite { <fields>; }
-keep class com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**

# ---- Coil / Compose (R8-friendly; suppress optional-reference warnings) ----
-dontwarn coil.**
-dontwarn androidx.compose.**
