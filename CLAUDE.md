---
description: project rule file. Architecture, UI, Figma-to-Compose.
alwaysApply: true
---

#Project Rules

This is the **only** rule file. Write all code inside these rules and the existing repo patterns. Reuse first. Do not invent parallel architecture, navigation, DI, permission, or delete flows.

**Reuse → Extend → Implement.** Never rebuild.

---

## Stack

Kotlin · Jetpack Compose · Material 3 · MVVM + Clean Architecture · **Koin only** · Navigation Compose · Coroutines + Flow · **StateFlow** (not LiveData) · DataStore · Coil · Lottie · Shimmer · ML Kit · Firebase (Analytics, Crashlytics, Remote Config) · Glance · OkHttp if needed · minSdk 24 · compile/target 37 · Java 11


---

## Architecture

```text
UI → ViewModel → UseCase → Repository → DataSource
```

- **UI:** render state, user events, navigation. No Repository, DataSource, MediaStore, file IO, or business logic.
- **ViewModel:** thin. State + UseCases. `viewModelScope`. Never `GlobalScope`.
- **UseCase:** business rules and multi-step work.
- **Repository:** data abstraction. No UI logic.
- **DataSource:** MediaStore, file system, Android APIs.
- Domain layer: no Android APIs (`Context`, `Uri`, `Cursor`, `Bitmap`, MediaStore types).
- State: `private val _uiState = MutableStateFlow(...)` + `val uiState = _uiState.asStateFlow()`.
- Prefer sealed UI states (`Loading` / `Content` / `Empty` / `Error`). No impossible boolean combos.
- Cancel previous scan before starting a new one. No fake delays instead of real work.
- One focused type per file. Do not add unused interfaces or one-impl wrappers.

---

## Screen files (mandatory)

`*Screen.kt` is a thin entry only: collect state, navigate, wire callbacks. Tiny private helpers (&lt; ~15 lines) only if extracting hurts clarity.

**Do not keep in `*Screen.kt`:** enums / sealed types, UI `data class`es, static lists, list / item / shimmer / empty / card composables.

```text
ui/screens/<feature>/
├── <Feature>Screen.kt     # entry + orchestration
├── components/            # list, item, shimmer, empty, bars, dialogs
├── models/                # tabs, filters, presentation models
└── data/                  # static configs only if not domain
```

Shared UI → `ui/common/`. Feature-only → feature `components/` / `models/`. Domain models stay in `domain/models`.

---

**Remote Config keys:** every new key follows the same path as the existing ones. Never read `remoteConfig.getX(...)` directly outside `readRemoteConfig()`, and never add a one-off getter on `AppFirebaseRemote`.
1. `const val <NAME>_KEY` in `AppFirebaseRemote` companion.
2. Default `<entry>` in `res/xml/remote_config_defaults.xml`.
3. Field with the same default in `AdsRemoteConfig` (use a `fromRemote` enum/value class for multi-value `Long` keys, like `BottomAdPosition`).
4. Read it in `AppFirebaseRemote.readRemoteConfig()`.
5. Persist in `AdsRemoteConfigStore`: `getX(KEY, defaults.field)` in `restore()` and `putX(KEY, config.field)` in `update()`.
6. Consumers inject `AdsRemoteConfigStore` (Koin) and read `store.current.<field>` (or collect `store.config` when the UI must react to changes).

---

## Comments (mandatory)

**This project is comment-free. Never add a comment to any file.**

- No `//`, no `/* */`, no KDoc `/** */`, no XML `<!-- -->`, no TODO / FIXME / region markers, no commented-out code.
- This applies to every file you create or edit: Kotlin, Gradle KTS, XML, manifest, resources, JSON.
- Never re-add comments that were removed, and never explain code in a trailing comment.
- Make the code self-explanatory instead: clear names, small focused functions, extracted composables, sealed states, named constants instead of magic values.
- If something truly cannot be understood without prose, that is a signal to rename or split it, not to comment it.
- Only exception: strings that merely look like comments (URLs such as `"https://..."`, `content://`, `market://`) stay as they are.

---

## UI and resources

