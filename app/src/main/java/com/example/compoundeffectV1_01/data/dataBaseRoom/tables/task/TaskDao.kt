package com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.compoundeffectV1_01.ui.categoryScreen.TaskReorderUpdate
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTask(vararg task: Task)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTaskAndReturnId(task: Task): Long


    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)


    @Query("SELECT * FROM task WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Int): Task?



    @Query("""
    SELECT COUNT(*) FROM task_schedule s
    INNER JOIN task t ON t.id = s.taskId
    WHERE t.categoryId = :categoryId
    """)
    fun observeScheduledCountByCategory(categoryId: Int): Flow<Int>

    @Transaction
    @Query("""
    SELECT * FROM task
    INNER JOIN task_schedule s ON s.taskId = task.id
    WHERE s.mode = 'TIME_RANGE'
    """)
    fun observeAllScheduledTasksWithSchedule(): Flow<List<TaskWithSchedule>>

    @Query("""
    SELECT * FROM task 
    WHERE categoryId = :categoryId
    ORDER BY orderIndex ASC, id ASC
""")
    fun observeTasksByCategory(categoryId: Int): Flow<List<Task>>

    @Transaction
    @Query("""
    SELECT * FROM task
    WHERE categoryId = :categoryId
    ORDER BY orderIndex ASC, id ASC
""")
    fun observeTasksWithScheduleByCategory(categoryId: Int): Flow<List<TaskWithSchedule>>


    @Query("""
    SELECT * FROM task
    WHERE categoryId = :categoryId
    ORDER BY orderIndex ASC, id ASC
""")
    suspend fun getTasksByCategoryOrdered(categoryId: Int): List<Task>

    @Query("SELECT MIN(orderIndex) FROM task WHERE categoryId = :categoryId")
    suspend fun getMinOrderIndex(categoryId: Int): Int?

    @Query("SELECT MAX(orderIndex) FROM task WHERE categoryId = :categoryId")
    suspend fun getMaxOrderIndex(categoryId: Int): Int?

    @Query("SELECT COUNT(*) FROM task WHERE parentTaskId = :taskId")
    suspend fun countChildren(taskId: Int): Int


    @Query("UPDATE task SET orderIndex = :orderIndex WHERE id = :id")
    suspend fun updateTaskOrder(id: Int, orderIndex: Int)

    @Query("UPDATE task SET indentLevel = :indentLevel, parentTaskId = :parentTaskId WHERE id = :id")
    suspend fun updateTaskHierarchy(id: Int, indentLevel: Int, parentTaskId: Int?)


    @Transaction
    suspend fun applyTaskReorderAndHierarchy(updates: List<TaskReorderUpdate>) {
        for (u in updates) {
            updateTaskOrder(u.id, u.orderIndex)
            updateTaskHierarchy(u.id, u.indentLevel, u.parentTaskId)
        }
    }

    @Query("UPDATE Task SET isCompleted = :done WHERE id IN (:ids)")
    suspend fun setCompletedForIds(ids: List<Int>, done: Boolean)


}
