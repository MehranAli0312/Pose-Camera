# Pose Camera — Clean Architecture Migration (Option B)

Base: `D:\WithSadam\PoseCompose` infra (4 modules, Koin, ads, billing, remote config, theme tokens, RTL helpers).
Branch: `restructure/clean-architecture`. No visual redesign — the shipped look is preserved and expressed through theme tokens.

## Status

| Phase | State |
|---|---|
| 0 — Base setup (modules, catalog, manifest, resources) | done, builds |
| 1 — Data layer (Room, DataStore, data sources, repos) | done |
| 2 — Domain (models, pose matcher, use cases) | done |
| 3 — ViewModels (god object split into 7) | done |
| 4 — UI port (home, collections, camera, photo edit, settings, onboarding, splash) | done |
| 5 — Navigation (routes, bottom bar Home/Collections/Settings) | done |
| 6 — Ads + Pro wiring | splash interstitial only (by request); in-app update kept |
| 7 — Comment purge, builds, RTL check, string cleanup | done |

## Verification done

- `:app:assembleDebug` — passes.
- `:app:assembleRelease` — passes with R8 minify + resource shrink (57 MB unsigned APK), so the proguard rules hold.
- Room schema hash compared against the shipped build's generated `AppDatabase_Impl` — identical.
- All 139 pose asset paths in `default_poses.json` resolve to real files.
- Koin graph: every constructor dependency of the 29 repositories / use cases / ViewModels is registered.
- RTL audit: no `left`/`right` modifiers anywhere, every directional icon is `AutoMirrored` or `autoMirrored="true"`, grid canvas has no labels to mirror.
- Not yet done: running the app. The local emulator would not boot in this environment, so launch it from Android Studio once to smoke-test the flow.

## Non-negotiable preservation (app is live on Play)

| Thing | Value | Verified |
|---|---|---|
| `applicationId` / `namespace` | `com.aipose.camera.posematch` | yes |
| `versionCode` | 4 → 5 | yes |
| Room DB name | `pose_match_camera_db` | yes |
| Room schema identity hash | `61c99a7bd017b4ddd2102699a556d1bb` — identical to the shipped build, so existing history survives and no destructive fallback is needed | yes, compared against the old generated `AppDatabase_Impl` |
| DataStore file | `pose_match_prefs`, with a one-time `DataMigration` importing the legacy keys | yes |
| Theme preference | still stored under the legacy `app_theme` key, so all four themes survive | yes |
| FileProvider authority | `${applicationId}.fileprovider` | yes |
| Assets | `default_poses.json` + 139 pose images, all paths verified to resolve | yes |

Legacy pref mapping (one-time, on first launch of the new build):
- `onboarding_completed` (bool) → `IS_ON_SPLASH_FIRST_RUN = !value`
- `selected_language` ("English") → `Language` ("en") via a name→code table
- `retain_skeleton` → `retain_skeleton_overlay`
- `app_theme` — reused as-is, no mapping needed

## New structure

```
app/src/main/java/com/aipose/camera/posematch/
├── ads/                     monetization wiring (placements, pro status, interstitials)
├── data/
│   ├── local/               Room (entity/dao/mapper/dto), DataStore, asset + file + location sources
│   ├── pose/                ML Kit detector, frame analyzer, subject cutout, grading engine, capture processor
│   └── repoImpl/            9 repository implementations
├── di/                      localModule, repositoryModule, useCaseModule, viewModelModule
├── domain/
│   ├── models/              Android-free models (Pose, Capture, PoseMatch, ColorGrade, …)
│   ├── pose/                PoseMatcher (pure bone-angle math)
│   ├── repo/                9 repository interfaces
│   └── usecase/             10 use cases
├── topLevel/MyApp.kt        Koin + ads + pro bootstrap
├── ui/
│   ├── activity/            MainActivity
│   ├── common/              30+ shared composables (incl. PoseImage, StudioSearchField, StudioTopBar)
│   ├── firebaseRemote/      Remote Config pipeline
│   ├── graph/               NavRoute, AppNavGraph, DashboardNavGraph
│   ├── screens/<feature>/   Screen.kt + components/ + models/ + data/
│   ├── theme/               palette tokens, 4 app themes, typography, shapes
│   └── vm/                  11 ViewModels
└── util/                    extensions, BidiText
```

Legacy sources are parked in `app/legacy/` (outside the source set) until the port is signed off, then deleted.

## Ads scope in the app module

Only one placement is live: the **splash interstitial** (`AdPlacement.SplashFullscreen`), kept warm at startup and shown at the end of the splash before routing to language / premium / home. The Play **in-app update** check also stays (`InAppUpdateManager` driven by `appUpdateConfig` from Remote Config).

Everything else was removed from the app module on request: the dashboard bottom banner/native slot, the app-open-on-resume ad (with its `AdsAppLifecycleObserver` registration, `ExternalScreenLaunch` markers and `PaywallVisibility` gate), the inner interstitial (including the one on leaving the save-success screen), and the onboarding ad page.

Nothing was stripped from Remote Config: every key, default and ad unit stays in `AppFirebaseRemote`, `AdsRemoteConfig`, `AdsRemoteConfigStore`, `AdUnitIds` and `remote_config_defaults.xml`, and `AppAdsBindings` still maps `AppOpenResume` / `InnerInterstitial` / `HomeScreenBottom` styles. So turning any of those placements back on later is a UI-side change only — the config plumbing is already there.

## Needed from you

1. `google-services.json` for `com.aipose.camera.posematch` (Firebase console → add Android app). The gms + Crashlytics plugins apply only when that file exists, so the build works without it and Firebase/Remote Config switch on the moment you drop it in.
2. AdMob app ID + ad unit IDs for this app. `ads/build.gradle.kts` currently carries Google's **test** unit IDs and the other app's AdMob app ID in `resValue("string", "admob_app_id", …)` — that must be replaced before a release build.
3. Play Console product IDs for Pro (`ProPlan.subscriptionProductIds`).
4. Which features (if any) should be Pro-only. Nothing is gated today: every feature that was free stays free, since gating an existing free feature is a product decision.
5. Release keystore if you want a signed build from here.

## Known follow-ups

- The 11 translated `values-*` files from the shipped app were staged as deleted before this work started; 40 of their 66 keys still match current keys. Say the word and I will restore those 40 and leave the rest.
- Pose feature strings are English-only right now; the 21 locales inherited from the reference cover the infra strings (settings, pro, rate-us, onboarding chrome).
- Urdu / Bangla / Filipino were in the shipped language list but are not in the reference language picker (no flag drawables). Needs 3 flags + translations to restore.
- Vendored modules (`ads/`, `billing/`, `common/`) keep their original packages and comments — they are reused dependencies, not app code.
- Legacy buzzed on every score change at or above 80%; that behaviour was kept as-is. It reads like a bug and is a one-line change if you want it fixed.
