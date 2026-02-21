package com.example.compoundeffectV1_01.data.dataBaseRoom.tables.taskSchedule

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.compoundeffectV1_01.ui.scheduleScreen.ScheduleItemsRow
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(schedule: TaskSchedule)


    @Query("DELETE FROM task_schedule WHERE taskId = :taskId")
    suspend fun deleteByTaskId(taskId: Int)

    @Query("SELECT * FROM task_schedule WHERE taskId = :taskId ORDER BY dateEpochDay ASC, startMinuteOfDay ASC, id ASC")
    fun observeByTaskId(taskId: Int): Flow<List<TaskSchedule>>

    @Query("SELECT * FROM task_schedule WHERE taskId = :taskId LIMIT 1")
    suspend fun getByTaskId(taskId: Int): TaskSchedule?



    @Delete
    suspend fun delete(schedule: TaskSchedule)

    @Query("DELETE FROM task_schedule WHERE taskId = :taskId")
    suspend fun deleteAllForTask(taskId: Int)





    @Insert
    suspend fun insert(schedule: TaskSchedule): Long  // ✅ id جدید را برمی‌گرداند

    @Update
    suspend fun update(schedule: TaskSchedule)

    @Query("""
        UPDATE task_schedule
        SET dateEpochDay = :dateEpochDay,
            startMinuteOfDay = :startMin,
            endMinuteOfDay = :endMin,
            mode = :mode
        WHERE id = :scheduleId
    """)
    suspend fun updateTimeRange(
        scheduleId: Int,
        mode: ScheduleMode = ScheduleMode.TIME_RANGE,
        dateEpochDay: Long,
        startMin: Int,
        endMin: Int
    ): Int  // rows affected

    @Query("""
        UPDATE task_schedule
        SET endMinuteOfDay = :endMin
        WHERE id = :scheduleId
    """)
    suspend fun updateEndMinute(scheduleId: Int, endMin: Int): Int

    @Query("""
        UPDATE task_schedule
        SET startMinuteOfDay = :startMin
        WHERE id = :scheduleId
    """)
    suspend fun updateStartMinute(scheduleId: Int, startMin: Int): Int

    @Query("SELECT * FROM task_schedule WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): TaskSchedule?

    // ✅ برای تایم‌لاین: join schedule + task
    @Query("""
SELECT 
  s.id             AS s_id,
  s.taskId         AS s_taskId,
  s.inPallet      AS s_inPallet,
  s.title          AS s_title,
  s.mode           AS s_mode,
  s.dateEpochDay   AS s_dateEpochDay,
  s.startMinuteOfDay AS s_startMinuteOfDay,
  s.endMinuteOfDay AS s_endMinuteOfDay,
  s.durationMinutes AS s_durationMinutes,
  s.repeating      AS s_repeating,

  t.id             AS t_id,
  t.name           AS t_name,
  t.color          AS t_color,
  t.description    AS t_description,
  t.categoryId     AS t_categoryId,
  t.isCompleted    AS t_isCompleted,
  t.priority       AS t_priority,
  t.selected       AS t_selected,
  t.changed        AS t_changed,

  c.name           AS c_name,
  c.iconName       AS c_iconName,
  c.color          AS c_color

FROM task_schedule s
JOIN task t ON t.id = s.taskId
LEFT JOIN category c ON c.categoryId = t.categoryId
""")
    fun observeAllSchedulesWithTask(): Flow<List<ScheduleItemsRow>>



    @Query("DELETE FROM task_schedule WHERE id = :scheduleId")
    suspend fun deleteById(scheduleId: Int)

    @Query("SELECT COUNT(*) FROM task_schedule WHERE taskId = :taskId")
    suspend fun countByTaskId(taskId: Int): Int



    @Query("UPDATE task_schedule SET inPallet = :inPallet WHERE id = :scheduleId")
    suspend fun setSchedulePalletState(scheduleId: Int, inPallet: Boolean)

    @Query("SELECT * FROM task_schedule WHERE taskId=:taskId AND mode=:mode AND inPallet=1 ORDER BY id DESC LIMIT 1")
    suspend fun getLastInactiveTimeRange(taskId: Int, mode: ScheduleMode = ScheduleMode.TIME_RANGE): TaskSchedule?

    @Query("""
        UPDATE task_schedule
        SET 
            inPallet = 0,
            dateEpochDay = :dateEpochDay,
            startMinuteOfDay = :startMin,
            endMinuteOfDay = :endMin,
            mode = :mode
        WHERE id = :scheduleId
    """)
    suspend fun dropFromPalletToTimeline(
        scheduleId: Int,
        dateEpochDay: Long,
        startMin: Int,
        endMin: Int,
        mode: ScheduleMode = ScheduleMode.TIME_RANGE
    )


}
