package com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime


interface TaskRepository {


    suspend fun insertTask(vararg task: Task)

    suspend fun insertTaskAndReturnId(task: Task): Int

    suspend fun updateTask(task: Task)

    suspend fun deleteTask(task: Task)

    fun observeTasksByCategory(categoryId: Int): Flow<List<Task>>

    suspend fun getTasksByCategory(categoryId: Int): List<Task>

    suspend fun updateSiblingIndex(id: Int, siblingIndex: Int)

    suspend fun updateTaskParent(id: Int, parentTaskId: Int?)

    suspend fun getTaskById(id: Int): Task?

    fun observeTasksWithScheduleByCategory(categoryId: Int): Flow<List<TaskWithSchedule>>

    fun observeScheduledCountByCategory(categoryId: Int): Flow<Int>

    fun observeAllScheduledTasksWithSchedule(): Flow<List<TaskWithSchedule>>

    suspend fun getTasksByCategoryOrdered(categoryId: Int): List<Task>

    suspend fun countChildren(taskId: Int): Int

    suspend fun setCompletedForIds(ids: List<Int>, done: Boolean)

    suspend fun getSiblings(categoryId: Int, parentId: Int): List<Task>

    suspend fun shiftSiblingsDown(categoryId: Int, parentId: Int)

    suspend fun normalizeNullParentsToRoot()


    suspend fun completeAllInCategory(categoryId: Int)

    suspend fun uncompleteAllInCategory(categoryId: Int)

    suspend fun deleteCompletedInCategory(categoryId: Int)

    suspend fun deleteAllInCategory(categoryId: Int)

    suspend fun incrementPomodoroDoneUnits(taskId: Int)
}