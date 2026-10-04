package com.aipose.camera.posematch.ui.screens.camera.models

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.PoseAmber400
import com.aipose.camera.posematch.ui.theme.PoseEmerald400
import com.aipose.camera.posematch.ui.theme.PoseRose400

enum class MatchFeedback(
    val minimumScore: Int,
    val ringColor: Color,
    @StringRes val titleRes: Int,
    @StringRes val hintRes: Int
) {
    Perfect(
        minimumScore = 80,
        ringColor = PoseEmerald400,
        titleRes = R.string.camera_match_great,
        hintRes = R.string.camera_match_hint_hold
    ),
    Almost(
        minimumScore = 60,
        ringColor = PoseEmerald400,
        titleRes = R.string.match_status_almost,
        hintRes = R.string.camera_match_hint_adjust
    ),
    Adjusting(
        minimumScore = 30,
        ringColor = PoseAmber400,
        titleRes = R.string.match_status_adjust,
        hintRes = R.string.camera_match_hint_adjust
    ),
    Starting(
        minimumScore = 1,
        ringColor = PoseAmber400,
        titleRes = R.string.match_status_raise,
        hintRes = R.string.camera_match_hint_frame
    ),
    None(
        minimumScore = 0,
        ringColor = PoseRose400,
        titleRes = R.string.match_status_move,
        hintRes = R.string.camera_match_hint_frame
    );

    companion object {
        fun forScore(score: Int): MatchFeedback =
            entries.firstOrNull { score >= it.minimumScore } ?: None
    }
}
