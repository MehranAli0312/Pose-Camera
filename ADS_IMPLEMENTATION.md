# Android Ads Implementation Standard

This document defines a reusable ads architecture for Android applications built with Kotlin and Jetpack Compose. It is project-independent: the host app supplies placement names, navigation, Remote Config and business rules, while the ads module owns loading, reuse, lifecycle safety and presentation guards.

The primary rule is:

> Request an ad only when there is a realistic opportunity to show it, and never discard a loaded ad that has not produced an impression when it can be safely reused.

## 1. Scope

This standard covers:

- interstitial, app-open and rewarded full-screen ads;
- banner and native ads;
- Compose lifecycle and configuration changes;
- splash and post-splash navigation;
- Remote Config, analytics and placement identity;
- request deduplication, caching, pooling and expiry;
- rewarded fallback behavior;
- automated and manual verification.

This standard does not choose product placements. Each app decides where ads belong and which user actions are valid triggers. An architecture migration must not silently add, remove or relocate placements.

## 2. Required architecture

```text
ads module                                 app module
├── AdsManager                            ├── placement registry
├── FullScreenAdController                ├── style and ad-unit providers
├── AdPool<T>                             ├── Remote Config model and store
├── BannerAdController                    ├── launch-flow integration
├── NativeAdController                    ├── feature trigger policies
├── PreparedSlotController                └── analytics listener
├── AdsRuntimeState
├── AdsAppLifecycleObserver
└── Compose renderers
```

- The ads module owns eligibility, request guards, loading, caching, pooling, expiry, showing and SDK callbacks.
- The app module owns placement ids, ad-unit mapping, feature policy, navigation timing and Remote Config values.
- Screens render slots or call a trigger. They do not access ad SDK objects or Remote Config directly.
- SDK loads run in a module-level `CoroutineScope(SupervisorJob() + dispatcher)`, not a screen scope.
- Controllers, runtime state, pools and manager are application-scoped Koin singletons.

## 3. Non-negotiable invariants

1. A pro or otherwise ad-free user generates no ad requests.
2. A new request requires consent, SDK readiness, network availability and a configured ad unit.
3. A new full-screen request also requires a valid show opportunity.
4. A cached ad is checked before a new request gate because reuse creates no network request.
5. An in-flight load is joined before starting another request with the same cache key.
6. Full-screen cache and in-flight keys are based on format only.
7. Banner and native ads are pooled by compatibility key.
8. An ad object that produced an impression is never returned to the pool.
9. An unseen, fresh banner or native ad is recycled instead of destroyed.
10. Loads continue in module scope if a screen leaves composition.
11. A configuration change does not release a prepared slot.
12. Every navigation continuation runs exactly once, including unavailable, failed and busy cases.
13. App-open never requests while another full-screen ad is active or immediately after returning from an ad click.
14. Switches, limits and waits use Remote Config with safe local defaults and bounded values.
15. Analytics callbacks are attached to the placement where the ad is currently used.

## 4. Gates

Keep request eligibility and presentation eligibility separate.

### 4.1 Request gate

A new SDK request is allowed only when:

```text
eligible user
AND consent permits ads
AND SDK initialized
AND network available
AND ad unit configured
```

The request gate must not prevent reuse of an already loaded ad merely because the device is currently offline.

### 4.2 Full-screen show gate

A full-screen opportunity is valid only when:

```text
eligible user
AND app in foreground
AND no full-screen operation owns the lock
AND minimum interval satisfied
AND placement policy is due
```

Check this gate before starting a new full-screen request and again immediately before showing.

### 4.3 Full-screen lock

Use one atomic process-wide lock for interstitial, rewarded and app-open presentation. Acquire it immediately before consuming the cached ad and release it in `finally` after dismissal or show failure.

The lock and visible state are different:

- the lock protects loading-to-show races;
- visible state begins in the SDK `onAdShowed` callback;
- both are cleared after presentation finishes.

## 5. Full-screen controller standard

Full-screen formats are `INTERSTITIAL`, `REWARDED` and `APP_OPEN`.

The cache key is the format only:

```kotlin
private fun key(format: AdFormat) = format.name
```

Do not include placement id or ad-unit id. All placements requesting the same format share one cached object and one in-flight load.

Required lookup order:

```text
fresh cached ad exists       -> return it
same-format load is running  -> join it
show gate fails              -> do not request
request gate fails           -> do not request
otherwise                    -> start one module-scope load
```

Required behavior:

- Cache expiry is 55 minutes unless the SDK requires less.
- Loading callers join one shared `Deferred`.
- Caller timeout does not cancel the module load.
- A late result remains cached for the next valid trigger.
- The requesting placement determines the ad unit used for the SDK request.
- The showing placement determines analytics `ad_placement` for show and click events.
- The ad network still attributes the impression to the ad unit that loaded the object.
- Failed shows always release the full-screen lock.

