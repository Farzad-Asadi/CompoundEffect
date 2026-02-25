package com.example.compoundeffectV1_01.data.modules

import android.content.Context
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.appSystemInfo.AppSystemInfoRepository
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.appSystemInfo.AppSystemInfoRepositoryImpl
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.category.CategoryRepository
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.category.CategoryRepositoryImpl
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.reminder.TaskReminderRepository
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.reminder.TaskReminderRepositoryImpl
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task.TaskRepository
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task.TaskRepositoryImpl
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.TaskScheduleRepository
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule.TaskScheduleRepositoryImpl
import com.example.compoundeffectV1_01.data.workManager.ReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds @Singleton
    abstract fun bindTaskRepository(
        impl: TaskRepositoryImpl
    ): TaskRepository

    @Binds @Singleton
    abstract fun bindTaskScheduleRepository(
        impl: TaskScheduleRepositoryImpl
    ): TaskScheduleRepository

    @Binds @Singleton
    abstract fun bindAppSystemInfoRepository(
        impl: AppSystemInfoRepositoryImpl
    ): AppSystemInfoRepository

    @Binds @Singleton
    abstract fun bindTaskReminderRepository (
        impl: TaskReminderRepositoryImpl
    ): TaskReminderRepository







}
