package com.aipose.camera.posematch.ui.screens.camera.models

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.MatchHigh
import com.aipose.camera.posematch.ui.theme.MatchLow
import com.aipose.camera.posematch.ui.theme.MatchMedium

enum class MatchFeedback(
    val minimumScore: Int,
    val badgeColor: Color,
    @StringRes val labelRes: Int
) {
    Perfect(80, MatchHigh, R.string.match_status_perfect),
    Almost(60, MatchMedium, R.string.match_status_almost),
    Adjusting(30, MatchLow, R.string.match_status_adjust),
    Starting(1, MatchLow, R.string.match_status_raise),
    None(0, MatchLow, R.string.match_status_move);

    companion object {
        fun forScore(score: Int): MatchFeedback =
            entries.firstOrNull { score >= it.minimumScore } ?: None
    }
}
