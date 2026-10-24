package com.aipose.camera.posematch.domain.engagement

import com.aipose.camera.posematch.domain.models.Capture
import com.aipose.camera.posematch.domain.models.CaptureLocation
import com.aipose.camera.posematch.domain.models.EngagementHistory
import com.aipose.camera.posematch.domain.models.EngagementNotification
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.domain.models.PoseSource
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone
import kotlin.random.Random

class EngagementNotificationPickerTest {

    private val picker = EngagementNotificationPicker(Random(SEED))
    private val pose = Pose(7, "Pose", "Viral", "", "Easy", emptyList(), "pose.webp", PoseSource.Bundled, emptyMap())
    private lateinit var originalTimeZone: TimeZone

    @Before
    fun useFixedTimeZone() {
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun restoreTimeZone() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun streakSaverWinsWhenTodayHasNoCapture() {
        val now = at(2026, Calendar.OCTOBER, 23, 19)
        val captures = listOf(capture(1, at(2026, Calendar.OCTOBER, 22, 10)))

        val picked = picker.pick(captures, listOf(pose), EngagementHistory(), currentStreak = 3, nowMillis = now)

        assertEquals(EngagementNotification.StreakSaver(3), picked)
    }

    @Test
    fun noStreakSaverAfterCapturingToday() {
        val now = at(2026, Calendar.OCTOBER, 23, 19)
        val captures = listOf(capture(1, at(2026, Calendar.OCTOBER, 23, 9)))

        val picked = picker.pick(captures, listOf(pose), history(lastOpen = now), currentStreak = 3, nowMillis = now)

        assertNull(picked)
    }

    @Test
    fun yearAgoMemoryPrefersFavoriteOverHigherScore() {
        val now = at(2026, Calendar.OCTOBER, 23, 19)
        val favorite = capture(1, at(2025, Calendar.OCTOBER, 23, 8), score = 60, isFavorite = true)
        val best = capture(2, at(2024, Calendar.OCTOBER, 23, 8), score = 95)

        val picked = picker.pick(listOf(favorite, best), listOf(pose), history(lastOpen = now), 0, now)

        assertEquals(EngagementNotification.Memory(favorite), picked)
    }

    @Test
    fun weekAgoMemoryWhenNoOlderMatch() {
        val now = at(2026, Calendar.OCTOBER, 23, 19)
        val weekAgo = capture(1, at(2026, Calendar.OCTOBER, 16, 12))

        val picked = picker.pick(listOf(weekAgo), listOf(pose), history(lastOpen = now), 0, now)

        assertEquals(EngagementNotification.Memory(weekAgo), picked)
    }

    @Test
    fun firstPhotoNudgeTheDayAfterLastOpen() {
        val now = at(2026, Calendar.OCTOBER, 23, 19)
        val yesterday = at(2026, Calendar.OCTOBER, 22, 20)

        val picked = picker.pick(emptyList(), listOf(pose), history(lastOpen = yesterday), 0, now)

        assertEquals(EngagementNotification.FirstPhoto(pose), picked)
    }

    @Test
    fun poseIdeaOnlyOnComebackDays() {
        val now = at(2026, Calendar.OCTOBER, 23, 19)
        val captures = listOf(capture(1, at(2026, Calendar.SEPTEMBER, 1, 12)))

        val onDayFive = picker.pick(captures, listOf(pose), history(lastOpen = now - days(5)), 0, now)
        val onDaySix = picker.pick(captures, listOf(pose), history(lastOpen = now - days(6)), 0, now)

        assertEquals(EngagementNotification.PoseIdea(pose), onDayFive)
        assertNull(onDaySix)
    }

    @Test
    fun sendsAtMostOncePerDay() {
        val now = at(2026, Calendar.OCTOBER, 23, 19)
        val history = EngagementHistory(lastSentAtMillis = at(2026, Calendar.OCTOBER, 23, 9))

        val picked = picker.pick(emptyList(), listOf(pose), history, currentStreak = 4, nowMillis = now)

        assertNull(picked)
    }

    @Test
    fun backsOffToEveryOtherDayAfterThreeUnopened() {
        val now = at(2026, Calendar.OCTOBER, 23, 19)
        val history = EngagementHistory(
            lastSentAtMillis = at(2026, Calendar.OCTOBER, 22, 19),
            unopenedInARow = 3,
        )

        val picked = picker.pick(emptyList(), listOf(pose), history, currentStreak = 4, nowMillis = now)

        assertNull(picked)
    }

    @Test
    fun longAbsenceOnlySendsMemories() {
        val now = at(2026, Calendar.OCTOBER, 23, 19)
        val captures = listOf(capture(1, at(2026, Calendar.JANUARY, 2, 12)))

        val picked = picker.pick(captures, listOf(pose), history(lastOpen = now - days(30 + 1)), 0, now)

        assertNull(picked)
    }

    @Test
    fun preferredHourFollowsCaptureHabitWithinWakingHours() {
        val habit = listOf(23, 23, 23, 14).mapIndexed { index, hour ->
            capture(index.toLong(), at(2026, Calendar.OCTOBER, index + 1, hour))
        }

        assertEquals(19, picker.preferredHour(habit.take(2)))
        assertEquals(21, picker.preferredHour(habit))
    }

    private fun history(lastOpen: Long) = EngagementHistory(lastAppOpenAtMillis = lastOpen)

    private fun capture(id: Long, at: Long, score: Int = 80, isFavorite: Boolean = false) = Capture(
        id = id,
        imagePath = "capture_$id.jpg",
        capturedAtMillis = at,
        poseId = null,
        matchScore = score,
        category = "Viral",
        title = "",
        isFavorite = isFavorite,
        location = CaptureLocation(null, null, null),
    )

    private fun at(year: Int, month: Int, day: Int, hour: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month, day, hour, 0)
        }.timeInMillis

    private fun days(count: Int): Long = count * MILLIS_PER_DAY

    private companion object {
        const val SEED = 42
        const val MILLIS_PER_DAY = 86_400_000L
    }
}
