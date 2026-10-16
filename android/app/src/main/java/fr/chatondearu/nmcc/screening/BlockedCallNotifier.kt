package fr.chatondearu.nmcc.screening

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

private const val CHANNEL_ID = "blocked_calls"

/** One local notification after a silent reject. No-op when the pref is off (caller decides). */
fun postBlockedCallNotification(context: Context, number: String, pattern: String?) {
    val nm = context.getSystemService(NotificationManager::class.java) ?: return
    if (Build.VERSION.SDK_INT >= 33) {
        val granted = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!granted) return
    }
    if (nm.getNotificationChannel(CHANNEL_ID) == null) {
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Appels bloqués", NotificationManager.IMPORTANCE_DEFAULT),
        )
    }
    val detail = if (pattern.isNullOrBlank()) number else "$number · $pattern"
    val notification = Notification.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle("Appel bloqué")
        .setContentText(detail)
        .setAutoCancel(true)
        .build()
    nm.notify(number.hashCode(), notification)
}
