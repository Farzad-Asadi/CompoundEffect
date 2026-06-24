package com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
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
    """)
    fun observeTasksByCategory(categoryId: Int): Flow<List<Task>>


    @Query("""
    SELECT * FROM task
    WHERE categoryId = :categoryId
""")
    suspend fun getTasksByCategory(categoryId: Int): List<Task>


    @Query("UPDATE task SET siblingIndex = :siblingIndex WHERE id = :id")
    suspend fun updateSiblingIndex(id: Int, siblingIndex: Int)

    @Query("UPDATE task SET parentTaskId = :parentTaskId WHERE id = :id")
    suspend fun updateTaskParent(id: Int, parentTaskId: Int?)









    @Transaction
    @Query("""
    SELECT * FROM task
    WHERE categoryId = :categoryId
    """)
    fun observeTasksWithScheduleByCategory(categoryId: Int): Flow<List<TaskWithSchedule>>


    @Query("""
    SELECT * FROM task
    WHERE categoryId = :categoryId
    """)
    suspend fun getTasksByCategoryOrdered(categoryId: Int): List<Task>


    @Query("SELECT COUNT(*) FROM task WHERE parentTaskId = :taskId")
    suspend fun countChildren(taskId: Int): Int



    @Query("UPDATE Task SET isCompleted = :done WHERE id IN (:ids)")
    suspend fun setCompletedForIds(ids: List<Int>, done: Boolean)

    @Query("""
    SELECT * FROM task
    WHERE categoryId = :categoryId 
      AND parentTaskId = :parentId
    ORDER BY siblingIndex ASC, id ASC
    """)
    suspend fun getSiblings(categoryId: Int, parentId: Int): List<Task>


    @Query("""
    UPDATE task
    SET siblingIndex = siblingIndex + 1
    WHERE categoryId = :categoryId
      AND parentTaskId = :parentId
    """)
    suspend fun shiftSiblingsDown(categoryId: Int, parentId: Int)






    @Query("UPDATE task SET parentTaskId = -1 WHERE parentTaskId IS NULL")
    suspend fun normalizeNullParentsToRoot()


    @Query("UPDATE task SET isCompleted = 1 WHERE categoryId = :categoryId")
    suspend fun completeAllInCategory(categoryId: Int)

    @Query("UPDATE task SET isCompleted = 0 WHERE categoryId = :categoryId")
    suspend fun uncompleteAllInCategory(categoryId: Int)

    @Query("DELETE FROM task WHERE categoryId = :categoryId AND isCompleted = 1")
    suspend fun deleteCompletedInCategory(categoryId: Int)

    @Query("DELETE FROM task WHERE categoryId = :categoryId")
    suspend fun deleteAllInCategory(categoryId: Int)


    @Query("""
    UPDATE task
    SET pomodoroDoneUnits = pomodoroDoneUnits + 1
    WHERE id = :taskId
""")
    suspend fun incrementPomodoroDoneUnits(taskId: Int)
}
