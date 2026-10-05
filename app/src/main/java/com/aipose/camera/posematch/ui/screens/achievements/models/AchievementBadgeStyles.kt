package com.aipose.camera.posematch.ui.screens.achievements.models

import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.AchievementBadge
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette

private val CameraGlyph = 30.dp
private val TargetGlyph = 24.dp
private val CheckGlyph = 18.dp
private val FlameGlyph = 22.dp
private val HeartGlyph = 18.dp
private val TrophyGlyph = 32.dp

val AchievementBadge.style: AchievementBadgeStyle
    get() = when (this) {
        AchievementBadge.FirstShot -> AchievementBadgeStyle(
            R.string.achievements_badge_first_shot, R.drawable.ic_pose_camera, GlossyBadgePalette.Violet, CameraGlyph
        )
        AchievementBadge.TenPoses -> AchievementBadgeStyle(
            R.string.achievements_badge_ten_poses, R.drawable.ic_pose_target, GlossyBadgePalette.Cyan, TargetGlyph
        )
        AchievementBadge.NinetyMatch -> AchievementBadgeStyle(
            R.string.achievements_badge_ninety_match, R.drawable.ic_pose_check_small, GlossyBadgePalette.Emerald, CheckGlyph
        )
        AchievementBadge.WeekStreak -> AchievementBadgeStyle(
            R.string.achievements_badge_week_streak, R.drawable.ic_pose_flame, GlossyBadgePalette.Orange, FlameGlyph
        )
        AchievementBadge.TenFavorites -> AchievementBadgeStyle(
            R.string.achievements_badge_ten_favorites, R.drawable.ic_pose_heart, GlossyBadgePalette.Pink, HeartGlyph
        )
        AchievementBadge.SixPlaces -> AchievementBadgeStyle(
            R.string.achievements_badge_six_places, R.drawable.ic_pose_trophy, GlossyBadgePalette.Amber, TrophyGlyph
        )
        AchievementBadge.HundredShots -> AchievementBadgeStyle(
            R.string.achievements_badge_hundred_shots, R.drawable.ic_pose_camera, GlossyBadgePalette.Indigo, CameraGlyph
        )
        AchievementBadge.Flawless -> AchievementBadgeStyle(
            R.string.achievements_badge_flawless, R.drawable.ic_pose_check_small, GlossyBadgePalette.Sky, CheckGlyph
        )
        AchievementBadge.ThirtyDayPro -> AchievementBadgeStyle(
            R.string.achievements_badge_thirty_day_pro, R.drawable.ic_pose_trophy, GlossyBadgePalette.Rose, TrophyGlyph
        )
        AchievementBadge.TenDayStreak -> AchievementBadgeStyle(
            R.string.achievements_badge_ten_day_streak, R.drawable.ic_pose_flame, GlossyBadgePalette.Amber, FlameGlyph
        )
        AchievementBadge.TwentyFivePerfect -> AchievementBadgeStyle(
            R.string.achievements_badge_twenty_five_perfect, R.drawable.ic_pose_check_small, GlossyBadgePalette.Blue, CheckGlyph
        )
        AchievementBadge.FiftyPoses -> AchievementBadgeStyle(
            R.string.achievements_badge_fifty_poses, R.drawable.ic_pose_target, GlossyBadgePalette.Brand, TargetGlyph
        )
    }
