package com.borarpg.tracking.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.borarpg.core.tracking.R
import com.borarpg.domain.model.combat.GuardianDefeat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GuardianVictoryNotifier
@Inject
constructor(@param:ApplicationContext private val context: Context) {
    fun notify(defeat: GuardianDefeat) {
        if (defeat.victories <= 0 || defeat.totalVictories < defeat.victories) return
        try {
            if (
                Build.VERSION.SDK_INT >= 33 &&
                    context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                        PackageManager.PERMISSION_GRANTED
            )
                return

            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            if (!manager.areNotificationsEnabled()) return
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Vitórias contra guardiões",
                    NotificationManager.IMPORTANCE_HIGH,
                )
            )
            val launchIntent =
                context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
            val openApp =
                PendingIntent.getActivity(
                    context,
                    NOTIFICATION_ID,
                    launchIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
            val title =
                if (defeat.victories == 1) {
                    "Guardião ${defeat.totalVictories} derrotado!"
                } else {
                    val first = defeat.totalVictories - defeat.victories + 1
                    "Guardiões $first a ${defeat.totalVictories} derrotados!"
                }
            val rewards = "+${defeat.bonusXp} XP · ${defeat.gold} ouro recebidos."
            manager.notify(
                NOTIFICATION_ID,
                Notification.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_guardian_victory)
                    .setContentTitle(title)
                    .setContentText(rewards)
                    .setStyle(Notification.BigTextStyle().bigText(rewards))
                    .setContentIntent(openApp)
                    .setCategory(Notification.CATEGORY_STATUS)
                    .setAutoCancel(true)
                    .build(),
            )
        } catch (_: RuntimeException) {
            return
        }
    }

    private companion object {
        const val CHANNEL_ID = "guardian_victories"
        const val NOTIFICATION_ID = 201
    }
}
