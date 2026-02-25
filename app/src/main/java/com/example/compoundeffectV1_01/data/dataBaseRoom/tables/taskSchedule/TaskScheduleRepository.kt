package com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule

import com.example.compoundeffectV1_01.ui.scheduleScreen.ScheduleItemsRow
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TaskScheduleRepository {
    fun observeByTaskId(taskId: Int): Flow<List<TaskSchedule>>
    suspend fun getByTaskId(taskId: Int): TaskSchedule?
    suspend fun upsert(schedule: TaskSchedule)
    suspend fun upsertAndReturnId(schedule: TaskSchedule):Int
    suspend fun delete(schedule: TaskSchedule)
    suspend fun deleteByTaskId(taskId: Int)
    suspend fun deleteAllForTask(taskId: Int)

    suspend fun insert(schedule: TaskSchedule): Int
    suspend fun updateTimeRange(scheduleId: Int, date: LocalDate, startMin: Int, endMin: Int)
    suspend fun updateEndMinute(scheduleId: Int, endMin: Int)
    suspend fun updateStartMinute(scheduleId: Int, startMin: Int)
    fun observeAllSchedulesWithTask(): Flow<List<ScheduleItemsRow>>

    suspend fun deleteById(scheduleId: Int)
    suspend fun countByTaskId(taskId: Int): Int
    suspend fun getById(id: Int): TaskSchedule?

    suspend fun setSchedulePalletState(scheduleId: Int, inPallet: Boolean)
    suspend fun getLastInactiveTimeRange(taskId: Int, mode: ScheduleMode = ScheduleMode.TIME_RANGE): TaskSchedule?

    suspend fun dropFromPalletToTimeline(
        scheduleId: Int,
        date: LocalDate,
        startMin: Int,
        endMin: Int,
        mode: ScheduleMode = ScheduleMode.TIME_RANGE
    )


}
