# Play Store icons — PoseAI

Three variants, 512 × 512, 32-bit PNG, full-bleed, opaque (Play Console masks the corners itself).
Rendered at 2048 px and downsampled, so edges stay clean at every listing size.

| File | Variant | Idea |
|---|---|---|
| `ic_playstore_v1_pose_match.png` | Pose Match | Brand navy + gradient match ring (cyan → indigo → violet → pink) around a white pose figure with cyan ML-Kit landmark dots. Says "pose + live match %". |
| `ic_playstore_v2_lens.png` | Lens | Indigo → violet → pink gradient, white lens disc with a deep-navy pose figure. Brightest and most legible at 48 px. |
| `ic_playstore_v3_overlay.png` | Overlay | Cyan ghost pose behind a white pose inside camera framing brackets. Says "align yourself to the overlay". |

`_preview.png` is a contact sheet showing all three at 240 px with a 48 px row underneath for
small-size legibility.

Colours are the redesign tokens: navy `#141A3D → #0B0E24 → #060814`, indigo `#6366F1`,
violet `#8B5CF6`, pink `#EC4899`, cyan `#06B6D4`.

No text in any variant, per Play Store icon guidance.

Regenerate or tweak with `tools/make_icons.py`.
