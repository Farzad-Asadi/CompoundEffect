package com.example.compoundeffectV1_01.data.workManager

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.reminder.BeforeAfter
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.reminder.ReminderMode
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.reminder.StartEnd
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.reminder.TaskReminderEntity
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.reminder.TaskReminderRepository
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.RepeatUnit
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.ScheduleMode
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.TaskSchedule
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.TaskScheduleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject


//سازنده و گرفتن WorkManager
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val reminderRepo: TaskReminderRepository,
    private val scheduleRepo: TaskScheduleRepository,
) {


    //ساخت ورک منیجر
    private val wm by lazy { WorkManager.getInstance(appContext) }



    //ایجاد زمانبندی
    suspend fun reschedule(reminderId: Int) {

        //خواندن از دیتابیس
        val reminder = reminderRepo.getById(reminderId) ?: return
        val schedule = scheduleRepo.getById(reminder.scheduleId) ?: return



        // زمان اجرای نهایی را حساب کن
        // زمان الان
        val after = System.currentTimeMillis() + 1000L // ✅ 1 ثانیه جلوتر تا تکرار همون لحظه نشه

        //تریگر به میلی ثانیه
        val triggerAtMillis = computeNextTriggerAtMillis(reminder, schedule, after) ?: run {
            cancel(reminderId)
            return
        }

        //اختلاف زمان اجرا با «الان»
        val delay = (triggerAtMillis - System.currentTimeMillis()).coerceAtLeast(0L) // نمیگذارد منفی باشد



        //درخواست Work را می‌سازد
        val req = OneTimeWorkRequestBuilder<ReminderNotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("reminderId" to reminderId))   //ارسال reminderId به doWork
            .addTag("reminders")
            .addTag(workName(reminderId))
            .build()



        //در صف قرار دادن درخواست کار
        wm.enqueueUniqueWork(
            workName(reminderId),
            ExistingWorkPolicy.REPLACE,  // Replace: اگر قبلاً work داشته، جایگزین کن
            req
        )



        //Log
        val name = workName(reminderId) // همون "reminder_$reminderId"
        withContext(Dispatchers.IO) {
            val infos = wm.getWorkInfosForUniqueWork(name).get()
            Log.d("WM", "after enqueue name=$name infos=${infos.map { it.state to it.id }} delayMs=$delay")
        }

    }



    //کنسل کردن کار در صف کار
    fun cancel(reminderId: Int) {
        wm.cancelUniqueWork(workName(reminderId))
    }

}


//نام گذاری
private fun workName(reminderId: Int) = "reminder_$reminderId"



