package com.aipose.camera.posematch.ui.screens.progress.models

import androidx.compose.ui.graphics.Color
import com.aipose.camera.posematch.ui.models.PoseCategories
import com.aipose.camera.posematch.ui.theme.PoseAmber400
import com.aipose.camera.posematch.ui.theme.PoseCyanLight
import com.aipose.camera.posematch.ui.theme.PoseEmerald400
import com.aipose.camera.posematch.ui.theme.PoseIndigo400
import com.aipose.camera.posematch.ui.theme.PoseOrange400
import com.aipose.camera.posematch.ui.theme.PosePinkSoft
import com.aipose.camera.posematch.ui.theme.PoseSkyLight
import com.aipose.camera.posematch.ui.theme.PoseVioletLight

private val categoryAccents = mapOf(
    PoseCategories.SUNSET to PoseAmber400,
    PoseCategories.COUPLE to PosePinkSoft,
    PoseCategories.VIRAL to PoseOrange400,
    PoseCategories.BEACH to PoseCyanLight,
    PoseCategories.NATURE to PoseEmerald400,
    PoseCategories.WATERFALL to PoseSkyLight,
    PoseCategories.MIRROR to PoseIndigo400,
)

fun categoryAccentColor(category: String): Color =
    categoryAccents[category] ?: PoseVioletLight
