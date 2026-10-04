package com.aipose.camera.posematch.admob_ads

import android.app.Activity

/**
 * Placeholder splash Activity type.
 *
 * The app-open ad manager ([OpenApp]) uses `activity is SplashScreen` to avoid showing an app-open
 * ad while the splash is on screen. This app's splash is actually a Fragment inside MainActivity,
 * so there is no dedicated splash Activity — this empty type only exists to satisfy that type check.
 * It is never launched and is intentionally not declared in the manifest. If a real splash Activity
 * is introduced later, make it extend this (or replace the check).
 */
open class SplashScreen : Activity()
