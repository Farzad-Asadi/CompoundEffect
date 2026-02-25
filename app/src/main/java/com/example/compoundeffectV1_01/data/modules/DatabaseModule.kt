package com.example.compoundeffectV1_01.data.modules

import android.content.Context
import com.example.compoundeffectV1_01.data.dataBaseRoom.appDataBase.AppDatabase
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.appSystemInfo.SystemDao
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.category.CategoryDao
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.reminder.TaskReminderDao
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.reminder.TaskReminderRepository
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task.TaskDao
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.TaskScheduleDao
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.TaskScheduleRepository
import com.example.compoundeffectV1_01.data.workManager.ReminderScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getDatabase(context)






    @Provides fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideEventDao(db: AppDatabase): TaskDao = db.taskDao()
    @Provides fun provideTaskScheduleDao(db: AppDatabase): TaskScheduleDao = db.taskScheduleDao()
    @Provides fun provideTaskReminderDao(db: AppDatabase): TaskReminderDao = db.taskReminderDao()
    @Provides fun provideSystemDao(db: AppDatabase): SystemDao = db.systemDao()




    //Others

    @Provides
    @Singleton
    fun provideReminderScheduler(
        @ApplicationContext context: Context,
        reminderRepo: TaskReminderRepository,
        scheduleRepo: TaskScheduleRepository,
    ): ReminderScheduler = ReminderScheduler(context, reminderRepo, scheduleRepo)


}