## 6. Banner and native pool standard

Use one generic `AdPool<T>` implementation.

| Operation | Required behavior |
|---|---|
| `acquire(key, load)` | Take a fresh unseen object, otherwise join the key's load, otherwise start one load |
| `prefetch(key, load)` | Start only when no fresh object and no active load exist |
| `markImpressed(ad)` | Mark the exact object from the SDK impression callback |
| `recycle(ad)` | Destroy impressed or expired objects; enqueue fresh unseen objects |
| `loadedAt(ad)` | Preserve original load time across screens and prepared slots |
| `clear()` | Cancel tracked loads and destroy all tracked objects |

Pool requirements:

- Track objects by identity, not equality.
- Keep at most two pooled objects per key.
- Remove expired objects before deciding whether prefetch is necessary.
- Use a 55-minute maximum lifetime unless the SDK requires less.
- If a caller is cancelled, the module load continues and a successful result enters the pool.
- Multiple waiters cannot acquire the same single object.
- Destroy a result that cannot be accepted into the pool.

Compatibility keys:

```text
native key = ad unit id
banner key = ad unit id:STYLE:WIDTHxHEIGHT
```

A banner with different dimensions or collapsible behavior is not interchangeable.

## 7. Banner and native controllers

Controllers must:

- resolve the compatibility key before applying the network request gate;
- let the pool return a cached object when a new request is impossible;
- run actual SDK loading through module scope;
- report load result once for the placement that initiated the load;
- reattach event callbacks after every acquisition;
- mark the exact object impressed before reporting the impression;
- expose `preload`, `load`, `recycle`, `loadedAt` and `destroy` as needed.

Use the loading and rendering API recommended by the installed ads SDK version. The architectural requirement is that the banner object can outlive a screen and that its renderer detaches a view without destroying the reusable ad object.

## 8. Compose rendering standard

### 8.1 Normal slots

`AdsBanner` and `AdsNative` must:

- wait for ads readiness;
- acquire through their controller;
- render placeholders only while loading;
- retry through the shared retry policy;
- call `recycle(ad)` from `DisposableEffect`;
- never call `destroy()` directly.

### 8.2 Shared banner renderer

Use one `BannerAdView` for normal and prepared slots. It must find the current activity, detach an existing parent before attaching, detach on release and never destroy the banner object.

### 8.3 Prepared slots

Use a prepared slot when an ad is temporarily hidden, appears inside a pager, must survive locale/theme/rotation recreation, or is loaded during navigation.

`PreparedSlotController` loads through the same pools and recycles objects on release.

```kotlin
AdsPreparedSlot(
    placement = placement,
    keepUntilReleased = true,
)
```

One configuration-aware effect owns release:

```text
configuration change    -> keep slot
screen leaves normally  -> release slot
activity finishes       -> release slot
```

Do not infer ownership from whether the slot composable is currently visible.

## 9. Startup and splash standard

Never load an ad from `Application.onCreate`. Android may start a process for receivers, workers, alarms or notifications without visible UI.

Start splash preparation from the launcher activity only on a fresh activity start:

```kotlin
val isFreshStart = savedInstanceState == null
if (isFreshStart) startSplashAdPreparation()
```

Required splash behavior:

- restore persisted Remote Config before using wait values;
- wait for consent and SDK readiness inside a bounded window;
- request only if the splash placement is enabled;
- use a real timeout rather than a fake delay;
- navigate when the ad is unavailable or fails;
- keep a late interstitial available for another interstitial placement;
- never count an activity recreation as a new launch.

## 10. Launch-flow standard

Apps with conditional Language, Onboarding, Premium, consent education or similar startup screens should build one ordered plan.

```text
LaunchSession = First | Second | Returning
LaunchStep    = app-defined ordered steps
LaunchFlow    = session + remaining steps
```

Requirements:

- Build the plan once in a ViewModel or use case.
- Cache it for the splash ViewModel lifetime.
- Persist returning-launch count in DataStore.
- Increment once per real returning launch, not after rotation.
- Encode the remaining plan in navigation arguments for process restoration.
- Mark first-run complete at the correct terminal step.
- If no first-run step remains, mark it complete at splash.

Navigation-time preload rule:

```text
next step selected
-> preload only that step's slots
-> navigate
```

Do not preload every possible startup screen from splash.

## 11. App-open standard

Before app-open `loadAndShow`, require:

- app-open enabled by Remote Config;
- splash flow completed;
- current route permits app-open;
- manager ready and user eligible;
- `canShowFullScreenNow(appOpenPlacement)` true.

Ad-click return protection:

1. Every full-screen controller records `markAdClicked()` from the click callback.
2. If the app backgrounds within 10 seconds, skip the next app-open.
3. Clear the skip after it is consumed.
4. Preserve protection for external settings, share sheets, stores and system pickers.

