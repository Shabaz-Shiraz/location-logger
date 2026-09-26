package com.example.locationlogger.service

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.locationlogger.MainActivity
import com.example.locationlogger.R
import com.example.locationlogger.domain.CAPTURE_INTERVAL
import com.example.locationlogger.domain.model.LocationEntry
import com.example.locationlogger.ui.DisplayOptions
import com.example.locationlogger.ui.format.formatCoordinates
import com.example.locationlogger.ui.format.formatTime
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.time.toJavaDuration

data class TrackingNotificationState(
    val lastEntry: LocationEntry? = null,
    val locationEnabled: Boolean = true
)

class TrackingNotificationFactory @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val notificationManager = NotificationManagerCompat.from(context)

    fun createChannel() {
        val channel = NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
            .setName(context.getString(R.string.notification_channel_name))
            .setDescription(context.getString(R.string.notification_channel_description))
            .setShowBadge(false)
            .build()
        notificationManager.createNotificationChannel(channel)
    }

    fun build(state: TrackingNotificationState): Notification {
        val text = contentText(state)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(openAppIntent())
            .addAction(0, context.getString(R.string.notification_action_pause), pauseIntent())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    fun post(state: TrackingNotificationState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return
        notificationManager.notify(NOTIFICATION_ID, build(state))
    }

    private fun contentText(state: TrackingNotificationState): String {
        if (!state.locationEnabled) return context.getString(R.string.notification_location_off)
        val entry = state.lastEntry ?: return context.getString(R.string.notification_first_fix)
        val place = entry.address ?: entry.point.formatCoordinates()
        if (!DisplayOptions.SHOW_NEXT_CAPTURE) return context.getString(R.string.notification_last, place)
        val next = formatTime(context, entry.timestamp.plus(CAPTURE_INTERVAL.toJavaDuration()))
        return context.getString(R.string.notification_last_and_next, place, next)
    }

    private fun openAppIntent(): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun pauseIntent(): PendingIntent = PendingIntent.getService(
        context,
        0,
        LocationTrackingService.pauseIntent(context),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    companion object {
        const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "location_tracking"
    }
}
