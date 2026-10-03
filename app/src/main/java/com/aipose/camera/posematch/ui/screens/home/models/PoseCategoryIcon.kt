package com.aipose.camera.posematch.ui.screens.home.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.ui.graphics.vector.ImageVector
import com.aipose.camera.posematch.ui.screens.home.data.PoseCategories

private val categoryIcons = mapOf(
    PoseCategories.VIRAL to Icons.Default.Whatshot,
    PoseCategories.COUPLE to Icons.Default.Favorite,
    PoseCategories.SUNSET to Icons.Default.WbTwilight,
    PoseCategories.DARK to Icons.Default.DarkMode,
    PoseCategories.MIRROR to Icons.Default.Flip,
    PoseCategories.BEACH to Icons.Default.BeachAccess,
    PoseCategories.CAFE to Icons.Default.Coffee,
    PoseCategories.FAMILY to Icons.Default.FamilyRestroom,
    PoseCategories.NATURE to Icons.Default.Nature,
    PoseCategories.WATERFALL to Icons.Default.Water,
)

fun iconForCategory(category: String): ImageVector =
    categoryIcons[category] ?: Icons.Default.Collections