## 12. Rewarded standard

Do not preload because an unlock dialog opened. Start or join the rewarded load only after the explicit watch-ad action.

```text
button tap
-> full-screen gate
-> cached rewarded or shared load
-> bounded wait
-> show rewarded if available
-> otherwise evaluate fallback
```

Recommended settings:

| Key | Default | Bounds |
|---|---:|---:|
| `rewarded_load_wait_sec` | 10 | 1–20 |
| `rewarded_fallback_wait_sec` | 6 | 1–15 |

Fallback rules:

- Use a dedicated fallback placement id for analytics.
- The fallback format is interstitial and reuses any cached or loading interstitial.
- Check the full-screen gate before starting the fallback request.
- If the app backgrounds during the wait, do not start fallback.
- Explicitly define whether a shown fallback grants the unlock.
- Keep a late rewarded result cached for the next tap.
- Grant a rewarded result only when the SDK reward callback fired.

## 13. Interstitial placement policy

This architecture controls reuse, not product placement.

- Preserve existing placements and triggers during an architecture-only migration.
- Give every placement a stable analytics id even though all interstitials share one cached object.
- Load only when that placement's policy is due.
- Do not request if any interstitial is already cached or loading.
- Continue the user action when no ad is ready unless a short bounded cover is explicitly approved.
- A busy `showThen` path still executes its continuation exactly once.
- Count caps and skips only after an ad was actually shown.
- Do not show on error, cancellation or recovery exits unless explicitly approved.

Time capping, per-session limits and trigger-skip patterns are app-level policies. The ads module must not know feature names.

## 14. Remote Config standard

Follow the host project's existing Remote Config pipeline. Do not add direct one-off reads in screens or controllers.

Every new setting requires:

1. a Firebase key or JSON field;
2. a safe local default;
3. a typed config field;
4. bounded conversion for counts and durations;
5. persistence through the existing config store;
6. one centralized read path;
7. tests for missing, malformed and unknown values.

Default policy:

- ad switches off;
- optional slots hidden;
- counts zero unless a non-zero operational default is required;
- durations conservative and clamped;
- unknown enums use a safe fallback;
- malformed payload keeps the last known good config.

Common keys:

| Key | Type | Safe default | Purpose |
|---|---|---:|---|
| `splash_interstitial` | bool | false | Splash interstitial |
| `splash_ad_max_wait_sec` | number | 12 | Splash wait |
| `app_open_on_resume` | bool | false | App-open on resume |
| `app_open_load_timeout_sec` | number | 8 | App-open wait |
| `inter_load_wait_sec` | number | 5 | Existing interstitial wait |
| `rewarded_load_wait_sec` | number | 10 | Rewarded wait after tap |
| `rewarded_fallback_wait_sec` | number | 6 | Fallback wait |
| `language_screen` | enum/number | 0 | Launch-session language policy |
| `show_onboarding_screen` | bool | app-defined | First-run onboarding |
| `first_session_premium` | enum/number | 0 | First-session premium position |
| `returning_session_premium` | enum/number | 0 | Returning premium policy |

One shared slot style scale is recommended:

```text
0 = off
1 = adaptive banner
2 = collapsible bottom banner
3 = medium rectangle
4 = small native
5 = medium native
```

## 15. Placement and ad-unit standard

Define placements centrally. Use names that describe product context.

```text
startup full-screen
resume app-open
rewarded unlock
rewarded fallback interstitial
screen bottom slot
screen inline slot
feature interstitial
prepared pager slot
```

- Placement id is stable analytics identity.
- Ad-unit id is request configuration.
- Cache key is reuse identity.
- These concepts must remain separate.
- Missing production ad units block safely.
- Debug builds use official test units.
- Production ad units never live in screen code.

## 16. Analytics standard

Report load success, load failure, impression/show, show failure and click.

Every event includes:

| Parameter | Meaning |
|---|---|
| `ad_format` | banner, native, interstitial, rewarded or app_open |
| `ad_placement` | current product placement id |
| `ad_detail` | normalized detail used by the app's reporting contract |

Register all three as GA4 custom dimensions before release. Registration is manual, is not retroactive and cannot be verified from source code.

When a pooled ad moves to another placement, reattach its callback before rendering so show and click events use the current placement.

## 17. Failure, timeout and cancellation rules

- No ad failure blocks navigation.
- No busy state silently drops a continuation.
- Use an atomic once-guard when both `onShown` and completion can continue a flow.
- Loading covers are bounded and removed in `finally`.
- Never use an artificial delay as a substitute for SDK readiness.
- Caller timeout does not cancel a useful module load.
- Activity destruction prevents presentation but does not invalidate a poolable result.
- A failed request does not count as an impression.
- A show failure releases the full-screen lock.