//محاسبه زمان تریگر
private fun computeNextTriggerAtMillis(
    reminder: TaskReminderEntity,
    schedule: TaskSchedule,
    afterMillis: Long   //از این زمان به بعد دنبال “نوبت بعدی” می‌گردیم.
): Long? {

    //TIME_RANGE
    if (schedule.mode == ScheduleMode.TIME_RANGE) {

        //گرفتن بازه‌ی روزانه schedule
        val startMin = schedule.startMinuteOfDay ?: return null
        val endMin = schedule.endMinuteOfDay ?: return null

        // تاریخ شروع/اولین تاریخ
        val baseDate = schedule.dateEpochDay?.let(LocalDate::ofEpochDay) ?: return null





        //توابع کمکی
        //دقیقه‌ی روز را به ساعت/دقیقه تبدیل می‌کند
        fun minuteToTime(m: Int) = LocalTime.of(m / 60, m % 60)

        //LocalDateTime را با timezone دستگاه به Epoch millis تبدیل می‌کند.
        fun toMillis(dt: LocalDateTime): Long =
            dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // کمک: پیدا کردن تاریخِ اجرای بعدی (برای repeat)
        //ورودی: یک تاریخ (from)
        //خروجی: اولین تاریخی که schedule اجازه می‌دهد
        fun nextScheduleDateOnOrAfter(from: LocalDate): LocalDate? {

            //اگر schedule تکرار ندارد
            if (!schedule.repeating) {
                return if (!baseDate.isBefore(from)) baseDate else null
            }

            //استخراج قوانین تکرار
            val interval = (schedule.repeatInterval ?: 1).coerceIn(1, 99)
            val unit = schedule.repeatUnit ?: RepeatUnit.DAY



            return when (unit) {
                RepeatUnit.DAY -> {
                    var d = baseDate
                    if (d.isBefore(from)) {
                        val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(d, from)
                        val steps = ((daysBetween + interval - 1) / interval) // ceil
                        d = d.plusDays(steps * interval.toLong())
                    }
                    d
                }

                RepeatUnit.WEEK -> {
                    val mask = (schedule.weekdaysMask ?: 0).coerceIn(0, 127)
                    if (mask == 0) return null

                    fun bitIndex(dow: DayOfWeek): Int = when (dow) {
                        DayOfWeek.SATURDAY -> 0
                        DayOfWeek.SUNDAY -> 1
                        DayOfWeek.MONDAY -> 2
                        DayOfWeek.TUESDAY -> 3
                        DayOfWeek.WEDNESDAY -> 4
                        DayOfWeek.THURSDAY -> 5
                        DayOfWeek.FRIDAY -> 6
                    }

                    fun isAllowed(date: LocalDate): Boolean {
                        val bit = 1 shl bitIndex(date.dayOfWeek)
                        return (mask and bit) != 0
                    }

                    // ✅ interval هفته‌ای را هم رعایت کن:
                    // هفته‌ها را نسبت به baseDate می‌سنجیم (بر اساس دوشنبه یا هرچی نمی‌خوایم؛ ساده: weeksBetween روی خود تاریخ)
                    fun isInAllowedWeek(date: LocalDate): Boolean {
                        val weeks = java.time.temporal.ChronoUnit.WEEKS.between(baseDate, date)
                        return weeks >= 0 && (weeks % interval == 0L)
                    }

                    // ساده و مطمئن: تا 366 روز جلو می‌گردیم
                    var d = if (baseDate.isAfter(from)) baseDate else from
                    repeat(366) {
                        if (!d.isBefore(baseDate) && isAllowed(d) && isInAllowedWeek(d)) return d
                        d = d.plusDays(1)
                    }
                    null
                }

                else -> {
                    // فعلاً سایر واحدها را مثل DAY
                    var d = baseDate
                    if (d.isBefore(from)) d = from
                    d
                }
            }
        }









        // تبدیل afterMillis به تاریخ/ساعت محلی
        val afterLocal = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(afterMillis),
            ZoneId.systemDefault()
        )






        // ---- حالت‌ها ----

        // helper برای محاسبه‌ی زمان بر اساس یک تاریخ مشخص
        fun fireDateTimeForDate(date: LocalDate): LocalDateTime? {
            val baseMinute = when (reminder.anchor) {
                StartEnd.START -> startMin
                StartEnd.END -> endMin
            }

            val base = date.atTime(minuteToTime(baseMinute))

            return when (reminder.mode) {
                ReminderMode.ALLOCATED -> {
                    // ✅ اگر کاربر چیزی وارد نکنه => offsetها صفر => دقیقاً خود base
                    val offsetTotalMinutes =
                        reminder.offsetDays * 24 * 60 + reminder.offsetHours * 60 + reminder.offsetMinutes

                    if (reminder.beforeAfter == BeforeAfter.BEFORE)
                        base.minusMinutes(offsetTotalMinutes.toLong())
                    else
                        base.plusMinutes(offsetTotalMinutes.toLong())
                }

                ReminderMode.FIXED_TIME -> {
                    val fixed = reminder.fixedMinuteOfDay ?: return null
                    date.atTime(minuteToTime(fixed))
                }

                ReminderMode.INTERVALLIC -> {
                    // اینجا برای intervallic، زمان “بعدی” را پایین‌تر محاسبه می‌کنیم
                    null
                }
            }
        }

        // 1) INTERVALLIC: تکرار داخل بازه
        if (reminder.mode == ReminderMode.INTERVALLIC) {
            val rStart = reminder.intervalStartMinuteOfDay ?: return null
            val rEnd = reminder.intervalEndMinuteOfDay ?: return null
            val every = (reminder.everyMinutesTotal ?: 1).coerceAtLeast(1)

            // ✅ clamp به بازه‌ی schedule
            val startTickMin = maxOf(rStart, startMin)
            val endTickMin = minOf(rEnd, endMin)

            // اگر بعد از clamp بازه نامعتبر شد، یعنی کاربر بازه‌ی ریمایندر را بیرون از schedule زده
            if (endTickMin < startTickMin) return null

            var date = nextScheduleDateOnOrAfter(afterLocal.toLocalDate()) ?: return null

            while (true) {
                val startDT = date.atTime(minuteToTime(startTickMin))
                val endDT = date.atTime(minuteToTime(endTickMin))

                // اگر بازه‌ی روزانه غلط شد (نباید با clamp رخ بده، ولی برای امنیت)
                if (endDT.isBefore(startDT)) {
                    date = nextScheduleDateOnOrAfter(date.plusDays(1)) ?: return null
                    continue
                }

                val nextDT = when {
                    afterLocal.isBefore(startDT) -> startDT
                    afterLocal.isAfter(endDT) -> null
                    else -> {
                        val minutesFromStart =
                            java.time.Duration.between(startDT, afterLocal).toMinutes()
                                .coerceAtLeast(0)
                        val k = (minutesFromStart + every - 1) / every // ceil
                        startDT.plusMinutes(k * every)
                    }
                }

                if (nextDT != null && !nextDT.isAfter(endDT)) return toMillis(nextDT)

                // برو روز بعدیِ schedule (repeat)
                date = nextScheduleDateOnOrAfter(date.plusDays(1)) ?: return null
            }
        }


        // 2) ALLOCATED / FIXED_TIME
        var date = nextScheduleDateOnOrAfter(afterLocal.toLocalDate()) ?: return null
        repeat(366) {
            val dt = fireDateTimeForDate(date)
            if (dt != null && toMillis(dt) > afterMillis) return toMillis(dt)

            // اگر امروز نشد، برو تاریخ بعدیِ schedule
            date = nextScheduleDateOnOrAfter(date.plusDays(1)) ?: return null
        }

    }



    return null
}
