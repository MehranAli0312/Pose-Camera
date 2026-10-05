package com.aipose.camera.posematch.ui.models

import androidx.compose.ui.graphics.Color
import com.aipose.camera.posematch.ui.theme.PoseAmber400
import com.aipose.camera.posematch.ui.theme.PoseEmerald400
import com.aipose.camera.posematch.ui.theme.PoseRed400

enum class PoseDifficulty(val key: String, val dotColor: Color) {
    Easy("Easy", PoseEmerald400),
    Medium("Medium", PoseAmber400),
    Hard("Hard", PoseRed400);

    companion object {
        fun fromKey(key: String): PoseDifficulty? =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) }

        fun ordered(keys: Collection<String>): List<String> {
            val known = entries.mapNotNull { difficulty ->
                keys.firstOrNull { it.equals(difficulty.key, ignoreCase = true) }
            }
            return known + keys.filter { key -> fromKey(key) == null }
        }
    }
}