## 18. Dependency injection standard

Register in existing Koin modules only:

- one ads coroutine scope;
- one `AdsRuntimeState`;
- one request/show gate;
- one controller per ad family;
- one `PreparedSlotController`;
- one `AdsManager`;
- one lifecycle observer;
- app-level providers for pro status, units, styles and analytics.

Do not add another service locator, static dependency graph or screen-owned controller.

## 19. Implementation order

1. Inventory existing placements, triggers, Remote Config keys and analytics ids.
2. Add `AdPool<T>` and pooled banner/native controllers.
3. Add shared banner rendering and recycle-on-dispose behavior.
4. Convert prepared slots to use the pools.
5. Change full-screen cache and in-flight keys to format-only.
6. Add pre-request show gates, process lock and ad-click tracking.
7. Move splash loading from `Application` to the fresh launcher activity.
8. Add a launch-flow planner when startup screens are conditional.
9. Move slot preloading to navigation-time decisions.
10. Make temporary slots configuration-safe.
11. Change rewarded loading to tap-only and add fallback policy.
12. Migrate Remote Config through the centralized pipeline.
13. Add `ad_detail` and register GA4 dimensions.
14. Add tests, compile, package and perform device verification.

During an architecture-only migration, do not change existing interstitial placements or their product trigger rules.

## 20. Automated test requirements

At minimum, verify:

- unseen pooled object is reused without another load;
- impressed and expired objects are destroyed;
- concurrent acquisitions join one in-flight load;
- maximum pool size is enforced;
- full-screen key depends only on format;
- caller timeout leaves a late module result reusable;
- full-screen lock rejects simultaneous presentations;
- busy `showThen` invokes continuation once;
- launch-flow encoding round-trips;
- returning count is not incremented on ViewModel reuse;
- Remote Config defaults, malformed values, enums and clamps;
- analytics include format, placement and detail.

Required build verification:

```text
ads module compile
app module compile
ads unit tests
app unit tests
debug package or assemble
```

## 21. Device test matrix

### Eligibility

- Free user can request enabled placements.
- Pro user creates no requests.
- Consent denied creates no requests.
- Offline state creates no new requests.
- A loaded compatible ad can still be reused when a new request is impossible.

### Splash and launch

- Cold launcher start can request splash.
- Receiver, worker or service process start does not request.
- Rotation during splash does not count another launch.
- Each launch step preloads only its own slots.
- Process restoration continues the remaining plan.

### Full-screen reuse

- One loaded interstitial can serve another interstitial placement.
- A second trigger joins an active interstitial load.
- App-open does not request while another full-screen ad is busy.
- Returning within 10 seconds of an ad click skips app-open.
- A normal later background return can show app-open.

### Banner and native

- Leaving before impression recycles the ad.
- Returning to a compatible slot does not request again.
- After impression, leaving destroys the object.
- Reused ads report the new placement.
- Different banner sizes do not share an object.

### Configuration and temporary visibility

- Rotation and locale recreation do not replace an unseen prepared ad.
- Temporarily hiding a slot does not release it.
- Permanently leaving the owner screen releases it.

### Rewarded

- Opening and dismissing the dialog creates no request.
- Tapping watch creates or joins one request.
- Backgrounding during wait does not start fallback.
- Late rewarded load is reused on the next tap.
- Reward and fallback grant behavior matches the documented product rule.

### UI

- Fixed bottom ads remain outside scrolling content.
- Snackbars, mini players and navigation bars do not overlap ads.
- Insets do not create double spacing.
- LTR and RTL layouts remain correct.

## 22. Anti-patterns

Do not:

- load from `Application.onCreate`;
- key full-screen cache by placement or ad-unit id;
- load rewarded when a dialog merely opens;
- preload every destination from splash;
- destroy unseen banner/native ads on every Compose disposal;
- release prepared slots during configuration changes;
- let screens call the ad SDK directly;
- read Remote Config directly from screens or controllers;
- request while a compatible load is active;
- count a request, load or failed show as an impression;
- block navigation indefinitely for an ad;
- add placements as a side effect of architecture migration;
- hardcode production switches, waits or ad-unit ids;
- introduce parallel DI, navigation or configuration systems.

## 23. Completion criteria

The migration is complete only when:

- existing placements are intentionally preserved or explicitly changed;
- every full-screen format has one shared cache and in-flight load;
- banner/native objects use impression-aware pools;
- Compose disposal recycles rather than blindly destroys;
- startup requests originate from visible activity flow;
- conditional screens use navigation-time preload;
- rewarded starts on explicit user action;
- app-open has show-gate and click-return protection;
- Remote Config defaults are safe and tested;
- GA4 dimensions are registered;
- compile, unit tests and debug packaging pass;
- device tests cover free, pro, offline, rotation and RTL cases.
