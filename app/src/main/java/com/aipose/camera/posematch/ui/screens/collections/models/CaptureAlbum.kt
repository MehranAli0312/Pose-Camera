package com.aipose.camera.posematch.ui.screens.collections.models

data class CaptureAlbum(
    val locationLabel: String,
    val captures: List<CaptureUi>
) {
    val count: Int get() = captures.size
}
