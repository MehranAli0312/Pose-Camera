# PoseAI — Navigation Map (Figma redesign)

File: Pose-Camera · page "complete design" · all frames 390×844 (Home 390×980)

Every tappable element in the redesign and the frame it opens. Rows in the Figma page are
grouped one row per feature flow.

## Figma page layout

| Row | y | Frames (x) |
|---|---|---|
| 1 — Onboarding flow | 0 | Splash (0) · Language (450) · Onboarding 1 (900) · Onboarding 2 (1350) · Onboarding 3 (1800) · Home (2250) |
| 2 — Capture & edit flow | 1200 | Live Pose Camera (0) · Photo Edit · Filters (450) · Photo Edit · Adjust (900) · Photo Edit · Crop (1350) · Capture Saved (1800) |
| 3 — Pose browsing flow | 2400 | All Poses / Explore (0) · Pose Detail (900) |
| 4 — Library flow | 3600 | Collections (0) · Location Album (450) · Photo Detail (900) · Saved / Favorites (1350) |
| 5 — Profile & settings flow | 4800 | Progress & Stats (0) · Achievements (450) · Settings (900) · Theme Picker (1350) · Capture Timer Sheet (1800) |

## Row 1 — Onboarding

| Screen | Tap target | Opens |
|---|---|---|
| Splash | auto after load | Language |
| Language | any language row | stays, marks selection |
| Language | Continue | Onboarding 1 |
| Onboarding 1/2/3 | Next | next onboarding page |
| Onboarding 1/2 | Skip | Home |
| Onboarding 3 | Get Started | Home |

## Row 1 — Home

| Tap target | Opens |
|---|---|
| Search field | All Poses (search focused) |
| Streak pill / trophy | Achievements |
| Category tile (Sunset, Beach, Couple, …) | All Poses, with that category chip pre-selected |
| Trending pose card | Pose Detail |
| **Explore All** (Trending header) | All Poses / Explore |
| Your Progress card body | Progress & Stats |
| **View all** (Your Progress header) | Progress & Stats |
| Recent capture thumbnail | Photo Detail |
| Bottom bar · Home | current |
| Bottom bar · Collections | Collections |
| Bottom bar · **Camera FAB** | Live Pose Camera |
| Bottom bar · **Saved** | Saved / Favorites |
| Bottom bar · Settings | Settings |

## Row 2 — Capture & edit

| Screen | Tap target | Opens |
|---|---|---|
| Live Pose Camera | pose-name pill (bottom-left) | All Poses, presented as a pose-picker sheet |
| Live Pose Camera | opacity slider / flash / grid / flip | in-place, no navigation |
| Live Pose Camera | timer icon | Capture Timer Sheet |
| Live Pose Camera | shutter | Photo Edit · Filters |
| Live Pose Camera | back | previous screen (Home) |
| Photo Edit | **Filters** tab | Photo Edit · Filters |
| Photo Edit | **Adjust** tab | Photo Edit · Adjust (Exposure, Brightness, Contrast, Saturation, Warmth, Tint, Hue, Fade sliders) |
| Photo Edit | **Crop** tab | Photo Edit · Crop (ratio chips, Rotate L/R, Flip H/V, 1080 × 1350 readout) |
| Photo Edit | Save / Done | Capture Saved |
| Photo Edit | back / discard | Live Pose Camera |
| Capture Saved | Share | OS share sheet (no frame) |
| Capture Saved | View in Collections | Collections |
| Capture Saved | Shoot again | Live Pose Camera |
| Capture Saved | Home | Home |

## Row 3 — Pose browsing

| Screen | Tap target | Opens |
|---|---|---|
| All Poses | category chip / difficulty chip | filters in place |
| All Poses | pose card | Pose Detail |
| All Poses | heart on a card | toggles favorite, no navigation |
| All Poses | back | caller (Home or Camera sheet) |
| Pose Detail | **Use this pose** | Live Pose Camera with that overlay loaded |
| Pose Detail | favorite | toggles, no navigation |
| Pose Detail | similar-pose card | Pose Detail (replaces) |

## Row 4 — Library

| Screen | Tap target | Opens |
|---|---|---|
| Collections | All / Favorites / Top Match / Recent chips | filters in place |
| Collections | **See all** on a location group | Location Album |
| Collections | photo thumbnail | Photo Detail |
| Location Album | shot thumbnail | Photo Detail |
| Location Album | Shoot here again | Live Pose Camera |
| Location Album | Remove all shots from this place | destructive confirm dialog |
| Photo Detail | Edit | Photo Edit · Filters |
| Photo Detail | Share | OS share sheet |
| Photo Detail | favorite | toggles, no navigation |
| Photo Detail | Delete | destructive confirm dialog |
| Photo Detail | Retake this pose | Live Pose Camera with the same overlay |
| Saved / Favorites | Poses / Photos segment | switches list in place |
| Saved / Favorites | saved pose card | Pose Detail |
| Saved / Favorites | saved photo card | Photo Detail |

## Row 5 — Profile & settings

| Screen | Tap target | Opens |
|---|---|---|
| Progress & Stats | range chips (Week / Month / All) | re-renders chart in place |
| Progress & Stats | **View all badges** | Achievements |
| Progress & Stats | best-shot thumbnail | Photo Detail |
| Achievements | badge tile | badge detail sheet (in-frame expanded state) |
| Settings | Language | Language (settings entry) |
| Settings | Theme | Theme Picker |
| Settings | Default timer | Capture Timer Sheet |
| Settings | Share app | OS share sheet |
| Settings | Rate us | Play Store (external) |
| Settings | Privacy Policy | external browser |
| Theme Picker | theme swatch | applies, returns to Settings |
| Capture Timer Sheet | Off / 3s / 5s / 10s | applies, dismisses |

## Reuse decisions (no separate frames drawn)

- **Home category tiles** and the **Camera pose-name pill** both open **All Poses** — one with the
  category chip pre-selected, one presented as a bottom sheet. No separate "Category Album" or
  "Pose Picker" frame is needed.
- **Share** anywhere opens the OS share sheet, not an app screen.
- Destructive actions (delete photo, remove location) use one confirm-dialog pattern over the
  current frame rather than a dedicated frame.
