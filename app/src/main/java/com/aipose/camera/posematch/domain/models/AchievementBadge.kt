package com.aipose.camera.posematch.domain.models

enum class AchievementBadge(val metric: AchievementMetric, val target: Int) {
    FirstShot(AchievementMetric.Shots, 1),
    TenPoses(AchievementMetric.PosesTried, 10),
    NinetyMatch(AchievementMetric.BestMatch, 90),
    WeekStreak(AchievementMetric.BestStreak, 7),
    TenFavorites(AchievementMetric.Favorites, 10),
    SixPlaces(AchievementMetric.Places, 6),
    HundredShots(AchievementMetric.Shots, 100),
    Flawless(AchievementMetric.BestMatch, 100),
    ThirtyDayPro(AchievementMetric.BestStreak, 30),
    TenDayStreak(AchievementMetric.BestStreak, 10),
    TwentyFivePerfect(AchievementMetric.PerfectShots, 25),
    FiftyPoses(AchievementMetric.PosesTried, 50)
}