- Reuse theme colors, typography, and existing components () before creating new ones.
- No hardcoded user-facing strings or URLs. Use `strings.xml` / plurals / content descriptions.
- Match existing coding style. Smallest safe change. Do not rewrite unrelated files.
- Register new VM / UseCase / Repo / DataSource in existing Koin modules only.

---

## Figma → Compose (MCP)

When the user pastes a Figma URL or asks to match a Figma frame, this section is mandatory.

Parse `https://www.figma.com/design/:fileKey/...?node-id=30-4778` → `fileKey` + `nodeId` `30:4778` (hyphen → colon). Do not guess a node id.

### Always

1. Inspect the **existing** screen, ViewModel, nav, state, and reusable components **before** any UI change.
2. Pull design via Figma MCP: `get_design_context` first (primary). Use screenshot only to orient or verify. Download real icons/images via MCP assets.
3. Treat MCP output as a **reference**. Adapt it to this repo: Compose + Material 3 + existing theme/components. Never paste React/Tailwind.
4. If the named screen already exists: **update/refactor it**. Do not create a duplicate screen.
5. Preserve behavior, ViewModel, and navigation unless the design is UI-only.
6. Use **actual Figma assets** when MCP provides them. No placeholder icons, drawn SVGs, or “close enough” images.
7. Match spacing, padding, size, type, color, corner radius, alignment, and layout as closely as possible.
8. Map Figma colors/type to existing theme tokens when they match. Add a resource only when the design introduces a new token the theme does not have.
9. After coding, compare the result to the Figma frame and these rules; fix mismatches.


# RTL / Arabic support (all Android Compose projects)

Every app must work in Arabic (RTL). Android mirrors the layout in RTL; that is correct.
Never force the whole app to LTR (`android:supportsRtl="false"` or a root
`LayoutDirection.Ltr`). Write all new UI code so it already works in RTL:

- **Direction-neutral layout:** use `start`/`end`, never `left`/`right`, in padding,
  alignment and arrangement. No hard-coded x offsets that assume LTR.
- **Directional icons:** every new vector icon that shows direction (back, forward, next,
  list chevron, prev/next) gets `android:autoMirrored="true"`, after `xmlns:android`.
  Don't mirror media controls, up/down arrows, checkmarks, logos, flags or illustrations.
  If you rotate a mirrored icon by ±90°, flip the angle in RTL
  (`LocalLayoutDirection.current == LayoutDirection.Rtl`).
- **Number + unit:** a number next to a Latin unit ("1.5 MB", "32.5 °C", "4432 mV")
  swaps order in RTL. Build these values only through the project's central formatter,
  and wrap them in a first-strong isolate (U+2068 … U+2069). If the project has no helper
  yet, add one once in `util/BidiText.kt`:
  `fun String.bidiIsolate(): String = "\u2068$this\u2069"`.
  Use U+2068, not U+2066 (U+2066 reverses translated Arabic words). If a number and a
  unit are two `Text`s in a `Row`, wrap that `Row` in `LtrLayout { }`.
- **Charts and Canvas:** a Canvas always draws left-to-right, but the Texts around it
  mirror. Wrap every chart that has axis or slot labels in `LtrLayout { }` (add
  `ui/common/LtrLayout.kt` once if missing:
  `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr, content = content)`).
  Any other custom drawing must read `LocalLayoutDirection`.
- **Digits:** the app locale is set with Latin digits (`nu=latn`) for languages whose
  default digits aren't 0–9 (`LanguageViewModel.applyLanguage`). Don't bypass it: format
  numbers with the default locale, or with `Locale.US` only where a fixed format is
  required. Never hard-code Arabic-Indic digits.
- **Strings:** every user-visible text goes in `strings.xml` with placeholders
  (`%1$s MB`) so translators control word order. Never build sentences by joining
  strings in code.
- **Don't** set `TextDirection.Content` globally in the typography, and don't
  force-LTR whole screens to hide an RTL bug; fix the specific component.

When a change touches UI, say in the reply whether it was checked in Arabic (RTL). When
testing on a device in RTL, tap by text via `uiautomator dump` bounds, not by fixed
coordinates.