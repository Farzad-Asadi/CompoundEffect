package com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule

import com.example.compoundeffectV1_01.ui.scheduleScreen.ScheduleItemsRow
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class TaskScheduleRepositoryImpl @Inject constructor(
    private val dao: TaskScheduleDao
) : TaskScheduleRepository {

    override fun observeByTaskId(taskId: Int): Flow<List<TaskSchedule>> = dao.observeByTaskId(taskId)
    override suspend fun getByTaskId(taskId: Int): TaskSchedule? = dao.getByTaskId(taskId)
    override suspend fun upsert(schedule: TaskSchedule) = dao.upsert(schedule)
    override suspend fun delete(schedule: TaskSchedule) = dao.delete(schedule)

    override suspend fun deleteByTaskId(taskId: Int) = dao.deleteByTaskId(taskId)
    override suspend fun deleteAllForTask(taskId: Int) = dao.deleteAllForTask(taskId)


    override suspend fun insert(schedule: TaskSchedule): Int =
        dao.insert(schedule).toInt()

    override suspend fun updateTimeRange(scheduleId: Int, date: LocalDate, startMin: Int, endMin: Int) {
        dao.updateTimeRange(
            scheduleId = scheduleId,
            dateEpochDay = date.toEpochDay(),
            startMin = startMin,
            endMin = endMin
        )
    }

    override suspend fun updateEndMinute(scheduleId: Int, endMin: Int) {
        dao.updateEndMinute(scheduleId, endMin)
    }

    override suspend fun updateStartMinute(scheduleId: Int, startMin: Int) {
        dao.updateStartMinute(scheduleId, startMin)
    }

    override fun observeAllSchedulesWithTask(): Flow<List<ScheduleItemsRow>> =
        dao.observeAllSchedulesWithTask()


    override suspend fun deleteById(scheduleId: Int) =
        dao.deleteById(scheduleId)

    override suspend fun countByTaskId(taskId: Int): Int =
            dao.countByTaskId(taskId)

    override suspend fun setSchedulePalletState(scheduleId: Int, inPallet: Boolean) =
        dao.setSchedulePalletState(scheduleId, inPallet)

    override suspend fun getLastInactiveTimeRange(taskId: Int, mode: ScheduleMode): TaskSchedule? =
        dao.getLastInactiveTimeRange(taskId, mode)

    override suspend fun dropFromPalletToTimeline(
        scheduleId: Int,
        date: LocalDate,
        startMin: Int,
        endMin: Int,
        mode: ScheduleMode
    ) =
        dao.dropFromPalletToTimeline(
            scheduleId = scheduleId,
            dateEpochDay = date.toEpochDay(), // ✅ تبدیل
            startMin = startMin,
            endMin = endMin
        )


}
