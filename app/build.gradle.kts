plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.secrets)

    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

// Single source of truth for the version — also drives the output file name below.
val appVersionCode = 7
val appVersionName = "0.0.7"

android {
    namespace = "com.aipose.camera.posematch"
    // Android 17. SDK platform android-37.0 is installed locally.
    compileSdk = 37

    defaultConfig {
        applicationId = "com.aipose.camera.posematch"
        minSdk = 26
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

/*    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
            storeFile = file(keystorePath)
            storePassword = System.getenv("STORE_PASSWORD")
            keyAlias = "upload"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
        create("debugConfig") {
            storeFile = file("${rootDir}/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }*/

    val adIdsDebug = mapOf(
        "admob_app_id" to "ca-app-pub-3940256099942544~3347511713", // Google sample app id
        "AppOpenResume" to "ca-app-pub-3940256099942544/9257395921", // App open
        "InterSplash" to "ca-app-pub-3940256099942544/1033173712",   // Interstitial (splash)
        "InterHome" to "ca-app-pub-3940256099942544/1033173712",     // Interstitial (all in-app)
        "NativeSplash" to "ca-app-pub-3940256099942544/2247696110",  // Native (splash bottom)
        "NativeAll" to "ca-app-pub-3940256099942544/2247696110",     // Native (every other screen)
        "BannerSplash" to "ca-app-pub-3940256099942544/6300978111",  // Banner (splash bottom)
        "Banner_Ad" to "ca-app-pub-3940256099942544/6300978111"      // Banner (every other screen)
    )

    val adIdsRelease = mapOf(
        "admob_app_id" to "ca-app-pub-6854526007331629~8767978934",
        "AppOpenResume" to "ca-app-pub-6854526007331629/5468388983",
        "InterSplash" to "ca-app-pub-6854526007331629/2409991126",
        "InterHome" to "ca-app-pub-6854526007331629/1755385005",
        "NativeSplash" to "ca-app-pub-6854526007331629/2531234451",
        "NativeAll" to "ca-app-pub-6854526007331629/6245294359",
        "BannerSplash" to "ca-app-pub-6854526007331629/9571285527",
        "Banner_Ad" to "ca-app-pub-6854526007331629/9675227305"
    )

    buildTypes {
        debug {
            adIdsDebug.forEach { (name, id) -> resValue("string", name, id) }
        }
        release {
            adIdsRelease.forEach { (name, id) -> resValue("string", name, id) }

            isCrunchPngs = true
            // R8 code shrinking/obfuscation + resource shrinking to cut release size.
            isMinifyEnabled = true
            isShrinkResources = true
            // Ship only real-phone ABIs (drop emulator-only x86/x86_64) — saves ~22 MB of
            // native libraries. Debug stays universal so x86 emulators still work.
            ndk {
                abiFilters.clear()
                abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
         //   signingConfig = signingConfigs.getByName("release")
        }
    /*    debug {
            signingConfig = signingConfigs.getByName("debugConfig")
        }*/
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
        resValues = true
    }
    testOptions { unitTests { isIncludeAndroidResources = true } }
}

base {
    archivesName = "AIPoseMatchPoseCamera-V$appVersionCode"
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
    propertiesFileName = ".env"
    defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // View-system single-Activity + Fragment Navigation (migrated from Compose to XML).
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    // Coil singleton + base (ImageView targets / Context.imageLoader) — no Compose integration.
    implementation("io.coil-kt:coil:2.7.0")
    implementation(libs.converter.moshi)
    implementation(libs.firebase.ai)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.logging.interceptor)
    implementation(libs.mlkit.pose)
    // On-device selfie/person segmentation (model bundled — no network) for overlay bg removal.
    implementation("com.google.mlkit:segmentation-selfie:16.0.0-beta6")
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)
    // implementation(libs.play.services.location)
    implementation(libs.retrofit)
    testImplementation(libs.androidx.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)
    "ksp"(libs.androidx.room.compiler)
    "ksp"(libs.moshi.kotlin.codegen)

    implementation(libs.sdp.android)
    implementation(libs.ssp.android)

//    for ads ...
    // The native-ad layouts use MaterialCardView; needed for Theme.PoseMatch.Ads to resolve.
    implementation("com.google.android.material:material:1.12.0")
    implementation(libs.shimmer) //Ads
    implementation(libs.ads.mobile.sdk)
    implementation (libs.androidx.lifecycle.process)
    // Google Play Billing REMOVED: the app has no BillingClient / launchBillingFlow anywhere, but the
    // library still merged its ProxyBillingActivity into the manifest. The system could recreate
    // that activity on task restore with no PendingIntent extra, crashing in the library own
    // onCreate (Crashlytics fatal, v5). Re-add this line if/when subscriptions are implemented.
    // implementation(libs.billing.ktx)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.config)
    implementation(libs.firebase.crashlytics)

}
