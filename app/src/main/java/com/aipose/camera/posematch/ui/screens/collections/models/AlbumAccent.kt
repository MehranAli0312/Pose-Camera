package com.aipose.camera.posematch.ui.screens.collections.models

import androidx.compose.ui.graphics.Color
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.theme.PoseAmber400
import com.aipose.camera.posematch.ui.theme.PoseAmberPale
import com.aipose.camera.posematch.ui.theme.PoseCyanBright
import com.aipose.camera.posematch.ui.theme.PoseCyanLight
import com.aipose.camera.posematch.ui.theme.PoseEmerald400
import com.aipose.camera.posematch.ui.theme.PoseEmeraldLight
import com.aipose.camera.posematch.ui.theme.PosePinkLabel
import com.aipose.camera.posematch.ui.theme.PosePinkSoft
import com.aipose.camera.posematch.ui.theme.PoseVioletPale
import com.aipose.camera.posematch.ui.theme.Violet as VioletColor

enum class AlbumAccent(val pin: Color, val label: Color, val palette: GlossyBadgePalette) {
    Violet(VioletColor, PoseVioletPale, GlossyBadgePalette.Violet),
    Cyan(PoseCyanBright, PoseCyanLight, GlossyBadgePalette.Cyan),
    Pink(PosePinkSoft, PosePinkLabel, GlossyBadgePalette.Pink),
    Amber(PoseAmber400, PoseAmberPale, GlossyBadgePalette.Amber),
    Emerald(PoseEmerald400, PoseEmeraldLight, GlossyBadgePalette.Emerald);

    companion object {
        fun forLabel(locationLabel: String): AlbumAccent =
            entries[Math.floorMod(locationLabel.hashCode(), entries.size)]
    }
}
