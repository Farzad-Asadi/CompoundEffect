package com.example.compoundeffectV1_01.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object ReminderNotifications {
    const val CHANNEL_ID = "reminders"
    const val CHANNEL_NAME = "Reminders"
    const val CHANNEL_DESC = "Task reminders notifications"

    fun ensureChannel(context: Context) {

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = nm.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESC
            enableVibration(true) // پیش‌فرض؛ بعداً از reminder.vibrate کنترلش می‌کنیم
        }

        nm.createNotificationChannel(channel)
    }
}
