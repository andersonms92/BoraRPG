package com.borarpg.tracking.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.service.notification.StatusBarNotification
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.borarpg.core.tracking.R
import com.borarpg.domain.model.combat.GuardianDefeat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GuardianVictoryNotifierTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.context
    private val manager = checkNotNull(context.getSystemService(NotificationManager::class.java))
    private var channelExisted = false

    @Before
    fun prepareNotifications() {
        channelExisted = manager.getNotificationChannel(CHANNEL_ID) != null
        manager.cancel(NOTIFICATION_ID)
        awaitNotificationCleared()
        if (
            Build.VERSION.SDK_INT >= 33 &&
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED
        ) {
            instrumentation.uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        }
        assertTrue(manager.areNotificationsEnabled())
    }

    @After
    fun clearNotifications() {
        manager.cancel(NOTIFICATION_ID)
        awaitNotificationCleared()
        if (!channelExisted) manager.deleteNotificationChannel(CHANNEL_ID)
    }

    @Test
    fun victoryPostsRewardsOnDedicatedHighImportanceChannelWithLauncherIntent() {
        GuardianVictoryNotifier(context).notify(GuardianDefeat(1, 2, 50, 25))

        val posted = awaitNotification("Guardião 2 derrotado!")
        val notification = posted.notification
        assertEquals(NOTIFICATION_ID, posted.id)
        assertEquals(CHANNEL_ID, notification.channelId)
        assertEquals(
            "+50 XP · 25 ouro recebidos.",
            notification.extras.getString(Notification.EXTRA_TEXT),
        )
        assertEquals(R.drawable.ic_guardian_victory, notification.smallIcon.resId)
        assertTrue(notification.flags and Notification.FLAG_AUTO_CANCEL != 0)
        assertFalse(notification.flags and Notification.FLAG_ONGOING_EVENT != 0)
        val channel = checkNotNull(manager.getNotificationChannel(CHANNEL_ID))
        assertEquals("Vitórias contra guardiões", channel.name.toString())
        assertEquals(NotificationManager.IMPORTANCE_HIGH, channel.importance)

        val launchIntent =
            checkNotNull(context.packageManager.getLaunchIntentForPackage(context.packageName))
        assertEquals(NotificationLaunchActivity::class.java.name, launchIntent.component?.className)
        val launcher =
            PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                launchIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )
        assertNotNull(launcher)
        assertEquals(launcher, notification.contentIntent)
        assertEquals(context.packageName, notification.contentIntent.creatorPackage)
        assertTrue(notification.contentIntent.isActivity)
    }

    @Test
    fun batchedVictoriesReplaceThePreviousAlertWithOneCombinedReward() {
        val notifier = GuardianVictoryNotifier(context)
        notifier.notify(GuardianDefeat(1, 1, 50, 25))
        notifier.notify(GuardianDefeat(3, 4, 150, 75))

        val posted = awaitNotification("Guardiões 2 a 4 derrotados!")
        assertEquals(1, manager.activeNotifications.count { it.id == NOTIFICATION_ID })
        assertEquals(
            "+150 XP · 75 ouro recebidos.",
            posted.notification.extras.getString(Notification.EXTRA_TEXT),
        )
    }

    @Test
    @SdkSuppress(minSdkVersion = 33)
    fun deniedPermissionDoesNotPostAnAlert() {
        val deniedContext =
            object : ContextWrapper(context) {
                override fun checkSelfPermission(permission: String): Int =
                    if (permission == Manifest.permission.POST_NOTIFICATIONS)
                        PackageManager.PERMISSION_DENIED
                    else super.checkSelfPermission(permission)
            }

        GuardianVictoryNotifier(deniedContext).notify(GuardianDefeat(1, 1, 50, 25))

        assertTrue(manager.activeNotifications.none { it.id == NOTIFICATION_ID })
    }

    @Test
    @SdkSuppress(minSdkVersion = 33)
    fun permissionSystemFailureDoesNotEscapeOrPostAnAlert() {
        val failingContext =
            object : ContextWrapper(context) {
                override fun checkSelfPermission(permission: String): Int {
                    throw SecurityException("Notification permission unavailable")
                }
            }

        GuardianVictoryNotifier(failingContext).notify(GuardianDefeat(1, 1, 50, 25))

        assertTrue(manager.activeNotifications.none { it.id == NOTIFICATION_ID })
    }

    private fun awaitNotificationCleared() {
        repeat(100) {
            if (manager.activeNotifications.none { it.id == NOTIFICATION_ID }) return
            SystemClock.sleep(50)
        }
        throw AssertionError("Previous victory notification was not cleared")
    }

    private fun awaitNotification(title: String): StatusBarNotification {
        repeat(100) {
            manager.activeNotifications
                .firstOrNull {
                    it.id == NOTIFICATION_ID &&
                        it.notification.extras.getString(Notification.EXTRA_TITLE) == title
                }
                ?.let {
                    return it
                }
            SystemClock.sleep(50)
        }
        throw AssertionError("Notification was not posted: $title")
    }

    private companion object {
        const val CHANNEL_ID = "guardian_victories"
        const val NOTIFICATION_ID = 201
    }
}
