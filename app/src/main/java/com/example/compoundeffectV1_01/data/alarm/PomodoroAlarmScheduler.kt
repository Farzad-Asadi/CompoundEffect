package com.example.compoundeffectV1_01.data.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.compoundeffectV1_01.domain.pomodoro.scheduler.PomodoroScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PomodoroAlarmScheduler @Inject constructor(
    @ApplicationContext
    private val context: Context
) : PomodoroScheduler {

    private val activeRequestCodes = mutableSetOf<Int>()

    private fun pomodoroIntent(): Intent {
        return Intent(context, PomodoroAlarmReceiver::class.java).apply {
            action = "com.example.compoundeffect.POMODORO_ALARM"
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    override fun schedule(type: String, triggerAtMillis: Long) {

        val alarmManager = context.getSystemService(AlarmManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e("ALARM_DEBUG", "❌ EXACT ALARM NOT GRANTED")
                return
            }
        }

        Log.e("ALARM_DEBUG", "🔥 schedule called type=$type")



        val intent = pomodoroIntent()

        val requestCode = (triggerAtMillis % Int.MAX_VALUE).toInt()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        activeRequestCodes += requestCode

        Log.e("ALARM_DEBUG", "⏰ set alarm at=$triggerAtMillis")

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    override fun cancelAll() {
        val alarmManager = context.getSystemService(AlarmManager::class.java)

        activeRequestCodes.forEach { requestCode ->
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                pomodoroIntent(),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )

            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }

        Log.e("ALARM_DEBUG", "🛑 cancelAll pomodoro alarms count=${activeRequestCodes.size}")

        activeRequestCodes.clear()
    }




}