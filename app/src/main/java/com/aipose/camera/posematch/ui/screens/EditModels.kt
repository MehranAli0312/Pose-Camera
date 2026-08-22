package com.aipose.camera.posematch.ui.screens

// Result of an edit session: colour matrix + geometry (rotation in degrees, crop aspect w/h).
data class EditResult(val matrix: FloatArray?, val rotationDeg: Int, val cropAspect: Float?)

// The manual adjustment tools in the editor's "Adjust" tab. Each colour tool maps to a linear
// color transform; Rotate/Crop are geometry tools with their own controls. (Icons are mapped to
// drawables by the editor binder.)
enum class AdjustTool(val label: String) {
    Exposure("Exposure"),
    Brightness("Brightness"),
    Contrast("Contrast"),
    Saturation("Saturation"),
    Warmth("Warmth"),
    Tint("Tint"),
    Hue("Hue"),
    Fade("Fade"),
    Rotate("Rotate"),
    Crop("Crop")
}
