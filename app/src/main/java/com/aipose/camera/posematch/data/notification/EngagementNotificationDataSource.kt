package com.aipose.camera.posematch.data.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.os.ConfigurationCompat
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.data.local.PoseImageDataSource
import com.aipose.camera.posematch.domain.models.EngagementNotification
import com.aipose.camera.posematch.domain.models.NotificationTarget
import com.aipose.camera.posematch.util.bidiIsolate
import java.text.DateFormat
import java.text.DecimalFormatSymbols
import java.util.Date
import java.util.Locale

class EngagementNotificationDataSource(
    private val context: Context,
    private val imageDataSource: PoseImageDataSource,
) {

    suspend fun post(notification: EngagementNotification, languageCode: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return false

        val localized = localizedContext(languageCode)
        createChannels(manager, localized)
        val title = titleOf(notification, localized)
        val text = textOf(notification, localized)
        val openIntent = openIntent(notification.target)
        val builder = NotificationCompat.Builder(context, channelOf(notification))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openIntent)
            .addAction(0, localized.getString(R.string.notification_action_view), openIntent)
            .addAction(
                0,
                localized.getString(R.string.notification_action_not_interested),
                dismissIntent(),
            )

        val picture = pictureOf(notification)
        if (picture != null) {
            builder.setLargeIcon(picture)
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(picture)
                        .bigLargeIcon(null as Bitmap?)
                        .setSummaryText(text),
                )
        } else {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(text))
        }

        manager.notify(NOTIFICATION_ID, builder.build())
        return true
    }

    private fun titleOf(notification: EngagementNotification, localized: Context): String =
        when (notification) {
            is EngagementNotification.Memory -> memoryTitleOf(notification, localized)

            is EngagementNotification.StreakSaver -> localized.getString(
                R.string.notification_streak_title,
                localized.resources.getQuantityString(
                    R.plurals.achievements_streak_title,
                    notification.streakDays,
                    notification.streakDays,
                ),
            )

            is EngagementNotification.FirstPhoto ->
                localized.getString(R.string.notification_first_photo_title)

            is EngagementNotification.PoseIdea ->
                localized.getString(R.string.notification_pose_idea_title)
        }

    private fun memoryTitleOf(memory: EngagementNotification.Memory, localized: Context): String {
        val locale = ConfigurationCompat.getLocales(localized.resources.configuration)[0]
            ?: Locale.getDefault()
        val date = DateFormat.getDateInstance(DateFormat.LONG, locale)
            .format(Date(memory.capture.capturedAtMillis))
        val place = memory.capture.location.name?.trim().orEmpty()
        return if (place.isEmpty()) {
            localized.getString(R.string.notification_memory_title, date)
        } else {
            localized.getString(R.string.notification_memory_title_place, date, place.bidiIsolate())
        }
    }

    private fun textOf(notification: EngagementNotification, localized: Context): String =
        localized.getString(
            when (notification) {
                is EngagementNotification.Memory -> R.string.notification_memory_text
                is EngagementNotification.StreakSaver -> R.string.notification_streak_text
                is EngagementNotification.FirstPhoto -> R.string.notification_first_photo_text
                is EngagementNotification.PoseIdea -> R.string.notification_pose_idea_text
            },
        )

    private suspend fun pictureOf(notification: EngagementNotification): Bitmap? = when (notification) {
        is EngagementNotification.Memory ->
            imageDataSource.decode(notification.capture.imagePath, CAPTURE_SAMPLE_SIZE)

        is EngagementNotification.FirstPhoto ->
            notification.pose?.let { pose -> imageDataSource.decode(pose.imagePath) }

        is EngagementNotification.PoseIdea -> imageDataSource.decode(notification.pose.imagePath)
        is EngagementNotification.StreakSaver -> null
    }

    private fun channelOf(notification: EngagementNotification): String = when (notification) {
        is EngagementNotification.Memory -> MEMORIES_CHANNEL_ID
        else -> REMINDERS_CHANNEL_ID
    }

    private fun createChannels(manager: NotificationManagerCompat, localized: Context) {
        manager.createNotificationChannelsCompat(
            listOf(
                NotificationChannelCompat.Builder(
                    MEMORIES_CHANNEL_ID,
                    NotificationManagerCompat.IMPORTANCE_DEFAULT,
                ).setName(localized.getString(R.string.notification_channel_memories)).build(),
                NotificationChannelCompat.Builder(
                    REMINDERS_CHANNEL_ID,
                    NotificationManagerCompat.IMPORTANCE_DEFAULT,
                ).setName(localized.getString(R.string.notification_channel_reminders)).build(),
            ),
        )
    }

    private fun openIntent(target: NotificationTarget): PendingIntent {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent()
        val intent = launchIntent
            .setPackage(context.packageName)
            .addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP,
            )
            .putNotificationTarget(target)
        return PendingIntent.getActivity(
            context,
            OPEN_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun dismissIntent(): PendingIntent = PendingIntent.getBroadcast(
        context,
        DISMISS_REQUEST_CODE,
        Intent(context, NotificationDismissReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun localizedContext(languageCode: String): Context {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(appLocale(languageCode))
        return context.createConfigurationContext(configuration)
    }

    private fun appLocale(languageCode: String): Locale {
        val requested = Locale.forLanguageTag(languageCode)
        if (DecimalFormatSymbols.getInstance(requested).zeroDigit == LATIN_ZERO) return requested
        return Locale.Builder()
            .setLocale(requested)
            .setUnicodeLocaleKeyword(NUMBERING_SYSTEM_KEY, LATIN_DIGITS)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 4100
        private const val MEMORIES_CHANNEL_ID = "engagement_memories"
        private const val REMINDERS_CHANNEL_ID = "engagement_reminders"
        private const val OPEN_REQUEST_CODE = 4101
        private const val DISMISS_REQUEST_CODE = 4102
        private const val CAPTURE_SAMPLE_SIZE = 4
        private const val LATIN_ZERO = '0'
        private const val NUMBERING_SYSTEM_KEY = "nu"
        private const val LATIN_DIGITS = "latn"
    }
}
