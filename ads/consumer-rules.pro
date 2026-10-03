# The GMA Next-Gen SDK ships its own consumer rules inside its AAR, so nothing is needed for the
# SDK itself. These rules cover this module's own surface.

# Koin resolves these by type at runtime.
-keep class com.example.ads.AdsManager { *; }
-keep class com.example.ads.di.AdsModuleKt { *; }

# Ad event callbacks are implemented as anonymous objects and invoked reflectively by the SDK.
-keepclassmembers class com.example.ads.internal.** {
    public void onAd*(...);
}
