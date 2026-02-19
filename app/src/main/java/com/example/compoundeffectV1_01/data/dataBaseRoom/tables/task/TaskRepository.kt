package com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.compoundeffectV1_01.ui.categoryScreen.TaskReorderUpdate
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime


interface TaskRepository {


    suspend fun insertTask(vararg task: Task)

    suspend fun insertTaskAndReturnId(task: Task): Int

    suspend fun updateTask(task: Task)

    suspend fun deleteTask(task: Task)

    fun observeTasksByCategory(categoryId: Int): Flow<List<Task>>

    suspend fun getTaskById(id: Int): Task?

    fun observeTasksWithScheduleByCategory(categoryId: Int): kotlinx.coroutines.flow.Flow<List<TaskWithSchedule>>

    fun observeScheduledCountByCategory(categoryId: Int): kotlinx.coroutines.flow.Flow<Int>

    fun observeAllScheduledTasksWithSchedule(): Flow<List<TaskWithSchedule>>

    suspend fun getTasksByCategoryOrdered(categoryId: Int): List<Task>

    suspend fun getMinOrderIndex(categoryId: Int): Int?

    suspend fun getMaxOrderIndex(categoryId: Int): Int?

    suspend fun countChildren(taskId: Int): Int

    suspend fun updateTaskOrder(id: Int, orderIndex: Int)

    suspend fun updateTaskHierarchy(id: Int, indentLevel: Int, parentTaskId: Int?)

    suspend fun applyTaskReorderAndHierarchy(updates: List<TaskReorderUpdate>)

    suspend fun setCompletedForIds(ids: List<Int>, done: Boolean)

}