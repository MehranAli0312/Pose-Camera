-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes Signature,InnerClasses,EnclosingMethod,Exceptions
-keepattributes *Annotation*,RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault

-keep class kotlin.Metadata { *; }
-keep class kotlin.jvm.internal.** { *; }
-dontwarn kotlin.**

-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers enum com.aipose.camera.posematch.** { *; }

-keep class com.aipose.camera.posematch.domain.models.** { *; }
-keep class com.aipose.camera.posematch.data.local.entity.** { *; }
-keepclassmembers class com.aipose.camera.posematch.data.local.entity.** { *; }

-dontnote kotlinx.serialization.**
-dontwarn kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.aipose.camera.posematch.**$$serializer { *; }
-keepclassmembers class com.aipose.camera.posematch.** {
    *** Companion;
}
-keepclasseswithmembers class com.aipose.camera.posematch.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**
-dontwarn com.google.mediapipe.**

-dontwarn coil.**
-dontwarn androidx.compose.**
