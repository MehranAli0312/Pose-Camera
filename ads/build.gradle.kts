plugins {
    id("com.android.library")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.example.ads"
    compileSdk = 37

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    testOptions {
        targetSdk = 37
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "admob_app_id", "ca-app-pub-3940256099942544~3347511713")
            buildConfigField(
                "String",
                "AD_UNIT_BANNER",
                "\"ca-app-pub-3940256099942544/9214589741\""
            )
            buildConfigField(
                "String",
                "AD_UNIT_NATIVE",
                "\"ca-app-pub-3940256099942544/2247696110\""
            )
            buildConfigField(
                "String",
                "AD_UNIT_INTERSTITIAL",
                "\"ca-app-pub-3940256099942544/1033173712\""
            )
            buildConfigField(
                "String",
                "AD_UNIT_REWARDED",
                "\"ca-app-pub-3940256099942544/5224354917\""
            )
            buildConfigField(
                "String",
                "AD_UNIT_REWARDED_INTERSTITIAL",
                "\"ca-app-pub-3940256099942544/5354046379\""
            )
            buildConfigField(
                "String",
                "AD_UNIT_APP_OPEN",
                "\"ca-app-pub-3940256099942544/9257395921\""
            )
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "admob_app_id", "ca-app-pub-6854526007331629~8767978934")
            buildConfigField(
                "String",
                "AD_UNIT_BANNER",
                "\"\""
            )
            buildConfigField(
                "String",
                "AD_UNIT_NATIVE",
                "\"\""
            )
            buildConfigField(
                "String",
                "AD_UNIT_INTERSTITIAL",
                "\"\""
            )
            buildConfigField(
                "String",
                "AD_UNIT_REWARDED",
                "\"\""
            )
            buildConfigField(
                "String",
                "AD_UNIT_REWARDED_INTERSTITIAL",
                "\"\""
            )
            buildConfigField(
                "String",
                "AD_UNIT_APP_OPEN",
                "\"\""
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        resValues = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material3)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
    implementation(libs.koin.android)
    api(libs.koin.androidx.compose)

    implementation(libs.shimmer.compose)

    api(libs.admob.ads.next.gen)

    api(libs.user.messaging)

    testImplementation(libs.junit)
    testImplementation(libs.koin.test)

    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
