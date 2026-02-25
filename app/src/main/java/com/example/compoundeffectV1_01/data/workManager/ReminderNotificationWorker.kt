package com.example.compoundeffectV1_01.data.workManager

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.compoundeffectV1_01.R
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.category.CategoryRepository
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.reminder.TaskReminderEntity
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.reminder.TaskReminderRepository
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task.TaskRepository
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.TaskScheduleRepository
import com.example.compoundeffectV1_01.data.notification.ReminderNotifications
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val reminderRepo: TaskReminderRepository,
    private val scheduleRepo: TaskScheduleRepository,
    private val taskRepo: TaskRepository,
    private val categoryRepo: CategoryRepository,
    private val reminderScheduler: ReminderScheduler,
) : CoroutineWorker(appContext, params) {



    override suspend fun doWork(): Result {

        //reminderId دریافت
        val reminderId = inputData.getInt("reminderId", 0)  // (همانی که Scheduler گذاشته بود)
        if (reminderId == 0) return Result.failure()



        //خواندن داده‌ها از دیتابیس
        val reminder = reminderRepo.getById(reminderId) ?: return Result.success()
        val schedule = scheduleRepo.getById(reminder.scheduleId) ?: return Result.success()
        val task = taskRepo.getTaskById(schedule.taskId) ?: return Result.success()
        val category = task.categoryId?.let { categoryRepo.getCategoryById(it) }



        //مطمئن شدن از Channel
        ReminderNotifications.ensureChannel(applicationContext)


        //ساخت نوتیفیکیشن
        //انتخاب متن نمایشی (عنوان)
        val displayName =
            reminderTitleOrNull(reminder)
                ?: schedule.title?.takeIf { it.isNotBlank() }
                ?: task.name.takeIf { it.isNotBlank() }
                ?: category?.name?.takeIf { it.isNotBlank() }
                ?: "Task"


        val notifId = reminderId // ساده: هر reminder یک notifId ثابت


        //ایکون
        val smallIconRes = R.drawable.ic_notification

        val builder = NotificationCompat.Builder(applicationContext, ReminderNotifications.CHANNEL_ID)
            .setSmallIcon(smallIconRes)
            .setContentTitle("یادآوری")
            .setContentText(displayName)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)




        // اگر vibrate خاموش است، pattern خالی می‌دهد.
        if (!reminder.vibrate) {
            builder.setVibrate(LongArray(0))
        }



        //اگر permission نیست: ارسال نکن
        if (!hasPostNotificationsPermission(applicationContext)) {
            Log.w("WM", "POST_NOTIFICATIONS not granted; skipping notify")
            return Result.success()
        }



        //ارسال نوتیف
        try {
            NotificationManagerCompat.from(applicationContext).notify(notifId, builder.build())

        } catch (se: SecurityException) {
            Log.w("WM", "notify SecurityException", se)
        }




        // بعد از ارسال: نوبت بعدی را زمان‌بندی کن
        try {
            reminderScheduler.reschedule(reminderId)
        } catch (t: Throwable) {
            Log.w("WM", "reschedule after notify failed", t)
        }




        Log.d("WM", "notify sent notifId=$notifId")
        Log.d("WM", "doWork start reminderId=$reminderId")

        return Result.success()
    }


}




//چک permission
private fun hasPostNotificationsPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}


//برای ایجاد عنوان در نوتیفیکیشن
private fun reminderTitleOrNull(e: TaskReminderEntity): String? {
    // اگر بعداً ستون title برای Reminder اضافه کردی اینجا استفاده کن.
    // فعلاً نداریم:
    return null
}