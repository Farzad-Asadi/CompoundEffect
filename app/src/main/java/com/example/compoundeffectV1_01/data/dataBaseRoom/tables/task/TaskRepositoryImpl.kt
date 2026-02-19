package com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task


import androidx.room.Transaction
import com.example.compoundeffectV1_01.ui.categoryScreen.TaskReorderUpdate
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override suspend fun insertTask(vararg task: Task) =
        taskDao.insertTask(*task)

    override suspend fun insertTaskAndReturnId(task: Task): Int =
        taskDao.insertTaskAndReturnId(task).toInt()

    override suspend fun updateTask(task: Task) =
            taskDao.updateTask(task)

    override suspend fun deleteTask(task: Task) =
                taskDao.deleteTask(task)

    override fun observeTasksByCategory(categoryId: Int): Flow<List<Task>> =
        taskDao.observeTasksByCategory(categoryId)

    override suspend fun getTaskById(id: Int): Task? =
        taskDao.getTaskById(id)

    override fun observeTasksWithScheduleByCategory(categoryId: Int): Flow<List<TaskWithSchedule>> =
        taskDao.observeTasksWithScheduleByCategory(categoryId)

    override fun observeScheduledCountByCategory(categoryId: Int): Flow<Int> =
        taskDao.observeScheduledCountByCategory(categoryId)

    override fun observeAllScheduledTasksWithSchedule(): Flow<List<TaskWithSchedule>> =
        taskDao.observeAllScheduledTasksWithSchedule()

    override suspend fun getTasksByCategoryOrdered(categoryId: Int): List<Task> =
        taskDao.getTasksByCategoryOrdered(categoryId)

    override suspend fun getMinOrderIndex(categoryId: Int): Int? =
            taskDao.getMinOrderIndex(categoryId)

    override suspend fun getMaxOrderIndex(categoryId: Int): Int? =
                taskDao.getMaxOrderIndex(categoryId)

    override suspend fun countChildren(taskId: Int): Int =
        taskDao.countChildren(taskId)

    override suspend fun updateTaskOrder(id: Int, orderIndex: Int) =
        taskDao.updateTaskOrder(id,orderIndex)

    override suspend fun updateTaskHierarchy(id: Int, indentLevel: Int, parentTaskId: Int?) =
        taskDao.updateTaskHierarchy(id,indentLevel,parentTaskId)

    @Transaction
    override suspend fun applyTaskReorderAndHierarchy(updates: List<TaskReorderUpdate>) {
        for (u in updates) {
            updateTaskOrder(u.id, u.orderIndex)
            updateTaskHierarchy(u.id, u.indentLevel, u.parentTaskId)
        }
    }

    override suspend fun setCompletedForIds(ids: List<Int>, done: Boolean) {
        if (ids.isEmpty()) return
        taskDao.setCompletedForIds(ids, done)
    }
}