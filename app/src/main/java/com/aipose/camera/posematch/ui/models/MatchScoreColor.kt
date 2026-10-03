package com.aipose.camera.posematch.ui.models

import androidx.compose.ui.graphics.Color
import com.aipose.camera.posematch.ui.theme.ScoreHigh
import com.aipose.camera.posematch.ui.theme.ScoreLow

private const val HIGH_SCORE_THRESHOLD = 80

fun matchScoreColor(score: Int): Color = if (score >= HIGH_SCORE_THRESHOLD) ScoreHigh else ScoreLow
